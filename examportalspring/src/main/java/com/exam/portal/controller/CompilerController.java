//package com.exam.portal.controller;
//
//import com.exam.portal.model.Job;
//import com.exam.portal.model.Question;
//import com.exam.portal.repository.QuestionRepository;
//import com.exam.portal.service.ExecutionWorkerService;
//import com.exam.portal.service.TaskQueue;
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.io.*;
//import java.nio.file.*;
//import java.util.Comparator;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.ExecutionException;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.TimeoutException;
//import java.util.*;
//import java.util.stream.Collectors;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//@RestController
//@RequestMapping("/api/compiler")
//@CrossOrigin(origins = "http://localhost:4200") // Adjust as necessary for your frontend URL
//public class CompilerController {
//
//    private static final Logger logger = LoggerFactory.getLogger(CompilerController.class);
//
//    // Timeout for the simple "/run" endpoint, configurable in application.properties
//    @Value("${app.compiler.timeout-seconds:15}")
//    private long executionTimeoutSeconds;
//
//    // Strict, short timeout for each test case during submission, optimized for 500+ students
//    private static final long PER_TEST_CASE_TIMEOUT_SECONDS = 2; // 2 seconds per test case
//
//    @Autowired
//    private QuestionRepository questionRepository;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @Autowired
//    private TaskQueue taskQueue;
//
//    @Autowired
//    private ExecutionWorkerService executionWorkerService;
//
//    // Nested static class for defining a test case structure
//    static class TestCase {
//        public String input;
//        public String output;
//
//        // Default constructor for Jackson
//        public TestCase() {}
//
//        public TestCase(String input, String output) {
//            this.input = input;
//            this.output = output;
//        }
//
//        // Getters for Jackson if fields are private
//        public String getInput() { return input; }
//        public String getOutput() { return output; }
//        public void setInput(String input) { this.input = input; }
//        public void setOutput(String output) { this.output = output; }
//    }
//
//    // Nested static classes for object-based test case format
//    static class InputObject {
//        public List<Integer> nums;
//        public int target;
//
//        public List<Integer> getNums() { return nums; }
//        public void setNums(List<Integer> nums) { this.nums = nums; }
//        public int getTarget() { return target; }
//        public void setTarget(int target) { this.target = target; }
//    }
//
//    static class TestCaseObject {
//        public InputObject input;
//        public List<Integer> expected;
//
//        public InputObject getInput() { return input; }
//        public void setInput(InputObject input) { this.input = input; }
//        public List<Integer> getExpected() { return expected; }
//        public void setExpected(List<Integer> expected) { this.expected = expected; }
//    }
//
//    /**
//     * Helper method to parse test cases in either format.
//     */
//    private List<TestCase> parseTestCases(String testCasesJson) throws Exception {
//        try {
//            return objectMapper.readValue(testCasesJson, new TypeReference<List<TestCase>>(){});
//        } catch (Exception e) {
//            List<TestCaseObject> testCaseObjects = objectMapper.readValue(testCasesJson, new TypeReference<List<TestCaseObject>>(){});
//            List<TestCase> testCases = new ArrayList<>();
//            for (TestCaseObject tco : testCaseObjects) {
//                String input = tco.input.nums.stream().map(Object::toString).collect(Collectors.joining(" ")) + "\n" + tco.input.target;
//                String output = tco.expected.stream().map(Object::toString).collect(Collectors.joining(" "));
//                testCases.add(new TestCase(input, output));
//            }
//            return testCases;
//        }
//    }
//
//    /**
//     * Endpoint for the "Run" button. Executes code with provided stdin for debugging/testing purposes.
//     * Uses a longer, configurable timeout.
//     */
//    @PostMapping("/run")
//    public ResponseEntity<Map<String, String>> runCode(@RequestBody Map<String, String> request) {
//        String language = request.get("language");
//        String code = request.get("code");
//        // FIX: Ensure stdin is never empty to prevent NoSuchElementException in Scanner.
//        // If the user provides no input, default to multiple "0\n" to provide default integers for Scanner.nextInt().
//        String stdin = request.get("stdin");
//        if (stdin == null || stdin.isEmpty()) { stdin = "0\n".repeat(10); }
//
//        if (language == null || code == null) {
//            return ResponseEntity.badRequest().body(Map.of("output", "Language and code are required."));
//        }
//
//        logger.info("Received /run request for language: {} with {} chars of code and {} chars of stdin.", language, code.length(), stdin.length());
//        String output;
//        try {
//            switch (language.toLowerCase()) {
//                case "python":
//                    output = runPython(code, stdin, this.executionTimeoutSeconds);
//                    break;
//                case "java":
//                    output = runJava(code, stdin, this.executionTimeoutSeconds);
//                    break;
//                case "c":
//                    output = runC(code, stdin, this.executionTimeoutSeconds);
//                    break;
//                default:
//                    return ResponseEntity.badRequest().body(Map.of("output", "Unsupported language: " + language));
//            }
//        } catch (RuntimeException e) {
//            // Catch timeouts or other controlled execution errors
//            logger.warn("Execution error for /run ({}): {}", language, e.getMessage());
//            return ResponseEntity.ok(Map.of("output", "Execution Error: " + e.getMessage()));
//        } catch (Exception e) {
//            // Catch unexpected server-side errors
//            logger.error("Unexpected server error during /run ({}):", language, e);
//            return ResponseEntity.internalServerError().body(Map.of("output", "Internal Server Error: " + e.getMessage()));
//        }
//
//        return ResponseEntity.ok(Map.of("output", output));
//    }
//
//    /**
//     * Endpoint for the "Submit" button. Queues the submission for asynchronous processing.
//     */
//    @PostMapping("/submit")
//    public ResponseEntity<Map<String, Object>> submitCode(@RequestBody Map<String, String> request) {
//        Long questionId;
//        try {
//            questionId = Long.parseLong(request.get("questionId"));
//        } catch (NumberFormatException e) {
//            return ResponseEntity.badRequest().body(Map.of("passed", false, "message", "Invalid question ID."));
//        }
//        String language = request.get("language");
//        String code = request.get("code");
//
//        if (language == null || code == null) {
//            return ResponseEntity.badRequest().body(Map.of("passed", false, "message", "Language and code are required."));
//        }
//
//        logger.info("Received /submit request for Question ID: {} (Language: {}) with {} chars of code.", questionId, language, code.length());
//
//        Question question = questionRepository.findById(questionId)
//                .orElse(null);
//
//        if (question == null) {
//            return ResponseEntity.status(404).body(Map.of("passed", false, "message", "Question not found with ID: " + questionId));
//        }
//        if (question.getTestCases() == null || question.getTestCases().isEmpty()) {
//            return ResponseEntity.ok(Map.of("passed", false, "message", "No test cases configured for this question."));
//        }
//
//        // Create job and enqueue
//        Job job = new Job(code, language, questionId, question.getTestCases());
//        taskQueue.enqueue(job);
//
//        logger.info("Job {} queued for processing.", job.getJobId());
//
//        return ResponseEntity.accepted().body(Map.of("jobId", job.getJobId(), "message", "Submission accepted for processing."));
//    }
//
//    /**
//     * Endpoint to check job status/results.
//     */
//    @GetMapping("/job/{jobId}")
//    public ResponseEntity<Map<String, Object>> getJobResult(@PathVariable String jobId) {
//        Map<String, Object> result = executionWorkerService.getJobResult(jobId);
//        if (result == null) {
//            return ResponseEntity.ok(Map.of("status", "processing"));
//        }
//        return ResponseEntity.ok(result);
//    }
//
//    /**
//     * Endpoint to fetch full details of a question, including boilerplate code.
//     * This is useful for the exam page to load the question and for the admin panel to edit it.
//     * @param questionId The ID of the question to fetch.
//     * @return A ResponseEntity containing the Question object or an error message.
//     */
//    @GetMapping("/question/{questionId}")
//    public ResponseEntity<?> getQuestionDetails(@PathVariable Long questionId) {
//        logger.info("Request to fetch details for question ID: {}", questionId);
//        return questionRepository.findById(questionId)
//                .<ResponseEntity<?>>map(ResponseEntity::ok)
//                .orElseGet(() -> {
//                    logger.warn("Question not found with ID: {}", questionId);
//                    return ResponseEntity.status(404).body(Map.of("message", "Question not found with ID: " + questionId));
//                });
//    }
//
//    /**
//     * Executes code directly on the host with resource limits.
//     * @param language The programming language.
//     * @param code The source code.
//     * @param stdin The input for the program.
//     * @param timeoutSeconds The execution timeout.
//     * @return The program's output.
//     * @throws Exception if an error occurs.
//     */
//    private String executeCode(String language, String code, String stdin, long timeoutSeconds) throws Exception {
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
//                    commandInside = "ulimit -m 262144 -t " + timeoutSeconds + " && python3 " + fileName + " < /dev/stdin";
//                    break;
//                case "java":
//                    String className = extractPublicClassName(code);
//                    fileName = className + ".java";
//                    Files.writeString(tempDir.resolve(fileName), code);
//                    commandInside = "ulimit -m 262144 -t " + timeoutSeconds + " && javac " + fileName + " && java " + className + " < /dev/stdin";
//                    break;
//                case "c":
//                    fileName = "main.c";
//                    Files.writeString(tempDir.resolve(fileName), code);
//                    commandInside = "ulimit -m 262144 -t " + timeoutSeconds + " && gcc " + fileName + " -o main && ./main < /dev/stdin";
//                    break;
//                default:
//                    throw new RuntimeException("Unsupported language: " + language);
//            }
//
//            // Execute command directly with limits
//            List<String> command = Arrays.asList("sh", "-c", commandInside);
//
//            // Execute command with stdin
//            return runProcess(command, tempDir, stdin, timeoutSeconds);
//        } finally {
//            if (tempDir != null) {
//                deleteDirectory(tempDir);
//            }
//        }
//    }
//
//    /**
//     * Executes a given command list in a specified working directory with stdin and a timeout.
//     * Captures stdout and stderr.
//     * @param command The command and its arguments to execute.
//     * @param workDir The directory in which to execute the command.
//     * @param stdin The input string to feed to the process's stdin.
//     * @param timeoutSeconds The maximum time (in seconds) to wait for the process to complete.
//     * @return The combined stdout and stderr of the process.
//     * @throws RuntimeException if the process times out or encounters other execution issues.
//     */
//    private String runProcess(List<String> command, Path workDir, String stdin, long timeoutSeconds) throws Exception {
//        ProcessBuilder pb = new ProcessBuilder(command);
//        pb.directory(workDir.toFile());
//        pb.redirectErrorStream(true); // Redirect stderr to stdout stream
//        Process process = pb.start();
//
//        logger.debug("Started process: {} in dir: {}", String.join(" ", command), workDir.toString());
//
//        // Write stdin to the process and close the stream to signal EOF.
//        try (OutputStream os = process.getOutputStream()) {
//            if (stdin != null && !stdin.isEmpty()) {
//                os.write(stdin.getBytes());
//            }
//            os.flush();
//        } catch (IOException e) {
//            logger.warn("Error writing stdin to process for command: {}: {}", String.join(" ", command), e.getMessage());
//        }
//
//        // Read process output asynchronously to prevent deadlocks
//        CompletableFuture<String> outputFuture = CompletableFuture.supplyAsync(() -> {
//            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
//                return reader.lines().collect(Collectors.joining("\n"));
//            } catch (IOException e) {
//                logger.error("Error reading output from process for command: {}", String.join(" ", command), e);
//                throw new UncheckedIOException(e);
//            }
//        });
//
//        boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
//
//        if (!finished) {
//            process.destroyForcibly();
//            logger.warn("Process for command '{}' timed out after {} seconds. Destroyed forcibly.", String.join(" ", command), timeoutSeconds);
//            throw new RuntimeException("Execution timed out after " + timeoutSeconds + " seconds");
//        }
//
//        try {
//            String output = outputFuture.get(5, TimeUnit.SECONDS);
//            int exitCode = process.exitValue();
//            logger.debug("Process '{}' exited with code {}. Output length: {}. ", String.join(" ", command), exitCode, output.length());
//
//            if (exitCode != 0 && !output.trim().isEmpty()) {
//                return output;
//            }
//            return output;
//        } catch (InterruptedException | ExecutionException | TimeoutException e) {
//            logger.error("Failed to get process output for command '{}' after execution (Timeout: {}s).", String.join(" ", command), 5, e);
//            throw new RuntimeException("Failed to collect process output: " + e.getMessage(), e);
//        }
//    }
//
//    /**
//     * Runs Python code directly on the host.
//     * @param code The Python code string.
//     * @param stdin The input for the program.
//     * @param timeoutSeconds The execution timeout.
//     * @return The program's output.
//     * @throws Exception if an error occurs during execution.
//     */
//    private String runPython(String code, String stdin, long timeoutSeconds) throws Exception {
//        return executeCode("python", code, stdin, timeoutSeconds);
//    }
//
//    /**
//     * Runs Java code directly on the host. Compiles first, then executes.
//     * @param code The Java code string (must contain a 'public static void main' in a 'Main' class).
//     * @param stdin The input for the program.
//     * @param timeoutSeconds The execution timeout.
//     * @return The program's output.
//     * @throws Exception if an error occurs during execution.
//     */
//    private String extractPublicClassName(String code) {
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
//    private String runJava(String code, String stdin, long timeoutSeconds) throws Exception {
//        return executeCode("java", code, stdin, timeoutSeconds);
//    }
//
//    /**
//     * Runs C code directly on the host. Compiles first, then executes.
//     * @param code The C code string (must contain a 'main' function).
//     * @param stdin The input for the program.
//     * @param timeoutSeconds The execution timeout.
//     * @return The program's output.
//     * @throws Exception if an error occurs during execution.
//     */
//    private String runC(String code, String stdin, long timeoutSeconds) throws Exception {
//        return executeCode("c", code, stdin, timeoutSeconds);
//    }
//
//    /**
//     * Recursively deletes a directory and its contents.
//     * @param path The path to the directory to delete.
//     */
//    private void deleteDirectory(Path path) {
//        try {
//            if (Files.exists(path)) {
//                Files.walk(path)
//                    .sorted(Comparator.reverseOrder()) // Delete files before directories
//                    .map(Path::toFile)
//                    .forEach(file -> {
//                        if (!file.delete()) {
//                            logger.warn("Failed to delete file/directory: {}", file.getAbsolutePath());
//                        }
//                    });
//            }
//        } catch (IOException e) {
//            logger.error("Failed to delete temporary directory: {}", path, e);
//        }
//    }
//}



