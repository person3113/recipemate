package com.recipemate.domain.search.service;

import com.recipemate.domain.groupbuy.dto.GroupBuyResponse;
import com.recipemate.domain.groupbuy.dto.GroupBuySearchCondition;
import com.recipemate.domain.groupbuy.entity.GroupBuy;
import com.recipemate.domain.groupbuy.entity.GroupBuyImage;
import com.recipemate.domain.groupbuy.repository.GroupBuyRepository;
import com.recipemate.domain.groupbuy.service.GroupBuyService;
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


/**
 * 통합 검색 서비스
 * GroupBuy, Post, Recipe를 동시에 검색하여 결과를 통합 제공
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final GroupBuyRepository groupBuyRepository;
    private final GroupBuyService groupBuyService;
    private final PostRepository postRepository;
    private final RecipeService recipeService;
    private final SearchKeywordRepository searchKeywordRepository;

    private static final int MAX_PAGE_SIZE = 20;
    private static final long SEARCH_COUNT_COOLDOWN_MS = 300000; // 5분 (300,000ms)

    /**
     * 통합 검색
     * 모든 도메인(GroupBuy, Post, Recipe)을 동시에 검색
     * 
     * @param keyword 검색 키워드
     * @param type 검색 타입 (ALL, RECIPE, GROUP_BUY, POST)
     * @param pageable 페이지 정보
     * @param request HTTP 요청 객체 (세션 관리용)
     * @return 통합 검색 결과
     */
    public UnifiedSearchResponse unifiedSearch(String keyword, String type, Pageable pageable, HttpServletRequest request) {
        // 검색어 유효성 검증
        validateKeyword(keyword);
        
        // type이 null이면 ALL로 기본 설정
        String searchType = (type == null || type.trim().isEmpty()) ? "ALL" : type.toUpperCase();
        
        log.info("통합 검색 시작 - keyword: {}, type: {}, page: {}, size: {}", 
            keyword, searchType, pageable.getPageNumber(), pageable.getPageSize());

        // 검색 키워드 저장 여부 결정 (세션 기반 중복 체크)
        if (shouldIncrementSearchCount(keyword, request.getSession())) {
            // 검색 키워드 저장 (쓰기 트랜잭션)
            saveSearchKeyword(keyword);
        }

        // 실제 검색 수행 (읽기 전용 트랜잭션)
        return performSearch(keyword, searchType, pageable);
    }

    /**
     * 검색어 저장 (쓰기 트랜잭션)
     * REQUIRES_NEW: 항상 새로운 트랜잭션을 시작하여 실행되도록 보장
     * 검색어 저장 실패가 검색 결과 조회에 영향을 주지 않도록 독립적으로 실행
     * 
     * @param keyword 검색 키워드
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveSearchKeyword(String keyword) {
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
            // 검색어 저장 실패가 검색 기능 전체를 방해하지 않도록 로깅만 수행
            log.error("검색 키워드 저장 중 오류 발생", e);
        }
    }

    /**
     * 실제 검색 수행 (읽기 전용 트랜잭션)
     * 
     * @param keyword 검색 키워드
     * @param searchType 검색 타입 (ALL, RECIPE, GROUP_BUY, POST)
     * @param pageable 페이지 정보
     * @return 통합 검색 결과
     */
    @Transactional(readOnly = true)
    public UnifiedSearchResponse performSearch(String keyword, String searchType, Pageable pageable) {
        List<SearchResultResponse> groupBuyResults;
        List<SearchResultResponse> postResults;
        List<SearchResultResponse> recipeResults;

        // type에 따라 검색 범위 결정
        if ("ALL".equals(searchType)) {
            // 전체 탭: 각 도메인별 상위 5개씩만 조회하되, 전체 개수도 계산
            Pageable previewPageable = PageRequest.of(0, 5);
            
            // Page 객체로 받아서 데이터와 전체 개수를 한 번에 가져오기
            Page<SearchResultResponse> groupBuyPage = searchGroupBuysPage(keyword, previewPageable);
            Page<SearchResultResponse> postPage = searchPostsPage(keyword, previewPageable);
            Page<SearchResultResponse> recipePage = searchRecipesPage(keyword, previewPageable);
            
            groupBuyResults = groupBuyPage.getContent();
            postResults = postPage.getContent();
            recipeResults = recipePage.getContent();
            
            // Page 객체에서 전체 개수 추출 (추가 쿼리 없음)
            Long totalGroupBuyCount = groupBuyPage.getTotalElements();
            Long totalPostCount = postPage.getTotalElements();
            Long totalRecipeCount = recipePage.getTotalElements();
            
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
            recipeResults = searchRecipesPage(keyword, pageable).getContent();
        } else if ("GROUP_BUY".equals(searchType)) {
            // 공구 탭: 공구만 페이징하여 전체 조회
            Pageable limitedPageable = limitPageSize(pageable);
            groupBuyResults = searchGroupBuysPage(keyword, limitedPageable).getContent();
            postResults = new ArrayList<>();
            recipeResults = new ArrayList<>();
        } else if ("POST".equals(searchType)) {
            // 커뮤니티 탭: 게시글만 페이징하여 전체 조회
            Pageable limitedPageable = limitPageSize(pageable);
            groupBuyResults = new ArrayList<>();
            postResults = searchPostsPage(keyword, limitedPageable).getContent();
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
     * 공동구매 검색 (Page 반환)
     * QueryDSL 기반 검색으로 제목 + 내용 검색 및 모든 상태 포함
     */
    @Transactional(readOnly = true)
    public Page<SearchResultResponse> searchGroupBuysPage(String keyword, Pageable pageable) {
        // GroupBuySearchCondition 생성 (status는 null로 두어 모든 상태 검색)
        GroupBuySearchCondition condition = GroupBuySearchCondition.builder()
            .keyword(keyword)
            .build();
        
        // GroupBuyService의 QueryDSL 메소드 사용
        Page<GroupBuyResponse> groupBuyPage = groupBuyService.getGroupBuyList(condition, pageable);
        
        return groupBuyPage.map(groupBuyResponse -> convertGroupBuyResponseToSearchResult(groupBuyResponse, keyword));
    }

    /**
     * 커뮤니티 게시글 검색 (Page 반환)
     */
    @Transactional(readOnly = true)
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
     * 레시피 검색 (Page 반환)
     * RecipeService의 findRecipes 메소드를 사용하여 페이징 지원
     */
    @Transactional(readOnly = true)
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
     * GroupBuyResponse DTO를 SearchResultResponse로 변환
     * (QueryDSL 검색 결과용)
     */
    private SearchResultResponse convertGroupBuyResponseToSearchResult(GroupBuyResponse groupBuyResponse, String keyword) {
        // 이미지 URL 추출 (첫 번째 이미지)
        String imageUrl = null;
        if (groupBuyResponse.getImageUrls() != null && !groupBuyResponse.getImageUrls().isEmpty()) {
            imageUrl = groupBuyResponse.getImageUrls().get(0);
        }
        
        // 참가율 계산
        double participationRate = 0.0;
        if (groupBuyResponse.getTargetHeadcount() > 0) {
            participationRate = (double) groupBuyResponse.getCurrentHeadcount() / groupBuyResponse.getTargetHeadcount() * 100;
        }
        
        // 1인당 가격 계산
        int pricePerPerson = 0;
        if (groupBuyResponse.getTargetHeadcount() > 0) {
            pricePerPerson = groupBuyResponse.getTargetAmount() / groupBuyResponse.getTargetHeadcount();
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
            .status(groupBuyResponse.getStatus())
            .currentParticipants(groupBuyResponse.getCurrentHeadcount())
            .maxParticipants(groupBuyResponse.getTargetHeadcount())
            .participationRate(participationRate)
            .pricePerPerson(pricePerPerson)
            .deadline(groupBuyResponse.getDeadline())
            .build();
    }

    /**
     * GroupBuy 엔티티를 SearchResultResponse로 변환
     * (레거시 메소드 - 필요시 제거 가능)
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
            pricePerPerson = groupBuy.getTargetAmount() / groupBuy.getTargetHeadcount();
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
     * 검색 결과 개수만 조회 (배지 표시용)
     * COUNT 쿼리만 실행하여 각 카테고리별 총 개수 반환
     * 
     * @param keyword 검색 키워드
     * @return 카테고리별 개수 정보를 담은 응답 (데이터는 비어있음)
     */
    @Transactional(readOnly = true)
    public UnifiedSearchResponse getSearchCounts(String keyword) {
        validateKeyword(keyword);
        
        // 각 repository의 count 메서드 사용 (COUNT 쿼리만 실행)
        GroupBuySearchCondition condition = GroupBuySearchCondition.builder()
            .keyword(keyword)
            .build();
        
        long groupBuyCount = groupBuyRepository.countByCondition(condition);
        long postCount = postRepository.countByKeyword(keyword);
        long recipeCount = recipeService.countRecipes(keyword);
        
        log.info("검색 개수 조회 - GroupBuy: {}, Post: {}, Recipe: {}", 
            groupBuyCount, postCount, recipeCount);
        
        // 빈 리스트와 함께 개수 정보만 반환
        return UnifiedSearchResponse.ofWithCounts(
            keyword,
            new ArrayList<>(),
            new ArrayList<>(),
            new ArrayList<>(),
            recipeCount,
            groupBuyCount,
            postCount
        );
    }

    /**
     * 공동구매 검색 결과 개수 조회
     */
    @Transactional(readOnly = true)
    public long countGroupBuys(String keyword) {
        validateKeyword(keyword);
        GroupBuySearchCondition condition = GroupBuySearchCondition.builder()
            .keyword(keyword)
            .build();
        return groupBuyRepository.countByCondition(condition);
    }

    /**
     * 게시글 검색 결과 개수 조회
     */
    @Transactional(readOnly = true)
    public long countPosts(String keyword) {
        validateKeyword(keyword);
        return postRepository.countByKeyword(keyword);
    }

    /**
     * 레시피 검색 결과 개수 조회
     */
    @Transactional(readOnly = true)
    public long countRecipes(String keyword) {
        validateKeyword(keyword);
        return recipeService.countRecipes(keyword);
    }

    /**
     * 인기 검색어 상위 N개 조회
     * 
     * @param limit 조회할 개수
     * @return 검색 횟수가 많은 순으로 정렬된 키워드 리스트
     */
    @Transactional(readOnly = true)
    public List<String> getPopularKeywords(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return searchKeywordRepository.findAllByOrderBySearchCountDesc(pageable)
                .stream()
                .map(SearchKeyword::getKeyword)
                .collect(Collectors.toList());
    }

    /**
     * 검색 수 증가 여부 결정
     * 세션에 기록된 마지막 검색 시간을 확인하여 일정 시간(5분) 이내 중복 검색을 필터링
     * 
     * @param keyword 검색 키워드
     * @param session HTTP 세션
     * @return 검색 수를 증가시킬지 여부
     */
    private boolean shouldIncrementSearchCount(String keyword, HttpSession session) {
        // 세션에서 'searchedKeywords' 맵 가져오기 (없으면 생성)
        @SuppressWarnings("unchecked")
        Map<String, Long> searchedKeywords = (Map<String, Long>) session.getAttribute("searchedKeywords");
        
        if (searchedKeywords == null) {
            searchedKeywords = new ConcurrentHashMap<>();
            session.setAttribute("searchedKeywords", searchedKeywords);
        }

        String normalizedKeyword = keyword.trim().toLowerCase();
        long now = System.currentTimeMillis();
        Long lastSearchedTime = searchedKeywords.get(normalizedKeyword);

        // 5분(300,000ms) 이내에 검색한 기록이 없으면 카운트 증가 허용
        if (lastSearchedTime == null || (now - lastSearchedTime) > SEARCH_COUNT_COOLDOWN_MS) {
            searchedKeywords.put(normalizedKeyword, now);
            log.debug("검색 수 증가 허용: {} (마지막 검색: {})", normalizedKeyword, 
                lastSearchedTime == null ? "없음" : (now - lastSearchedTime) + "ms 전");
            return true; // 카운트 증가 필요
        }

        log.debug("검색 수 증가 스킵: {} ({}ms 전 검색함)", normalizedKeyword, now - lastSearchedTime);
        return false; // 카운트 증가 불필요
    }
}
