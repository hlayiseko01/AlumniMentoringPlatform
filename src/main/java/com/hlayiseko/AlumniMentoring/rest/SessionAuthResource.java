package com.hlayiseko.AlumniMentoring.rest;

import jakarta.annotation.security.PermitAll;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;

/**
 * Creates a container-managed session using programmatic login,
 * without changing the existing AuthResource.login() JSON contract.
 */
@Path("/auth")
@PermitAll
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class SessionAuthResource {

    @Context
    private HttpServletRequest request;

    @POST
    @Path("/session-login")
    public Response sessionLogin(LoginRequest body) {
        if (body == null || body.getEmail() == null || body.getPassword() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", "Email and password are required"))
                    .build();
        }
        try {
            request.login(body.getEmail(), body.getPassword());
            // At this point the container established an authenticated session.
            return Response.ok(Map.of("message", "Login successful"))
                    .build();
        } catch (ServletException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("message", "Invalid email or password"))
                    .build();
        }
    }


}
