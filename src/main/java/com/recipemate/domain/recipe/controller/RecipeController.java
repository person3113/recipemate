package com.recipemate.domain.recipe.controller;

import com.recipemate.domain.groupbuy.dto.GroupBuyResponse;
import com.recipemate.domain.recipe.dto.CategoryResponse;
import com.recipemate.domain.recipe.dto.RecipeDetailResponse;
import com.recipemate.domain.recipe.dto.RecipeListResponse;
import com.recipemate.domain.recipe.service.RecipeService;
import com.recipemate.global.common.ApiResponse;
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

    /**
     * 레시피 검색 페이지
     * GET /recipes?keyword={keyword}
     */
    @GetMapping
    public String searchRecipesPage(
            @RequestParam(required = false) String keyword,
            Model model) {
        
        if (keyword != null && !keyword.trim().isEmpty()) {
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
        
        return "recipes/detail";
    }

    /**
     * 랜덤 레시피 추천 페이지
     * GET /recipes/random?count={count}
     */
    @GetMapping("/random")
    public String randomRecipesPage(
            @RequestParam(defaultValue = "5") int count,
            Model model) {
        
        RecipeListResponse recipes = recipeService.getRandomRecipes(count);
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
