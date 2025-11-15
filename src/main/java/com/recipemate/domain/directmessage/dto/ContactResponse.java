package com.recipemate.domain.directmessage.dto;

import com.recipemate.domain.user.entity.User;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ContactResponse {
    private Long userId;
    private String nickname;
    private String profileImageUrl;
    private Double mannerTemperature;

    public static ContactResponse from(User user) {
        return ContactResponse.builder()
            .userId(user.getId())
            .nickname(user.getNickname())
            .profileImageUrl(user.getProfileImageUrl())
            .mannerTemperature(user.getMannerTemperature())
            .build();
    }
}