package com.exam.portal.controller;

import com.exam.portal.model.Question;
import com.exam.portal.repository.QuestionRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.file.*;
import java.util.Comparator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/compiler")
@CrossOrigin(origins = "http://localhost:4200") // Adjust as necessary for your frontend URL
public class CompilerController {

    private static final Logger logger = LoggerFactory.getLogger(CompilerController.class);

    // Timeout for the simple "/run" endpoint, configurable in application.properties
    @Value("${app.compiler.timeout-seconds:15}")
    private long executionTimeoutSeconds;

    // Strict, short timeout for each test case during submission, optimized for 500+ students
    private static final long PER_TEST_CASE_TIMEOUT_SECONDS = 2; // 2 seconds per test case

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    // No more async dependencies needed

    // Nested static class for defining a test case structure
    static class TestCase {
        public Object input;
        public Object output;        // Main output field
        public Object expected;      // Alternative output field
        public Object expectedOutput; // Another alternative output field

        // Default constructor for Jackson
        public TestCase() {}

        public TestCase(Object input, Object output) {
            this.input = input;
            this.output = output;
        }

        public Object getInput() { return input; }
        public void setInput(Object input) { this.input = input; }
        
        /**
         * Gets the expected output in order of precedence:
         * 1. output
         * 2. expected
         * 3. expectedOutput
         */
        public String getExpectedOutput() {
            if (output != null) return output.toString().trim();
            if (expected != null) return expected.toString().trim();
            if (expectedOutput != null) return expectedOutput.toString().trim();
            return "";
        }
    }

