
package com.exam.portal.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "results", uniqueConstraints = {
    @UniqueConstraint(name = "uk_student_exam", columnNames = {"student_email", "exam_id"})
})
public class Result {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Explicitly name columns to match the unique constraint definition
    @Column(nullable = false, name = "student_email")
    private String studentEmail;

    @Column(nullable = false, name = "exam_id")
    private Integer examId;

    @Column(nullable = false)
    private Integer score = 0;

    @Column(nullable = false)
    private Integer totalQuestions = 0;

    @Column(nullable = false, name = "exam_date")
    private LocalDateTime examDate = LocalDateTime.now();

    @Column(columnDefinition = "TEXT")
    private String mcqAnswers; // JSON string of MCQ answers

    @Column(columnDefinition = "TEXT")
    private String codingAnswers; // JSON string of coding answers

    // Constructors
    public Result() {
        this.examDate = LocalDateTime.now();
    }

    public Result(String studentEmail, Integer examId) {
        this.studentEmail = studentEmail;
        this.examId = examId;
        this.examDate = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getStudentEmail() { return studentEmail; }
    public void setStudentEmail(String studentEmail) { this.studentEmail = studentEmail; }

    public Integer getExamId() { return examId; }
    public void setExamId(Integer examId) { this.examId = examId; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public Integer getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(Integer totalQuestions) { this.totalQuestions = totalQuestions; }

    public LocalDateTime getExamDate() { return examDate; }
    public void setExamDate(LocalDateTime examDate) { this.examDate = examDate; }

    public String getMcqAnswers() { return mcqAnswers; }
    public void setMcqAnswers(String mcqAnswers) { this.mcqAnswers = mcqAnswers; }

    public String getCodingAnswers() { return codingAnswers; }
    public void setCodingAnswers(String codingAnswers) { this.codingAnswers = codingAnswers; }

    @Override
    public String toString() {
        return "Result{" +
                "id=" + id +
                ", studentEmail='" + studentEmail + '\'' +
                ", examId=" + examId +
                ", score=" + score +
                ", totalQuestions=" + totalQuestions +
                ", examDate=" + examDate +
                '}';
    }
}

