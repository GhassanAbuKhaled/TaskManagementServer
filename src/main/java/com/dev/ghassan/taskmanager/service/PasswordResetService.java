package com.dev.ghassan.taskmanager.service;

import com.dev.ghassan.taskmanager.exception.ResourceNotFoundException;
import com.dev.ghassan.taskmanager.model.PasswordResetToken;
import com.dev.ghassan.taskmanager.model.User;
import com.dev.ghassan.taskmanager.repository.PasswordResetTokenRepository;
import com.dev.ghassan.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {
    
    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();
    
    private static final int TOKEN_EXPIRY_HOURS = 1;
    
    @Transactional
    public void initiatePasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        
        // Delete any existing tokens for this user
        tokenRepository.deleteByUser(user);
        
        // Generate secure token
        String token = generateSecureToken();
        
        // Create and save new token
        PasswordResetToken resetToken = new PasswordResetToken(
                token, 
                user, 
                LocalDateTime.now().plusHours(TOKEN_EXPIRY_HOURS)
        );
        
        tokenRepository.save(resetToken);
        
        // Send email
        emailService.sendPasswordResetEmail(user.getEmail(), token);
        
        log.info("Password reset initiated for user: {}", email);
    }
    
    public boolean verifyResetToken(String token) {
        return tokenRepository.findByTokenAndUsedFalse(token)
                .map(resetToken -> !resetToken.isExpired())
                .orElse(false);
    }
    
    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByTokenAndUsedFalse(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset token"));
        
        if (resetToken.isExpired()) {
            throw new IllegalArgumentException("Reset token has expired");
        }
        
        // Update user password
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        // Mark token as used
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
        
        log.info("Password reset completed for user: {}", user.getEmail());
    }
    
    private String generateSecureToken() {
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }
    
    @Scheduled(fixedRate = 3600000) // Run every hour
    @Transactional
    public void cleanupExpiredTokens() {
        tokenRepository.deleteExpiredTokens(LocalDateTime.now());
        log.debug("Cleaned up expired password reset tokens");
    }
}