    // Nested static classes for object-based test case format
    static class InputObject {
        public List<Integer> nums;
        public int target;

        public List<Integer> getNums() { return nums; }
        public void setNums(List<Integer> nums) { this.nums = nums; }
        public int getTarget() { return target; }
        public void setTarget(int target) { this.target = target; }
    }

    static class TestCaseObject {
        public InputObject input;
        public List<Integer> expected;

        public InputObject getInput() { return input; }
        public void setInput(InputObject input) { this.input = input; }
        public List<Integer> getExpected() { return expected; }
        public void setExpected(List<Integer> expected) { this.expected = expected; }
    }

    /**
     * Helper method to parse test cases in either format.
     */
    private List<TestCase> parseTestCases(String testCasesJson) throws Exception {
        try {
            // Try parsing as standard test case format first
            return objectMapper.readValue(testCasesJson, new TypeReference<List<TestCase>>(){});
        } catch (Exception firstError) {
            try {
                // Try parsing special "two sum" format
                List<TestCaseObject> testCaseObjects = objectMapper.readValue(testCasesJson, new TypeReference<List<TestCaseObject>>(){});
                List<TestCase> testCases = new ArrayList<>();
                for (TestCaseObject tco : testCaseObjects) {
                    if (tco.input == null || tco.input.nums == null || tco.expected == null) {
                        throw new IllegalArgumentException("Invalid test case format: missing required fields");
                    }
                    String input = tco.input.nums.stream()
                            .map(Object::toString)
                            .collect(Collectors.joining(" ")) + "\n" + tco.input.target;
                    String output = tco.expected.stream()
                            .map(Object::toString)
                            .collect(Collectors.joining(" "));
                    testCases.add(new TestCase(input, output));
                }
                return testCases;
            } catch (Exception secondError) {
                throw new IllegalArgumentException(
                    "Failed to parse test cases in both standard and special formats. " +
                    "Standard format error: " + firstError.getMessage() + ". " +
                    "Special format error: " + secondError.getMessage()
                );
            }
        }
    }

