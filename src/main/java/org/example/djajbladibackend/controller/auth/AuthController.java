package org.example.djajbladibackend.controller.auth;

import jakarta.validation.Valid;
import org.example.djajbladibackend.dto.auth.JwtResponse;
import org.example.djajbladibackend.dto.auth.LoginRequest;
import org.example.djajbladibackend.dto.auth.RegisterRequest;
import org.example.djajbladibackend.dto.user.UserResponse;
import org.example.djajbladibackend.services.auth.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller pour l'authentification (login, register)
 * ✅ Spring Boot Best Practice: @RestController avec Service layer
 * ✅ CORS géré globalement dans SecurityConfig
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        JwtResponse jwtResponse = authService.login(loginRequest);
        return ResponseEntity.ok(jwtResponse);
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        
        if (authService.emailExists(registerRequest.getEmail())) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .build();
        }

        UserResponse userResponse = authService.register(registerRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userResponse);
    }
}
