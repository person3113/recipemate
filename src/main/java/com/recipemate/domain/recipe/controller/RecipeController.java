package com.recipemate.domain.recipe.controller;

import com.recipemate.domain.groupbuy.dto.GroupBuyResponse;
import com.recipemate.domain.recipe.dto.CategoryResponse;
import com.recipemate.domain.recipe.dto.RecipeDetailResponse;
import com.recipemate.domain.recipe.dto.RecipeListResponse;
import com.recipemate.domain.recipe.entity.RecipeSource;
import com.recipemate.domain.recipe.service.RecipeService;
import com.recipemate.domain.recipe.service.RecipeSyncService;
import com.recipemate.global.common.ApiResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private final RecipeSyncService recipeSyncService;

    private static final int DEFAULT_RANDOM_COUNT = 5;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;
    private static final String SESSION_KEY_RANDOM_RECIPES = "randomRecipes";
    private static final String SESSION_KEY_RANDOM_COUNT = "randomCount";

    /**
     * 레시피 검색 페이지 (DB 기반)
     * GET /recipes?keyword={keyword}&category={category}&area={area}&source={source}&ingredients={ing1,ing2}&maxCalories={cal}&page={page}&size={size}
     * 
     * 지원하는 필터:
     * - keyword: 제목 검색
     * - category: 카테고리 필터
     * - area: 지역 필터
     * - source: 데이터 출처 (themealdb, foodsafety)
     * - ingredients: 재료 검색 (쉼표로 구분)
     * - maxCalories, maxCarbohydrate, maxProtein, maxFat, maxSodium: 영양정보 필터
     */
    @GetMapping
    public String searchRecipesPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String area,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String ingredients,
            @RequestParam(required = false) Integer maxCalories,
            @RequestParam(required = false) Integer maxCarbohydrate,
            @RequestParam(required = false) Integer maxProtein,
            @RequestParam(required = false) Integer maxFat,
            @RequestParam(required = false) Integer maxSodium,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model) {
        
        // 페이지 사이즈 제한
        size = Math.min(size, MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, size, Sort.by("lastSyncedAt").descending());
        
        RecipeListResponse recipes;
        
        // 재료 검색이 있는 경우
        if (ingredients != null && !ingredients.trim().isEmpty()) {
            List<String> ingredientList = Arrays.stream(ingredients.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            
            recipes = recipeService.findRecipesByIngredients(ingredientList, pageable);
            model.addAttribute("ingredients", ingredients);
            
        // 영양정보 필터가 있는 경우
        } else if (maxCalories != null || maxCarbohydrate != null || 
                   maxProtein != null || maxFat != null || maxSodium != null) {
            
            recipes = recipeService.findRecipesByNutrition(
                    maxCalories, maxCarbohydrate, maxProtein, maxFat, maxSodium, pageable);
            
            model.addAttribute("maxCalories", maxCalories);
            model.addAttribute("maxCarbohydrate", maxCarbohydrate);
            model.addAttribute("maxProtein", maxProtein);
            model.addAttribute("maxFat", maxFat);
            model.addAttribute("maxSodium", maxSodium);
            
        // 일반 검색 (키워드, 카테고리, 지역, 출처)
        } else {
            RecipeSource sourceApi = null;
            if (source != null && !source.trim().isEmpty()) {
                try {
                    sourceApi = RecipeSource.valueOf(source.toUpperCase());
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid source parameter: {}", source);
                }
            }
            
            recipes = recipeService.findRecipes(keyword, category, area, sourceApi, pageable);
            
            model.addAttribute("keyword", keyword);
            model.addAttribute("category", category);
            model.addAttribute("area", area);
            model.addAttribute("source", source);
        }
        
        model.addAttribute("recipes", recipes);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        
        return "recipes/list";
    }

    /**
     * 레시피 상세 페이지
     * GET /recipes/{recipeId}
     * 
     * recipeId 형식:
     * - 숫자만: DB ID (예: "123")
     * - 접두사 포함: API ID (예: "meal-52772", "food-1234")
     */
    @GetMapping("/{recipeId}")
    public String recipeDetailPage(
            @PathVariable String recipeId,
            Model model) {
        
        RecipeDetailResponse recipe;
        
        // recipeId가 순수 숫자인지 확인 (DB ID)
        if (recipeId.matches("\\d+")) {
            // DB ID로 조회
            Long dbId = Long.parseLong(recipeId);
            recipe = recipeService.getRecipeDetailById(dbId);
        } else {
            // API ID로 조회 (DB 우선, API 폴백)
            recipe = recipeService.getRecipeDetailByApiId(recipeId);
        }
        
        model.addAttribute("recipe", recipe);
        
        // 관련 공동구매 조회
        List<GroupBuyResponse> relatedGroupBuys = recipeService.getRelatedGroupBuys(recipe.getId());
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

    /**
     * TheMealDB 데이터 수동 동기화 (관리자용)
     * POST /recipes/admin/sync-mealdb?count={count}
     * 
     * @param count 동기화할 랜덤 레시피 개수 (기본값: 100)
     * @return 동기화 결과 (요청 개수, 실제 동기화된 개수)
     */
    @PostMapping("/admin/sync-mealdb")
    @ResponseBody
    public ResponseEntity<ApiResponse<Map<String, Object>>> syncMealDbData(
            @RequestParam(defaultValue = "100") int count) {
        
        log.info("Manual TheMealDB sync triggered: count={}", count);
        int syncedCount = recipeSyncService.syncMealDbRandomRecipes(count);
        
        Map<String, Object> result = new HashMap<>();
        result.put("syncedCount", syncedCount);
        result.put("requestedCount", count);
        
        log.info("TheMealDB sync completed: requested={}, synced={}", count, syncedCount);
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
