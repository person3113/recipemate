package com.recipemate.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ChangePasswordRequest {

    @NotBlank(message = "현재 비밀번호는 필수입니다")
    private String currentPassword;

    @NotBlank(message = "새 비밀번호는 필수입니다")
    @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다")
    private String newPassword;

    public static ChangePasswordRequest of(String currentPassword, String newPassword) {
        return new ChangePasswordRequest(currentPassword, newPassword);
    }
}
