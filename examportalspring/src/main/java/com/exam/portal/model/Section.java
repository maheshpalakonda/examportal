//package com.exam.portal.model;
//
//import jakarta.persistence.*;
//import com.fasterxml.jackson.annotation.JsonBackReference; // Important for preventing infinite loops
//import java.util.List;
//
//@Entity
//@Table(name = "sections")
//public class Section {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Integer id;
//    
//    @Column(nullable = false)
//    private String name;
//    
//    @Column(name = "duration_in_minutes", nullable = false)
//    private Integer durationInMinutes;
//    
//    @Column(name = "order_index", nullable = false)
//    private Integer orderIndex;
//
//    @Column(nullable = false)
//    private Integer marks;
//
//    @Column(name = "has_min_pass_marks", nullable = false)
//    private Boolean hasMinPassMarks = false;
//
//    @Column(name = "min_pass_marks")
//    private Integer minPassMarks;
//
//    @Column(name = "num_questions_to_select")
//    private Integer numQuestionsToSelect;
//
//    // This defines the many-to-one relationship. Many Sections belong to one Exam.
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "exam_id", nullable = true) // The FK column in the 'sections' table
//    @JsonBackReference // Manages the child side of the relationship for JSON conversion
//    private Exam exam;
//    
//    // One Section has Many Questions
//    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
//    private List<Question> questions;
//
//    // --- GETTERS AND SETTERS ---
//    public Integer getId() { return id; }
//    public void setId(Integer id) { this.id = id; }
//    public String getName() { return name; }
//    public void setName(String name) { this.name = name; }
//    public Integer getDurationInMinutes() { return durationInMinutes; }
//    public void setDurationInMinutes(Integer durationInMinutes) { this.durationInMinutes = durationInMinutes; }
//    public Integer getOrderIndex() { return orderIndex; }
//    public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }
//    public Integer getMarks() { return marks; }
//    public void setMarks(Integer marks) { this.marks = marks; }
//    public Exam getExam() { return exam; }
//    public void setExam(Exam exam) { this.exam = exam; }
//    public List<Question> getQuestions() { return questions; }
//    public void setQuestions(List<Question> questions) { this.questions = questions; }
//    public Boolean getHasMinPassMarks() { return hasMinPassMarks; }
//    public void setHasMinPassMarks(Boolean hasMinPassMarks) { this.hasMinPassMarks = hasMinPassMarks; }
//    public Integer getMinPassMarks() { return minPassMarks; }
//    public void setMinPassMarks(Integer minPassMarks) { this.minPassMarks = minPassMarks; }
//    public Integer getNumQuestionsToSelect() { return numQuestionsToSelect; }
//    public void setNumQuestionsToSelect(Integer numQuestionsToSelect) { this.numQuestionsToSelect = numQuestionsToSelect; }
//
//    public Integer getExamId() {
//        return exam != null ? exam.getId() : null;
//    }
//}

//
//package com.exam.portal.model;
//
//import jakarta.persistence.*;
//import com.fasterxml.jackson.annotation.JsonBackReference;
//import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
//import java.util.List;
//
//@Entity
//@Table(name = "sections")
//public class Section {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Integer id;
//    
//    @Column(nullable = false)
//    private String name;
//    
//    @Column(name = "duration_in_minutes", nullable = false)
//    private Integer durationInMinutes;
//    
//    @Column(name = "order_index", nullable = false)
//    private Integer orderIndex;
//
//    @Column(nullable = false)
//    private Integer marks;
//
//    @Column(name = "has_min_pass_marks", nullable = false)
//    private Boolean hasMinPassMarks = false;
//
//    @Column(name = "min_pass_marks")
//    private Integer minPassMarks;
//
//    @Column(name = "num_questions_to_select")
//    private Integer numQuestionsToSelect;
//
//    // Many Sections belong to One Exam
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "exam_id", nullable = true)
//    @JsonBackReference
//    private Exam exam;
//    
//    // One Section has Many Questions
//    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
//    @JsonIgnoreProperties("section") // this avoids infinite loop when returning list of Section
//    private List<Question> questions;
//
//    // --- GETTERS AND SETTERS ---
//    public Integer getId() { return id; }
//    public void setId(Integer id) { this.id = id; }
//
//    public String getName() { return name; }
//    public void setName(String name) { this.name = name; }
//
//    public Integer getDurationInMinutes() { return durationInMinutes; }
//    public void setDurationInMinutes(Integer durationInMinutes) { this.durationInMinutes = durationInMinutes; }
//
//    public Integer getOrderIndex() { return orderIndex; }
//    public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }
//
//    public Integer getMarks() { return marks; }
//    public void setMarks(Integer marks) { this.marks = marks; }
//
//    public Boolean getHasMinPassMarks() { return hasMinPassMarks; }
//    public void setHasMinPassMarks(Boolean hasMinPassMarks) { this.hasMinPassMarks = hasMinPassMarks; }
//
//    public Integer getMinPassMarks() { return minPassMarks; }
//    public void setMinPassMarks(Integer minPassMarks) { this.minPassMarks = minPassMarks; }
//
//    public Integer getNumQuestionsToSelect() { return numQuestionsToSelect; }
//    public void setNumQuestionsToSelect(Integer numQuestionsToSelect) { this.numQuestionsToSelect = numQuestionsToSelect; }
//
//    public Exam getExam() { return exam; }
//    public void setExam(Exam exam) { this.exam = exam; }
//
//    public List<Question> getQuestions() { return questions; }
//    public void setQuestions(List<Question> questions) { this.questions = questions; }
//
//    public Integer getExamId() {
//        return exam != null ? exam.getId() : null;
//    }
//}








