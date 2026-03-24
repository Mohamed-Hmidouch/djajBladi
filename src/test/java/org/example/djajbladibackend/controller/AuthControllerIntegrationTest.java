package org.example.djajbladibackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.djajbladibackend.dto.auth.LoginRequest;
import org.example.djajbladibackend.dto.auth.RegisterRequest;
import org.example.djajbladibackend.models.enums.RoleEnum;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Tests for AuthController.
 * Uses @AutoConfigureMockMvc so requests execute within the test transaction,
 * and @Transactional rolls back after each test for full isolation.
 */
@SpringBootTest(properties = "test.context.id=auth-integration")
@AutoConfigureMockMvc
@ActiveProfiles("ci")
@Sql(
    statements = "TRUNCATE TABLE users CASCADE",
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
    config = @SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED)
)
@DisplayName("AuthController Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntityManager entityManager;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String AUTH_BASE_URL = "/api/auth";

    @BeforeEach
    void clearCaches() {
        entityManager.getEntityManagerFactory().getCache().evictAll();
    }

    @Test
    @Order(1)
    @DisplayName("POST /api/auth/register with role Admin should return 400 (registration not allowed)")
    void testRegister_Admin_Rejected_BadRequest() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Admin");
        request.setLastName("User");
        request.setEmail("admin@djajbladi.com");
        request.setPassword("Admin@123");
        request.setPhoneNumber("+212600000001");
        request.setRole(RoleEnum.Admin);

        mockMvc.perform(post(AUTH_BASE_URL + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(2)
    @DisplayName("POST /api/auth/register with role Ouvrier should return 400 (only Client can self-register)")
    void testRegister_Ouvrier_Rejected_BadRequest() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Ouvrier");
        request.setLastName("Worker");
        request.setEmail("ouvrier@djajbladi.com");
        request.setPassword("Worker@123");
        request.setPhoneNumber("+212600000002");
        request.setRole(RoleEnum.Ouvrier);

        mockMvc.perform(post(AUTH_BASE_URL + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(3)
    @DisplayName("POST /api/auth/register should create Client user")
    void testRegister_Client_Success() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Client");
        request.setLastName("User");
        request.setEmail("client@djajbladi.com");
        request.setPassword("Client@123");
        request.setPhoneNumber("+212600000003");
        request.setRole(RoleEnum.Client);

        mockMvc.perform(post(AUTH_BASE_URL + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("client@djajbladi.com"))
                .andExpect(jsonPath("$.role").value("Client"));
    }

    @Test
    @Order(4)
    @DisplayName("POST /api/auth/register should fail with duplicate email")
    void testRegister_DuplicateEmail_BadRequest() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("First");
        request.setLastName("User");
        request.setEmail("dup-test-" + System.currentTimeMillis() + "@djajbladi.com");
        request.setPassword("Test@12345");
        request.setRole(RoleEnum.Client);

        // First registration - should succeed
        mockMvc.perform(post(AUTH_BASE_URL + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Second registration with same email - should fail (400 or 409)
        mockMvc.perform(post(AUTH_BASE_URL + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @Order(5)
    @DisplayName("POST /api/auth/login should return JWT tokens")
    void testLogin_Success() throws Exception {
        // First register a user (Client; Admin cannot be registered via API)
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setFirstName("Login");
        registerRequest.setLastName("Test");
        registerRequest.setEmail("login@djajbladi.com");
        registerRequest.setPassword("Login@123");
        registerRequest.setRole(RoleEnum.Client);

        mockMvc.perform(post(AUTH_BASE_URL + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Then login
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("login@djajbladi.com");
        loginRequest.setPassword("Login@123");

        mockMvc.perform(post(AUTH_BASE_URL + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.email").value("login@djajbladi.com"));
    }

    @Test
    @Order(6)
    @DisplayName("POST /api/auth/login should fail with wrong password")
    void testLogin_WrongPassword_Unauthorized() throws Exception {
        // First register a user
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setFirstName("Wrong");
        registerRequest.setLastName("Password");
        registerRequest.setEmail("wrong@djajbladi.com");
        registerRequest.setPassword("Correct@123");
        registerRequest.setRole(RoleEnum.Client);

        mockMvc.perform(post(AUTH_BASE_URL + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Try login with wrong password
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("wrong@djajbladi.com");
        loginRequest.setPassword("WrongPassword@123");

        mockMvc.perform(post(AUTH_BASE_URL + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(7)
    @DisplayName("POST /api/auth/login should fail with non-existent user")
    void testLogin_NonExistentUser_Unauthorized() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("nonexistent@djajbladi.com");
        loginRequest.setPassword("Password@123");

        mockMvc.perform(post(AUTH_BASE_URL + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(8)
    @DisplayName("POST /api/auth/register should fail with invalid email format")
    void testRegister_InvalidEmail_BadRequest() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Invalid");
        request.setLastName("Email");
        request.setEmail("invalid-email");
        request.setPassword("Test@123");
        request.setRole(RoleEnum.Client);

        mockMvc.perform(post(AUTH_BASE_URL + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
