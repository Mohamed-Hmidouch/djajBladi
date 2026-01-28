package org.example.djajbladibackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.djajbladibackend.dto.auth.LoginRequest;
import org.example.djajbladibackend.dto.auth.RegisterRequest;
import org.example.djajbladibackend.models.enums.RoleEnum;
import org.example.djajbladibackend.repository.auth.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ✅ Spring Boot Best Practice: Integration Tests for AuthController
 * Using MockMvc with @SpringBootTest for full context integration testing
 * This approach is preferred over TestRestTemplate as it:
 * - Doesn't require a real HTTP server
 * - Provides better assertions and matchers
 * - Gives access to the actual Spring context
 */
@SpringBootTest
@Transactional
@DisplayName("AuthController Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthControllerIntegrationTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Using existing Docker Compose PostgreSQL
        registry.add("spring.datasource.url", () -> "jdbc:postgresql://localhost:5432/djaj_bladi");
        registry.add("spring.datasource.username", () -> "postgres");
        registry.add("spring.datasource.password", () -> "postgres");
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    private static final String AUTH_BASE_URL = "/api/auth";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
        userRepository.deleteAll();
    }

    @Test
    @Order(1)
    @DisplayName("✅ POST /api/auth/register should create Admin user")
    void testRegister_Admin_Success() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Admin");
        request.setLastName("User");
        request.setEmail("admin@djajbladi.com");
        request.setPassword("Admin@123");
        request.setPhoneNumber("+212600000001");
        request.setRole(RoleEnum.Admin);

        MvcResult result = mockMvc.perform(post(AUTH_BASE_URL + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("admin@djajbladi.com"))
                .andExpect(jsonPath("$.role").value("Admin"))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody).contains("Admin User");
    }

    @Test
    @Order(2)
    @DisplayName("✅ POST /api/auth/register should create Ouvrier user")
    void testRegister_Worker_Success() throws Exception {
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
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("ouvrier@djajbladi.com"));
    }

    @Test
    @Order(3)
    @DisplayName("❌ POST /api/auth/register should fail with duplicate email")
    void testRegister_DuplicateEmail_BadRequest() throws Exception {
        RegisterRequest request1 = new RegisterRequest();
        request1.setFirstName("First");
        request1.setLastName("User");
        request1.setEmail("duplicate@djajbladi.com");
        request1.setPassword("Test@123");
        request1.setRole(RoleEnum.Client);

        // First registration - should succeed
        mockMvc.perform(post(AUTH_BASE_URL + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        // Second registration with same email - should fail
        RegisterRequest request2 = new RegisterRequest();
        request2.setFirstName("Second");
        request2.setLastName("User");
        request2.setEmail("duplicate@djajbladi.com");
        request2.setPassword("Test@456");
        request2.setRole(RoleEnum.Client);

        mockMvc.perform(post(AUTH_BASE_URL + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(4)
    @DisplayName("✅ POST /api/auth/login should return JWT tokens")
    void testLogin_Success() throws Exception {
        // First register a user
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setFirstName("Login");
        registerRequest.setLastName("Test");
        registerRequest.setEmail("login@djajbladi.com");
        registerRequest.setPassword("Login@123");
        registerRequest.setRole(RoleEnum.Admin);

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
                .andExpect(jsonPath("$.email").value("login@djajbladi.com"))
                .andExpect(jsonPath("$.role").value("Admin"));
    }

    @Test
    @Order(5)
    @DisplayName("❌ POST /api/auth/login should fail with wrong password")
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
    @Order(6)
    @DisplayName("❌ POST /api/auth/login should fail with non-existent user")
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
    @Order(7)
    @DisplayName("❌ POST /api/auth/register should fail with invalid email format")
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

// ✅ Best Practices Applied:
// - Using MockMvc instead of TestRestTemplate (recommended for Spring Boot 4.x)
// - @Transactional for test isolation and automatic rollback
// - @AutoConfigureMockMvc for full MVC testing without HTTP server
// - JSONPath assertions for response validation
// - Clean test data setup with @BeforeEach
// - Descriptive test names with @DisplayName
// - Ordered test execution with @TestMethodOrder
