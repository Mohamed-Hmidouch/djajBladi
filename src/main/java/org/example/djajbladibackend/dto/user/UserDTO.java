package org.example.djajbladibackend.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.example.djajbladibackend.models.enums.RoleEnum;

/**
 * DTO pour créer/mettre à jour un utilisateur
 * ✅ Spring Boot Best Practice: Validation avec annotations Jakarta
 */
@Data
public class UserDTO {
    
    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;
    
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phoneNumber;
    
    private RoleEnum role;
    
    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;
    
    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;
    
    @Size(max = 10, message = "Postal code must not exceed 10 characters")
    private String postalCode;
    
    @Size(max = 50, message = "License number must not exceed 50 characters")
    private String licenseNumber;
    
    @Size(max = 200, message = "Company name must not exceed 200 characters")
    private String companyName;
}
