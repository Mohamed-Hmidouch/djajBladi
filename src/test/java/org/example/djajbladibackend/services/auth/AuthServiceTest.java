package org.example.djajbladibackend.services.auth;

import org.example.djajbladibackend.dto.auth.JwtResponse;
import org.example.djajbladibackend.dto.auth.LoginRequest;
import org.example.djajbladibackend.dto.auth.RegisterRequest;
import org.example.djajbladibackend.dto.user.UserResponse;
import org.example.djajbladibackend.factory.UserFactory;
import org.example.djajbladibackend.models.enums.RoleEnum;
import org.example.djajbladibackend.models.User;
import org.example.djajbladibackend.repository.auth.UserRepository;
import org.example.djajbladibackend.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * ✅ Spring Boot Best Practice & Agent-MD: Tests unitaires pour AuthService
 * Tests avec Mockito pour isoler la logique métier
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UserFactory userFactory;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        // Setup test data
        testUser = User.builder()
                .id(1L)
                .fullName("Test User")
                .email("test@djajbladi.com")
                .passwordHash("$2a$10$hashedPassword")
                .phoneNumber("+212600000001")
                .role(RoleEnum.Admin)
                .isActive(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        registerRequest = new RegisterRequest();
        registerRequest.setFirstName("Test");
        registerRequest.setLastName("User");
        registerRequest.setEmail("test@djajbladi.com");
        registerRequest.setPassword("Test@123");
        registerRequest.setPhoneNumber("+212600000001");
        registerRequest.setRole(RoleEnum.Admin);

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@djajbladi.com");
        loginRequest.setPassword("Test@123");
    }

    @Test
    @DisplayName("✅ Login should return JWT tokens when credentials are valid")
    void testLogin_Success() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtils.generateJwtToken(authentication)).thenReturn("jwt-access-token");
        when(jwtUtils.generateRefreshToken(anyString())).thenReturn("jwt-refresh-token");
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));

        // When
        JwtResponse response = authService.login(loginRequest);

        // Then
        assertNotNull(response);
        assertEquals("jwt-access-token", response.getToken());
        assertEquals("jwt-refresh-token", response.getRefreshToken());
        assertEquals("test@djajbladi.com", response.getEmail());
        assertEquals("Admin", response.getRole());

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtils, times(1)).generateJwtToken(authentication);
        verify(jwtUtils, times(1)).generateRefreshToken(loginRequest.getEmail());
        verify(userRepository, times(1)).findByEmail(loginRequest.getEmail());
    }

    @Test
    @DisplayName("❌ Login should throw exception when user not found")
    void testLogin_UserNotFound() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("User not found", exception.getMessage());
        verify(userRepository, times(1)).findByEmail(loginRequest.getEmail());
    }

    @Test
    @DisplayName("✅ Register should create new user when email doesn't exist")
    void testRegister_Success() {
        // Given
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(userFactory.createUser(anyString(), anyString(), anyString(), any(RoleEnum.class)))
                .thenReturn(testUser);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserResponse response = authService.register(registerRequest);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Test User", response.getFullName());
        assertEquals("test@djajbladi.com", response.getEmail());
        assertEquals("+212600000001", response.getPhoneNumber());
        assertEquals(RoleEnum.Admin, response.getRole());
        assertTrue(response.getIsActive());

        verify(userRepository, times(1)).existsByEmail(registerRequest.getEmail());
        verify(userFactory, times(1)).createUser(anyString(), anyString(), anyString(), any(RoleEnum.class));
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("❌ Register should throw exception when email already exists")
    void testRegister_EmailExists() {
        // Given
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.register(registerRequest);
        });

        assertEquals("Email already exists", exception.getMessage());
        verify(userRepository, times(1)).existsByEmail(registerRequest.getEmail());
        verify(userFactory, never()).createUser(anyString(), anyString(), anyString(), any(RoleEnum.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("✅ emailExists should return true when email exists")
    void testEmailExists_True() {
        // Given
        when(userRepository.existsByEmail("test@djajbladi.com")).thenReturn(true);

        // When
        boolean exists = authService.emailExists("test@djajbladi.com");

        // Then
        assertTrue(exists);
        verify(userRepository, times(1)).existsByEmail("test@djajbladi.com");
    }

    @Test
    @DisplayName("✅ emailExists should return false when email doesn't exist")
    void testEmailExists_False() {
        // Given
        when(userRepository.existsByEmail("nonexistent@djajbladi.com")).thenReturn(false);

        // When
        boolean exists = authService.emailExists("nonexistent@djajbladi.com");

        // Then
        assertFalse(exists);
        verify(userRepository, times(1)).existsByEmail("nonexistent@djajbladi.com");
    }

    @Test
    @DisplayName("✅ getUserByEmail should return user when email exists")
    void testGetUserByEmail_Success() {
        // Given
        when(userRepository.findByEmail("test@djajbladi.com")).thenReturn(Optional.of(testUser));

        // When
        User user = authService.getUserByEmail("test@djajbladi.com");

        // Then
        assertNotNull(user);
        assertEquals("test@djajbladi.com", user.getEmail());
        assertEquals("Test User", user.getFullName());
        verify(userRepository, times(1)).findByEmail("test@djajbladi.com");
    }

    @Test
    @DisplayName("❌ getUserByEmail should throw exception when user not found")
    void testGetUserByEmail_NotFound() {
        // Given
        when(userRepository.findByEmail("nonexistent@djajbladi.com")).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.getUserByEmail("nonexistent@djajbladi.com");
        });

        assertTrue(exception.getMessage().contains("User not found with email"));
        verify(userRepository, times(1)).findByEmail("nonexistent@djajbladi.com");
    }
}

// ✅ Conforme aux bonnes pratiques Spring Boot & Agent-MD
