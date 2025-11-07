package com.exam.portal.repository;

import com.exam.portal.model.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional; // <-- Make sure this is imported
import java.util.Optional;

public interface ExamRepository extends JpaRepository<Exam, Integer> {

    Optional<Exam> findByIsActiveTrue();

    /**
     * This custom query first sets ALL exams to be inactive.
     * The @Transactional and @Modifying annotations are critical.
     */
    @Transactional
    @Modifying
    @Query("UPDATE Exam e SET e.isActive = false")
    void deactivateAllExams();

    /**
     * This custom query then activates ONLY the one exam specified by its ID.
     */
    @Transactional
    @Modifying
    @Query("UPDATE Exam e SET e.isActive = true WHERE e.id = :examId")
    void activateExamById(Integer examId);

    /**
     * This custom query deactivates a single exam.
     */
    @Transactional
    @Modifying
    @Query("UPDATE Exam e SET e.isActive = false WHERE e.id = :examId")
    void deactivateExamById(Integer examId);
}