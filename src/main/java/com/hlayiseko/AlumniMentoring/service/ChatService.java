package com.hlayiseko.AlumniMentoring.service;

import com.hlayiseko.AlumniMentoring.entity.*;
import jakarta.ejb.Stateless;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Stateless
public class ChatService {

    @PersistenceContext(unitName = "AlumniPU")
    private EntityManager em;

    public ChatRoom createOrGetChatRoom(Student student, AlumniProfile alumni) {
        // Check if chat room already exists
        Optional<ChatRoom> existingRoom = em.createQuery(
                "SELECT cr FROM ChatRoom cr WHERE cr.student = :student AND cr.alumni = :alumni",
                ChatRoom.class)
                .setParameter("student", student)
                .setParameter("alumni", alumni)
                .getResultStream()
                .findFirst();

        if (existingRoom.isPresent()) {
            return existingRoom.get();
        }

        // Create new chat room
        ChatRoom chatRoom = new ChatRoom(student, alumni);
        em.persist(chatRoom);
        return chatRoom;
    }

    public ChatRoom getChatRoomById(Long chatRoomId) {
        return em.find(ChatRoom.class, chatRoomId);
    }

    public ChatRoom getChatRoomByParticipants(Long studentId, Long alumniId) {
        return em.createQuery(
                "SELECT cr FROM ChatRoom cr WHERE cr.student.id = :studentId AND cr.alumni.id = :alumniId",
                ChatRoom.class)
                .setParameter("studentId", studentId)
                .setParameter("alumniId", alumniId)
                .getSingleResult();
    }

    public List<ChatRoom> getUserChatRooms(User user) {
        System.out.println("=== ChatService.getUserChatRooms called ===");
        System.out.println("User: " + user.getEmail() + ", role: " + user.getRole() + ", ID: " + user.getId());
        
        try {
            // Use role-based queries instead of instanceof checks
            if (user.getRole() == Role.STUDENT) {
                System.out.println("Querying for student chat rooms...");
                List<ChatRoom> rooms = em.createQuery(
                        "SELECT cr FROM ChatRoom cr WHERE cr.student.id = :userId ORDER BY COALESCE(cr.lastMessageAt, cr.createdAt) DESC",
                        ChatRoom.class)
                        .setParameter("userId", user.getId())
                        .getResultList();
                System.out.println("Found " + rooms.size() + " chat rooms for student: " + user.getEmail());
                return rooms;
            } else if (user.getRole() == Role.ALUMNI || user.getRole() == Role.ADMIN) {
                System.out.println("Querying for alumni chat rooms...");
                List<ChatRoom> rooms = em.createQuery(
                        "SELECT cr FROM ChatRoom cr WHERE cr.alumni.id = :userId ORDER BY COALESCE(cr.lastMessageAt, cr.createdAt) DESC",
                        ChatRoom.class)
                        .setParameter("userId", user.getId())
                        .getResultList();
                System.out.println("Found " + rooms.size() + " chat rooms for alumni: " + user.getEmail());
                for (ChatRoom room : rooms) {
                    System.out.println("  Chat Room ID: " + room.getId() + ", Student: " + room.getStudent().getEmail() + ", Alumni: " + room.getAlumni().getEmail());
                }
                return rooms;
            }
            System.out.println("No chat rooms found for user role: " + user.getRole());
            return List.of();
        } catch (Exception e) {
            System.err.println("Exception in ChatService.getUserChatRooms: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public Message sendMessage(Long chatRoomId, User sender, String content) {
        ChatRoom chatRoom = getChatRoomById(chatRoomId);
        if (chatRoom == null) {
            throw new IllegalArgumentException("Chat room not found");
        }

        // Validate participant

        if (!chatRoom.isParticipant(sender)) {
            throw new IllegalArgumentException("User is not a participant in this chat room");
        }

        // Determine recipient
        User recipient = (sender.getId().equals(chatRoom.getStudent().getId())) ? 
            chatRoom.getAlumni() : chatRoom.getStudent();

        Message message = new Message();
        message.setSender(sender);
        message.setRecipient(recipient);
        message.setContent(content);
        em.persist(message);

        // Update chat room's last message time
        chatRoom.setLastMessageAt(LocalDateTime.now());
        em.merge(chatRoom);

        return message;
    }

    public List<Message> getChatMessages(Long chatRoomId, int limit, int offset) {
        ChatRoom chatRoom = getChatRoomById(chatRoomId);
        if (chatRoom == null) {
            return List.of();
        }

        return em.createQuery(
                "SELECT m FROM Message m WHERE (m.sender = :student AND m.recipient = :alumni) " +
                "OR (m.sender = :alumni AND m.recipient = :student) ORDER BY m.createdAt ASC",
                Message.class)
                .setParameter("student", chatRoom.getStudent())
                .setParameter("alumni", chatRoom.getAlumni())
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    public List<Message> getChatMessages(Long chatRoomId) {
        return getChatMessages(chatRoomId, 50, 0); // Default to last 50 messages
    }

    public void markMessagesAsRead(Long chatRoomId, User user) {
        ChatRoom chatRoom = getChatRoomById(chatRoomId);
        if (chatRoom == null) return;

        // Use ID comparison instead of object equality
        User otherUser = (user.getId().equals(chatRoom.getStudent().getId())) ? 
            chatRoom.getAlumni() : chatRoom.getStudent();

        em.createQuery(
                "UPDATE Message m SET m.read = true " +
                "WHERE m.sender = :sender AND m.recipient = :recipient AND m.read = false")
                .setParameter("sender", otherUser)
                .setParameter("recipient", user)
                .executeUpdate();
    }

    public long getUnreadMessageCount(Long chatRoomId, User user) {
        ChatRoom chatRoom = getChatRoomById(chatRoomId);
        if (chatRoom == null) return 0;

        // Use ID comparison instead of object equality
        User otherUser = (user.getId().equals(chatRoom.getStudent().getId())) ? 
            chatRoom.getAlumni() : chatRoom.getStudent();

        return em.createQuery(
                "SELECT COUNT(m) FROM Message m WHERE m.sender = :sender AND m.recipient = :recipient AND m.read = false",
                Long.class)
                .setParameter("sender", otherUser)
                .setParameter("recipient", user)
                .getSingleResult();
    }

    public long getTotalUnreadMessageCount(User user) {
        return em.createQuery(
                "SELECT COUNT(m) FROM Message m WHERE m.recipient = :user AND m.read = false",
                Long.class)
                .setParameter("user", user)
                .getSingleResult();
    }

    public ChatRoom createChatRoomFromMentorRequest(MentorRequest mentorRequest) {
        if (mentorRequest.getStatus() != RequestStatus.ACCEPTED) {
            throw new IllegalArgumentException("Mentor request must be accepted to create chat room");
        }

        return createOrGetChatRoom(mentorRequest.getStudent(), mentorRequest.getAlumni());
    }
}
