package kz.legeal.ease.backend.service.impl;

import kz.legeal.ease.backend.config.JwtProperties;
import kz.legeal.ease.backend.domain.RefreshToken;
import kz.legeal.ease.backend.domain.User;
import kz.legeal.ease.backend.jwt.JwtUtils;
import kz.legeal.ease.backend.jwt.PersonDetails;
import kz.legeal.ease.backend.repository.RefreshTokenRepository;
import kz.legeal.ease.backend.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final RefreshTokenRepository repository;
    private final JwtUtils jwtUtils;
    private final JwtProperties jwtProperties;

    @Override
    @Transactional
    public String generateAccessToken(PersonDetails userDetails) {
        return jwtUtils.generateAccessToken(userDetails);
    }

    @Override
    @Transactional
    public String generateRefreshToken(PersonDetails userDetails) {
        final var refreshToken = jwtUtils.generateRefreshToken(userDetails);
        saveRefreshToken(userDetails.getUser(), refreshToken);
        return refreshToken;
    }

    @Override
    @Transactional
    public void revokeToken(RefreshToken token) {
        markAsRevoked(token);
    }

    @Override
    @Transactional
    public RefreshToken findValidStoredToken(String refreshToken) {
        final var storedToken = repository.findByTokenAndRevokedFalse(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or revoked refresh token"));

        if (storedToken.isExpired()) {
            markAsRevoked(storedToken);
            throw new IllegalArgumentException("Refresh token has expired");
        }
        return storedToken;
    }

    @Override
    @Transactional
    public void revokeAllByUser(User user) {
        repository.findAllByUserAndRevokedFalse(user)
                .forEach(this::markAsRevoked);
    }

    @Override
    @Transactional(readOnly = true)
    public long getAccessExpMin() {
        return jwtProperties.getAccessExpMin() * 60L;
    }

    @Override
    @Transactional(readOnly = true)
    public Long extractUserId(String token, boolean isRefreshToken) {
        return jwtUtils.extractUserId(token, isRefreshToken);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isTokenValid(String token, PersonDetails userDetails, boolean isRefreshToken) {
        return jwtUtils.isTokenValid(token, userDetails, isRefreshToken);
    }

    private void saveRefreshToken(User user, String tokenValue) {
        final var refreshToken = RefreshToken.builder()
                .token(tokenValue)
                .user(user)
                .expiryDate(LocalDateTime.now().plusMinutes(jwtProperties.getRefreshExpMin()))
                .revoked(false)
                .build();
        repository.save(refreshToken);
    }

    private void markAsRevoked(RefreshToken token) {
        token.setRevoked(true);
        repository.save(token);
    }
}
