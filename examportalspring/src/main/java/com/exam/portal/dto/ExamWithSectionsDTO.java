package com.exam.portal.dto;

import java.util.List;

public class ExamWithSectionsDTO {
    public ExamInfo exam;
    public List<SectionInfo> sections;

    public static class ExamInfo {
        public Integer id;
        public String examName;
        public boolean isActive;
        public boolean includeTechnical, includeAptitude, includeReasoning, includeCoding;
    }
    public static class SectionInfo {
        public Long id;
        public String name;
        public int durationInMinutes;
        public int orderIndex;
        public List<QuestionInfo> questions;
    }
    public static class QuestionInfo {
        public Long id;
        public String questionText;
        public String category;
        public String option1, option2, option3, option4, correctAnswer;
        public boolean isCodingQuestion;
        public String boilerplateCode;
        public String testCases;
    }
}
