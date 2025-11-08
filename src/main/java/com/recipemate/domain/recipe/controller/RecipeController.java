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

    /**
     * 레시피 목록 페이지 (통합 검색 + 필터 + 정렬 + 페이징)
     * 
     * 파라미터:
     * - keyword: 제목 검색 키워드
     * - category: 카테고리 필터
     * - ingredients: 재료 검색 (쉼표로 구분)
     * - sort: 정렬 기준 (latest, name, popularity)
     */
    @GetMapping
    public String searchRecipesPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String ingredients,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model) {
        
        // 카테고리 목록 조회 (드롭다운 필터용)
        List<CategoryResponse> categories = recipeService.getCategories();
        model.addAttribute("categories", categories);
        
        // 페이지 사이즈 제한
        size = Math.min(size, MAX_PAGE_SIZE);
        
        // 정렬 방향 결정
        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sortOrder;
        
        if ("name".equals(sort)) {
            sortOrder = Sort.by(sortDirection, "title");
        } else if ("popularity".equals(sort)) {
            // 인기순 정렬은 복잡하므로 서비스 레이어에서 별도 처리
            sortOrder = Sort.unsorted();
        } else {
            // latest (최신순)
            sortOrder = Sort.by(sortDirection, "lastSyncedAt");
        }
        
        Pageable pageable = PageRequest.of(page, size, sortOrder);
        
        RecipeListResponse recipes;
        
        // 재료 리스트 파싱
        List<String> ingredientList = null;
        if (ingredients != null && !ingredients.trim().isEmpty()) {
            ingredientList = Arrays.stream(ingredients.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        }
        
        // 통합 검색 메서드 호출
        recipes = recipeService.findRecipes(keyword, ingredientList, category, sort, direction, pageable);
        
        // 모든 검색 파라미터를 모델에 추가
        model.addAttribute("keyword", keyword);
        model.addAttribute("ingredients", ingredients);
        model.addAttribute("category", category);
        
        model.addAttribute("recipes", recipes);
        model.addAttribute("sort", sort);
        model.addAttribute("direction", direction);
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
     * GET /recipes/random?page={page}&size={size}&refresh={true/false}
     * 
     * refresh 파라미터가 true인 경우: 새로운 랜덤 레시피 조회 (다시 추천받기 버튼)
     * refresh가 없거나 false인 경우: 세션에 저장된 결과를 페이징하여 표시
     */
    @GetMapping("/random")
    public String randomRecipesPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false, defaultValue = "false") boolean refresh,
            HttpSession session,
            Model model) {
        
        // 페이지 사이즈 제한
        size = Math.min(size, MAX_PAGE_SIZE);
        
        RecipeListResponse allRecipes = null;
        
        // refresh=true이거나 세션에 결과가 없으면 새로 조회 (최대 100개)
        if (refresh || session.getAttribute(SESSION_KEY_RANDOM_RECIPES) == null) {
            allRecipes = recipeService.getRandomRecipes(100);
            session.setAttribute(SESSION_KEY_RANDOM_RECIPES, allRecipes);
        } else {
            // 세션에서 가져오기
            allRecipes = (RecipeListResponse) session.getAttribute(SESSION_KEY_RANDOM_RECIPES);
        }
        
        // 페이지네이션 적용
        List<RecipeListResponse.RecipeSimpleInfo> allRecipesList = allRecipes.getRecipes();
        int totalCount = allRecipesList.size();
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, totalCount);
        
        // 페이지 범위를 벗어나면 첫 페이지로
        if (fromIndex >= totalCount && totalCount > 0) {
            fromIndex = 0;
            toIndex = Math.min(size, totalCount);
            page = 0;
        }
        
        List<RecipeListResponse.RecipeSimpleInfo> pagedRecipes = 
            fromIndex < totalCount ? allRecipesList.subList(fromIndex, toIndex) : List.of();
        
        // 페이징된 결과로 새 응답 생성
        RecipeListResponse pagedResponse = RecipeListResponse.builder()
                .recipes(pagedRecipes)
                .totalCount(totalCount)
                .source(allRecipes.getSource())
                .build();
        
        model.addAttribute("recipes", pagedResponse);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        
        return "recipes/random";
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
