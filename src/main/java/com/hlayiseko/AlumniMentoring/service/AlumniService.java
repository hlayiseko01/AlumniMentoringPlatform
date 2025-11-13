package com.hlayiseko.AlumniMentoring.service;


import com.hlayiseko.AlumniMentoring.entity.*;
import jakarta.ejb.Stateless;
import jakarta.persistence.*;

import java.util.List;

@Stateless
public class AlumniService {

    @PersistenceContext(unitName = "AlumniPU")
    private EntityManager em;

    public AlumniProfile findById(Long id) {
        return em.find(AlumniProfile.class, id);
    }

    public List<AlumniProfile> getAllAvailableAlumni() {
        return em.createQuery(
                        "SELECT a FROM AlumniProfile a WHERE a.availableForMentoring = true",
                        AlumniProfile.class)
                .getResultList();
    }

    public List<AlumniProfile> searchBySkill(String skill) {
        return em.createQuery(
                        "SELECT DISTINCT a FROM AlumniProfile a JOIN a.skills s " +
                                "WHERE LOWER(s) LIKE LOWER(:skill) AND a.availableForMentoring = true",
                        AlumniProfile.class)
                .setParameter("skill", "%" + skill + "%")
                .getResultList();
    }

    public List<AlumniProfile> searchByCompany(String company) {
        return em.createQuery(
                        "SELECT a FROM AlumniProfile a WHERE LOWER(a.company) LIKE LOWER(:company) " +
                                "AND a.availableForMentoring = true",
                        AlumniProfile.class)
                .setParameter("company", "%" + company + "%")
                .getResultList();
    }

    public AlumniProfile create(AlumniProfile alumni) {
        em.persist(alumni);
        return alumni;
    }

    public AlumniProfile update(Long id, AlumniProfile alumni, String currentUserEmail) {
        AlumniProfile existing = findById(id);
        if (existing == null) {
            throw new IllegalArgumentException("Alumni not found");
        }

        // Update fields
        existing.setFullName(alumni.getFullName());
        existing.setGraduationYear(alumni.getGraduationYear());
        existing.setCompany(alumni.getCompany());
        existing.setPosition(alumni.getPosition());
        existing.setBio(alumni.getBio());
        existing.setSkills(alumni.getSkills());
        existing.setAvailableForMentoring(alumni.getAvailableForMentoring());
        existing.setLinkedin(alumni.getLinkedin());

        return em.merge(existing);
    }
}
