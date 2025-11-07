//package com.exam.portal.model;
//
//import jakarta.persistence.*;
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "proctoring_incidents")
//public class ProctoringIncident {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(nullable = false)
//    private String studentEmail;
//
//    @Column(nullable = false)
//    private Integer examId;
//
//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false)
//    private IncidentType type;
//
//    @Column(nullable = false)
//    private String details;
//
//    @Column(nullable = false)
//    private LocalDateTime timestamp;
//
//    @Column
//    private String recordingUrl; // For future recording storage
//
//    public enum IncidentType {
//        FACE_NOT_DETECTED,
//        VOICE_DETECTED,
//        TAB_SWITCH,
//        MULTIPLE_FACES,
//        NOISE_DETECTED
//    }
//
//    // Constructors
//    public ProctoringIncident() {}
//
//    public ProctoringIncident(String studentEmail, Integer examId, IncidentType type, String details) {
//        this.studentEmail = studentEmail;
//        this.examId = examId;
//        this.type = type;
//        this.details = details;
//        this.timestamp = LocalDateTime.now();
//    }
//
//    // Getters and Setters
//    public Long getId() {
//        return id;
//    }
//
//    public void setId(Long id) {
//        this.id = id;
//    }
//
//    public String getStudentEmail() {
//        return studentEmail;
//    }
//
//    public void setStudentEmail(String studentEmail) {
//        this.studentEmail = studentEmail;
//    }
//
//    public Integer getExamId() {
//        return examId;
//    }
//
//    public void setExamId(Integer examId) {
//        this.examId = examId;
//    }
//
//    public IncidentType getType() {
//        return type;
//    }
//
//    public void setType(IncidentType type) {
//        this.type = type;
//    }
//
//    public String getDetails() {
//        return details;
//    }
//
//    public void setDetails(String details) {
//        this.details = details;
//    }
//
//    public LocalDateTime getTimestamp() {
//        return timestamp;
//    }
//
//    public void setTimestamp(LocalDateTime timestamp) {
//        this.timestamp = timestamp;
//    }
//
//    public String getRecordingUrl() {
//        return recordingUrl;
//    }
//
//    public void setRecordingUrl(String recordingUrl) {
//        this.recordingUrl = recordingUrl;
//    }
//}
