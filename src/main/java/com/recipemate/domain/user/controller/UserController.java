package com.recipemate.domain.user.controller;

import com.recipemate.domain.groupbuy.dto.GroupBuyResponse;
import com.recipemate.global.common.ApiResponse;
import com.recipemate.domain.user.dto.ChangePasswordRequest;
import com.recipemate.domain.user.dto.UpdateProfileRequest;
import com.recipemate.domain.user.dto.UserResponse;
import com.recipemate.domain.user.service.UserService;
import com.recipemate.global.common.GroupBuyStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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

    @GetMapping("/me/group-purchases")
    public ApiResponse<Page<GroupBuyResponse>> getMyGroupBuys(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) GroupBuyStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        UserResponse userResponse = userService.getMyProfile(userDetails.getUsername());
        Page<GroupBuyResponse> groupBuys = userService.getMyGroupBuys(userResponse.getId(), status, pageable);
        return ApiResponse.success(groupBuys);
    }

    @GetMapping("/me/participations")
    public ApiResponse<Page<GroupBuyResponse>> getParticipatedGroupBuys(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) GroupBuyStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        UserResponse userResponse = userService.getMyProfile(userDetails.getUsername());
        Page<GroupBuyResponse> participations = userService.getParticipatedGroupBuys(userResponse.getId(), status, pageable);
        return ApiResponse.success(participations);
    }
}
