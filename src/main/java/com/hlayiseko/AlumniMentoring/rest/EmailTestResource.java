package com.hlayiseko.AlumniMentoring.rest;

import com.hlayiseko.AlumniMentoring.entity.User;
import com.hlayiseko.AlumniMentoring.entity.Role;
import com.hlayiseko.AlumniMentoring.service.EmailService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/test/email")
@Produces(MediaType.APPLICATION_JSON)
public class EmailTestResource {

    @Inject
    private EmailService emailService;

    @POST
    @Path("/welcome")
    public Response testWelcomeEmail(@QueryParam("email") String email, 
                                   @QueryParam("name") String name,
                                   @QueryParam("role") String role) {
        try {
            // Create a test user
            User testUser = new User() {
                @Override
                public String getFullName() { return name != null ? name : "Test User"; }
                @Override
                public String getEmail() { return email != null ? email : "test@example.com"; }
                @Override
                public Role getRole() { 
                    return role != null ? Role.valueOf(role.toUpperCase()) : Role.STUDENT; 
                }
            };
            
            emailService.sendWelcomeEmail(testUser);
            
            return Response.ok()
                    .entity("{\"message\":\"Welcome email sent successfully to " + email + "\"}")
                    .build();
                    
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Failed to send email: " + e.getMessage() + "\"}")
                    .build();
        }
    }
}
