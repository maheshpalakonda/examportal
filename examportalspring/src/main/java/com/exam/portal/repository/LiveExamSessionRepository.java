//package com.exam.portal.repository;
//
//import com.exam.portal.model.LiveExamSession;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//import java.util.Optional;
//
//@Repository
//public interface LiveExamSessionRepository extends JpaRepository<LiveExamSession, Long> {
//
//    List<LiveExamSession> findByExamId(Integer examId);
//
//    Optional<LiveExamSession> findByStudentEmailAndExamId(String studentEmail, Integer examId);
//
//    List<LiveExamSession> findByStatus(LiveExamSession.SessionStatus status);
//
//    @Query("SELECT s FROM LiveExamSession s WHERE s.status IN ('ACTIVE', 'WARNING')")
//    List<LiveExamSession> findActiveSessions();
//
//    long countByExamIdAndStatus(Integer examId, LiveExamSession.SessionStatus status);
//}
package com.exam.portal.repository;

import com.exam.portal.model.LiveExamSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LiveExamSessionRepository extends JpaRepository<LiveExamSession, Long> {

    Optional<LiveExamSession> findByStudentEmailAndExamId(String studentEmail, Integer examId);

    List<LiveExamSession> findByExamId(Integer examId);

    @Query("SELECT s FROM LiveExamSession s WHERE s.status = 'ACTIVE' OR s.status = 'WARNING'")
    List<LiveExamSession> findActiveSessions();

    @Query("SELECT s FROM LiveExamSession s WHERE s.studentEmail = :studentEmail AND s.examId = :examId AND (s.status = 'ACTIVE' OR s.status = 'WARNING')")
    Optional<LiveExamSession> findActiveSessionByStudentAndExam(@Param("studentEmail") String studentEmail, @Param("examId") Integer examId);

    long countByExamIdAndStatus(Integer examId, LiveExamSession.SessionStatus status);
}
