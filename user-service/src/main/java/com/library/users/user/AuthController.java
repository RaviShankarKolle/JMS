package com.library.users.user;

import com.library.users.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.library.users.user.UserDtos.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest servletRequest) {
        authService.register(request, servletRequest.getRemoteAddr(), servletRequest.getHeader("User-Agent"));
        return ResponseEntity.ok(ApiResponse.success(null, Map.of("message", "Registration successful.")));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        AuthResponse response = authService.login(request, servletRequest.getRemoteAddr(), servletRequest.getHeader("User-Agent"));
        return ResponseEntity.ok(ApiResponse.success(response, Map.of()));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request, HttpServletRequest servletRequest) {
        authService.forgotPassword(request, servletRequest.getRemoteAddr(), servletRequest.getHeader("User-Agent"));
        return ResponseEntity.ok(ApiResponse.success(null, Map.of("message", "If account exists, reset instruction is processed.")));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request, HttpServletRequest servletRequest) {
        authService.resetPassword(request, servletRequest.getRemoteAddr(), servletRequest.getHeader("User-Agent"));
        return ResponseEntity.ok(ApiResponse.success(null, Map.of("message", "Password reset successful.")));
    }
}
