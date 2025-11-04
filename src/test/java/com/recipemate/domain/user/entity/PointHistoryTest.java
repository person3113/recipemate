package com.recipemate.domain.user.entity;

import com.recipemate.global.common.PointType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PointHistoryTest {

    @Test
    @DisplayName("PointHistory 생성 시 모든 필드가 올바르게 설정된다")
    void createPointHistory() {
        User user = User.create(
                "test@example.com",
                "encodedPassword",
                "테스터",
                "010-1234-5678"
        );

        PointHistory pointHistory = PointHistory.create(
                user,
                50,
                "공동구매 생성",
                PointType.EARN
        );

        assertThat(pointHistory.getUser()).isEqualTo(user);
        assertThat(pointHistory.getAmount()).isEqualTo(50);
        assertThat(pointHistory.getDescription()).isEqualTo("공동구매 생성");
        assertThat(pointHistory.getType()).isEqualTo(PointType.EARN);
    }

    @Test
    @DisplayName("포인트 사용 내역을 생성할 수 있다")
    void createPointHistoryForUse() {
        User user = User.create(
                "test@example.com",
                "encodedPassword",
                "테스터",
                "010-1234-5678"
        );

        PointHistory pointHistory = PointHistory.create(
                user,
                30,
                "상품 구매",
                PointType.USE
        );

        assertThat(pointHistory.getType()).isEqualTo(PointType.USE);
        assertThat(pointHistory.getAmount()).isEqualTo(30);
    }

    @Test
    @DisplayName("포인트 적립 내역을 생성할 수 있다")
    void createPointHistoryForEarn() {
        User user = User.create(
                "test@example.com",
                "encodedPassword",
                "테스터",
                "010-1234-5678"
        );

        PointHistory pointHistory = PointHistory.create(
                user,
                20,
                "후기 작성",
                PointType.EARN
        );

        assertThat(pointHistory.getType()).isEqualTo(PointType.EARN);
        assertThat(pointHistory.getAmount()).isEqualTo(20);
        assertThat(pointHistory.getDescription()).isEqualTo("후기 작성");
    }
}
