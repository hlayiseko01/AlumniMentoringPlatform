package com.hlayiseko.AlumniMentoring.service;

import com.hlayiseko.AlumniMentoring.entity.*;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.*;

import java.util.List;

@Stateless
public class MentorRequestService {

    @PersistenceContext(unitName = "AlumniPU")
    private EntityManager em;

    @Inject
    private EmailService emailService;

    @Inject
    private ChatService chatService;

    public MentorRequest createRequest(MentorRequest request, String studentEmail) {
        Student student = em.createQuery(
                        "SELECT s FROM Student s WHERE s.email= :email", Student.class)
                .setParameter("email", studentEmail)
                .getSingleResult();

        request.setStudent(student);
        request.setStatus(RequestStatus.PENDING);
        em.persist(request);

        // Send email notification to mentor
        emailService.sendMentorRequestNotification(
            request.getAlumni().getEmail(),
            student.getFullName(),
            request.getMessage()
        );

        return request;
    }

    public MentorRequest findById(Long id) {
        return em.find(MentorRequest.class, id);
    }

    public List<MentorRequest> getUserRequests(String userEmail, String role) {
        System.out.println("Getting user requests for email: " + userEmail + ", role: " + role);
        
        if ("STUDENT".equals(role)) {
            List<MentorRequest> requests = em.createQuery(
                            "SELECT r FROM MentorRequest r " +
                                    "JOIN FETCH r.student s " +
                                    "JOIN FETCH r.alumni a " +
                                    "WHERE r.student.email = :email " +
                                    "ORDER BY r.createdAt DESC", MentorRequest.class)
                    .setParameter("email", userEmail)
                    .getResultList();
            System.out.println("Found " + requests.size() + " requests for student: " + userEmail);
            return requests;
        } else if ("ALUMNI".equals(role) || "ADMIN".equals(role)) {
            List<MentorRequest> requests = em.createQuery(
                            "SELECT r FROM MentorRequest r " +
                                    "JOIN FETCH r.student s " +
                                    "JOIN FETCH r.alumni a " +
                                    "WHERE r.alumni.email = :email " +
                                    "ORDER BY r.createdAt DESC", MentorRequest.class)
                    .setParameter("email", userEmail)
                    .getResultList();
            System.out.println("Found " + requests.size() + " requests for alumni: " + userEmail);
            for (MentorRequest req : requests) {
                System.out.println("  Request ID: " + req.getId() + 
                    ", Student: " + req.getStudent().getEmail() + 
                    ", Student Name: " + req.getStudent().getFullName() + 
                    ", Alumni: " + req.getAlumni().getEmail() + 
                    ", Status: " + req.getStatus());
            }
            return requests;
        } else {
            List<MentorRequest> requests = em.createQuery(
                            "SELECT r FROM MentorRequest r " +
                                    "JOIN FETCH r.student s " +
                                    "JOIN FETCH r.alumni a " +
                                    "ORDER BY r.createdAt DESC",
                            MentorRequest.class)
                    .getResultList();
            System.out.println("Found " + requests.size() + " requests for admin");
            return requests;
        }
    }

    public List<MentorRequest> getRequestsByStatus(RequestStatus status,
                                                   String userEmail, String role) {
        if ("STUDENT".equals(role)) {
            return em.createQuery(
                            "SELECT r FROM MentorRequest r WHERE r.student.email = :email " +
                                    "AND r.status = :status ORDER BY r.createdAt DESC", MentorRequest.class)
                    .setParameter("email", userEmail)
                    .setParameter("status", status)
                    .getResultList();
        } else {
            return em.createQuery(
                            "SELECT r FROM MentorRequest r WHERE r.alumni.email = :email " +
                                    "AND r.status = :status ORDER BY r.createdAt DESC", MentorRequest.class)
                    .setParameter("email", userEmail)
                    .setParameter("status", status)
                    .getResultList();
        }
    }

    public MentorRequest updateStatus(Long id, RequestStatus status, String alumniEmail) {
        MentorRequest request = findById(id);
        if (request == null) {
            throw new IllegalArgumentException("Request not found");
        }

        request.setStatus(status);
        MentorRequest updatedRequest = em.merge(request);
        
        // If request is accepted, create a chat room
        if (status == RequestStatus.ACCEPTED) {
            try {
                chatService.createChatRoomFromMentorRequest(updatedRequest);
            } catch (Exception e) {
                System.err.println("Error creating chat room: " + e.getMessage());
                // Don't fail the request update if chat room creation fails
            }
        }
        
        // Send email notification to student about status change
        emailService.sendRequestStatusUpdate(
            request.getStudent().getEmail(),
            request.getAlumni().getFullName(),
            status.toString()
        );
        
        return updatedRequest;
    }

    // Temporarily commented out JMS method
    /*
    private void sendWelcomeNotification(MentorRequest request) {
        try {
            String message = String.format(
                    "New mentor request from %s to %s",
                    request.getStudent().getFullName(),
                    request.getAlumni().getFullName()
            );
            jmsContext.createProducer().send(welcomeQueue, message);
        } catch (Exception e) {
            // Log error but don't fail the request
            e.printStackTrace();
        }
    }
    */
}
