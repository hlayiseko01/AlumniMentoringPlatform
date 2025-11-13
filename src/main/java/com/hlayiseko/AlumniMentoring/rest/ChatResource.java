package com.hlayiseko.AlumniMentoring.rest;

import com.hlayiseko.AlumniMentoring.entity.*;
import com.hlayiseko.AlumniMentoring.service.ChatService;
import com.hlayiseko.AlumniMentoring.service.MentorRequestService;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/chat")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ChatResource {

    @Inject
    private ChatService chatService;

    @Inject
    private MentorRequestService mentorRequestService;

    @PersistenceContext(unitName = "AlumniPU")
    private EntityManager em;

    @Context
    private HttpServletRequest httpRequest;

    private boolean isAuthenticated() {
        var session = httpRequest.getSession(false);
        return session != null && session.getAttribute("userEmail") != null;
    }

    @GET
    @Path("/rooms")
    public Response getUserChatRooms() {
        System.out.println("=== getUserChatRooms called ===");
        
        if (!isAuthenticated()) {
            System.out.println("Authentication failed");
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", "Authentication required"))
                    .build();
        }

        var session = httpRequest.getSession(false);
        String userEmail = (String) session.getAttribute("userEmail");
        Long userId = (Long) session.getAttribute("userId");
        
        System.out.println("Session data - Email: " + userEmail + ", UserId: " + userId);
        
        try {
            // Get user by ID from session (more reliable)
            User user = em.find(User.class, userId);
            if (user == null) {
                System.out.println("User not found for ID: " + userId);
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "User not found"))
                        .build();
            }
            
            System.out.println("User found: " + user.getEmail() + ", Role: " + user.getRole());

            List<ChatRoom> rooms = chatService.getUserChatRooms(user);
            System.out.println("Found " + rooms.size() + " chat rooms for user: " + user.getEmail());
            
            List<Object> dto = rooms.stream()
                    .map(chatRoom -> {
                        try {
                            return createChatRoomResponse(chatRoom, user);
                        } catch (Exception e) {
                            System.err.println("Error creating chat room response for room " + chatRoom.getId() + ": " + e.getMessage());
                            e.printStackTrace();
                            // Return a basic response without unread count
                            final long fallbackUnreadCount = 0L;
                            return new Object() {
                                public final Long id = chatRoom.getId();
                                public final String roomName = chatRoom.getRoomName();
                                public final Long studentId = chatRoom.getStudent().getId();
                                public final String studentName = chatRoom.getStudent().getFullName();
                                public final Long alumniId = chatRoom.getAlumni().getId();
                                public final String alumniName = chatRoom.getAlumni().getFullName();
                                public final String createdAt = chatRoom.getCreatedAt().toString();
                                public final String lastMessageAt = chatRoom.getLastMessageAt() != null ? chatRoom.getLastMessageAt().toString() : "";
                                public final Boolean isActive = chatRoom.getIsActive();
                                public final Long unreadCount = fallbackUnreadCount;
                            };
                        }
                    })
                    .collect(Collectors.toList());

            return Response.ok(dto).build();
        } catch (NoResultException e) {
            System.err.println("NoResultException in getUserChatRooms: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.UNAUTHORIZED).build();
        } catch (Exception ex) {
            System.err.println("Exception in getUserChatRooms: " + ex.getMessage());
            ex.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"" + ex.getMessage() + "\"}")
                    .build();
        }
    }

    @POST
    @Path("/rooms/create-from-request/{requestId}")
    public Response createChatRoomFromRequest(@PathParam("requestId") Long requestId) {
        if (!isAuthenticated()) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", "Authentication required"))
                    .build();
        }

        try {
            var session = httpRequest.getSession(false);
            Long userId = (Long) session.getAttribute("userId");
            User user = em.find(User.class, userId);
            if (user == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "User not found"))
                        .build();
            }

            MentorRequest mentorRequest = mentorRequestService.findById(requestId);
            if (mentorRequest == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\":\"Mentor request not found\"}")
                        .build();
            }

            if (mentorRequest.getStatus() != RequestStatus.ACCEPTED) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\":\"Mentor request must be accepted to create chat room\"}")
                        .build();
            }

            ChatRoom chatRoom = chatService.createChatRoomFromMentorRequest(mentorRequest);
            
            return Response.status(Response.Status.CREATED)
                    .entity(createChatRoomResponse(chatRoom, user))
                    .build();
                    
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"" + e.getMessage() + "\"}")
                    .build();
        }
    }

    @GET
    @Path("/rooms/{roomId}/messages")
    public Response getChatMessages(@PathParam("roomId") Long roomId,
                                  @QueryParam("limit") @DefaultValue("50") int limit,
                                  @QueryParam("offset") @DefaultValue("0") int offset) {
        if (!isAuthenticated()) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", "Authentication required"))
                    .build();
        }

        try {
            ChatRoom chatRoom = chatService.getChatRoomById(roomId);
            if (chatRoom == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "Chat room not found"))
                        .build();
            }

            // Check if user is participant in this chat room
            var session = httpRequest.getSession(false);
            Long userId = (Long) session.getAttribute("userId");
            User user = em.find(User.class, userId);
            if (user == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "User not found"))
                        .build();
            }

            if (!chatRoom.isParticipant(user)) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity(Map.of("error", "Access denied to this chat room"))
                        .build();
            }

            List<Message> messages = chatService.getChatMessages(roomId, limit, offset);
            
            return Response.ok(messages.stream()
                    .map(this::createMessageResponse)
                    .collect(Collectors.toList()))
                    .build();
                    
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    @POST
    @Path("/rooms/{roomId}/messages")
    public Response sendMessage(@PathParam("roomId") Long roomId,
                              @QueryParam("content") String content) {
        try {
            // This is a simplified implementation
            // In a real application, you'd get the user from the security context
            // and validate they're a participant in the chat room
            
            return Response.status(Response.Status.NOT_IMPLEMENTED)
                    .entity("{\"message\":\"Use WebSocket for real-time messaging\"}")
                    .build();
                    
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"" + e.getMessage() + "\"}")
                    .build();
        }
    }

 

    @POST
    @Path("/rooms/{roomId}/mark-read")
    public Response markMessagesAsRead(@PathParam("roomId") Long roomId) {
        if (!isAuthenticated()) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", "Authentication required"))
                    .build();
        }

        try {
            var session = httpRequest.getSession(false);
            Long userId = (Long) session.getAttribute("userId");
            
            // Get user entity
            User user = em.find(User.class, userId);
            if (user == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "User not found"))
                        .build();
            }
            
            // Mark messages as read
            chatService.markMessagesAsRead(roomId, user);
            return Response.ok(Map.of("message", "Messages marked as read")).build();
                    
        } catch (Exception e) {
            System.err.println("Error marking messages as read: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/debug/rooms")
    public Response debugChatRooms() {
        if (!isAuthenticated()) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", "Authentication required"))
                    .build();
        }

        try {
            var session = httpRequest.getSession(false);
            Long userId = (Long) session.getAttribute("userId");
            String userEmail = (String) session.getAttribute("userEmail");
            
            // Get all chat rooms
            List<ChatRoom> allRooms = em.createQuery("SELECT cr FROM ChatRoom cr", ChatRoom.class).getResultList();
            
            // Get user
            User user = em.find(User.class, userId);
            
            return Response.ok(Map.of(
                "userEmail", userEmail,
                "userId", userId,
                "userRole", user != null ? user.getRole().toString() : "null",
                "totalRooms", allRooms.size(),
                "rooms", allRooms.stream().map(room -> Map.of(
                    "id", room.getId(),
                    "studentEmail", room.getStudent().getEmail(),
                    "alumniEmail", room.getAlumni().getEmail(),
                    "isActive", room.getIsActive()
                )).toList()
            )).build();
                    
        } catch (Exception e) {
            System.err.println("Error in debug chat rooms: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/unread-count")
    public Response getUnreadCount() {
        if (!isAuthenticated()) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", "Authentication required"))
                    .build();
        }

        try {
            var session = httpRequest.getSession(false);
            String userEmail = (String) session.getAttribute("userEmail");
            Long userId = (Long) session.getAttribute("userId");
            
            // Get user entity
            User user = em.find(User.class, userId);
            if (user == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "User not found"))
                        .build();
            }
            
            long unreadCount = chatService.getTotalUnreadMessageCount(user);
            return Response.ok(Map.of("unreadCount", unreadCount)).build();
                    
        } catch (Exception e) {
            System.err.println("Error getting unread count: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    private long getUnreadCountForRoom(Long roomId, User currentUser) {
        try {
            long count = chatService.getUnreadMessageCount(roomId, currentUser);
            System.out.println("Unread count for room " + roomId + ": " + count);
            return count;
        } catch (Exception e) {
            System.err.println("Error getting unread count for room " + roomId + ": " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    private Object createChatRoomResponse(ChatRoom chatRoom, User currentUser) {
        try {
            System.out.println("Creating chat room response for room " + chatRoom.getId() + " and user " + currentUser.getEmail());
            
            // Get unread count for this specific chat room
            final long unreadCountValue = getUnreadCountForRoom(chatRoom.getId(), currentUser);
            
            return new Object() {
                public final Long id = chatRoom.getId();
                public final String roomName = chatRoom.getRoomName();
                public final Long studentId = chatRoom.getStudent().getId();
                public final String studentName = chatRoom.getStudent().getFullName();
                public final Long alumniId = chatRoom.getAlumni().getId();
                public final String alumniName = chatRoom.getAlumni().getFullName();
                public final String createdAt = chatRoom.getCreatedAt().toString();
                public final String lastMessageAt = chatRoom.getLastMessageAt() != null ? chatRoom.getLastMessageAt().toString() : "";
                public final Boolean isActive = chatRoom.getIsActive();
                public final Long unreadCount = unreadCountValue;
            };
        } catch (Exception e) {
            System.err.println("Error in createChatRoomResponse: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private Object createMessageResponse(Message message) {
        return new Object() {
            public final Long id = message.getId();
            public final String content = message.getContent();
            public final Long senderId = message.getSender().getId();
            public final String senderName = message.getSender().getFullName();
            public final String senderRole = message.getSender().getRole().toString();
            public final String messageType = "TEXT";
            public final String sentAt = message.getCreatedAt().toString();
            public final Boolean isRead = message.getRead();
            public final Long recipientId = message.getRecipient().getId();
            public final String recipientName = message.getRecipient().getFullName();
        };
    }
}
