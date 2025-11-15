package com.recipemate.domain.user.service;

import com.recipemate.domain.user.dto.PointHistoryResponse;
import com.recipemate.domain.user.entity.PointHistory;
import com.recipemate.domain.user.entity.User;
import com.recipemate.domain.user.repository.PointHistoryRepository;
import com.recipemate.domain.user.repository.UserRepository;
import com.recipemate.global.common.PointType;
import com.recipemate.global.exception.CustomException;
import com.recipemate.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
@Transactional
public class PointService {

    private final UserRepository userRepository;
    private final PointHistoryRepository pointHistoryRepository;

    /**
     * 포인트 적립 (하루 1회 제한)
     */
    public void earnPoints(Long userId, int amount, String description) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (hasEarnedPointsToday(user, description)) {
            return;
        }

        user.earnPoints(amount);

        PointHistory history = PointHistory.create(user, amount, description, PointType.EARN);
        pointHistoryRepository.save(history);
    }

    /**
     * 출석 체크 포인트 적립
     */
    public void dailyCheckIn(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String description = "출석 체크";
        
        if (hasEarnedPointsToday(user, description)) {
            throw new CustomException(ErrorCode.ALREADY_CHECKED_IN_TODAY);
        }

        user.earnPoints(5);

        PointHistory history = PointHistory.create(user, 5, description, PointType.EARN);
        pointHistoryRepository.save(history);
    }

    /**
     * 오늘 이미 특정 사유로 포인트를 획득했는지 확인
     */
    private boolean hasEarnedPointsToday(User user, String description) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        
        return pointHistoryRepository.existsByUserAndDescriptionAndCreatedAtBetween(
                user, description, startOfDay, endOfDay
        );
    }

    /**
     * 포인트 사용
     */
    public void usePoints(Long userId, int amount, String description) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        boolean success = user.usePoints(amount);
        if (!success) {
            throw new CustomException(ErrorCode.INSUFFICIENT_POINTS);
        }

        PointHistory history = PointHistory.create(user, amount, description, PointType.USE);
        pointHistoryRepository.save(history);
    }

    /**
     * 포인트 충전
     */
    public void chargePoints(Long userId, int amount, String description) {
        if (amount <= 0) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        user.earnPoints(amount);

        PointHistory history = PointHistory.create(user, amount, description, PointType.CHARGE);
        pointHistoryRepository.save(history);
    }

    /**
     * 포인트 환불
     */
    public void refundPoints(Long userId, int amount, String description) {
        if (amount <= 0) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        user.earnPoints(amount);

        PointHistory history = PointHistory.create(user, amount, description, PointType.REFUND);
        pointHistoryRepository.save(history);
    }

    /**
     * 포인트 내역 조회
     */
    @Transactional(readOnly = true)
    public Page<PointHistoryResponse> getPointHistory(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Page<PointHistory> historyPage = pointHistoryRepository
                .findByUserOrderByCreatedAtDesc(user, pageable);

        return historyPage.map(PointHistoryResponse::from);
    }
}
