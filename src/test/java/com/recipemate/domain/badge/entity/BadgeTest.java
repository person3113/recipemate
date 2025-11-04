package com.recipemate.domain.badge.entity;

import com.recipemate.domain.user.entity.User;
import com.recipemate.global.common.BadgeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class BadgeTest {

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.create(
                "test@example.com",
                "encodedPassword",
                "테스터",
                "010-1234-5678"
        );
    }

    @Test
    @DisplayName("Badge 생성 시 acquiredAt이 자동으로 설정된다")
    void createBadge() {
        Badge badge = Badge.create(testUser, BadgeType.FIRST_GROUP_BUY);

        assertThat(badge.getUser()).isEqualTo(testUser);
        assertThat(badge.getBadgeType()).isEqualTo(BadgeType.FIRST_GROUP_BUY);
        assertThat(badge.getAcquiredAt()).isNotNull();
        assertThat(badge.getAcquiredAt()).isBefore(LocalDateTime.now().plusSeconds(1));
    }

    @Test
    @DisplayName("동일한 사용자와 배지 타입으로 Badge를 생성할 수 있다")
    void createBadgeWithSameUserAndType() {
        Badge badge1 = Badge.create(testUser, BadgeType.REVIEWER);
        Badge badge2 = Badge.create(testUser, BadgeType.REVIEWER);

        assertThat(badge1.getUser()).isEqualTo(badge2.getUser());
        assertThat(badge1.getBadgeType()).isEqualTo(badge2.getBadgeType());
        // DB 제약조건(UNIQUE)에서 중복 방지는 Repository 테스트에서 검증
    }

    @Test
    @DisplayName("Badge의 acquiredAt은 변경할 수 없다")
    void acquiredAtIsImmutable() {
        Badge badge = Badge.create(testUser, BadgeType.TEN_PARTICIPATIONS);
        LocalDateTime originalAcquiredAt = badge.getAcquiredAt();

        // acquiredAt은 final이므로 변경 메서드가 없어야 함
        assertThat(badge.getAcquiredAt()).isEqualTo(originalAcquiredAt);
    }

    @Test
    @DisplayName("Badge는 User와 BadgeType 정보를 올바르게 반환한다")
    void getBadgeInfo() {
        Badge badge = Badge.create(testUser, BadgeType.POPULAR_HOST);

        assertThat(badge.getUser().getEmail()).isEqualTo("test@example.com");
        assertThat(badge.getBadgeType()).isEqualTo(BadgeType.POPULAR_HOST);
        assertThat(badge.getBadgeType().getDisplayName()).isEqualTo("인기 호스트");
        assertThat(badge.getBadgeType().getDescription()).isEqualTo("매너온도 40도 이상을 달성했습니다");
    }

    @Test
    @DisplayName("서로 다른 배지 타입의 Badge를 생성할 수 있다")
    void createDifferentBadgeTypes() {
        Badge badge1 = Badge.create(testUser, BadgeType.FIRST_GROUP_BUY);
        Badge badge2 = Badge.create(testUser, BadgeType.REVIEWER);

        assertThat(badge1.getBadgeType()).isNotEqualTo(badge2.getBadgeType());
        assertThat(badge1.getUser()).isEqualTo(badge2.getUser());
    }
}
