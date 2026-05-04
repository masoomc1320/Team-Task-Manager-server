package com.taskmanager.dto;

import com.taskmanager.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthDtos {
    public record SignupRequest(
            @NotBlank @Size(min = 2) String name,
            @NotBlank @Email String email,
            @NotBlank @Size(min = 6) String password,
            Role role
    ) {}

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank @Size(min = 6) String password
    ) {}

    public record UserResponse(Long id, String name, String email, String role) {}
    public record AuthResponse(String token, UserResponse user) {}
}
