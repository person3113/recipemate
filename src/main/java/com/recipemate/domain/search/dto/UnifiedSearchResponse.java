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
}
