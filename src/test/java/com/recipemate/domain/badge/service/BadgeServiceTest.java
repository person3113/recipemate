package com.recipemate.domain.badge.service;

import com.recipemate.domain.badge.entity.Badge;
import com.recipemate.domain.badge.repository.BadgeRepository;
import com.recipemate.domain.user.entity.User;
import com.recipemate.domain.user.repository.UserRepository;
import com.recipemate.global.common.BadgeType;
import com.recipemate.global.exception.CustomException;
import com.recipemate.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("BadgeService 테스트")
class BadgeServiceTest {

    @Mock
    private BadgeRepository badgeRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BadgeService badgeService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .nickname("테스터")
                .build();
    }

    @Test
    @DisplayName("배지 수여 - 성공")
    void awardBadge_Success() {
        // given
        Long userId = testUser.getId();
        BadgeType badgeType = BadgeType.FIRST_GROUP_BUY;

        given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
        given(badgeRepository.existsByUserIdAndBadgeType(userId, badgeType)).willReturn(false);

        // when
        badgeService.checkAndAwardBadge(userId, badgeType);

        // then
        ArgumentCaptor<Badge> captor = ArgumentCaptor.forClass(Badge.class);
        then(badgeRepository).should().save(captor.capture());

        Badge savedBadge = captor.getValue();
        assertThat(savedBadge.getUser()).isEqualTo(testUser);
        assertThat(savedBadge.getBadgeType()).isEqualTo(badgeType);
        assertThat(savedBadge.getAcquiredAt()).isNotNull();
    }

    @Test
    @DisplayName("배지 수여 - 이미 획득한 배지는 수여하지 않음")
    void awardBadge_AlreadyHasBadge_DoesNotAward() {
        // given
        Long userId = testUser.getId();
        BadgeType badgeType = BadgeType.REVIEWER;

        given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
        given(badgeRepository.existsByUserIdAndBadgeType(userId, badgeType)).willReturn(true);

        // when
        badgeService.checkAndAwardBadge(userId, badgeType);

        // then
        then(badgeRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("배지 수여 - 사용자 없음 예외")
    void awardBadge_UserNotFound_ThrowsException() {
        // given
        Long userId = 999L;
        BadgeType badgeType = BadgeType.TEN_PARTICIPATIONS;

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> badgeService.checkAndAwardBadge(userId, badgeType))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);

        then(badgeRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("배지 수여 - 여러 배지 타입 테스트")
    void awardBadge_DifferentBadgeTypes_Success() {
        // given
        Long userId = testUser.getId();

        given(userRepository.findById(userId)).willReturn(Optional.of(testUser));

        // FIRST_GROUP_BUY 배지
        given(badgeRepository.existsByUserIdAndBadgeType(userId, BadgeType.FIRST_GROUP_BUY))
                .willReturn(false);

        // when
        badgeService.checkAndAwardBadge(userId, BadgeType.FIRST_GROUP_BUY);

        // then
        ArgumentCaptor<Badge> captor = ArgumentCaptor.forClass(Badge.class);
        then(badgeRepository).should().save(captor.capture());
        assertThat(captor.getValue().getBadgeType()).isEqualTo(BadgeType.FIRST_GROUP_BUY);
    }

    @Test
    @DisplayName("배지 수여 - POPULAR_HOST 배지")
    void awardBadge_PopularHost_Success() {
        // given
        Long userId = testUser.getId();
        BadgeType badgeType = BadgeType.POPULAR_HOST;

        given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
        given(badgeRepository.existsByUserIdAndBadgeType(userId, badgeType)).willReturn(false);

        // when
        badgeService.checkAndAwardBadge(userId, badgeType);

        // then
        ArgumentCaptor<Badge> captor = ArgumentCaptor.forClass(Badge.class);
        then(badgeRepository).should().save(captor.capture());

        Badge savedBadge = captor.getValue();
        assertThat(savedBadge.getBadgeType()).isEqualTo(BadgeType.POPULAR_HOST);
        assertThat(savedBadge.getBadgeType().getDisplayName()).isEqualTo("인기 호스트");
    }
}
