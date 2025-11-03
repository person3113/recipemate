package com.recipemate.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LoginRequest {

    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다")
    private String password;

    @Builder.Default
    private Boolean rememberMe = false;

    public static LoginRequest of(String email, String password) {
        return LoginRequest.builder()
                .email(email)
                .password(password)
                .rememberMe(false)
                .build();
    }

    public static LoginRequest of(String email, String password, Boolean rememberMe) {
        return LoginRequest.builder()
                .email(email)
                .password(password)
                .rememberMe(rememberMe != null ? rememberMe : false)
                .build();
    }
}

