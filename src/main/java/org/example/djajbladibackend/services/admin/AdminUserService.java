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
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.example.djajbladibackend.config.RedisCacheConfig.CACHE_EMAIL_EXISTS;
import static org.example.djajbladibackend.config.RedisCacheConfig.CACHE_USERS;

@Service
@Transactional(readOnly = true)
public class AdminUserService {

    private final UserRepository userRepository;
    private final UserFactory userFactory;
    private final CacheManager cacheManager;

    public AdminUserService(UserRepository userRepository, UserFactory userFactory, CacheManager cacheManager) {
        this.userRepository = userRepository;
        this.userFactory = userFactory;
        this.cacheManager = cacheManager;
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest req, String adminEmail) {
        userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + adminEmail));

        if (req.getRole() == RoleEnum.Client) {
            throw new InvalidRoleForAdminCreationException(
                    "Client cannot be created via this endpoint. Clients self-register. Use Admin, Ouvrier or Veterinaire.");
        }
        if (req.getRole() != RoleEnum.Admin && req.getRole() != RoleEnum.Ouvrier && req.getRole() != RoleEnum.Veterinaire) {
            throw new InvalidRoleForAdminCreationException(
                    "Invalid role. Use Admin, Ouvrier or Veterinaire. Received: " + req.getRole());
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists");
        }

        String fullName = req.getFirstName() + " " + req.getLastName();
        User user = switch (req.getRole()) {
            case Admin -> userFactory.createAdmin(fullName, req.getEmail(), req.getPassword());
            case Ouvrier -> userFactory.createWorker(fullName, req.getEmail(), req.getPassword());
            case Veterinaire -> userFactory.createVeterinarian(fullName, req.getEmail(), req.getPassword());
            default -> throw new InvalidRoleForAdminCreationException("Unsupported role: " + req.getRole());
        };
        if (req.getPhoneNumber() != null) {
            user.setPhoneNumber(req.getPhoneNumber());
        }

        User saved = userRepository.save(user);

        var usersCache = cacheManager.getCache(CACHE_USERS);
        if (usersCache != null) usersCache.evict(req.getEmail());
        var emailExistsCache = cacheManager.getCache(CACHE_EMAIL_EXISTS);
        if (emailExistsCache != null) emailExistsCache.evict(req.getEmail());

        return toUserResponse(saved);
    }

    /**
     * Returns all users (Admin only). Verifies the caller is a valid user.
     */
    public List<UserResponse> getAllUsers(String adminEmail) {
        userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + adminEmail));

        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        return userRepository.findAll(sort).stream()
                .map(this::toUserResponse)
                .toList();
    }

    private UserResponse toUserResponse(User u) {
        return UserResponse.builder()
                .id(u.getId())
                .fullName(u.getFullName())
                .email(u.getEmail())
                .phoneNumber(u.getPhoneNumber())
                .role(u.getRole())
                .isActive(u.getIsActive())
                .city(u.getCity())
                .createdAt(u.getCreatedAt())
                .updatedAt(u.getUpdatedAt())
                .lastLoginAt(u.getLastLoginAt())
                .build();
    }
}
