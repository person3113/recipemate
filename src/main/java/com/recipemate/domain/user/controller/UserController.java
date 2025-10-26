package com.recipemate.domain.user.controller;

import com.recipemate.global.common.ApiResponse;
import com.recipemate.domain.user.dto.ChangePasswordRequest;
import com.recipemate.domain.user.dto.UpdateProfileRequest;
import com.recipemate.domain.user.dto.UserResponse;
import com.recipemate.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ApiResponse<UserResponse> getMyProfile(@AuthenticationPrincipal UserDetails userDetails) {
        UserResponse response = userService.getMyProfile(userDetails.getUsername());
        return ApiResponse.success(response);
    }

    @PutMapping("/me")
    public ApiResponse<UserResponse> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserResponse response = userService.updateProfile(userDetails.getUsername(), request);
        return ApiResponse.success(response);
    }

    @PutMapping("/me/password")
    public ApiResponse<Void> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(userDetails.getUsername(), request);
        return ApiResponse.success(null);
    }
}
