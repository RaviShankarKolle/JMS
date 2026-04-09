package com.library.borrow.client;

import com.library.borrow.common.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface UserClient {

    @GetMapping("/api/v1/internal/users/{userId}/eligibility")
    ApiResponse<UserEligibilityResponse> getEligibility(@PathVariable("userId") Long userId);

    record UserEligibilityResponse(Long userId, boolean eligible, String reason) {}
}