//
//
//
//package com.exam.portal.model;
//
//import jakarta.persistence.*;
//import com.fasterxml.jackson.annotation.JsonBackReference; // Important for preventing infinite loops
//import java.util.List;
//
//@Entity
//@Table(name = "sections")
//public class Section {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Integer id;
//    
//    @Column(nullable = false)
//    private String name;
//    
//    @Column(name = "duration_in_minutes", nullable = false)
//    private Integer durationInMinutes;
//    
//    @Column(name = "order_index", nullable = false)
//    private Integer orderIndex;
//
//    @Column(nullable = false)
//    private Integer marks;
//
//    @Column(name = "has_min_pass_marks", nullable = false)
//    private Boolean hasMinPassMarks = false;
//
//    @Column(name = "min_pass_marks")
//    private Integer minPassMarks;
//
//    @Column(name = "num_questions_to_select")
//    private Integer numQuestionsToSelect;
//
//    // This defines the many-to-one relationship. Many Sections belong to one Exam.
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "exam_id", nullable = false) // The FK column in the 'sections' table
//    @JsonBackReference // Manages the child side of the relationship for JSON conversion
//    private Exam exam;
//    
//    // One Section has Many Questions
//    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
//    private List<Question> questions;
//
//    // --- GETTERS AND SETTERS ---
//    public Integer getId() { return id; }
//    public void setId(Integer id) { this.id = id; }
//    public String getName() { return name; }
//    public void setName(String name) { this.name = name; }
//    public Integer getDurationInMinutes() { return durationInMinutes; }
//    public void setDurationInMinutes(Integer durationInMinutes) { this.durationInMinutes = durationInMinutes; }
//    public Integer getOrderIndex() { return orderIndex; }
//    public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }
//    public Integer getMarks() { return marks; }
//    public void setMarks(Integer marks) { this.marks = marks; }
//    public Exam getExam() { return exam; }
//    public void setExam(Exam exam) { this.exam = exam; }
//    public List<Question> getQuestions() { return questions; }
//    public void setQuestions(List<Question> questions) { this.questions = questions; }
//    public Boolean getHasMinPassMarks() { return hasMinPassMarks; }
//    public void setHasMinPassMarks(Boolean hasMinPassMarks) { this.hasMinPassMarks = hasMinPassMarks; }
//    public Integer getMinPassMarks() { return minPassMarks; }
//    public void setMinPassMarks(Integer minPassMarks) { this.minPassMarks = minPassMarks; }
//    public Integer getNumQuestionsToSelect() { return numQuestionsToSelect; }
//    public void setNumQuestionsToSelect(Integer numQuestionsToSelect) { this.numQuestionsToSelect = numQuestionsToSelect; }
//}












