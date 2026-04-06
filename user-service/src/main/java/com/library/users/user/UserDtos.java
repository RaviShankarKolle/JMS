package com.library.users.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Set;

public class UserDtos {
    public record RegisterRequest(
            @NotBlank @Size(max = 120) String name,
            @NotBlank @Email String email,
            @NotBlank @Size(min = 8, max = 64) String password,
            @Pattern(regexp = "^[0-9+\\-() ]{7,30}$", message = "Invalid phone format") String phone,
            String role
    ) {}

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password
    ) {}

    public record ForgotPasswordRequest(@NotBlank @Email String email) {}

    public record ResetPasswordRequest(
            @NotBlank String token,
            @NotBlank @Size(min = 8, max = 64) String newPassword
    ) {}

    public record UpdateProfileRequest(
            @NotBlank @Size(max = 120) String name,
            @Pattern(regexp = "^[0-9+\\-() ]{7,30}$", message = "Invalid phone format") String phone
    ) {}

    public record AuthResponse(String accessToken, String tokenType, long expiresInSeconds) {}

    public record UserProfileResponse(Long id, String name, String email, String phone, String status, Set<String> roles) {}
}
