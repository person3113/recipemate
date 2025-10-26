package com.recipemate.domain.user.controller;

import com.recipemate.global.common.ApiResponse;
import com.recipemate.domain.user.dto.LoginRequest;
import com.recipemate.domain.user.dto.SignupRequest;
import com.recipemate.domain.user.dto.UserResponse;
import com.recipemate.domain.user.service.AuthService;
import com.recipemate.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthService authService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserResponse> signup(@Valid @RequestBody SignupRequest request) {
        UserResponse response = userService.signup(request);
        return ApiResponse.success(response);
    }

    @PostMapping("/login")
    public ApiResponse<UserResponse> login(@Valid @RequestBody LoginRequest request) {
        UserResponse response = authService.login(request);
        return ApiResponse.success(response);
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        authService.logout();
        return ApiResponse.success(null);
    }
}
