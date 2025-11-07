//
//
//
//
//package com.exam.portal.model;
//
//import jakarta.persistence.*;
//import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
//import com.fasterxml.jackson.annotation.JsonManagedReference;
//
//@Entity
//@Table(name = "questions")
//public class Question {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(name = "question_text", columnDefinition = "TEXT", nullable = false)
//    private String questionText;
//
//    // Section
//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "section_id")
//    @JsonIgnoreProperties("questions")
//    private Section section;
//
//    // MCQ
//    private String option1;
//    private String option2;
//    private String option3;
//    private String option4;
//
//    @Column(name = "correct_answer")
//    private String correctAnswer;
//
//    // CODING BOILERPLATES
//    @Column(name = "boilerplate_java", columnDefinition = "TEXT")
//    private String boilerplateJava;
//
//    @Column(name = "boilerplate_python", columnDefinition = "TEXT")
//    private String boilerplatePython;
//
//    @Column(name = "boilerplate_c", columnDefinition = "TEXT")
//    private String boilerplateC;
//
//    @Column(name = "boilerplate_sql", columnDefinition = "TEXT")
//    private String boilerplateSql;
//
//    @Column(name = "test_cases", columnDefinition = "TEXT")
//    private String testCases;
//
//    @Column(name = "setup_sql", columnDefinition = "TEXT")
//    private String setupSql;
//
//    @Column(name = "is_coding_question", nullable = false)
//    private boolean isCodingQuestion = false;
//
//    // GETTERS / SETTERS
//
//    public Long getId() { return id; }
//    public void setId(Long id) { this.id = id; }
//
//    public String getQuestionText() { return questionText; }
//    public void setQuestionText(String questionText) { this.questionText = questionText; }
//
//    public Section getSection() { return section; }
//    public void setSection(Section section) { this.section = section; }
//
//    public String getOption1() { return option1; }
//    public void setOption1(String option1) { this.option1 = option1; }
//
//    public String getOption2() { return option2; }
//    public void setOption2(String option2) { this.option2 = option2; }
//
//    public String getOption3() { return option3; }
//    public void setOption3(String option3) { this.option3 = option3; }
//
//    public String getOption4() { return option4; }
//    public void setOption4(String option4) { this.option4 = option4; }
//
//    public String getCorrectAnswer() { return correctAnswer; }
//    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }
//
//    public String getBoilerplateJava() { return boilerplateJava; }
//    public void setBoilerplateJava(String boilerplateJava) { this.boilerplateJava = boilerplateJava; }
//
//    public String getBoilerplatePython() { return boilerplatePython; }
//    public void setBoilerplatePython(String boilerplatePython) { this.boilerplatePython = boilerplatePython; }
//
//    public String getBoilerplateC() { return boilerplateC; }
//    public void setBoilerplateC(String boilerplateC) { this.boilerplateC = boilerplateC; }
//
//    public String getBoilerplateSql() { return boilerplateSql; }
//    public void setBoilerplateSql(String boilerplateSql) { this.boilerplateSql = boilerplateSql; }
//
//    public String getTestCases() { return testCases; }
//    public void setTestCases(String testCases) { this.testCases = testCases; }
//
//    public String getSetupSql() { return setupSql; }
//    public void setSetupSql(String setupSql) { this.setupSql = setupSql; }
//
//    public boolean getIsCodingQuestion() { return isCodingQuestion; }
//    public void setIsCodingQuestion(boolean isCodingQuestion) { this.isCodingQuestion = isCodingQuestion; }
//
//    @Override
//    public String toString() {
//        return "Question{" +
//                "id=" + id +
//                ", questionText='" + questionText + '\'' +
//                ", section=" + (section != null ? section.getName() : "null") +
//                ", isCodingQuestion=" + isCodingQuestion +
//                '}';
//    }
//}

