package com.studyhub.platform.modules.auth.service;

import com.studyhub.platform.common.enums.Role;
import com.studyhub.platform.common.enums.UserStatus;
import com.studyhub.platform.common.exception.BadRequestException;
import com.studyhub.platform.config.ApplicationProperties;
import com.studyhub.platform.modules.auth.dto.AuthResponse;
import com.studyhub.platform.modules.auth.dto.RegisterRequest;
import com.studyhub.platform.modules.auth.entity.User;
import com.studyhub.platform.modules.auth.repository.UserRepository;
import com.studyhub.platform.modules.taxonomy.entity.Department;
import com.studyhub.platform.modules.taxonomy.repository.DepartmentRepository;
import com.studyhub.platform.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private ApplicationProperties applicationProperties;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        ApplicationProperties.Jwt jwt = new ApplicationProperties.Jwt();
        jwt.setAccessExpirationMs(900000);
        lenient().when(applicationProperties.getJwt()).thenReturn(jwt);
    }

    @Test
    void register_ShouldSucceed_WhenValidRequest() {
        RegisterRequest request = new RegisterRequest("student@test.edu", "Password123!", "Test Student", 1L, 4);

        Department dept = new Department("Computer Science & Engineering", "CSE");
        dept.setId(1L);

        when(userRepository.existsByEmail("student@test.edu")).thenReturn(false);
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(dept));
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setEmail("student@test.edu");
        savedUser.setFullName("Test Student");
        savedUser.setDepartment(dept);
        savedUser.setSemester(4);
        savedUser.setRole(Role.STUDENT);
        savedUser.setStatus(UserStatus.ACTIVE);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtTokenProvider.generateAccessToken(any())).thenReturn("mock-access-token");
        when(refreshTokenService.createRefreshToken(any())).thenReturn("mock-refresh-token");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("mock-access-token", response.accessToken());
        assertEquals("mock-refresh-token", response.refreshToken());
        assertEquals("student@test.edu", response.email());
        assertEquals(Role.STUDENT, response.role());
    }

    @Test
    void register_ShouldThrowException_WhenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest("existing@test.edu", "Password123!", "Test Student", 1L, 4);
        when(userRepository.existsByEmail("existing@test.edu")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }
}
