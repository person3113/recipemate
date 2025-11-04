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

@Service
@RequiredArgsConstructor
@Transactional
public class PointService {

    private final UserRepository userRepository;
    private final PointHistoryRepository pointHistoryRepository;

    /**
     * 포인트 적립
     *
     * @param userId 사용자 ID
     * @param amount 적립할 포인트
     * @param description 적립 사유
     */
    public void earnPoints(Long userId, int amount, String description) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        user.earnPoints(amount);

        PointHistory history = PointHistory.create(user, amount, description, PointType.EARN);
        pointHistoryRepository.save(history);
    }

    /**
     * 포인트 사용
     *
     * @param userId 사용자 ID
     * @param amount 사용할 포인트
     * @param description 사용 사유
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
     * 포인트 내역 조회
     *
     * @param userId 사용자 ID
     * @param pageable 페이징 정보
     * @return 포인트 내역 페이지
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
