package org.example.djajbladibackend.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * ✅ Security Best Practice: Role n'est PAS exposé en clair
 * Le rôle est uniquement encodé dans le JWT token (claims)
 * Le frontend doit décoder le JWT pour obtenir le rôle
 */
@Data
@AllArgsConstructor
public class JwtResponse {
    private String token;
    private String refreshToken;
    private String type = "Bearer";
    private String email;

    public JwtResponse(String token, String refreshToken, String email) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.email = email;
    }
}
