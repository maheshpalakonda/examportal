//package com.exam.portal.model;
//
//import jakarta.persistence.*;
//import com.fasterxml.jackson.annotation.JsonManagedReference; // Important for preventing infinite loops
//import java.util.List;
//
//@Entity
//@Table(name = "exams")
//public class Exam {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Integer id;
//
//    @Column(name = "exam_name", nullable = false)
//    private String examName;
//
//    @Column(name = "is_active", columnDefinition = "BOOLEAN DEFAULT FALSE")
//    private boolean isActive = false;
//    
//    @Transient // This field is not persisted in the database
//    private Integer durationMinutes;
//
//    // This defines the one-to-many relationship. One Exam has Many Sections.
//    // fetch = FetchType.EAGER means whenever you load an Exam, ALWAYS load its Sections too.
//    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
//    @OrderBy("orderIndex ASC") // Ensure sections are always in the correct order
//    @JsonManagedReference // Manages the parent side of the relationship for JSON conversion
//    private List<Section> sections;
//
//    // This method is automatically called by JPA after an Exam entity is loaded.
//    // It calculates the total duration from its sections.
//    @PostLoad
//    private void calculateDuration() {
//        if (sections == null || sections.isEmpty()) {
//            this.durationMinutes = 0;
//        } else {
//            this.durationMinutes = sections.stream().mapToInt(Section::getDurationInMinutes).sum();
//        }
//    }
//
//    // --- GETTERS AND SETTERS ---
//    public Integer getId() { return id; }
//    public void setId(Integer id) { this.id = id; }
//    public String getExamName() { return examName; }
//    public void setExamName(String examName) { this.examName = examName; }
//    public boolean getIsActive() { return isActive; }
//    public void setActive(boolean active) { this.isActive = active; }
//    public List<Section> getSections() { return sections; }
//    public void setSections(List<Section> sections) { this.sections = sections; }
//    public Integer getDurationMinutes() { return durationMinutes; }
//    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
//}
//package com.exam.portal.model;
//
//import jakarta.persistence.*;
//import com.fasterxml.jackson.annotation.JsonIdentityInfo;
//import com.fasterxml.jackson.annotation.ObjectIdGenerators;
//
//import java.util.List;
//
//@Entity
//@Table(name = "exams")
//public class Exam {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
//
//    private Integer id;
//
//    @Column(name = "exam_name", nullable = false)
//    private String examName;
//
//    @Column(name = "is_active", columnDefinition = "BOOLEAN DEFAULT FALSE")
//    private boolean isActive = false;
//    
//    @Transient // This field is not persisted in the database
//    private Integer durationMinutes;
//
//    // This defines the one-to-many relationship. One Exam has Many Sections.
//    // fetch = FetchType.EAGER means whenever you load an Exam, ALWAYS load its Sections too.
//    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
//    @OrderBy("orderIndex ASC") // Ensure sections are always in the correct order
//    private List<Section> sections;
//
//    // This method is automatically called by JPA after an Exam entity is loaded.
//    // It calculates the total duration from its sections.
//    @PostLoad
//    private void calculateDuration() {
//        if (sections == null || sections.isEmpty()) {
//            this.durationMinutes = 0;
//        } else {
//            this.durationMinutes = sections.stream().mapToInt(Section::getDurationInMinutes).sum();
//        }
//    }
//
//    // --- GETTERS AND SETTERS ---
//    public Integer getId() { return id; }
//    public void setId(Integer id) { this.id = id; }
//    public String getExamName() { return examName; }
//    public void setExamName(String examName) { this.examName = examName; }
//    public boolean getIsActive() { return isActive; }
//    public void setActive(boolean active) { this.isActive = active; }
//    public List<Section> getSections() { return sections; }
//    public void setSections(List<Section> sections) { this.sections = sections; }
//    public Integer getDurationMinutes() { return durationMinutes; }
//    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
//}



package com.exam.portal.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import java.util.List;

@Entity
@Table(name = "exams")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Exam {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "exam_name", nullable = false)
    private String examName;

    @Column(name = "is_active", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean isActive = false;
    
    @Transient // This field is not persisted in the database
    private Integer durationMinutes;

    // This defines the one-to-many relationship. One Exam has Many Sections.
    // fetch = FetchType.EAGER means whenever you load an Exam, ALWAYS load its Sections too.
    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference("exam-section")
    @OrderBy("orderIndex ASC") // Ensure sections are always in the correct order
    private List<Section> sections;

    // This method is automatically called by JPA after an Exam entity is loaded.
    // It calculates the total duration from its sections.
    @PostLoad
    private void calculateDuration() {
        if (sections == null || sections.isEmpty()) {
            this.durationMinutes = 0;
        } else {
            this.durationMinutes = sections.stream().mapToInt(Section::getDurationInMinutes).sum();
        }
    }

    // --- GETTERS AND SETTERS ---
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getExamName() { return examName; }
    public void setExamName(String examName) { this.examName = examName; }
    public boolean getIsActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }
    public List<Section> getSections() { return sections; }
    public void setSections(List<Section> sections) { this.sections = sections; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
}