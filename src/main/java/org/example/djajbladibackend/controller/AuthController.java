package org.example.djajbladibackend.controller;

import jakarta.validation.Valid;
import org.example.djajbladibackend.dto.auth.JwtResponse;
import org.example.djajbladibackend.dto.auth.LoginRequest;
import org.example.djajbladibackend.dto.auth.RegisterRequest;
import org.example.djajbladibackend.dto.user.UserResponse;
import org.example.djajbladibackend.factory.UserFactory;
import org.example.djajbladibackend.models.User;
import org.example.djajbladibackend.repository.auth.UserRepository;
import org.example.djajbladibackend.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Controller pour l'authentification (login, register)
 * ✅ Spring Boot Best Practice: @RestController avec Factory pattern
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserFactory userFactory;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        String jwt = jwtUtils.generateJwtToken(authentication);
        String refreshToken = jwtUtils.generateRefreshToken(loginRequest.getEmail());

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(new JwtResponse(
                jwt,
                refreshToken,
                user.getEmail(),
                user.getRole().name()
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .build();
        }

        // ✅ Utiliser UserFactory pour créer l'utilisateur
        User user = userFactory.createUser(
                registerRequest.getFirstName() + " " + registerRequest.getLastName(),
                registerRequest.getEmail(),
                registerRequest.getPassword(),
                registerRequest.getRole()
        );
        
        if (registerRequest.getPhoneNumber() != null) {
            user.setPhoneNumber(registerRequest.getPhoneNumber());
        }

        User savedUser = userRepository.save(user);

        // ✅ Builder pattern pour UserResponse
        UserResponse userResponse = UserResponse.builder()
                .id(savedUser.getId())
                .fullName(savedUser.getFullName())
                .email(savedUser.getEmail())
                .phoneNumber(savedUser.getPhoneNumber())
                .role(savedUser.getRole())
                .isActive(savedUser.getIsActive())
                .city(savedUser.getCity())
                .createdAt(savedUser.getCreatedAt())
                .updatedAt(savedUser.getUpdatedAt())
                .build();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userResponse);
    }
}
