package com.dev.ghassan.taskmanager.controller;

import com.dev.ghassan.taskmanager.dto.UserResponse;
import com.dev.ghassan.taskmanager.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        // authentication.getName() now returns email
        String email = authentication.getName();
        return ResponseEntity.ok(userService.getUserProfile(email));
    }
}