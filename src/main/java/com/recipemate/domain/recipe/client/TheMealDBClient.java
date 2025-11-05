package com.recipemate.domain.recipe.client;

import com.recipemate.domain.recipe.dto.CategoryListResponse;
import com.recipemate.domain.recipe.dto.CategoryResponse;
import com.recipemate.domain.recipe.dto.MealListResponse;
import com.recipemate.domain.recipe.dto.MealResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * TheMealDB API 클라이언트
 * 외부 레시피 API와 통신하는 클래스
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TheMealDBClient {

    private final RestTemplate restTemplate;
    private static final String BASE_URL = "https://www.themealdb.com/api/json/v1/1";

    /**
     * 레시피 이름으로 검색
     * @param keyword 검색어
     * @return 검색된 레시피 목록
     */
    public List<MealResponse> searchRecipes(String keyword) {
        String url = BASE_URL + "/search.php?s=" + keyword;
        
        try {
            MealListResponse response = restTemplate.getForObject(url, MealListResponse.class);
            
            if (response == null || response.getMeals() == null) {
                return Collections.emptyList();
            }
            
            return response.getMeals();
        } catch (Exception e) {
            log.error("레시피 검색 중 오류 발생: keyword={}", keyword, e);
            return Collections.emptyList();
        }
    }

    /**
     * 레시피 ID로 상세 조회
     * @param mealId 레시피 ID
     * @return 레시피 상세 정보
     */
    public MealResponse getRecipeById(String mealId) {
        String url = BASE_URL + "/lookup.php?i=" + mealId;
        
        try {
            MealListResponse response = restTemplate.getForObject(url, MealListResponse.class);
            
            if (response == null || response.getMeals() == null || response.getMeals().isEmpty()) {
                return null;
            }
            
            return response.getMeals().get(0);
        } catch (Exception e) {
            log.error("레시피 상세 조회 중 오류 발생: mealId={}", mealId, e);
            return null;
        }
    }

    /**
     * 랜덤 레시피 1개 조회
     * @return 랜덤 레시피
     */
    public MealResponse getRandomRecipe() {
        String url = BASE_URL + "/random.php";
        
        try {
            MealListResponse response = restTemplate.getForObject(url, MealListResponse.class);
            
            if (response == null || response.getMeals() == null || response.getMeals().isEmpty()) {
                return null;
            }
            
            return response.getMeals().get(0);
        } catch (Exception e) {
            log.error("랜덤 레시피 조회 중 오류 발생", e);
            return null;
        }
    }

    /**
     * 랜덤 레시피 여러 개 조회
     * @param count 조회할 레시피 개수
     * @return 랜덤 레시피 목록
     */
    public List<MealResponse> getRandomRecipes(int count) {
        List<MealResponse> recipes = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            MealResponse recipe = getRandomRecipe();
            if (recipe != null) {
                recipes.add(recipe);
            }
        }
        
        return recipes;
    }

    /**
     * 카테고리 목록 조회
     * @return 카테고리 목록
     */
    public List<CategoryResponse> getCategories() {
        String url = BASE_URL + "/categories.php";
        
        try {
            CategoryListResponse response = restTemplate.getForObject(url, CategoryListResponse.class);
            
            if (response == null || response.getCategories() == null) {
                return Collections.emptyList();
            }
            
            return response.getCategories();
        } catch (Exception e) {
            log.error("카테고리 목록 조회 중 오류 발생", e);
            return Collections.emptyList();
        }
    }

    /**
     * 카테고리별 레시피 목록 조회
     * @param category 카테고리 이름
     * @return 해당 카테고리의 레시피 목록
     */
    public List<MealResponse> getRecipesByCategory(String category) {
        String url = BASE_URL + "/filter.php?c=" + category;
        
        try {
            MealListResponse response = restTemplate.getForObject(url, MealListResponse.class);
            
            if (response == null || response.getMeals() == null) {
                return Collections.emptyList();
            }
            
            // filter.php는 간단한 정보만 반환하므로, 각 레시피의 상세 정보를 조회
            List<MealResponse> detailedRecipes = new ArrayList<>();
            for (MealResponse meal : response.getMeals()) {
                if (meal.getId() != null) {
                    MealResponse detailed = getRecipeById(meal.getId());
                    if (detailed != null) {
                        detailedRecipes.add(detailed);
                    }
                }
            }
            
            return detailedRecipes;
        } catch (Exception e) {
            log.error("카테고리별 레시피 조회 중 오류 발생: category={}", category, e);
            return Collections.emptyList();
        }
    }
}
