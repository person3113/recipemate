package com.recipemate.domain.groupbuy.dto;

import com.recipemate.domain.groupbuy.entity.GroupBuy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * GroupBuy와 리뷰 통계를 함께 담는 DTO
 * N+1 문제 해결을 위해 QueryDSL Projection에서 사용
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupBuyWithReviewStatsDto {
    private GroupBuy groupBuy;
    private Double averageRating;
    private Long reviewCount;
    
    public int getReviewCountAsInt() {
        return reviewCount != null ? reviewCount.intValue() : 0;
    }
}
