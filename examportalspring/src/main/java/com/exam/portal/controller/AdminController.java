//
//
//
//
//package com.exam.portal.controller;
//
//import com.exam.portal.model.*;
//import com.exam.portal.repository.*;
//import jakarta.persistence.EntityManager;
//import jakarta.persistence.PersistenceContext;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.bind.annotation.*;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//import java.util.Set;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.core.type.TypeReference;
//
//@RestController
//@RequestMapping("/api/admin")
//@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:4000"})
//public class AdminController {
//
//    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
//
//    @Autowired private StudentRepository studentRepository;
//    @Autowired private QuestionRepository questionRepository;
//    @Autowired private ExamRepository examRepository;
//    @Autowired private ResultRepository resultRepository;
//    @Autowired private SectionRepository sectionRepository;
//
//    @Autowired private CompilerController compilerController; // For judging coding answers
//    @Autowired private LiveExamSessionRepository liveExamSessionRepository;
//
//    // --- Student Management ---
//    @GetMapping("/students")
//    public List<Student> getAllStudents() {
//        return studentRepository.findAll();
//    }
//
//    // --- Question Management ---
//    @PostMapping("/questions")
//    public ResponseEntity<Question> addQuestion(@RequestBody Question questionDetails) {
//        try {
//            Question newQuestion = new Question();
//            newQuestion.setQuestionText(questionDetails.getQuestionText());
//
//            // Set section relationship
//            if (questionDetails.getSection() != null && questionDetails.getSection().getId() != null) {
//                Section section = sectionRepository.findById(questionDetails.getSection().getId())
//                        .orElseThrow(() -> new RuntimeException("Section not found"));
//                newQuestion.setSection(section);
//            }
//
//            boolean isCoding = isCodingQuestion(questionDetails);
//            newQuestion.setIsCodingQuestion(isCoding);
//
//            // Determine which fields to set based on the isCodingQuestion flag.
//            if (isCoding) {
//                // Set language-specific boilerplate codes
//                newQuestion.setBoilerplateJava(questionDetails.getBoilerplateJava());
//                newQuestion.setBoilerplatePython(questionDetails.getBoilerplatePython());
//                newQuestion.setBoilerplateC(questionDetails.getBoilerplateC());
//                newQuestion.setBoilerplateSql(questionDetails.getBoilerplateSql());
//                newQuestion.setTestCases(questionDetails.getTestCases());
//                // SQL dataset setup (per-question isolated environment)
//                newQuestion.setSetupSql(questionDetails.getSetupSql());
//
//                // Clear MCQ fields
//                newQuestion.setOption1(null);
//                newQuestion.setOption2(null);
//                newQuestion.setOption3(null);
//                newQuestion.setOption4(null);
//                newQuestion.setCorrectAnswer(null);
//            } else {
//                // Handle MCQ questions
//                newQuestion.setOption1(questionDetails.getOption1());
//                newQuestion.setOption2(questionDetails.getOption2());
//                newQuestion.setOption3(questionDetails.getOption3());
//                newQuestion.setOption4(questionDetails.getOption4());
//                newQuestion.setCorrectAnswer(questionDetails.getCorrectAnswer());
//
//                // Clear coding fields
//                newQuestion.setBoilerplateJava(null);
//                newQuestion.setBoilerplatePython(null);
//                newQuestion.setBoilerplateC(null);
//                newQuestion.setBoilerplateSql(null);
//                newQuestion.setTestCases(null);
//                newQuestion.setSetupSql(null);
//            }
//
//            Question savedQuestion = questionRepository.save(newQuestion);
//            return ResponseEntity.ok(savedQuestion);
//        } catch (Exception e) {
//            logger.error("Error adding question: {}", questionDetails.getQuestionText(), e);
//            return ResponseEntity.status(500).body(null);
//        }
//    }
//
//    @GetMapping("/questions")
//    public List<Question> getAllQuestions() {
//        return questionRepository.findAll();
//    }
//
//    @PutMapping("/questions/{id}")
//    public ResponseEntity<Question> updateQuestion(@PathVariable Long id, @RequestBody Question qDetails) {
//        try {
//            Question q = questionRepository.findById(id).orElseThrow(() -> new RuntimeException("Question not found with id: " + id));
//
//            q.setQuestionText(qDetails.getQuestionText());
//
//            // Set section relationship
//            if (qDetails.getSection() != null && qDetails.getSection().getId() != null) {
//                Section section = sectionRepository.findById(qDetails.getSection().getId())
//                        .orElseThrow(() -> new RuntimeException("Section not found"));
//                q.setSection(section);
//            }
//
//            boolean isCoding = isCodingQuestion(qDetails);
//            q.setIsCodingQuestion(isCoding);
//
//            // Determine which fields to set based on the isCodingQuestion flag.
//            if (q.getIsCodingQuestion()) {
//                // Set language-specific boilerplate codes
//                q.setBoilerplateJava(qDetails.getBoilerplateJava());
//                q.setBoilerplatePython(qDetails.getBoilerplatePython());
//                q.setBoilerplateC(qDetails.getBoilerplateC());
//                q.setBoilerplateSql(qDetails.getBoilerplateSql());
//                q.setTestCases(qDetails.getTestCases());
//                // SQL dataset setup (per-question isolated environment)
//                q.setSetupSql(qDetails.getSetupSql());
//
//                // Clear MCQ fields
//                q.setOption1(null);
//                q.setOption2(null);
//                q.setOption3(null);
//                q.setOption4(null);
//                q.setCorrectAnswer(null);
//            } else {
//                q.setOption1(qDetails.getOption1());
//                q.setOption2(qDetails.getOption2());
//                q.setOption3(qDetails.getOption3());
//                q.setOption4(qDetails.getOption4());
//                q.setCorrectAnswer(qDetails.getCorrectAnswer());
//
//                // Clear coding fields
//                q.setBoilerplateJava(null);
//                q.setBoilerplatePython(null);
//                q.setBoilerplateC(null);
//                q.setBoilerplateSql(null);
//                q.setTestCases(null);
//                q.setSetupSql(null);
//            }
//
//            final Question updatedQuestion = questionRepository.save(q);
//            return ResponseEntity.ok(updatedQuestion);
//        } catch (Exception e) {
//            logger.error("Error updating question with id: {}", id, e);
//            return ResponseEntity.status(500).body(null);
//        }
//    }
//
//    private boolean isCodingQuestion(Question question) {
//        // Prefer the explicit flag from the request
//        if (Boolean.TRUE.equals(question.getIsCodingQuestion())) {
//            return true;
//        }
//        // Fallback to checking the section name
//        return question.getSection() != null && question.getSection().getName() != null &&
//               ("Coding".equalsIgnoreCase(question.getSection().getName()) || "SQL".equalsIgnoreCase(question.getSection().getName()));
//    }
//
//    @DeleteMapping("/questions/{id}")
//    public ResponseEntity<?> deleteQuestion(@PathVariable Long id) {
//        questionRepository.deleteById(id);
//        return ResponseEntity.ok().build();
//    }
//
//    // --- Exam Management ---
//    @PostMapping("/exams")
//    public Exam createExam(@RequestBody Exam exam) {
//        return examRepository.save(exam);
//    }
//
//    @PostMapping("/exams/with-sections")
//    public ResponseEntity<Exam> createExamWithSections(@RequestBody Map<String, Object> examData) {
//        try {
//            String examName = (String) examData.get("examName");
//            @SuppressWarnings("unchecked")
//            List<Map<String, Object>> sectionsData = (List<Map<String, Object>>) examData.get("sections");
//
//            // Validate input
//            if (examName == null || examName.trim().isEmpty()) {
//                return ResponseEntity.badRequest().body(null);
//            }
//            if (sectionsData == null || sectionsData.isEmpty()) {
//                return ResponseEntity.badRequest().body(null);
//            }
//
//            // Create the exam
//            Exam exam = new Exam();
//            exam.setExamName(examName);
//            exam.setActive(false); // New exams are inactive by default
//            Exam savedExam = examRepository.save(exam);
//
//            // Link selected sections to this exam
//            List<Section> sections = new ArrayList<>();
//            Set<String> sectionNames = new HashSet<>(); // Track section names to prevent duplicates
//
//            for (Map<String, Object> sectionData : sectionsData) {
//                Integer sectionId = (Integer) sectionData.get("id");
//                Integer durationInMinutes = (Integer) sectionData.get("durationInMinutes");
//                Integer orderIndex = (Integer) sectionData.get("orderIndex");
//
//                // Validate section data
//                if (sectionId == null) {
//                    logger.warn("Skipping section with null ID for exam: {}", examName);
//                    continue;
//                }
//                if (durationInMinutes == null || durationInMinutes <= 0) {
//                    logger.warn("Skipping section ID '{}' with invalid duration for exam: {}", sectionId, examName);
//                    continue;
//                }
//                if (orderIndex == null || orderIndex < 0) {
//                    logger.warn("Skipping section ID '{}' with invalid order index for exam: {}", sectionId, examName);
//                    continue;
//                }
//
//                // Find the standalone section by ID and link it to the exam
//                Section sectionToLink = sectionRepository.findById(sectionId)
//                    .orElse(null);
//
//                if (sectionToLink == null) {
//                    logger.warn("No standalone section found with ID '{}' for exam: {}. Skipping.", sectionId, examName);
//                    continue;
//                }
//
//                // Update the section to link to the exam
//                sectionToLink.setExam(savedExam);
//                sectionToLink.setOrderIndex(orderIndex); // Update order index
//                Section savedSection = sectionRepository.save(sectionToLink);
//                sections.add(savedSection);
//                sectionNames.add(savedSection.getName().toLowerCase());
//
//                logger.info("Linked section '{}' (ID: {}) to exam '{}' with order index {}", savedSection.getName(), sectionId, examName, orderIndex);
//            }
//
//            if (sections.isEmpty()) {
//                // If no valid sections were created, delete the exam and return error
//                examRepository.delete(savedExam);
//                return ResponseEntity.badRequest().body(null);
//            }
//
//            savedExam.setSections(sections);
//            logger.info("Successfully created exam '{}' with {} sections", examName, sections.size());
//            return ResponseEntity.ok(savedExam);
//
//        } catch (Exception e) {
//            logger.error("Error creating exam with sections: {}", examData.get("examName"), e);
//            return ResponseEntity.status(500).body(null);
//        }
//    }
//
//    @GetMapping("/exams")
//    public List<Exam> getAllExams() {
//        return examRepository.findAll();
//    }
//
//    @Transactional
//    @PutMapping("/exams/{id}")
//    public ResponseEntity<Exam> updateExam(@PathVariable Integer id, @RequestParam boolean isActive) {
//        try {
//            // Ensure the target exam exists before proceeding
//            if (!examRepository.existsById(id)) {
//                throw new RuntimeException("Exam not found with id: " + id);
//            }
//
//            if (isActive) {
//                // If activating one, deactivate all others.
//                List<Exam> allExams = examRepository.findAll();
//                for (Exam exam : allExams) {
//                    exam.setActive(exam.getId().equals(id));
//                }
//                examRepository.saveAll(allExams);
//            } else {
//                // If deactivating, just update the specific exam.
//                examRepository.deactivateExamById(id);
//            }
//
//            // Re-fetch the exam to return its final, correct state.
//            return ResponseEntity.ok(examRepository.findById(id).get());
//        } catch (Exception e) {
//            logger.error("Error updating exam with id: {}", id, e);
//            return ResponseEntity.status(500).body(null);
//        }
//    }
//
//    @PutMapping("/exams/{id}/details")
//    public ResponseEntity<Exam> updateExamDetails(@PathVariable Integer id, @RequestBody Map<String, Object> examData) {
//        try {
//            Exam exam = examRepository.findById(id)
//                    .orElseThrow(() -> new RuntimeException("Exam not found with id: " + id));
//
//            if (examData.containsKey("examName")) {
//                exam.setExamName((String) examData.get("examName"));
//            }
//
//            // Save the exam name change first
//            exam = examRepository.save(exam);
//
//            // Update sections
//            // First, clear existing sections
//            List<Section> existingSections = sectionRepository.findByExamIdOrderByOrderIndexAsc(id);
//            for (Section s : existingSections) {
//                s.setExam(null);
//                sectionRepository.save(s);
//                s.setOrderIndex(null); // Also clear the order index
//            }
//
//            // Add new sections
//            @SuppressWarnings("unchecked")
//            List<Map<String, Object>> sectionsData = (List<Map<String, Object>>) examData.get("sections");
//            if (sectionsData != null) {
//                for (Map<String, Object> sd : sectionsData) {
//                    Integer sid = (Integer) sd.get("id");
//                    Section s = sectionRepository.findById(sid).orElse(null);
//                    if (s != null) {
//                        s.setExam(exam);
//                        s.setOrderIndex((Integer) sd.get("orderIndex"));
//                        // The save operation will link it back
//                        sectionRepository.save(s);
//                    }
//                }
//            }
//
//            // Re-fetch the exam with all its updated associations to return the freshest state
//            return ResponseEntity.ok(examRepository.findById(id).get());
//        } catch (Exception e) {
//            logger.error("Error updating exam details for id: {}", id, e);
//            return ResponseEntity.status(500).body(null);
//        }
//    }
//
//    @DeleteMapping("/exams/{id}")
//    public ResponseEntity<?> deleteExam(@PathVariable Integer id) {
//        try {
//            Exam exam = examRepository.findById(id)
//                    .orElseThrow(() -> new RuntimeException("Exam not found"));
//
//            if (exam.isActive()) {
//                return ResponseEntity.badRequest().body("Cannot delete active exam");
//            }
//
//            // Clear sections
//            List<Section> sections = sectionRepository.findByExamIdOrderByOrderIndexAsc(id);
//            for (Section s : sections) {
//                s.setExam(null);
//                sectionRepository.save(s);
//            }
//
//            examRepository.delete(exam);
//            return ResponseEntity.ok().build();
//        } catch (Exception e) {
//            logger.error("Error deleting exam with id: {}", id, e);
//            return ResponseEntity.status(500).body(Map.of("message", "Error deleting exam"));
//        }
//    }
//    
//    // --- Result Management ---
//    @GetMapping("/results")
//    public List<Map<String, Object>> getAllResults() {
//        List<Result> results = resultRepository.findAll();
//        List<Map<String, Object>> resultList = new ArrayList<>();
//        for (Result result : results) {
//            try {
//                Map<String, Object> resultMap = new HashMap<>();
//                resultMap.put("id", result.getId());
//                resultMap.put("studentEmail", result.getStudentEmail());
//                // Get student name
//                Optional<Student> studentOpt = studentRepository.findByEmail(result.getStudentEmail());
//                resultMap.put("studentName", studentOpt.map(Student::getName).orElse("Unknown"));
//                resultMap.put("examId", result.getExamId());
//                resultMap.put("score", result.getScore());
//                resultMap.put("totalQuestions", result.getTotalQuestions());
//                resultMap.put("examDate", result.getExamDate());
//
//                // Calculate section-wise scores
//                List<Section> sections = sectionRepository.findByExamIdOrderByOrderIndexAsc(result.getExamId());
//                List<Map<String, Object>> sectionResults = calculateSectionResults(result, sections);
//                resultMap.put("sectionResults", sectionResults);
//
//                // Compute total maximum marks for this exam (sum of section marks)
//                int totalMarksForExam = sections.stream()
//                        .map(s -> s.getMarks() != null ? s.getMarks() : 0)
//                        .mapToInt(Integer::intValue)
//                        .sum();
//                resultMap.put("totalMarks", totalMarksForExam);
//
//                // Calculate passed status based on section min pass marks
//                boolean passed = calculatePassedStatus(result, sections);
//                resultMap.put("passed", passed);
//
//                resultList.add(resultMap);
//            } catch (Exception e) {
//                logger.error("Error processing result with id {}: {}", result.getId(), e.getMessage(), e);
//                // Skip this result to avoid breaking the entire response
//            }
//        }
//        return resultList;
//    }
//
//    private boolean calculatePassedStatus(Result result, List<Section> sections) {
//        if (sections.isEmpty()) return true; // No sections, assume passed
//
//        // Parse answers
//        ObjectMapper mapper = new ObjectMapper();
//        Map<String, String> mcqAnswers = new HashMap<>();
//        if (result.getMcqAnswers() != null) {
//            try {
//                mcqAnswers = mapper.readValue(result.getMcqAnswers(), new TypeReference<Map<String, String>>() {});
//            } catch (Exception e) {
//                logger.error("Error parsing MCQ answers for result {}: {}", result.getId(), e);
//                return false;
//            }
//        }
//
//        Map<String, Map<String, String>> codingAnswers = new HashMap<>();
//        if (result.getCodingAnswers() != null) {
//            try {
//                codingAnswers = mapper.readValue(result.getCodingAnswers(), new TypeReference<Map<String, Map<String, String>>>() {});
//            } catch (Exception e) {
//                logger.error("Error parsing coding answers for result {}: {}", result.getId(), e);
//                return false;
//            }
//        }
//
//        for (Section section : sections) {
//            if (!section.getHasMinPassMarks() || section.getMinPassMarks() == null) continue; // Skip if no min pass
//
//            List<Question> questions = questionRepository.findBySectionId(section.getId());
//            int correctCount = 0;
//            for (Question q : questions) {
//                try {
//                    if (q.getIsCodingQuestion()) {
//                        Map<String, String> submittedCodeMap = codingAnswers.get(String.valueOf(q.getId()));
//                        if (submittedCodeMap != null && !submittedCodeMap.isEmpty()) {
//                            String lang = submittedCodeMap.keySet().stream().findFirst().orElse(null);
//                            String code = (lang != null) ? submittedCodeMap.get(lang) : null;
//
//                            if (code != null && !code.trim().isEmpty()) {
//                                Map<String, String> submissionRequest = new HashMap<>();
//                                submissionRequest.put("questionId", String.valueOf(q.getId()));
//                                submissionRequest.put("language", lang);
//                                submissionRequest.put("code", code);
//                                try {
//                                    ResponseEntity<Map<String, Object>> response = compilerController.submitCode(submissionRequest);
//                                    if (response.getStatusCode().is2xxSuccessful() && (Boolean) response.getBody().get("passed")) {
//                                        correctCount++;
//                                    }
//                                } catch (Exception e) {
//                                    logger.error("Error re-judging coding for result {}: {}", result.getId(), e);
//                                }
//                            }
//                        }
//                    } else {
//                        String answer = mcqAnswers.get(String.valueOf(q.getId()));
//                        if (answer != null && answer.equals(q.getCorrectAnswer())) {
//                            correctCount++;
//                        }
//                    }
//                } catch (Exception e) {
//                    logger.error("Error processing question {} for result {}: {}", q.getId(), result.getId(), e);
//                }
//            }
//
//            // Calculate section score
//            int sectionMarks = section.getMarks() != null ? section.getMarks() : 0;
//            double sectionScore;
//            if (sectionMarks > 0 && !questions.isEmpty()) {
//                sectionScore = (double) correctCount / questions.size() * sectionMarks;
//            } else {
//                sectionScore = correctCount;
//            }
//
//            if (Math.round(sectionScore) < section.getMinPassMarks()) {
//                return false; // Failed this section
//            }
//        }
//        return true; // Passed all sections with min pass
//    }
//
//    private List<Map<String, Object>> calculateSectionResults(Result result, List<Section> sections) {
//        List<Map<String, Object>> sectionResults = new ArrayList<>();
//
//        // Parse answers
//        ObjectMapper mapper = new ObjectMapper();
//        Map<String, String> mcqAnswers = new HashMap<>();
//        if (result.getMcqAnswers() != null) {
//            try {
//                mcqAnswers = mapper.readValue(result.getMcqAnswers(), new TypeReference<Map<String, String>>() {});
//            } catch (Exception e) {
//                logger.error("Error parsing MCQ answers", e);
//            }
//        }
//
//        Map<String, Map<String, String>> codingAnswers = new HashMap<>();
//        if (result.getCodingAnswers() != null) {
//            try {
//                codingAnswers = mapper.readValue(result.getCodingAnswers(), new TypeReference<Map<String, Map<String, String>>>() {});
//            } catch (Exception e) {
//                logger.error("Error parsing coding answers", e);
//            }
//        }
//
//        for (Section section : sections) {
//            List<Question> questions = questionRepository.findBySectionId(section.getId());
//            int correctCount = 0;
//            int sectionTotal = questions.size();
//            for (Question q : questions) {
//                try {
//                    if (q.getIsCodingQuestion()) {
//                        // For coding questions, re-judge the submission to get an accurate score.
//                        Map<String, String> submittedCodeMap = codingAnswers.get(String.valueOf(q.getId()));
//                        if (submittedCodeMap != null && !submittedCodeMap.isEmpty()) {
//                            // Find the language they submitted in (java, python, c, sql)
//                            String lang = submittedCodeMap.keySet().stream().findFirst().orElse(null);
//                            String code = (lang != null) ? submittedCodeMap.get(lang) : null;
//
//                            if (code != null && !code.trim().isEmpty()) {
//                                // Prepare request for compiler controller
//                                Map<String, String> submissionRequest = new HashMap<>();
//                                submissionRequest.put("questionId", String.valueOf(q.getId()));
//                                submissionRequest.put("language", lang);
//                                submissionRequest.put("code", code);
//
//                                // Judge the code
//                                ResponseEntity<Map<String, Object>> response = compilerController.submitCode(submissionRequest);
//                                if (response.getStatusCode().is2xxSuccessful() && (Boolean) response.getBody().get("passed")) {
//                                    correctCount++;
//                                }
//                            }
//                        }
//                    } else {
//                        String answer = mcqAnswers.get(String.valueOf(q.getId()));
//                        if (answer != null && answer.equals(q.getCorrectAnswer())) {
//                            correctCount++;
//                        }
//                    }
//                } catch (Exception e) {
//                    logger.error("Error processing question {} for result {}: {}", q.getId(), result.getId(), e);
//                }
//            }
//
//            // Calculate section score: if marks > 0, use proportional, else correct count
//            int sectionMarks = section.getMarks() != null ? section.getMarks() : 0;
//            double sectionScore;
//            if (sectionMarks > 0 && sectionTotal > 0) {
//                sectionScore = (double) correctCount / sectionTotal * sectionMarks;
//            } else {
//                sectionScore = correctCount;
//            }
//
//            Map<String, Object> sectionResult = new HashMap<>();
//            sectionResult.put("sectionId", section.getId());
//            sectionResult.put("sectionName", section.getName());
//            sectionResult.put("score", Math.round(sectionScore)); // Rounded score
//            sectionResult.put("total", sectionTotal);
//            sectionResult.put("marks", section.getMarks());
//            sectionResult.put("hasMinPassMarks", section.getHasMinPassMarks());
//            sectionResult.put("minPassMarks", section.getMinPassMarks());
//            sectionResults.add(sectionResult);
//        }
//
//        return sectionResults;
//    }
//
//    @GetMapping("/results/{id}/details")
//    public ResponseEntity<?> getDetailedResult(@PathVariable Long id) {
//        try {
//            Result result = resultRepository.findById(id).orElse(null);
//            if (result == null) {
//                return ResponseEntity.notFound().build();
//            }
//
//            // Get the exam
//            Exam exam = examRepository.findById(result.getExamId()).orElse(null);
//            if (exam == null) {
//                return ResponseEntity.badRequest().body("Exam not found");
//            }
//
//            // Get sections
//            List<Section> sections = sectionRepository.findByExamIdOrderByOrderIndexAsc(result.getExamId());
//
//            // Parse answers
//            ObjectMapper mapper = new ObjectMapper();
//            Map<String, String> mcqAnswers = new HashMap<>();
//            if (result.getMcqAnswers() != null) {
//                try {
//                    mcqAnswers = mapper.readValue(result.getMcqAnswers(), new TypeReference<Map<String, String>>() {});
//                } catch (Exception e) {
//                    logger.error("Error parsing MCQ answers", e);
//                }
//            }
//
//            Map<String, Map<String, String>> codingAnswers = new HashMap<>();
//            if (result.getCodingAnswers() != null) {
//                try {
//                    codingAnswers = mapper.readValue(result.getCodingAnswers(), new TypeReference<Map<String, Map<String, String>>>() {});
//                } catch (Exception e) {
//                    logger.error("Error parsing coding answers", e);
//                }
//            }
//
//            // Calculate section-wise scores
//            List<Map<String, Object>> sectionResults = new ArrayList<>();
//            for (Section section : sections) {
//                List<Question> questions = questionRepository.findBySectionId(section.getId());
//                int correctCount = 0;
//                int sectionTotal = questions.size();
//                for (Question q : questions) {
//                    try {
//                        if (q.getIsCodingQuestion()) {
//                            // For coding questions, re-judge the submission to get an accurate score.
//                            Map<String, String> submittedCodeMap = codingAnswers.get(String.valueOf(q.getId()));
//                            if (submittedCodeMap != null && !submittedCodeMap.isEmpty()) {
//                                String lang = submittedCodeMap.keySet().stream().findFirst().orElse(null);
//                                String code = (lang != null) ? submittedCodeMap.get(lang) : null;
//
//                                if (code != null && !code.trim().isEmpty()) {
//                                    Map<String, String> submissionRequest = new HashMap<>();
//                                    submissionRequest.put("questionId", String.valueOf(q.getId()));
//                                    submissionRequest.put("language", lang);
//                                    submissionRequest.put("code", code);
//
//                                    ResponseEntity<Map<String, Object>> response = compilerController.submitCode(submissionRequest);
//                                    if (response.getStatusCode().is2xxSuccessful() && (Boolean) response.getBody().get("passed")) {
//                                        correctCount++;
//                                    }
//                                }
//                            }
//                        } else {
//                            // For MCQ questions, check the stored answer.
//                            String answer = mcqAnswers.get(String.valueOf(q.getId()));
//                            if (answer != null && answer.equals(q.getCorrectAnswer())) {
//                                correctCount++;
//                            }
//                        }
//                    } catch (Exception e) {
//                        logger.error("Error processing question {} for result {}: {}", q.getId(), result.getId(), e);
//                    }
//                }
//
//                // Calculate section score: if marks > 0, use proportional, else correct count
//                int sectionMarks = section.getMarks() != null ? section.getMarks() : 0;
//                double sectionScore;
//                if (sectionMarks > 0 && sectionTotal > 0) {
//                    sectionScore = (double) correctCount / sectionTotal * sectionMarks;
//                } else {
//                    sectionScore = correctCount;
//                }
//
//                Map<String, Object> sectionResult = new HashMap<>();
//                sectionResult.put("sectionName", section.getName());
//                sectionResult.put("score", Math.round(sectionScore)); // Rounded score
//                sectionResult.put("total", sectionTotal);
//                sectionResult.put("marks", section.getMarks());
//                sectionResults.add(sectionResult);
//            }
//
//            Map<String, Object> response = new HashMap<>();
//            response.put("studentEmail", result.getStudentEmail());
//            response.put("examId", result.getExamId());
//            response.put("examName", exam.getExamName());
//            response.put("totalScore", result.getScore());
//            response.put("totalQuestions", result.getTotalQuestions());
//            // Total maximum marks across all sections in this exam
//            int totalMarks = sections.stream()
//                    .map(s -> s.getMarks() != null ? s.getMarks() : 0)
//                    .mapToInt(Integer::intValue)
//                    .sum();
//            response.put("totalMarks", totalMarks);
//            response.put("examDate", result.getExamDate());
//            response.put("sectionResults", sectionResults);
//
//            return ResponseEntity.ok(response);
//        } catch (Exception e) {
//            logger.error("Error getting detailed result for id {}: {}", id, e.getMessage(), e);
//            return ResponseEntity.status(500).body("Error retrieving detailed result");
//        }
//    }
//
//    // --- Section Management ---
//    
//    // Standalone section management (not tied to specific exam)
//    @PostMapping("/sections")
//    public ResponseEntity<Section> createStandaloneSection(@RequestBody Section section) {
//        try {
//            if (section.getMarks() == null) {
//                section.setMarks(0); // default
//            }
//            if (section.getHasMinPassMarks() == null) {
//                section.setHasMinPassMarks(false);
//            }
//            if (section.getMinPassMarks() == null) {
//                section.setMinPassMarks(0);
//            }
//            Section savedSection = sectionRepository.save(section);
//            return ResponseEntity.ok(savedSection);
//        } catch (Exception e) {
//            logger.error("Error creating standalone section: {}", section.getName(), e);
//            return ResponseEntity.status(500).body(null);
//        }
//    }
//    
//    @GetMapping("/sections")
//    public List<Section> getAllSections() {
//        return sectionRepository.findAll();
//    }
//    
//    @PutMapping("/sections/{sectionId}")
//    public ResponseEntity<Section> updateStandaloneSection(@PathVariable Integer sectionId, @RequestBody Section sectionDetails) {
//        try {
//            Section section = sectionRepository.findById(sectionId)
//                    .orElseThrow(() -> new RuntimeException("Section not found with id: " + sectionId));
//
//            section.setName(sectionDetails.getName());
//            section.setDurationInMinutes(sectionDetails.getDurationInMinutes());
//            section.setOrderIndex(sectionDetails.getOrderIndex());
//            section.setMarks(sectionDetails.getMarks());
//            section.setHasMinPassMarks(sectionDetails.getHasMinPassMarks());
//            section.setMinPassMarks(sectionDetails.getMinPassMarks());
//
//            final Section updatedSection = sectionRepository.save(section);
//            return ResponseEntity.ok(updatedSection);
//        } catch (Exception e) {
//            logger.error("Error updating standalone section with id: {}", sectionId, e);
//            return ResponseEntity.status(500).body(null);
//        }
//    }
//    
//    @DeleteMapping("/sections/{sectionId}")
//    public ResponseEntity<?> deleteStandaloneSection(@PathVariable Integer sectionId) {
//        sectionRepository.deleteById(sectionId);
//        return ResponseEntity.ok().build();
//    }
//
//    // Exam-specific section management
//    @PostMapping("/exams/{examId}/sections")
//    public ResponseEntity<Section> createSection(@PathVariable Integer examId, @RequestBody Section section) {
//        try {
//            Exam exam = examRepository.findById(examId)
//                    .orElseThrow(() -> new RuntimeException("Exam not found with id: " + examId));
//
//            // Validate section data
//            if (section.getName() == null || section.getName().trim().isEmpty()) {
//                return ResponseEntity.badRequest().body(null);
//            }
//            if (section.getDurationInMinutes() == null || section.getDurationInMinutes() <= 0) {
//                return ResponseEntity.badRequest().body(null);
//            }
//            if (section.getOrderIndex() == null || section.getOrderIndex() < 0) {
//                return ResponseEntity.badRequest().body(null);
//            }
//
//            // Check if section name already exists for this exam
//            if (sectionRepository.existsByNameAndExamId(section.getName(), examId)) {
//                logger.warn("Section '{}' already exists for exam ID: {}. Cannot create duplicate.", section.getName(), examId);
//                return ResponseEntity.badRequest().body(null);
//            }
//
//            section.setExam(exam);
//            if (section.getMarks() == null) {
//                section.setMarks(0); // default
//            }
//            if (section.getHasMinPassMarks() == null) {
//                section.setHasMinPassMarks(false);
//            }
//            if (section.getMinPassMarks() == null) {
//                section.setMinPassMarks(0);
//            }
//            Section savedSection = sectionRepository.save(section);
//            logger.info("Created section '{}' for exam ID: {} with order index {}", section.getName(), examId, section.getOrderIndex());
//            return ResponseEntity.ok(savedSection);
//        } catch (Exception e) {
//            logger.error("Error creating section for exam ID: {}", examId, e);
//            return ResponseEntity.status(500).body(null);
//        }
//    }
//
//    @GetMapping("/exams/{examId}/sections")
//    public List<Section> getSectionsByExam(@PathVariable Integer examId) {
//        return sectionRepository.findByExamIdOrderByOrderIndexAsc(examId);
//    }
//
//    @PutMapping("/exams/{examId}/sections/{sectionId}")
//    public ResponseEntity<Section> updateSection(@PathVariable Integer examId, @PathVariable Integer sectionId, @RequestBody Section sectionDetails) {
//        try {
//            Section section = sectionRepository.findById(sectionId)
//                    .orElseThrow(() -> new RuntimeException("Section not found with id: " + sectionId));
//
//            // Verify the section belongs to the specified exam
//            if (!section.getExam().getId().equals(examId)) {
//                throw new RuntimeException("Section does not belong to the specified exam");
//            }
//
//            section.setName(sectionDetails.getName());
//            section.setDurationInMinutes(sectionDetails.getDurationInMinutes());
//            section.setOrderIndex(sectionDetails.getOrderIndex());
//            section.setMarks(sectionDetails.getMarks());
//            section.setHasMinPassMarks(sectionDetails.getHasMinPassMarks());
//            section.setMinPassMarks(sectionDetails.getMinPassMarks());
//
//            final Section updatedSection = sectionRepository.save(section);
//            return ResponseEntity.ok(updatedSection);
//        } catch (Exception e) {
//            logger.error("Error updating section with id: {} for exam: {}", sectionId, examId, e);
//            return ResponseEntity.status(500).body(null);
//        }
//    }
//
//    @DeleteMapping("/exams/{examId}/sections/{sectionId}")
//    public ResponseEntity<?> deleteSection(@PathVariable Integer examId, @PathVariable Integer sectionId) {
//        try {
//            Section section = sectionRepository.findById(sectionId)
//                    .orElseThrow(() -> new RuntimeException("Section not found with id: " + sectionId));
//
//            // Verify the section belongs to the specified exam
//            if (!section.getExam().getId().equals(examId)) {
//                throw new RuntimeException("Section does not belong to the specified exam");
//            }
//
//            sectionRepository.deleteById(sectionId);
//            logger.info("Deleted section '{}' (ID: {}) for exam ID: {}", section.getName(), sectionId, examId);
//            return ResponseEntity.ok().build();
//        } catch (Exception e) {
//            logger.error("Error deleting section with id: {} for exam: {}", sectionId, examId, e);
//            return ResponseEntity.status(500).body(Map.of("message", "Error deleting section"));
//        }
//    }
//
//    // --- Section Cleanup Methods ---
//    @PostMapping("/exams/{examId}/sections/cleanup-duplicates")
//    public ResponseEntity<?> cleanupDuplicateSections(@PathVariable Integer examId) {
//        try {
//            List<Section> allSections = sectionRepository.findByExamIdOrderByOrderIndexAsc(examId);
//            List<Section> sectionsToDelete = new ArrayList<>();
//            Set<String> seenNames = new HashSet<>();
//
//            for (Section section : allSections) {
//                String sectionName = section.getName().toLowerCase();
//                if (seenNames.contains(sectionName)) {
//                    sectionsToDelete.add(section);
//                    logger.warn("Found duplicate section '{}' for exam ID: {}. Will be deleted.", section.getName(), examId);
//                } else {
//                    seenNames.add(sectionName);
//                }
//            }
//
//            int deletedCount = 0;
//            for (Section section : sectionsToDelete) {
//                sectionRepository.deleteById(section.getId());
//                deletedCount++;
//            }
//
//            if (deletedCount > 0) {
//                logger.info("Cleaned up {} duplicate sections for exam ID: {}", deletedCount, examId);
//                return ResponseEntity.ok(Map.of("message", "Cleaned up " + deletedCount + " duplicate sections"));
//            } else {
//                return ResponseEntity.ok(Map.of("message", "No duplicate sections found"));
//            }
//        } catch (Exception e) {
//            logger.error("Error cleaning up duplicate sections for exam ID: {}", examId, e);
//            return ResponseEntity.status(500).body(Map.of("message", "Error cleaning up duplicates"));
//        }
//    }
//
//    // --- Proctoring Management ---
//    @GetMapping("/proctoring/live-sessions")
//    public ResponseEntity<?> getLiveExamSessions() {
//        try {
//            List<LiveExamSession> sessions = liveExamSessionRepository.findActiveSessions();
//            List<Map<String, Object>> sessionData = new ArrayList<>();
//
//            for (LiveExamSession session : sessions) {
//                Map<String, Object> data = new HashMap<>();
//                data.put("studentEmail", session.getStudentEmail());
//
//                // Get student name
//                Optional<Student> studentOpt = studentRepository.findByEmail(session.getStudentEmail());
//                data.put("studentName", studentOpt.map(Student::getName).orElse("Unknown"));
//
//                data.put("examId", session.getExamId());
//
//                // Get exam name
//                Optional<Exam> examOpt = examRepository.findById(session.getExamId());
//                data.put("examName", examOpt.map(Exam::getExamName).orElse("Unknown Exam"));
//
//                data.put("startTime", session.getStartTime());
//                data.put("currentSection", session.getCurrentSection());
//                data.put("timeRemaining", session.getTimeRemaining());
//                data.put("status", session.getStatus().toString().toLowerCase());
//                data.put("warningCount", session.getWarningCount() != null ? session.getWarningCount() : 0);
//
//                // Get incidents for this session
//                data.put("incidents", new ArrayList<>());
//                data.put("incidentCount", 0);
//
//                sessionData.add(data);
//            }
//
//            return ResponseEntity.ok(Map.of("sessions", sessionData));
//        } catch (Exception e) {
//            logger.error("Error fetching live exam sessions", e);
//            return ResponseEntity.status(500).body(Map.of("message", "Error fetching live sessions"));
//        }
//    }
//
//    @GetMapping("/proctoring/incidents")
//    public ResponseEntity<?> getRecentIncidents() {
//        try {
//            return ResponseEntity.ok(Map.of("incidents", new ArrayList<>()));
//        } catch (Exception e) {
//            logger.error("Error fetching proctoring incidents", e);
//            return ResponseEntity.status(500).body(Map.of("message", "Error fetching incidents"));
//        }
//    }
//
//    @PostMapping("/proctoring/terminate")
//    public ResponseEntity<?> terminateSession(@RequestBody Map<String, Object> request) {
//        try {
//            String studentEmail = (String) request.get("studentEmail");
//            Integer examId = (Integer) request.get("examId");
//
//            if (studentEmail == null || examId == null) {
//                return ResponseEntity.badRequest().body(Map.of("message", "Missing required fields"));
//            }
//
//            Optional<LiveExamSession> sessionOpt = liveExamSessionRepository.findByStudentEmailAndExamId(studentEmail, examId);
//            if (sessionOpt.isPresent()) {
//                LiveExamSession session = sessionOpt.get();
//                session.setStatus(LiveExamSession.SessionStatus.TERMINATED);
//                session.setEndTime(java.time.LocalDateTime.now());
//                liveExamSessionRepository.save(session);
//
//                // Log termination incident (removed proctoring incident logging)
//
//                return ResponseEntity.ok(Map.of("message", "Session terminated successfully"));
//            } else {
//                return ResponseEntity.notFound().build();
//            }
//        } catch (Exception e) {
//            logger.error("Error terminating session", e);
//            return ResponseEntity.status(500).body(Map.of("message", "Error terminating session"));
//        }
//    }
//
//    @PostMapping("/proctoring/report-incident")
//    public ResponseEntity<?> reportIncident(@RequestBody Map<String, Object> request) {
//        try {
//            String studentEmail = (String) request.get("studentEmail");
//            Integer examId = (Integer) request.get("examId");
//
//            if (studentEmail == null || examId == null) {
//                return ResponseEntity.badRequest().body(Map.of("message", "Missing required fields"));
//            }
//
//            // Update session warning count if active session exists
//            Optional<LiveExamSession> sessionOpt = liveExamSessionRepository.findByStudentEmailAndExamId(studentEmail, examId);
//            if (sessionOpt.isPresent()) {
//                LiveExamSession session = sessionOpt.get();
//                session.setWarningCount(session.getWarningCount() + 1);
//
//                // If too many warnings, change status to WARNING or TERMINATED
//                if (session.getWarningCount() >= 5) {
//                    session.setStatus(LiveExamSession.SessionStatus.TERMINATED);
//                    session.setEndTime(java.time.LocalDateTime.now());
//                } else if (session.getWarningCount() >= 3) {
//                    session.setStatus(LiveExamSession.SessionStatus.WARNING);
//                }
//
//                LiveExamSession savedSession = liveExamSessionRepository.save(session);
//
//                // Return current warning count to frontend
//                return ResponseEntity.ok(Map.of(
//                    "message", "Incident reported successfully",
//                    "warningCount", savedSession.getWarningCount(),
//                    "status", savedSession.getStatus().toString().toLowerCase()
//                ));
//            }
//
//            return ResponseEntity.ok(Map.of("message", "Incident reported successfully"));
//        } catch (Exception e) {
//            logger.error("Error reporting incident", e);
//            return ResponseEntity.status(500).body(Map.of("message", "Error reporting incident"));
//        }
//    }
//
//    @GetMapping("/proctoring/recordings/{studentEmail}/{examId}")
//    public ResponseEntity<?> getRecordings(@PathVariable String studentEmail, @PathVariable Integer examId) {
//        try {
//            return ResponseEntity.ok(Map.of("recordings", new ArrayList<>()));
//        } catch (Exception e) {
//            logger.error("Error fetching recordings for student {} exam {}", studentEmail, examId, e);
//            return ResponseEntity.status(500).body(Map.of("message", "Error fetching recordings"));
//        }
//    }
//
//    // --- Email Management ---
//    @GetMapping("/student-counts")
//    public ResponseEntity<?> getStudentCounts() {
//        try {
//            // Use the same data source as getAllStudents() - studentRepository
//            long totalStudents = studentRepository.count();
//            long newStudentsCount = studentRepository.countByExamLinkSentIsNull();
//            long oldStudentsCount = studentRepository.countByExamLinkSentIsNotNull();
//
//            return ResponseEntity.ok(Map.of(
//                "totalStudents", totalStudents,
//                "newStudents", newStudentsCount,
//                "oldStudents", oldStudentsCount
//            ));
//        } catch (Exception e) {
//            logger.error("Error getting student counts", e);
//            return ResponseEntity.status(500).body(Map.of("message", "Error retrieving student counts"));
//        }
//    }
//
//    @PostMapping("/send-exam-link")
//    public ResponseEntity<?> sendExamLink(@RequestBody Map<String, Object> request) {
//        try {
//            Integer examId = (Integer) request.get("examId");
//            String customMessage = (String) request.get("customMessage");
//
//            if (examId == null) {
//                return ResponseEntity.badRequest().body(Map.of("message", "Exam ID is required"));
//            }
//
//            // Get exam details
//            Optional<Exam> examOpt = examRepository.findById(examId);
//            if (examOpt.isEmpty()) {
//                return ResponseEntity.badRequest().body(Map.of("message", "Exam not found"));
//            }
//            Exam exam = examOpt.get();
//
//            // Get all registered students
//            List<Student> students = studentRepository.findAll();
//            if (students.isEmpty()) {
//                return ResponseEntity.badRequest().body(Map.of("message", "No students found"));
//            }
//
//            // Prepare email content
//            String subject = "Exam Link: " + exam.getExamName();
//            String baseUrl = "http://localhost:4200"; // Update this to your actual frontend URL
//            String examLink = baseUrl + "/student-login?examId=" + examId;
//
//            StringBuilder body = new StringBuilder();
//            body.append("Dear Student,\n\n");
//            if (customMessage != null && !customMessage.trim().isEmpty()) {
//                body.append(customMessage).append("\n\n");
//            }
//            body.append("You have been invited to take the following exam:\n\n");
//            body.append("Exam Name: ").append(exam.getExamName()).append("\n");
//            body.append("Exam Link: ").append(examLink).append("\n\n");
//            body.append("Please click the link above to start your exam.\n\n");
//            body.append("Best regards,\n");
//            body.append("Exam Portal Administration");
//
//            // Send emails to all students
//            String[] emailAddresses = students.stream()
//                .map(Student::getEmail)
//                .toArray(String[]::new);
//
//            // Email service removed - bulk email sending disabled
//            // Update exam_link_sent timestamp for all students
//            List<Student> allStudents = studentRepository.findAll();
//            java.time.LocalDateTime now = java.time.LocalDateTime.now();
//            for (Student student : allStudents) {
//                student.setExamLinkSent(now);
//            }
//            studentRepository.saveAll(allStudents);
//
//            logger.info("Exam link tracking updated for {} students (email sending disabled)", emailAddresses.length);
//
//            return ResponseEntity.ok(Map.of(
//                "message", "Exam link emails are being sent to " + emailAddresses.length + " students",
//                "studentCount", emailAddresses.length
//            ));
//
//        } catch (Exception e) {
//            logger.error("Error sending exam link emails", e);
//            return ResponseEntity.status(500).body(Map.of("message", "Error sending emails"));
//        }
//    }
//
//    @PostMapping("/send-exam-link/new-students")
//    public ResponseEntity<?> sendExamLinkToNewStudents(@RequestBody Map<String, Object> request) {
//        try {
//            Integer examId = (Integer) request.get("examId");
//            String customMessage = (String) request.get("customMessage");
//
//            if (examId == null) {
//                return ResponseEntity.badRequest().body(Map.of("message", "Exam ID is required"));
//            }
//
//            // Get exam details
//            Optional<Exam> examOpt = examRepository.findById(examId);
//            if (examOpt.isEmpty()) {
//                return ResponseEntity.badRequest().body(Map.of("message", "Exam not found"));
//            }
//            Exam exam = examOpt.get();
//
//            // Get only new students (who haven't received exam link yet)
//            List<Student> newStudents = studentRepository.findByExamLinkSentIsNull();
//            if (newStudents.isEmpty()) {
//                return ResponseEntity.badRequest().body(Map.of("message", "No new students found"));
//            }
//
//            // Prepare email content
//            String subject = "Exam Link: " + exam.getExamName();
//            String baseUrl = "http://localhost:4200"; // Update this to your actual frontend URL
//            String examLink = baseUrl + "/student-login?examId=" + examId;
//
//            StringBuilder body = new StringBuilder();
//            body.append("Dear Student,\n\n");
//            if (customMessage != null && !customMessage.trim().isEmpty()) {
//                body.append(customMessage).append("\n\n");
//            }
//            body.append("You have been invited to take the following exam:\n\n");
//            body.append("Exam Name: ").append(exam.getExamName()).append("\n");
//            body.append("Exam Link: ").append(examLink).append("\n\n");
//            body.append("Please click the link above to start your exam.\n\n");
//            body.append("Best regards,\n");
//            body.append("Apical Soft Solutions");
//
//            // Send emails to new students only
//            String[] emailAddresses = newStudents.stream()
//                .map(Student::getEmail)
//                .toArray(String[]::new);
//
//            // Email service removed - bulk email sending disabled
//            // Update exam_link_sent timestamp for these students
//            java.time.LocalDateTime now = java.time.LocalDateTime.now();
//            for (Student student : newStudents) {
//                student.setExamLinkSent(now);
//            }
//            studentRepository.saveAll(newStudents);
//
//            logger.info("Exam link tracking updated for {} new students (email sending disabled)", emailAddresses.length);
//
//            return ResponseEntity.ok(Map.of(
//                "message", "Exam link emails are being sent to " + emailAddresses.length + " new students",
//                "studentCount", emailAddresses.length
//            ));
//
//        } catch (Exception e) {
//            logger.error("Error sending exam link emails to new students", e);
//            return ResponseEntity.status(500).body(Map.of("message", "Error sending emails"));
//        }
//    }
//}




