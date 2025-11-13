package com.recipemate.domain.search.controller;

import com.recipemate.domain.search.dto.SearchResultResponse;
import com.recipemate.domain.search.dto.UnifiedSearchResponse;
import com.recipemate.domain.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 통합 검색 컨트롤러
 * 공동구매, 게시글, 레시피를 통합 검색하는 기능 제공
 */
@Controller
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    /**
     * 통합 검색 결과 페이지 렌더링
     * 
     * @param keyword 검색어 (필수)
     * @param type 검색 타입 (선택: ALL, RECIPE, GROUP_BUY, POST)
     * @param pageable 페이지네이션 정보 (기본: page=0, size=10)
     * @param response HTTP 응답 객체
     * @param model 뷰에 전달할 모델
     * @return 검색 결과 페이지 뷰 이름
     */
    @GetMapping
    public String searchResults(
        @RequestParam String keyword,
        @RequestParam(required = false, defaultValue = "ALL") String type,
        @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
        HttpServletResponse response,
        Model model
    ) {
        // 브라우저 뒤로가기 시 캐시 사용 방지 (항상 최신 상태 유지)
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        
        // 공통 속성 추가
        model.addAttribute("keyword", keyword);
        model.addAttribute("type", type);
        
        // 타입별 처리
        if ("ALL".equalsIgnoreCase(type)) {
            // 전체 탭: 통합 검색으로 미리보기와 전체 개수 조회
            UnifiedSearchResponse searchResults = searchService.unifiedSearch(keyword, type, pageable);
            long totalCount = searchResults.getTotalRecipeCount() 
                            + searchResults.getTotalGroupBuyCount() 
                            + searchResults.getTotalPostCount();
            
            model.addAttribute("searchResults", searchResults);
            model.addAttribute("totalResults", totalCount);  // 전체 결과 개수의 합
            model.addAttribute("recipes", searchResults.getRecipes());
            model.addAttribute("groupbuys", searchResults.getGroupBuys());
            model.addAttribute("posts", searchResults.getPosts());
            model.addAttribute("recipeCount", searchResults.getTotalRecipeCount());
            model.addAttribute("groupbuyCount", searchResults.getTotalGroupBuyCount());
            model.addAttribute("postCount", searchResults.getTotalPostCount());
        } else {
            // 개별 탭: 먼저 전체 탭의 개수 정보를 조회 (배지 표시용)
            UnifiedSearchResponse allResults = searchService.unifiedSearch(keyword, "ALL", pageable);
            long totalCount = allResults.getTotalRecipeCount() 
                            + allResults.getTotalGroupBuyCount() 
                            + allResults.getTotalPostCount();
            
            if ("RECIPE".equalsIgnoreCase(type)) {
                // 레시피 탭: 레시피 페이지 객체 제공
                Page<SearchResultResponse> recipePage = searchService.searchRecipesPage(keyword, pageable);
                model.addAttribute("recipes", recipePage.getContent());
                model.addAttribute("recipePage", recipePage);
                model.addAttribute("recipeCount", allResults.getTotalRecipeCount());
                model.addAttribute("groupbuyCount", allResults.getTotalGroupBuyCount());
                model.addAttribute("postCount", allResults.getTotalPostCount());
                model.addAttribute("totalResults", totalCount);  // 전체 결과 개수의 합
            } else if ("GROUP_BUY".equalsIgnoreCase(type)) {
                // 공동구매 탭: 공동구매 페이지 객체 제공
                Page<SearchResultResponse> groupBuyPage = searchService.searchGroupBuysPage(keyword, pageable);
                model.addAttribute("groupbuys", groupBuyPage.getContent());
                model.addAttribute("groupBuyPage", groupBuyPage);
                model.addAttribute("recipeCount", allResults.getTotalRecipeCount());
                model.addAttribute("groupbuyCount", allResults.getTotalGroupBuyCount());
                model.addAttribute("postCount", allResults.getTotalPostCount());
                model.addAttribute("totalResults", totalCount);  // 전체 결과 개수의 합
            } else if ("POST".equalsIgnoreCase(type)) {
                // 커뮤니티 탭: 커뮤니티 페이지 객체 제공
                Page<SearchResultResponse> postPage = searchService.searchPostsPage(keyword, pageable);
                model.addAttribute("posts", postPage.getContent());
                model.addAttribute("postPage", postPage);
                model.addAttribute("recipeCount", allResults.getTotalRecipeCount());
                model.addAttribute("groupbuyCount", allResults.getTotalGroupBuyCount());
                model.addAttribute("postCount", allResults.getTotalPostCount());
                model.addAttribute("totalResults", totalCount);  // 전체 결과 개수의 합
            }
        }

        return "search/results";
    }

    /**
     * 인기 검색어 조회 API
     * 
     * @param limit 조회할 개수 (기본: 10)
     * @return 인기 검색어 리스트
     */
    @GetMapping("/popular-keywords")
    @ResponseBody
    public List<String> getPopularKeywords(
        @RequestParam(required = false, defaultValue = "10") int limit
    ) {
        return searchService.getPopularKeywords(limit);
    }
}

