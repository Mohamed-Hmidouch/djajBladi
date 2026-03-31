package org.example.djajbladibackend.services.admin;

import org.example.djajbladibackend.dto.admin.AdminChangePasswordRequest;
import org.example.djajbladibackend.dto.admin.CreateUserRequest;
import org.example.djajbladibackend.dto.common.PageResponse;
import org.example.djajbladibackend.dto.user.UserResponse;
import org.example.djajbladibackend.exception.EmailAlreadyExistsException;
import org.example.djajbladibackend.exception.InvalidRoleForAdminCreationException;
import org.example.djajbladibackend.exception.ResourceNotFoundException;
import org.example.djajbladibackend.factory.UserFactory;
import org.example.djajbladibackend.models.User;
import org.example.djajbladibackend.models.enums.RoleEnum;
import org.example.djajbladibackend.repository.auth.UserRepository;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.example.djajbladibackend.config.RedisCacheConfig.CACHE_EMAIL_EXISTS;
import static org.example.djajbladibackend.config.RedisCacheConfig.CACHE_USERS;

@Service
@Transactional(readOnly = true)
public class AdminUserService {

    private final UserRepository userRepository;
    private final UserFactory userFactory;
    private final CacheManager cacheManager;
    private final PasswordEncoder passwordEncoder;

    public AdminUserService(UserRepository userRepository, UserFactory userFactory,
                            CacheManager cacheManager, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userFactory = userFactory;
        this.cacheManager = cacheManager;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest req, String adminEmail) {
        userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable avec l'email : " + adminEmail));

        if (req.getRole() == RoleEnum.Client) {
            throw new InvalidRoleForAdminCreationException(
                    "Le rôle Client ne peut pas être créé via cette interface. Les clients s'inscrivent eux-mêmes.");
        }
        if (req.getRole() != RoleEnum.Admin && req.getRole() != RoleEnum.Ouvrier && req.getRole() != RoleEnum.Veterinaire) {
            throw new InvalidRoleForAdminCreationException(
                    "Rôle invalide. Utilisez Admin, Ouvrier ou Vétérinaire. Reçu : " + req.getRole());
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new EmailAlreadyExistsException("Un compte avec cette adresse email existe déjà.");
        }

        String fullName = req.getFirstName() + " " + req.getLastName();
        User user = switch (req.getRole()) {
            case Admin -> userFactory.createAdmin(fullName, req.getEmail(), req.getPassword());
            case Ouvrier -> userFactory.createWorker(fullName, req.getEmail(), req.getPassword());
            case Veterinaire -> userFactory.createVeterinarian(fullName, req.getEmail(), req.getPassword());
            default -> throw new InvalidRoleForAdminCreationException("Rôle non supporté : " + req.getRole());
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

    @Transactional
    public void adminForceChangePassword(Long targetUserId, AdminChangePasswordRequest req, String adminEmail) {
        userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Administrateur introuvable : " + adminEmail));

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable avec l'id : " + targetUserId));

        target.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(target);

        var usersCache = cacheManager.getCache(CACHE_USERS);
        if (usersCache != null) usersCache.evict(target.getEmail());
    }

    public PageResponse<UserResponse> getAllUsers(String adminEmail, int page, int size) {
        userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable avec l'email : " + adminEmail));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<UserResponse> result = userRepository.findAll(pageable)
                .map(this::toUserResponse);
        return PageResponse.from(result);
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
