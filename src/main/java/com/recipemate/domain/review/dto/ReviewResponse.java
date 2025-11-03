package com.recipemate.domain.review.dto;

import com.recipemate.domain.review.entity.Review;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ReviewResponse {

    private Long id;
    private Integer rating;
    private String content;

    // 작성자 정보
    private Long reviewerId;
    private String reviewerNickname;

    // 공구 정보
    private Long groupBuyId;
    private String groupBuyTitle;

    // 타임스탬프
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 정적 팩토리 메서드
    public static ReviewResponse from(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .rating(review.getRating())
                .content(review.getContent())
                .reviewerId(review.getReviewer().getId())
                .reviewerNickname(review.getReviewer().getNickname())
                .groupBuyId(review.getGroupBuy().getId())
                .groupBuyTitle(review.getGroupBuy().getTitle())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
