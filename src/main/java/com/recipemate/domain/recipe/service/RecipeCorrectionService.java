package com.recipemate.domain.recipe.service;

import com.recipemate.domain.notification.service.NotificationService;
import com.recipemate.domain.recipe.dto.CorrectionProcessRequest;
import com.recipemate.domain.recipe.dto.RecipeCorrectionCreateRequest;
import com.recipemate.domain.recipe.dto.RecipeCorrectionResponse;
import com.recipemate.domain.recipe.entity.*;
import com.recipemate.domain.recipe.repository.RecipeCorrectionRepository;
import com.recipemate.domain.recipe.repository.RecipeRepository;
import com.recipemate.domain.user.entity.MannerTempHistory;
import com.recipemate.domain.user.entity.PointHistory;
import com.recipemate.domain.user.entity.User;
import com.recipemate.domain.user.repository.MannerTempHistoryRepository;
import com.recipemate.domain.user.repository.PointHistoryRepository;
import com.recipemate.domain.user.repository.UserRepository;
import com.recipemate.global.common.EntityType;
import com.recipemate.global.common.NotificationType;
import com.recipemate.global.common.PointType;
import com.recipemate.global.exception.CustomException;
import com.recipemate.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecipeCorrectionService {

    private final RecipeCorrectionRepository correctionRepository;
    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;
    private final MannerTempHistoryRepository mannerTempHistoryRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final NotificationService notificationService;

    private static final double CORRECTION_APPROVED_MANNER_TEMP = 0.5;
    private static final int CORRECTION_APPROVED_POINTS = 50;

    /**
     * 제안 생성
     */
    @Transactional
    public RecipeCorrectionResponse createCorrection(
            Long userId,
            Long recipeId,
            RecipeCorrectionCreateRequest request
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new CustomException(ErrorCode.RECIPE_NOT_FOUND));

        // 동일한 레시피에 대한 대기 중인 제안이 이미 있는지 확인
        long pendingCount = correctionRepository.countPendingByRecipeAndProposer(recipeId, userId);
        if (pendingCount > 0) {
            throw new CustomException(ErrorCode.DUPLICATE_CORRECTION_REQUEST);
        }

        RecipeCorrection correction = RecipeCorrection.create(
                recipe,
                user,
                request.getCorrectionType(),
                request.getProposedChange()
        );

        RecipeCorrection saved = correctionRepository.save(correction);
        return RecipeCorrectionResponse.from(saved);
    }

    /**
     * PENDING 상태의 모든 제안 조회 (관리자용)
     */
    public List<RecipeCorrectionResponse> getPendingCorrections() {
        return correctionRepository.findAllPending().stream()
                .map(RecipeCorrectionResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 제안 상세 조회
     */
    public RecipeCorrectionResponse getCorrection(Long correctionId) {
        RecipeCorrection correction = correctionRepository.findById(correctionId)
                .orElseThrow(() -> new CustomException(ErrorCode.CORRECTION_NOT_FOUND));
        return RecipeCorrectionResponse.from(correction);
    }

    /**
     * 특정 레시피의 제안 목록 조회
     */
    public List<RecipeCorrectionResponse> getCorrectionsByRecipe(Long recipeId) {
        return correctionRepository.findByRecipeId(recipeId).stream()
                .map(RecipeCorrectionResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 사용자의 제안 목록 조회
     */
    public Page<RecipeCorrectionResponse> getUserCorrections(Long userId, Pageable pageable) {
        return correctionRepository.findByProposerId(userId, pageable)
                .map(RecipeCorrectionResponse::from);
    }

    /**
     * 제안 승인
     */
    @Transactional
    public RecipeCorrectionResponse approveCorrection(
            Long adminId,
            Long correctionId,
            CorrectionProcessRequest request
    ) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        RecipeCorrection correction = correctionRepository.findById(correctionId)
                .orElseThrow(() -> new CustomException(ErrorCode.CORRECTION_NOT_FOUND));

        if (!correction.isPending()) {
            throw new CustomException(ErrorCode.ALREADY_PROCESSED_CORRECTION);
        }

        // 제안 승인 처리
        correction.approve(admin, request.getReason());

        // 제안자에게 보상 지급
        User proposer = correction.getProposer();
        rewardProposer(proposer, correction);

        // 알림 발송
        notificationService.createNotification(
                proposer.getId(),
                NotificationType.RECIPE_CORRECTION_APPROVED,
                null,
                correction.getId(),
                EntityType.RECIPE
        );

        return RecipeCorrectionResponse.from(correction);
    }

    /**
     * 제안 기각
     */
    @Transactional
    public RecipeCorrectionResponse rejectCorrection(
            Long adminId,
            Long correctionId,
            CorrectionProcessRequest request
    ) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        RecipeCorrection correction = correctionRepository.findById(correctionId)
                .orElseThrow(() -> new CustomException(ErrorCode.CORRECTION_NOT_FOUND));

        if (!correction.isPending()) {
            throw new CustomException(ErrorCode.ALREADY_PROCESSED_CORRECTION);
        }

        // 제안 기각 처리
        correction.reject(admin, request.getReason());

        // 알림 발송
        notificationService.createNotification(
                correction.getProposer().getId(),
                NotificationType.RECIPE_CORRECTION_REJECTED,
                null,
                correction.getId(),
                EntityType.RECIPE
        );

        return RecipeCorrectionResponse.from(correction);
    }

    /**
     * 제안자에게 보상 지급
     */
    private void rewardProposer(User proposer, RecipeCorrection correction) {
        double previousTemp = proposer.getMannerTemperature();

        // 매너온도 증가
        proposer.updateMannerTemperature(CORRECTION_APPROVED_MANNER_TEMP);

        // 매너온도 이력 저장
        MannerTempHistory mannerHistory = MannerTempHistory.create(
                proposer,
                CORRECTION_APPROVED_MANNER_TEMP,
                previousTemp,
                proposer.getMannerTemperature(),
                "레시피 개선 제안 승인",
                null
        );
        mannerTempHistoryRepository.save(mannerHistory);

        // 포인트 지급
        proposer.earnPoints(CORRECTION_APPROVED_POINTS);

        // 포인트 이력 저장
        PointHistory pointHistory = PointHistory.create(
                proposer,
                CORRECTION_APPROVED_POINTS,
                "레시피 개선 제안 승인 보상",
                PointType.RECIPE_CORRECTION
        );
        pointHistoryRepository.save(pointHistory);
    }
}
