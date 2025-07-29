package com.dev.ghassan.taskmanager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AuthResponse {
    @JsonProperty("access_token")
    private String accessToken;
    
    @JsonProperty("refresh_token")
    private String refreshToken;
    
    @JsonProperty("token_type")
    @Builder.Default
    private String tokenType = "Bearer";
    
    @JsonProperty("expires_in")
    private Long expiresIn; // seconds
    
    private UserInfo user;
    
    @JsonProperty("issued_at")
    @Builder.Default
    private LocalDateTime issuedAt = LocalDateTime.now();
    
    @Data
    @Builder
    public static class UserInfo {
        private Long id;
        private String username;
        private String email;
    }
}