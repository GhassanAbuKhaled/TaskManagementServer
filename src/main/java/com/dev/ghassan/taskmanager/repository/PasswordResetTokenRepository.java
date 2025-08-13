package com.dev.ghassan.taskmanager.repository;

import com.dev.ghassan.taskmanager.model.PasswordResetToken;
import com.dev.ghassan.taskmanager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    
    Optional<PasswordResetToken> findByTokenAndUsedFalse(String token);
    
    void deleteByUser(User user);
    
    @Modifying
    @Query("DELETE FROM PasswordResetToken p WHERE p.expiryDate < :now")
    void deleteExpiredTokens(LocalDateTime now);
}