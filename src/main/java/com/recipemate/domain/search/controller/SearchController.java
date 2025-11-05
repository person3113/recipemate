package com.recipemate.domain.search.controller;

import com.recipemate.domain.search.dto.SearchResultResponse;
import com.recipemate.domain.search.dto.UnifiedSearchResponse;
import com.recipemate.domain.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
     * @param query 검색어 (필수)
     * @param pageable 페이지네이션 정보 (기본: page=0, size=10)
     * @param model 뷰에 전달할 모델
     * @return 검색 결과 페이지 뷰 이름
     */
    @GetMapping
    public String searchResults(
        @RequestParam String query,
        @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
        Model model
    ) {
        // 통합 검색 수행
        UnifiedSearchResponse searchResults = searchService.unifiedSearch(query, pageable);

        // 모델에 검색 결과 추가
        model.addAttribute("searchResults", searchResults);
        model.addAttribute("keyword", query);
        model.addAttribute("query", query);
        model.addAttribute("totalResults", searchResults.getTotalResults());
        model.addAttribute("recipes", searchResults.getRecipes());
        model.addAttribute("groupbuys", searchResults.getGroupBuys());
        model.addAttribute("posts", searchResults.getPosts());
        model.addAttribute("recipeCount", searchResults.getRecipes().size());
        model.addAttribute("groupbuyCount", searchResults.getGroupBuys().size());
        model.addAttribute("postCount", searchResults.getPosts().size());

        return "search/results";
    }

    /**
     * htmx용 검색 프래그먼트 렌더링
     * 특정 타입의 검색 결과만 반환
     * 
     * @param query 검색어 (필수)
     * @param type 엔티티 타입 (필수: GROUP_BUY, POST, RECIPE)
     * @param pageable 페이지네이션 정보
     * @param model 뷰에 전달할 모델
     * @return 타입별 프래그먼트 뷰 이름
     * @throws IllegalArgumentException 유효하지 않은 타입인 경우
     */
    @GetMapping("/fragments")
    public String searchFragments(
        @RequestParam String query,
        @RequestParam String type,
        @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
        Model model
    ) {
        // 통합 검색 수행
        UnifiedSearchResponse searchResults = searchService.unifiedSearch(query, pageable);

        // 타입에 따라 적절한 결과와 프래그먼트 반환
        List<SearchResultResponse> results;
        String fragmentName;

        switch (type.toUpperCase()) {
            case "GROUP_BUY":
                results = searchResults.getGroupBuys();
                fragmentName = "groupBuyResults";
                break;
            case "POST":
                results = searchResults.getPosts();
                fragmentName = "postResults";
                break;
            case "RECIPE":
                results = searchResults.getRecipes();
                fragmentName = "recipeResults";
                break;
            default:
                throw new IllegalArgumentException("유효하지 않은 타입입니다: " + type);
        }

        model.addAttribute("results", results);
        model.addAttribute("query", query);

        return "search/fragments :: " + fragmentName;
    }
}
