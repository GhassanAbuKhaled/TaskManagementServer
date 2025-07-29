package com.dev.ghassan.taskmanager.service;

import com.dev.ghassan.taskmanager.dto.AuthResponse;
import com.dev.ghassan.taskmanager.dto.LoginRequest;
import com.dev.ghassan.taskmanager.dto.RegisterRequest;
import com.dev.ghassan.taskmanager.dto.UserResponse;
import com.dev.ghassan.taskmanager.exception.*;
import com.dev.ghassan.taskmanager.model.User;
import com.dev.ghassan.taskmanager.repository.UserRepository;
import com.dev.ghassan.taskmanager.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    
    @Value("${jwt.expiration:900000}")
    private Long jwtExpiration;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registration attempt for email: {}", request.getEmail());
        
        String email = request.getEmail().toLowerCase().trim();
        String username = request.getUsername().trim();
        
        // Check for existing email
        if (userRepository.existsByEmail(email)) {
            log.warn("Registration failed - email already exists: {}", email);
            throw new EmailAlreadyExistsException("An account with this email already exists");
        }
        
        // Check for existing username
        if (userRepository.existsByUsername(username)) {
            log.warn("Registration failed - username already exists: {}", username);
            throw new UserAlreadyExistsException("This username is already taken");
        }

        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .accountLocked(false)
                .build();
        
        user = userRepository.save(user);
        log.info("User registered successfully: {}", user.getEmail());

        return generateAuthResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        log.info("Login attempt for email: {}", email);
        
        try {
            // Authenticate user
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword())
            );
            
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new AuthenticationException("Invalid email or password"));
            
            log.info("User logged in successfully: {}", user.getEmail());
            return generateAuthResponse(user);
            
        } catch (BadCredentialsException e) {
            log.warn("Login failed - invalid credentials for email: {}", email);
            throw new AuthenticationException("Invalid email or password");
        } catch (DisabledException e) {
            log.warn("Login failed - account disabled for email: {}", email);
            throw new AuthenticationException("Account is disabled");
        } catch (LockedException e) {
            log.warn("Login failed - account locked for email: {}", email);
            throw new AccountLockedException("Account is locked");
        } catch (Exception e) {
            log.error("Login failed for email: {} - {}", email, e.getMessage());
            throw new AuthenticationException("Authentication failed");
        }
    }
    
    public UserResponse getUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail());
    }
    
    private AuthResponse generateAuthResponse(User user) {
        String accessToken = jwtUtil.generateToken(user.getEmail());
        String refreshToken = refreshTokenService.createRefreshToken(user).getToken();
        
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtExpiration / 1000) // Convert to seconds
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .build())
                .build();
    }
}