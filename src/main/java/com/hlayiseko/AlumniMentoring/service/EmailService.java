package com.hlayiseko.AlumniMentoring.service;

import com.hlayiseko.AlumniMentoring.entity.User;
import jakarta.annotation.Resource;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.jms.*;
import jakarta.json.Json;
import jakarta.json.JsonObject;

@Stateless
public class EmailService {

    @Resource(lookup = "jms/EmailQueue")
    private Queue emailQueue;

    @Inject
    private JMSContext jmsContext;

    public void sendWelcomeEmail(User user) {
        try {
            JsonObject emailData = Json.createObjectBuilder()
                    .add("to", user.getEmail())
                    .add("subject", "Welcome to Alumni Mentoring Platform!")
                    .add("userName", user.getFullName())
                    .add("userRole", user.getRole().toString())
                    .add("template", "welcome")
                    .build();

            MessageProducer producer = (MessageProducer) jmsContext.createProducer();
            TextMessage message = jmsContext.createTextMessage(emailData.toString());
            message.setStringProperty("emailType", "welcome");
            message.setStringProperty("recipient", user.getEmail());
            
            producer.send(emailQueue, message);
            System.out.println("Welcome email queued for: " + user.getEmail());
            
        } catch (Exception e) {
            System.err.println("Error sending welcome email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void sendMentorRequestNotification(String mentorEmail, String studentName, String message) {
        try {
            JsonObject emailData = Json.createObjectBuilder()
                    .add("to", mentorEmail)
                    .add("subject", "New Mentorship Request from " + studentName)
                    .add("studentName", studentName)
                    .add("message", message)
                    .add("template", "mentor_request")
                    .build();

            MessageProducer producer = (MessageProducer) jmsContext.createProducer();
            TextMessage jmsMessage = jmsContext.createTextMessage(emailData.toString());
            jmsMessage.setStringProperty("emailType", "mentor_request");
            jmsMessage.setStringProperty("recipient", mentorEmail);
            
            producer.send(emailQueue, jmsMessage);
            System.out.println("Mentor request notification queued for: " + mentorEmail);
            
        } catch (Exception e) {
            System.err.println("Error sending mentor request notification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void sendRequestStatusUpdate(String studentEmail, String mentorName, String status) {
        try {
            JsonObject emailData = Json.createObjectBuilder()
                    .add("to", studentEmail)
                    .add("subject", "Mentorship Request " + status)
                    .add("mentorName", mentorName)
                    .add("status", status)
                    .add("template", "request_status")
                    .build();

            MessageProducer producer = (MessageProducer) jmsContext.createProducer();
            TextMessage message = jmsContext.createTextMessage(emailData.toString());
            message.setStringProperty("emailType", "request_status");
            message.setStringProperty("recipient", studentEmail);
            
            producer.send(emailQueue, message);
            System.out.println("Request status update queued for: " + studentEmail);
            
        } catch (Exception e) {
            System.err.println("Error sending request status update: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
