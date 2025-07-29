package com.dev.ghassan.taskmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtResponse {
    private String accessToken;
    private String refreshToken;
    private String username;
    private String email;
    private String tokenType = "Bearer";
    
    public JwtResponse(String accessToken, String refreshToken, String username) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.username = username;
    }
    
    public JwtResponse(String accessToken, String refreshToken, String username, String email) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.username = username;
        this.email = email;
    }
}