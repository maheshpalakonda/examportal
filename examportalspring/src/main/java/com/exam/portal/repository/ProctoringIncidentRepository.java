//package com.exam.portal.repository;
//
//import com.exam.portal.model.ProctoringIncident;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Repository
//public interface ProctoringIncidentRepository extends JpaRepository<ProctoringIncident, Long> {
//
//    List<ProctoringIncident> findByStudentEmailAndExamId(String studentEmail, Integer examId);
//
//    List<ProctoringIncident> findByExamId(Integer examId);
//
//    @Query("SELECT i FROM ProctoringIncident i WHERE i.timestamp >= :since ORDER BY i.timestamp DESC")
//    List<ProctoringIncident> findRecentIncidents(@Param("since") LocalDateTime since);
//
//    @Query("SELECT i FROM ProctoringIncident i ORDER BY i.timestamp DESC")
//    List<ProctoringIncident> findAllOrderByTimestampDesc();
//
//    long countByStudentEmailAndExamId(String studentEmail, Integer examId);
//}
