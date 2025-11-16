package com.recipemate.domain.review.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 리뷰 통계 정보를 담는 DTO
 * N+1 문제 해결을 위해 GROUP BY 쿼리 결과를 매핑
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewStatsDto {
    private Long groupBuyId;
    private Double averageRating;
    private Long reviewCount;
}