//
//
//
//
//
//package com.exam.portal.controller;
//
//import com.exam.portal.model.*;
//import com.exam.portal.repository.*;
//import jakarta.persistence.EntityManager;
//import jakarta.persistence.PersistenceContext;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.bind.annotation.*;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//import java.util.Set;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.core.type.TypeReference;
//
//@RestController
//@RequestMapping("/api/admin")
//@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:4000"})
//public class AdminController {
//
//    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
//
//    @Autowired private StudentRepository studentRepository;
//    @Autowired private QuestionRepository questionRepository;
//    @Autowired private ExamRepository examRepository;
//    @Autowired private ResultRepository resultRepository;
//    @Autowired private SectionRepository sectionRepository;
//
//    @Autowired private CompilerController compilerController; // For judging coding answers
//    @Autowired private LiveExamSessionRepository liveExamSessionRepository;
//
//    // --- Student Management ---
//    @GetMapping("/students")
//    public List<Student> getAllStudents() {
//        return studentRepository.findAll();
//    }
//
//    // --- Question Management ---
//    @PostMapping("/questions")
//    public ResponseEntity<Question> addQuestion(@RequestBody Question questionDetails) {
//        try {
//            Question newQuestion = new Question();
//            newQuestion.setQuestionText(questionDetails.getQuestionText());
//
//            // Set section relationship
//            if (questionDetails.getSection() != null && questionDetails.getSection().getId() != null) {
//                Section section = sectionRepository.findById(questionDetails.getSection().getId())
//                        .orElseThrow(() -> new RuntimeException("Section not found"));
//                newQuestion.setSection(section);
//            }
//
//            boolean isCoding = isCodingQuestion(questionDetails);
//            newQuestion.setIsCodingQuestion(isCoding);
//
//            // Determine which fields to set based on the isCodingQuestion flag.
//            if (isCoding) {
//                // Set language-specific boilerplate codes
//                newQuestion.setBoilerplateJava(questionDetails.getBoilerplateJava());
//                newQuestion.setBoilerplatePython(questionDetails.getBoilerplatePython());
//                newQuestion.setBoilerplateC(questionDetails.getBoilerplateC());
//                newQuestion.setBoilerplateSql(questionDetails.getBoilerplateSql());
//                newQuestion.setTestCases(questionDetails.getTestCases());
//                // SQL dataset setup (per-question isolated environment)
//                newQuestion.setSetupSql(questionDetails.getSetupSql());
//
//                // Clear MCQ fields
//                newQuestion.setOption1(null);
//                newQuestion.setOption2(null);
//                newQuestion.setOption3(null);
//                newQuestion.setOption4(null);
//                newQuestion.setCorrectAnswer(null);
//            } else {
//                // Handle MCQ questions
//                newQuestion.setOption1(questionDetails.getOption1());
//                newQuestion.setOption2(questionDetails.getOption2());
//                newQuestion.setOption3(questionDetails.getOption3());
//                newQuestion.setOption4(questionDetails.getOption4());
//                newQuestion.setCorrectAnswer(questionDetails.getCorrectAnswer());
//
//                // Clear coding fields
//                newQuestion.setBoilerplateJava(null);
//                newQuestion.setBoilerplatePython(null);
//                newQuestion.setBoilerplateC(null);
//                newQuestion.setBoilerplateSql(null);
//                newQuestion.setTestCases(null);
//                newQuestion.setSetupSql(null);
//            }
//
//            Question savedQuestion = questionRepository.save(newQuestion);
//            return ResponseEntity.ok(savedQuestion);
//        } catch (Exception e) {
//            logger.error("Error adding question: {}", questionDetails.getQuestionText(), e);
//            return ResponseEntity.status(500).body(null);
//        }
//    }
//
//    @GetMapping("/questions")
//    public List<Question> getAllQuestions() {
//        return questionRepository.findAll();
//    }
//
//    @PutMapping("/questions/{id}")
//    public ResponseEntity<Question> updateQuestion(@PathVariable Long id, @RequestBody Question qDetails) {
//        try {
//            Question q = questionRepository.findById(id).orElseThrow(() -> new RuntimeException("Question not found with id: " + id));
//
//            q.setQuestionText(qDetails.getQuestionText());
//
//            // Set section relationship
//            if (qDetails.getSection() != null && qDetails.getSection().getId() != null) {
//                Section section = sectionRepository.findById(qDetails.getSection().getId())
//                        .orElseThrow(() -> new RuntimeException("Section not found"));
//                q.setSection(section);
//            }
//
//            boolean isCoding = isCodingQuestion(qDetails);
//            q.setIsCodingQuestion(isCoding);
//
//            // Determine which fields to set based on the isCodingQuestion flag.
//            if (q.getIsCodingQuestion()) {
//                // Set language-specific boilerplate codes
//                q.setBoilerplateJava(qDetails.getBoilerplateJava());
//                q.setBoilerplatePython(qDetails.getBoilerplatePython());
//                q.setBoilerplateC(qDetails.getBoilerplateC());
//                q.setBoilerplateSql(qDetails.getBoilerplateSql());
//                q.setTestCases(qDetails.getTestCases());
//                // SQL dataset setup (per-question isolated environment)
//                q.setSetupSql(qDetails.getSetupSql());
//
//                // Clear MCQ fields
//                q.setOption1(null);
//                q.setOption2(null);
//                q.setOption3(null);
//                q.setOption4(null);
//                q.setCorrectAnswer(null);
//            } else {
//                q.setOption1(qDetails.getOption1());
//                q.setOption2(qDetails.getOption2());
//                q.setOption3(qDetails.getOption3());
//                q.setOption4(qDetails.getOption4());
//                q.setCorrectAnswer(qDetails.getCorrectAnswer());
//
//                // Clear coding fields
//                q.setBoilerplateJava(null);
//                q.setBoilerplatePython(null);
//                q.setBoilerplateC(null);
//                q.setBoilerplateSql(null);
//                q.setTestCases(null);
//                q.setSetupSql(null);
//            }
//
//            final Question updatedQuestion = questionRepository.save(q);
//            return ResponseEntity.ok(updatedQuestion);
//        } catch (Exception e) {
//            logger.error("Error updating question with id: {}", id, e);
//            return ResponseEntity.status(500).body(null);
//        }
//    }
//
//    private boolean isCodingQuestion(Question question) {
//        // Prefer the explicit flag from the request
//        if (Boolean.TRUE.equals(question.getIsCodingQuestion())) {
//            return true;
//        }
//        // Fallback to checking the section name
//        return question.getSection() != null && question.getSection().getName() != null &&
//               ("Coding".equalsIgnoreCase(question.getSection().getName()) || "SQL".equalsIgnoreCase(question.getSection().getName()));
//    }
//
//    @DeleteMapping("/questions/{id}")
//    public ResponseEntity<?> deleteQuestion(@PathVariable Long id) {
//        questionRepository.deleteById(id);
//        return ResponseEntity.ok().build();
//    }
//
//    // --- Exam Management ---
//    @PostMapping("/exams")
//    public Exam createExam(@RequestBody Exam exam) {
//        return examRepository.save(exam);
//    }
//
//    @PostMapping("/exams/with-sections")
//    public ResponseEntity<Exam> createExamWithSections(@RequestBody Map<String, Object> examData) {
//        try {
//            String examName = (String) examData.get("examName");
//            @SuppressWarnings("unchecked")
//            List<Map<String, Object>> sectionsData = (List<Map<String, Object>>) examData.get("sections");
//
//            // Validate input
//            if (examName == null || examName.trim().isEmpty()) {
//                return ResponseEntity.badRequest().body(null);
//            }
//            if (sectionsData == null || sectionsData.isEmpty()) {
//                return ResponseEntity.badRequest().body(null);
//            }
//
//            // Create the exam
//            Exam exam = new Exam();
//            exam.setExamName(examName);
//            exam.setActive(false); // New exams are inactive by default
//            Exam savedExam = examRepository.save(exam);
//
//            // Link selected sections to this exam
//            List<Section> sections = new ArrayList<>();
//            Set<String> sectionNames = new HashSet<>(); // Track section names to prevent duplicates
//
//            for (Map<String, Object> sectionData : sectionsData) {
//                Integer sectionId = (Integer) sectionData.get("id");
//                Integer durationInMinutes = (Integer) sectionData.get("durationInMinutes");
//                Integer orderIndex = (Integer) sectionData.get("orderIndex");
//
//                // Validate section data
//                if (sectionId == null) {
//                    logger.warn("Skipping section with null ID for exam: {}", examName);
//                    continue;
//                }
//                if (durationInMinutes == null || durationInMinutes <= 0) {
//                    logger.warn("Skipping section ID '{}' with invalid duration for exam: {}", sectionId, examName);
//                    continue;
//                }
//                if (orderIndex == null || orderIndex < 0) {
//                    logger.warn("Skipping section ID '{}' with invalid order index for exam: {}", sectionId, examName);
//                    continue;
//                }
//
//                // Find the standalone section by ID and link it to the exam
//                Section sectionToLink = sectionRepository.findById(sectionId)
//                    .orElse(null);
//
//                if (sectionToLink == null) {
//                    logger.warn("No standalone section found with ID '{}' for exam: {}. Skipping.", sectionId, examName);
//                    continue;
//                }
//
//                // Update the section to link to the exam
//                sectionToLink.setExam(savedExam);
//                sectionToLink.setOrderIndex(orderIndex); // Update order index
//                Section savedSection = sectionRepository.save(sectionToLink);
//                sections.add(savedSection);
//                sectionNames.add(savedSection.getName().toLowerCase());
//
//                logger.info("Linked section '{}' (ID: {}) to exam '{}' with order index {}", savedSection.getName(), sectionId, examName, orderIndex);
//            }
//
//            if (sections.isEmpty()) {
//                // If no valid sections were created, delete the exam and return error
//                examRepository.delete(savedExam);
//                return ResponseEntity.badRequest().body(null);
//            }
//
//            savedExam.setSections(sections);
//            logger.info("Successfully created exam '{}' with {} sections", examName, sections.size());
//            return ResponseEntity.ok(savedExam);
//
//        } catch (Exception e) {
//            logger.error("Error creating exam with sections: {}", examData.get("examName"), e);
//            return ResponseEntity.status(500).body(null);
//        }
//    }
//
//    @GetMapping("/exams")
//    public List<Exam> getAllExams() {
//        return examRepository.findAll();
//    }
//
//    @Transactional
//    @PutMapping("/exams/{id}")
//    public ResponseEntity<Exam> updateExam(@PathVariable Integer id, @RequestParam boolean isActive) {
//        try {
//            // Ensure the target exam exists before proceeding
//            if (!examRepository.existsById(id)) {
//                throw new RuntimeException("Exam not found with id: " + id);
//            }
//
//            if (isActive) {
//                // If activating one, deactivate all others.
//                List<Exam> allExams = examRepository.findAll();
//                for (Exam exam : allExams) {
//                    exam.setActive(exam.getId().equals(id));
//                }
//                examRepository.saveAll(allExams);
//            } else {
//                // If deactivating, just update the specific exam.
//                examRepository.deactivateExamById(id);
//            }
//
//            // Re-fetch the exam to return its final, correct state.
//            return ResponseEntity.ok(examRepository.findById(id).get());
//        } catch (Exception e) {
//            logger.error("Error updating exam with id: {}", id, e);
//            return ResponseEntity.status(500).body(null);
//        }
//    }
//
//    @PutMapping("/exams/{id}/details")
//    public ResponseEntity<Exam> updateExamDetails(@PathVariable Integer id, @RequestBody Map<String, Object> examData) {
//        try {
//            Exam exam = examRepository.findById(id)
//                    .orElseThrow(() -> new RuntimeException("Exam not found with id: " + id));
//
//            if (examData.containsKey("examName")) {
//                exam.setExamName((String) examData.get("examName"));
//            }
//
//            // Save the exam name change first
//            exam = examRepository.save(exam);
//
//            // Update sections
//            // First, clear existing sections
//            List<Section> existingSections = sectionRepository.findByExamIdOrderByOrderIndexAsc(id);
//            for (Section s : existingSections) {
//                s.setExam(null);
//                sectionRepository.save(s);
//                s.setOrderIndex(null); // Also clear the order index
//            }
//
//            // Add new sections
//            @SuppressWarnings("unchecked")
//            List<Map<String, Object>> sectionsData = (List<Map<String, Object>>) examData.get("sections");
//            if (sectionsData != null) {
//                for (Map<String, Object> sd : sectionsData) {
//                    Integer sid = (Integer) sd.get("id");
//                    Section s = sectionRepository.findById(sid).orElse(null);
//                    if (s != null) {
//                        s.setExam(exam);
//                        s.setOrderIndex((Integer) sd.get("orderIndex"));
//                        // The save operation will link it back
//                        sectionRepository.save(s);
//                    }
//                }
//            }
//
//            // Re-fetch the exam with all its updated associations to return the freshest state
//            return ResponseEntity.ok(examRepository.findById(id).get());
//        } catch (Exception e) {
//            logger.error("Error updating exam details for id: {}", id, e);
//            return ResponseEntity.status(500).body(null);
//        }
//    }
//
//    @DeleteMapping("/exams/{id}")
//    public ResponseEntity<?> deleteExam(@PathVariable Integer id) {
//        try {
//            Exam exam = examRepository.findById(id)
//                    .orElseThrow(() -> new RuntimeException("Exam not found"));
//
//            if (exam.getIsActive()) {
//                return ResponseEntity.badRequest().body("Cannot delete active exam");
//            }
//
//            // Clear sections
//            List<Section> sections = sectionRepository.findByExamIdOrderByOrderIndexAsc(id);
//            for (Section s : sections) {
//                s.setExam(null);
//                sectionRepository.save(s);
//            }
//
//            examRepository.delete(exam);
//            return ResponseEntity.ok().build();
//        } catch (Exception e) {
//            logger.error("Error deleting exam with id: {}", id, e);
//            return ResponseEntity.status(500).body(Map.of("message", "Error deleting exam"));
//        }
//    }
//    
//    // --- Result Management ---
//    @GetMapping("/results")
//    public List<Map<String, Object>> getAllResults() {
//        List<Result> results = resultRepository.findAll();
//        List<Map<String, Object>> resultList = new ArrayList<>();
//        for (Result result : results) {
//            try {
//                Map<String, Object> resultMap = new HashMap<>();
//                resultMap.put("id", result.getId());
//                resultMap.put("studentEmail", result.getStudentEmail());
//                // Get student name
//                Optional<Student> studentOpt = studentRepository.findByEmail(result.getStudentEmail());
//                resultMap.put("studentName", studentOpt.map(Student::getName).orElse("Unknown"));
//                resultMap.put("examId", result.getExamId());
//                resultMap.put("score", result.getScore());
//                resultMap.put("totalQuestions", result.getTotalQuestions());
//                resultMap.put("examDate", result.getExamDate());
//
//                // Calculate section-wise scores
//                List<Section> sections = sectionRepository.findByExamIdOrderByOrderIndexAsc(result.getExamId());
//                List<Map<String, Object>> sectionResults = calculateSectionResults(result, sections);
//                resultMap.put("sectionResults", sectionResults);
//
//                // Compute total maximum marks for this exam (sum of section marks)
//                int totalMarksForExam = sections.stream()
//                        .map(s -> s.getMarks() != null ? s.getMarks() : 0)
//                        .mapToInt(Integer::intValue)
//                        .sum();
//                resultMap.put("totalMarks", totalMarksForExam);
//
//                // Calculate passed status based on section min pass marks
//                boolean passed = calculatePassedStatus(result, sections);
//                resultMap.put("passed", passed);
//
//                resultList.add(resultMap);
//            } catch (Exception e) {
//                logger.error("Error processing result with id {}: {}", result.getId(), e.getMessage(), e);
//                // Skip this result to avoid breaking the entire response
//            }
//        }
//        return resultList;
//    }
//
//    private boolean calculatePassedStatus(Result result, List<Section> sections) {
//        if (sections.isEmpty()) return true; // No sections, assume passed
//
//        // Parse answers
//        ObjectMapper mapper = new ObjectMapper();
//        Map<String, String> mcqAnswers = new HashMap<>();
//        if (result.getMcqAnswers() != null) {
//            try {
//                mcqAnswers = mapper.readValue(result.getMcqAnswers(), new TypeReference<Map<String, String>>() {});
//            } catch (Exception e) {
//                logger.error("Error parsing MCQ answers for result {}: {}", result.getId(), e);
//                return false;
//            }
//        }
//
//        Map<String, Map<String, String>> codingAnswers = new HashMap<>();
//        if (result.getCodingAnswers() != null) {
//            try {
//                codingAnswers = mapper.readValue(result.getCodingAnswers(), new TypeReference<Map<String, Map<String, String>>>() {});
//            } catch (Exception e) {
//                logger.error("Error parsing coding answers for result {}: {}", result.getId(), e);
//                return false;
//            }
//        }
//
//        for (Section section : sections) {
//            if (!section.getHasMinPassMarks() || section.getMinPassMarks() == null) continue; // Skip if no min pass
//
//            List<Question> questions = questionRepository.findBySectionId(section.getId());
//            int correctCount = 0;
//            for (Question q : questions) {
//                try {
//                    if (q.getIsCodingQuestion()) {
//                        Map<String, String> submittedCodeMap = codingAnswers.get(String.valueOf(q.getId()));
//                        if (submittedCodeMap != null && !submittedCodeMap.isEmpty()) {
//                            String lang = submittedCodeMap.keySet().stream().findFirst().orElse(null);
//                            String code = (lang != null) ? submittedCodeMap.get(lang) : null;
//
//                            if (code != null && !code.trim().isEmpty()) {
//                                Map<String, String> submissionRequest = new HashMap<>();
//                                submissionRequest.put("questionId", String.valueOf(q.getId()));
//                                submissionRequest.put("language", lang);
//                                submissionRequest.put("code", code);
//                                try {
//                                    ResponseEntity<Map<String, Object>> response = compilerController.submitCode(submissionRequest);
//                                    if (response.getStatusCode().is2xxSuccessful() && Boolean.TRUE.equals(response.getBody().get("passed"))) {
//                                        correctCount++;
//                                    }
//                                } catch (Exception e) {
//                                    logger.error("Error re-judging coding for result {}: {}", result.getId(), e);
//                                }
//                            }
//                        }
//                    } else {
//                        String answer = mcqAnswers.get(String.valueOf(q.getId()));
//                        if (answer != null && answer.equals(q.getCorrectAnswer())) {
//                            correctCount++;
//                        }
//                    }
//                } catch (Exception e) {
//                    logger.error("Error processing question {} for result {}: {}", q.getId(), result.getId(), e);
//                }
//            }
//
//            // Calculate section score
//            int sectionMarks = section.getMarks() != null ? section.getMarks() : 0;
//            double sectionScore;
//            if (sectionMarks > 0 && !questions.isEmpty()) {
//                sectionScore = (double) correctCount / questions.size() * sectionMarks;
//            } else {
//                sectionScore = correctCount;
//            }
//
//            if (Math.round(sectionScore) < section.getMinPassMarks()) {
//                return false; // Failed this section
//            }
//        }
//        return true; // Passed all sections with min pass
//    }
//
//    private List<Map<String, Object>> calculateSectionResults(Result result, List<Section> sections) {
//        List<Map<String, Object>> sectionResults = new ArrayList<>();
//
//        // Parse answers
//        ObjectMapper mapper = new ObjectMapper();
//        Map<String, String> mcqAnswers = new HashMap<>();
//        if (result.getMcqAnswers() != null) {
//            try {
//                mcqAnswers = mapper.readValue(result.getMcqAnswers(), new TypeReference<Map<String, String>>() {});
//            } catch (Exception e) {
//                logger.error("Error parsing MCQ answers", e);
//            }
//        }
//
//        Map<String, Map<String, String>> codingAnswers = new HashMap<>();
//        if (result.getCodingAnswers() != null) {
//            try {
//                codingAnswers = mapper.readValue(result.getCodingAnswers(), new TypeReference<Map<String, Map<String, String>>>() {});
//            } catch (Exception e) {
//                logger.error("Error parsing coding answers", e);
//            }
//        }
//
//        for (Section section : sections) {
//            List<Question> questions = questionRepository.findBySectionId(section.getId());
//            int correctCount = 0;
//            int sectionTotal = questions.size();
//            for (Question q : questions) {
//                try {
//                    if (q.getIsCodingQuestion()) {
//                        // For coding questions, re-judge the submission to get an accurate score.
//                        Map<String, String> submittedCodeMap = codingAnswers.get(String.valueOf(q.getId()));
//                        if (submittedCodeMap != null && !submittedCodeMap.isEmpty()) {
//                            // Find the language they submitted in (java, python, c, sql)
//                            String lang = submittedCodeMap.keySet().stream().findFirst().orElse(null);
//                            String code = (lang != null) ? submittedCodeMap.get(lang) : null;
//
//                            if (code != null && !code.trim().isEmpty()) {
//                                // Prepare request for compiler controller
//                                Map<String, String> submissionRequest = new HashMap<>();
//                                submissionRequest.put("questionId", String.valueOf(q.getId()));
//                                submissionRequest.put("language", lang);
//                                submissionRequest.put("code", code);
//
//                                // Judge the code
//                                ResponseEntity<Map<String, Object>> response = compilerController.submitCode(submissionRequest);
//                                if (response.getStatusCode().is2xxSuccessful() && Boolean.TRUE.equals(response.getBody().get("passed"))) {
//                                    correctCount++;
//                                }
//                            }
//                        }
//                    } else {
//                        String answer = mcqAnswers.get(String.valueOf(q.getId()));
//                        if (answer != null && answer.equals(q.getCorrectAnswer())) {
//                            correctCount++;
//                        }
//                    }
//                } catch (Exception e) {
//                    logger.error("Error processing question {} for result {}: {}", q.getId(), result.getId(), e);
//                }
//            }
//
//            // Calculate section score: if marks > 0, use proportional, else correct count
//            int sectionMarks = section.getMarks() != null ? section.getMarks() : 0;
//            double sectionScore;
//            if (sectionMarks > 0 && sectionTotal > 0) {
//                sectionScore = (double) correctCount / sectionTotal * sectionMarks;
//            } else {
//                sectionScore = correctCount;
//            }
//
//            Map<String, Object> sectionResult = new HashMap<>();
//            sectionResult.put("sectionId", section.getId());
//            sectionResult.put("sectionName", section.getName());
//            sectionResult.put("score", Math.round(sectionScore)); // Rounded score
//            sectionResult.put("total", sectionTotal);
//            sectionResult.put("marks", section.getMarks());
//            sectionResult.put("hasMinPassMarks", section.getHasMinPassMarks());
//            sectionResult.put("minPassMarks", section.getMinPassMarks());
//            sectionResults.add(sectionResult);
//        }
//
//        return sectionResults;
//    }
//
//    @GetMapping("/results/{id}/details")
//    public ResponseEntity<?> getDetailedResult(@PathVariable Long id) {
//        try {
//            Result result = resultRepository.findById(id).orElse(null);
//            if (result == null) {
//                return ResponseEntity.notFound().build();
//            }
//
//            // Get the exam
//            Exam exam = examRepository.findById(result.getExamId()).orElse(null);
//            if (exam == null) {
//                return ResponseEntity.badRequest().body("Exam not found");
//            }
//
//            // Get sections
//            List<Section> sections = sectionRepository.findByExamIdOrderByOrderIndexAsc(result.getExamId());
//
//            // Parse answers
//            ObjectMapper mapper = new ObjectMapper();
//            Map<String, String> mcqAnswers = new HashMap<>();
//            if (result.getMcqAnswers() != null) {
//                try {
//                    mcqAnswers = mapper.readValue(result.getMcqAnswers(), new TypeReference<Map<String, String>>() {});
//                } catch (Exception e) {
//                    logger.error("Error parsing MCQ answers", e);
//                }
//            }
//
//            Map<String, Map<String, String>> codingAnswers = new HashMap<>();
//            if (result.getCodingAnswers() != null) {
//                try {
//                    codingAnswers = mapper.readValue(result.getCodingAnswers(), new TypeReference<Map<String, Map<String, String>>>() {});
//                } catch (Exception e) {
//                    logger.error("Error parsing coding answers", e);
//                }
//            }
//
//            // Calculate section-wise scores
//            List<Map<String, Object>> sectionResults = new ArrayList<>();
//            for (Section section : sections) {
//                List<Question> questions = questionRepository.findBySectionId(section.getId());
//                int correctCount = 0;
//                int sectionTotal = questions.size();
//                for (Question q : questions) {
//                    try {
//                        if (q.getIsCodingQuestion()) {
//                            // For coding questions, re-judge the submission to get an accurate score.
//                            Map<String, String> submittedCodeMap = codingAnswers.get(String.valueOf(q.getId()));
//                            if (submittedCodeMap != null && !submittedCodeMap.isEmpty()) {
//                                String lang = submittedCodeMap.keySet().stream().findFirst().orElse(null);
//                                String code = (lang != null) ? submittedCodeMap.get(lang) : null;
//
//                                if (code != null && !code.trim().isEmpty()) {
//                                    Map<String, String> submissionRequest = new HashMap<>();
//                                    submissionRequest.put("questionId", String.valueOf(q.getId()));
//                                    submissionRequest.put("language", lang);
//                                    submissionRequest.put("code", code);
//
//                                    ResponseEntity<Map<String, Object>> response = compilerController.submitCode(submissionRequest);
//                                    if (response.getStatusCode().is2xxSuccessful() && Boolean.TRUE.equals(response.getBody().get("passed"))) {
//                                        correctCount++;
//                                    }
//                                }
//                            }
//                        } else {
//                            // For MCQ questions, check the stored answer.
//                            String answer = mcqAnswers.get(String.valueOf(q.getId()));
//                            if (answer != null && answer.equals(q.getCorrectAnswer())) {
//                                correctCount++;
//                            }
//                        }
//                    } catch (Exception e) {
//                        logger.error("Error processing question {} for result {}: {}", q.getId(), result.getId(), e);
//                    }
//                }
//
//                // Calculate section score: if marks > 0, use proportional, else correct count
//                int sectionMarks = section.getMarks() != null ? section.getMarks() : 0;
//                double sectionScore;
//                if (sectionMarks > 0 && sectionTotal > 0) {
//                    sectionScore = (double) correctCount / sectionTotal * sectionMarks;
//                } else {
//                    sectionScore = correctCount;
//                }
//
//                Map<String, Object> sectionResult = new HashMap<>();
//                sectionResult.put("sectionName", section.getName());
//                sectionResult.put("score", Math.round(sectionScore)); // Rounded score
//                sectionResult.put("total", sectionTotal);
//                sectionResult.put("marks", section.getMarks());
//                sectionResults.add(sectionResult);
//            }
//
//            Map<String, Object> response = new HashMap<>();
//            response.put("studentEmail", result.getStudentEmail());
//            response.put("examId", result.getExamId());
//            response.put("examName", exam.getExamName());
//            response.put("totalScore", result.getScore());
//            response.put("totalQuestions", result.getTotalQuestions());
//            // Total maximum marks across all sections in this exam
//            int totalMarks = sections.stream()
//                    .map(s -> s.getMarks() != null ? s.getMarks() : 0)
//                    .mapToInt(Integer::intValue)
//                    .sum();
//            response.put("totalMarks", totalMarks);
//            response.put("examDate", result.getExamDate());
//            response.put("sectionResults", sectionResults);
//
//            return ResponseEntity.ok(response);
//        } catch (Exception e) {
//            logger.error("Error getting detailed result for id {}: {}", id, e.getMessage(), e);
//            return ResponseEntity.status(500).body("Error retrieving detailed result");
//        }
//    }
//
//    // --- Section Management ---
//    
//    // Standalone section management (not tied to specific exam)
//    @PostMapping("/sections")
//    public ResponseEntity<Section> createStandaloneSection(@RequestBody Section section) {
//        try {
//            if (section.getMarks() == null) {
//                section.setMarks(0); // default
//            }
//            if (section.getHasMinPassMarks() == null) {
//                section.setHasMinPassMarks(false);
//            }
//            if (section.getMinPassMarks() == null) {
//                section.setMinPassMarks(0);
//            }
//            Section savedSection = sectionRepository.save(section);
//            return ResponseEntity.ok(savedSection);
//        } catch (Exception e) {
//            logger.error("Error creating standalone section: {}", section.getName(), e);
//            return ResponseEntity.status(500).body(null);
//        }
//    }
//    
//    @GetMapping("/sections")
//    public List<Section> getAllSections() {
//        return sectionRepository.findAll();
//    }
//    
//    @PutMapping("/sections/{sectionId}")
//    public ResponseEntity<Section> updateStandaloneSection(@PathVariable Integer sectionId, @RequestBody Section sectionDetails) {
//        try {
//            Section section = sectionRepository.findById(sectionId)
//                    .orElseThrow(() -> new RuntimeException("Section not found with id: " + sectionId));
//
//            section.setName(sectionDetails.getName());
//            section.setDurationInMinutes(sectionDetails.getDurationInMinutes());
//            section.setOrderIndex(sectionDetails.getOrderIndex());
//            section.setMarks(sectionDetails.getMarks());
//            section.setHasMinPassMarks(sectionDetails.getHasMinPassMarks());
//            section.setMinPassMarks(sectionDetails.getMinPassMarks());
//
//            final Section updatedSection = sectionRepository.save(section);
//            return ResponseEntity.ok(updatedSection);
//        } catch (Exception e) {
//            logger.error("Error updating standalone section with id: {}", sectionId, e);
//            return ResponseEntity.status(500).body(null);
//        }
//    }
//    
//    @DeleteMapping("/sections/{sectionId}")
//    public ResponseEntity<?> deleteStandaloneSection(@PathVariable Integer sectionId) {
//        sectionRepository.deleteById(sectionId);
//        return ResponseEntity.ok().build();
//    }
//
//    // Exam-specific section management
//    @PostMapping("/exams/{examId}/sections")
//    public ResponseEntity<Section> createSection(@PathVariable Integer examId, @RequestBody Section section) {
//        try {
//            Exam exam = examRepository.findById(examId)
//                    .orElseThrow(() -> new RuntimeException("Exam not found with id: " + examId));
//
//            // Validate section data
//            if (section.getName() == null || section.getName().trim().isEmpty()) {
//                return ResponseEntity.badRequest().body(null);
//            }
//            if (section.getDurationInMinutes() == null || section.getDurationInMinutes() <= 0) {
//                return ResponseEntity.badRequest().body(null);
//            }
//            if (section.getOrderIndex() == null || section.getOrderIndex() < 0) {
//                return ResponseEntity.badRequest().body(null);
//            }
//
//            // Check if section name already exists for this exam
//            if (sectionRepository.existsByNameAndExamId(section.getName(), examId)) {
//                logger.warn("Section '{}' already exists for exam ID: {}. Cannot create duplicate.", section.getName(), examId);
//                return ResponseEntity.badRequest().body(null);
//            }
//
//            section.setExam(exam);
//            if (section.getMarks() == null) {
//                section.setMarks(0); // default
//            }
//            if (section.getHasMinPassMarks() == null) {
//                section.setHasMinPassMarks(false);
//            }
//            if (section.getMinPassMarks() == null) {
//                section.setMinPassMarks(0);
//            }
//            Section savedSection = sectionRepository.save(section);
//            logger.info("Created section '{}' for exam ID: {} with order index {}", section.getName(), examId, section.getOrderIndex());
//            return ResponseEntity.ok(savedSection);
//        } catch (Exception e) {
//            logger.error("Error creating section for exam ID: {}", examId, e);
//            return ResponseEntity.status(500).body(null);
//        }
//    }
//
//    @GetMapping("/exams/{examId}/sections")
//    public List<Section> getSectionsByExam(@PathVariable Integer examId) {
//        return sectionRepository.findByExamIdOrderByOrderIndexAsc(examId);
//    }
//
//    @PutMapping("/exams/{examId}/sections/{sectionId}")
//    public ResponseEntity<Section> updateSection(@PathVariable Integer examId, @PathVariable Integer sectionId, @RequestBody Section sectionDetails) {
//        try {
//            Section section = sectionRepository.findById(sectionId)
//                    .orElseThrow(() -> new RuntimeException("Section not found with id: " + sectionId));
//
//            // Verify the section belongs to the specified exam
//            if (!section.getExam().getId().equals(examId)) {
//                throw new RuntimeException("Section does not belong to the specified exam");
//            }
//
//            section.setName(sectionDetails.getName());
//            section.setDurationInMinutes(sectionDetails.getDurationInMinutes());
//            section.setOrderIndex(sectionDetails.getOrderIndex());
//            section.setMarks(sectionDetails.getMarks());
//            section.setHasMinPassMarks(sectionDetails.getHasMinPassMarks());
//            section.setMinPassMarks(sectionDetails.getMinPassMarks());
//
//            final Section updatedSection = sectionRepository.save(section);
//            return ResponseEntity.ok(updatedSection);
//        } catch (Exception e) {
//            logger.error("Error updating section with id: {} for exam: {}", sectionId, examId, e);
//            return ResponseEntity.status(500).body(null);
//        }
//    }
//
//    @DeleteMapping("/exams/{examId}/sections/{sectionId}")
//    public ResponseEntity<?> deleteSection(@PathVariable Integer examId, @PathVariable Integer sectionId) {
//        try {
//            Section section = sectionRepository.findById(sectionId)
//                    .orElseThrow(() -> new RuntimeException("Section not found with id: " + sectionId));
//
//            // Verify the section belongs to the specified exam
//            if (!section.getExam().getId().equals(examId)) {
//                throw new RuntimeException("Section does not belong to the specified exam");
//            }
//
//            sectionRepository.deleteById(sectionId);
//            logger.info("Deleted section '{}' (ID: {}) for exam ID: {}", section.getName(), sectionId, examId);
//            return ResponseEntity.ok().build();
//        } catch (Exception e) {
//            logger.error("Error deleting section with id: {} for exam: {}", sectionId, examId, e);
//            return ResponseEntity.status(500).body(Map.of("message", "Error deleting section"));
//        }
//    }
//
//    // --- Section Cleanup Methods ---
//    @PostMapping("/exams/{examId}/sections/cleanup-duplicates")
//    public ResponseEntity<?> cleanupDuplicateSections(@PathVariable Integer examId) {
//        try {
//            List<Section> allSections = sectionRepository.findByExamIdOrderByOrderIndexAsc(examId);
//            List<Section> sectionsToDelete = new ArrayList<>();
//            Set<String> seenNames = new HashSet<>();
//
//            for (Section section : allSections) {
//                String sectionName = section.getName().toLowerCase();
//                if (seenNames.contains(sectionName)) {
//                    sectionsToDelete.add(section);
//                    logger.warn("Found duplicate section '{}' for exam ID: {}. Will be deleted.", section.getName(), examId);
//                } else {
//                    seenNames.add(sectionName);
//                }
//            }
//
//            int deletedCount = 0;
//            for (Section section : sectionsToDelete) {
//                sectionRepository.deleteById(section.getId());
//                deletedCount++;
//            }
//
//            if (deletedCount > 0) {
//                logger.info("Cleaned up {} duplicate sections for exam ID: {}", deletedCount, examId);
//                return ResponseEntity.ok(Map.of("message", "Cleaned up " + deletedCount + " duplicate sections"));
//            } else {
//                return ResponseEntity.ok(Map.of("message", "No duplicate sections found"));
//            }
//        } catch (Exception e) {
//            logger.error("Error cleaning up duplicate sections for exam ID: {}", examId, e);
//            return ResponseEntity.status(500).body(Map.of("message", "Error cleaning up duplicates"));
//        }
//    }
//
//    // --- Proctoring Management ---
//    @GetMapping("/proctoring/live-sessions")
//    public ResponseEntity<?> getLiveExamSessions() {
//        try {
//            List<LiveExamSession> sessions = liveExamSessionRepository.findActiveSessions();
//            List<Map<String, Object>> sessionData = new ArrayList<>();
//
//            for (LiveExamSession session : sessions) {
//                Map<String, Object> data = new HashMap<>();
//                data.put("studentEmail", session.getStudentEmail());
//
//                // Get student name
//                Optional<Student> studentOpt = studentRepository.findByEmail(session.getStudentEmail());
//                data.put("studentName", studentOpt.map(Student::getName).orElse("Unknown"));
//
//                data.put("examId", session.getExamId());
//
//                // Get exam name
//                Optional<Exam> examOpt = examRepository.findById(session.getExamId());
//                data.put("examName", examOpt.map(Exam::getExamName).orElse("Unknown Exam"));
//
//                data.put("startTime", session.getStartTime());
//                data.put("currentSection", session.getCurrentSection());
//                data.put("timeRemaining", session.getTimeRemaining());
//                data.put("status", session.getStatus().toString().toLowerCase());
//                data.put("warningCount", session.getWarningCount() != null ? session.getWarningCount() : 0);
//
//                // Get incidents for this session
//                data.put("incidents", new ArrayList<>());
//                data.put("incidentCount", 0);
//
//                sessionData.add(data);
//            }
//
//            return ResponseEntity.ok(Map.of("sessions", sessionData));
//        } catch (Exception e) {
//            logger.error("Error fetching live exam sessions", e);
//            return ResponseEntity.status(500).body(Map.of("message", "Error fetching live sessions"));
//        }
//    }
//
//    @GetMapping("/proctoring/incidents")
//    public ResponseEntity<?> getRecentIncidents() {
//        try {
//            return ResponseEntity.ok(Map.of("incidents", new ArrayList<>()));
//        } catch (Exception e) {
//            logger.error("Error fetching proctoring incidents", e);
//            return ResponseEntity.status(500).body(Map.of("message", "Error fetching incidents"));
//        }
//    }
//
//    @PostMapping("/proctoring/terminate")
//    public ResponseEntity<?> terminateSession(@RequestBody Map<String, Object> request) {
//        try {
//            String studentEmail = (String) request.get("studentEmail");
//            Integer examId = (Integer) request.get("examId");
//
//            if (studentEmail == null || examId == null) {
//                return ResponseEntity.badRequest().body(Map.of("message", "Missing required fields"));
//            }
//
//            Optional<LiveExamSession> sessionOpt = liveExamSessionRepository.findByStudentEmailAndExamId(studentEmail, examId);
//            if (sessionOpt.isPresent()) {
//                LiveExamSession session = sessionOpt.get();
//                session.setStatus(LiveExamSession.SessionStatus.TERMINATED);
//                session.setEndTime(java.time.LocalDateTime.now());
//                liveExamSessionRepository.save(session);
//
//                // Log termination incident (removed proctoring incident logging)
//
//                return ResponseEntity.ok(Map.of("message", "Session terminated successfully"));
//            } else {
//                return ResponseEntity.notFound().build();
//            }
//        } catch (Exception e) {
//            logger.error("Error terminating session", e);
//            return ResponseEntity.status(500).body(Map.of("message", "Error terminating session"));
//        }
//    }
//
//    @PostMapping("/proctoring/report-incident")
//    public ResponseEntity<?> reportIncident(@RequestBody Map<String, Object> request) {
//        try {
//            String studentEmail = (String) request.get("studentEmail");
//            Integer examId = (Integer) request.get("examId");
//
//            if (studentEmail == null || examId == null) {
//                return ResponseEntity.badRequest().body(Map.of("message", "Missing required fields"));
//            }
//
//            // Update session warning count if active session exists
//            Optional<LiveExamSession> sessionOpt = liveExamSessionRepository.findByStudentEmailAndExamId(studentEmail, examId);
//            if (sessionOpt.isPresent()) {
//                LiveExamSession session = sessionOpt.get();
//                session.setWarningCount(session.getWarningCount() + 1);
//
//                // If too many warnings, change status to WARNING or TERMINATED
//                if (session.getWarningCount() >= 5) {
//                    session.setStatus(LiveExamSession.SessionStatus.TERMINATED);
//                    session.setEndTime(java.time.LocalDateTime.now());
//                } else if (session.getWarningCount() >= 3) {
//                    session.setStatus(LiveExamSession.SessionStatus.WARNING);
//                }
//
//                LiveExamSession savedSession = liveExamSessionRepository.save(session);
//
//                // Return current warning count to frontend
//                return ResponseEntity.ok(Map.of(
//                    "message", "Incident reported successfully",
//                    "warningCount", savedSession.getWarningCount(),
//                    "status", savedSession.getStatus().toString().toLowerCase()
//                ));
//            }
//
//            return ResponseEntity.ok(Map.of("message", "Incident reported successfully"));
//        } catch (Exception e) {
//            logger.error("Error reporting incident", e);
//            return ResponseEntity.status(500).body(Map.of("message", "Error reporting incident"));
//        }
//    }
//
//    @GetMapping("/proctoring/recordings/{studentEmail}/{examId}")
//    public ResponseEntity<?> getRecordings(@PathVariable String studentEmail, @PathVariable Integer examId) {
//        try {
//            return ResponseEntity.ok(Map.of("recordings", new ArrayList<>()));
//        } catch (Exception e) {
//            logger.error("Error fetching recordings for student {} exam {}", studentEmail, examId, e);
//            return ResponseEntity.status(500).body(Map.of("message", "Error fetching recordings"));
//        }
//    }
//
//    // --- Email Management ---
//    @GetMapping("/student-counts")
//    public ResponseEntity<?> getStudentCounts() {
//        try {
//            // Use the same data source as getAllStudents() - studentRepository
//            long totalStudents = studentRepository.count();
//            long newStudentsCount = studentRepository.countByExamLinkSentIsNull();
//            long oldStudentsCount = studentRepository.countByExamLinkSentIsNotNull();
//
//            return ResponseEntity.ok(Map.of(
//                "totalStudents", totalStudents,
//                "newStudents", newStudentsCount,
//                "oldStudents", oldStudentsCount
//            ));
//        } catch (Exception e) {
//            logger.error("Error getting student counts", e);
//            return ResponseEntity.status(500).body(Map.of("message", "Error retrieving student counts"));
//        }
//    }
//
//    @PostMapping("/send-exam-link")
//    public ResponseEntity<?> sendExamLink(@RequestBody Map<String, Object> request) {
//        try {
//            Integer examId = (Integer) request.get("examId");
//            String customMessage = (String) request.get("customMessage");
//
//            if (examId == null) {
//                return ResponseEntity.badRequest().body(Map.of("message", "Exam ID is required"));
//            }
//
//            // Get exam details
//            Optional<Exam> examOpt = examRepository.findById(examId);
//            if (examOpt.isEmpty()) {
//                return ResponseEntity.badRequest().body(Map.of("message", "Exam not found"));
//            }
//            Exam exam = examOpt.get();
//
//            // Get all registered students
//            List<Student> students = studentRepository.findAll();
//            if (students.isEmpty()) {
//                return ResponseEntity.badRequest().body(Map.of("message", "No students found"));
//            }
//
//            // Prepare email content
//            String subject = "Exam Link: " + exam.getExamName();
//            String baseUrl = "http://localhost:4200"; // Update this to your actual frontend URL
//            String examLink = baseUrl + "/student-login?examId=" + examId;
//
//            StringBuilder body = new StringBuilder();
//            body.append("Dear Student,\n\n");
//            if (customMessage != null && !customMessage.trim().isEmpty()) {
//                body.append(customMessage).append("\n\n");
//            }
//            body.append("You have been invited to take the following exam:\n\n");
//            body.append("Exam Name: ").append(exam.getExamName()).append("\n");
//            body.append("Exam Link: ").append(examLink).append("\n\n");
//            body.append("Please click the link above to start your exam.\n\n");
//            body.append("Best regards,\n");
//            body.append("Exam Portal Administration");
//
//            // Send emails to all students
//            String[] emailAddresses = students.stream()
//                .map(Student::getEmail)
//                .toArray(String[]::new);
//
//            // Email service removed - bulk email sending disabled
//            // Update exam_link_sent timestamp for all students
//            List<Student> allStudents = studentRepository.findAll();
//            java.time.LocalDateTime now = java.time.LocalDateTime.now();
//            for (Student student : allStudents) {
//                student.setExamLinkSent(now);
//            }
//            studentRepository.saveAll(allStudents);
//
//            logger.info("Exam link tracking updated for {} students (email sending disabled)", emailAddresses.length);
//
//            return ResponseEntity.ok(Map.of(
//                "message", "Exam link emails are being sent to " + emailAddresses.length + " students",
//                "studentCount", emailAddresses.length
//            ));
//
//        } catch (Exception e) {
//            logger.error("Error sending exam link emails", e);
//            return ResponseEntity.status(500).body(Map.of("message", "Error sending emails"));
//        }
//    }
//
//    @PostMapping("/send-exam-link/new-students")
//    public ResponseEntity<?> sendExamLinkToNewStudents(@RequestBody Map<String, Object> request) {
//        try {
//            Integer examId = (Integer) request.get("examId");
//            String customMessage = (String) request.get("customMessage");
//
//            if (examId == null) {
//                return ResponseEntity.badRequest().body(Map.of("message", "Exam ID is required"));
//            }
//
//            // Get exam details
//            Optional<Exam> examOpt = examRepository.findById(examId);
//            if (examOpt.isEmpty()) {
//                return ResponseEntity.badRequest().body(Map.of("message", "Exam not found"));
//            }
//            Exam exam = examOpt.get();
//
//            // Get only new students (who haven't received exam link yet)
//            List<Student> newStudents = studentRepository.findByExamLinkSentIsNull();
//            if (newStudents.isEmpty()) {
//                return ResponseEntity.badRequest().body(Map.of("message", "No new students found"));
//            }
//
//            // Prepare email content
//            String subject = "Exam Link: " + exam.getExamName();
//            String baseUrl = "http://localhost:4200"; // Update this to your actual frontend URL
//            String examLink = baseUrl + "/student-login?examId=" + examId;
//
//            StringBuilder body = new StringBuilder();
//            body.append("Dear Student,\n\n");
//            if (customMessage != null && !customMessage.trim().isEmpty()) {
//                body.append(customMessage).append("\n\n");
//            }
//            body.append("You have been invited to take the following exam:\n\n");
//            body.append("Exam Name: ").append(exam.getExamName()).append("\n");
//            body.append("Exam Link: ").append(examLink).append("\n\n");
//            body.append("Please click the link above to start your exam.\n\n");
//            body.append("Best regards,\n");
//            body.append("Apical Soft Solutions");
//
//            // Send emails to new students only
//            String[] emailAddresses = newStudents.stream()
//                .map(Student::getEmail)
//                .toArray(String[]::new);
//
//            // Email service removed - bulk email sending disabled
//            // Update exam_link_sent timestamp for these students
//            java.time.LocalDateTime now = java.time.LocalDateTime.now();
//            for (Student student : newStudents) {
//                student.setExamLinkSent(now);
//            }
//            studentRepository.saveAll(newStudents);
//
//            logger.info("Exam link tracking updated for {} new students (email sending disabled)", emailAddresses.length);
//
//            return ResponseEntity.ok(Map.of(
//                "message", "Exam link emails are being sent to " + emailAddresses.length + " new students",
//                "studentCount", emailAddresses.length
//            ));
//
//        } catch (Exception e) {
//            logger.error("Error sending exam link emails to new students", e);
//            return ResponseEntity.status(500).body(Map.of("message", "Error sending emails"));
//        }
//    }
//}



