//
//
//
//
//
//
//
//
//
//package com.exam.portal.model;
//
//import jakarta.persistence.*;
//
//import java.util.List;
//
//import com.fasterxml.jackson.annotation.JsonBackReference;
//import com.fasterxml.jackson.annotation.JsonIdentityInfo;
//import com.fasterxml.jackson.annotation.ObjectIdGenerators;
//
//@Entity
//@Table(name = "sections")
//public class Section {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Integer id;
//    
//    @Column(nullable = false)
//    private String name;
//    
//    @Column(name = "duration_in_minutes", nullable = false)
//    private Integer durationInMinutes;
//    
//    @Column(name = "order_index", nullable = false)
//    private Integer orderIndex;
//
//    @Column(nullable = false)
//    private Integer marks;
//
//    @Column(name = "has_min_pass_marks", nullable = false)
//    private Boolean hasMinPassMarks = false;
//
//    @Column(name = "min_pass_marks")
//    private Integer minPassMarks;
//
//    @Column(name = "num_questions_to_select")
//    private Integer numQuestionsToSelect;
//
//    // This defines the many-to-one relationship. Many Sections belong to one Exam.
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "exam_id", nullable = true) // The FK column in the 'sections' table
//    @JsonBackReference("exam-section")
//    private Exam exam;
//    
//    // One Section has Many Questions
//    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
//    private List<Question> questions;
//
//    // --- GETTERS AND SETTERS ---
//    public Integer getId() { return id; }
//    public void setId(Integer id) { this.id = id; }
//    public String getName() { return name; }
//    public void setName(String name) { this.name = name; }
//    public Integer getDurationInMinutes() { return durationInMinutes; }
//    public void setDurationInMinutes(Integer durationInMinutes) { this.durationInMinutes = durationInMinutes; }
//    public Integer getOrderIndex() { return orderIndex; }
//    public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }
//    public Integer getMarks() { return marks; }
//    public void setMarks(Integer marks) { this.marks = marks; }
//    public Exam getExam() { return exam; }
//    public void setExam(Exam exam) { this.exam = exam; }
//    public List<Question> getQuestions() { return questions; }
//    public void setQuestions(List<Question> questions) { this.questions = questions; }
//    public Boolean getHasMinPassMarks() { return hasMinPassMarks; }
//    public void setHasMinPassMarks(Boolean hasMinPassMarks) { this.hasMinPassMarks = hasMinPassMarks; }
//    public Integer getMinPassMarks() { return minPassMarks; }
//    public void setMinPassMarks(Integer minPassMarks) { this.minPassMarks = minPassMarks; }
//    public Integer getNumQuestionsToSelect() { return numQuestionsToSelect; }
//    public void setNumQuestionsToSelect(Integer numQuestionsToSelect) { this.numQuestionsToSelect = numQuestionsToSelect; }
//}
















package com.exam.portal.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import java.util.List;

@Entity
@Table(name = "sections")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Section {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(name = "duration_in_minutes", nullable = false)
    private Integer durationInMinutes;
    
    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Column(nullable = false)
    private Integer marks;

    @Column(name = "has_min_pass_marks", nullable = false)
    private Boolean hasMinPassMarks = false;

    @Column(name = "min_pass_marks")
    private Integer minPassMarks;

    @Column(name = "num_questions_to_select")
    private Integer numQuestionsToSelect;

    // This defines the many-to-one relationship. Many Sections belong to one Exam.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = true) // The FK column in the 'sections' table
    @JsonBackReference("exam-section")
    private Exam exam;
    
    // One Section has Many Questions
    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Question> questions;

    // --- GETTERS AND SETTERS ---
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getDurationInMinutes() { return durationInMinutes; }
    public void setDurationInMinutes(Integer durationInMinutes) { this.durationInMinutes = durationInMinutes; }
    public Integer getOrderIndex() { return orderIndex; }
    public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }
    public Integer getMarks() { return marks; }
    public void setMarks(Integer marks) { this.marks = marks; }
    public Exam getExam() { return exam; }
    public void setExam(Exam exam) { this.exam = exam; }
    public List<Question> getQuestions() { return questions; }
    public void setQuestions(List<Question> questions) { this.questions = questions; }
    public Boolean getHasMinPassMarks() { return hasMinPassMarks; }
    public void setHasMinPassMarks(Boolean hasMinPassMarks) { this.hasMinPassMarks = hasMinPassMarks; }
    public Integer getMinPassMarks() { return minPassMarks; }
    public void setMinPassMarks(Integer minPassMarks) { this.minPassMarks = minPassMarks; }
    public Integer getNumQuestionsToSelect() { return numQuestionsToSelect; }
    public void setNumQuestionsToSelect(Integer numQuestionsToSelect) { this.numQuestionsToSelect = numQuestionsToSelect; }
}
