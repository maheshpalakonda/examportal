package com.exam.portal.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "live_exam_sessions")
public class LiveExamSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String studentEmail;

    @Column(nullable = false)
    private Integer examId;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column
    private LocalDateTime endTime;

    @Column(nullable = false)
    private String currentSection;

    @Column(nullable = false)
    private Integer timeRemaining; // in seconds

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status;

    @Column
    private Integer warningCount = 0;

//    @Transient // Not persisted, calculated from incidents
//    private List<ProctoringIncident> incidents;

    public enum SessionStatus {
        ACTIVE,
        WARNING,
        TERMINATED,
        COMPLETED
    }

    // Constructors
    public LiveExamSession() {}

    public LiveExamSession(String studentEmail, Integer examId, String currentSection, Integer timeRemaining) {
        this.studentEmail = studentEmail;
        this.examId = examId;
        this.startTime = LocalDateTime.now();
        this.currentSection = currentSection;
        this.timeRemaining = timeRemaining;
        this.status = SessionStatus.ACTIVE;
        this.warningCount = 0;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }

    public Integer getExamId() {
        return examId;
    }

    public void setExamId(Integer examId) {
        this.examId = examId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getCurrentSection() {
        return currentSection;
    }

    public void setCurrentSection(String currentSection) {
        this.currentSection = currentSection;
    }

    public Integer getTimeRemaining() {
        return timeRemaining;
    }

    public void setTimeRemaining(Integer timeRemaining) {
        this.timeRemaining = timeRemaining;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public void setStatus(SessionStatus status) {
        this.status = status;
    }

    public Integer getWarningCount() {
        return warningCount;
    }

    public void setWarningCount(Integer warningCount) {
        this.warningCount = warningCount;
    }

//    public List<ProctoringIncident> getIncidents() {
//        return incidents;
//    }
//
//    public void setIncidents(List<ProctoringIncident> incidents) {
//        this.incidents = incidents;
//    }
}
