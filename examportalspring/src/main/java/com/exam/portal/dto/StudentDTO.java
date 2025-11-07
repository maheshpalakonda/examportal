package com.exam.portal.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

// NO LOMBOK ANNOTATIONS
public class StudentDTO {
    private String name;
    private String hallTicketNumber;
    private String email;
    private String mobileNumber;
    private String branch;
    private BigDecimal cgpa;
    private LocalDate dateOfBirth;
    private String gender;

    // Manual All-Arguments Constructor
    public StudentDTO(String name, String hallTicketNumber, String email, String mobileNumber, String branch, BigDecimal cgpa, LocalDate dateOfBirth, String gender) {
        this.name = name;
        this.hallTicketNumber = hallTicketNumber;
        this.email = email;
        this.mobileNumber = mobileNumber;
        this.branch = branch;
        this.cgpa = cgpa;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
    }

    // Manual Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getHallTicketNumber() { return hallTicketNumber; }
    public void setHallTicketNumber(String hallTicketNumber) { this.hallTicketNumber = hallTicketNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }

    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }

    public BigDecimal getCgpa() { return cgpa; }
    public void setCgpa(BigDecimal cgpa) { this.cgpa = cgpa; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
}