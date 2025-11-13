package com.hlayiseko.AlumniMentoring.service;

import com.hlayiseko.AlumniMentoring.entity.*;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.*;
import jakarta.security.enterprise.identitystore.Pbkdf2PasswordHash;
import java.util.List;

/**
 * Authentication Service
 * 
 * Handles user authentication, registration, and credential validation.
 * Uses PBKDF2 password hashing for secure password storage.
 * 
 * @author Alumni Mentoring Platform
 * @version 1.0
 */
@Stateless
public class AuthService {

    @PersistenceContext(unitName = "AlumniPU")
    private EntityManager em;

    @Inject
    private Pbkdf2PasswordHash passwordHash;

    @Inject
    private EmailService emailService;

    /**
     * Validates user credentials against the database.
     * 
     * @param email The user's email address
     * @param password The plain text password to validate
     * @return true if credentials are valid, false otherwise
     */
    public boolean validateCredentials(String email, String password) {
        try {
            User user = em.createQuery(
                            "SELECT u FROM User u WHERE u.email = :email", User.class)
                    .setParameter("email", email)
                    .getSingleResult();

            // Verify password using Pbkdf2PasswordHash
            return passwordHash.verify(password.toCharArray(), user.getPassword());
        } catch (NoResultException e) {
            return false;
        } catch (Exception e) {
            System.err.println("Error validating credentials: " + e.getMessage());
            return false;
        }
    }

    /**
     * Registers a new user in the system.
     * 
     * @param user The user entity to register
     * @return The persisted user entity
     */
    public User register(User user) {
        // Hash the password before storing
        user.setPassword(passwordHash.generate(user.getPassword().toCharArray()));
        em.persist(user);
        
        // Send welcome email notification
        emailService.sendWelcomeEmail(user);
        
        return user;
    }

    /**
     * Finds a user by their email address.
     * 
     * @param email The email address to search for
     * @return The user entity if found, null otherwise
     */
    public User findUserByEmail(String email) {
        try {
            return em.createQuery(
                            "SELECT u FROM User u WHERE u.email = :email", User.class)
                    .setParameter("email", email)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Updates an existing user in the system.
     * 
     * @param user The user entity to update
     * @return The updated user entity
     */
    public User updateUser(User user) {
        try {
            return em.merge(user);
        } catch (Exception e) {
            System.err.println("Error updating user: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Retrieves all users from the database.
     * Used for debugging and administrative purposes.
     * 
     * @return List of all users in the system
     */
    public List<User> getAllUsers() {
        try {
            return em.createQuery("SELECT u FROM User u", User.class).getResultList();
        } catch (Exception e) {
            System.err.println("Error getting all users: " + e.getMessage());
            return List.of();
        }
    }

}
