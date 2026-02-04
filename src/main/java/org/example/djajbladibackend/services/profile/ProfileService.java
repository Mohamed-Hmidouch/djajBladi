package org.example.djajbladibackend.services.profile;

import org.example.djajbladibackend.dto.user.ChangePasswordRequest;
import org.example.djajbladibackend.dto.user.UpdateProfileRequest;
import org.example.djajbladibackend.dto.user.UserResponse;
import org.example.djajbladibackend.exception.InvalidDataException;
import org.example.djajbladibackend.exception.ResourceNotFoundException;
import org.example.djajbladibackend.models.User;
import org.example.djajbladibackend.repository.auth.UserRepository;
import org.springframework.cache.CacheManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.example.djajbladibackend.config.RedisCacheConfig.CACHE_EMAIL_EXISTS;
import static org.example.djajbladibackend.config.RedisCacheConfig.CACHE_USERS;

@Service
@Transactional(readOnly = true)
public class ProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CacheManager cacheManager;

    public ProfileService(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          CacheManager cacheManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.cacheManager = cacheManager;
    }

    @Transactional
    public UserResponse updateProfile(String email, UpdateProfileRequest req) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        if (req.getFullName() != null && !req.getFullName().trim().isEmpty()) {
            user.setFullName(req.getFullName().trim());
        }
        if (req.getPhoneNumber() != null) {
            user.setPhoneNumber(req.getPhoneNumber().trim().isEmpty() ? null : req.getPhoneNumber().trim());
        }
        if (req.getCity() != null) {
            user.setCity(req.getCity().trim().isEmpty() ? null : req.getCity().trim());
        }

        User saved = userRepository.save(user);
        evictUserCache(email);

        return toResponse(saved);
    }

    @Transactional
    public void changePassword(String email, ChangePasswordRequest req) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPasswordHash())) {
            throw new InvalidDataException("Current password is incorrect");
        }
        if (req.getCurrentPassword().equals(req.getNewPassword())) {
            throw new InvalidDataException("New password must be different from current password");
        }

        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
        evictUserCache(email);
    }

    private void evictUserCache(String email) {
        var usersCache = cacheManager.getCache(CACHE_USERS);
        if (usersCache != null) usersCache.evict(email);
        var emailExistsCache = cacheManager.getCache(CACHE_EMAIL_EXISTS);
        if (emailExistsCache != null) emailExistsCache.evict(email);
    }

    private UserResponse toResponse(User u) {
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
