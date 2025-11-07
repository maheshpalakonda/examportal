package com.exam.portal.repository;
import com.exam.portal.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    
    // Find questions by section ID
    List<Question> findBySectionId(Integer sectionId);
    
    // Find questions by multiple section IDs
    List<Question> findBySectionIdIn(List<Integer> sectionIds);
    
    // Find questions by section name
    @Query("SELECT q FROM Question q WHERE q.section.name = :sectionName")
    List<Question> findBySectionName(@Param("sectionName") String sectionName);
    
    // Find questions by multiple section names
    @Query("SELECT q FROM Question q WHERE q.section.name IN :sectionNames")
    List<Question> findBySectionNameIn(@Param("sectionNames") List<String> sectionNames);
    
    // Find all questions with their sections
    @Query("SELECT q FROM Question q LEFT JOIN FETCH q.section")
    List<Question> findAllWithSections();
}
