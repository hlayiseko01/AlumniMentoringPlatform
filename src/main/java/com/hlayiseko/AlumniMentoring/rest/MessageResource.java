package com.hlayiseko.AlumniMentoring.rest;

import com.hlayiseko.AlumniMentoring.entity.*;
import com.hlayiseko.AlumniMentoring.service.*;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.List;

@Path("/messages")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MessageResource {

    @Inject
    private MessageService messageService;

    @POST
    @RolesAllowed({"STUDENT", "ALUMNI"})
    public Response sendMessage(@Valid Message message,
                                @Context SecurityContext securityContext) {
        String senderEmail = securityContext.getUserPrincipal().getName();
        Message sent = messageService.sendMessage(message, senderEmail);
        return Response.status(Response.Status.CREATED)
                .entity(sent)
                .build();
    }

    @GET
    @RolesAllowed({"STUDENT", "ALUMNI", "ADMIN"})
    public Response getMessages(@QueryParam("withUser") Long userId,
                                @Context SecurityContext securityContext) {
        String userEmail = securityContext.getUserPrincipal().getName();
        List<Message> messages;

        if (userId != null) {
            messages = messageService.getConversation(userEmail, userId);
        } else {
            messages = messageService.getUserMessages(userEmail);
        }

        return Response.ok(messages).build();
    }

    @PUT
    @Path("/{id}/read")
    @RolesAllowed({"STUDENT", "ALUMNI"})
    public Response markAsRead(@PathParam("id") Long id,
                               @Context SecurityContext securityContext) {
        String userEmail = securityContext.getUserPrincipal().getName();
        messageService.markAsRead(id, userEmail);
        return Response.ok().build();
    }

    @GET
    @Path("/unread/count")
    @RolesAllowed({"STUDENT", "ALUMNI"})
    public Response getUnreadCount(@Context SecurityContext securityContext) {
        String userEmail = securityContext.getUserPrincipal().getName();
        long count = messageService.getUnreadCount(userEmail);
        return Response.ok(new UnreadCountDTO(count)).build();
    }
}
