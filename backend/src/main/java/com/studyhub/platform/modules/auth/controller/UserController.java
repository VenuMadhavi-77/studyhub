package com.studyhub.platform.modules.auth.controller;

import com.studyhub.platform.common.dto.ApiResponse;
import com.studyhub.platform.modules.auth.dto.UpdateProfileRequest;
import com.studyhub.platform.modules.auth.dto.UserProfileResponse;
import com.studyhub.platform.modules.auth.service.AuthService;
import com.studyhub.platform.security.SecurityUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/v1/users", "/api/users"})
@Tag(name = "User Identity & Profile", description = "Current authenticated user profile endpoints")
public class UserController {

    private final AuthService authService;

    public UserController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getCurrentUser(
            @AuthenticationPrincipal SecurityUserPrincipal principal
    ) {
        UserProfileResponse profile = authService.getCurrentUserProfile(principal.getId());
        return ResponseEntity.ok(ApiResponse.ok(profile));
    }

    @PutMapping("/me")
    @Operation(summary = "Update current user profile (name, department, semester)")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateCurrentUser(
            @AuthenticationPrincipal SecurityUserPrincipal principal,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        UserProfileResponse updated = authService.updateCurrentUserProfile(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok(updated, "Profile updated successfully"));
    }

    @PutMapping("/me/password")
    @Operation(summary = "Change password for the current authenticated user")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal SecurityUserPrincipal principal,
            @Valid @RequestBody com.studyhub.platform.modules.auth.dto.ChangePasswordRequest request
    ) {
        authService.changePassword(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Password changed successfully"));
    }
}
