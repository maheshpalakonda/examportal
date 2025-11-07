package com.exam.portal.repository;
import com.exam.portal.model.Result;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResultRepository extends JpaRepository<Result, Long> {

    // Find results by student email
    List<Result> findByStudentEmail(String studentEmail);

    // Find result(s) by student email and exam ID.
    // Changed from Optional<Result> to List<Result> to prevent NonUniqueResultException if duplicates exist.
    List<Result> findByStudentEmailAndExamId(String studentEmail, Integer examId);

    // Find all results for a specific exam
    List<Result> findByExamId(Integer examId);

    // Find latest result for a student
    Optional<Result> findTopByStudentEmailOrderByExamDateDesc(String studentEmail);
}
