package com.hlayiseko.AlumniMentoring.security;


import com.hlayiseko.AlumniMentoring.entity.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.NoResultException;
import jakarta.security.enterprise.credential.Credential;
import jakarta.security.enterprise.credential.UsernamePasswordCredential;
import jakarta.security.enterprise.identitystore.CredentialValidationResult;
import jakarta.security.enterprise.identitystore.IdentityStore;
import jakarta.security.enterprise.identitystore.Pbkdf2PasswordHash;
import java.util.*;

import static jakarta.security.enterprise.identitystore.CredentialValidationResult.INVALID_RESULT;

@ApplicationScoped
public class DatabaseIdentityStore implements IdentityStore {

    @PersistenceContext(unitName = "AlumniPU")
    private EntityManager em;

    @Inject
    private Pbkdf2PasswordHash passwordHash;

    @Override
    public CredentialValidationResult validate(Credential credential) {
        if (credential instanceof UsernamePasswordCredential) {
            UsernamePasswordCredential usernamePassword =
                    (UsernamePasswordCredential) credential;

            try {
                User user = em.createQuery(
                                "SELECT u FROM User u WHERE u.email = :email", User.class)
                        .setParameter("email", usernamePassword.getCaller())
                        .getSingleResult();

                if (passwordHash.verify(
                        usernamePassword.getPassword().getValue(),
                        user.getPassword())) {

                    Set<String> roles = new HashSet<>();
                    roles.add(user.getRole().name());

                    return new CredentialValidationResult(
                            user.getEmail(), roles);
                }
            } catch (NoResultException e) {
                return INVALID_RESULT;
            } catch (Exception e) {
                System.err.println("Error in DatabaseIdentityStore: " + e.getMessage());
                return INVALID_RESULT;
            }
        }
        return INVALID_RESULT;
    }
}
