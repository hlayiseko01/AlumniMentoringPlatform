package com.hlayiseko.AlumniMentoring.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "chat_rooms")
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    @NotNull
    private Student student;

    @ManyToOne
    @JoinColumn(name = "alumni_id", nullable = false)
    @NotNull
    private AlumniProfile alumni;

   @Transient
    private List<Message> messages;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastMessageAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastMessageAt = LocalDateTime.now();
    }

    // Constructors
    public ChatRoom() {}

    public ChatRoom(Student student, AlumniProfile alumni) {
        this.student = student;
        this.alumni = alumni;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public AlumniProfile getAlumni() { return alumni; }
    public void setAlumni(AlumniProfile alumni) { this.alumni = alumni; }

    public List<Message> getMessages() { return messages; }
    public void setMessages(List<Message> messages) { this.messages = messages; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastMessageAt() { return lastMessageAt; }
    public void setLastMessageAt(LocalDateTime lastMessageAt) { this.lastMessageAt = lastMessageAt; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    // Helper methods
    public String getRoomName() {
        return "chat_" + student.getId() + "_" + alumni.getId();
    }

    public boolean isParticipant(User user) {
        if (user == null || student == null || alumni == null) {
            return false;
        }
        return user.getId().equals(student.getId()) || user.getId().equals(alumni.getId());
    }
}
