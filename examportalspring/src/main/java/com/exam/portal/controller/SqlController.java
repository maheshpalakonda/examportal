package com.exam.portal.controller;

import com.exam.portal.model.Question;
import com.exam.portal.repository.QuestionRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class SqlController {

    private static final Set<String> FORBIDDEN_KEYWORDS = Set.of(
            "insert", "update", "delete", "drop", "alter", "create",
            "truncate", "replace", "rename", "grant", "revoke"
    );

    private static final int MAX_ROWS = 200;          // hard cap rows returned
    private static final int QUERY_TIMEOUT_SEC = 20;  // seconds

    private final DataSource dataSource;

    @Autowired private QuestionRepository questionRepository;
    @Autowired private ObjectMapper objectMapper;

    @Autowired
    public SqlController(@Qualifier("primaryDataSource") DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // ---------------------------
    // Run (debug) - SELECT only
    // ---------------------------
    @PostMapping("/api/run-sql")
    public ResponseEntity<?> runSql(@RequestBody Map<String, String> body) {
        String query = body != null ? body.get("query") : null;
        String questionIdStr = body != null ? body.get("questionId") : null;
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Empty query"));
        }

        query = query.trim();

        // Sanitize: remove comments, get first statement, enforce SELECT-only, add LIMIT
        String sanitized = removeComments(query);
        String singleStmt = firstStatement(sanitized);

        if (!isSelectOnly(singleStmt)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Only SELECT queries are allowed."));
        }

        String limitedQuery = enforceLimit(singleStmt, MAX_ROWS);

        try (Connection conn = dataSource.getConnection()) {
            // If questionId provided and setupSql exists, prepare per-question dataset in this session
            if (questionIdStr != null) {
                try {
                    Long qid = Long.parseLong(questionIdStr);
                    Question q = questionRepository.findById(qid).orElse(null);
                    if (q != null && q.getSetupSql() != null && !q.getSetupSql().trim().isEmpty()) {
                        executeSetupSql(conn, q.getSetupSql());
                    }
                } catch (NumberFormatException ignored) {}
            }

            try (Statement stmt = conn.createStatement()) {
                stmt.setMaxRows(MAX_ROWS);
                try { stmt.setQueryTimeout(QUERY_TIMEOUT_SEC); } catch (Throwable ignored) {}

                boolean hasResult = stmt.execute(limitedQuery);
                if (!hasResult) {
                    return ResponseEntity.ok(Map.of(
                            "rows", List.of(),
                            "message", "Query executed but returned no rows."
                    ));
                }

                try (ResultSet rs = stmt.getResultSet()) {
                    List<Map<String, Object>> rows = toRows(rs);
                    return ResponseEntity.ok(Map.of("rows", rows));
                }
            }

        } catch (SQLTimeoutException e) {
            return ResponseEntity.status(408).body(Map.of("message", "Query timed out"));
        } catch (SQLException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Server error: " + e.getMessage()));
        }
    }

    // ---------------------------
    // Submit (judge) - SELECT only
    // Expects Question.testCases to contain expected rows JSON
    // Supported formats:
    //   1) { "expected": [ {"col": "val"}, ... ] }
    //   2) [ {"col": "val"}, ... ]
    // ---------------------------
    @PostMapping("/api/sql/submit")
    public ResponseEntity<Map<String, Object>> submitSql(@RequestBody Map<String, String> body) {
        try {
            Long questionId = Long.parseLong(body.get("questionId"));
            String query = body.get("query");
            if (query == null || query.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("passed", false, "message", "Empty query"));
            }

            query = query.trim();

            Question q = questionRepository.findById(questionId).orElse(null);
            if (q == null) {
                return ResponseEntity.status(404).body(Map.of("passed", false, "message", "Question not found"));
            }
            if (q.getTestCases() == null || q.getTestCases().trim().isEmpty()) {
                return ResponseEntity.ok(Map.of("passed", false, "message", "No expected result configured for this SQL question."));
            }

            // Parse expected rows
            List<Map<String, Object>> expectedRows = parseExpectedRows(q.getTestCases());
            if (expectedRows == null) {
                return ResponseEntity.ok(Map.of("passed", false, "message", "Invalid expected result JSON. Use either {\"expected\": [...]} or an array of rows."));
            }

            // Sanitize and run student's query
            String sanitized = removeComments(query);
            String singleStmt = firstStatement(sanitized);
            if (!isSelectOnly(singleStmt)) {
                return ResponseEntity.badRequest().body(Map.of("passed", false, "message", "Only SELECT queries are allowed."));
            }
            String limitedQuery = enforceLimit(singleStmt, Math.max(MAX_ROWS, expectedRows.size()));

            List<Map<String, Object>> actualRows;
            try (Connection conn = dataSource.getConnection()) {
                // Prepare per-question dataset in this session using setupSql (temporary tables)
                if (q.getSetupSql() != null && !q.getSetupSql().trim().isEmpty()) {
                    executeSetupSql(conn, q.getSetupSql());
                }
                try (Statement stmt = conn.createStatement()) {
                    stmt.setMaxRows(Math.max(MAX_ROWS, expectedRows.size()));
                    try { stmt.setQueryTimeout(QUERY_TIMEOUT_SEC); } catch (Throwable ignored) {}
                    boolean hasResult = stmt.execute(limitedQuery);
                    if (!hasResult) {
                        actualRows = List.of();
                    } else {
                        try (ResultSet rs = stmt.getResultSet()) {
                            actualRows = toRows(rs);
                        }
                    }
                }
            }

            // Compare result sets (order-insensitive)
            ComparisonResult cmp = compareRowSets(expectedRows, actualRows);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("testCase", 1);
            result.put("status", cmp.passed ? "Passed" : "Wrong Answer");
            if (!cmp.passed) {
                result.put("details", cmp.message);
                result.put("yourOutput", previewRows(actualRows));
                result.put("expectedOutput", previewRows(expectedRows));
            }

            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("passed", cmp.passed);
            resp.put("message", cmp.passed ? "Result set matches expected output." : "Result set does not match expected output.");
            resp.put("results", List.of(result));
            return ResponseEntity.ok(resp);

        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("passed", false, "message", "Invalid question ID"));
        } catch (SQLTimeoutException e) {
            return ResponseEntity.status(408).body(Map.of("passed", false, "message", "Query timed out"));
        } catch (SQLException e) {
            return ResponseEntity.badRequest().body(Map.of("passed", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("passed", false, "message", "Server error: " + e.getMessage()));
        }
    }

    // ---------------------------
    // Database schema (for display in UI)
    // ---------------------------
    @GetMapping("/api/sql/schema")
    public ResponseEntity<?> getSchema() {
        try (Connection conn = dataSource.getConnection()) {
            String schemaSql = "SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() ORDER BY TABLE_NAME";
            String colsSql = "SELECT TABLE_NAME, COLUMN_NAME, DATA_TYPE FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() ORDER BY TABLE_NAME, ORDINAL_POSITION";

            Set<String> tables = new LinkedHashSet<>();
            try (PreparedStatement ps = conn.prepareStatement(schemaSql); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) tables.add(rs.getString(1));
            }

            Map<String, List<Map<String, String>>> tableCols = new LinkedHashMap<>();
            for (String t : tables) tableCols.put(t, new ArrayList<>());
            try (PreparedStatement ps = conn.prepareStatement(colsSql); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String t = rs.getString("TABLE_NAME");
                    String c = rs.getString("COLUMN_NAME");
                    String dt = rs.getString("DATA_TYPE");
                    tableCols.computeIfAbsent(t, k -> new ArrayList<>()).add(Map.of("name", c, "type", dt));
                }
            }

            List<Map<String, Object>> out = tableCols.entrySet().stream()
                    .map(e -> Map.of("table", e.getKey(), "columns", e.getValue()))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of("tables", out));
        } catch (SQLException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Server error: " + e.getMessage()));
        }
    }

    // ---------------------------
    // Helpers
    // ---------------------------
    // Execute per-question Setup SQL in the same connection, rewriting CREATE TABLE to CREATE TEMPORARY TABLE
    private void executeSetupSql(Connection conn, String setupSql) throws SQLException {
        if (setupSql == null || setupSql.trim().isEmpty()) return;
        List<String> statements = splitSqlStatements(setupSql);
        try (Statement st = conn.createStatement()) {
            for (String raw : statements) {
                String stmt = raw.trim();
                if (stmt.isEmpty()) continue;
                if (!isSetupStatementAllowed(stmt)) {
                    throw new SQLException("Disallowed statement in setup SQL: " + stmt);
                }
                stmt = rewriteCreateToTemporary(stmt);
                // If this is a CREATE (TEMPORARY) TABLE, proactively drop any existing temp table of the same name
                String createTableName = extractCreateTableName(stmt);
                if (createTableName != null) {
                    try {
                        st.execute("DROP TEMPORARY TABLE IF EXISTS " + createTableName);
                    } catch (SQLException ignore) {}
                }
                st.execute(stmt);
            }
        }
    }

    private String rewriteCreateToTemporary(String sql) {
        // Case-insensitive replace of "CREATE TABLE" with "CREATE TEMPORARY TABLE"
        return sql.replaceAll("(?i)CREATE\\s+TABLE", "CREATE TEMPORARY TABLE");
    }

    private String extractCreateTableName(String sql) {
        // Extract table name from a CREATE TABLE statement
        // Matches: CREATE [TEMPORARY] TABLE [`]name[`]
        String s = sql.trim();
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("(?is)^\\s*create\\s+(?:temporary\\s+)?table\\s+`?([a-zA-Z0-9_]+)`?.*");
        java.util.regex.Matcher m = p.matcher(s);
        if (m.matches()) {
            return m.group(1);
        }
        return null;
    }

    private boolean isSetupStatementAllowed(String sql) {
        String s = sql.trim().toLowerCase(Locale.ROOT);
        if (s.startsWith("create table") || s.startsWith("create temporary table")) return true;
        if (s.startsWith("insert into")) return true;
        if (s.startsWith("create index") || s.startsWith("create unique index")) return true;
        // Disallow dropping/altering persistent objects and DML that mutates outside temp context
        if (s.startsWith("drop ") || s.startsWith("alter ") || s.startsWith("update ") || s.startsWith("delete ")) return false;
        // Allow harmless SETs if needed
        if (s.startsWith("set ")) return true;
        // By default block unknown types
        return false;
    }

    private List<String> splitSqlStatements(String sql) {
        // Naive split by semicolon. Authors should avoid semicolons in string literals.
        String[] parts = sql.split(";");
        List<String> out = new ArrayList<>();
        for (String p : parts) out.add(p);
        return out;
    }

    private List<Map<String, Object>> toRows(ResultSet rs) throws SQLException {
        List<Map<String, Object>> rows = new ArrayList<>();
        ResultSetMetaData md = rs.getMetaData();
        int cols = md.getColumnCount();
        while (rs.next()) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (int i = 1; i <= cols; i++) {
                String label = md.getColumnLabel(i);
                Object val = rs.getObject(i);
                row.put(label, val);
            }
            rows.add(row);
        }
        return rows;
    }

    private String removeComments(String sql) {
        // Remove /* ... */ and -- ... end-of-line comments
        String noBlock = sql.replaceAll("/\\*.*?\\*/", " ");
        return noBlock.replaceAll("--.*?(\\r?\\n)", " ");
    }

    private String firstStatement(String sql) {
        String s = sql.trim();
        int semi = s.indexOf(';');
        if (semi >= 0) {
            s = s.substring(0, semi);
        }
        return s;
    }

    private boolean isSelectOnly(String sql) {
        String s = sql.trim().toLowerCase(Locale.ROOT);
        if (!s.startsWith("select")) return false;
        if (s.contains(";")) return false; // single statement only
        for (String forbid : FORBIDDEN_KEYWORDS) {
            // crude containment check; deliberately blocks any mention
            if (s.contains(forbid + " ") || s.contains(" " + forbid + "(") || s.contains("\n" + forbid + " ")) {
                return false;
            }
        }
        // block dangerous INTO OUTFILE / DUMPFILE
        if (s.contains(" into outfile") || s.contains(" into dumpfile")) return false;
        return true;
    }

    private String enforceLimit(String sql, int maxRows) {
        String s = sql.trim();
        if (!s.toLowerCase(Locale.ROOT).matches(".*\\blimit\\b.*")) {
            s = s + " LIMIT " + maxRows;
        }
        return s;
    }

    private List<Map<String, Object>> parseExpectedRows(String json) {
        try {
            json = json.trim();
            if (json.startsWith("{")) {
                Map<String, Object> obj = objectMapper.readValue(json, new TypeReference<>() {});
                Object arr = obj.get("expected");
                if (arr instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> rows = (List<Map<String, Object>>) arr;
                    return rows;
                }
                return null;
            } else if (json.startsWith("[")) {
                return objectMapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {});
            }
        } catch (Exception ignored) {}
        return null;
    }

    static class ComparisonResult {
        boolean passed; String message;
        ComparisonResult(boolean p, String m) { passed = p; message = m; }
    }

    private ComparisonResult compareRowSets(List<Map<String, Object>> expected, List<Map<String, Object>> actual) {
        // Normalize: lower-case keys, stringify values trimmed, ignore order
        List<Map<String, String>> normExpected = expected.stream().map(this::normalizeRow).collect(Collectors.toList());
        List<Map<String, String>> normActual = actual.stream().map(this::normalizeRow).collect(Collectors.toList());

        if (normExpected.isEmpty() && normActual.isEmpty()) {
            return new ComparisonResult(true, "Both result sets empty");
        }

        // Columns must match exactly
        Set<String> expCols = normExpected.isEmpty() ? (normActual.isEmpty()? Set.of() : normActual.get(0).keySet()) : normExpected.get(0).keySet();
        for (Map<String, String> r : normExpected) if (!r.keySet().equals(expCols)) return new ComparisonResult(false, "Expected columns do not match across expected rows");
        for (Map<String, String> r : normActual) if (!r.keySet().equals(expCols)) return new ComparisonResult(false, "Your result columns differ from expected: " + normActual.get(0).keySet() + " vs " + expCols);

        // Compare as multisets of row JSON
        Map<String, Long> expCounts = normExpected.stream().collect(Collectors.groupingBy(this::rowKey, Collectors.counting()));
        Map<String, Long> actCounts = normActual.stream().collect(Collectors.groupingBy(this::rowKey, Collectors.counting()));

        if (expCounts.equals(actCounts)) return new ComparisonResult(true, "Match");

        // Build diff message (first few diffs)
        List<String> diffs = new ArrayList<>();
        for (String k : expCounts.keySet()) {
            long e = expCounts.getOrDefault(k, 0L);
            long a = actCounts.getOrDefault(k, 0L);
            if (e != a) diffs.add(String.format(Locale.ROOT, "Row %s expected %d time(s), got %d", k, e, a));
            if (diffs.size() >= 5) break;
        }
        for (String k : actCounts.keySet()) {
            if (!expCounts.containsKey(k) && diffs.size() < 5) diffs.add("Unexpected row " + k);
        }
        return new ComparisonResult(false, String.join("; ", diffs));
    }

    private Map<String, String> normalizeRow(Map<String, Object> row) {
        // Lower-case keys, stringify values, trim whitespace
        Map<String, String> out = new TreeMap<>(); // sorted keys for stable rowKey
        for (Map.Entry<String, Object> e : row.entrySet()) {
            String k = e.getKey() == null ? "" : e.getKey().toLowerCase(Locale.ROOT);
            Object v = e.getValue();
            String s;
            if (v == null) s = "NULL";
            else if (v instanceof Double || v instanceof Float) s = String.format(Locale.ROOT, "%s", v);
            else s = String.valueOf(v);
            out.put(k, s.trim());
        }
        return out;
    }

    private String rowKey(Map<String, String> row) {
        // Canonical JSON of sorted map
        return row.entrySet().stream()
                .map(e -> e.getKey() + ":" + e.getValue())
                .collect(Collectors.joining(",", "{", "}"));
    }

    private List<Map<String, Object>> previewRows(List<Map<String, Object>> rows) {
        return rows.stream().limit(10).collect(Collectors.toList());
    }
}
