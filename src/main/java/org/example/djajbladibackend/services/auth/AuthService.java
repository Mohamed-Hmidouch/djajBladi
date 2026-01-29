package org.example.djajbladibackend.services.auth;

import org.example.djajbladibackend.dto.auth.JwtResponse;
import org.example.djajbladibackend.dto.auth.LoginRequest;
import org.example.djajbladibackend.dto.auth.RegisterRequest;
import org.example.djajbladibackend.dto.user.UserResponse;
import org.example.djajbladibackend.exception.EmailAlreadyExistsException;
import org.example.djajbladibackend.exception.RegistrationNotAllowedException;
import org.example.djajbladibackend.factory.UserFactory;
import org.example.djajbladibackend.models.User;
import org.example.djajbladibackend.models.enums.RoleEnum;
import org.example.djajbladibackend.repository.auth.UserRepository;
import org.example.djajbladibackend.security.JwtUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.example.djajbladibackend.config.RedisCacheConfig.CACHE_EMAIL_EXISTS;
import static org.example.djajbladibackend.config.RedisCacheConfig.CACHE_USERS;

/**
 * Spring Boot Best Practice: Service avec @Transactional
 * Service d'authentification (login, register)
 */
@Service
@Transactional(readOnly = true)
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final UserFactory userFactory;

    public AuthService(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            JwtUtils jwtUtils,
            UserFactory userFactory
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtUtils = jwtUtils;
        this.userFactory = userFactory;
    }

    /**
     * Spring Boot Best Practice: Méthode readOnly pour login
     * Authentifie un utilisateur et génère les tokens JWT
     */
    public JwtResponse login(LoginRequest loginRequest) {
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

        return new JwtResponse(
                jwt,
                refreshToken,
                user.getEmail(),
                user.getRole().name()
        );
    }

    /**
     * Spring Boot Best Practice: @Transactional pour opération d'écriture
     * Enregistre un nouvel utilisateur. Evicts cache for new email.
     */
    @Transactional
    @CacheEvict(cacheNames = { CACHE_USERS, CACHE_EMAIL_EXISTS }, key = "#registerRequest.email")
    public UserResponse register(RegisterRequest registerRequest) {
        RoleEnum role = registerRequest.getRole() != null ? registerRequest.getRole() : RoleEnum.Client;
        if (role != RoleEnum.Client) {
            throw new RegistrationNotAllowedException(
                "Only Client role can self-register. Admin, Ouvrier and Veterinaire are created by an admin."
            );
        }
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists");
        }

        // ✅ Utiliser UserFactory pour créer l'utilisateur (always Client)
        User user = userFactory.createUser(
                registerRequest.getFirstName() + " " + registerRequest.getLastName(),
                registerRequest.getEmail(),
                registerRequest.getPassword(),
                role
        );

        if (registerRequest.getPhoneNumber() != null) {
            user.setPhoneNumber(registerRequest.getPhoneNumber());
        }

        User savedUser = userRepository.save(user);

        // ✅ Builder pattern pour UserResponse
        return UserResponse.builder()
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
    }

    /**
     * Vérifie si un email existe déjà (cached for optimisation).
     */
    @Cacheable(cacheNames = CACHE_EMAIL_EXISTS, key = "#email")
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Récupère un utilisateur par email (cached: first request hits DB, next hit Redis).
     */
    @Cacheable(cacheNames = CACHE_USERS, key = "#email")
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    /**
     * Returns current user profile (uses cached getUserByEmail).
     */
    public UserResponse getCurrentUserProfile(String email) {
        User user = getUserByEmail(email);
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .city(user.getCity())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }
}
