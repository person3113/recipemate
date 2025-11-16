package com.recipemate.domain.search.service;

import com.recipemate.global.common.GroupBuyStatus;
import com.recipemate.domain.groupbuy.dto.GroupBuyResponse;
import com.recipemate.domain.groupbuy.entity.GroupBuy;
import com.recipemate.domain.groupbuy.entity.GroupBuyImage;
import com.recipemate.domain.groupbuy.repository.GroupBuyRepository;
import com.recipemate.domain.post.dto.PostResponse;
import com.recipemate.domain.post.entity.Post;
import com.recipemate.domain.post.repository.PostRepository;
import com.recipemate.domain.recipe.dto.RecipeListResponse;
import com.recipemate.domain.recipe.service.RecipeService;
import com.recipemate.domain.search.dto.SearchResultResponse;
import com.recipemate.domain.search.dto.UnifiedSearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 통합 검색 서비스
 * GroupBuy, Post, Recipe를 동시에 검색하여 결과를 통합 제공
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchService {

    private final GroupBuyRepository groupBuyRepository;
    private final PostRepository postRepository;
    private final RecipeService recipeService;

    private static final int MAX_PAGE_SIZE = 20;

    /**
     * 통합 검색
     * 모든 도메인(GroupBuy, Post, Recipe)을 동시에 검색
     * 
     * @param keyword 검색 키워드
     * @param pageable 페이지 정보
     * @return 통합 검색 결과
     */
    public UnifiedSearchResponse unifiedSearch(String keyword, Pageable pageable) {
        // 검색어 유효성 검증
        validateKeyword(keyword);
        
        // 페이지 크기 제한 적용
        Pageable limitedPageable = limitPageSize(pageable);
        
        log.info("통합 검색 시작 - keyword: {}, page: {}, size: {}", 
            keyword, limitedPageable.getPageNumber(), limitedPageable.getPageSize());

        // 각 도메인에서 병렬 검색
        List<SearchResultResponse> groupBuyResults = searchGroupBuys(keyword, limitedPageable);
        List<SearchResultResponse> postResults = searchPosts(keyword, limitedPageable);
        List<SearchResultResponse> recipeResults = searchRecipes(keyword);

        log.info("통합 검색 결과 - GroupBuy: {}, Post: {}, Recipe: {}", 
            groupBuyResults.size(), postResults.size(), recipeResults.size());

        // 결과 통합 및 반환
        return UnifiedSearchResponse.of(keyword, groupBuyResults, postResults, recipeResults);
    }

    /**
     * 공동구매 검색
     */
    private List<SearchResultResponse> searchGroupBuys(String keyword, Pageable pageable) {
        try {
            Page<GroupBuy> groupBuyPage = groupBuyRepository.searchByKeyword(
                keyword, 
                GroupBuyStatus.RECRUITING, 
                pageable
            );

            return groupBuyPage.getContent().stream()
                .map(this::convertGroupBuyToSearchResult)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("공동구매 검색 중 오류 발생", e);
            return new ArrayList<>();
        }
    }

    /**
     * 커뮤니티 게시글 검색
     */
    private List<SearchResultResponse> searchPosts(String keyword, Pageable pageable) {
        try {
            Page<Post> postPage = postRepository.searchByKeyword(keyword, pageable);

            return postPage.getContent().stream()
                .map(this::convertPostToSearchResult)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("게시글 검색 중 오류 발생", e);
            return new ArrayList<>();
        }
    }

    /**
     * 레시피 검색
     */
    private List<SearchResultResponse> searchRecipes(String keyword) {
        try {
            RecipeListResponse recipeResponse = recipeService.searchRecipes(keyword);

            return recipeResponse.getRecipes().stream()
                .map(this::convertRecipeToSearchResult)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("레시피 검색 중 오류 발생", e);
            return new ArrayList<>();
        }
    }

    /**
     * GroupBuy 엔티티를 SearchResultResponse로 변환
     */
    private SearchResultResponse convertGroupBuyToSearchResult(GroupBuy groupBuy) {
        // 이미지 URL 추출
        String imageUrl = null;
        if (groupBuy.getImages() != null && !groupBuy.getImages().isEmpty()) {
            imageUrl = groupBuy.getImages().stream()
                .findFirst()
                .map(GroupBuyImage::getImageUrl)
                .orElse(null);
        }

        // GroupBuyResponse 생성
        List<String> imageUrls = groupBuy.getImages() != null 
            ? groupBuy.getImages().stream()
                .map(GroupBuyImage::getImageUrl)
                .collect(Collectors.toList())
            : new ArrayList<>();

        GroupBuyResponse groupBuyResponse = GroupBuyResponse.from(groupBuy, imageUrls);
        
        return SearchResultResponse.fromGroupBuy(groupBuyResponse);
    }

    /**
     * Post 엔티티를 SearchResultResponse로 변환
     */
    private SearchResultResponse convertPostToSearchResult(Post post) {
        PostResponse postResponse = PostResponse.from(post);
        return SearchResultResponse.fromPost(postResponse);
    }

    /**
     * Recipe를 SearchResultResponse로 변환
     */
    private SearchResultResponse convertRecipeToSearchResult(RecipeListResponse.RecipeSimpleInfo recipe) {
        return SearchResultResponse.builder()
            .entityType(com.recipemate.global.common.EntityType.RECIPE)
            .apiId(recipe.getId())
            .title(recipe.getName())
            .content(recipe.getCategory())
            .imageUrl(recipe.getImageUrl())
            .build();
    }

    /**
     * 검색어 유효성 검증
     */
    private void validateKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("검색어를 입력해주세요");
        }
    }

    /**
     * 페이지 크기 제한 적용
     */
    private Pageable limitPageSize(Pageable pageable) {
        if (pageable.getPageSize() > MAX_PAGE_SIZE) {
            return PageRequest.of(
                pageable.getPageNumber(), 
                MAX_PAGE_SIZE, 
                pageable.getSort()
            );
        }
        return pageable;
    }
}
