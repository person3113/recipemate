package com.recipemate.domain.search.dto;

import com.recipemate.domain.groupbuy.dto.GroupBuyResponse;
import com.recipemate.domain.post.dto.PostResponse;
import com.recipemate.domain.recipe.dto.CookRecipeResponse;
import com.recipemate.global.common.EntityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 검색 결과 단일 항목 DTO
 * 각 엔티티 타입(GroupBuy, Post, Recipe)의 검색 결과를 담는 공통 응답 형식
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchResultResponse {

    private EntityType entityType;
    private Long id; // GroupBuy, Post의 경우 사용
    private String apiId; // Recipe의 경우 사용
    private String title;
    private String content;
    private String imageUrl;
    private String authorOrHost;
    private LocalDateTime createdAt;

    // GroupBuy용 정적 팩토리 메서드
    public static SearchResultResponse fromGroupBuy(GroupBuyResponse groupBuy) {
        return SearchResultResponse.builder()
                .entityType(EntityType.GROUP_BUY)
                .id(groupBuy.getId())
                .title(groupBuy.getTitle())
                .content(groupBuy.getContent())
                .imageUrl(groupBuy.getImageUrls() != null && !groupBuy.getImageUrls().isEmpty() 
                    ? groupBuy.getImageUrls().get(0) : null)
                .authorOrHost(groupBuy.getHostNickname())
                .createdAt(groupBuy.getCreatedAt())
                .build();
    }

    // Post용 정적 팩토리 메서드
    public static SearchResultResponse fromPost(PostResponse post) {
        return SearchResultResponse.builder()
                .entityType(EntityType.POST)
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .authorOrHost(post.getAuthorNickname())
                .createdAt(post.getCreatedAt())
                .build();
    }

    // Recipe용 정적 팩토리 메서드
    public static SearchResultResponse fromRecipe(CookRecipeResponse recipe) {
        return SearchResultResponse.builder()
                .entityType(EntityType.RECIPE)
                .apiId(recipe.getRcpSeq())
                .title(recipe.getRcpNm())
                .content(recipe.getRcpWay2() != null ? recipe.getRcpWay2() : recipe.getRcpPat2())
                .imageUrl(recipe.getAttFileNoMain())
                .build();
    }
}
