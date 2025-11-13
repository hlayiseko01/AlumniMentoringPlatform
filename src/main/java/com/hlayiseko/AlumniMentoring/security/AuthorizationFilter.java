package com.hlayiseko.AlumniMentoring.security;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AuthorizationFilter {

    public boolean canAccessAlumniProfile(String userEmail, Long alumniId, String role) {
        if (role.equals("ADMIN")) {
            return true;
        }

        if (role.equals("ALUMNI")) {
            // Alumni can only edit their own profile
            // Implementation needed to check if alumniId matches userEmail
            return true;
        }

        return false;
    }

    public boolean canModerateRequest(String role) {
        return role.equals("ADMIN") || role.equals("ALUMNI");
    }
}
