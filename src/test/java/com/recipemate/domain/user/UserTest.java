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

    @Test
    @DisplayName("User 생성 시 포인트 기본값은 0이다")
    void defaultPoints() {
        User user = User.create(
                "test@example.com",
                "encodedPassword",
                "테스터",
                "010-1234-5678"
        );

        assertThat(user.getPoints()).isEqualTo(0);
    }

    @Test
    @DisplayName("포인트를 적립할 수 있다")
    void earnPoints() {
        User user = User.create(
                "test@example.com",
                "encodedPassword",
                "테스터",
                "010-1234-5678"
        );

        user.earnPoints(50);

        assertThat(user.getPoints()).isEqualTo(50);
    }

    @Test
    @DisplayName("포인트를 여러 번 적립할 수 있다")
    void earnPointsMultipleTimes() {
        User user = User.create(
                "test@example.com",
                "encodedPassword",
                "테스터",
                "010-1234-5678"
        );

        user.earnPoints(50);
        user.earnPoints(30);
        user.earnPoints(20);

        assertThat(user.getPoints()).isEqualTo(100);
    }

    @Test
    @DisplayName("포인트를 사용할 수 있다")
    void usePoints() {
        User user = User.create(
                "test@example.com",
                "encodedPassword",
                "테스터",
                "010-1234-5678"
        );
        user.earnPoints(100);

        user.usePoints(30);

        assertThat(user.getPoints()).isEqualTo(70);
    }

    @Test
    @DisplayName("보유 포인트보다 많은 포인트를 사용할 수 없다")
    void cannotUseMorePointsThanAvailable() {
        User user = User.create(
                "test@example.com",
                "encodedPassword",
                "테스터",
                "010-1234-5678"
        );
        user.earnPoints(50);

        boolean result = user.usePoints(100);

        assertThat(result).isFalse();
        assertThat(user.getPoints()).isEqualTo(50);
    }

    @Test
    @DisplayName("포인트가 충분한 경우 사용 성공을 반환한다")
    void usePointsReturnsSuccessWhenSufficientPoints() {
        User user = User.create(
                "test@example.com",
                "encodedPassword",
                "테스터",
                "010-1234-5678"
        );
        user.earnPoints(100);

        boolean result = user.usePoints(50);

        assertThat(result).isTrue();
        assertThat(user.getPoints()).isEqualTo(50);
    }
}
