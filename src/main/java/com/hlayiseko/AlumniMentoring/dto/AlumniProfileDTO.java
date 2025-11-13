/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hlayiseko.AlumniMentoring.dto;

import java.util.List;

/**
 *
 * @author nhlay
 */
public record AlumniProfileDTO(
        Long id,
        String fullName,
        String email,
        String company,
        String position,
        String bio,
        boolean availableForMentoring,
        int graduationYear,
        String linkedin,
        List<String> skills
) {}