//
//package com.exam.portal.model;
//
//import jakarta.persistence.*;
//import com.fasterxml.jackson.annotation.JsonBackReference;
//
//@Entity
//@Table(name = "questions")
//public class Question {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(name = "question_text", columnDefinition = "TEXT", nullable = false)
//    private String questionText;
//
//    // Section
//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "section_id")
//    @JsonBackReference
//    private Section section;
//
//    // MCQ
//    private String option1;
//    private String option2;
//    private String option3;
//    private String option4;
//
//    @Column(name = "correct_answer")
//    private String correctAnswer;
//
//    // CODING BOILERPLATES
//    @Column(name = "boilerplate_java", columnDefinition = "TEXT")
//    private String boilerplateJava;
//
//    @Column(name = "boilerplate_python", columnDefinition = "TEXT")
//    private String boilerplatePython;
//
//    @Column(name = "boilerplate_c", columnDefinition = "TEXT")
//    private String boilerplateC;
//
//    @Column(name = "boilerplate_sql", columnDefinition = "TEXT")
//    private String boilerplateSql;
//
//    @Column(name = "test_cases", columnDefinition = "TEXT")
//    private String testCases;
//
//    @Column(name = "setup_sql", columnDefinition = "TEXT")
//    private String setupSql;
//
//    @Column(name = "is_coding_question", nullable = false)
//    private boolean isCodingQuestion = false;
//
//    // GETTERS / SETTERS
//
//    public Long getId() { return id; }
//    public void setId(Long id) { this.id = id; }
//
//    public String getQuestionText() { return questionText; }
//    public void setQuestionText(String questionText) { this.questionText = questionText; }
//
//    public Section getSection() { return section; }
//    public void setSection(Section section) { this.section = section; }
//
//    public String getOption1() { return option1; }
//    public void setOption1(String option1) { this.option1 = option1; }
//
//    public String getOption2() { return option2; }
//    public void setOption2(String option2) { this.option2 = option2; }
//
//    public String getOption3() { return option3; }
//    public void setOption3(String option3) { this.option3 = option3; }
//
//    public String getOption4() { return option4; }
//    public void setOption4(String option4) { this.option4 = option4; }
//
//    public String getCorrectAnswer() { return correctAnswer; }
//    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }
//
//    public String getBoilerplateJava() { return boilerplateJava; }
//    public void setBoilerplateJava(String boilerplateJava) { this.boilerplateJava = boilerplateJava; }
//
//    public String getBoilerplatePython() { return boilerplatePython; }
//    public void setBoilerplatePython(String boilerplatePython) { this.boilerplatePython = boilerplatePython; }
//
//    public String getBoilerplateC() { return boilerplateC; }
//    public void setBoilerplateC(String boilerplateC) { this.boilerplateC = boilerplateC; }
//
//    public String getBoilerplateSql() { return boilerplateSql; }
//    public void setBoilerplateSql(String boilerplateSql) { this.boilerplateSql = boilerplateSql; }
//
//    public String getTestCases() { return testCases; }
//    public void setTestCases(String testCases) { this.testCases = testCases; }
//
//    public String getSetupSql() { return setupSql; }
//    public void setSetupSql(String setupSql) { this.setupSql = setupSql; }
//
//    public boolean getIsCodingQuestion() { return isCodingQuestion; }
//    public void setIsCodingQuestion(boolean isCodingQuestion) { this.isCodingQuestion = isCodingQuestion; }
//
//    @Override
//    public String toString() {
//        return "Question{" +
//                "id=" + id +
//                ", questionText='" + questionText + '\'' +
//                ", section=" + (section != null ? section.getName() : "null") +
//                ", isCodingQuestion=" + isCodingQuestion +
//                '}';
//    }
//}
//







package com.exam.portal.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "question_text", columnDefinition = "TEXT", nullable = false)
    private String questionText;

    // Section
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "section_id")
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = false) // Serialize as full object
    private Section section;

    // MCQ
    private String option1;
    private String option2;
    private String option3;
    private String option4;

    @Column(name = "correct_answer")
    private String correctAnswer;

    // CODING BOILERPLATES
    @Column(name = "boilerplate_java", columnDefinition = "TEXT")
    private String boilerplateJava;

    @Column(name = "boilerplate_python", columnDefinition = "TEXT")
    private String boilerplatePython;

    @Column(name = "boilerplate_c", columnDefinition = "TEXT")
    private String boilerplateC;

    @Column(name = "boilerplate_sql", columnDefinition = "TEXT")
    private String boilerplateSql;

    @Column(name = "test_cases", columnDefinition = "TEXT")
    private String testCases;

    @Column(name = "setup_sql", columnDefinition = "TEXT")
    private String setupSql;

    @Column(name = "is_coding_question", nullable = false)
    private boolean isCodingQuestion = false;

    // GETTERS / SETTERS

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public Section getSection() { return section; }
    public void setSection(Section section) { this.section = section; }

    public String getOption1() { return option1; }
    public void setOption1(String option1) { this.option1 = option1; }

    public String getOption2() { return option2; }
    public void setOption2(String option2) { this.option2 = option2; }

    public String getOption3() { return option3; }
    public void setOption3(String option3) { this.option3 = option3; }

    public String getOption4() { return option4; }
    public void setOption4(String option4) { this.option4 = option4; }

    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }

    public String getBoilerplateJava() { return boilerplateJava; }
    public void setBoilerplateJava(String boilerplateJava) { this.boilerplateJava = boilerplateJava; }

    public String getBoilerplatePython() { return boilerplatePython; }
    public void setBoilerplatePython(String boilerplatePython) { this.boilerplatePython = boilerplatePython; }

    public String getBoilerplateC() { return boilerplateC; }
    public void setBoilerplateC(String boilerplateC) { this.boilerplateC = boilerplateC; }

    public String getBoilerplateSql() { return boilerplateSql; }
    public void setBoilerplateSql(String boilerplateSql) { this.boilerplateSql = boilerplateSql; }

    public String getTestCases() { return testCases; }
    public void setTestCases(String testCases) { this.testCases = testCases; }

    public String getSetupSql() { return setupSql; }
    public void setSetupSql(String setupSql) { this.setupSql = setupSql; }

    public boolean getIsCodingQuestion() { return isCodingQuestion; }
    public void setIsCodingQuestion(boolean isCodingQuestion) { this.isCodingQuestion = isCodingQuestion; }

    @Override
    public String toString() {
        return "Question{" +
                "id=" + id +
                ", questionText='" + questionText + '\'' +
                ", section=" + (section != null ? section.getName() : "null") +
                ", isCodingQuestion=" + isCodingQuestion +
                '}';
    }
}
