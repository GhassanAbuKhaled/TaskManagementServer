package com.dev.ghassan.taskmanager.config;

import com.dev.ghassan.taskmanager.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class SchedulingConfig {
    private final RefreshTokenService refreshTokenService;

    @Scheduled(fixedRate = 3600000) // Run every hour
    public void cleanupExpiredTokens() {
        log.info("Cleaning up expired refresh tokens");
        refreshTokenService.deleteExpiredTokens();
    }
}