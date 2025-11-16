package com.recipemate.domain.review.entity;

import com.recipemate.domain.groupbuy.entity.GroupBuy;
import com.recipemate.domain.user.entity.User;
import com.recipemate.global.common.BaseEntity;
import com.recipemate.global.common.GroupBuyStatus;
import com.recipemate.global.exception.CustomException;
import com.recipemate.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "reviews",
    indexes = {
        @Index(name = "idx_review_group_buy_id", columnList = "group_buy_id"),
        @Index(name = "idx_review_reviewer_id", columnList = "reviewer_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_review_reviewer_groupbuy", columnNames = {"reviewer_id", "group_buy_id"})
    }
)
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false, foreignKey = @ForeignKey(name = "fk_review_reviewer"))
    private User reviewer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_buy_id", nullable = false, foreignKey = @ForeignKey(name = "fk_review_groupbuy"))
    private GroupBuy groupBuy;

    @Column(nullable = false)
    private Integer rating;

    @Column(length = 500)
    private String content;

    //== 생성 메서드 ==//
    public static Review create(User reviewer, GroupBuy groupBuy, Integer rating, String content) {
        validateCreateArgs(groupBuy, rating);

        return Review.builder()
                .reviewer(reviewer)
                .groupBuy(groupBuy)
                .rating(rating)
                .content(content)
                .build();
    }

    private static void validateCreateArgs(GroupBuy groupBuy, Integer rating) {
        // 별점 유효성 검증
        if (rating == null || rating < 1 || rating > 5) {
            throw new CustomException(ErrorCode.INVALID_RATING);
        }

        // 공구 상태 검증: COMPLETED 상태만 후기 작성 가능
        if (groupBuy.getStatus() != GroupBuyStatus.COMPLETED) {
            throw new CustomException(ErrorCode.GROUP_BUY_NOT_COMPLETED);
        }
    }

    //== 수정 메서드 ==//
    /**
     * 후기 수정
     * @return 수정 전 별점 (매너온도 재계산용)
     */
    public Integer update(Integer rating, String content) {
        Integer oldRating = this.rating;
        
        if (rating != null) {
            if (rating < 1 || rating > 5) {
                throw new CustomException(ErrorCode.INVALID_RATING);
            }
            this.rating = rating;
        }
        if (content != null) {
            this.content = content;
        }
        
        return oldRating;
    }

    //== 비즈니스 로직 ==//
    /**
     * 별점에 따른 매너온도 변화량 계산
     * 5점: +0.5, 4점: +0.2, 3점: 0, 2점: -0.2, 1점: -0.5
     */
    public double calculateMannerTemperatureDelta() {
        return switch (rating) {
            case 5 -> 0.5;
            case 4 -> 0.2;
            case 3 -> 0.0;
            case 2 -> -0.2;
            case 1 -> -0.5;
            default -> throw new CustomException(ErrorCode.INVALID_RATING);
        };
    }

    public boolean isReviewedBy(User user) {
        return this.reviewer.equals(user);
    }

    //== 삭제 메서드 ==//
    /**
     * 소프트 삭제
     */
    public void softDelete() {
        this.delete();
    }
}
