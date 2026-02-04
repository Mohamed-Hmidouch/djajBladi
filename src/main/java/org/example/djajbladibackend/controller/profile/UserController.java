package org.example.djajbladibackend.controller.profile;

import jakarta.validation.Valid;
import org.example.djajbladibackend.dto.user.ChangePasswordRequest;
import org.example.djajbladibackend.dto.user.UpdateProfileRequest;
import org.example.djajbladibackend.dto.user.UserResponse;
import org.example.djajbladibackend.services.auth.AuthService;
import org.example.djajbladibackend.services.profile.ProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * ✅ Security Best Practice: @PreAuthorize pour tout utilisateur authentifié
 */
@RestController
@RequestMapping(value = { "/api/users", "/api/dashboard/users" })
@PreAuthorize("isAuthenticated()")
public class UserController {

    private final AuthService authService;
    private final ProfileService profileService;

    public UserController(AuthService authService, ProfileService profileService) {
        this.authService = authService;
        this.profileService = profileService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(authService.getCurrentUserProfile(userDetails.getUsername()));
    }

    @PatchMapping("/me")
    public ResponseEntity<UserResponse> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(profileService.updateProfile(userDetails.getUsername(), request));
    }

    @PostMapping("/me/password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        profileService.changePassword(userDetails.getUsername(), request);
        return ResponseEntity.noContent().build();
    }
}
