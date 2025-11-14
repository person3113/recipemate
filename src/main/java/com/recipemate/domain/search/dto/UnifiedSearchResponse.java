package com.recipemate.domain.search.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 통합 검색 응답 DTO
 * GroupBuy, Post, Recipe의 검색 결과를 하나의 응답으로 통합
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnifiedSearchResponse {

    private String query;
    private List<SearchResultResponse> groupBuys;
    private List<SearchResultResponse> posts;
    private List<SearchResultResponse> recipes;
    private Integer totalResults;
    
    // 각 카테고리의 전체 개수 (전체 탭에서 배지 표시용)
    private Long totalRecipeCount;
    private Long totalGroupBuyCount;
    private Long totalPostCount;

    public static UnifiedSearchResponse of(
            String query,
            List<SearchResultResponse> groupBuys,
            List<SearchResultResponse> posts,
            List<SearchResultResponse> recipes) {
        
        int total = groupBuys.size() + posts.size() + recipes.size();
        
        return UnifiedSearchResponse.builder()
                .query(query)
                .groupBuys(groupBuys)
                .posts(posts)
                .recipes(recipes)
                .totalResults(total)
                .build();
    }
    
    public static UnifiedSearchResponse ofWithCounts(
            String query,
            List<SearchResultResponse> groupBuys,
            List<SearchResultResponse> posts,
            List<SearchResultResponse> recipes,
            Long totalRecipeCount,
            Long totalGroupBuyCount,
            Long totalPostCount) {
        
        int total = groupBuys.size() + posts.size() + recipes.size();
        
        return UnifiedSearchResponse.builder()
                .query(query)
                .groupBuys(groupBuys)
                .posts(posts)
                .recipes(recipes)
                .totalResults(total)
                .totalRecipeCount(totalRecipeCount)
                .totalGroupBuyCount(totalGroupBuyCount)
                .totalPostCount(totalPostCount)
                .build();
    }
}
