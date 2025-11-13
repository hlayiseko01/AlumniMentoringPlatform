package com.hlayiseko.AlumniMentoring.rest;

import com.hlayiseko.AlumniMentoring.entity.*;
import com.hlayiseko.AlumniMentoring.service.*;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.Map;

/**
 * Authentication REST Resource
 * 
 * Handles user authentication, registration, and session management.
 * Provides endpoints for login, logout, registration, and user information retrieval.
 * 
 * @author Alumni Mentoring Platform
 * @version 1.0
 */
@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@PermitAll
public class AuthResource {

    @Inject
    private AuthService authService;

    private final Jsonb jsonb;

    @Context
    private HttpServletRequest httpRequest;

    private boolean isAuthenticated() {
        var session = httpRequest.getSession(false);
        return session != null && session.getAttribute("userEmail") != null;
    }

    public AuthResource() {
        // Configure JSON-B with your UserAdapter
        JsonbConfig config = new JsonbConfig().withAdapters(new UserAdapter());
        jsonb = JsonbBuilder.create(config);
    }

    
    /**
     * Authenticates a user and creates a session.
     * 
     * @param request Login request containing email and password
     * @return Response with success message or error
     */
    @POST
    @Path("/login")
    @PermitAll
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(LoginRequest request) {
        // Validate user credentials
        boolean valid = authService.validateCredentials(request.getEmail(), request.getPassword());

        if (!valid) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("message", "Invalid email or password"))
                    .build();
        }

        // Create session and store user info
        try {
            var session = httpRequest.getSession(true);
            
            // Get user details and store in session
            User user = authService.findUserByEmail(request.getEmail());
            if (user != null) {
                session.setAttribute("userEmail", user.getEmail());
                session.setAttribute("userRole", user.getRole().name());
                session.setAttribute("userId", user.getId());
            }
            
            return Response.ok(Map.of("message", "Login successful")).build();
        } catch (Exception e) {
            System.err.println("Error creating session: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("message", "Login failed"))
                    .build();
        }
    }

    
    /**
     * Registers a new user in the system.
     * 
     * @param userJson JSON object containing user registration data
     * @return Response with created user data or error
     */
    @POST
    @Path("/register")
    @PermitAll
    public Response register(JsonObject userJson) {
        try {
            // Deserialize JSON into correct subclass (Student or AlumniProfile)
            User user = jsonb.fromJson(userJson.toString(), User.class);

            // Persist user via service
            User created = authService.register(user);

            // Serialize back to JSON
            String jsonResponse = jsonb.toJson(created);

            return Response.status(Response.Status.CREATED)
                    .entity(jsonResponse)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"" + e.getMessage() + "\"}")
                    .build();
        }
    }

    /**
     * Retrieves the currently logged-in user's information.
     * 
     * @return Response with user data or unauthorized error
     */
    @GET
    @Path("/current-user")
    @PermitAll
    public Response getCurrentUser() {
        try {
            var session = httpRequest.getSession(false);
            if (session == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(Map.of("error", "No active session"))
                        .build();
            }

            String userEmail = (String) session.getAttribute("userEmail");
            if (userEmail == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(Map.of("error", "User not logged in"))
                        .build();
            }

            User user = authService.findUserByEmail(userEmail);
            if (user == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "User not found"))
                        .build();
            }

            // Serialize user to JSON
            String jsonResponse = jsonb.toJson(user);
            return Response.ok(jsonResponse).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Failed to get current user"))
                    .build();
        }
    }

    /**
     * Logs out the current user by invalidating their session.
     * 
     * @return Response with success message or error
     */
    @POST
    @Path("/logout")
    @PermitAll
    public Response logout() {
        try {
            var session = httpRequest.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            return Response.ok(Map.of("message", "Logout successful")).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Logout failed"))
                    .build();
        }
    }

    /**
     * Updates the current user's profile.
     * 
     * @param userJson JSON object containing updated user data
     * @return Response with updated user data or error
     */
    @PUT
    @Path("/profile")
    @PermitAll
    public Response updateProfile(JsonObject userJson) {
        if (!isAuthenticated()) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", "Authentication required"))
                    .build();
        }

        try {
            var session = httpRequest.getSession(false);
            String userEmail = (String) session.getAttribute("userEmail");
            
            // Get current user
            User currentUser = authService.findUserByEmail(userEmail);
            if (currentUser == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "User not found"))
                        .build();
            }

            // Deserialize updated user data
            User updatedUser = jsonb.fromJson(userJson.toString(), User.class);
            
            // Update only allowed fields (not email, password, or role)
            currentUser.setFullName(updatedUser.getFullName());
            
            // Update role-specific fields
            if (currentUser instanceof Student && updatedUser instanceof Student) {
                Student currentStudent = (Student) currentUser;
                Student updatedStudent = (Student) updatedUser;
                currentStudent.setEnrollmentYear(updatedStudent.getEnrollmentYear());
                currentStudent.setMajor(updatedStudent.getMajor());
            } else if (currentUser instanceof AlumniProfile && updatedUser instanceof AlumniProfile) {
                AlumniProfile currentAlumni = (AlumniProfile) currentUser;
                AlumniProfile updatedAlumni = (AlumniProfile) updatedUser;
                currentAlumni.setGraduationYear(updatedAlumni.getGraduationYear());
                currentAlumni.setCompany(updatedAlumni.getCompany());
                currentAlumni.setPosition(updatedAlumni.getPosition());
                currentAlumni.setBio(updatedAlumni.getBio());
                currentAlumni.setLinkedin(updatedAlumni.getLinkedin());
                currentAlumni.setAvailableForMentoring(updatedAlumni.getAvailableForMentoring());
                currentAlumni.setSkills(updatedAlumni.getSkills());
            }

            // Save updated user
            User savedUser = authService.updateUser(currentUser);
            
            // Serialize back to JSON
            String jsonResponse = jsonb.toJson(savedUser);
            return Response.ok(jsonResponse).build();
            
        } catch (Exception e) {
            System.err.println("Error updating profile: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }
}
