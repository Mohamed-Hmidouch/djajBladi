package org.example.djajbladibackend.dto.user;

import lombok.Builder;
import lombok.Data;
import org.example.djajbladibackend.models.enums.RoleEnum;

import java.time.Instant;

/**
 * DTO pour retourner les informations utilisateur (sans password)
 * ✅ Spring Boot Best Practice: DTO séparé avec Builder
 */
@Data
@Builder
public class UserResponse {
    private Long id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private RoleEnum role;
    private Boolean isActive;
    private String address;
    private String city;
    private String postalCode;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant lastLoginAt;
}
