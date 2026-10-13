package com.studyhub.platform.modules.auth.service;

import com.studyhub.platform.common.exception.UnauthorizedException;
import com.studyhub.platform.common.utils.SecurityUtils;
import com.studyhub.platform.config.ApplicationProperties;
import com.studyhub.platform.modules.auth.entity.RefreshToken;
import com.studyhub.platform.modules.auth.entity.User;
import com.studyhub.platform.modules.auth.repository.RefreshTokenRepository;
import com.studyhub.platform.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
public class RefreshTokenService {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenService.class);

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final ApplicationProperties applicationProperties;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository,
                               JwtTokenProvider jwtTokenProvider,
                               ApplicationProperties applicationProperties) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.applicationProperties = applicationProperties;
    }

    @Transactional
    public String createRefreshToken(User user) {
        String rawToken = jwtTokenProvider.generateRefreshTokenString();
        String tokenHash = SecurityUtils.hashSha256(rawToken);

        Instant expiryDate = Instant.now().plusMillis(applicationProperties.getJwt().getRefreshExpirationMs());
        RefreshToken refreshToken = new RefreshToken(user, tokenHash, expiryDate);

        refreshTokenRepository.save(refreshToken);
        return rawToken;
    }

    @Transactional
    public RotateTokenResult rotateRefreshToken(String rawOldToken) {
        String tokenHash = SecurityUtils.hashSha256(rawOldToken);
        Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByTokenHash(tokenHash);

        if (tokenOpt.isEmpty()) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        RefreshToken oldToken = tokenOpt.get();

        // Replay / Breach Defense
        if (oldToken.isRevoked()) {
            log.warn("Detected re-use of revoked refresh token for user {}. Revoking all user tokens.", oldToken.getUser().getEmail());
            refreshTokenRepository.revokeAllUserTokens(oldToken.getUser());
            throw new UnauthorizedException("Invalid refresh token state. Session terminated for security.");
        }

        if (oldToken.getExpiryDate().isBefore(Instant.now())) {
            oldToken.setRevoked(true);
            refreshTokenRepository.save(oldToken);
            throw new UnauthorizedException("Refresh token has expired. Please log in again.");
        }

        // Revoke the old token
        oldToken.setRevoked(true);
        refreshTokenRepository.save(oldToken);

        // Generate and persist new refresh token (Rotation)
        User user = oldToken.getUser();
        String newRawToken = createRefreshToken(user);

        return new RotateTokenResult(user, newRawToken);
    }

    @Transactional
    public void revokeRefreshToken(String rawToken) {
        String tokenHash = SecurityUtils.hashSha256(rawToken);
        refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }

    public record RotateTokenResult(User user, String newRefreshToken) {}
}
