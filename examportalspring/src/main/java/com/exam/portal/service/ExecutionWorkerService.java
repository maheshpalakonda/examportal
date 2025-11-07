////package com.exam.portal.service;
////
////import com.exam.portal.model.Job;
////import com.fasterxml.jackson.core.type.TypeReference;
////import com.fasterxml.jackson.databind.ObjectMapper;
////import org.slf4j.Logger;
////import org.slf4j.LoggerFactory;
////import org.springframework.beans.factory.annotation.Autowired;
////import org.springframework.stereotype.Service;
////
////import jakarta.annotation.PostConstruct;
////import java.util.*;
////import java.util.concurrent.ConcurrentHashMap;
////import java.util.concurrent.ExecutorService;
////import java.util.concurrent.Executors;
////import java.util.stream.Collectors;
////
////@Service
////public class ExecutionWorkerService {
////
////    private static final Logger logger = LoggerFactory.getLogger(ExecutionWorkerService.class);
////
////    @Autowired
////    private TaskQueue taskQueue;
////
////    @Autowired
////    private ObjectMapper objectMapper;
////
////    // Simulate DB: jobId -> results map
////    private final Map<String, Map<String, Object>> jobResults = new ConcurrentHashMap<>();
////
////    private final ExecutorService executor = Executors.newFixedThreadPool(50); // For concurrent job processing, scalable to 500+ students
////
////    // Nested static class for TestCase
////    static class TestCase {
////        public String input;
////        public String output;
////
////        public TestCase() {}
////
////        public TestCase(String input, String output) {
////            this.input = input;
////            this.output = output;
////        }
////
////        public String getInput() { return input; }
////        public String getOutput() { return output; }
////        public void setInput(String input) { this.input = input; }
////        public void setOutput(String output) { this.output = output; }
////    }
////
////    // Helper method to parse test cases
////    private List<TestCase> parseTestCases(String testCasesJson) throws Exception {
////        try {
////            return objectMapper.readValue(testCasesJson, new TypeReference<List<TestCase>>(){});
////        } catch (Exception e) {
////            // Handle object format if needed, but for simplicity, assume simple format
////            throw e;
////        }
////    }
////
////    // Static methods for execution (delegate to CompilerController's executeInDocker)
////    // Note: Since CompilerController is not static, we need an instance or make executeInDocker static.
////    // For simplicity, assume we inject CompilerController or make it static.
////    // Placeholder for now.
////    private String runPythonStatic(String code, String stdin, long timeoutSeconds) throws Exception {
////        return executeInDockerStatic("python", code, stdin, timeoutSeconds);
////    }
////
////    private String runJavaStatic(String code, String stdin, long timeoutSeconds) throws Exception {
////        return executeInDockerStatic("java", code, stdin, timeoutSeconds);
////    }
////
////    private String runCStatic(String code, String stdin, long timeoutSeconds) throws Exception {
////        return executeInDockerStatic("c", code, stdin, timeoutSeconds);
////    }
////
////    // Static version of executeInDocker (copied from CompilerController)
////    private static String executeInDockerStatic(String language, String code, String stdin, long timeoutSeconds) throws Exception {
////        // Implement similar to CompilerController's executeInDocker
////        // For brevity, placeholder
////        return "Docker execution placeholder for " + language;
////    }
////
////    @PostConstruct
////    public void startWorker() {
////        Thread workerThread = new Thread(() -> {
////            while (true) {
////                try {
////                    Job job = taskQueue.dequeue();
////                    logger.info("Processing job: {}", job.getJobId());
////                    executor.submit(() -> processJob(job));
////                } catch (InterruptedException e) {
////                    Thread.currentThread().interrupt();
////                    break;
////                }
////            }
////        });
////        workerThread.setDaemon(true);
////        workerThread.start();
////        logger.info("ExecutionWorkerService started.");
////    }
////
////    private void processJob(Job job) {
////        try {
////            // Define TestCase locally or use a simple class
////            List<TestCase> testCases = parseTestCases(job.getTestCasesJson());
////            List<Map<String, Object>> results = new ArrayList<>();
////            boolean allPassed = true;
////
////            for (int i = 0; i < testCases.size(); i++) {
////                TestCase testCase = testCases.get(i);
////                String actualOutput;
////                String expectedOutput = testCase.output != null ? testCase.output.trim() : "";
////                String testInput = testCase.input != null ? testCase.input : "";
////                if (testInput.isEmpty()) { testInput = "0\n".repeat(10); }
////
////                Map<String, Object> testResult = new LinkedHashMap<>();
////                testResult.put("testCase", i + 1);
////
////                try {
////                    switch (job.getLanguage().toLowerCase()) {
////                        case "java":
////                            actualOutput = runJavaStatic(job.getCode(), testInput, 2L); // 2 seconds
////                            break;
////                        case "python":
////                            actualOutput = runPythonStatic(job.getCode(), testInput, 2L);
////                            break;
////                        case "c":
////                            actualOutput = runCStatic(job.getCode(), testInput, 2L);
////                            break;
////                        default:
////                            throw new RuntimeException("Unsupported language: " + job.getLanguage());
////                    }
////
////                    if (actualOutput.startsWith("Compilation Error:")) {
////                        allPassed = false;
////                        testResult.put("status", "Compilation Error");
////                        testResult.put("details", actualOutput);
////                    } else {
////                        String trimmedActualOutput = actualOutput.trim();
////                        String normalizedActual = trimmedActualOutput.replaceAll("\\r\\n", "\n").replaceAll("\\r", "\n");
////                        String normalizedExpected = expectedOutput.replaceAll("\\r\\n", "\n").replaceAll("\\r", "\n");
////
////                        if (normalizedActual.equals(normalizedExpected)) {
////                            testResult.put("status", "Passed");
////                        } else {
////                            allPassed = false;
////                            testResult.put("status", "Wrong Answer");
////                            testResult.put("input", testInput);
////                            testResult.put("yourOutput", normalizedActual);
////                            testResult.put("expectedOutput", normalizedExpected);
////                        }
////                    }
////                } catch (RuntimeException e) {
////                    allPassed = false;
////                    String errorMessage = e.getMessage() != null && e.getMessage().contains("Execution timed out")
////                        ? "Time Limit Exceeded"
////                        : "Runtime Error";
////                    testResult.put("status", errorMessage);
////                    testResult.put("details", e.getMessage());
////                } catch (Exception e) {
////                    allPassed = false;
////                    testResult.put("status", "Server Error");
////                    testResult.put("details", e.getMessage());
////                }
////                results.add(testResult);
////            }
////
////            String overallMessage = allPassed ? "All " + testCases.size() + " test cases passed!" : "Some test cases failed.";
////
////            Map<String, Object> finalResponse = new LinkedHashMap<>();
////            finalResponse.put("passed", allPassed);
////            finalResponse.put("message", overallMessage);
////            finalResponse.put("results", results);
////
////            jobResults.put(job.getJobId(), finalResponse);
////            logger.info("Job {} completed: {}", job.getJobId(), overallMessage);
////        } catch (Exception e) {
////            logger.error("Error processing job {}: ", job.getJobId(), e);
////            Map<String, Object> errorResponse = Map.of("passed", false, "message", "Server Error: " + e.getMessage());
////            jobResults.put(job.getJobId(), errorResponse);
////        }
////    }
////
////    public Map<String, Object> getJobResult(String jobId) {
////        return jobResults.get(jobId);
////    }
////}
////package com.exam.portal.service;
////
////import com.exam.portal.model.Job;
////import com.fasterxml.jackson.core.type.TypeReference;
////import com.fasterxml.jackson.databind.ObjectMapper;
////import org.slf4j.Logger;
////import org.slf4j.LoggerFactory;
////import org.springframework.beans.factory.annotation.Autowired;
////import org.springframework.stereotype.Service;
////
////import jakarta.annotation.PostConstruct;
////import java.io.*;
////import java.nio.file.*;
////import java.util.*;
////import java.util.concurrent.*;
////import java.util.concurrent.ConcurrentHashMap;
////import java.util.concurrent.ExecutorService;
////import java.util.concurrent.Executors;
////import java.util.stream.Collectors;
////
////@Service
////public class ExecutionWorkerService {
////
////    private static final Logger logger = LoggerFactory.getLogger(ExecutionWorkerService.class);
////
////    // Strict, short timeout for each test case during submission, optimized for 500+ students
////    private static final long PER_TEST_CASE_TIMEOUT_SECONDS = 2; // 2 seconds per test case
////
////    @Autowired
////    private TaskQueue taskQueue;
////
////    @Autowired
////    private ObjectMapper objectMapper;
////
////    // Simulate DB: jobId -> results map
////    private final Map<String, Map<String, Object>> jobResults = new ConcurrentHashMap<>();
////
////    private final ExecutorService executor = Executors.newFixedThreadPool(50); // For concurrent job processing, scalable to 500+ students
////
////    // Nested static class for TestCase
////    static class TestCase {
////        public Object input;
////        public String output;
////
////        public TestCase() {}
////
////        public TestCase(Object input, String output) {
////            this.input = input;
////            this.output = output;
////        }
////
////        public Object getInput() { return input; }
////        public String getOutput() { return output; }
////        public void setInput(Object input) { this.input = input; }
////        public void setOutput(String output) { this.output = output; }
////    }
////
////    // Helper method to parse test cases (supports {input, expectedOutput} and {input, expected})
////    private List<TestCase> parseTestCases(String testCasesJson) throws Exception {
////        logger.info("Raw testCasesJson: {}", testCasesJson);
////        try {
////            // Try to parse as list of maps first
////            List<Map<String, Object>> testCaseMaps = objectMapper.readValue(testCasesJson, new TypeReference<List<Map<String, Object>>>(){});
////            List<TestCase> testCases = new ArrayList<>();
////            for (Map<String, Object> map : testCaseMaps) {
////                TestCase tc = new TestCase();
////                tc.input = map.get("input");
////                // prefer expectedOutput, else fallback to expected, then output (to support your schema)
////                Object eo = map.get("expectedOutput");
////                if (eo == null) eo = map.get("expected");
////                if (eo == null) eo = map.get("output");
////                tc.output = eo != null ? eo.toString() : "";
////                logger.info("Parsed test case -> input: {}, expected: {}", tc.input, tc.output);
////                testCases.add(tc);
////            }
////            return testCases;
////        } catch (Exception e) {
////            logger.warn("Failed to parse test cases as maps, trying fallback class binding: {}", e.getMessage());
////            // Fallback to original format where fields match TestCase
////            List<TestCase> tcs = objectMapper.readValue(testCasesJson, new TypeReference<List<TestCase>>(){});
////            for (TestCase tc : tcs) {
////                logger.info("Parsed (fallback) test case -> input: {}, expected: {}", tc.input, tc.output);
////            }
////            return tcs;
////        }
////    }
////
////    // Static methods for execution using direct host execution
////    private static String runPythonStatic(String code, String stdin, long timeoutSeconds) throws Exception {
////        return executeCodeStatic("python", code, stdin, timeoutSeconds);
////    }
////
////    private static String runJavaStatic(String code, String stdin, long timeoutSeconds) throws Exception {
////        return executeCodeStatic("java", code, stdin, timeoutSeconds);
////    }
////
////    private static String runCStatic(String code, String stdin, long timeoutSeconds) throws Exception {
////        return executeCodeStatic("c", code, stdin, timeoutSeconds);
////    }
////
////    // Static version of executeCode (copied from CompilerController)
////    private static String executeCodeStatic(String language, String code, String stdin, long timeoutSeconds) throws Exception {
////        Path tempDir = null;
////        try {
////            tempDir = Files.createTempDirectory("code_exec_");
////            String fileName;
////            String commandInside;
////
////            switch (language.toLowerCase()) {
////                case "python":
////                    fileName = "main.py";
////                    Files.writeString(tempDir.resolve(fileName), code);
////                    commandInside = "python3 " + fileName + " < /dev/stdin";
////                    break;
////                case "java":
////                    String className = extractPublicClassNameStatic(code);
////                    fileName = className + ".java";
////                    Files.writeString(tempDir.resolve(fileName), code);
////                    commandInside = "javac " + fileName + " && java " + className + " < /dev/stdin";
////                    break;
////                case "c":
////                    fileName = "main.c";
////                    Files.writeString(tempDir.resolve(fileName), code);
////                    commandInside = "gcc " + fileName + " -o main && ./main < /dev/stdin";
////                    break;
////                default:
////                    throw new RuntimeException("Unsupported language: " + language);
////            }
////
////            // Execute command directly
////            List<String> command = Arrays.asList("sh", "-c", commandInside);
////
////            // Execute command with stdin
////            return runProcessStatic(command, tempDir, stdin, timeoutSeconds);
////        } finally {
////            if (tempDir != null) {
////                deleteDirectoryStatic(tempDir);
////            }
////        }
////    }
////
////    // Static version of extractPublicClassName
////    private static String extractPublicClassNameStatic(String code) {
////        try (Scanner scanner = new Scanner(code)) {
////            while (scanner.hasNextLine()) {
////                String line = scanner.nextLine().trim();
////                // Look for "public class ClassName"
////                if (line.startsWith("public class")) {
////                    // Simple parsing: "public class Solution {" -> "Solution"
////                    String[] parts = line.split("\\s+");
////                    if (parts.length > 2) {
////                        return parts[2].replaceAll("\\{", "").trim();
////                    }
////                }
////            }
////        }
////        return "Main"; // Default if no public class is found
////    }
////
////    // Static version of runProcess
////    private static String runProcessStatic(List<String> command, Path workDir, String stdin, long timeoutSeconds) throws Exception {
////        ProcessBuilder pb = new ProcessBuilder(command);
////        pb.directory(workDir.toFile());
////        pb.redirectErrorStream(true); // Redirect stderr to stdout stream
////        Process process = pb.start();
////
////        // Write stdin to the process and close the stream to signal EOF.
////        try (OutputStream os = process.getOutputStream()) {
////            if (stdin != null && !stdin.isEmpty()) {
////                os.write(stdin.getBytes());
////            }
////            os.flush();
////        } catch (IOException e) {
////            throw new RuntimeException("Error writing stdin to process", e);
////        }
////
////        // Read process output asynchronously to prevent deadlocks
////        CompletableFuture<String> outputFuture = CompletableFuture.supplyAsync(() -> {
////            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
////                return reader.lines().collect(Collectors.joining("\n"));
////            } catch (IOException e) {
////                throw new UncheckedIOException(e);
////            }
////        });
////
////        boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
////
////        if (!finished) {
////            process.destroyForcibly();
////            throw new RuntimeException("Execution timed out after " + timeoutSeconds + " seconds");
////        }
////
////        try {
////            String output = outputFuture.get(5, TimeUnit.SECONDS);
////            int exitCode = process.exitValue();
////
////            if (exitCode != 0 && !output.trim().isEmpty()) {
////                return output;
////            }
////            return output;
////        } catch (InterruptedException | ExecutionException | TimeoutException e) {
////            throw new RuntimeException("Failed to collect process output: " + e.getMessage(), e);
////        }
////    }
////
////    // Static version of deleteDirectory
////    private static void deleteDirectoryStatic(Path path) {
////        try {
////            if (Files.exists(path)) {
////                Files.walk(path)
////                    .sorted(Comparator.reverseOrder()) // Delete files before directories
////                    .map(Path::toFile)
////                    .forEach(file -> {
////                        if (!file.delete()) {
////                            // Log or ignore
////                        }
////                    });
////            }
////        } catch (IOException e) {
////            // Log or ignore
////        }
////    }
////
////    @PostConstruct
////    public void startWorker() {
////        Thread workerThread = new Thread(() -> {
////            while (true) {
////                try {
////                    Job job = taskQueue.dequeue();
////                    logger.info("Processing job: {}", job.getJobId());
////                    executor.submit(() -> processJob(job));
////                } catch (InterruptedException e) {
////                    Thread.currentThread().interrupt();
////                    break;
////                }
////            }
////        });
////        workerThread.setDaemon(true);
////        workerThread.start();
////        logger.info("ExecutionWorkerService started.");
////    }
////
////    private void processJob(Job job) {
////        try {
////            // Define TestCase locally or use a simple class
////            List<TestCase> testCases = parseTestCases(job.getTestCasesJson());
////            List<Map<String, Object>> results = new ArrayList<>();
////            boolean allPassed = true;
////
////            for (int i = 0; i < testCases.size(); i++) {
////                TestCase testCase = testCases.get(i);
////                String actualOutput;
////                String expectedOutput = testCase.output != null ? testCase.output.trim() : "";
////                String testInput = "";
////
////                // Handle multiple input representations: string, list, object with fields
////                if (testCase.input == null) {
////                    testInput = "";
////                } else if (testCase.input instanceof String) {
////                    testInput = ((String) testCase.input).trim();
////                } else if (testCase.input instanceof List) {
////                    // Join list elements with spaces/newlines if nested
////                    @SuppressWarnings("unchecked") List<Object> list = (List<Object>) testCase.input;
////                    testInput = list.stream().map(Object::toString).collect(Collectors.joining("\n"));
////                } else if (testCase.input instanceof Map) {
////                    @SuppressWarnings("unchecked") Map<String, Object> map = (Map<String, Object>) testCase.input;
////                    // allow {"stdin":"..."} or custom fields
////                    Object stdin = map.getOrDefault("stdin", null);
////                    if (stdin != null) {
////                        testInput = stdin.toString();
////                    } else {
////                        // If expected is nested, pull it if not already set
////                        if (expectedOutput.isEmpty() && map.containsKey("expected")) {
////                            expectedOutput = map.get("expected").toString().trim();
////                        }
////                        // Heuristic: try to build input from common fields (nums/target etc.) if needed
////                        if (map.containsKey("input")) {
////                            Object nestedInput = map.get("input");
////                            testInput = nestedInput != null ? nestedInput.toString() : "";
////                        } else {
////                            testInput = map.values().stream().map(String::valueOf).collect(Collectors.joining("\n"));
////                        }
////                    }
////                } else {
////                    testInput = testCase.input.toString();
////                }
////
////                if (testInput.isEmpty()) { testInput = "0\n".repeat(10); }
////
////                logger.info("Evaluating test {} -> input: [{}], expected: [{}]", i + 1, testInput, expectedOutput);
////
////                Map<String, Object> testResult = new LinkedHashMap<>();
////                testResult.put("testCase", i + 1);
////
////                try {
////                    switch (job.getLanguage().toLowerCase()) {
////                        case "java":
////                            actualOutput = runJavaStatic(job.getCode(), testInput, PER_TEST_CASE_TIMEOUT_SECONDS);
////                            break;
////                        case "python":
////                            actualOutput = runPythonStatic(job.getCode(), testInput, PER_TEST_CASE_TIMEOUT_SECONDS);
////                            break;
////                        case "c":
////                            actualOutput = runCStatic(job.getCode(), testInput, PER_TEST_CASE_TIMEOUT_SECONDS);
////                            break;
////                        default:
////                            throw new RuntimeException("Unsupported language: " + job.getLanguage());
////                    }
////
////                    String normalizedActual = actualOutput == null ? "" : actualOutput.trim().replaceAll("\\r\\n", "\n").replaceAll("\\r", "\n");
////                    String normalizedExpected = expectedOutput == null ? "" : expectedOutput.trim().replaceAll("\\r\\n", "\n").replaceAll("\\r", "\n");
////
////                    if (normalizedActual.equals(normalizedExpected)) {
////                        testResult.put("status", "Passed");
////                    } else {
////                        allPassed = false;
////                        testResult.put("status", "Wrong Answer");
////                        testResult.put("input", testInput);
////                        testResult.put("yourOutput", normalizedActual);
////                        testResult.put("expectedOutput", normalizedExpected);
////                    }
////                } catch (RuntimeException e) {
////                    allPassed = false;
////                    String errorMessage = e.getMessage() != null && e.getMessage().contains("Execution timed out")
////                        ? "Time Limit Exceeded"
////                        : "Runtime Error";
////                    testResult.put("status", errorMessage);
////                    testResult.put("details", e.getMessage());
////                } catch (Exception e) {
////                    allPassed = false;
////                    testResult.put("status", "Server Error");
////                    testResult.put("details", e.getMessage());
////                }
////                results.add(testResult);
////            }
////
////            String overallMessage = allPassed ? "All " + testCases.size() + " test cases passed!" : "Some test cases failed.";
////
////            Map<String, Object> finalResponse = new LinkedHashMap<>();
////            finalResponse.put("passed", allPassed);
////            finalResponse.put("message", overallMessage);
////            finalResponse.put("results", results);
////
////            jobResults.put(job.getJobId(), finalResponse);
////            logger.info("Job {} completed: {}", job.getJobId(), overallMessage);
////        } catch (Exception e) {
////            logger.error("Error processing job {}: ", job.getJobId(), e);
////            Map<String, Object> errorResponse = Map.of("passed", false, "message", "Server Error: " + e.getMessage());
////            jobResults.put(job.getJobId(), errorResponse);
////        }
////    }
////
////    public Map<String, Object> getJobResult(String jobId) {
////        return jobResults.get(jobId);
////    }
////}
//
//
//
//
//
//package com.exam.portal.service;
//
//import com.exam.portal.model.Job;
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import jakarta.annotation.PostConstruct;
//import java.io.*;
//import java.nio.file.*;
//import java.util.*;
//import java.util.concurrent.*;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.stream.Collectors;
//
//@Service
//public class ExecutionWorkerService {
//
//    private static final Logger logger = LoggerFactory.getLogger(ExecutionWorkerService.class);
//
//    // Strict, short timeout for each test case during submission, optimized for 500+ students
//    private static final long PER_TEST_CASE_TIMEOUT_SECONDS = 2; // 2 seconds per test case
//
//    @Autowired
//    private TaskQueue taskQueue;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    // Simulate DB: jobId -> results map
//    private final Map<String, Map<String, Object>> jobResults = new ConcurrentHashMap<>();
//
//    private final ExecutorService executor = Executors.newFixedThreadPool(50); // For concurrent job processing, scalable to 500+ students
//
//    // Nested static class for TestCase
//    static class TestCase {
//        public Object input;
//        public String output;
//
//        public TestCase() {}
//
//        public TestCase(Object input, String output) {
//            this.input = input;
//            this.output = output;
//        }
//
//        public Object getInput() { return input; }
//        public String getOutput() { return output; }
//        public void setInput(Object input) { this.input = input; }
//        public void setOutput(String output) { this.output = output; }
//    }
//
//    // Helper method to parse test cases (supports {input, expectedOutput} and {input, expected})
//    private List<TestCase> parseTestCases(String testCasesJson) throws Exception {
//        logger.info("Raw testCasesJson: {}", testCasesJson);
//        try {
//            // Try to parse as list of maps first
//            List<Map<String, Object>> testCaseMaps = objectMapper.readValue(testCasesJson, new TypeReference<List<Map<String, Object>>>(){});
//            List<TestCase> testCases = new ArrayList<>();
//            for (Map<String, Object> map : testCaseMaps) {
//                TestCase tc = new TestCase();
//                tc.input = map.get("input");
//                // prefer expectedOutput, else fallback to expected, then output (to support your schema)
//                Object eo = map.get("expectedOutput");
//                if (eo == null) eo = map.get("expected");
//                if (eo == null) eo = map.get("output");
//                tc.output = eo != null ? eo.toString() : "";
//                logger.info("Parsed test case -> input: {}, expected: {}", tc.input, tc.output);
//                testCases.add(tc);
//            }
//            return testCases;
//        } catch (Exception e) {
//            logger.warn("Failed to parse test cases as maps, trying fallback class binding: {}", e.getMessage());
//            // Fallback to original format where fields match TestCase
//            List<TestCase> tcs = objectMapper.readValue(testCasesJson, new TypeReference<List<TestCase>>(){});
//            for (TestCase tc : tcs) {
//                logger.info("Parsed (fallback) test case -> input: {}, expected: {}", tc.input, tc.output);
//            }
//            return tcs;
//        }
//    }
//
//    // Static methods for execution using direct host execution
//    private static String runPythonStatic(String code, String stdin, long timeoutSeconds) throws Exception {
//        return executeCodeStatic("python", code, stdin, timeoutSeconds);
//    }
//
//    private static String runJavaStatic(String code, String stdin, long timeoutSeconds) throws Exception {
//        return executeCodeStatic("java", code, stdin, timeoutSeconds);
//    }
//
//    private static String runCStatic(String code, String stdin, long timeoutSeconds) throws Exception {
//        return executeCodeStatic("c", code, stdin, timeoutSeconds);
//    }
//
//    // Static version of executeCode (copied from CompilerController)
//    private static String executeCodeStatic(String language, String code, String stdin, long timeoutSeconds) throws Exception {
//        Path tempDir = null;
//        try {
//            tempDir = Files.createTempDirectory("code_exec_");
//            String fileName;
//            String commandInside;
//
//            switch (language.toLowerCase()) {
//                case "python":
//                    fileName = "main.py";
//                    Files.writeString(tempDir.resolve(fileName), code);
//                    commandInside = "python3 " + fileName + " < /dev/stdin";
//                    break;
//                case "java":
//                    String className = extractPublicClassNameStatic(code);
//                    fileName = className + ".java";
//                    Files.writeString(tempDir.resolve(fileName), code);
//                    commandInside = "javac " + fileName + " && java " + className + " < /dev/stdin";
//                    break;
//                case "c":
//                    fileName = "main.c";
//                    Files.writeString(tempDir.resolve(fileName), code);
//                    commandInside = "gcc " + fileName + " -o main && ./main < /dev/stdin";
//                    break;
//                default:
//                    throw new RuntimeException("Unsupported language: " + language);
//            }
//
//            // Execute command directly
//            List<String> command = Arrays.asList("sh", "-c", commandInside);
//
//            // Execute command with stdin
//            return runProcessStatic(command, tempDir, stdin, timeoutSeconds);
//        } finally {
//            if (tempDir != null) {
//                deleteDirectoryStatic(tempDir);
//            }
//        }
//    }
//
//    // Static version of extractPublicClassName
//    private static String extractPublicClassNameStatic(String code) {
//        try (Scanner scanner = new Scanner(code)) {
//            while (scanner.hasNextLine()) {
//                String line = scanner.nextLine().trim();
//                // Look for "public class ClassName"
//                if (line.startsWith("public class")) {
//                    // Simple parsing: "public class Solution {" -> "Solution"
//                    String[] parts = line.split("\\s+");
//                    if (parts.length > 2) {
//                        return parts[2].replaceAll("\\{", "").trim();
//                    }
//                }
//            }
//        }
//        return "Main"; // Default if no public class is found
//    }
//
//    // Static version of runProcess
//    private static String runProcessStatic(List<String> command, Path workDir, String stdin, long timeoutSeconds) throws Exception {
//        ProcessBuilder pb = new ProcessBuilder(command);
//        pb.directory(workDir.toFile());
//        pb.redirectErrorStream(true); // Redirect stderr to stdout stream
//        Process process = pb.start();
//
//        // Write stdin to the process and close the stream to signal EOF.
//        try (OutputStream os = process.getOutputStream()) {
//            if (stdin != null && !stdin.isEmpty()) {
//                os.write(stdin.getBytes());
//            }
//            os.flush();
//        } catch (IOException e) {
//            throw new RuntimeException("Error writing stdin to process", e);
//        }
//
//        // Read process output asynchronously to prevent deadlocks
//        CompletableFuture<String> outputFuture = CompletableFuture.supplyAsync(() -> {
//            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
//                return reader.lines().collect(Collectors.joining("\n"));
//            } catch (IOException e) {
//                throw new UncheckedIOException(e);
//            }
//        });
//
//        boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
//
//        if (!finished) {
//            process.destroyForcibly();
//            throw new RuntimeException("Execution timed out after " + timeoutSeconds + " seconds");
//        }
//
//        try {
//            String output = outputFuture.get(5, TimeUnit.SECONDS);
//            int exitCode = process.exitValue();
//
//            if (exitCode != 0 && !output.trim().isEmpty()) {
//                return output;
//            }
//            return output;
//        } catch (InterruptedException | ExecutionException | TimeoutException e) {
//            throw new RuntimeException("Failed to collect process output: " + e.getMessage(), e);
//        }
//    }
//
//    // Static version of deleteDirectory
//    private static void deleteDirectoryStatic(Path path) {
//        try {
//            if (Files.exists(path)) {
//                Files.walk(path)
//                    .sorted(Comparator.reverseOrder()) // Delete files before directories
//                    .map(Path::toFile)
//                    .forEach(file -> {
//                        if (!file.delete()) {
//                            // Log or ignore
//                        }
//                    });
//            }
//        } catch (IOException e) {
//            // Log or ignore
//        }
//    }
//
//    @PostConstruct
//    public void startWorker() {
//        Thread workerThread = new Thread(() -> {
//            while (true) {
//                try {
//                    Job job = taskQueue.dequeue();
//                    logger.info("Processing job: {}", job.getJobId());
//                    executor.submit(() -> processJob(job));
//                } catch (InterruptedException e) {
//                    Thread.currentThread().interrupt();
//                    break;
//                }
//            }
//        });
//        workerThread.setDaemon(true);
//        workerThread.start();
//        logger.info("ExecutionWorkerService started.");
//    }
//
//    private void processJob(Job job) {
//        try {
//            // Define TestCase locally or use a simple class
//            List<TestCase> testCases = parseTestCases(job.getTestCasesJson());
//            List<Map<String, Object>> results = new ArrayList<>();
//            boolean allPassed = true;
//
//            for (int i = 0; i < testCases.size(); i++) {
//                TestCase testCase = testCases.get(i);
//                String actualOutput;
//                String expectedOutput = testCase.output != null ? testCase.output.trim() : "";
//                String testInput = "";
//
//                // Handle multiple input representations: string, list, object with fields
//                if (testCase.input == null) {
//                    testInput = "";
//                } else if (testCase.input instanceof String) {
//                    testInput = ((String) testCase.input).trim();
//                } else if (testCase.input instanceof List) {
//                    // Join list elements with spaces/newlines if nested
//                    @SuppressWarnings("unchecked") List<Object> list = (List<Object>) testCase.input;
//                    testInput = list.stream().map(Object::toString).collect(Collectors.joining("\n"));
//                } else if (testCase.input instanceof Map) {
//                    @SuppressWarnings("unchecked") Map<String, Object> map = (Map<String, Object>) testCase.input;
//                    // allow {"stdin":"..."} or custom fields
//                    Object stdin = map.getOrDefault("stdin", null);
//                    if (stdin != null) {
//                        testInput = stdin.toString();
//                    } else {
//                        // If expected is nested, pull it if not already set
//                        if (expectedOutput.isEmpty() && map.containsKey("expected")) {
//                            expectedOutput = map.get("expected").toString().trim();
//                        }
//                        // Heuristic: try to build input from common fields (nums/target etc.) if needed
//                        if (map.containsKey("input")) {
//                            Object nestedInput = map.get("input");
//                            testInput = nestedInput != null ? nestedInput.toString() : "";
//                        } else {
//                            testInput = map.values().stream().map(String::valueOf).collect(Collectors.joining("\n"));
//                        }
//                    }
//                } else {
//                    testInput = testCase.input.toString();
//                }
//
//                if (testInput.isEmpty()) { testInput = "0\n".repeat(10); }
//
//                logger.info("Evaluating test {} -> input: [{}], expected: [{}]", i + 1, testInput, expectedOutput);
//
//                Map<String, Object> testResult = new LinkedHashMap<>();
//                testResult.put("testCase", i + 1);
//
//                try {
//                    switch (job.getLanguage().toLowerCase()) {
//                        case "java":
//                            actualOutput = runJavaStatic(job.getCode(), testInput, PER_TEST_CASE_TIMEOUT_SECONDS);
//                            break;
//                        case "python":
//                            actualOutput = runPythonStatic(job.getCode(), testInput, PER_TEST_CASE_TIMEOUT_SECONDS);
//                            break;
//                        case "c":
//                            actualOutput = runCStatic(job.getCode(), testInput, PER_TEST_CASE_TIMEOUT_SECONDS);
//                            break;
//                        default:
//                            throw new RuntimeException("Unsupported language: " + job.getLanguage());
//                    }
//
//                    String normalizedActual = actualOutput == null ? "" : actualOutput.trim().replaceAll("\\r\\n", "\n").replaceAll("\\r", "\n");
//                    String normalizedExpected = expectedOutput == null ? "" : expectedOutput.trim().replaceAll("\\r\\n", "\n").replaceAll("\\r", "\n");
//
//                    if (normalizedActual.equals(normalizedExpected)) {
//                        testResult.put("status", "Passed");
//                    } else {
//                        allPassed = false;
//                        testResult.put("status", "Wrong Answer");
//                        testResult.put("input", testInput);
//                        testResult.put("yourOutput", normalizedActual);
//                        testResult.put("expectedOutput", normalizedExpected);
//                    }
//                } catch (RuntimeException e) {
//                    allPassed = false;
//                    String errorMessage = e.getMessage() != null && e.getMessage().contains("Execution timed out")
//                        ? "Time Limit Exceeded"
//                        : "Runtime Error";
//                    testResult.put("status", errorMessage);
//                    testResult.put("details", e.getMessage());
//                } catch (Exception e) {
//                    allPassed = false;
//                    testResult.put("status", "Server Error");
//                    testResult.put("details", e.getMessage());
//                }
//                results.add(testResult);
//            }
//
//            String overallMessage = allPassed ? "All " + testCases.size() + " test cases passed!" : "Some test cases failed.";
//
//            Map<String, Object> finalResponse = new LinkedHashMap<>();
//            finalResponse.put("passed", allPassed);
//            finalResponse.put("message", overallMessage);
//            finalResponse.put("results", results);
//
//            jobResults.put(job.getJobId(), finalResponse);
//            logger.info("Job {} completed: {}", job.getJobId(), overallMessage);
//        } catch (Exception e) {
//            logger.error("Error processing job {}: ", job.getJobId(), e);
//            Map<String, Object> errorResponse = Map.of("passed", false, "message", "Server Error: " + e.getMessage());
//            jobResults.put(job.getJobId(), errorResponse);
//        }
//    }
//
//    public Map<String, Object> getJobResult(String jobId) {
//        return jobResults.get(jobId);
//    }
//}
//
