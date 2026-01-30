package org.example.djajbladibackend.services.admin;

import org.example.djajbladibackend.dto.admin.CreateUserRequest;
import org.example.djajbladibackend.dto.user.UserResponse;
import org.example.djajbladibackend.exception.EmailAlreadyExistsException;
import org.example.djajbladibackend.exception.InvalidRoleForAdminCreationException;
import org.example.djajbladibackend.exception.ResourceNotFoundException;
import org.example.djajbladibackend.factory.UserFactory;
import org.example.djajbladibackend.models.User;
import org.example.djajbladibackend.models.enums.RoleEnum;
import org.example.djajbladibackend.repository.auth.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminUserService Unit Tests")
class AdminUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserFactory userFactory;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache usersCache;

    @Mock
    private Cache emailExistsCache;

    @InjectMocks
    private AdminUserService adminUserService;

    private User adminUser;
    private User ouvrierUser;
    private CreateUserRequest createRequest;

    @BeforeEach
    void setUp() {
        adminUser = User.builder()
                .id(1L)
                .fullName("Admin User")
                .email("admin@djajbladi.com")
                .role(RoleEnum.Admin)
                .isActive(true)
                .build();

        ouvrierUser = User.builder()
                .id(2L)
                .fullName("Ouvrier One")
                .email("ouvrier@djajbladi.com")
                .phoneNumber("+212600000002")
                .role(RoleEnum.Ouvrier)
                .isActive(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        createRequest = new CreateUserRequest();
        createRequest.setFirstName("Ouvrier");
        createRequest.setLastName("One");
        createRequest.setEmail("ouvrier@djajbladi.com");
        createRequest.setPassword("Ouvrier@123");
        createRequest.setPhoneNumber("+212600000002");
        createRequest.setRole(RoleEnum.Ouvrier);
    }

    @Test
    @DisplayName("✅ createUser should create Ouvrier when admin exists and email available")
    void createUser_Ouvrier_Success() {
        when(userRepository.findByEmail("admin@djajbladi.com")).thenReturn(Optional.of(adminUser));
        when(userRepository.existsByEmail(createRequest.getEmail())).thenReturn(false);
        when(userFactory.createWorker(anyString(), anyString(), anyString())).thenReturn(ouvrierUser);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(cacheManager.getCache(anyString())).thenReturn(usersCache, emailExistsCache);

        UserResponse resp = adminUserService.createUser(createRequest, "admin@djajbladi.com");

        assertNotNull(resp);
        assertEquals(2L, resp.getId());
        assertEquals("Ouvrier One", resp.getFullName());
        assertEquals("ouvrier@djajbladi.com", resp.getEmail());
        assertEquals(RoleEnum.Ouvrier, resp.getRole());
        verify(userFactory, times(1)).createWorker(eq("Ouvrier One"), eq("ouvrier@djajbladi.com"), eq("Ouvrier@123"));
        verify(userRepository, times(1)).save(any(User.class));
        verify(usersCache, times(1)).evict("ouvrier@djajbladi.com");
        verify(emailExistsCache, times(1)).evict("ouvrier@djajbladi.com");
    }

    @Test
    @DisplayName("✅ createUser should create Veterinaire when role is Veterinaire")
    void createUser_Veterinaire_Success() {
        createRequest.setRole(RoleEnum.Veterinaire);
        createRequest.setFirstName("Vet");
        createRequest.setLastName("One");
        createRequest.setEmail("vet@djajbladi.com");
        User vet = User.builder().id(3L).fullName("Vet One").email("vet@djajbladi.com").role(RoleEnum.Veterinaire).isActive(true).createdAt(Instant.now()).updatedAt(Instant.now()).build();

        when(userRepository.findByEmail("admin@djajbladi.com")).thenReturn(Optional.of(adminUser));
        when(userRepository.existsByEmail("vet@djajbladi.com")).thenReturn(false);
        when(userFactory.createVeterinarian(eq("Vet One"), eq("vet@djajbladi.com"), anyString())).thenReturn(vet);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(cacheManager.getCache(anyString())).thenReturn(usersCache, emailExistsCache);

        UserResponse resp = adminUserService.createUser(createRequest, "admin@djajbladi.com");

        assertNotNull(resp);
        assertEquals(RoleEnum.Veterinaire, resp.getRole());
        verify(userFactory, times(1)).createVeterinarian(eq("Vet One"), eq("vet@djajbladi.com"), anyString());
    }

    @Test
    @DisplayName("❌ createUser should throw when admin email not found")
    void createUser_AdminNotFound() {
        when(userRepository.findByEmail("unknown@djajbladi.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                adminUserService.createUser(createRequest, "unknown@djajbladi.com"));
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userFactory, never()).createWorker(anyString(), anyString(), anyString());
        verify(userFactory, never()).createVeterinarian(anyString(), anyString(), anyString());
        verify(userFactory, never()).createAdmin(anyString(), anyString(), anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("✅ createUser should create Admin when role is Admin")
    void createUser_Admin_Success() {
        createRequest.setRole(RoleEnum.Admin);
        createRequest.setFirstName("New");
        createRequest.setLastName("Admin");
        createRequest.setEmail("newadmin@djajbladi.com");
        User newAdmin = User.builder().id(4L).fullName("New Admin").email("newadmin@djajbladi.com").role(RoleEnum.Admin).isActive(true).createdAt(Instant.now()).updatedAt(Instant.now()).build();

        when(userRepository.findByEmail("admin@djajbladi.com")).thenReturn(Optional.of(adminUser));
        when(userRepository.existsByEmail("newadmin@djajbladi.com")).thenReturn(false);
        when(userFactory.createAdmin(eq("New Admin"), eq("newadmin@djajbladi.com"), anyString())).thenReturn(newAdmin);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(cacheManager.getCache(anyString())).thenReturn(usersCache, emailExistsCache);

        UserResponse resp = adminUserService.createUser(createRequest, "admin@djajbladi.com");

        assertNotNull(resp);
        assertEquals(RoleEnum.Admin, resp.getRole());
        assertEquals("New Admin", resp.getFullName());
        verify(userFactory, times(1)).createAdmin(eq("New Admin"), eq("newadmin@djajbladi.com"), anyString());
    }

    @Test
    @DisplayName("❌ createUser should throw when role is Client")
    void createUser_RoleClient_Throws() {
        createRequest.setRole(RoleEnum.Client);
        when(userRepository.findByEmail("admin@djajbladi.com")).thenReturn(Optional.of(adminUser));

        InvalidRoleForAdminCreationException ex = assertThrows(InvalidRoleForAdminCreationException.class, () ->
                adminUserService.createUser(createRequest, "admin@djajbladi.com"));
        assertTrue(ex.getMessage().contains("Client"));
        assertTrue(ex.getMessage().contains("self-register"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("❌ createUser should throw when email already exists")
    void createUser_EmailExists_Throws() {
        when(userRepository.findByEmail("admin@djajbladi.com")).thenReturn(Optional.of(adminUser));
        when(userRepository.existsByEmail(createRequest.getEmail())).thenReturn(true);

        EmailAlreadyExistsException ex = assertThrows(EmailAlreadyExistsException.class, () ->
                adminUserService.createUser(createRequest, "admin@djajbladi.com"));
        assertEquals("Email already exists", ex.getMessage());
        verify(userFactory, never()).createWorker(anyString(), anyString(), anyString());
        verify(userRepository, never()).save(any(User.class));
    }
}