    /**
     * Endpoint for the "Run" button. Executes code with provided stdin for debugging/testing purposes.
     * Uses a longer, configurable timeout.
     */
    @PostMapping("/run")
    public ResponseEntity<Map<String, String>> runCode(@RequestBody Map<String, String> request) {
        String language = request.get("language");
        String code = request.get("code");
        // FIX: Ensure stdin is never empty to prevent NoSuchElementException in Scanner.
        // If the user provides no input, default to multiple "0\n" to provide default integers for Scanner.nextInt().
        String stdin = request.get("stdin");
        if (stdin == null || stdin.isEmpty()) { stdin = "0\n".repeat(10); }

        if (language == null || code == null) {
            return ResponseEntity.badRequest().body(Map.of("output", "Language and code are required."));
        }

        logger.info("Received /run request for language: {} with {} chars of code and {} chars of stdin.", language, code.length(), stdin.length());
        String output;
        try {
            switch (language.toLowerCase()) {
                case "python":
                    output = runPython(code, stdin, this.executionTimeoutSeconds);
                    break;
                case "java":
                    output = runJava(code, stdin, this.executionTimeoutSeconds);
                    break;
                case "c":
                    output = runC(code, stdin, this.executionTimeoutSeconds);
                    break;
                default:
                    return ResponseEntity.badRequest().body(Map.of("output", "Unsupported language: " + language));
            }
        } catch (RuntimeException e) {
            // Catch timeouts or other controlled execution errors
            logger.warn("Execution error for /run ({}): {}", language, e.getMessage());
            return ResponseEntity.ok(Map.of("output", "Execution Error: " + e.getMessage()));
        } catch (Exception e) {
            // Catch unexpected server-side errors
            logger.error("Unexpected server error during /run ({}):", language, e);
            return ResponseEntity.internalServerError().body(Map.of("output", "Internal Server Error: " + e.getMessage()));
        }

        return ResponseEntity.ok(Map.of("output", output));
    }

