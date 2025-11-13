package com.hlayiseko.AlumniMentoring.entity;

import jakarta.persistence.Column;

import jakarta.persistence.*;

import java.util.*;


@Entity
@DiscriminatorValue("ALUMNI")
public class AlumniProfile extends User {
    @Column(name = "graduation_year")
    private Integer graduationYear;

    @Column(length = 200)
    private String company;

    @Column(length = 200)
    private String position;

    @Column(length = 1000)
    private String bio;

    @ElementCollection
    @CollectionTable(name = "alumni_skills", joinColumns = @JoinColumn(name = "alumni_id"))
    @Column(name = "skill")
    private List<String> skills = new ArrayList<>();

    @Column(name = "available_for_mentoring")
    private Boolean availableForMentoring = true;

    @Column(length = 200)
    private String linkedin;

    public AlumniProfile() {
        setRole(Role.ALUMNI);
    }

    // Getters and setters
    public Integer getGraduationYear() { return graduationYear; }
    public void setGraduationYear(Integer graduationYear) { this.graduationYear = graduationYear; }
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public List<String> getSkills() { return skills; }
    public void setSkills(List<String> skills) { this.skills = skills; }
    public Boolean getAvailableForMentoring() { return availableForMentoring; }
    public void setAvailableForMentoring(Boolean availableForMentoring) { this.availableForMentoring = availableForMentoring; }
    public String getLinkedin() { return linkedin; }
    public void setLinkedin(String linkedin) { this.linkedin = linkedin; }
}
