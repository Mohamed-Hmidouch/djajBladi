package org.example.djajbladibackend.dto.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.example.djajbladibackend.models.enums.RoleEnum;

/**
 * Request body for admin creating Admin, Ouvrier or Veterinaire users.
 * Client cannot be created via this endpoint (self-register only).
 */
@Data
public class CreateUserRequest {

    @NotBlank(message = "First name is required")
    @Size(max = 100)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100)
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 255)
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    private String phoneNumber;

    @NotNull(message = "Role is required")
    private RoleEnum role;
}
