package com.recipemate.domain.user.dto;

import com.recipemate.global.common.UserRole;
import com.recipemate.domain.user.entity.User;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserResponse {

    private Long id;
    private String email;
    private String nickname;
    private String phoneNumber;
    private String profileImageUrl;
    private Double mannerTemperature;
    private UserRole role;
    private LocalDateTime createdAt;

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getPhoneNumber(),
                user.getProfileImageUrl(),
                user.getMannerTemperature(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}
