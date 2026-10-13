package com.studyhub.platform.modules.auth.service;

import com.studyhub.platform.common.enums.Role;
import com.studyhub.platform.common.enums.UserStatus;
import com.studyhub.platform.common.exception.UnauthorizedException;
import com.studyhub.platform.common.utils.SecurityUtils;
import com.studyhub.platform.config.ApplicationProperties;
import com.studyhub.platform.modules.auth.entity.RefreshToken;
import com.studyhub.platform.modules.auth.entity.User;
import com.studyhub.platform.modules.auth.repository.RefreshTokenRepository;
import com.studyhub.platform.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private ApplicationProperties applicationProperties;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("student@test.edu");
        testUser.setRole(Role.STUDENT);
        testUser.setStatus(UserStatus.ACTIVE);

        ApplicationProperties.Jwt jwt = new ApplicationProperties.Jwt();
        jwt.setRefreshExpirationMs(604800000);
        lenient().when(applicationProperties.getJwt()).thenReturn(jwt);
    }

    @Test
    void rotateRefreshToken_ShouldRotateAndReturnNewToken_WhenValid() {
        String rawToken = "raw-valid-token";
        String tokenHash = SecurityUtils.hashSha256(rawToken);

        RefreshToken validToken = new RefreshToken(testUser, tokenHash, Instant.now().plus(7, ChronoUnit.DAYS));

        when(refreshTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(validToken));
        when(jwtTokenProvider.generateRefreshTokenString()).thenReturn("new-raw-refresh-token");

        RefreshTokenService.RotateTokenResult result = refreshTokenService.rotateRefreshToken(rawToken);

        assertNotNull(result);
        assertEquals("new-raw-refresh-token", result.newRefreshToken());
        assertEquals(testUser.getEmail(), result.user().getEmail());
        assertTrue(validToken.isRevoked());
        verify(refreshTokenRepository, times(1)).save(validToken);
    }

    @Test
    void rotateRefreshToken_ShouldRevokeAllUserTokens_WhenReplayAttackDetected() {
        String rawToken = "revoked-token";
        String tokenHash = SecurityUtils.hashSha256(rawToken);

        RefreshToken revokedToken = new RefreshToken(testUser, tokenHash, Instant.now().plus(7, ChronoUnit.DAYS));
        revokedToken.setRevoked(true); // already revoked!

        when(refreshTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(revokedToken));

        assertThrows(UnauthorizedException.class, () -> refreshTokenService.rotateRefreshToken(rawToken));
        verify(refreshTokenRepository, times(1)).revokeAllUserTokens(testUser);
    }
}