package com.exam.portal.controller;

import com.exam.portal.model.*;
import com.exam.portal.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:4000", "http://72.60.219.208"})
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired private StudentRepository studentRepository;
    @Autowired private QuestionRepository questionRepository;
    @Autowired private ExamRepository examRepository;
    @Autowired private ResultRepository resultRepository;
    @Autowired private SectionRepository sectionRepository;

    @Autowired private CompilerController compilerController; // For judging coding answers
    @Autowired private LiveExamSessionRepository liveExamSessionRepository;

    // --- Student Management ---
    @GetMapping("/students")
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    // --- Question Management ---
    @PostMapping("/questions")
    public ResponseEntity<?> addQuestion(@RequestBody Question questionDetails) {
        try {
            // Basic request validation
            if (questionDetails.getQuestionText() == null || questionDetails.getQuestionText().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "questionText is required"));
            }

            Section resolvedSection = null;
            if (questionDetails.getSection() != null && questionDetails.getSection().getId() != null) {
                resolvedSection = sectionRepository.findById(questionDetails.getSection().getId())
                        .orElse(null);
                if (resolvedSection == null) {
                    return ResponseEntity.badRequest().body(Map.of("message", "section.id is invalid"));
                }
            } else if (questionDetails.getSection() != null && questionDetails.getSection().getId() == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "section.id is required when section is provided"));
            }

            boolean isCoding = isCodingQuestion(questionDetails);

            // Conditional field validation to surface clear 400s instead of 500s
            if (isCoding) {
                // For coding questions, at least one boilerplate or SQL/test cases should be provided
                boolean hasAnyBoilerplate =
                        (questionDetails.getBoilerplateJava() != null && !questionDetails.getBoilerplateJava().isBlank()) ||
                        (questionDetails.getBoilerplatePython() != null && !questionDetails.getBoilerplatePython().isBlank()) ||
                        (questionDetails.getBoilerplateC() != null && !questionDetails.getBoilerplateC().isBlank()) ||
                        (questionDetails.getBoilerplateSql() != null && !questionDetails.getBoilerplateSql().isBlank());
                boolean hasSqlSetupOrTests =
                        (questionDetails.getSetupSql() != null && !questionDetails.getSetupSql().isBlank()) ||
                        (questionDetails.getTestCases() != null && !questionDetails.getTestCases().isBlank());
                if (!hasAnyBoilerplate && !hasSqlSetupOrTests) {
                    return ResponseEntity.badRequest().body(Map.of("message", "Provide coding boilerplate and/or test cases for coding question"));
                }
            } else {
                // MCQ minimal validation
                if (questionDetails.getOption1() == null || questionDetails.getOption2() == null ||
                    questionDetails.getOption3() == null || questionDetails.getOption4() == null ||
                    questionDetails.getCorrectAnswer() == null || questionDetails.getCorrectAnswer().trim().isEmpty()) {
                    return ResponseEntity.badRequest().body(Map.of("message", "MCQ requires option1..option4 and correctAnswer"));
                }
            }

            // Build entity for persistence
            Question newQuestion = new Question();
            newQuestion.setQuestionText(questionDetails.getQuestionText().trim());
            if (resolvedSection != null) newQuestion.setSection(resolvedSection);
            newQuestion.setIsCodingQuestion(isCoding);

            if (isCoding) {
                newQuestion.setBoilerplateJava(questionDetails.getBoilerplateJava());
                newQuestion.setBoilerplatePython(questionDetails.getBoilerplatePython());
                newQuestion.setBoilerplateC(questionDetails.getBoilerplateC());
                newQuestion.setBoilerplateSql(questionDetails.getBoilerplateSql());
                newQuestion.setTestCases(questionDetails.getTestCases());
                newQuestion.setSetupSql(questionDetails.getSetupSql());

                newQuestion.setOption1(null);
                newQuestion.setOption2(null);
                newQuestion.setOption3(null);
                newQuestion.setOption4(null);
                newQuestion.setCorrectAnswer(null);
            } else {
                newQuestion.setOption1(questionDetails.getOption1());
                newQuestion.setOption2(questionDetails.getOption2());
                newQuestion.setOption3(questionDetails.getOption3());
                newQuestion.setOption4(questionDetails.getOption4());
                newQuestion.setCorrectAnswer(questionDetails.getCorrectAnswer());

                newQuestion.setBoilerplateJava(null);
                newQuestion.setBoilerplatePython(null);
                newQuestion.setBoilerplateC(null);
                newQuestion.setBoilerplateSql(null);
                newQuestion.setTestCases(null);
                newQuestion.setSetupSql(null);
            }

            Question savedQuestion = questionRepository.save(newQuestion);
            return ResponseEntity.ok(savedQuestion);
        } catch (Exception e) {
            logger.error("Error adding question: {}", questionDetails != null ? questionDetails.getQuestionText() : "<null>", e);
            return ResponseEntity.status(500).body(Map.of("message", "Internal server error while adding question"));
        }
    }

    @GetMapping("/questions")
    public List<Question> getAllQuestions() {
        return questionRepository.findAll();
    }

    @PutMapping("/questions/{id}")
    public ResponseEntity<Question> updateQuestion(@PathVariable Long id, @RequestBody Question qDetails) {
        try {
            Question q = questionRepository.findById(id).orElseThrow(() -> new RuntimeException("Question not found with id: " + id));

            q.setQuestionText(qDetails.getQuestionText());

            // Set section relationship
            if (qDetails.getSection() != null && qDetails.getSection().getId() != null) {
                Section section = sectionRepository.findById(qDetails.getSection().getId())
                        .orElseThrow(() -> new RuntimeException("Section not found"));
                q.setSection(section);
            }

            boolean isCoding = isCodingQuestion(qDetails);
            q.setIsCodingQuestion(isCoding);

            // Determine which fields to set based on the isCodingQuestion flag.
            if (q.getIsCodingQuestion()) {
                // Set language-specific boilerplate codes
                q.setBoilerplateJava(qDetails.getBoilerplateJava());
                q.setBoilerplatePython(qDetails.getBoilerplatePython());
                q.setBoilerplateC(qDetails.getBoilerplateC());
                q.setBoilerplateSql(qDetails.getBoilerplateSql());
                q.setTestCases(qDetails.getTestCases());
                // SQL dataset setup (per-question isolated environment)
                q.setSetupSql(qDetails.getSetupSql());

                // Clear MCQ fields
                q.setOption1(null);
                q.setOption2(null);
                q.setOption3(null);
                q.setOption4(null);
                q.setCorrectAnswer(null);
            } else {
                q.setOption1(qDetails.getOption1());
                q.setOption2(qDetails.getOption2());
                q.setOption3(qDetails.getOption3());
                q.setOption4(qDetails.getOption4());
                q.setCorrectAnswer(qDetails.getCorrectAnswer());

                // Clear coding fields
                q.setBoilerplateJava(null);
                q.setBoilerplatePython(null);
                q.setBoilerplateC(null);
                q.setBoilerplateSql(null);
                q.setTestCases(null);
                q.setSetupSql(null);
            }

            final Question updatedQuestion = questionRepository.save(q);
            return ResponseEntity.ok(updatedQuestion);
        } catch (Exception e) {
            logger.error("Error updating question with id: {}", id, e);
            return ResponseEntity.status(500).body(null);
        }
    }

    private boolean isCodingQuestion(Question question) {
        // Prefer the explicit flag from the request
        if (Boolean.TRUE.equals(question.getIsCodingQuestion())) {
            return true;
        }
        // Fallback to checking the section name
        return question.getSection() != null && question.getSection().getName() != null &&
               ("Coding".equalsIgnoreCase(question.getSection().getName()) || "SQL".equalsIgnoreCase(question.getSection().getName()));
    }

    @DeleteMapping("/questions/{id}")
    public ResponseEntity<?> deleteQuestion(@PathVariable Long id) {
        questionRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // --- Exam Management ---
    @PostMapping("/exams")
    public Exam createExam(@RequestBody Exam exam) {
        return examRepository.save(exam);
    }

    @PostMapping("/exams/with-sections")
    public ResponseEntity<Exam> createExamWithSections(@RequestBody Map<String, Object> examData) {
        try {
            String examName = (String) examData.get("examName");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> sectionsData = (List<Map<String, Object>>) examData.get("sections");

            // Validate input
            if (examName == null || examName.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(null);
            }
            if (sectionsData == null || sectionsData.isEmpty()) {
                return ResponseEntity.badRequest().body(null);
            }

            // Create the exam
            Exam exam = new Exam();
            exam.setExamName(examName);
            exam.setActive(false); // New exams are inactive by default
            Exam savedExam = examRepository.save(exam);

            // Link selected sections to this exam
            List<Section> sections = new ArrayList<>();
            Set<String> sectionNames = new HashSet<>(); // Track section names to prevent duplicates

            for (Map<String, Object> sectionData : sectionsData) {
                Integer sectionId = (Integer) sectionData.get("id");
                Integer durationInMinutes = (Integer) sectionData.get("durationInMinutes");
                Integer orderIndex = (Integer) sectionData.get("orderIndex");

                // Validate section data
                if (sectionId == null) {
                    logger.warn("Skipping section with null ID for exam: {}", examName);
                    continue;
                }
                if (durationInMinutes == null || durationInMinutes <= 0) {
                    logger.warn("Skipping section ID '{}' with invalid duration for exam: {}", sectionId, examName);
                    continue;
                }
                if (orderIndex == null || orderIndex < 0) {
                    logger.warn("Skipping section ID '{}' with invalid order index for exam: {}", sectionId, examName);
                    continue;
                }

                // Find the standalone section by ID and link it to the exam
                Section sectionToLink = sectionRepository.findById(sectionId)
                    .orElse(null);

                if (sectionToLink == null) {
                    logger.warn("No standalone section found with ID '{}' for exam: {}. Skipping.", sectionId, examName);
                    continue;
                }

                // Update the section to link to the exam
                sectionToLink.setExam(savedExam);
                sectionToLink.setOrderIndex(orderIndex); // Update order index
                Section savedSection = sectionRepository.save(sectionToLink);
                sections.add(savedSection);
                sectionNames.add(savedSection.getName().toLowerCase());

                logger.info("Linked section '{}' (ID: {}) to exam '{}' with order index {}", savedSection.getName(), sectionId, examName, orderIndex);
            }

            if (sections.isEmpty()) {
                // If no valid sections were created, delete the exam and return error
                examRepository.delete(savedExam);
                return ResponseEntity.badRequest().body(null);
            }

            savedExam.setSections(sections);
            logger.info("Successfully created exam '{}' with {} sections", examName, sections.size());
            return ResponseEntity.ok(savedExam);

        } catch (Exception e) {
            logger.error("Error creating exam with sections: {}", examData.get("examName"), e);
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/exams")
    public List<Exam> getAllExams() {
        return examRepository.findAll();
    }

    @Transactional
    @PutMapping("/exams/{id}")
    public ResponseEntity<Exam> updateExam(@PathVariable Integer id, @RequestParam boolean isActive) {
        try {
            // Ensure the target exam exists before proceeding
            if (!examRepository.existsById(id)) {
                throw new RuntimeException("Exam not found with id: " + id);
            }

            if (isActive) {
                // If activating one, deactivate all others.
                List<Exam> allExams = examRepository.findAll();
                for (Exam exam : allExams) {
                    exam.setActive(exam.getId().equals(id));
                }
                examRepository.saveAll(allExams);
            } else {
                // If deactivating, just update the specific exam.
                examRepository.deactivateExamById(id);
            }

            // Re-fetch the exam to return its final, correct state.
            return ResponseEntity.ok(examRepository.findById(id).get());
        } catch (Exception e) {
            logger.error("Error updating exam with id: {}", id, e);
            return ResponseEntity.status(500).body(null);
        }
    }

    @PutMapping("/exams/{id}/details")
    public ResponseEntity<Exam> updateExamDetails(@PathVariable Integer id, @RequestBody Map<String, Object> examData) {
        try {
            Exam exam = examRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Exam not found with id: " + id));

            if (examData.containsKey("examName")) {
                exam.setExamName((String) examData.get("examName"));
            }

            // Save the exam name change first
            exam = examRepository.save(exam);

            // Update sections
            // First, clear existing sections
            List<Section> existingSections = sectionRepository.findByExamIdOrderByOrderIndexAsc(id);
            for (Section s : existingSections) {
                s.setExam(null);
                sectionRepository.save(s);
                s.setOrderIndex(null); // Also clear the order index
            }

            // Add new sections
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> sectionsData = (List<Map<String, Object>>) examData.get("sections");
            if (sectionsData != null) {
                for (Map<String, Object> sd : sectionsData) {
                    Integer sid = (Integer) sd.get("id");
                    Section s = sectionRepository.findById(sid).orElse(null);
                    if (s != null) {
                        s.setExam(exam);
                        s.setOrderIndex((Integer) sd.get("orderIndex"));
                        // The save operation will link it back
                        sectionRepository.save(s);
                    }
                }
            }

            // Re-fetch the exam with all its updated associations to return the freshest state
            return ResponseEntity.ok(examRepository.findById(id).get());
        } catch (Exception e) {
            logger.error("Error updating exam details for id: {}", id, e);
            return ResponseEntity.status(500).body(null);
        }
    }

    @DeleteMapping("/exams/{id}")
    public ResponseEntity<?> deleteExam(@PathVariable Integer id) {
        try {
            Exam exam = examRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Exam not found"));

            if (exam.getIsActive()) {
                return ResponseEntity.badRequest().body("Cannot delete active exam");
            }

            // Clear sections
            List<Section> sections = sectionRepository.findByExamIdOrderByOrderIndexAsc(id);
            for (Section s : sections) {
                s.setExam(null);
                sectionRepository.save(s);
            }

            examRepository.delete(exam);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error deleting exam with id: {}", id, e);
            return ResponseEntity.status(500).body(Map.of("message", "Error deleting exam"));
        }
    }
    
    // --- Result Management ---
    @GetMapping("/results")
    public List<Map<String, Object>> getAllResults() {
        List<Result> results = resultRepository.findAll();
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (Result result : results) {
            try {
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("id", result.getId());
                resultMap.put("studentEmail", result.getStudentEmail());
                // Get student name
                Optional<Student> studentOpt = studentRepository.findByEmail(result.getStudentEmail());
                resultMap.put("studentName", studentOpt.map(Student::getName).orElse("Unknown"));
                resultMap.put("examId", result.getExamId());
                resultMap.put("score", result.getScore());
                resultMap.put("totalQuestions", result.getTotalQuestions());
                resultMap.put("examDate", result.getExamDate());

                // Calculate section-wise scores
                List<Section> sections = sectionRepository.findByExamIdOrderByOrderIndexAsc(result.getExamId());
                List<Map<String, Object>> sectionResults = calculateSectionResults(result, sections);
                resultMap.put("sectionResults", sectionResults);

                // Compute total maximum marks for this exam (sum of section marks)
                int totalMarksForExam = sections.stream()
                        .map(s -> s.getMarks() != null ? s.getMarks() : 0)
                        .mapToInt(Integer::intValue)
                        .sum();
                resultMap.put("totalMarks", totalMarksForExam);

                // Calculate passed status based on section min pass marks
                boolean passed = calculatePassedStatus(result, sections);
                resultMap.put("passed", passed);

                resultList.add(resultMap);
            } catch (Exception e) {
                logger.error("Error processing result with id {}: {}", result.getId(), e.getMessage(), e);
                // Skip this result to avoid breaking the entire response
            }
        }
        return resultList;
    }

    private boolean calculatePassedStatus(Result result, List<Section> sections) {
        if (sections.isEmpty()) return true; // No sections, assume passed

        // Parse answers
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> mcqAnswers = new HashMap<>();
        if (result.getMcqAnswers() != null) {
            try {
                mcqAnswers = mapper.readValue(result.getMcqAnswers(), new TypeReference<Map<String, String>>() {});
            } catch (Exception e) {
                logger.error("Error parsing MCQ answers for result {}: {}", result.getId(), e);
                return false;
            }
        }

        Map<String, Map<String, String>> codingAnswers = new HashMap<>();
        if (result.getCodingAnswers() != null) {
            try {
                codingAnswers = mapper.readValue(result.getCodingAnswers(), new TypeReference<Map<String, Map<String, String>>>() {});
            } catch (Exception e) {
                logger.error("Error parsing coding answers for result {}: {}", result.getId(), e);
                return false;
            }
        }

        for (Section section : sections) {
            if (!section.getHasMinPassMarks() || section.getMinPassMarks() == null) continue; // Skip if no min pass

            List<Question> questions = questionRepository.findBySectionId(section.getId());
            int correctCount = 0;
            for (Question q : questions) {
                try {
                    if (q.getIsCodingQuestion()) {
                        Map<String, String> submittedCodeMap = codingAnswers.get(String.valueOf(q.getId()));
                        if (submittedCodeMap != null && !submittedCodeMap.isEmpty()) {
                            String lang = submittedCodeMap.keySet().stream().findFirst().orElse(null);
                            String code = (lang != null) ? submittedCodeMap.get(lang) : null;

                            if (code != null && !code.trim().isEmpty()) {
                                Map<String, String> submissionRequest = new HashMap<>();
                                submissionRequest.put("questionId", String.valueOf(q.getId()));
                                submissionRequest.put("language", lang);
                                submissionRequest.put("code", code);
                                try {
                                    ResponseEntity<Map<String, Object>> response = compilerController.submitCode(submissionRequest);
                                    if (response.getStatusCode().is2xxSuccessful() && Boolean.TRUE.equals(response.getBody().get("passed"))) {
                                        correctCount++;
                                    }
                                } catch (Exception e) {
                                    logger.error("Error re-judging coding for result {}: {}", result.getId(), e);
                                }
                            }
                        }
                    } else {
                        String answer = mcqAnswers.get(String.valueOf(q.getId()));
                        if (answer != null && answer.equals(q.getCorrectAnswer())) {
                            correctCount++;
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error processing question {} for result {}: {}", q.getId(), result.getId(), e);
                }
            }

            // Calculate section score
            int sectionMarks = section.getMarks() != null ? section.getMarks() : 0;
            double sectionScore;
            if (sectionMarks > 0 && !questions.isEmpty()) {
                sectionScore = (double) correctCount / questions.size() * sectionMarks;
            } else {
                sectionScore = correctCount;
            }

            if (Math.round(sectionScore) < section.getMinPassMarks()) {
                return false; // Failed this section
            }
        }
        return true; // Passed all sections with min pass
    }

    private List<Map<String, Object>> calculateSectionResults(Result result, List<Section> sections) {
        List<Map<String, Object>> sectionResults = new ArrayList<>();

        // Parse answers
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> mcqAnswers = new HashMap<>();
        if (result.getMcqAnswers() != null) {
            try {
                mcqAnswers = mapper.readValue(result.getMcqAnswers(), new TypeReference<Map<String, String>>() {});
            } catch (Exception e) {
                logger.error("Error parsing MCQ answers", e);
            }
        }

        Map<String, Map<String, String>> codingAnswers = new HashMap<>();
        if (result.getCodingAnswers() != null) {
            try {
                codingAnswers = mapper.readValue(result.getCodingAnswers(), new TypeReference<Map<String, Map<String, String>>>() {});
            } catch (Exception e) {
                logger.error("Error parsing coding answers", e);
            }
        }

        for (Section section : sections) {
            List<Question> questions = questionRepository.findBySectionId(section.getId());
            int correctCount = 0;
            int sectionTotal = questions.size();
            for (Question q : questions) {
                try {
                    if (q.getIsCodingQuestion()) {
                        // For coding questions, re-judge the submission to get an accurate score.
                        Map<String, String> submittedCodeMap = codingAnswers.get(String.valueOf(q.getId()));
                        if (submittedCodeMap != null && !submittedCodeMap.isEmpty()) {
                            // Find the language they submitted in (java, python, c, sql)
                            String lang = submittedCodeMap.keySet().stream().findFirst().orElse(null);
                            String code = (lang != null) ? submittedCodeMap.get(lang) : null;

                            if (code != null && !code.trim().isEmpty()) {
                                // Prepare request for compiler controller
                                Map<String, String> submissionRequest = new HashMap<>();
                                submissionRequest.put("questionId", String.valueOf(q.getId()));
                                submissionRequest.put("language", lang);
                                submissionRequest.put("code", code);

                                // Judge the code
                                ResponseEntity<Map<String, Object>> response = compilerController.submitCode(submissionRequest);
                                if (response.getStatusCode().is2xxSuccessful() && Boolean.TRUE.equals(response.getBody().get("passed"))) {
                                    correctCount++;
                                }
                            }
                        }
                    } else {
                        String answer = mcqAnswers.get(String.valueOf(q.getId()));
                        if (answer != null && answer.equals(q.getCorrectAnswer())) {
                            correctCount++;
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error processing question {} for result {}: {}", q.getId(), result.getId(), e);
                }
            }

            // Calculate section score: if marks > 0, use proportional, else correct count
            int sectionMarks = section.getMarks() != null ? section.getMarks() : 0;
            double sectionScore;
            if (sectionMarks > 0 && sectionTotal > 0) {
                sectionScore = (double) correctCount / sectionTotal * sectionMarks;
            } else {
                sectionScore = correctCount;
            }

            Map<String, Object> sectionResult = new HashMap<>();
            sectionResult.put("sectionId", section.getId());
            sectionResult.put("sectionName", section.getName());
            sectionResult.put("score", Math.round(sectionScore)); // Rounded score
            sectionResult.put("total", sectionTotal);
            sectionResult.put("marks", section.getMarks());
            sectionResult.put("hasMinPassMarks", section.getHasMinPassMarks());
            sectionResult.put("minPassMarks", section.getMinPassMarks());
            sectionResults.add(sectionResult);
        }

        return sectionResults;
    }

    @GetMapping("/results/{id}/details")
    public ResponseEntity<?> getDetailedResult(@PathVariable Long id) {
        try {
            Result result = resultRepository.findById(id).orElse(null);
            if (result == null) {
                return ResponseEntity.notFound().build();
            }

            // Get the exam
            Exam exam = examRepository.findById(result.getExamId()).orElse(null);
            if (exam == null) {
                return ResponseEntity.badRequest().body("Exam not found");
            }

            // Get sections
            List<Section> sections = sectionRepository.findByExamIdOrderByOrderIndexAsc(result.getExamId());

            // Parse answers
            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> mcqAnswers = new HashMap<>();
            if (result.getMcqAnswers() != null) {
                try {
                    mcqAnswers = mapper.readValue(result.getMcqAnswers(), new TypeReference<Map<String, String>>() {});
                } catch (Exception e) {
                    logger.error("Error parsing MCQ answers", e);
                }
            }

            Map<String, Map<String, String>> codingAnswers = new HashMap<>();
            if (result.getCodingAnswers() != null) {
                try {
                    codingAnswers = mapper.readValue(result.getCodingAnswers(), new TypeReference<Map<String, Map<String, String>>>() {});
                } catch (Exception e) {
                    logger.error("Error parsing coding answers", e);
                }
            }

            // Calculate section-wise scores
            List<Map<String, Object>> sectionResults = new ArrayList<>();
            for (Section section : sections) {
                List<Question> questions = questionRepository.findBySectionId(section.getId());
                int correctCount = 0;
                int sectionTotal = questions.size();
                for (Question q : questions) {
                    try {
                        if (q.getIsCodingQuestion()) {
                            // For coding questions, re-judge the submission to get an accurate score.
                            Map<String, String> submittedCodeMap = codingAnswers.get(String.valueOf(q.getId()));
                            if (submittedCodeMap != null && !submittedCodeMap.isEmpty()) {
                                String lang = submittedCodeMap.keySet().stream().findFirst().orElse(null);
                                String code = (lang != null) ? submittedCodeMap.get(lang) : null;

                                if (code != null && !code.trim().isEmpty()) {
                                    Map<String, String> submissionRequest = new HashMap<>();
                                    submissionRequest.put("questionId", String.valueOf(q.getId()));
                                    submissionRequest.put("language", lang);
                                    submissionRequest.put("code", code);

                                    ResponseEntity<Map<String, Object>> response = compilerController.submitCode(submissionRequest);
                                    if (response.getStatusCode().is2xxSuccessful() && Boolean.TRUE.equals(response.getBody().get("passed"))) {
                                        correctCount++;
                                    }
                                }
                            }
                        } else {
                            // For MCQ questions, check the stored answer.
                            String answer = mcqAnswers.get(String.valueOf(q.getId()));
                            if (answer != null && answer.equals(q.getCorrectAnswer())) {
                                correctCount++;
                            }
                        }
                    } catch (Exception e) {
                        logger.error("Error processing question {} for result {}: {}", q.getId(), result.getId(), e);
                    }
                }

                // Calculate section score: if marks > 0, use proportional, else correct count
                int sectionMarks = section.getMarks() != null ? section.getMarks() : 0;
                double sectionScore;
                if (sectionMarks > 0 && sectionTotal > 0) {
                    sectionScore = (double) correctCount / sectionTotal * sectionMarks;
                } else {
                    sectionScore = correctCount;
                }

                Map<String, Object> sectionResult = new HashMap<>();
                sectionResult.put("sectionName", section.getName());
                sectionResult.put("score", Math.round(sectionScore)); // Rounded score
                sectionResult.put("total", sectionTotal);
                sectionResult.put("marks", section.getMarks());
                sectionResults.add(sectionResult);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("studentEmail", result.getStudentEmail());
            response.put("examId", result.getExamId());
            response.put("examName", exam.getExamName());
            response.put("totalScore", result.getScore());
            response.put("totalQuestions", result.getTotalQuestions());
            // Total maximum marks across all sections in this exam
            int totalMarks = sections.stream()
                    .map(s -> s.getMarks() != null ? s.getMarks() : 0)
                    .mapToInt(Integer::intValue)
                    .sum();
            response.put("totalMarks", totalMarks);
            response.put("examDate", result.getExamDate());
            response.put("sectionResults", sectionResults);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting detailed result for id {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500).body("Error retrieving detailed result");
        }
    }

    // --- Section Management ---
    
    // Standalone section management (not tied to specific exam)
    @PostMapping("/sections")
    public ResponseEntity<Section> createStandaloneSection(@RequestBody Section section) {
        try {
            if (section.getMarks() == null) {
                section.setMarks(0); // default
            }
            if (section.getHasMinPassMarks() == null) {
                section.setHasMinPassMarks(false);
            }
            if (section.getMinPassMarks() == null) {
                section.setMinPassMarks(0);
            }
            Section savedSection = sectionRepository.save(section);
            return ResponseEntity.ok(savedSection);
        } catch (Exception e) {
            logger.error("Error creating standalone section: {}", section.getName(), e);
            return ResponseEntity.status(500).body(null);
        }
    }
    
    @GetMapping("/sections")
    public List<Section> getAllSections() {
        return sectionRepository.findAll();
    }
    
    @PutMapping("/sections/{sectionId}")
    public ResponseEntity<Section> updateStandaloneSection(@PathVariable Integer sectionId, @RequestBody Section sectionDetails) {
        try {
            Section section = sectionRepository.findById(sectionId)
                    .orElseThrow(() -> new RuntimeException("Section not found with id: " + sectionId));

            section.setName(sectionDetails.getName());
            section.setDurationInMinutes(sectionDetails.getDurationInMinutes());
            section.setOrderIndex(sectionDetails.getOrderIndex());
            section.setMarks(sectionDetails.getMarks());
            section.setHasMinPassMarks(sectionDetails.getHasMinPassMarks());
            section.setMinPassMarks(sectionDetails.getMinPassMarks());

            final Section updatedSection = sectionRepository.save(section);
            return ResponseEntity.ok(updatedSection);
        } catch (Exception e) {
            logger.error("Error updating standalone section with id: {}", sectionId, e);
            return ResponseEntity.status(500).body(null);
        }
    }
    
    @DeleteMapping("/sections/{sectionId}")
    public ResponseEntity<?> deleteStandaloneSection(@PathVariable Integer sectionId) {
        sectionRepository.deleteById(sectionId);
        return ResponseEntity.ok().build();
    }

    // Exam-specific section management
    @PostMapping("/exams/{examId}/sections")
    public ResponseEntity<Section> createSection(@PathVariable Integer examId, @RequestBody Section section) {
        try {
            Exam exam = examRepository.findById(examId)
                    .orElseThrow(() -> new RuntimeException("Exam not found with id: " + examId));

            // Validate section data
            if (section.getName() == null || section.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(null);
            }
            if (section.getDurationInMinutes() == null || section.getDurationInMinutes() <= 0) {
                return ResponseEntity.badRequest().body(null);
            }
            if (section.getOrderIndex() == null || section.getOrderIndex() < 0) {
                return ResponseEntity.badRequest().body(null);
            }

            // Check if section name already exists for this exam
            if (sectionRepository.existsByNameAndExamId(section.getName(), examId)) {
                logger.warn("Section '{}' already exists for exam ID: {}. Cannot create duplicate.", section.getName(), examId);
                return ResponseEntity.badRequest().body(null);
            }

            section.setExam(exam);
            if (section.getMarks() == null) {
                section.setMarks(0); // default
            }
            if (section.getHasMinPassMarks() == null) {
                section.setHasMinPassMarks(false);
            }
            if (section.getMinPassMarks() == null) {
                section.setMinPassMarks(0);
            }
            Section savedSection = sectionRepository.save(section);
            logger.info("Created section '{}' for exam ID: {} with order index {}", section.getName(), examId, section.getOrderIndex());
            return ResponseEntity.ok(savedSection);
        } catch (Exception e) {
            logger.error("Error creating section for exam ID: {}", examId, e);
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/exams/{examId}/sections")
    public List<Section> getSectionsByExam(@PathVariable Integer examId) {
        return sectionRepository.findByExamIdOrderByOrderIndexAsc(examId);
    }

    @PutMapping("/exams/{examId}/sections/{sectionId}")
    public ResponseEntity<Section> updateSection(@PathVariable Integer examId, @PathVariable Integer sectionId, @RequestBody Section sectionDetails) {
        try {
            Section section = sectionRepository.findById(sectionId)
                    .orElseThrow(() -> new RuntimeException("Section not found with id: " + sectionId));

            // Verify the section belongs to the specified exam
            if (!section.getExam().getId().equals(examId)) {
                throw new RuntimeException("Section does not belong to the specified exam");
            }

            section.setName(sectionDetails.getName());
            section.setDurationInMinutes(sectionDetails.getDurationInMinutes());
            section.setOrderIndex(sectionDetails.getOrderIndex());
            section.setMarks(sectionDetails.getMarks());
            section.setHasMinPassMarks(sectionDetails.getHasMinPassMarks());
            section.setMinPassMarks(sectionDetails.getMinPassMarks());

            final Section updatedSection = sectionRepository.save(section);
            return ResponseEntity.ok(updatedSection);
        } catch (Exception e) {
            logger.error("Error updating section with id: {} for exam: {}", sectionId, examId, e);
            return ResponseEntity.status(500).body(null);
        }
    }

    @DeleteMapping("/exams/{examId}/sections/{sectionId}")
    public ResponseEntity<?> deleteSection(@PathVariable Integer examId, @PathVariable Integer sectionId) {
        try {
            Section section = sectionRepository.findById(sectionId)
                    .orElseThrow(() -> new RuntimeException("Section not found with id: " + sectionId));

            // Verify the section belongs to the specified exam
            if (!section.getExam().getId().equals(examId)) {
                throw new RuntimeException("Section does not belong to the specified exam");
            }

            sectionRepository.deleteById(sectionId);
            logger.info("Deleted section '{}' (ID: {}) for exam ID: {}", section.getName(), sectionId, examId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error deleting section with id: {} for exam: {}", sectionId, examId, e);
            return ResponseEntity.status(500).body(Map.of("message", "Error deleting section"));
        }
    }

    // --- Section Cleanup Methods ---
    @PostMapping("/exams/{examId}/sections/cleanup-duplicates")
    public ResponseEntity<?> cleanupDuplicateSections(@PathVariable Integer examId) {
        try {
            List<Section> allSections = sectionRepository.findByExamIdOrderByOrderIndexAsc(examId);
            List<Section> sectionsToDelete = new ArrayList<>();
            Set<String> seenNames = new HashSet<>();

            for (Section section : allSections) {
                String sectionName = section.getName().toLowerCase();
                if (seenNames.contains(sectionName)) {
                    sectionsToDelete.add(section);
                    logger.warn("Found duplicate section '{}' for exam ID: {}. Will be deleted.", section.getName(), examId);
                } else {
                    seenNames.add(sectionName);
                }
            }

            int deletedCount = 0;
            for (Section section : sectionsToDelete) {
                sectionRepository.deleteById(section.getId());
                deletedCount++;
            }

            if (deletedCount > 0) {
                logger.info("Cleaned up {} duplicate sections for exam ID: {}", deletedCount, examId);
                return ResponseEntity.ok(Map.of("message", "Cleaned up " + deletedCount + " duplicate sections"));
            } else {
                return ResponseEntity.ok(Map.of("message", "No duplicate sections found"));
            }
        } catch (Exception e) {
            logger.error("Error cleaning up duplicate sections for exam ID: {}", examId, e);
            return ResponseEntity.status(500).body(Map.of("message", "Error cleaning up duplicates"));
        }
    }

//    // --- Proctoring Management ---
//    @GetMapping("/proctoring/live-sessions")
//    public ResponseEntity<?> getLiveExamSessions() {
//        try {
//            List<LiveExamSession> sessions = liveExamSessionRepository.findActiveSessions();
//            List<Map<String, Object>> sessionData = new ArrayList<>();
//
//            for (LiveExamSession session : sessions) {
//                Map<String, Object> data = new HashMap<>();
//                data.put("studentEmail", session.getStudentEmail());
//
//                // Get student name
//                Optional<Student> studentOpt = studentRepository.findByEmail(session.getStudentEmail());
//                data.put("studentName", studentOpt.map(Student::getName).orElse("Unknown"));
//
//                data.put("examId", session.getExamId());
//
//                // Get exam name
//                Optional<Exam> examOpt = examRepository.findById(session.getExamId());
//                data.put("examName", examOpt.map(Exam::getExamName).orElse("Unknown Exam"));
//
//                data.put("startTime", session.getStartTime());
//                data.put("currentSection", session.getCurrentSection());
//                data.put("timeRemaining", session.getTimeRemaining());
//                data.put("status", session.getStatus().toString().toLowerCase());
//                data.put("warningCount", session.getWarningCount() != null ? session.getWarningCount() : 0);
//
//                // Get incidents for this session
//                data.put("incidents", new ArrayList<>());
//                data.put("incidentCount", 0);
//
//                sessionData.add(data);
//            }
//
//            return ResponseEntity.ok(Map.of("sessions", sessionData));
//        } catch (Exception e) {
//            logger.error("Error fetching live exam sessions", e);
//            return ResponseEntity.status(500).body(Map.of("message", "Error fetching live sessions"));
//        }
//    }
//
//    @GetMapping("/proctoring/incidents")
//    public ResponseEntity<?> getRecentIncidents() {
//        try {
//            return ResponseEntity.ok(Map.of("incidents", new ArrayList<>()));
//        } catch (Exception e) {
//            logger.error("Error fetching proctoring incidents", e);
//            return ResponseEntity.status(500).body(Map.of("message", "Error fetching incidents"));
//        }
//    }
//
//    @PostMapping("/proctoring/terminate")
//    public ResponseEntity<?> terminateSession(@RequestBody Map<String, Object> request) {
//        try {
//            String studentEmail = (String) request.get("studentEmail");
//            Integer examId = (Integer) request.get("examId");
//
//            if (studentEmail == null || examId == null) {
//                return ResponseEntity.badRequest().body(Map.of("message", "Missing required fields"));
//            }
//
//            Optional<LiveExamSession> sessionOpt = liveExamSessionRepository.findByStudentEmailAndExamId(studentEmail, examId);
//            if (sessionOpt.isPresent()) {
//                LiveExamSession session = sessionOpt.get();
//                session.setStatus(LiveExamSession.SessionStatus.TERMINATED);
//                session.setEndTime(java.time.LocalDateTime.now());
//                liveExamSessionRepository.save(session);
//
//                // Log termination incident (removed proctoring incident logging)
//
//                return ResponseEntity.ok(Map.of("message", "Session terminated successfully"));
//            } else {
//                return ResponseEntity.notFound().build();
//            }
//        } catch (Exception e) {
//            logger.error("Error terminating session", e);
//            return ResponseEntity.status(500).body(Map.of("message", "Error terminating session"));
//        }
//    }
//
//    @PostMapping("/proctoring/report-incident")
//    public ResponseEntity<?> reportIncident(@RequestBody Map<String, Object> request) {
//        try {
//            String studentEmail = (String) request.get("studentEmail");
//            Integer examId = (Integer) request.get("examId");
//
//            if (studentEmail == null || examId == null) {
//                return ResponseEntity.badRequest().body(Map.of("message", "Missing required fields"));
//            }
//
//            // Update session warning count if active session exists
//            Optional<LiveExamSession> sessionOpt = liveExamSessionRepository.findByStudentEmailAndExamId(studentEmail, examId);
//            if (sessionOpt.isPresent()) {
//                LiveExamSession session = sessionOpt.get();
//                session.setWarningCount(session.getWarningCount() + 1);
//
//                // If too many warnings, change status to WARNING or TERMINATED
//                if (session.getWarningCount() >= 5) {
//                    session.setStatus(LiveExamSession.SessionStatus.TERMINATED);
//                    session.setEndTime(java.time.LocalDateTime.now());
//                } else if (session.getWarningCount() >= 3) {
//                    session.setStatus(LiveExamSession.SessionStatus.WARNING);
//                }
//
//                LiveExamSession savedSession = liveExamSessionRepository.save(session);
//
//                // Return current warning count to frontend
//                return ResponseEntity.ok(Map.of(
//                    "message", "Incident reported successfully",
//                    "warningCount", savedSession.getWarningCount(),
//                    "status", savedSession.getStatus().toString().toLowerCase()
//                ));
//            }
//
//            return ResponseEntity.ok(Map.of("message", "Incident reported successfully"));
//        } catch (Exception e) {
//            logger.error("Error reporting incident", e);
//            return ResponseEntity.status(500).body(Map.of("message", "Error reporting incident"));
//        }
//    }
//
//    @GetMapping("/proctoring/recordings/{studentEmail}/{examId}")
//    public ResponseEntity<?> getRecordings(@PathVariable String studentEmail, @PathVariable Integer examId) {
//        try {
//            return ResponseEntity.ok(Map.of("recordings", new ArrayList<>()));
//        } catch (Exception e) {
//            logger.error("Error fetching recordings for student {} exam {}", studentEmail, examId, e);
//            return ResponseEntity.status(500).body(Map.of("message", "Error fetching recordings"));
//        }
//    }
//
//    // --- Email Management ---
//    @GetMapping("/student-counts")
//    public ResponseEntity<?> getStudentCounts() {
//        try {
//            // Use the same data source as getAllStudents() - studentRepository
//            long totalStudents = studentRepository.count();
//            long newStudentsCount = studentRepository.countByExamLinkSentIsNull();
//            long oldStudentsCount = studentRepository.countByExamLinkSentIsNotNull();
//
//            return ResponseEntity.ok(Map.of(
//                "totalStudents", totalStudents,
//                "newStudents", newStudentsCount,
//                "oldStudents", oldStudentsCount
//            ));
//        } catch (Exception e) {
//            logger.error("Error getting student counts", e);
//            return ResponseEntity.status(500).body(Map.of("message", "Error retrieving student counts"));
//        }
//    }
//
//    @PostMapping("/send-exam-link")
//    public ResponseEntity<?> sendExamLink(@RequestBody Map<String, Object> request) {
//        try {
//            Integer examId = (Integer) request.get("examId");
//            String customMessage = (String) request.get("customMessage");
//
//            if (examId == null) {
//                return ResponseEntity.badRequest().body(Map.of("message", "Exam ID is required"));
//            }
//
//            // Get exam details
//            Optional<Exam> examOpt = examRepository.findById(examId);
//            if (examOpt.isEmpty()) {
//                return ResponseEntity.badRequest().body(Map.of("message", "Exam not found"));
//            }
//            Exam exam = examOpt.get();
//
//            // Get all registered students
//            List<Student> students = studentRepository.findAll();
//            if (students.isEmpty()) {
//                return ResponseEntity.badRequest().body(Map.of("message", "No students found"));
//            }
//
//            // Prepare email content
//            String subject = "Exam Link: " + exam.getExamName();
//            String baseUrl = "http://localhost:4200"; // Update this to your actual frontend URL
//            String examLink = baseUrl + "/student-login?examId=" + examId;
//
//            StringBuilder body = new StringBuilder();
//            body.append("Dear Student,\n\n");
//            if (customMessage != null && !customMessage.trim().isEmpty()) {
//                body.append(customMessage).append("\n\n");
//            }
//            body.append("You have been invited to take the following exam:\n\n");
//            body.append("Exam Name: ").append(exam.getExamName()).append("\n");
//            body.append("Exam Link: ").append(examLink).append("\n\n");
//            body.append("Please click the link above to start your exam.\n\n");
//            body.append("Best regards,\n");
//            body.append("Exam Portal Administration");
//
//            // Send emails to all students
//            String[] emailAddresses = students.stream()
//                .map(Student::getEmail)
//                .toArray(String[]::new);
//
//            // Email service removed - bulk email sending disabled
//            // Update exam_link_sent timestamp for all students
//            List<Student> allStudents = studentRepository.findAll();
//            java.time.LocalDateTime now = java.time.LocalDateTime.now();
//            for (Student student : allStudents) {
//                student.setExamLinkSent(now);
//            }
//            studentRepository.saveAll(allStudents);
//
//            logger.info("Exam link tracking updated for {} students (email sending disabled)", emailAddresses.length);
//
//            return ResponseEntity.ok(Map.of(
//                "message", "Exam link emails are being sent to " + emailAddresses.length + " students",
//                "studentCount", emailAddresses.length
//            ));
//
//        } catch (Exception e) {
//            logger.error("Error sending exam link emails", e);
//            return ResponseEntity.status(500).body(Map.of("message", "Error sending emails"));
//        }
//    }
//
//    @PostMapping("/send-exam-link/new-students")
//    public ResponseEntity<?> sendExamLinkToNewStudents(@RequestBody Map<String, Object> request) {
//        try {
//            Integer examId = (Integer) request.get("examId");
//            String customMessage = (String) request.get("customMessage");
//
//            if (examId == null) {
//                return ResponseEntity.badRequest().body(Map.of("message", "Exam ID is required"));
//            }
//
//            // Get exam details
//            Optional<Exam> examOpt = examRepository.findById(examId);
//            if (examOpt.isEmpty()) {
//                return ResponseEntity.badRequest().body(Map.of("message", "Exam not found"));
//            }
//            Exam exam = examOpt.get();
//
//            // Get only new students (who haven't received exam link yet)
//            List<Student> newStudents = studentRepository.findByExamLinkSentIsNull();
//            if (newStudents.isEmpty()) {
//                return ResponseEntity.badRequest().body(Map.of("message", "No new students found"));
//            }
//
//            // Prepare email content
//            String subject = "Exam Link: " + exam.getExamName();
//            String baseUrl = "http://localhost:4200"; // Update this to your actual frontend URL
//            String examLink = baseUrl + "/student-login?examId=" + examId;
//
//            StringBuilder body = new StringBuilder();
//            body.append("Dear Student,\n\n");
//            if (customMessage != null && !customMessage.trim().isEmpty()) {
//                body.append(customMessage).append("\n\n");
//            }
//            body.append("You have been invited to take the following exam:\n\n");
//            body.append("Exam Name: ").append(exam.getExamName()).append("\n");
//            body.append("Exam Link: ").append(examLink).append("\n\n");
//            body.append("Please click the link above to start your exam.\n\n");
//            body.append("Best regards,\n");
//            body.append("Apical Soft Solutions");
//
//            // Send emails to new students only
//            String[] emailAddresses = newStudents.stream()
//                .map(Student::getEmail)
//                .toArray(String[]::new);
//
//            // Email service removed - bulk email sending disabled
//            // Update exam_link_sent timestamp for these students
//            java.time.LocalDateTime now = java.time.LocalDateTime.now();
//            for (Student student : newStudents) {
//                student.setExamLinkSent(now);
//            }
//            studentRepository.saveAll(newStudents);
//
//            logger.info("Exam link tracking updated for {} new students (email sending disabled)", emailAddresses.length);
//
//            return ResponseEntity.ok(Map.of(
//                "message", "Exam link emails are being sent to " + emailAddresses.length + " new students",
//                "studentCount", emailAddresses.length
//            ));
//
//        } catch (Exception e) {
//            logger.error("Error sending exam link emails to new students", e);
//            return ResponseEntity.status(500).body(Map.of("message", "Error sending emails"));
//        }
//    }
}
