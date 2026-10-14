package com.studyhub.platform.modules.auth.service;

import com.studyhub.platform.common.enums.Role;
import com.studyhub.platform.common.enums.UserStatus;
import com.studyhub.platform.common.exception.BadRequestException;
import com.studyhub.platform.common.exception.ResourceNotFoundException;
import com.studyhub.platform.common.exception.UnauthorizedException;
import com.studyhub.platform.config.ApplicationProperties;
import com.studyhub.platform.modules.auth.dto.*;
import com.studyhub.platform.modules.auth.entity.User;
import com.studyhub.platform.modules.auth.repository.UserRepository;
import com.studyhub.platform.modules.taxonomy.entity.Department;
import com.studyhub.platform.modules.taxonomy.repository.DepartmentRepository;
import com.studyhub.platform.security.JwtTokenProvider;
import com.studyhub.platform.security.SecurityUserPrincipal;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final ApplicationProperties applicationProperties;

    public AuthService(UserRepository userRepository,
                       DepartmentRepository departmentRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider jwtTokenProvider,
                       RefreshTokenService refreshTokenService,
                       ApplicationProperties applicationProperties) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenService = refreshTokenService;
        this.applicationProperties = applicationProperties;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email().toLowerCase().trim())) {
            throw new BadRequestException("An account with this email address already exists");
        }

        User user = new User();
        user.setEmail(request.email().toLowerCase().trim());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName().trim());
        user.setRole(Role.STUDENT);
        user.setStatus(UserStatus.ACTIVE);
        user.setSemester(request.semester());

        if (request.departmentId() != null) {
            Department dept = departmentRepository.findById(request.departmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.departmentId()));
            user.setDepartment(dept);
        }

        User savedUser = userRepository.save(user);

        SecurityUserPrincipal principal = SecurityUserPrincipal.create(savedUser);
        String accessToken = jwtTokenProvider.generateAccessToken(principal);
        String rawRefreshToken = refreshTokenService.createRefreshToken(savedUser);

        return AuthResponse.of(
                accessToken,
                rawRefreshToken,
                applicationProperties.getJwt().getAccessExpirationMs(),
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getFullName(),
                savedUser.getRole(),
                mapToUserProfile(savedUser)
        );
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email().toLowerCase().trim(),
                        request.password()
                )
        );

        SecurityUserPrincipal principal = (SecurityUserPrincipal) authentication.getPrincipal();

        if (principal.getStatus() == UserStatus.SUSPENDED) {
            throw new UnauthorizedException("Your account has been suspended by an administrator");
        }

        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", principal.getId()));

        String accessToken = jwtTokenProvider.generateAccessToken(principal);
        String rawRefreshToken = refreshTokenService.createRefreshToken(user);

        return AuthResponse.of(
                accessToken,
                rawRefreshToken,
                applicationProperties.getJwt().getAccessExpirationMs(),
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                mapToUserProfile(user)
        );
    }

    @Transactional
    public AuthResponse refreshToken(TokenRefreshRequest request) {
        RefreshTokenService.RotateTokenResult result = refreshTokenService.rotateRefreshToken(request.refreshToken());
        User user = result.user();

        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new UnauthorizedException("Account is suspended");
        }

        SecurityUserPrincipal principal = SecurityUserPrincipal.create(user);
        String newAccessToken = jwtTokenProvider.generateAccessToken(principal);

        return AuthResponse.of(
                newAccessToken,
                result.newRefreshToken(),
                applicationProperties.getJwt().getAccessExpirationMs(),
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                mapToUserProfile(user)
        );
    }

    @Transactional
    public void logout(TokenRefreshRequest request) {
        if (request != null && request.refreshToken() != null) {
            refreshTokenService.revokeRefreshToken(request.refreshToken());
        }
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        return mapToUserProfile(user);
    }

    private UserProfileResponse mapToUserProfile(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.getStatus(),
                user.getDepartment() != null ? user.getDepartment().getId() : null,
                user.getDepartment() != null ? user.getDepartment().getName() : null,
                user.getSemester(),
                user.getGoals(),
                user.getPreferredLanguage(),
                user.getLevel(),
                user.getCreatedAt()
        );
    }

    @Transactional
    public UserProfileResponse updateCurrentUserProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setFullName(request.fullName().trim());
        user.setSemester(request.semester());
        if (request.goals() != null) {
            user.setGoals(request.goals().trim());
        }
        if (request.preferredLanguage() != null) {
            user.setPreferredLanguage(request.preferredLanguage());
        }
        if (request.level() != null) {
            user.setLevel(request.level());
        }

        if (request.departmentId() != null) {
            Department dept = departmentRepository.findById(request.departmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.departmentId()));
            user.setDepartment(dept);
        } else {
            user.setDepartment(null);
        }

        User updated = userRepository.save(user);
        return new UserProfileResponse(
                updated.getId(),
                updated.getEmail(),
                updated.getFullName(),
                updated.getRole(),
                updated.getStatus(),
                updated.getDepartment() != null ? updated.getDepartment().getId() : null,
                updated.getDepartment() != null ? updated.getDepartment().getName() : null,
                updated.getSemester(),
                updated.getGoals(),
                updated.getPreferredLanguage(),
                updated.getLevel(),
                updated.getCreatedAt()
        );
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password does not match");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }
}
