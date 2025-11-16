package com.recipemate.domain.badge.service;

import com.recipemate.domain.badge.dto.BadgeChallengeResponse;
import com.recipemate.domain.badge.dto.BadgeResponse;
import com.recipemate.domain.badge.entity.Badge;
import com.recipemate.domain.badge.repository.BadgeRepository;
import com.recipemate.domain.groupbuy.repository.GroupBuyRepository;
import com.recipemate.domain.groupbuy.repository.ParticipationRepository;
import com.recipemate.domain.review.repository.ReviewRepository;
import com.recipemate.domain.user.entity.User;
import com.recipemate.domain.user.repository.UserRepository;
import com.recipemate.global.common.BadgeType;
import com.recipemate.global.exception.CustomException;
import com.recipemate.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BadgeService {

    private final BadgeRepository badgeRepository;
    private final UserRepository userRepository;
    private final GroupBuyRepository groupBuyRepository;
    private final ParticipationRepository participationRepository;
    private final ReviewRepository reviewRepository;

    /**
     * 배지 조건 확인 후 수여
     * 
     * @param userId 사용자 ID
     * @param badgeType 배지 타입
     */
    public void checkAndAwardBadge(Long userId, BadgeType badgeType) {
        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 이미 획득한 배지인지 확인
        if (badgeRepository.existsByUserIdAndBadgeType(userId, badgeType)) {
            return; // 중복 수여 방지
        }

        // 배지 생성 및 저장
        Badge badge = Badge.create(user, badgeType);
        badgeRepository.save(badge);
    }

    /**
     * 모든 배지 타입에 대한 사용자의 현재 진행 상황을 반환
     * 조건 달성 시 자동으로 배지 부여
     * 
     * @param userId 사용자 ID
     * @return 배지 도전과제 목록
     */
    public List<BadgeChallengeResponse> getBadgeChallenges(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        List<Badge> acquiredBadges = badgeRepository.findByUserIdOrderByAcquiredAtDesc(userId);

        return Arrays.stream(BadgeType.values())
                .map(badgeType -> {
                    boolean isAcquired = acquiredBadges.stream()
                            .anyMatch(b -> b.getBadgeType() == badgeType);

                    long currentCount = 0;
                    long goalCount = 0;

                    switch (badgeType) {
                        case FIRST_GROUP_BUY:
                            currentCount = groupBuyRepository.countByHostIdAndStatus(
                                    user.getId(), 
                                    com.recipemate.global.common.GroupBuyStatus.RECRUITING);
                            currentCount += groupBuyRepository.countByHostIdAndStatus(
                                    user.getId(), 
                                    com.recipemate.global.common.GroupBuyStatus.COMPLETED);
                            goalCount = 1;
                            break;
                        case TEN_PARTICIPATIONS:
                            currentCount = participationRepository.countByUserId(user.getId());
                            goalCount = 10;
                            break;
                        case REVIEWER:
                            currentCount = reviewRepository.countByReviewerId(user.getId());
                            goalCount = 5;
                            break;
                        case POPULAR_HOST:
                            currentCount = Math.round(user.getMannerTemperature());
                            goalCount = 40;
                            break;
                    }

                    // 조건을 달성했지만 아직 배지를 획득하지 않은 경우 자동으로 배지 부여
                    if (!isAcquired && currentCount >= goalCount) {
                        checkAndAwardBadge(userId, badgeType);
                        isAcquired = true;
                    }

                    // 획득했으면 현재 카운트를 최소한 목표 카운트로 설정
                    if (isAcquired && currentCount < goalCount) {
                        currentCount = goalCount;
                    }

                    return BadgeChallengeResponse.builder()
                            .badgeType(badgeType)
                            .displayName(badgeType.getDisplayName())
                            .description(badgeType.getDescription())
                            .iconUrl(null) // 향후 확장을 위한 필드
                            .isAcquired(isAcquired)
                            .progress(String.format("%d/%d", Math.min(currentCount, goalCount), goalCount))
                            .currentCount(currentCount)
                            .goalCount(goalCount)
                            .build();
                })
                .collect(Collectors.toList());
    }
}
