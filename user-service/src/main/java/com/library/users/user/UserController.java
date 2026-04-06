package com.library.users.user;

import com.library.users.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.library.users.user.UserDtos.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserProfileService userProfileService;

    public UserController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getCurrentUserProfile(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(userProfileService.getCurrentUserProfile(userId), Map.of()));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateCurrentUserProfile(@Valid @RequestBody UpdateProfileRequest request,
                                                                                      Authentication authentication,
                                                                                      HttpServletRequest servletRequest) {
        Long userId = Long.parseLong(authentication.getName());
        UserProfileResponse response = userProfileService.updateCurrentUserProfile(
                userId, request, servletRequest.getRemoteAddr(), servletRequest.getHeader("User-Agent")
        );
        return ResponseEntity.ok(ApiResponse.success(response, Map.of("message", "Profile updated.")));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/ping")
    public ResponseEntity<ApiResponse<String>> adminPing() {
        return ResponseEntity.ok(ApiResponse.success("admin-ok", Map.of()));
    }
}
