package com.recipemate.domain.user;

import com.recipemate.global.common.UserRole;
import com.recipemate.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    @DisplayName("User 생성 시 매너온도 기본값은 36.5이다")
    void defaultMannerTemperature() {
        User user = User.create(
                "test@example.com",
                "encodedPassword",
                "테스터",
                "010-1234-5678"
        );

        assertThat(user.getMannerTemperature()).isEqualTo(36.5);
    }

    @Test
    @DisplayName("User 생성 시 기본 역할은 USER이다")
    void defaultRole() {
        User user = User.create(
                "test@example.com",
                "encodedPassword",
                "테스터",
                "010-1234-5678"
        );

        assertThat(user.getRole()).isEqualTo(UserRole.USER);
    }

    @Test
    @DisplayName("매너온도를 변경할 수 있다")
    void updateMannerTemperature() {
        User user = User.create(
                "test@example.com",
                "encodedPassword",
                "테스터",
                "010-1234-5678"
        );

        user.updateMannerTemperature(0.5);

        assertThat(user.getMannerTemperature()).isEqualTo(37.0);
    }

    @Test
    @DisplayName("프로필을 수정할 수 있다")
    void updateProfile() {
        User user = User.create(
                "test@example.com",
                "encodedPassword",
                "테스터",
                "010-1234-5678"
        );

        user.updateProfile("새닉네임", "010-9999-9999", "https://example.com/image.jpg");

        assertThat(user.getNickname()).isEqualTo("새닉네임");
        assertThat(user.getPhoneNumber()).isEqualTo("010-9999-9999");
        assertThat(user.getProfileImageUrl()).isEqualTo("https://example.com/image.jpg");
    }

    @Test
    @DisplayName("비밀번호를 변경할 수 있다")
    void changePassword() {
        User user = User.create(
                "test@example.com",
                "encodedPassword",
                "테스터",
                "010-1234-5678"
        );

        user.changePassword("newEncodedPassword");

        assertThat(user.getPassword()).isEqualTo("newEncodedPassword");
    }
}
