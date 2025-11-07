//package com.exam.portal.repository;
//
//import com.exam.portal.model.Section;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//import java.util.List;
//
//@Repository
//public interface SectionRepository extends JpaRepository<Section, Integer> {
//    
//    /**
//     * Find all sections for a specific exam, ordered by orderIndex
//     */
//    List<Section> findByExamIdOrderByOrderIndexAsc(Integer examId);
//    
//    /**
//     * Eagerly fetches sections and their associated questions for a given exam ID.
//     */
//    @Query("SELECT s FROM Section s LEFT JOIN FETCH s.questions WHERE s.exam.id = :examId ORDER BY s.orderIndex ASC")
//    List<Section> findByExamIdWithQuestions(@Param("examId") Integer examId);
//
//    /**
//     * Find sections by name (case-insensitive, exact match)
//     */
//    @Query("SELECT s FROM Section s WHERE LOWER(s.name) = LOWER(:name)")
//    List<Section> findByNameIgnoreCase(@Param("name") String name);
//
//    /**
//     * Find sections by name (case-insensitive, containing)
//     */
//    List<Section> findByNameContainingIgnoreCase(String name);
//    
//    /**
//     * Check if a section with the given name exists for a specific exam
//     */
//    boolean existsByNameAndExamId(String name, Integer examId);
//    
//    /**
//     * Find the maximum order index for sections in a specific exam
//     */
//    Integer findMaxOrderIndexByExamId(Integer examId);
//    
//    /**
//     * Delete all sections for a specific exam
//     */
//    void deleteByExamId(Integer examId);
//
//    /**
//     * Find all standalone sections (not tied to any exam)
//     */
//    List<Section> findByExamIsNull();
//}
//package com.exam.portal.repository;
//
//import com.exam.portal.model.Section;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//import java.util.List;
//
//@Repository
//public interface SectionRepository extends JpaRepository<Section, Integer> {
//    
//    /**
//     * Find all sections for a specific exam, ordered by orderIndex
//     */
//    List<Section> findByExamIdOrderByOrderIndexAsc(Integer examId);
//    
//    /**
//     * Eagerly fetches sections and their associated questions for a given exam ID.
//     */
//    @Query("SELECT s FROM Section s LEFT JOIN FETCH s.questions WHERE s.exam.id = :examId ORDER BY s.orderIndex ASC")
//    List<Section> findByExamIdWithQuestions(@Param("examId") Integer examId);
//
//    /**
//     * Find sections by name (case-insensitive, exact match)
//     */
//    @Query("SELECT s FROM Section s WHERE LOWER(s.name) = LOWER(:name)")
//    List<Section> findByNameIgnoreCase(@Param("name") String name);
//
//    /**
//     * Find sections by name (case-insensitive, containing)
//     */
//    List<Section> findByNameContainingIgnoreCase(String name);
//    
//    /**
//     * Check if a section with the given name exists for a specific exam
//     */
//    boolean existsByNameAndExamId(String name, Integer examId);
//    
//    /**
//     * Find the maximum order index for sections in a specific exam
//     */
//    Integer findMaxOrderIndexByExamId(Integer examId);
//    
//    /**
//     * Delete all sections for a specific exam
//     */
//    void deleteByExamId(Integer examId);
//
//    /**
//     * Find all standalone sections (not tied to any exam)
//     */
//    List<Section> findByExamIsNull();
//}




package com.exam.portal.repository;

import com.exam.portal.model.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SectionRepository extends JpaRepository<Section, Integer> {
    
    /**
     * Find all sections for a specific exam, ordered by orderIndex
     */
    @Query("SELECT s FROM Section s WHERE s.exam.id = :examId ORDER BY s.orderIndex ASC")
    List<Section> findByExamIdOrderByOrderIndexAsc(@Param("examId") Integer examId);
    
    /**
     * Eagerly fetches sections and their associated questions for a given exam ID.
     */
    @Query("SELECT s FROM Section s LEFT JOIN FETCH s.questions WHERE s.exam.id = :examId ORDER BY s.orderIndex ASC")
    List<Section> findByExamIdWithQuestions(@Param("examId") Integer examId);

    /**
     * Find sections by name (case-insensitive, exact match)
     */
    @Query("SELECT s FROM Section s WHERE LOWER(s.name) = LOWER(:name)")
    List<Section> findByNameIgnoreCase(@Param("name") String name);

    /**
     * Find sections by name (case-insensitive, containing)
     */
    List<Section> findByNameContainingIgnoreCase(String name);
    
    /**
     * Check if a section with the given name exists for a specific exam
     */
    @Query("SELECT COUNT(s) > 0 FROM Section s WHERE s.name = :name AND s.exam.id = :examId")
    boolean existsByNameAndExamId(@Param("name") String name, @Param("examId") Integer examId);
    
    /**
     * Find the maximum order index for sections in a specific exam
     */
    @Query("SELECT MAX(s.orderIndex) FROM Section s WHERE s.exam.id = :examId")
    Integer findMaxOrderIndexByExamId(@Param("examId") Integer examId);
    
    /**
     * Delete all sections for a specific exam
     */
    @Query("DELETE FROM Section s WHERE s.exam.id = :examId")
    void deleteByExamId(@Param("examId") Integer examId);

    /**
     * Find all standalone sections (not tied to any exam)
     */
    List<Section> findByExamIsNull();
}
