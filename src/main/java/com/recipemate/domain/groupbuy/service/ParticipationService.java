package com.recipemate.domain.groupbuy.service;

import com.recipemate.domain.groupbuy.dto.ParticipantResponse;
import com.recipemate.domain.groupbuy.dto.ParticipateRequest;
import com.recipemate.domain.groupbuy.entity.GroupBuy;
import com.recipemate.domain.groupbuy.entity.Participation;
import com.recipemate.domain.groupbuy.repository.GroupBuyRepository;
import com.recipemate.domain.groupbuy.repository.ParticipationRepository;
import com.recipemate.domain.user.entity.Address;
import com.recipemate.domain.user.entity.User;
import com.recipemate.domain.user.repository.AddressRepository;
import com.recipemate.domain.user.repository.UserRepository;
import com.recipemate.domain.user.service.PointService;
import com.recipemate.global.common.DeliveryMethod;
import com.recipemate.global.common.GroupBuyStatus;
import com.recipemate.global.event.GroupBuyCompletedEvent;
import com.recipemate.global.event.ParticipationCancelledEvent;
import com.recipemate.global.event.ParticipationCreatedEvent;
import com.recipemate.global.exception.CustomException;
import com.recipemate.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
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
    private final AddressRepository addressRepository;
    private final PointService pointService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    @Retryable(
        retryFor = ObjectOptimisticLockingFailureException.class,
        noRetryFor = CustomException.class,
        maxAttempts = 3,
        backoff = @Backoff(delay = 100, multiplier = 2)
    )
    public void participate(Long userId, Long groupBuyId, ParticipateRequest request) {
        // 1. 엔티티 조회
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        GroupBuy groupBuy = groupBuyRepository.findByIdWithHost(groupBuyId)
            .orElseThrow(() -> new CustomException(ErrorCode.GROUP_BUY_NOT_FOUND));

        // 2. 중복 참여 체크
        if (participationRepository.existsByUserIdAndGroupBuyId(userId, groupBuyId)) {
            throw new CustomException(ErrorCode.ALREADY_PARTICIPATED);
        }

        // 3. 배송지 검증 (택배 선택 시)
        Address address = null;
        if (request.getSelectedDeliveryMethod() == DeliveryMethod.PARCEL) {
            if (request.getAddressId() == null) {
                throw new CustomException(ErrorCode.ADDRESS_REQUIRED_FOR_PARCEL);
            }
            address = addressRepository.findByUserIdAndAddressId(userId, request.getAddressId())
                .orElseThrow(() -> new CustomException(ErrorCode.ADDRESS_NOT_FOUND));
        }

        // 4. 포인트 결제 처리
        pointService.usePoints(userId, request.getTotalPayment(), 
            String.format("공동구매 참여 (공구 ID: %d)", groupBuyId));

        // 5. 도메인 엔티티에 참여 위임
        Participation participation = groupBuy.addParticipant(
            user,
            request.getQuantity(),
            request.getSelectedDeliveryMethod(),
            address,
            request.getTotalPayment()
        );

        // 6. 참여 정보 저장
        participationRepository.save(participation);

        // 7. 참여 생성 관련 이벤트 발행 (알림, 포인트, 뱃지 등)
        eventPublisher.publishEvent(new ParticipationCreatedEvent(userId, groupBuyId));
        
        // 8. 목표 인원 달성 시 알림 이벤트 발행
        if (groupBuy.getCurrentHeadcount() >= groupBuy.getTargetHeadcount()) {
            eventPublisher.publishEvent(new GroupBuyCompletedEvent(groupBuyId));
        }
    }

    @Transactional
    @Retryable(
        retryFor = ObjectOptimisticLockingFailureException.class,
        noRetryFor = CustomException.class,
        maxAttempts = 3,
        backoff = @Backoff(delay = 100, multiplier = 2)
    )
    public void cancelParticipation(Long userId, Long groupBuyId) {
        // 1. 참여 기록 조회 (GroupBuy를 함께 fetch)
        Participation participation = participationRepository.findByUserIdAndGroupBuyIdWithGroupBuy(userId, groupBuyId)
            .orElseThrow(() -> new CustomException(ErrorCode.PARTICIPATION_NOT_FOUND));

        GroupBuy groupBuy = participation.getGroupBuy();

        // 2. 도메인 엔티티에 참여 취소 위임
        groupBuy.cancelParticipation(participation);

        // 3. 포인트 환불 처리 (전액 환불: 재료비 + 택배비)
        pointService.refundPoints(userId, participation.getTotalPayment(),
            String.format("공동구매 참여 취소 (공구 ID: %d)", groupBuyId));

        // 4. 공구 금액 차감 (택배비 제외한 재료비만 차감)
        Integer itemAmount = calculateItemAmountFromParticipation(groupBuy, participation);
        groupBuy.decreaseCurrentAmount(itemAmount);

        // 5. 참여 취소 관련 이벤트 발행 (알림 등)
        eventPublisher.publishEvent(new ParticipationCancelledEvent(userId, groupBuyId));
    }

    @Transactional
    @Retryable(
        retryFor = ObjectOptimisticLockingFailureException.class,
        noRetryFor = CustomException.class,
        maxAttempts = 3,
        backoff = @Backoff(delay = 100, multiplier = 2)
    )
    public void forceRemoveParticipant(Long hostId, Long groupBuyId, Long participantUserId) {
        // 1. 공구 조회 (주최자 정보 포함)
        GroupBuy groupBuy = groupBuyRepository.findByIdWithHost(groupBuyId)
            .orElseThrow(() -> new CustomException(ErrorCode.GROUP_BUY_NOT_FOUND));

        // 2. 주최자 확인
        User host = userRepository.findById(hostId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!groupBuy.isHost(host)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_GROUP_BUY_ACCESS);
        }

        // 3. 참여자 조회
        Participation participation = participationRepository.findByUserIdAndGroupBuyIdWithGroupBuy(participantUserId, groupBuyId)
            .orElseThrow(() -> new CustomException(ErrorCode.PARTICIPATION_NOT_FOUND));

        // 4. 강제 탈퇴 (마감일 제한 없음)
        groupBuy.forceRemoveParticipant(participation);

        // 5. 포인트 환불 처리
        pointService.refundPoints(participantUserId, participation.getTotalPayment(),
            String.format("공동구매 강제 탈퇴 (공구 ID: %d)", groupBuyId));

        // 6. 공구 금액 차감 (택배비 제외한 재료비만 차감)
        Integer itemAmount = calculateItemAmountFromParticipation(groupBuy, participation);
        groupBuy.decreaseCurrentAmount(itemAmount);

        // 7. 강제 탈퇴 이벤트 발행 (알림 등)
        eventPublisher.publishEvent(new ParticipationCancelledEvent(participantUserId, groupBuyId));
        log.info("강제 탈퇴 완료 - hostId: {}, groupBuyId: {}, participantUserId: {}", hostId, groupBuyId, participantUserId);
    }

    /**
     * 참여 정보로부터 재료비(item amount)만 계산합니다.
     * 택배 배송인 경우 총 결제금액에서 택배비를 제외한 금액을 반환합니다.
     * 
     * @param groupBuy 공동구매 엔티티
     * @param participation 참여 정보
     * @return 재료비만 포함된 금액 (택배비 제외)
     */
    private Integer calculateItemAmountFromParticipation(GroupBuy groupBuy, Participation participation) {
        if (participation.getSelectedDeliveryMethod() == DeliveryMethod.PARCEL 
            && groupBuy.getParcelFee() != null && groupBuy.getParcelFee() > 0) {
            return participation.getTotalPayment() - groupBuy.getParcelFee();
        }
        return participation.getTotalPayment();
    }

    @Recover
    public void participate(ObjectOptimisticLockingFailureException e, Long userId, Long groupBuyId, ParticipateRequest request) {
        log.error("공구 참여 재시도 실패 - userId: {}, groupBuyId: {}, 최대 재시도 횟수 초과", userId, groupBuyId, e);
        throw new CustomException(ErrorCode.CONCURRENCY_FAILURE);
    }

    @Recover
    public void participate(CustomException e, Long userId, Long groupBuyId, ParticipateRequest request) {
        log.debug("공구 참여 비즈니스 로직 실패 - userId: {}, groupBuyId: {}, error: {}", userId, groupBuyId, e.getMessage());
        throw e; // 비즈니스 예외는 그대로 전파
    }

    @Recover
    public void cancelParticipation(ObjectOptimisticLockingFailureException e, Long userId, Long groupBuyId) {
        log.error("공구 참여 취소 재시도 실패 - userId: {}, groupBuyId: {}, 최대 재시도 횟수 초과", userId, groupBuyId, e);
        throw new CustomException(ErrorCode.CONCURRENCY_FAILURE);
    }

    @Recover
    public void cancelParticipation(CustomException e, Long userId, Long groupBuyId) {
        log.debug("공구 참여 취소 비즈니스 로직 실패 - userId: {}, groupBuyId: {}, error: {}", userId, groupBuyId, e.getMessage());
        throw e; // 비즈니스 예외는 그대로 전파
    }

    @Recover
    public void forceRemoveParticipant(ObjectOptimisticLockingFailureException e, Long hostId, Long groupBuyId, Long participantUserId) {
        log.error("강제 탈퇴 재시도 실패 - hostId: {}, groupBuyId: {}, participantUserId: {}, 최대 재시도 횟수 초과", 
            hostId, groupBuyId, participantUserId, e);
        throw new CustomException(ErrorCode.CONCURRENCY_FAILURE);
    }

    @Recover
    public void forceRemoveParticipant(CustomException e, Long hostId, Long groupBuyId, Long participantUserId) {
        log.debug("강제 탈퇴 비즈니스 로직 실패 - hostId: {}, groupBuyId: {}, participantUserId: {}, error: {}", 
            hostId, groupBuyId, participantUserId, e.getMessage());
        throw e; // 비즈니스 예외는 그대로 전파
    }

    public List<ParticipantResponse> getParticipants(Long groupBuyId, Long currentUserId) {
        // 1. 공구 조회 (주최자 정보 포함)
        GroupBuy groupBuy = groupBuyRepository.findByIdWithHost(groupBuyId)
            .orElseThrow(() -> new CustomException(ErrorCode.GROUP_BUY_NOT_FOUND));

        // 2. 비공개 목록인 경우에만 사용자 인증 및 권한 검증
        if (!groupBuy.getParticipantListPublic()) {
            // 비로그인 사용자는 비공개 목록 접근 불가
            if (currentUserId == null) {
                throw new CustomException(ErrorCode.UNAUTHORIZED_PARTICIPANT_LIST_ACCESS);
            }
            
            // 로그인한 사용자가 주최자가 아니면 비공개 목록 접근 불가
            User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
            
            if (!groupBuy.isHost(currentUser)) {
                throw new CustomException(ErrorCode.UNAUTHORIZED_PARTICIPANT_LIST_ACCESS);
            }
        }

        // 3. 참여자 목록 조회 및 DTO 변환 (주최자 ID 전달)
        Long hostId = groupBuy.getHost().getId();
        List<Participation> participations = participationRepository.findByGroupBuyIdWithUser(groupBuyId);
        return participations.stream()
            .map(p -> ParticipantResponse.from(p, hostId))
            .collect(Collectors.toList());
    }
}
