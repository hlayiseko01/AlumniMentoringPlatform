package com.hlayiseko.AlumniMentoring.rest;
import com.hlayiseko.AlumniMentoring.dto.MentorRequestDTO;
import com.hlayiseko.AlumniMentoring.entity.*;
import com.hlayiseko.AlumniMentoring.service.*;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/requests")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MentorRequestResource {

    @Inject
    private MentorRequestService requestService;

    @PersistenceContext(unitName = "AlumniPU")
    private EntityManager em;

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

    @POST
    public Response createRequest(@Valid MentorRequest request) {
        if (!isAuthenticated()) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", "Authentication required"))
                    .build();
        }

        // Only students can create requests
        if (!hasRole("STUDENT")) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("error", "Only students can create mentorship requests"))
                    .build();
        }

        var session = httpRequest.getSession(false);
        String studentEmail = (String) session.getAttribute("userEmail");
        MentorRequest created = requestService.createRequest(request, studentEmail);
        return Response.status(Response.Status.CREATED)
                // Student created it -> return student-safe view (no student details)
                .entity(MentorRequestDTO.forStudentView(created))
                .build();
    }


    @GET
    public Response getRequests(@QueryParam("status") String status) {
        if (!isAuthenticated()) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", "Authentication required"))
                    .build();
        }

        var session = httpRequest.getSession(false);
        String userEmail = (String) session.getAttribute("userEmail");
        String role = (String) session.getAttribute("userRole");

        List<MentorRequest> requests;
        if (status != null) {
            RequestStatus reqStatus = RequestStatus.valueOf(status.toUpperCase());
            requests = requestService.getRequestsByStatus(reqStatus, userEmail, role);
        } else {
            requests = requestService.getUserRequests(userEmail, role);
        }

        boolean isAlumniOrAdmin = "ALUMNI".equals(role) || "ADMIN".equals(role);
        List<MentorRequestDTO> dtoList = requests.stream()
                .map(r -> isAlumniOrAdmin ? MentorRequestDTO.forAlumniView(r)
                                           : MentorRequestDTO.forStudentView(r))
                .collect(Collectors.toList());

        return Response.ok(dtoList).build();
    }

    @PUT
    @Path("/{id}")
    public Response updateRequestStatus(@PathParam("id") Long id,
                                        @QueryParam("status") String status) {
        if (!isAuthenticated()) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", "Authentication required"))
                    .build();
        }

        // Only alumni and admins can approve/reject requests
        if (!hasAnyRole("ALUMNI", "ADMIN")) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("error", "Only alumni and admins can approve/reject requests"))
                    .build();
        }

        var session = httpRequest.getSession(false);
        String alumniEmail = (String) session.getAttribute("userEmail");
        RequestStatus newStatus = RequestStatus.valueOf(status.toUpperCase());
        MentorRequest updated = requestService.updateStatus(id, newStatus, alumniEmail);
        // Alumni/Admin updating -> show student details
        return Response.ok(MentorRequestDTO.forAlumniView(updated)).build();
    }

    @GET
    @Path("/{id}")
    public Response getRequestById(@PathParam("id") Long id) {
        if (!isAuthenticated()) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", "Authentication required"))
                    .build();
        }

        MentorRequest request = requestService.findById(id);
        if (request == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        var session = httpRequest.getSession(false);
        String role = (String) session.getAttribute("userRole");
        boolean isAlumniOrAdmin = "ALUMNI".equals(role) || "ADMIN".equals(role);
        return Response.ok(
                isAlumniOrAdmin ? MentorRequestDTO.forAlumniView(request)
                                 : MentorRequestDTO.forStudentView(request)
        ).build();
    }
}
