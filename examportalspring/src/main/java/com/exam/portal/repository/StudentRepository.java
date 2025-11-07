
package com.exam.portal.repository;

import com.exam.portal.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByEmailAndHallTicketNumber(String email, String hallTicketNumber);
    Optional<Student> findByEmail(String email);
    Optional<Student> findByHallTicketNumber(String hallTicketNumber);
    boolean existsByEmail(String email);

    // Count students who haven't received exam link yet (new students)
    @Query("SELECT COUNT(s) FROM Student s WHERE s.examLinkSent IS NULL")
    long countByExamLinkSentIsNull();

    // Count students who have received exam link (old students)
    @Query("SELECT COUNT(s) FROM Student s WHERE s.examLinkSent IS NOT NULL")
    long countByExamLinkSentIsNotNull();

    // Find students who haven't received exam link yet
    List<Student> findByExamLinkSentIsNull();

    // Find students who have received exam link
    List<Student> findByExamLinkSentIsNotNull();
}
