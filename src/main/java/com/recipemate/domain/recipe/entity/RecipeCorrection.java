package com.recipemate.domain.recipe.entity;

import com.recipemate.domain.user.entity.User;
import com.recipemate.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 레시피 오류 신고 및 개선 제안 엔티티
 */
@Entity
@Table(name = "recipe_corrections", indexes = {
        @Index(name = "idx_recipe_correction_status", columnList = "status"),
        @Index(name = "idx_recipe_correction_recipe_id", columnList = "recipe_id"),
        @Index(name = "idx_recipe_correction_proposer_id", columnList = "proposer_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@SuperBuilder
public class RecipeCorrection extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 대상 레시피
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    /**
     * 제안자
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposer_id", nullable = false)
    private User proposer;

    /**
     * 제안 종류
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CorrectionType correctionType;

    /**
     * 제안 내용
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String proposedChange;

    /**
     * 처리 상태
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CorrectionStatus status;

    /**
     * 관리자 처리 사유
     */
    @Column(columnDefinition = "TEXT")
    private String adminReason;

    /**
     * 처리한 관리자
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolver_id")
    private User resolver;

    /**
     * 제안 생성
     */
    public static RecipeCorrection create(
            Recipe recipe,
            User proposer,
            CorrectionType correctionType,
            String proposedChange
    ) {
        return RecipeCorrection.builder()
                .recipe(recipe)
                .proposer(proposer)
                .correctionType(correctionType)
                .proposedChange(proposedChange)
                .status(CorrectionStatus.PENDING)
                .build();
    }

    /**
     * 제안 승인
     */
    public void approve(User admin, String reason) {
        this.status = CorrectionStatus.APPROVED;
        this.adminReason = reason;
        this.resolver = admin;
    }

    /**
     * 제안 기각
     */
    public void reject(User admin, String reason) {
        this.status = CorrectionStatus.REJECTED;
        this.adminReason = reason;
        this.resolver = admin;
    }

    /**
     * 처리 대기 중인지 확인
     */
    public boolean isPending() {
        return this.status == CorrectionStatus.PENDING;
    }

    /**
     * 승인되었는지 확인
     */
    public boolean isApproved() {
        return this.status == CorrectionStatus.APPROVED;
    }

    /**
     * 기각되었는지 확인
     */
    public boolean isRejected() {
        return this.status == CorrectionStatus.REJECTED;
    }
}
