package com.hlayiseko.AlumniMentoring.dto;

import com.hlayiseko.AlumniMentoring.entity.AlumniProfile;
import com.hlayiseko.AlumniMentoring.entity.MentorRequest;
import com.hlayiseko.AlumniMentoring.entity.Student;
import com.hlayiseko.AlumniMentoring.entity.RequestStatus;

import java.time.LocalDateTime;

/**
 * Safe DTO for MentorRequest to avoid leaking sensitive fields like passwords and emails.
 */
public class MentorRequestDTO {
    private Long id;
    private String message;
    private RequestStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Student (safe subset)
    private Long studentId;
    private String studentFullName;
    private String studentEmail;
    private String studentMajor;
    private Integer studentEnrollmentYear;

    // Alumni (safe subset)
    private Long alumniId;
    private String alumniFullName;
    private String alumniCompany;
    private String alumniPosition;
    private Boolean alumniAvailableForMentoring;

    public MentorRequestDTO() {}

    private static MentorRequestDTO base(MentorRequest mr) {
        MentorRequestDTO dto = new MentorRequestDTO();
        dto.id = mr.getId();
        dto.message = mr.getMessage();
        dto.status = mr.getStatus();
        dto.createdAt = mr.getCreatedAt();
        dto.updatedAt = mr.getUpdatedAt();

        AlumniProfile a = mr.getAlumni();
        if (a != null) {
            dto.alumniId = a.getId();
            dto.alumniFullName = a.getFullName();
            dto.alumniCompany = a.getCompany();
            dto.alumniPosition = a.getPosition();
            dto.alumniAvailableForMentoring = a.getAvailableForMentoring();
        }
        return dto;
    }

    // View for students (hide student details entirely)
    public static MentorRequestDTO forStudentView(MentorRequest mr) {
        MentorRequestDTO dto = base(mr);
        // Do NOT set any student* fields
        return dto;
    }

    // View for alumni/admin (include student details)
    public static MentorRequestDTO forAlumniView(MentorRequest mr) {
        MentorRequestDTO dto = base(mr);
        Student s = mr.getStudent();
        if (s != null) {
            dto.studentId = s.getId();
            dto.studentFullName = s.getFullName();
            dto.studentEmail = s.getEmail();
            dto.studentMajor = s.getMajor();
            dto.studentEnrollmentYear = s.getEnrollmentYear();
            System.out.println("DTO forAlumniView - Student ID: " + s.getId() + 
                ", Student Name: " + s.getFullName() + 
                ", Student Email: " + s.getEmail());
        } else {
            System.out.println("DTO forAlumniView - Student is null for request ID: " + mr.getId());
        }
        return dto;
    }

    // Getters only (immutable for API output)
    public Long getId() { return id; }
    public String getMessage() { return message; }
    public RequestStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public Long getStudentId() { return studentId; }
    public String getStudentFullName() { return studentFullName; }
    public String getStudentEmail() { return studentEmail; }
    public String getStudentMajor() { return studentMajor; }
    public Integer getStudentEnrollmentYear() { return studentEnrollmentYear; }
    public Long getAlumniId() { return alumniId; }
    public String getAlumniFullName() { return alumniFullName; }
    public String getAlumniCompany() { return alumniCompany; }
    public String getAlumniPosition() { return alumniPosition; }
    public Boolean getAlumniAvailableForMentoring() { return alumniAvailableForMentoring; }
}
