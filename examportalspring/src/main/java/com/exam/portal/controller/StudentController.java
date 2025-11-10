//package com.exam.portal.controller;
//import com.exam.portal.model.*;
//import com.exam.portal.repository.*;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import java.util.*;
//import java.util.stream.Collectors;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//@RestController
//@RequestMapping("/api/student")
//@CrossOrigin(origins = "http://localhost:4200")
//public class StudentController {
//
//    private static final Logger logger = LoggerFactory.getLogger(StudentController.class);
//
//    @Autowired
//    private StudentRepository studentRepository;
//
//   
//
//    @Autowired
//    private ExamRepository examRepository;
//
//    @Autowired
//    private QuestionRepository questionRepository;
//
//    @Autowired
//    private ResultRepository resultRepository;
//
//    @Autowired
//    private SectionRepository sectionRepository;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @Autowired
//    private LiveExamSessionRepository liveExamSessionRepository;
//
//    @PostMapping("/register")
//    public ResponseEntity<?> register(@RequestBody Map<String, Object> studentData) {
//        try {
//            String name = (String) studentData.get("name");
//            String hallTicketNumber = (String) studentData.get("hallTicketNumber");
//            String email = (String) studentData.get("email");
//            String mobileNumber = (String) studentData.get("mobileNumber");
//            String collegeName = (String) studentData.get("collegeName");
//            String branch = (String) studentData.get("branch");
//            Double cgpa = Double.valueOf(studentData.get("cgpa").toString());
//            String dateOfBirth = (String) studentData.get("dateOfBirth");
//            String gender = (String) studentData.get("gender");
//            String skills = (String) studentData.get("skills");
//
//            // Validate required fields
//            if (name == null || name.trim().isEmpty() ||
//                hallTicketNumber == null || hallTicketNumber.trim().isEmpty() ||
//                email == null || email.trim().isEmpty() ||
//                mobileNumber == null || mobileNumber.trim().isEmpty() ||
//                collegeName == null || collegeName.trim().isEmpty() ||
//                branch == null || branch.trim().isEmpty() ||
//                cgpa == null || dateOfBirth == null || dateOfBirth.trim().isEmpty() ||
//                gender == null || gender.trim().isEmpty() ||
//                skills == null || skills.trim().isEmpty()) {
//                return ResponseEntity.badRequest().body(Map.of("message", "All fields are required"));
//            }
//
//            // Check if student already exists
//            if (studentRepository.findByEmail(email).isPresent()) {
//                return ResponseEntity.badRequest().body(Map.of("message", "Student with this email already exists"));
//            }
//
//            if (studentRepository.findByHallTicketNumber(hallTicketNumber).isPresent()) {
//                return ResponseEntity.badRequest().body(Map.of("message", "Student with this hall ticket number already exists"));
//            }
//
//            // Create new student
//            Student student = new Student();
//            student.setName(name);
//            student.setHallTicketNumber(hallTicketNumber);
//            student.setEmail(email);
//            student.setMobileNumber(mobileNumber);
//            student.setCollegeName(collegeName);
//            student.setBranch(branch);
//            student.setCgpa(java.math.BigDecimal.valueOf(cgpa));
//            student.setDateOfBirth(java.time.LocalDate.parse(dateOfBirth));
//            student.setGender(gender);
//            student.setSkills(skills);
//
//            Student savedStudent = studentRepository.save(student);
//
//            logger.info("Student registered successfully: {}", email);
//            return ResponseEntity.ok(Map.of("message", "Registration successful", "studentId", savedStudent.getId()));
//
//        } catch (Exception e) {
//            logger.error("Error during student registration", e);
//            return ResponseEntity.status(500).body(Map.of("message", "Registration failed. Please try again."));
//        }
//    }
//
//    @PostMapping("/login")
//    public ResponseEntity<?> login(@RequestBody Map<String, String> creds) {
//        String email = creds.get("email");
//        String hallTicket = creds.get("hallTicketNumber");
//
//
//
//        // Fallback: check local students table (if any legacy data exists)
//        Optional<Student> localStudent = studentRepository.findByEmailAndHallTicketNumber(email, hallTicket);
//        if (localStudent.isPresent()) {
//            // Check if already submitted for active exam
//            Optional<Exam> activeExamOpt = examRepository.findByIsActiveTrue();
//            boolean alreadySubmitted = false;
//            if (activeExamOpt.isPresent()) {
//                List<Result> results = resultRepository.findByStudentEmailAndExamId(email, activeExamOpt.get().getId());
//                alreadySubmitted = !results.isEmpty();
//            }
//            return ResponseEntity.ok(Map.of("message", "Login successful", "alreadySubmitted", alreadySubmitted));
//        }
//
//        return ResponseEntity.status(401).body(Map.of("message", "Invalid Credentials"));
//    }
//
//    @GetMapping("/exam/active")
//    public ResponseEntity<?> getActiveExam() {
//        Optional<Exam> activeExamOpt = examRepository.findByIsActiveTrue();
//        if (activeExamOpt.isEmpty()) {
//            return ResponseEntity.status(404).body(Map.of("message", "No active exam found."));
//        }
//
//        Exam activeExam = activeExamOpt.get();
//        // Because of FetchType.EAGER, the sections and their questions are already loaded with the exam.
//
//        // For each section, select random questions if numQuestionsToSelect is set
//        for (Section section : activeExam.getSections()) {
//            List<Question> questions = section.getQuestions();
//            if (questions != null && !questions.isEmpty()) {
//                Integer numToSelect = section.getNumQuestionsToSelect();
//                if (numToSelect != null && numToSelect > 0 && numToSelect < questions.size()) {
//                    // Shuffle and select the first numToSelect
//                    Collections.shuffle(questions);
//                    section.setQuestions(questions.subList(0, numToSelect));
//                }
//                // If numToSelect is null or >= size, use all
//            }
//        }
//
//        // Flatten the list of selected questions from all sections
//        List<Question> allQuestions = activeExam.getSections().stream()
//                .flatMap(section -> section.getQuestions().stream())
//                .collect(Collectors.toList());
//
//        // Shuffle the questions for fairness (global shuffle after selection)
//        Collections.shuffle(allQuestions);
//
//        Map<String, Object> response = new HashMap<>();
//        response.put("exam", activeExam); // This now contains the sections
//        response.put("questions", allQuestions); // Return the shuffled, flattened list
//        return ResponseEntity.ok(response);
//    }
//    @PostMapping("/exam/start-session")
//    public ResponseEntity<?> startExamSession(@RequestBody Map<String, Object> sessionData) {
//        try {
//            String studentEmail = (String) sessionData.get("studentEmail");
//            Integer examId = (Integer) sessionData.get("examId");
//            String currentSection = (String) sessionData.get("currentSection");
//            Integer timeRemaining = (Integer) sessionData.get("timeRemaining");
//
//            if (studentEmail == null || examId == null || currentSection == null || timeRemaining == null) {
//                return ResponseEntity.badRequest().body(Map.of("message", "Missing required fields"));
//            }
//
//            // Check if session already exists
//            Optional<LiveExamSession> existingSession = liveExamSessionRepository.findByStudentEmailAndExamId(studentEmail, examId);
//            if (existingSession.isPresent()) {
//                // Update existing session
//                LiveExamSession session = existingSession.get();
//                session.setCurrentSection(currentSection);
//                session.setTimeRemaining(timeRemaining);
//                liveExamSessionRepository.save(session);
//                return ResponseEntity.ok(Map.of("message", "Session updated"));
//            } else {
//                // Create new session
//                LiveExamSession session = new LiveExamSession(studentEmail, examId, currentSection, timeRemaining);
//                liveExamSessionRepository.save(session);
//                return ResponseEntity.ok(Map.of("message", "Session started"));
//            }
//        } catch (Exception e) {
//            logger.error("Error starting/updating exam session", e);
//            return ResponseEntity.status(500).body(Map.of("message", "Error managing session"));
//        }
//    }
//
//    @PostMapping("/exam/submit")
//    public ResponseEntity<?> submitExam(@RequestBody Map<String, Object> submissionData) {
//        try {
//            String studentEmail = (String) submissionData.get("studentEmail");
//            Integer examId = (Integer) submissionData.get("examId");
//
//            if (studentEmail == null || examId == null) {
//                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Missing required fields"));
//            }
//
//            // End the live session
//            Optional<LiveExamSession> sessionOpt = liveExamSessionRepository.findByStudentEmailAndExamId(studentEmail, examId);
//            if (sessionOpt.isPresent()) {
//                LiveExamSession session = sessionOpt.get();
//                session.setStatus(LiveExamSession.SessionStatus.COMPLETED);
//                session.setEndTime(java.time.LocalDateTime.now());
//                liveExamSessionRepository.save(session);
//            }
//
//            // Check if result already exists
//            List<Result> existingResults = resultRepository.findByStudentEmailAndExamId(studentEmail, examId);
//            if (!existingResults.isEmpty()) {
//                if (existingResults.size() > 1) {
//                    // This indicates a data integrity issue (duplicates), which the new unique constraint should prevent in the future.
//                    logger.warn("Found {} duplicate submissions for student {} and exam {}. Denying new submission.", existingResults.size(), studentEmail, examId);
//                }
//                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Exam already submitted"));
//            }
//
//            // Get exam and questions
//            Optional<Exam> examOpt = examRepository.findById(examId);
//            if (examOpt.isEmpty()) {
//                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Exam not found"));
//            }
//
//            // Get all sections for this exam and their questions
//            List<Section> examSections = sectionRepository.findByExamIdOrderByOrderIndexAsc(examId);
//            List<Integer> sectionIds = examSections.stream()
//                    .map(Section::getId)
//                    .toList();
//            List<Question> examQuestions = questionRepository.findBySectionIdIn(sectionIds);
//
//            @SuppressWarnings("unchecked")
//            Map<String, Object> mcqAnswers = (Map<String, Object>) submissionData.get("mcqAnswers");
//            @SuppressWarnings("unchecked")
//            Map<String, Object> codingAnswers = (Map<String, Object>) submissionData.get("codingAnswers");
//            @SuppressWarnings("unchecked")
//            Map<String, Object> codingPassed = (Map<String, Object>) submissionData.get("codingPassed");
//
//            // Calculate scores (coding questions only get marks if all their test cases passed)
//            int totalScore = calculateTotalScore(mcqAnswers, codingAnswers, codingPassed, examSections, examQuestions);
//
//            // Calculate total questions based on submitted answers (for legacy display)
//            int totalQuestions = (mcqAnswers != null ? mcqAnswers.size() : 0) + (codingAnswers != null ? codingAnswers.size() : 0);
//
//            // Compute total maximum marks available for the exam
//            int totalMarks = examSections.stream()
//                    .map(s -> s.getMarks() != null ? s.getMarks() : 0)
//                    .mapToInt(Integer::intValue)
//                    .sum();
//
//            // Create result
//            Result result = new Result();
//            result.setStudentEmail(studentEmail);
//            result.setExamId(examId);
//            result.setScore(totalScore);
//            result.setTotalQuestions(totalQuestions);
//
//            // Store answer details as JSON strings
//            if (mcqAnswers != null) {
//                result.setMcqAnswers(objectMapper.writeValueAsString(mcqAnswers));
//            }
//            if (codingAnswers != null) {
//                result.setCodingAnswers(objectMapper.writeValueAsString(codingAnswers));
//            }
//
//            Result savedResult = resultRepository.save(result);
//
//            return ResponseEntity.ok(Map.of(
//                "success", true,
//                "message", "Exam submitted successfully",
//                "result", Map.of(
//                    "id", savedResult.getId(),
//                    "score", savedResult.getScore(),
//                    "totalQuestions", savedResult.getTotalQuestions(),
//                    "totalMarks", totalMarks,
//                    "submittedAt", savedResult.getExamDate()
//                )
//            ));
//
//        } catch (Exception e) {
//            logger.error("Error during exam submission for student {}:", submissionData.get("studentEmail"), e);
//            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "Error submitting exam. Please try again."));
//        }
//    }
//
//    // Helper method to calculate total score based on section marks
//    private int calculateTotalScore(Map<String, Object> mcqAnswers, Map<String, Object> codingAnswers, Map<String, Object> codingPassed, List<Section> sections, List<Question> allQuestions) {
//        int totalScore = 0;
//
//        for (Section section : sections) {
//            if (section == null || section.getId() == null) {
//                logger.warn("Skipping null or invalid section");
//                continue;
//            }
//            List<Question> sectionQuestions = allQuestions.stream()
//                    .filter(q -> q != null && q.getSection() != null && q.getSection().getId() != null && q.getSection().getId().equals(section.getId()))
//                    .collect(Collectors.toList());
//
//            int correctCount = 0;
//            for (Question q : sectionQuestions) {
//                if (q.getIsCodingQuestion()) {
//                    // Coding question: only award if ALL test cases passed for that question
//                    boolean passed = false;
//                    if (codingPassed != null) {
//                        Object val = codingPassed.get(String.valueOf(q.getId()));
//                        if (val instanceof Boolean) {
//                            passed = (Boolean) val;
//                        } else if (val instanceof String) {
//                            passed = Boolean.parseBoolean((String) val);
//                        }
//                    }
//                    if (passed) {
//                        correctCount++;
//                    }
//                } else {
//                    // MCQ question
//                    String submitted = mcqAnswers != null ? (String) mcqAnswers.get(String.valueOf(q.getId())) : null;
//                    String correct = q.getCorrectAnswer();
//                    if (submitted != null && correct != null && submitted.equals(correct)) {
//                        correctCount++;
//                    }
//                }
//            }
//
//            // Calculate section score: proportional to section marks; default 1 per question if marks unset
//            int sectionMarks = section.getMarks() != null ? section.getMarks() : 0;
//            double sectionScore;
//            if (sectionMarks > 0 && !sectionQuestions.isEmpty()) {
//                sectionScore = (double) correctCount / sectionQuestions.size() * sectionMarks;
//            } else {
//                sectionScore = correctCount;
//            }
//
//            totalScore += Math.round(sectionScore); // Round to nearest int for consistency
//            logger.info("Section '{}' (ID: {}): Correct {}/{}, Marks {}, Score {}", section.getName(), section.getId(), correctCount, sectionQuestions.size(), sectionMarks, Math.round(sectionScore));
//        }
//
//        logger.info("Calculated total score: {}", totalScore);
//        return totalScore;
//    }
//
//
//
//    // --- Section Management for Students ---
//    @GetMapping("/exam/{examId}/sections")
//    public ResponseEntity<?> getSectionsByExam(@PathVariable Integer examId) {
//        try {
//            List<Section> sections = sectionRepository.findByExamIdOrderByOrderIndexAsc(examId);
//            return ResponseEntity.ok(sections);
//        } catch (Exception e) {
//            logger.error("Error fetching sections for exam {}:", examId, e);
//            return ResponseEntity.status(500).body(Map.of("message", "Error fetching sections"));
//        }
//    }
//
//
//
//
//
//}
//
//