    /**
     * Endpoint for the "Submit" button. Executes test cases immediately and returns results.
     */
    @PostMapping("/submit")
    public ResponseEntity<Map<String, Object>> submitCode(@RequestBody Map<String, String> request) {
        Long questionId;
        try {
            questionId = Long.parseLong(request.get("questionId"));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "passed", false,
                "message", "Invalid question ID",
                "results", Collections.emptyList()
            ));
        }

        String language = request.get("language");
        String code = request.get("code");

        if (language == null || code == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "passed", false,
                "message", "Language and code are required",
                "results", Collections.emptyList()
            ));
        }

        logger.info("Received /submit request for Question ID: {} (Language: {}) with {} chars of code.", 
                   questionId, language, code.length());

        Question question = questionRepository.findById(questionId).orElse(null);
        if (question == null) {
            return ResponseEntity.status(404).body(Map.of(
                "passed", false,
                "message", "Question not found",
                "results", Collections.emptyList()
            ));
        }

        if (question.getTestCases() == null || question.getTestCases().isEmpty()) {
            return ResponseEntity.ok(Map.of(
                "passed", false,
                "message", "No test cases configured for this question",
                "results", Collections.emptyList()
            ));
        }

        try {
            Map<String, Object> result = executeTestCases(code, language, question.getTestCases());
            return ResponseEntity.ok(Map.of(
                "passed", result.get("passed"),
                "message", result.get("message"),
                "results", result.get("results")
            ));
        } catch (Exception e) {
            logger.error("Error executing test cases for question {}: ", questionId, e);
            return ResponseEntity.internalServerError().body(Map.of(
                "passed", false,
                "message", "Server error during execution: " + e.getMessage(),
                "results", Collections.emptyList()
            ));
        }
    }

    // Removed /job/{jobId} endpoint - no longer using async processing

    /**
     * Executes test cases immediately and returns the results.
     */
    private Map<String, Object> executeTestCases(String code, String language, String testCasesJson) throws Exception {
        List<TestCase> testCases = parseTestCases(testCasesJson);
        List<Map<String, Object>> results = new ArrayList<>();
        boolean allPassed = true;

        for (int i = 0; i < testCases.size(); i++) {
            TestCase testCase = testCases.get(i);
            String actualOutput;
            String expectedOutput = testCase.getExpectedOutput(); // Uses our new method that checks all fields
            String testInput = testCase.getInput() != null ? testCase.getInput().toString() : "";
            if (testInput.isEmpty()) { testInput = "0\n".repeat(10); }

            Map<String, Object> testResult = new LinkedHashMap<>();
            testResult.put("testCase", i + 1);

            try {
                switch (language.toLowerCase()) {
                    case "java":
                        actualOutput = runJava(code, testInput, PER_TEST_CASE_TIMEOUT_SECONDS);
                        break;
                    case "python":
                        actualOutput = runPython(code, testInput, PER_TEST_CASE_TIMEOUT_SECONDS);
                        break;
                    case "c":
                        actualOutput = runC(code, testInput, PER_TEST_CASE_TIMEOUT_SECONDS);
                        break;
                    default:
                        throw new RuntimeException("Unsupported language: " + language);
                }

                if (actualOutput.startsWith("Compilation Error:")) {
                    allPassed = false;
                    testResult.put("status", "Compilation Error");
                    testResult.put("details", actualOutput);
                } else {
                    String trimmedActualOutput = actualOutput.trim();
                    String normalizedActual = trimmedActualOutput.replaceAll("\\r\\n", "\n").replaceAll("\\r", "\n");
                    String normalizedExpected = expectedOutput.replaceAll("\\r\\n", "\n").replaceAll("\\r", "\n");

                    if (normalizedActual.equals(normalizedExpected)) {
                        testResult.put("status", "Passed");
                    } else {
                        allPassed = false;
                        testResult.put("status", "Wrong Answer");
                        testResult.put("input", testInput);
                        testResult.put("yourOutput", normalizedActual);
                        testResult.put("expectedOutput", normalizedExpected);
                    }
                }
            } catch (RuntimeException e) {
                allPassed = false;
                String errorMessage = e.getMessage() != null && e.getMessage().contains("Execution timed out")
                    ? "Time Limit Exceeded"
                    : "Runtime Error";
                testResult.put("status", errorMessage);
                testResult.put("details", e.getMessage());
            } catch (Exception e) {
                allPassed = false;
                testResult.put("status", "Server Error");
                testResult.put("details", e.getMessage());
            }
            results.add(testResult);
        }

        String overallMessage = allPassed ? "All " + testCases.size() + " test cases passed!" : "Some test cases failed.";

        Map<String, Object> finalResponse = new LinkedHashMap<>();
        finalResponse.put("passed", allPassed);
        finalResponse.put("message", overallMessage);
        finalResponse.put("results", results);

        return finalResponse;
    }

    /**
     * Endpoint to fetch full details of a question, including boilerplate code.
     * This is useful for the exam page to load the question and for the admin panel to edit it.
     * @param questionId The ID of the question to fetch.
     * @return A ResponseEntity containing the Question object or an error message.
     */
    @GetMapping("/question/{questionId}")
    public ResponseEntity<?> getQuestionDetails(@PathVariable Long questionId) {
        logger.info("Request to fetch details for question ID: {}", questionId);
        return questionRepository.findById(questionId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> {
                    logger.warn("Question not found with ID: {}", questionId);
                    return ResponseEntity.status(404).body(Map.of("message", "Question not found with ID: " + questionId));
                });
    }

    /**
     * Executes code directly on the host with resource limits.
     * @param language The programming language.
     * @param code The source code.
     * @param stdin The input for the program.
     * @param timeoutSeconds The execution timeout.
     * @return The program's output.
     * @throws Exception if an error occurs.
     */
    private String executeCode(String language, String code, String stdin, long timeoutSeconds) throws Exception {
        Path tempDir = null;
        try {
            tempDir = Files.createTempDirectory("code_exec_");
            String fileName;
            String compileCommand = null;
            String runCommand;

            switch (language.toLowerCase()) {
                case "python":
                    fileName = "main.py";
                    Files.writeString(tempDir.resolve(fileName), code);
                    runCommand = "python3 " + fileName + " < /dev/stdin";
                    break;

                case "java":
                    String className = extractPublicClassName(code);
                    fileName = className + ".java";
                    Files.writeString(tempDir.resolve(fileName), code);
                    compileCommand = "javac " + fileName;
                    runCommand = "java " + className + " < /dev/stdin";
                    break;

                case "c":
                    fileName = "main.c";
                    Files.writeString(tempDir.resolve(fileName), code);
                    compileCommand = "gcc " + fileName + " -o main";
                    runCommand = "./main < /dev/stdin";
                    break;

                default:
                    throw new RuntimeException("Unsupported language: " + language);
            }

            // Handle compilation if needed
            if (compileCommand != null) {
                Process compileProcess = new ProcessBuilder("sh", "-c", compileCommand)
                    .directory(tempDir.toFile())
                    .redirectErrorStream(true)
                    .start();

                if (!compileProcess.waitFor(timeoutSeconds, TimeUnit.SECONDS)) {
                    compileProcess.destroyForcibly();
                    throw new RuntimeException("Compilation timed out after " + timeoutSeconds + " seconds");
                }

                String compileOutput = new String(compileProcess.getInputStream().readAllBytes());
                if (compileProcess.exitValue() != 0) {
                    return "Compilation Error: " + compileOutput;
                }
            }

            // Execute the program with input
            List<String> command = Arrays.asList("sh", "-c", runCommand);
            ProcessBuilder pb = new ProcessBuilder(command)
                .directory(tempDir.toFile())
                .redirectErrorStream(true);
            Process process = pb.start();

            // Write input to process
            try (OutputStream os = process.getOutputStream()) {
                if (stdin != null && !stdin.isEmpty()) {
                    os.write(stdin.getBytes());
                }
                os.flush();
            }

            // Read output asynchronously to prevent deadlocks
            CompletableFuture<String> outputFuture = CompletableFuture.supplyAsync(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    return reader.lines().collect(Collectors.joining("\n"));
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });

            // Wait for process to complete with timeout
            if (!process.waitFor(timeoutSeconds, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                throw new RuntimeException("Execution timed out after " + timeoutSeconds + " seconds");
            }

            try {
                String output = outputFuture.get(5, TimeUnit.SECONDS);
                return process.exitValue() == 0 ? output : "Runtime Error: " + output;
            } catch (TimeoutException e) {
                throw new RuntimeException("Failed to collect output: " + e.getMessage());
            }

            // The entire command execution has been moved up
        } finally {
            if (tempDir != null) {
                deleteDirectory(tempDir);
            }
        }
    }

    // Removed redundant runProcess method as its functionality is now integrated into executeCode

    /**
     * Runs Python code directly on the host.
     * @param code The Python code string.
     * @param stdin The input for the program.
     * @param timeoutSeconds The execution timeout.
     * @return The program's output.
     * @throws Exception if an error occurs during execution.
     */
    private String runPython(String code, String stdin, long timeoutSeconds) throws Exception {
        return executeCode("python", code, stdin, timeoutSeconds);
    }

    /**
     * Runs Java code directly on the host. Compiles first, then executes.
     * @param code The Java code string (must contain a 'public static void main' in a 'Main' class).
     * @param stdin The input for the program.
     * @param timeoutSeconds The execution timeout.
     * @return The program's output.
     * @throws Exception if an error occurs during execution.
     */
    private String extractPublicClassName(String code) {
        try (Scanner scanner = new Scanner(code)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                // Look for "public class ClassName"
                if (line.startsWith("public class")) {
                    // Simple parsing: "public class Solution {" -> "Solution"
                    String[] parts = line.split("\\s+");
                    if (parts.length > 2) {
                        return parts[2].replaceAll("\\{", "").trim();
                    }
                }
            }
        }
        return "Main"; // Default if no public class is found
    }

    private String runJava(String code, String stdin, long timeoutSeconds) throws Exception {
        return executeCode("java", code, stdin, timeoutSeconds);
    }

    /**
     * Runs C code directly on the host. Compiles first, then executes.
     * @param code The C code string (must contain a 'main' function).
     * @param stdin The input for the program.
     * @param timeoutSeconds The execution timeout.
     * @return The program's output.
     * @throws Exception if an error occurs during execution.
     */
    private String runC(String code, String stdin, long timeoutSeconds) throws Exception {
        return executeCode("c", code, stdin, timeoutSeconds);
    }

    /**
     * Recursively deletes a directory and its contents.
     * @param path The path to the directory to delete.
     */
    private void deleteDirectory(Path path) {
        try {
            if (Files.exists(path)) {
                Files.walk(path)
                    .sorted(Comparator.reverseOrder()) // Delete files before directories
                    .map(Path::toFile)
                    .forEach(file -> {
                        if (!file.delete()) {
                            logger.warn("Failed to delete file/directory: {}", file.getAbsolutePath());
                        }
                    });
            }
        } catch (IOException e) {
            logger.error("Failed to delete temporary directory: {}", path, e);
        }
    }
}


