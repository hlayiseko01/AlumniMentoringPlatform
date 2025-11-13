package com.hlayiseko.AlumniMentoring.service;

import com.hlayiseko.AlumniMentoring.entity.*;
import com.hlayiseko.AlumniMentoring.entity.Message;
import jakarta.ejb.Stateless;
import jakarta.persistence.*;

import java.util.List;

@Stateless
public class MessageService {

    @PersistenceContext(unitName = "AlumniPU")
    private EntityManager em;

    public Message sendMessage(Message message, String senderEmail) {
        User sender = em.createQuery(
                        "SELECT u FROM User u WHERE u.email = :email", User.class)
                .setParameter("email", senderEmail)
                .getSingleResult();

        message.setSender(sender);
        message.setRead(false);
        em.persist(message);
        return message;
    }

    public List<Message> getUserMessages(String userEmail) {
        return em.createQuery(
                        "SELECT m FROM Message m WHERE m.sender.email = :email " +
                                "OR m.recipient.email = :email ORDER BY m.createdAt DESC", Message.class)
                .setParameter("email", userEmail)
                .getResultList();
    }

    public List<Message> getConversation(String userEmail, Long otherUserId) {
        return em.createQuery(
                        "SELECT m FROM Message m WHERE " +
                                "(m.sender.email = :email AND m.recipient.id = :otherId) OR " +
                                "(m.sender.id = :otherId AND m.recipient.email = :email) " +
                                "ORDER BY m.createdAt ASC", Message.class)
                .setParameter("email", userEmail)
                .setParameter("otherId", otherUserId)
                .getResultList();
    }

    public void markAsRead(Long messageId, String userEmail) {
        Message message = em.find(Message.class, messageId);
        if (message != null && message.getRecipient().getEmail().equals(userEmail)) {
            message.setRead(true);
            em.merge(message);
        }
    }

    public long getUnreadCount(String userEmail) {
        return em.createQuery(
                        "SELECT COUNT(m) FROM Message m WHERE m.recipient.email = :email " +
                                "AND m.read = false", Long.class)
                .setParameter("email", userEmail)
                .getSingleResult();
    }
}
