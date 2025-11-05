package com.recipemate.domain.recipe.controller;

import com.recipemate.domain.groupbuy.dto.GroupBuyResponse;
import com.recipemate.domain.recipe.dto.CategoryResponse;
import com.recipemate.domain.recipe.dto.RecipeDetailResponse;
import com.recipemate.domain.recipe.dto.RecipeListResponse;
import com.recipemate.domain.recipe.service.RecipeService;
import com.recipemate.global.common.ApiResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * 레시피 컨트롤러
 * 외부 API를 통해 레시피 정보를 조회하고 페이지를 렌더링
 */
@Slf4j
@Controller
@RequestMapping("/recipes")
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeService recipeService;

    private static final int DEFAULT_RANDOM_COUNT = 5;
    private static final String SESSION_KEY_RANDOM_RECIPES = "randomRecipes";
    private static final String SESSION_KEY_RANDOM_COUNT = "randomCount";

    /**
     * 레시피 검색 페이지
     * GET /recipes?keyword={keyword}&category={category}
     */
    @GetMapping
    public String searchRecipesPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            Model model) {
        
        // 카테고리 필터가 있는 경우
        if (category != null && !category.trim().isEmpty()) {
            RecipeListResponse recipes = recipeService.getRecipesByCategory(category);
            model.addAttribute("recipes", recipes);
            model.addAttribute("category", category);
        } 
        // 키워드 검색
        else if (keyword != null && !keyword.trim().isEmpty()) {
            RecipeListResponse recipes = recipeService.searchRecipes(keyword);
            model.addAttribute("recipes", recipes);
            model.addAttribute("keyword", keyword);
        }
        
        return "recipes/list";
    }

    /**
     * 레시피 상세 페이지
     * GET /recipes/{recipeId}
     */
    @GetMapping("/{recipeId}")
    public String recipeDetailPage(
            @PathVariable String recipeId,
            Model model) {
        
        RecipeDetailResponse recipe = recipeService.getRecipeDetail(recipeId);
        model.addAttribute("recipe", recipe);
        
        // 관련 공동구매 조회
        List<GroupBuyResponse> relatedGroupBuys = recipeService.getRelatedGroupBuys(recipeId);
        model.addAttribute("relatedGroupBuys", relatedGroupBuys);
        
        return "recipes/detail";
    }

    /**
     * 랜덤 레시피 추천 페이지
     * GET /recipes/random?count={count}&refresh={true/false}
     * 
     * refresh 파라미터가 없거나 false인 경우: 세션에 저장된 이전 결과 사용 (뒤로가기 대응)
     * refresh=true인 경우: 새로운 랜덤 레시피 조회 (다시 추천받기 버튼)
     */
    @GetMapping("/random")
    public String randomRecipesPage(
            @RequestParam(defaultValue = "5") int count,
            @RequestParam(required = false, defaultValue = "false") boolean refresh,
            HttpSession session,
            Model model) {
        
        RecipeListResponse recipes = null;
        Integer sessionCount = (Integer) session.getAttribute(SESSION_KEY_RANDOM_COUNT);
        
        // 세션에 저장된 결과가 있고, refresh가 false이고, count가 같으면 세션 결과 사용
        if (!refresh && sessionCount != null && sessionCount == count) {
            recipes = (RecipeListResponse) session.getAttribute(SESSION_KEY_RANDOM_RECIPES);
        }
        
        // 세션에 결과가 없거나 refresh=true이거나 count가 다르면 새로 조회
        if (recipes == null) {
            recipes = recipeService.getRandomRecipes(count);
            session.setAttribute(SESSION_KEY_RANDOM_RECIPES, recipes);
            session.setAttribute(SESSION_KEY_RANDOM_COUNT, count);
        }
        
        model.addAttribute("recipes", recipes);
        model.addAttribute("count", count);
        
        return "recipes/random";
    }

    /**
     * 카테고리 목록 페이지
     * GET /recipes/categories
     */
    @GetMapping("/categories")
    public String categoriesPage(Model model) {
        List<CategoryResponse> categories = recipeService.getCategories();
        model.addAttribute("categories", categories);
        
        return "recipes/categories";
    }

    /**
     * 레시피 관련 공동구매 목록 조회 API
     * GET /recipes/{recipeId}/group-purchases
     */
    @GetMapping("/{recipeId}/group-purchases")
    @ResponseBody
    public ResponseEntity<ApiResponse<List<GroupBuyResponse>>> getRelatedGroupBuys(
            @PathVariable String recipeId) {
        
        log.info("레시피 관련 공동구매 조회 요청: recipeId={}", recipeId);
        
        List<GroupBuyResponse> groupBuys = recipeService.getRelatedGroupBuys(recipeId);
        
        return ResponseEntity.ok(ApiResponse.success(groupBuys));
    }
}
