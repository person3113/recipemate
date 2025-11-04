package com.recipemate.domain.badge.service;

import com.recipemate.domain.badge.dto.BadgeResponse;
import com.recipemate.domain.badge.entity.Badge;
import com.recipemate.domain.badge.repository.BadgeRepository;
import com.recipemate.domain.user.entity.User;
import com.recipemate.domain.user.repository.UserRepository;
import com.recipemate.global.common.BadgeType;
import com.recipemate.global.exception.CustomException;
import com.recipemate.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BadgeService {

    private final BadgeRepository badgeRepository;
    private final UserRepository userRepository;

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
     * 사용자 배지 목록 조회
     * 
     * @param userId 사용자 ID
     * @return 배지 목록
     */
    @Transactional(readOnly = true)
    public List<BadgeResponse> getUserBadges(Long userId) {
        List<Badge> badges = badgeRepository.findByUserIdOrderByAcquiredAtDesc(userId);
        return badges.stream()
                .map(BadgeResponse::from)
                .collect(Collectors.toList());
    }
}
