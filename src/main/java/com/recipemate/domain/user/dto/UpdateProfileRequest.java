package com.recipemate.domain.user.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdateProfileRequest {

    @Size(min = 2, max = 50, message = "닉네임은 2자 이상 50자 이하여야 합니다")
    private String nickname;

    @Pattern(regexp = "^\\d{3}-\\d{4}-\\d{4}$", message = "전화번호 형식은 000-0000-0000 입니다")
    private String phoneNumber;

    private String profileImageUrl;

    public static UpdateProfileRequest of(String nickname, String phoneNumber, String profileImageUrl) {
        return new UpdateProfileRequest(nickname, phoneNumber, profileImageUrl);
    }
}
