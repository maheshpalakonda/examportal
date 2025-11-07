////package com.exam.portal.campus.repository;
////
////import com.exam.portal.campus.model.CampusStudent;
////import org.springframework.data.jpa.repository.JpaRepository;
////
////import java.util.Optional;
////
////public interface CampusStudentRepository extends JpaRepository<CampusStudent, Integer> {
////    Optional<CampusStudent> findByEmailAndHallTicketNumber(String email, String hallTicketNumber);
////    Optional<CampusStudent> findByEmail(String email);
////    Optional<CampusStudent> findByHallTicketNumber(String hallTicketNumber);
////    boolean existsByEmail(String email);
////}
//package com.exam.portal.campus.repository;
//
//import com.exam.portal.campus.model.CampusStudent;
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import java.util.List;
//import java.util.Optional;
//
//public interface CampusStudentRepository extends JpaRepository<CampusStudent, Integer> {
//    Optional<CampusStudent> findByEmailAndHallTicketNumber(String email, String hallTicketNumber);
//    Optional<CampusStudent> findByEmail(String email);
//    Optional<CampusStudent> findByHallTicketNumber(String hallTicketNumber);
//    boolean existsByEmail(String email);
//
//    // For tracking exam link sent status
//    long countByExamLinkSentIsNull();
//    long countByExamLinkSentIsNotNull();
//    List<CampusStudent> findByExamLinkSentIsNull();
//    List<CampusStudent> findByExamLinkSentIsNotNull();
//}
