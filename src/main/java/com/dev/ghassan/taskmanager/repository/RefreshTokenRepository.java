package com.dev.ghassan.taskmanager.repository;

import com.dev.ghassan.taskmanager.model.RefreshToken;
import com.dev.ghassan.taskmanager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
    Optional<RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByUser(User user);
    void deleteByExpiryDateBefore(LocalDateTime now);
    void deleteByUser(User user);
}