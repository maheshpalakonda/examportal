////package com.exam.portal.campus.model;
////
////import jakarta.persistence.*;
////import java.math.BigDecimal;
////import java.time.LocalDate;
////import java.time.LocalDateTime;
////
////@Entity
////@Table(name = "students")
////public class CampusStudent {
////    @Id
////    @GeneratedValue(strategy = GenerationType.IDENTITY)
////    private Integer id;
////
////    private String name;
////
////    @Column(name = "hall_ticket_number")
////    private String hallTicketNumber;
////
////    private String email;
////
////    @Column(name = "mobile_number")
////    private String mobileNumber;
////
////    @Column(name = "college_name")
////    private String collegeName;
////
////    private String branch;
////
////    // Match TEXT type in MySQL
////    @Column(columnDefinition = "TEXT")
////    private String skills;
////
////    @Column(precision = 4, scale = 2)
////    private BigDecimal cgpa;
////
////    @Column(name = "date_of_birth")
////    private LocalDate dateOfBirth;
////
////    private String gender;
////
////    // Let DB set default CURRENT_TIMESTAMP
////    @Column(name = "registered_at", insertable = false, updatable = false)
////    private LocalDateTime registeredAt;
////
////    // Getters and Setters
////    public Integer getId() { return id; }
////    public void setId(Integer id) { this.id = id; }
////
////    public String getName() { return name; }
////    public void setName(String name) { this.name = name; }
////
////    public String getHallTicketNumber() { return hallTicketNumber; }
////    public void setHallTicketNumber(String hallTicketNumber) { this.hallTicketNumber = hallTicketNumber; }
////
////    public String getEmail() { return email; }
////    public void setEmail(String email) { this.email = email; }
////
////    public String getMobileNumber() { return mobileNumber; }
////    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }
////
////    public String getCollegeName() { return collegeName; }
////    public void setCollegeName(String collegeName) { this.collegeName = collegeName; }
////
////    public String getBranch() { return branch; }
////    public void setBranch(String branch) { this.branch = branch; }
////
////    public String getSkills() { return skills; }
////    public void setSkills(String skills) { this.skills = skills; }
////
////    public BigDecimal getCgpa() { return cgpa; }
////    public void setCgpa(BigDecimal cgpa) { this.cgpa = cgpa; }
////
////    public LocalDate getDateOfBirth() { return dateOfBirth; }
////    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
////
////    public String getGender() { return gender; }
////    public void setGender(String gender) { this.gender = gender; }
////
////    public LocalDateTime getRegisteredAt() { return registeredAt; }
////    public void setRegisteredAt(LocalDateTime registeredAt) { this.registeredAt = registeredAt; }
////}
//
//
//
//package com.exam.portal.campus.model;
//
//import jakarta.persistence.*;
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "students")
//public class CampusStudent {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Integer id;
//
//    private String name;
//
//    @Column(name = "hall_ticket_number")
//    private String hallTicketNumber;
//
//    private String email;
//
//    @Column(name = "mobile_number")
//    private String mobileNumber;
//
//    @Column(name = "college_name")
//    private String collegeName;
//
//    private String branch;
//
//    // Match TEXT type in MySQL
//    @Column(columnDefinition = "TEXT")
//    private String skills;
//
//    @Column(precision = 4, scale = 2)
//    private BigDecimal cgpa;
//
//    @Column(name = "date_of_birth")
//    private LocalDate dateOfBirth;
//
//    private String gender;
//
//    // Let DB set default CURRENT_TIMESTAMP
//    @Column(name = "registered_at", insertable = false, updatable = false)
//    private LocalDateTime registeredAt;
//
//    @Column(name = "exam_link_sent")
//    private LocalDateTime examLinkSent;
//
//    // Getters and Setters
//    public Integer getId() { return id; }
//    public void setId(Integer id) { this.id = id; }
//
//    public String getName() { return name; }
//    public void setName(String name) { this.name = name; }
//
//    public String getHallTicketNumber() { return hallTicketNumber; }
//    public void setHallTicketNumber(String hallTicketNumber) { this.hallTicketNumber = hallTicketNumber; }
//
//    public String getEmail() { return email; }
//    public void setEmail(String email) { this.email = email; }
//
//    public String getMobileNumber() { return mobileNumber; }
//    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }
//
//    public String getCollegeName() { return collegeName; }
//    public void setCollegeName(String collegeName) { this.collegeName = collegeName; }
//
//    public String getBranch() { return branch; }
//    public void setBranch(String branch) { this.branch = branch; }
//
//    public String getSkills() { return skills; }
//    public void setSkills(String skills) { this.skills = skills; }
//
//    public BigDecimal getCgpa() { return cgpa; }
//    public void setCgpa(BigDecimal cgpa) { this.cgpa = cgpa; }
//
//    public LocalDate getDateOfBirth() { return dateOfBirth; }
//    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
//
//    public String getGender() { return gender; }
//    public void setGender(String gender) { this.gender = gender; }
//
//    public LocalDateTime getRegisteredAt() { return registeredAt; }
//    public void setRegisteredAt(LocalDateTime registeredAt) { this.registeredAt = registeredAt; }
//
//    public LocalDateTime getExamLinkSent() { return examLinkSent; }
//    public void setExamLinkSent(LocalDateTime examLinkSent) { this.examLinkSent = examLinkSent; }
//}
