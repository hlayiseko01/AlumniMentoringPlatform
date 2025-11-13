package com.hlayiseko.AlumniMentoring.websocket;

import com.hlayiseko.AlumniMentoring.entity.*;
import com.hlayiseko.AlumniMentoring.service.ChatService;
import jakarta.ejb.EJB;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/chat/{chatRoomId}/{userId}")
public class ChatWebSocket {

    @EJB
    private ChatService chatService;

    @PersistenceContext(unitName = "AlumniPU")
    private EntityManager em;

    // Store active sessions by chat room and user
    private static final Map<String, Map<String, Session>> chatSessions = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("chatRoomId") String chatRoomId, @PathParam("userId") String userId) {
        try {
            System.out.println("WebSocket connection attempt - Room: " + chatRoomId + ", User: " + userId);
            
            Long roomId = Long.parseLong(chatRoomId);
            Long userIdLong = Long.parseLong(userId);
            
            // Validate user exists
            User user = getUserById(userIdLong);
            if (user == null) {
                System.err.println("User not found: " + userIdLong);
                session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "User not found"));
                return;
            }
            
            // Validate chat room exists and user is participant
            ChatRoom chatRoom = chatService.getChatRoomById(roomId);
            if (chatRoom == null) {
                System.err.println("Chat room not found: " + roomId);
                session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "Chat room not found"));
                return;
            }

            // Check if user is participant in this chat room
            boolean isParticipant = chatRoom.getStudent().getId().equals(userIdLong) || 
                                  chatRoom.getAlumni().getId().equals(userIdLong);
            if (!isParticipant) {
                System.err.println("User " + userIdLong + " is not a participant in chat room " + roomId);
                session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "Access denied"));
                return;
            }

            // Store session
            chatSessions.computeIfAbsent(chatRoomId, k -> new ConcurrentHashMap<>()).put(userId, session);
            
            // Mark messages as read
            chatService.markMessagesAsRead(roomId, user);

            // Notify other participants that user joined
            broadcastToRoom(chatRoomId, createSystemMessage(user.getFullName() + " joined the chat"), userId);
            
            System.out.println("User " + user.getFullName() + " (" + userId + ") joined chat room " + chatRoomId);
            
        } catch (NumberFormatException e) {
            System.err.println("Invalid room ID or user ID format: " + e.getMessage());
            try {
                session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "Invalid ID format"));
            } catch (IOException ioException) {
                System.err.println("Error closing session: " + ioException.getMessage());
            }
        } catch (Exception e) {
            System.err.println("Error opening WebSocket connection: " + e.getMessage());
            e.printStackTrace();
            try {
                session.close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, "Error opening connection"));
            } catch (IOException ioException) {
                System.err.println("Error closing session: " + ioException.getMessage());
            }
        }
    }

    @OnMessage
    public void onMessage(String message, Session session, @PathParam("chatRoomId") String chatRoomId, @PathParam("userId") String userId) {
        try {
            Long roomId = Long.parseLong(chatRoomId);
            Long userIdLong = Long.parseLong(userId);
            
            System.out.println("Processing message from user " + userIdLong + " in room " + roomId);
            
            // Parse messag JSON
            JsonObject messageJson = Json.createReader(new java.io.StringReader(message)).readObject();
            String content = messageJson.getString("content", "");
            String messageType = messageJson.getString("type", "TEXT");
            
            if (content.trim().isEmpty()) {
                System.out.println("Empty message content, ignoring");
                return;
            }

            // Get user and send messag
            User user = getUserById(userIdLong);
            if (user == null) {
                System.err.println("User not found for ID: " + userIdLong);
                return;
            }

            System.out.println("Sending message as user: " + user.getFullName() + " (ID: " + user.getId() + ")");
            Message messag = chatService.sendMessage(roomId, user, content);
            
            // Create message response
            JsonObject response = Json.createObjectBuilder()
                    .add("type", "message")
                    .add("id", messag.getId())
                    .add("content", messag.getContent())
                    .add("senderId", messag.getSender().getId())
                    .add("senderName", messag.getSender().getFullName())
                    .add("senderRole", messag.getSender().getRole().toString())
                    .add("messageType", "TEXT")
                    .add("sentAt", messag.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .add("isRead", messag.getRead())
                    .build();

            // Broadcast to all participants in the chat room
            broadcastToRoom(chatRoomId, response.toString(), userId);
            
        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @OnClose
    public void onClose(Session session, @PathParam("chatRoomId") String chatRoomId, @PathParam("userId") String userId) {
        try {
            // Remove session
            Map<String, Session> roomSessions = chatSessions.get(chatRoomId);
            if (roomSessions != null) {
                roomSessions.remove(userId);
                if (roomSessions.isEmpty()) {
                    chatSessions.remove(chatRoomId);
                }
            }

            // Notify other participants that user left
            broadcastToRoom(chatRoomId, createSystemMessage(userId + " left the chat"), userId);
            
            System.out.println("User " + userId + " left chat room " + chatRoomId);
            
        } catch (Exception e) {
            System.err.println("Error closing WebSocket connection: " + e.getMessage());
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable, @PathParam("chatRoomId") String chatRoomId, @PathParam("userId") String userId) {
        System.err.println("WebSocket error for user " + userId + " in room " + chatRoomId + ": " + throwable.getMessage());
        throwable.printStackTrace();
    }

    private void broadcastToRoom(String chatRoomId, String message, String excludeUserId) {
        Map<String, Session> roomSessions = chatSessions.get(chatRoomId);
        if (roomSessions != null) {
            roomSessions.entrySet().stream()
                    .filter(entry -> !entry.getKey().equals(excludeUserId))
                    .forEach(entry -> {
                        try {
                            entry.getValue().getBasicRemote().sendText(message);
                        } catch (IOException e) {
                            System.err.println("Error sending message to user " + entry.getKey() + ": " + e.getMessage());
                        }
                    });
        }
    }

    private String createSystemMessage(String content) {
        return Json.createObjectBuilder()
                .add("type", "system")
                .add("content", content)
                .add("sentAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .build()
                .toString();
    }

    private User getUserById(Long userId) {
        try {
            return em.find(User.class, userId);
        } catch (Exception e) {
            return null;
        }
    }
}
