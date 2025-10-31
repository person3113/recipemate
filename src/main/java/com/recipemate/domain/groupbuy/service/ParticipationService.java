package com.recipemate.domain.groupbuy.service;

import com.recipemate.domain.groupbuy.dto.ParticipateRequest;
import com.recipemate.domain.groupbuy.entity.GroupBuy;
import com.recipemate.domain.groupbuy.entity.Participation;
import com.recipemate.domain.groupbuy.repository.GroupBuyRepository;
import com.recipemate.domain.groupbuy.repository.ParticipationRepository;
import com.recipemate.domain.user.entity.User;
import com.recipemate.domain.user.repository.UserRepository;
import com.recipemate.global.common.GroupBuyStatus;
import com.recipemate.global.exception.CustomException;
import com.recipemate.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParticipationService {

    private final ParticipationRepository participationRepository;
    private final GroupBuyRepository groupBuyRepository;
    private final UserRepository userRepository;

    @Transactional
    @Retryable(
        retryFor = ObjectOptimisticLockingFailureException.class,
        maxAttempts = 3,
        backoff = @Backoff(delay = 100)
    )
    public void participate(Long userId, Long groupBuyId, ParticipateRequest request) {
        // 1. 사용자 조회
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2. 공구 조회
        GroupBuy groupBuy = groupBuyRepository.findByIdWithHost(groupBuyId)
            .orElseThrow(() -> new CustomException(ErrorCode.GROUP_BUY_NOT_FOUND));

        // 3. 공구 상태 검증 (RECRUITING 상태가 아니면 참여 불가)
        if (groupBuy.getStatus() != GroupBuyStatus.RECRUITING) {
            throw new CustomException(ErrorCode.GROUP_BUY_CLOSED);
        }

        // 4. 주최자는 자신의 공구에 참여 불가
        if (groupBuy.isHost(user)) {
            throw new CustomException(ErrorCode.HOST_CANNOT_PARTICIPATE);
        }

        // 5. 중복 참여 체크
        if (participationRepository.existsByUserIdAndGroupBuyId(userId, groupBuyId)) {
            throw new CustomException(ErrorCode.ALREADY_PARTICIPATED);
        }

        // 6. 목표 인원 도달 체크
        if (groupBuy.isTargetReached()) {
            throw new CustomException(ErrorCode.MAX_PARTICIPANTS_EXCEEDED);
        }

        // 7. 참여 엔티티 생성 (도메인 로직에서 수령 방법 호환성 검증)
        Participation participation = Participation.create(
            user,
            groupBuy,
            request.getQuantity(),
            request.getSelectedDeliveryMethod()
        );

        // 8. 참여 저장
        participationRepository.save(participation);

        // 9. 공구 참여 인원 증가
        groupBuy.increaseParticipant();

        // 10. 목표 인원 도달 시 공구 상태를 CLOSED로 변경
        if (groupBuy.isTargetReached()) {
            groupBuy.close();
        }
    }
}
