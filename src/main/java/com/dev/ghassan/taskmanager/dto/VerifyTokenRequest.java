package com.dev.ghassan.taskmanager.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyTokenRequest {
    
    @NotBlank(message = "Token is required")
    private String token;
}