package com.exam.portal.controller;
import com.exam.portal.model.*;
import com.exam.portal.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@RestController
@RequestMapping("/api/student")
//@CrossOrigin(origins = "http://localhost:4200")
@CrossOrigin(origins = "http://72.60.219.208")
public class StudentController {

    private static final Logger logger = LoggerFactory.getLogger(StudentController.class);

    @Autowired
    private StudentRepository studentRepository;

   

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private ResultRepository resultRepository;

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JavaMailSender mailSender;

    // Removed live session persistence per requirement

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, Object> studentData) {
        try {
            String name = (String) studentData.get("name");
            String hallTicketNumber = (String) studentData.get("hallTicketNumber");
            String email = (String) studentData.get("email");
            String mobileNumber = (String) studentData.get("mobileNumber");
            String collegeName = (String) studentData.get("collegeName");
            String branch = (String) studentData.get("branch");
            Double cgpa = Double.valueOf(studentData.get("cgpa").toString());
            String dateOfBirth = (String) studentData.get("dateOfBirth");
            String gender = (String) studentData.get("gender");
            String skills = (String) studentData.get("skills");

            // Validate required fields
            if (name == null || name.trim().isEmpty() ||
                hallTicketNumber == null || hallTicketNumber.trim().isEmpty() ||
                email == null || email.trim().isEmpty() ||
                mobileNumber == null || mobileNumber.trim().isEmpty() ||
                collegeName == null || collegeName.trim().isEmpty() ||
                branch == null || branch.trim().isEmpty() ||
                cgpa == null || dateOfBirth == null || dateOfBirth.trim().isEmpty() ||
                gender == null || gender.trim().isEmpty() ||
                skills == null || skills.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "All fields are required"));
            }

            // Check if student already exists
            if (studentRepository.findByEmail(email).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Student with this email already exists"));
            }

            if (studentRepository.findByHallTicketNumber(hallTicketNumber).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Student with this hall ticket number already exists"));
            }

            // Create new student
            Student student = new Student();
            student.setName(name);
            student.setHallTicketNumber(hallTicketNumber);
            student.setEmail(email);
            student.setMobileNumber(mobileNumber);
            student.setCollegeName(collegeName);
            student.setBranch(branch);
            student.setCgpa(java.math.BigDecimal.valueOf(cgpa));
            student.setDateOfBirth(java.time.LocalDate.parse(dateOfBirth));
            student.setGender(gender);
            student.setSkills(skills);

            Student savedStudent = studentRepository.save(student);
	    
	    // Send registration email
            try {
                sendRegistrationEmail(savedStudent);
            } catch (Exception e) {
                logger.warn("Failed to send registration email to {}: {}", email, e.getMessage());
                // Don't fail registration if email fails
            }

            logger.info("Student registered successfully: {}", email);
            return ResponseEntity.ok(Map.of("message", "Registration successful", "studentId", savedStudent.getId()));

        } catch (Exception e) {
            logger.error("Error during student registration", e);
            return ResponseEntity.status(500).body(Map.of("message", "Registration failed. Please try again."));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> creds) {
        String email = creds.get("email");
        String hallTicket = creds.get("hallTicketNumber");

        if (email == null || email.trim().isEmpty() || hallTicket == null || hallTicket.trim().isEmpty()) {
            logger.warn("Login attempt with missing credentials. Email: {}, HallTicket: {}", email, hallTicket);
            return ResponseEntity.badRequest().body(Map.of("message", "Email and Hall Ticket Number are required."));
        }

        // Fallback: check local students table (if any legacy data exists)
        Optional<Student> localStudent = studentRepository.findByEmailAndHallTicketNumber(email, hallTicket);
        if (localStudent.isPresent()) {
            // Check if already submitted for active exam
            Optional<Exam> activeExamOpt = examRepository.findByIsActiveTrue();
            boolean alreadySubmitted = false;
            if (activeExamOpt.isPresent()) {
                List<Result> results = resultRepository.findByStudentEmailAndExamId(email, activeExamOpt.get().getId());
                alreadySubmitted = !results.isEmpty();
            }
            return ResponseEntity.ok(Map.of("message", "Login successful", "alreadySubmitted", alreadySubmitted));
        }

        return ResponseEntity.status(401).body(Map.of("message", "Invalid Credentials"));
    }

    @GetMapping("/exam/active")
    public ResponseEntity<?> getActiveExam() {
        Optional<Exam> activeExamOpt = examRepository.findByIsActiveTrue();
        if (activeExamOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("message", "No active exam found."));
        }

        Exam activeExam = activeExamOpt.get();
        // Because of FetchType.EAGER, the sections and their questions are already loaded with the exam.

        // For each section, select random questions if numQuestionsToSelect is set
        for (Section section : activeExam.getSections()) {
            List<Question> questions = section.getQuestions();
            if (questions != null && !questions.isEmpty()) {
                Integer numToSelect = section.getNumQuestionsToSelect();
                if (numToSelect != null && numToSelect > 0 && numToSelect < questions.size()) {
                    // Shuffle and select the first numToSelect
                    Collections.shuffle(questions);
                    section.setQuestions(questions.subList(0, numToSelect));
                }
                // If numToSelect is null or >= size, use all
            }
        }

        // Flatten the list of selected questions from all sections
        List<Question> allQuestions = activeExam.getSections().stream()
                .flatMap(section -> section.getQuestions().stream())
                .collect(Collectors.toList());

        // Shuffle the questions for fairness (global shuffle after selection)
        Collections.shuffle(allQuestions);

        Map<String, Object> response = new HashMap<>();
        response.put("exam", activeExam); // This now contains the sections
        response.put("questions", allQuestions); // Return the shuffled, flattened list
        return ResponseEntity.ok(response);
    }
    @PostMapping("/exam/start-session")
    public ResponseEntity<?> startExamSession(@RequestBody Map<String, Object> sessionData) {
        // Simplified: no persistence or proctoring, just acknowledge start
        return ResponseEntity.ok(Map.of("status", "started"));
    }

    @PostMapping("/exam/submit")
    public ResponseEntity<?> submitExam(@RequestBody Map<String, Object> submissionData) {
        try {
            String studentEmail = (String) submissionData.get("studentEmail");
            Integer examId = (Integer) submissionData.get("examId");

            if (studentEmail == null || examId == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Missing required fields"));
            }

            // Get all sections for this exam and their questions
            List<Section> examSections = sectionRepository.findByExamIdOrderByOrderIndexAsc(examId);
            List<Integer> sectionIds = examSections.stream().map(Section::getId).toList();
            List<Question> examQuestions = questionRepository.findBySectionIdIn(sectionIds);

            @SuppressWarnings("unchecked")
            Map<String, Object> mcqAnswers = (Map<String, Object>) submissionData.get("mcqAnswers");
            @SuppressWarnings("unchecked")
            Map<String, Object> codingAnswers = (Map<String, Object>) submissionData.get("codingAnswers");
            @SuppressWarnings("unchecked")
            Map<String, Object> codingPassed = (Map<String, Object>) submissionData.get("codingPassed");

            int totalScore = calculateTotalScore(mcqAnswers, codingAnswers, codingPassed, examSections, examQuestions);
            int totalQuestions = (mcqAnswers != null ? mcqAnswers.size() : 0) + (codingAnswers != null ? codingAnswers.size() : 0);

            Result result = new Result();
            result.setStudentEmail(studentEmail);
            result.setExamId(examId);
            result.setScore(totalScore);
            result.setTotalQuestions(totalQuestions);
            if (mcqAnswers != null) result.setMcqAnswers(objectMapper.writeValueAsString(mcqAnswers));
            if (codingAnswers != null) result.setCodingAnswers(objectMapper.writeValueAsString(codingAnswers));
            resultRepository.save(result);
		
	    // Send exam submission email
            try {
                sendExamSubmissionEmail(studentEmail, totalScore, totalQuestions);
            } catch (Exception e) {
                logger.warn("Failed to send exam submission email to {}: {}", studentEmail, e.getMessage());
                // Don't fail submission if email fails
            }


            return ResponseEntity.ok(Map.of("status", "submitted", "score", totalScore));
        } catch (Exception e) {
            logger.error("Error during exam submission for student {}:", submissionData.get("studentEmail"), e);
            return ResponseEntity.internalServerError().body(Map.of("message", "Error submitting exam. Please try again."));
        }
    }

    // Helper method to calculate total score based on section marks
    private int calculateTotalScore(Map<String, Object> mcqAnswers, Map<String, Object> codingAnswers, Map<String, Object> codingPassed, List<Section> sections, List<Question> allQuestions) {
        int totalScore = 0;

        for (Section section : sections) {
            if (section == null || section.getId() == null) {
                logger.warn("Skipping null or invalid section");
                continue;
            }
            List<Question> sectionQuestions = allQuestions.stream()
                    .filter(q -> q != null && q.getSection() != null && q.getSection().getId() != null && q.getSection().getId().equals(section.getId()))
                    .collect(Collectors.toList());

            int correctCount = 0;
            for (Question q : sectionQuestions) {
                if (q.getIsCodingQuestion()) {
                    // Coding question: only award if ALL test cases passed for that question
                    boolean passed = false;
                    if (codingPassed != null) {
                        Object val = codingPassed.get(String.valueOf(q.getId()));
                        if (val instanceof Boolean) {
                            passed = (Boolean) val;
                        } else if (val instanceof String) {
                            passed = Boolean.parseBoolean((String) val);
                        }
                    }
                    if (passed) {
                        correctCount++;
                    }
                } else {
                    // MCQ question
                    String submitted = mcqAnswers != null ? (String) mcqAnswers.get(String.valueOf(q.getId())) : null;
                    String correct = q.getCorrectAnswer();
                    if (submitted != null && correct != null && submitted.equals(correct)) {
                        correctCount++;
                    }
                }
            }

            // Calculate section score: proportional to section marks; default 1 per question if marks unset
            int sectionMarks = section.getMarks() != null ? section.getMarks() : 0;
            double sectionScore;
            if (sectionMarks > 0 && !sectionQuestions.isEmpty()) {
                sectionScore = (double) correctCount / sectionQuestions.size() * sectionMarks;
            } else {
                sectionScore = correctCount;
            }

            totalScore += Math.round(sectionScore); // Round to nearest int for consistency
            logger.info("Section '{}' (ID: {}): Correct {}/{}, Marks {}, Score {}", section.getName(), section.getId(), correctCount, sectionQuestions.size(), sectionMarks, Math.round(sectionScore));
        }

        logger.info("Calculated total score: {}", totalScore);
        return totalScore;
    }

