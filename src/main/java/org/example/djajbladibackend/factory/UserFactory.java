package org.example.djajbladibackend.factory;

import org.example.djajbladibackend.models.User;
import org.example.djajbladibackend.models.enums.RoleEnum;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Factory pour créer des utilisateurs avec différents rôles
 *  Spring Boot Best Practice: Factory pattern pour création d'objets complexes
 */
@Component
public class UserFactory {

    private final PasswordEncoder passwordEncoder;

    public UserFactory(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Crée un Admin
     */
    public User createAdmin(String fullName, String email, String password) {
        return createUser(fullName, email, password, RoleEnum.Admin);
    }

    /**
     * Crée un Ouvrier
     */
    public User createWorker(String fullName, String email, String password) {
        return createUser(fullName, email, password, RoleEnum.Ouvrier);
    }

    /**
     * Crée un Vétérinaire
     */
    public User createVeterinarian(String fullName, String email, String password) {
        return createUser(fullName, email, password, RoleEnum.Veterinaire);
    }

    /**
     * Crée un Client
     */
    public User createClient(String fullName, String email, String password) {
        return createUser(fullName, email, password, RoleEnum.Client);
    }

    /**
     * Crée un utilisateur de base avec role
     */
    public User createUser(String fullName, String email, String password, RoleEnum role) {
        return User.builder()
                .fullName(fullName)
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .role(role)
                .isActive(true)
                .build();
    }

    /**
     * Crée un utilisateur avec toutes les informations
     */
    public User createUserWithDetails(
            String fullName,
            String email,
            String password,
            String phoneNumber,
            RoleEnum role,
            String city
    ) {
        return User.builder()
                .fullName(fullName)
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .phoneNumber(phoneNumber)
                .role(role)
                .city(city)
                .isActive(true)
                .build();
    }
}