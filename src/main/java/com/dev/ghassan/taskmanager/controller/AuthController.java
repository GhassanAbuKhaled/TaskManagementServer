package com.dev.ghassan.taskmanager.controller;

import com.dev.ghassan.taskmanager.dto.*;
import com.dev.ghassan.taskmanager.exception.TokenRefreshException;
import com.dev.ghassan.taskmanager.model.RefreshToken;
import com.dev.ghassan.taskmanager.security.JwtUtil;
import com.dev.ghassan.taskmanager.service.RefreshTokenService;
import com.dev.ghassan.taskmanager.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final JwtUtil jwtUtil;
    
    @Value("${jwt.expiration:900000}")
    private Long jwtExpiration;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration request received for email: {}", request.getEmail());
        AuthResponse response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request received for email: {}", request.getEmail());
        AuthResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        log.info("Token refresh request received");
        
        String requestRefreshToken = request.getRefreshToken();
        
        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String newAccessToken = jwtUtil.generateToken(user.getEmail());
                    
                    AuthResponse response = AuthResponse.builder()
                            .accessToken(newAccessToken)
                            .refreshToken(requestRefreshToken)
                            .expiresIn(jwtExpiration / 1000)
                            .user(AuthResponse.UserInfo.builder()
                                    .id(user.getId())
                                    .username(user.getUsername())
                                    .email(user.getEmail())
                                    .build())
                            .build();
                    
                    return ResponseEntity.ok(response);
                })
                .orElseThrow(() -> new TokenRefreshException("Invalid refresh token"));
    }
    
    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getUserProfile(Authentication authentication) {
        String email = authentication.getName();
        log.debug("Profile request for user: {}", email);
        return ResponseEntity.ok(userService.getUserProfile(email));
    }
    
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(Authentication authentication) {
        String email = authentication.getName();
        log.info("Logout request for user: {}", email);
        // Invalidate refresh tokens for the user
        // This would be implemented based on your refresh token strategy
        return ResponseEntity.ok().build();
    }
}