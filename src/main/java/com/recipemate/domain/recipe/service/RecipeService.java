package com.recipemate.domain.recipe.service;

import com.recipemate.domain.groupbuy.dto.GroupBuyResponse;
import com.recipemate.domain.groupbuy.entity.GroupBuy;
import com.recipemate.domain.groupbuy.repository.GroupBuyRepository;
import com.recipemate.domain.recipe.client.FoodSafetyClient;
import com.recipemate.domain.recipe.client.TheMealDBClient;
import com.recipemate.domain.recipe.dto.*;
import com.recipemate.global.common.GroupBuyStatus;
import com.recipemate.global.config.CacheConfig;
import com.recipemate.global.exception.CustomException;
import com.recipemate.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 레시피 서비스
 * TheMealDB와 식품안전나라 API를 통합하여 레시피 정보를 제공
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecipeService {

    private final TheMealDBClient theMealDBClient;
    private final FoodSafetyClient foodSafetyClient;
    private final GroupBuyRepository groupBuyRepository;

    private static final String MEAL_PREFIX = "meal-";
    private static final String FOOD_PREFIX = "food-";
    private static final int MAX_RANDOM_COUNT = 100;
    private static final int FOOD_SAFETY_SEARCH_SIZE = 100;

    /**
     * 레시피 검색
     * 두 API에서 검색 후 결과를 통합하여 반환
     * @param keyword 검색어
     * @return 통합된 레시피 목록
     */
    @Cacheable(value = CacheConfig.RECIPES_CACHE, key = "'search:' + #keyword")
    public RecipeListResponse searchRecipes(String keyword) {
        validateKeyword(keyword);

        // 두 API에서 동시에 검색
        List<MealResponse> mealResults = theMealDBClient.searchRecipes(keyword);
        List<CookRecipeResponse> foodResults = foodSafetyClient.searchRecipesByName(keyword, 1, FOOD_SAFETY_SEARCH_SIZE);

        // 결과 통합
        List<RecipeListResponse.RecipeSimpleInfo> recipes = new ArrayList<>();
        
        // TheMealDB 결과 변환
        recipes.addAll(mealResults.stream()
                .map(this::convertMealToSimpleInfo)
                .collect(Collectors.toList()));
        
        // 식품안전나라 결과 변환
        recipes.addAll(foodResults.stream()
                .map(this::convertCookRecipeToSimpleInfo)
                .collect(Collectors.toList()));

        // 출처 결정
        String source = determineSource(mealResults.isEmpty(), foodResults.isEmpty());

        return RecipeListResponse.builder()
                .recipes(recipes)
                .totalCount(recipes.size())
                .source(source)
                .build();
    }

    /**
     * 레시피 상세 조회
     * API ID 형식에 따라 적절한 API를 호출하여 상세 정보 반환
     * @param apiId API ID (meal-{id} 또는 food-{id} 형식)
     * @return 레시피 상세 정보
     */
    @Cacheable(value = CacheConfig.RECIPES_CACHE, key = "'detail:' + #apiId")
    public RecipeDetailResponse getRecipeDetail(String apiId) {
        validateApiId(apiId);

        if (apiId.startsWith(MEAL_PREFIX)) {
            // TheMealDB 레시피 조회
            String mealId = apiId.substring(MEAL_PREFIX.length());
            MealResponse meal = theMealDBClient.getRecipeById(mealId);
            
            if (meal == null) {
                throw new CustomException(ErrorCode.RECIPE_NOT_FOUND);
            }
            
            return convertMealToDetailResponse(meal);
            
        } else if (apiId.startsWith(FOOD_PREFIX)) {
            // 식품안전나라 레시피 조회
            String foodSeq = apiId.substring(FOOD_PREFIX.length());
            
            // 식품안전나라는 ID로 직접 조회가 안되므로 rcpSeq 범위로 조회
            try {
                int seq = Integer.parseInt(foodSeq);
                List<CookRecipeResponse> results = foodSafetyClient.getKoreanRecipes(seq, seq);
                
                if (results.isEmpty()) {
                    throw new CustomException(ErrorCode.RECIPE_NOT_FOUND);
                }
                
                return convertCookRecipeToDetailResponse(results.get(0));
            } catch (NumberFormatException e) {
                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
            }
            
        } else {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    /**
     * 랜덤 레시피 조회
     * TheMealDB API를 사용하여 랜덤 레시피 반환
     * 매번 새로운 랜덤 레시피를 제공하기 위해 캐싱하지 않음
     * @param count 조회할 레시피 개수
     * @return 랜덤 레시피 목록
     */
    public RecipeListResponse getRandomRecipes(int count) {
        validateRandomCount(count);

        List<MealResponse> meals = theMealDBClient.getRandomRecipes(count);
        
        List<RecipeListResponse.RecipeSimpleInfo> recipes = meals.stream()
                .map(this::convertMealToSimpleInfo)
                .collect(Collectors.toList());

        return RecipeListResponse.builder()
                .recipes(recipes)
                .totalCount(recipes.size())
                .source("themealdb")
                .build();
    }

    /**
     * 카테고리 목록 조회
     * TheMealDB API의 카테고리 목록 반환
     * @return 카테고리 목록
     */
    @Cacheable(value = CacheConfig.RECIPES_CACHE, key = "'categories'")
    public List<CategoryResponse> getCategories() {
        return theMealDBClient.getCategories();
    }

    /**
     * 카테고리별 레시피 조회
     * 특정 카테고리에 속한 레시피 목록 반환
     * @param category 카테고리 이름
     * @return 해당 카테고리의 레시피 목록
     */
    @Cacheable(value = CacheConfig.RECIPES_CACHE, key = "'category:' + #category")
    public RecipeListResponse getRecipesByCategory(String category) {
        validateCategory(category);

        List<MealResponse> meals = theMealDBClient.getRecipesByCategory(category);
        
        List<RecipeListResponse.RecipeSimpleInfo> recipes = meals.stream()
                .map(this::convertMealToSimpleInfo)
                .collect(Collectors.toList());

        return RecipeListResponse.builder()
                .recipes(recipes)
                .totalCount(recipes.size())
                .source("themealdb")
                .build();
    }

    /**
     * 레시피 관련 공동구매 조회
     * 특정 레시피 ID와 연결된 활성 공동구매 목록 반환
     * @param recipeApiId 레시피 API ID (meal-{id} 또는 food-{id} 형식)
     * @return 활성 상태의 공동구매 목록
     */
    public List<GroupBuyResponse> getRelatedGroupBuys(String recipeApiId) {
        validateRecipeApiId(recipeApiId);

        // 레시피 ID로 공동구매 조회
        List<GroupBuy> groupBuys = groupBuyRepository.findByRecipeApiId(recipeApiId);

        // 활성 상태 (RECRUITING, IMMINENT)인 공동구매만 필터링하여 변환
        return groupBuys.stream()
                .filter(gb -> gb.getStatus() == GroupBuyStatus.RECRUITING || 
                              gb.getStatus() == GroupBuyStatus.IMMINENT)
                .map(gb -> {
                    // 이미지 URL 목록 수집
                    List<String> imageUrls = gb.getImages().stream()
                            .map(img -> img.getImageUrl())
                            .collect(Collectors.toList());
                    return GroupBuyResponse.from(gb, imageUrls);
                })
                .collect(Collectors.toList());
    }

    // ========== 변환 메서드 ==========

    /**
     * MealResponse를 RecipeSimpleInfo로 변환
     */
    private RecipeListResponse.RecipeSimpleInfo convertMealToSimpleInfo(MealResponse meal) {
        return RecipeListResponse.RecipeSimpleInfo.builder()
                .id(MEAL_PREFIX + meal.getId())
                .name(meal.getName())
                .imageUrl(meal.getThumbnail())
                .category(meal.getCategory())
                .source("themealdb")
                .build();
    }

    /**
     * CookRecipeResponse를 RecipeSimpleInfo로 변환
     */
    private RecipeListResponse.RecipeSimpleInfo convertCookRecipeToSimpleInfo(CookRecipeResponse recipe) {
        return RecipeListResponse.RecipeSimpleInfo.builder()
                .id(FOOD_PREFIX + recipe.getRcpSeq())
                .name(recipe.getRcpNm())
                .imageUrl(recipe.getAttFileNoMain())
                .category(recipe.getRcpPat2())
                .source("foodsafety")
                .build();
    }

    /**
     * MealResponse를 RecipeDetailResponse로 변환
     */
    private RecipeDetailResponse convertMealToDetailResponse(MealResponse meal) {
        // 재료 정보 변환
        List<RecipeDetailResponse.IngredientInfo> ingredients = meal.getIngredients().stream()
                .map(ing -> RecipeDetailResponse.IngredientInfo.builder()
                        .name(ing.getName())
                        .measure(ing.getMeasure())
                        .build())
                .collect(Collectors.toList());

        return RecipeDetailResponse.builder()
                .id(MEAL_PREFIX + meal.getId())
                .name(meal.getName())
                .imageUrl(meal.getThumbnail())
                .category(meal.getCategory())
                .area(meal.getArea())
                .instructions(meal.getInstructions())
                .youtubeUrl(meal.getYoutubeUrl())
                .sourceUrl(meal.getSourceUrl())
                .ingredients(ingredients)
                .manualSteps(null) // TheMealDB는 단계별 이미지가 없음
                .nutritionInfo(null) // TheMealDB는 영양 정보가 없음
                .source("themealdb")
                .build();
    }

    /**
     * CookRecipeResponse를 RecipeDetailResponse로 변환
     */
    private RecipeDetailResponse convertCookRecipeToDetailResponse(CookRecipeResponse recipe) {
        // 재료 정보 변환
        List<RecipeDetailResponse.IngredientInfo> ingredients = recipe.getIngredients().stream()
                .map(ing -> RecipeDetailResponse.IngredientInfo.builder()
                        .name(ing)
                        .measure("") // 식품안전나라는 재료 분량 정보가 별도로 없음
                        .build())
                .collect(Collectors.toList());

        // 조리 단계 변환
        List<RecipeDetailResponse.ManualStep> manualSteps = recipe.getManualSteps().stream()
                .map(step -> RecipeDetailResponse.ManualStep.builder()
                        .stepNumber(step.getStepNumber())
                        .description(step.getDescription())
                        .imageUrl(step.getImageUrl())
                        .build())
                .collect(Collectors.toList());

        // 영양 정보 변환
        RecipeDetailResponse.NutritionInfo nutritionInfo = RecipeDetailResponse.NutritionInfo.builder()
                .weight(recipe.getInfoWgt())
                .energy(recipe.getInfoEng())
                .carbohydrate(recipe.getInfoCar())
                .protein(recipe.getInfoPro())
                .fat(recipe.getInfoFat())
                .sodium(recipe.getInfoNa())
                .build();

        return RecipeDetailResponse.builder()
                .id(FOOD_PREFIX + recipe.getRcpSeq())
                .name(recipe.getRcpNm())
                .imageUrl(recipe.getAttFileNoMain())
                .category(recipe.getRcpPat2())
                .area(null) // 식품안전나라는 지역 정보가 없음
                .instructions(null) // 단계별로 나뉘어 있음
                .youtubeUrl(null) // 식품안전나라는 유튜브 링크가 없음
                .sourceUrl(null) // 식품안전나라는 소스 URL이 없음
                .ingredients(ingredients)
                .manualSteps(manualSteps)
                .nutritionInfo(nutritionInfo)
                .source("foodsafety")
                .build();
    }

    // ========== 유효성 검증 메서드 ==========

    /**
     * 검색어 유효성 검증
     */
    private void validateKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    /**
     * API ID 유효성 검증
     */
    private void validateApiId(String apiId) {
        if (apiId == null || apiId.trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        
        if (!apiId.startsWith(MEAL_PREFIX) && !apiId.startsWith(FOOD_PREFIX)) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    /**
     * 랜덤 레시피 개수 유효성 검증
     */
    private void validateRandomCount(int count) {
        if (count < 0 || count > MAX_RANDOM_COUNT) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    /**
     * 레시피 API ID 유효성 검증
     */
    private void validateRecipeApiId(String recipeApiId) {
        if (recipeApiId == null || recipeApiId.trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    /**
     * 카테고리 유효성 검증
     */
    private void validateCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    /**
     * 검색 결과 출처 결정
     */
    private String determineSource(boolean mealEmpty, boolean foodEmpty) {
        if (mealEmpty && foodEmpty) {
            return "both";
        } else if (mealEmpty) {
            return "foodsafety";
        } else if (foodEmpty) {
            return "themealdb";
        } else {
            return "both";
        }
    }
}
