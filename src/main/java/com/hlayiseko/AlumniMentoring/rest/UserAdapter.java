/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hlayiseko.AlumniMentoring.rest;

import com.hlayiseko.AlumniMentoring.entity.User;
import com.hlayiseko.AlumniMentoring.entity.Student;
import com.hlayiseko.AlumniMentoring.entity.AlumniProfile;
import com.hlayiseko.AlumniMentoring.entity.*;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.bind.adapter.JsonbAdapter;
import java.util.ArrayList;
import java.util.List;

public class UserAdapter implements JsonbAdapter<User, JsonObject> {

    @Override
    public JsonObject adaptToJson(User user) throws Exception {
        var builder = Json.createObjectBuilder()
                .add("id", user.getId() != null ? user.getId() : 0)
                .add("email", user.getEmail() != null ? user.getEmail() : "")
                .add("fullName", user.getFullName() != null ? user.getFullName() : "")
                .add("role", user.getRole() != null ? user.getRole().name() : "")
                .add("password", user.getPassword() != null ? user.getPassword() : "");

        if (user instanceof Student student) {
            builder.add("enrollmentYear", student.getEnrollmentYear() != null ? student.getEnrollmentYear() : 0)
                    .add("major", student.getMajor() != null ? student.getMajor() : "");
        } else if (user instanceof AlumniProfile alumni) {
            builder.add("graduationYear", alumni.getGraduationYear() != null ? alumni.getGraduationYear() : 0)
                    .add("company", alumni.getCompany() != null ? alumni.getCompany() : "")
                    .add("position", alumni.getPosition() != null ? alumni.getPosition() : "")
                    .add("bio", alumni.getBio() != null ? alumni.getBio() : "")
                    .add("linkedin", alumni.getLinkedin() != null ? alumni.getLinkedin() : "")
                    .add("availableForMentoring", alumni.getAvailableForMentoring() != null ? alumni.getAvailableForMentoring() : true);

            // Add skills array
            var skillsArray = Json.createArrayBuilder();
            if (alumni.getSkills() != null) {
                for (String skill : alumni.getSkills()) {
                    skillsArray.add(skill);
                }
            }
            builder.add("skills", skillsArray);
        }

        return builder.build();
    }

    @Override
    public User adaptFromJson(JsonObject json) throws Exception {
        String roleStr = json.getString("role", "");
        User user;

        // Instantiate correct subclass
        if ("STUDENT".equalsIgnoreCase(roleStr)) {
            user = new Student();
            if (json.containsKey("enrollmentYear")) {
                ((Student) user).setEnrollmentYear(json.getJsonNumber("enrollmentYear").intValue());
            }
            ((Student) user).setMajor(json.getString("major", ""));
        } else if ("ALUMNI".equalsIgnoreCase(roleStr)) {
            AlumniProfile alumni = new AlumniProfile();
            if (json.containsKey("graduationYear")) {
                alumni.setGraduationYear(json.getJsonNumber("graduationYear").intValue());
            }
            alumni.setCompany(json.getString("company", ""));
            alumni.setPosition(json.getString("position", ""));
            alumni.setBio(json.getString("bio", ""));
            alumni.setLinkedin(json.getString("linkedin", ""));
            alumni.setAvailableForMentoring(json.getBoolean("availableForMentoring", true));

            // Handle skills array
            List<String> skills = new ArrayList<>();
            if (json.containsKey("skills")) {
                JsonArray jsonSkills = json.getJsonArray("skills");
                for (var s : jsonSkills) {
                    skills.add(s.toString().replace("\"", "")); // clean quotes
                }
            }
            alumni.setSkills(skills);
            user = alumni;
        } else {
            throw new IllegalArgumentException("Invalid role: " + roleStr);
        }

        // Common fields
        if (json.containsKey("id")) {
            user.setId(json.getJsonNumber("id").longValue());
        }
        user.setEmail(json.getString("email", null));
        user.setFullName(json.getString("fullName", null));
        user.setPassword(json.getString("password", null));
        user.setRole(roleStr.isEmpty() ? null : Role.valueOf(roleStr.toUpperCase()));

        return user;
    }
}



