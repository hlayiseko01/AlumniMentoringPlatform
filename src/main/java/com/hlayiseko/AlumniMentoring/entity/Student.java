package com.hlayiseko.AlumniMentoring.entity;
import jakarta.persistence.Column;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("STUDENT")
public class Student extends User {
    @Column(name = "enrollment_year")
    private Integer enrollmentYear;

    @Column(length = 200)
    private String major;

    public Student() {

        setRole(Role.STUDENT);
    }

    // Getters and setters
    public Integer getEnrollmentYear() { return enrollmentYear; }
    public void setEnrollmentYear(Integer enrollmentYear) { this.enrollmentYear = enrollmentYear; }
    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }
}
