package com.recipemate.domain.groupbuy.service;

import com.recipemate.domain.groupbuy.dto.ParticipantResponse;
import com.recipemate.domain.groupbuy.dto.ParticipateRequest;
import com.recipemate.domain.groupbuy.entity.GroupBuy;
import com.recipemate.domain.groupbuy.entity.Participation;
import com.recipemate.domain.groupbuy.repository.GroupBuyRepository;
import com.recipemate.domain.groupbuy.repository.ParticipationRepository;
import com.recipemate.domain.notification.service.NotificationService;
import com.recipemate.domain.user.entity.User;
import com.recipemate.domain.user.repository.UserRepository;
import com.recipemate.global.common.EntityType;
import com.recipemate.global.common.GroupBuyStatus;
import com.recipemate.global.common.NotificationType;
import com.recipemate.global.exception.CustomException;
import com.recipemate.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParticipationService {

    private final ParticipationRepository participationRepository;
    private final GroupBuyRepository groupBuyRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    @Retryable(
        retryFor = ObjectOptimisticLockingFailureException.class,
        maxAttempts = 3,
        backoff = @Backoff(delay = 100, multiplier = 2)
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

        // 11. 주최자에게 참여 알림 전송
        notificationService.createNotification(
            groupBuy.getHost().getId(),
            NotificationType.JOIN_GROUP_BUY,
            userId,
            groupBuyId,
            EntityType.GROUP_BUY
        );
    }

    @Transactional
    @Retryable(
        retryFor = ObjectOptimisticLockingFailureException.class,
        maxAttempts = 3,
        backoff = @Backoff(delay = 100, multiplier = 2)
    )
    public void cancelParticipation(Long userId, Long groupBuyId) {
        // 1. 참여 기록 조회
        Participation participation = participationRepository.findByUserIdAndGroupBuyId(userId, groupBuyId)
            .orElseThrow(() -> new CustomException(ErrorCode.PARTICIPATION_NOT_FOUND));

        // 2. 공구 조회
        GroupBuy groupBuy = groupBuyRepository.findById(groupBuyId)
            .orElseThrow(() -> new CustomException(ErrorCode.GROUP_BUY_NOT_FOUND));

        // 3. 마감 1일 전 취소 제한 검증
        LocalDateTime now = LocalDateTime.now();
        long hoursUntilDeadline = ChronoUnit.HOURS.between(now, groupBuy.getDeadline());
        if (hoursUntilDeadline < 24) {
            throw new CustomException(ErrorCode.CANCELLATION_DEADLINE_PASSED);
        }

        // 4. 참여 기록 삭제
        participationRepository.delete(participation);

        // 5. 공구 참여 인원 감소
        groupBuy.decreaseParticipant();

        // 6. CLOSED 상태였다면 RECRUITING으로 재개
        if (groupBuy.getStatus() == GroupBuyStatus.CLOSED && !groupBuy.isTargetReached()) {
            groupBuy.reopen();
        }

        // 7. 주최자에게 취소 알림 전송
        notificationService.createNotification(
            groupBuy.getHost().getId(),
            NotificationType.CANCEL_PARTICIPATION,
            userId,
            groupBuyId,
            EntityType.GROUP_BUY
        );
    }

    @Recover
    public void participate(ObjectOptimisticLockingFailureException e, Long userId, Long groupBuyId, ParticipateRequest request) {
        log.error("공구 참여 재시도 실패 - userId: {}, groupBuyId: {}, 최대 재시도 횟수 초과", userId, groupBuyId, e);
        throw new CustomException(ErrorCode.CONCURRENCY_FAILURE);
    }

    @Recover
    public void cancelParticipation(ObjectOptimisticLockingFailureException e, Long userId, Long groupBuyId) {
        log.error("공구 참여 취소 재시도 실패 - userId: {}, groupBuyId: {}, 최대 재시도 횟수 초과", userId, groupBuyId, e);
        throw new CustomException(ErrorCode.CONCURRENCY_FAILURE);
    }

    public List<ParticipantResponse> getParticipants(Long groupBuyId, Long currentUserId) {
        // 1. 공구 조회
        GroupBuy groupBuy = groupBuyRepository.findByIdWithHost(groupBuyId)
            .orElseThrow(() -> new CustomException(ErrorCode.GROUP_BUY_NOT_FOUND));

        // 2. 현재 사용자 조회
        User currentUser = userRepository.findById(currentUserId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 3. 접근 권한 검증
        if (!groupBuy.getParticipantListPublic() && !groupBuy.isHost(currentUser)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_PARTICIPANT_LIST_ACCESS);
        }

        // 4. 참여자 목록 조회 및 DTO 변환
        List<Participation> participations = participationRepository.findByGroupBuyIdWithUser(groupBuyId);
        return participations.stream()
            .map(ParticipantResponse::from)
            .collect(Collectors.toList());
    }
}
