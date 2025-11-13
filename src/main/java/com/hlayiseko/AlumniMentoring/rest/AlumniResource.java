package com.hlayiseko.AlumniMentoring.rest;

import com.hlayiseko.AlumniMentoring.dto.AlumniProfileDTO;
import com.hlayiseko.AlumniMentoring.entity.*;
import com.hlayiseko.AlumniMentoring.service.*;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.List;
import java.util.Map;

@Path("/alumni")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AlumniResource {

    @Inject
    private AlumniService alumniService;

    @Context
    private HttpServletRequest httpRequest;

    private boolean isAuthenticated() {
        var session = httpRequest.getSession(false);
        return session != null && session.getAttribute("userEmail") != null;
    }

    private String getUserRole() {
        var session = httpRequest.getSession(false);
        return session != null ? (String) session.getAttribute("userRole") : null;
    }

    private boolean hasRole(String requiredRole) {
        String userRole = getUserRole();
        return requiredRole.equals(userRole);
    }

    private boolean hasAnyRole(String... roles) {
        String userRole = getUserRole();
        for (String role : roles) {
            if (role.equals(userRole)) {
                return true;
            }
        }
        return false;
    }

    @GET
    public Response getAlumni(@QueryParam("skill") String skill,
                              @QueryParam("company") String company) {
        if (!isAuthenticated()) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", "Authentication required"))
                    .build();
        }

        // Only students can browse alumni - alumni should not see other alumni
        String userRole = getUserRole();
        if (!"STUDENT".equals(userRole)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("error", "Only students can browse alumni"))
                    .build();
        }

        List<AlumniProfile> alumni;

        if (skill != null && !skill.isEmpty()) {
            alumni = alumniService.searchBySkill(skill);
        } else if (company != null && !company.isEmpty()) {
            alumni = alumniService.searchByCompany(company);
        } else {
            alumni = alumniService.getAllAvailableAlumni();
        }

    // Map entities to DTOs
    List<AlumniProfileDTO> alumniDTOs = alumni.stream()
            .map(a -> new AlumniProfileDTO(
                    a.getId(),
                    a.getFullName(),
                    a.getEmail(),
                    a.getCompany(),
                    a.getPosition(),
                    a.getBio(),
                    a.getAvailableForMentoring(),
                    a.getGraduationYear(),
                    a.getLinkedin(),
                    a.getSkills()
            ))
            .toList();

    return Response.ok(alumniDTOs).build();
}


    @GET
    @Path("/{id}")
    public Response getAlumniById(@PathParam("id") Long id) {
        if (!isAuthenticated()) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", "Authentication required"))
                    .build();
        }

        AlumniProfile alumni = alumniService.findById(id);
        if (alumni == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(alumni).build();
    }

    @POST
    public Response createAlumni(@Valid AlumniProfile alumni) {
        if (!isAuthenticated()) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", "Authentication required"))
                    .build();
        }

        // Only admins can create alumni profiles
        if (!hasRole("ADMIN")) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("error", "Only admins can create alumni profiles"))
                    .build();
        }

        AlumniProfile created = alumniService.create(alumni);
        return Response.status(Response.Status.CREATED)
                .entity(created)
                .build();
    }

    @PUT
    @Path("/{id}")
    public Response updateAlumni(@PathParam("id") Long id,
                                 @Valid AlumniProfile alumni) {
        if (!isAuthenticated()) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", "Authentication required"))
                    .build();
        }

        // Only alumni and admins can update alumni profiles
        if (!hasAnyRole("ALUMNI", "ADMIN")) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("error", "Only alumni and admins can update alumni profiles"))
                    .build();
        }

        var session = httpRequest.getSession(false);
        String currentUser = (String) session.getAttribute("userEmail");
        AlumniProfile updated = alumniService.update(id, alumni, currentUser);
        return Response.ok(updated).build();
    }
}