// Helper methods for email sending
    private void sendRegistrationEmail(Student student) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(student.getEmail());
        message.setSubject("Apical Soft Solutions - Registration Successful");
        message.setText(String.format(
            "Dear %s,\n\n" +
            "Congratulations! You have successfully registered for the Online Exam.\n\n" +
            "Your registration details:\n" +
            "Name: %s\n" +
            "Email: %s\n" +
            "Hall Ticket Number: %s\n" +
            "College: %s\n" +
            "Branch: %s\n\n" +
            "Please keep your hall ticket number safe for login purposes.\n\n" +
            "Best regards,\n" +
            "Apical Soft Solutions",
            student.getName(), student.getName(), student.getEmail(), student.getHallTicketNumber(),
            student.getCollegeName(), student.getBranch()
        ));
        mailSender.send(message);
    }

    private void sendExamSubmissionEmail(String studentEmail, int score, int totalQuestions) {
        Optional<Student> studentOpt = studentRepository.findByEmail(studentEmail);
        if (studentOpt.isEmpty()) {
            logger.warn("Student not found for email: {}", studentEmail);
            return;
        }
        Student student = studentOpt.get();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(studentEmail);
        message.setSubject("Exam Submission Confirmation - Online Exam");
        message.setText(String.format(
            "Dear %s,\n\n" +
            "Your exam has been successfully submitted.\n\n" +
            "Student Email: %s\n" +
            "Thank you for participating in the exam.\n\n" +
            "Best regards,\n" +
            "Apical Soft Solutions\n\n" ,
            student.getName(), studentEmail
        ));
        mailSender.send(message);
    }

    // --- Section Management for Students ---
    @GetMapping("/exam/{examId}/sections")
    public ResponseEntity<?> getSectionsByExam(@PathVariable Integer examId) {
        try {
            List<Section> sections = sectionRepository.findByExamIdOrderByOrderIndexAsc(examId);
            return ResponseEntity.ok(sections);
        } catch (Exception e) {
            logger.error("Error fetching sections for exam {}:", examId, e);
            return ResponseEntity.status(500).body(Map.of("message", "Error fetching sections"));
        }
    }





}
