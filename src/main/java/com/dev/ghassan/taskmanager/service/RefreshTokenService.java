package com.dev.ghassan.taskmanager.service;

import com.dev.ghassan.taskmanager.exception.TokenRefreshException;
import com.dev.ghassan.taskmanager.model.RefreshToken;
import com.dev.ghassan.taskmanager.model.User;
import com.dev.ghassan.taskmanager.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh.expiration:604800000}")
    private Long refreshTokenDurationMs; // 7 days

    @Transactional
    public RefreshToken createRefreshToken(User user) {
        // Check if user already has a valid refresh token
        Optional<RefreshToken> existingToken = refreshTokenRepository.findByUser(user);
        if (existingToken.isPresent()) {
            RefreshToken token = existingToken.get();
            if (token.getExpiryDate().isAfter(LocalDateTime.now())) {
                // Return existing valid token
                return token;
            }
            // Delete expired token
            refreshTokenRepository.delete(token);
        }
        
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(LocalDateTime.now().plusSeconds(refreshTokenDurationMs / 1000));
        refreshToken.setToken(UUID.randomUUID().toString());

        return refreshTokenRepository.save(refreshToken);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException("Refresh token was expired. Please make a new signin request");
        }
        return token;
    }

    @Transactional
    public void deleteByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }

    @Transactional
    public void deleteExpiredTokens() {
        refreshTokenRepository.deleteByExpiryDateBefore(LocalDateTime.now());
    }
}