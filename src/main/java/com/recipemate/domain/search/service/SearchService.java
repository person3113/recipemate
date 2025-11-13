package com.recipemate.domain.search.service;

import com.recipemate.domain.groupbuy.dto.GroupBuyResponse;
import com.recipemate.domain.groupbuy.entity.GroupBuy;
import com.recipemate.domain.groupbuy.entity.GroupBuyImage;
import com.recipemate.domain.groupbuy.repository.GroupBuyRepository;
import com.recipemate.domain.post.dto.PostWithCountsDto;
import com.recipemate.domain.post.dto.PostResponse;
import com.recipemate.domain.post.entity.Post;
import com.recipemate.domain.post.repository.PostRepository;
import com.recipemate.domain.recipe.dto.RecipeListResponse;
import com.recipemate.domain.recipe.service.RecipeService;
import com.recipemate.domain.search.dto.SearchResultResponse;
import com.recipemate.domain.search.dto.UnifiedSearchResponse;
import com.recipemate.domain.search.entity.SearchKeyword;
import com.recipemate.domain.search.repository.SearchKeywordRepository;
import com.recipemate.global.common.GroupBuyStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
    private final SearchKeywordRepository searchKeywordRepository;

    private static final int MAX_PAGE_SIZE = 20;

    /**
     * 통합 검색
     * 모든 도메인(GroupBuy, Post, Recipe)을 동시에 검색
     * 
     * @param keyword 검색 키워드
     * @param type 검색 타입 (ALL, RECIPE, GROUP_BUY, POST)
     * @param pageable 페이지 정보
     * @return 통합 검색 결과
     */
    public UnifiedSearchResponse unifiedSearch(String keyword, String type, Pageable pageable) {
        // 검색어 유효성 검증
        validateKeyword(keyword);
        
        // type이 null이면 ALL로 기본 설정
        String searchType = (type == null || type.trim().isEmpty()) ? "ALL" : type.toUpperCase();
        
        log.info("통합 검색 시작 - keyword: {}, type: {}, page: {}, size: {}", 
            keyword, searchType, pageable.getPageNumber(), pageable.getPageSize());

        // 검색 키워드 비동기 저장 (인기 검색어 집계용)
        saveSearchKeywordAsync(keyword);

        List<SearchResultResponse> groupBuyResults;
        List<SearchResultResponse> postResults;
        List<SearchResultResponse> recipeResults;

        // type에 따라 검색 범위 결정
        if ("ALL".equals(searchType)) {
            // 전체 탭: 각 도메인별 상위 5개씩만 조회하되, 전체 개수도 계산
            Pageable previewPageable = PageRequest.of(0, 5);
            groupBuyResults = searchGroupBuys(keyword, previewPageable);
            postResults = searchPosts(keyword, previewPageable);
            recipeResults = searchRecipes(keyword, previewPageable);
            
            // 각 카테고리의 전체 개수 계산
            Long totalRecipeCount = countRecipes(keyword);
            Long totalGroupBuyCount = countGroupBuys(keyword);
            Long totalPostCount = countPosts(keyword);
            
            log.info("통합 검색 결과 - GroupBuy: {}/{}, Post: {}/{}, Recipe: {}/{}", 
                groupBuyResults.size(), totalGroupBuyCount,
                postResults.size(), totalPostCount,
                recipeResults.size(), totalRecipeCount);
            
            return UnifiedSearchResponse.ofWithCounts(
                keyword, 
                groupBuyResults, 
                postResults, 
                recipeResults,
                totalRecipeCount,
                totalGroupBuyCount,
                totalPostCount
            );
        } else if ("RECIPE".equals(searchType)) {
            // 레시피 탭: 레시피만 페이징하여 전체 조회
            groupBuyResults = new ArrayList<>();
            postResults = new ArrayList<>();
            recipeResults = searchRecipes(keyword, pageable);
        } else if ("GROUP_BUY".equals(searchType)) {
            // 공구 탭: 공구만 페이징하여 전체 조회
            Pageable limitedPageable = limitPageSize(pageable);
            groupBuyResults = searchGroupBuys(keyword, limitedPageable);
            postResults = new ArrayList<>();
            recipeResults = new ArrayList<>();
        } else if ("POST".equals(searchType)) {
            // 커뮤니티 탭: 게시글만 페이징하여 전체 조회
            Pageable limitedPageable = limitPageSize(pageable);
            groupBuyResults = new ArrayList<>();
            postResults = searchPosts(keyword, limitedPageable);
            recipeResults = new ArrayList<>();
        } else {
            throw new IllegalArgumentException("유효하지 않은 검색 타입입니다: " + searchType);
        }

        log.info("통합 검색 결과 - GroupBuy: {}, Post: {}, Recipe: {}", 
            groupBuyResults.size(), postResults.size(), recipeResults.size());

        // 결과 통합 및 반환
        return UnifiedSearchResponse.of(keyword, groupBuyResults, postResults, recipeResults);
    }

    /**
     * 공동구매 검색 (List 반환 - 전체 탭용)
     */
    private List<SearchResultResponse> searchGroupBuys(String keyword, Pageable pageable) {
        try {
            Page<GroupBuy> groupBuyPage = groupBuyRepository.searchByKeyword(
                keyword, 
                GroupBuyStatus.RECRUITING, 
                pageable
            );

            return groupBuyPage.getContent().stream()
                .map(groupBuy -> convertGroupBuyToSearchResult(groupBuy, keyword))
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("공동구매 검색 중 오류 발생", e);
            return new ArrayList<>();
        }
    }

    /**
     * 공동구매 검색 (Page 반환 - 개별 탭용)
     */
    public Page<SearchResultResponse> searchGroupBuysPage(String keyword, Pageable pageable) {
        Page<GroupBuy> groupBuyPage = groupBuyRepository.searchByKeyword(
            keyword, 
            GroupBuyStatus.RECRUITING, 
            pageable
        );
        return groupBuyPage.map(groupBuy -> convertGroupBuyToSearchResult(groupBuy, keyword));
    }

    /**
     * 커뮤니티 게시글 검색 (List 반환 - 전체 탭용)
     */
    private List<SearchResultResponse> searchPosts(String keyword, Pageable pageable) {
        try {
            Page<PostWithCountsDto> postPage = postRepository.searchByKeywordWithCounts(keyword, pageable);

            return postPage.getContent().stream()
                .map(dto -> convertPostToSearchResult(dto, keyword))
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("게시글 검색 중 오류 발생", e);
            return new ArrayList<>();
        }
    }

    /**
     * 커뮤니티 게시글 검색 (Page 반환 - 개별 탭용)
     */
    public Page<SearchResultResponse> searchPostsPage(String keyword, Pageable pageable) {
        Page<PostWithCountsDto> postPage = postRepository.searchByKeywordWithCounts(keyword, pageable);
        return postPage.map(dto -> convertPostToSearchResult(dto, keyword));
    }
    
    /**
     * Post를 SearchResultResponse로 변환
     */
    private SearchResultResponse convertPostToSearchResult(PostWithCountsDto dto, String keyword) {
        Post post = dto.getPost();
        
        // Post 엔티티는 이미지를 직접 가지지 않으므로 imageUrl은 null
        String imageUrl = null;
        
        return SearchResultResponse.builder()
            .entityType(com.recipemate.global.common.EntityType.POST)
            .id(post.getId())
            .title(post.getTitle())
            .highlightedTitle(highlightKeyword(post.getTitle(), keyword))
            .content(post.getContent())
            .imageUrl(imageUrl)
            .authorOrHost(post.getAuthor().getNickname())
            .authorNickname(post.getAuthor().getNickname())
            .createdAt(post.getCreatedAt())
            .viewCount(post.getViewCount())
            .build();
    }

    /**
     * 레시피 검색 (List 반환 - 전체 탭용)
     * RecipeService의 findRecipes 메소드를 사용하여 페이징 지원
     */
    private List<SearchResultResponse> searchRecipes(String keyword, Pageable pageable) {
        try {
            // RecipeService의 findRecipes 메소드 호출 (페이징 지원)
            RecipeListResponse recipeResponse = recipeService.findRecipes(
                keyword,    // keyword
                null,       // ingredients
                null,       // category
                "latest",   // sort
                "desc",     // direction
                pageable    // pageable
            );

            return recipeResponse.getRecipes().stream()
                .map(recipe -> convertRecipeToSearchResult(recipe, keyword))
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("레시피 검색 중 오류 발생", e);
            return new ArrayList<>();
        }
    }

    /**
     * 레시피 검색 (Page 반환 - 개별 탭용)
     */
    public Page<SearchResultResponse> searchRecipesPage(String keyword, Pageable pageable) {
        RecipeListResponse recipeResponse = recipeService.findRecipes(
            keyword,    // keyword
            null,       // ingredients
            null,       // category
            "latest",   // sort
            "desc",     // direction
            pageable    // pageable
        );
        
        // RecipeListResponse의 Page 정보를 사용하여 Page<SearchResultResponse> 생성
        List<SearchResultResponse> content = recipeResponse.getRecipes().stream()
            .map(recipe -> convertRecipeToSearchResult(recipe, keyword))
            .collect(Collectors.toList());
        
        return new org.springframework.data.domain.PageImpl<>(
            content,
            pageable,
            recipeResponse.getTotalCount()
        );
    }

    /**
     * GroupBuy 엔티티를 SearchResultResponse로 변환
     */
    private SearchResultResponse convertGroupBuyToSearchResult(GroupBuy groupBuy, String keyword) {
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
        
        // 참가율 계산
        double participationRate = 0.0;
        if (groupBuy.getTargetHeadcount() > 0) {
            participationRate = (double) groupBuy.getCurrentHeadcount() / groupBuy.getTargetHeadcount() * 100;
        }
        
        // 1인당 가격 계산
        int pricePerPerson = 0;
        if (groupBuy.getTargetHeadcount() > 0) {
            pricePerPerson = groupBuy.getTotalPrice() / groupBuy.getTargetHeadcount();
        }
        
        return SearchResultResponse.builder()
            .entityType(com.recipemate.global.common.EntityType.GROUP_BUY)
            .id(groupBuyResponse.getId())
            .title(groupBuyResponse.getTitle())
            .highlightedTitle(highlightKeyword(groupBuyResponse.getTitle(), keyword))
            .content(groupBuyResponse.getContent())
            .imageUrl(imageUrl)
            .authorOrHost(groupBuyResponse.getHostNickname())
            .createdAt(groupBuyResponse.getCreatedAt())
            .status(groupBuy.getStatus())
            .currentParticipants(groupBuy.getCurrentHeadcount())
            .maxParticipants(groupBuy.getTargetHeadcount())
            .participationRate(participationRate)
            .pricePerPerson(pricePerPerson)
            .deadline(groupBuy.getDeadline())
            .build();
    }

    /**
     * Recipe를 SearchResultResponse로 변환
     */
    private SearchResultResponse convertRecipeToSearchResult(RecipeListResponse.RecipeSimpleInfo recipe, String keyword) {
        return SearchResultResponse.builder()
            .entityType(com.recipemate.global.common.EntityType.RECIPE)
            .apiId(recipe.getId())
            .title(recipe.getName())
            .name(recipe.getName())
            .highlightedTitle(highlightKeyword(recipe.getName(), keyword))
            .content(recipe.getCategory())
            .category(recipe.getCategory())
            .imageUrl(recipe.getImageUrl())
            .build();
    }

    /**
     * 검색어 하이라이트 처리
     * 제목에 포함된 검색어를 <mark> 태그로 감싸서 반환
     */
    private String highlightKeyword(String text, String keyword) {
        if (text == null || keyword == null || keyword.trim().isEmpty()) {
            return text;
        }
        
        try {
            // 대소문자 구분 없이 검색어를 찾아 <mark> 태그로 감싸기
            String pattern = "(?i)(" + java.util.regex.Pattern.quote(keyword) + ")";
            return text.replaceAll(pattern, "<mark>$1</mark>");
        } catch (Exception e) {
            log.error("하이라이트 처리 중 오류 발생", e);
            return text;
        }
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

    /**
     * 검색 키워드 비동기 저장
     * 검색 시 호출되어 검색어를 DB에 저장하고 검색 횟수를 증가시킴
     */
    @Async
    @Transactional
    public void saveSearchKeywordAsync(String keyword) {
        try {
            String normalizedKeyword = keyword.trim().toLowerCase();
            
            // 기존 키워드가 있는지 확인
            Optional<SearchKeyword> existingKeyword = searchKeywordRepository.findByKeyword(normalizedKeyword);
            
            if (existingKeyword.isPresent()) {
                // 기존 키워드면 검색 횟수만 증가 (동시성 제어)
                int updated = searchKeywordRepository.incrementSearchCount(normalizedKeyword);
                log.debug("검색 횟수 증가 완료: {} (updated: {})", normalizedKeyword, updated);
            } else {
                // 새 키워드면 생성
                SearchKeyword newKeyword = SearchKeyword.create(normalizedKeyword);
                searchKeywordRepository.save(newKeyword);
                log.debug("새 검색 키워드 저장 완료: {}", normalizedKeyword);
            }
        } catch (Exception e) {
            log.error("검색 키워드 저장 중 오류 발생", e);
        }
    }

    /**
     * 인기 검색어 상위 N개 조회
     * 
     * @param limit 조회할 개수
     * @return 검색 횟수가 많은 순으로 정렬된 키워드 리스트
     */
    public List<String> getPopularKeywords(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return searchKeywordRepository.findAllByOrderBySearchCountDesc(pageable)
                .stream()
                .map(SearchKeyword::getKeyword)
                .collect(Collectors.toList());
    }
    
    /**
     * 레시피 검색 결과 개수 조회
     */
    private Long countRecipes(String keyword) {
        try {
            RecipeListResponse recipeResponse = recipeService.findRecipes(
                keyword,
                null,
                null,
                "latest",
                "desc",
                PageRequest.of(0, 1) // 개수만 필요하므로 1개만 조회
            );
            return (long) recipeResponse.getTotalCount();
        } catch (Exception e) {
            log.error("레시피 개수 조회 중 오류 발생", e);
            return 0L;
        }
    }
    
    /**
     * 공동구매 검색 결과 개수 조회
     */
    private Long countGroupBuys(String keyword) {
        try {
            Page<GroupBuy> groupBuyPage = groupBuyRepository.searchByKeyword(
                keyword,
                GroupBuyStatus.RECRUITING,
                PageRequest.of(0, 1)
            );
            return groupBuyPage.getTotalElements();
        } catch (Exception e) {
            log.error("공동구매 개수 조회 중 오류 발생", e);
            return 0L;
        }
    }
    
    /**
     * 커뮤니티 게시글 검색 결과 개수 조회
     */
    private Long countPosts(String keyword) {
        try {
            Page<PostWithCountsDto> postPage = postRepository.searchByKeywordWithCounts(
                keyword,
                PageRequest.of(0, 1)
            );
            return postPage.getTotalElements();
        } catch (Exception e) {
            log.error("게시글 개수 조회 중 오류 발생", e);
            return 0L;
        }
    }
}
