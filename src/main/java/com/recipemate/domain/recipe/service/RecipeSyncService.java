package com.recipemate.domain.recipe.service;

import com.recipemate.domain.recipe.client.FoodSafetyClient;
import com.recipemate.domain.recipe.client.TheMealDBClient;
import com.recipemate.domain.recipe.dto.CookRecipeResponse;
import com.recipemate.domain.recipe.dto.MealResponse;
import com.recipemate.domain.recipe.entity.Recipe;
import com.recipemate.domain.recipe.entity.RecipeSource;
import com.recipemate.domain.recipe.mapper.RecipeMapper;
import com.recipemate.domain.recipe.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 레시피 동기화 서비스
 * 외부 API로부터 레시피 데이터를 가져와 DB에 저장/업데이트
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecipeSyncService {

    private final TheMealDBClient theMealDBClient;
    private final FoodSafetyClient foodSafetyClient;
    private final RecipeRepository recipeRepository;
    private final RecipeMapper recipeMapper;

    // FoodSafety API는 최대 1000건까지 조회 가능
    private static final int FOOD_SAFETY_MAX_SIZE = 1000;
    private static final int BATCH_SIZE = 100;

    /**
     * TheMealDB 레시피 동기화 (랜덤 레시피)
     * 
     * @param count 가져올 레시피 개수
     * @return 동기화 성공한 레시피 개수
     */
    @Transactional
    public int syncMealDbRandomRecipes(int count) {
        log.info("Starting TheMealDB random recipes sync: count={}", count);
        
        int syncedCount = 0;
        
        try {
            List<MealResponse> mealResponses = theMealDBClient.getRandomRecipes(count);
            
            if (mealResponses.isEmpty()) {
                log.warn("No recipes fetched from TheMealDB");
                return 0;
            }
            
            for (MealResponse mealResponse : mealResponses) {
                try {
                    if (syncMealDbRecipe(mealResponse)) {
                        syncedCount++;
                    }
                } catch (Exception e) {
                    log.error("Failed to sync recipe: mealId={}, name={}", 
                             mealResponse.getId(), mealResponse.getName(), e);
                }
            }
            
            log.info("TheMealDB sync completed: total={}, synced={}", mealResponses.size(), syncedCount);
            
        } catch (Exception e) {
            log.error("Error during TheMealDB sync", e);
        }
        
        return syncedCount;
    }

    /**
     * TheMealDB 특정 카테고리 레시피 동기화
     * 
     * @param category 카테고리명
     * @return 동기화 성공한 레시피 개수
     */
    @Transactional
    public int syncMealDbRecipesByCategory(String category) {
        log.info("Starting TheMealDB category sync: category={}", category);
        
        int syncedCount = 0;
        
        try {
            List<MealResponse> mealResponses = theMealDBClient.getRecipesByCategory(category);
            
            if (mealResponses.isEmpty()) {
                log.warn("No recipes fetched from TheMealDB for category: {}", category);
                return 0;
            }
            
            for (MealResponse mealResponse : mealResponses) {
                try {
                    if (syncMealDbRecipe(mealResponse)) {
                        syncedCount++;
                    }
                } catch (Exception e) {
                    log.error("Failed to sync recipe: mealId={}, name={}", 
                             mealResponse.getId(), mealResponse.getName(), e);
                }
            }
            
            log.info("TheMealDB category sync completed: category={}, total={}, synced={}", 
                     category, mealResponses.size(), syncedCount);
            
        } catch (Exception e) {
            log.error("Error during TheMealDB category sync: category={}", category, e);
        }
        
        return syncedCount;
    }

    /**
     * TheMealDB 단일 레시피 동기화
     * 
     * @param mealResponse TheMealDB API 응답
     * @return 동기화 성공 여부
     */
    private boolean syncMealDbRecipe(MealResponse mealResponse) {
        if (mealResponse == null || mealResponse.getId() == null) {
            log.warn("Invalid meal response: null or missing ID");
            return false;
        }
        
        String sourceApiId = mealResponse.getId();
        
        // 기존 레시피 확인
        Optional<Recipe> existingRecipe = recipeRepository.findBySourceApiAndSourceApiId(
                RecipeSource.MEAL_DB, sourceApiId);
        
        if (existingRecipe.isPresent()) {
            // 기존 레시피 업데이트
            return updateExistingRecipe(existingRecipe.get(), mealResponse);
        } else {
            // 새 레시피 생성
            return createNewRecipe(mealResponse);
        }
    }

    /**
     * 식품안전나라 레시피 전체 동기화
     * 
     * @return 동기화 성공한 레시피 개수
     */
    @Transactional
    public int syncFoodSafetyRecipes() {
        log.info("Starting FoodSafety full sync");
        
        int syncedCount = 0;
        
        try {
            // 식품안전나라 API는 최대 1000건 조회 가능
            List<CookRecipeResponse> cookRecipes = foodSafetyClient.getKoreanRecipes(1, FOOD_SAFETY_MAX_SIZE);
            
            if (cookRecipes.isEmpty()) {
                log.warn("No recipes fetched from FoodSafety");
                return 0;
            }
            
            for (CookRecipeResponse cookRecipe : cookRecipes) {
                try {
                    if (syncFoodSafetyRecipe(cookRecipe)) {
                        syncedCount++;
                    }
                } catch (Exception e) {
                    log.error("Failed to sync recipe: rcpSeq={}, name={}", 
                             cookRecipe.getRcpSeq(), cookRecipe.getRcpNm(), e);
                }
            }
            
            log.info("FoodSafety sync completed: total={}, synced={}", cookRecipes.size(), syncedCount);
            
        } catch (Exception e) {
            log.error("Error during FoodSafety sync", e);
        }
        
        return syncedCount;
    }

    /**
     * 식품안전나라 레시피 배치 동기화
     * 
     * @param start 시작 위치
     * @param end 종료 위치
     * @return 동기화 성공한 레시피 개수
     */
    @Transactional
    public int syncFoodSafetyRecipesBatch(int start, int end) {
        log.info("Starting FoodSafety batch sync: start={}, end={}", start, end);
        
        int syncedCount = 0;
        
        try {
            List<CookRecipeResponse> cookRecipes = foodSafetyClient.getKoreanRecipes(start, end);
            
            if (cookRecipes.isEmpty()) {
                log.warn("No recipes fetched from FoodSafety: start={}, end={}", start, end);
                return 0;
            }
            
            for (CookRecipeResponse cookRecipe : cookRecipes) {
                try {
                    if (syncFoodSafetyRecipe(cookRecipe)) {
                        syncedCount++;
                    }
                } catch (Exception e) {
                    log.error("Failed to sync recipe: rcpSeq={}, name={}", 
                             cookRecipe.getRcpSeq(), cookRecipe.getRcpNm(), e);
                }
            }
            
            log.info("FoodSafety batch sync completed: start={}, end={}, total={}, synced={}", 
                     start, end, cookRecipes.size(), syncedCount);
            
        } catch (Exception e) {
            log.error("Error during FoodSafety batch sync: start={}, end={}", start, end, e);
        }
        
        return syncedCount;
    }

    /**
     * 식품안전나라 단일 레시피 동기화
     * 
     * @param cookRecipe 식품안전나라 API 응답
     * @return 동기화 성공 여부
     */
    private boolean syncFoodSafetyRecipe(CookRecipeResponse cookRecipe) {
        if (cookRecipe == null || cookRecipe.getRcpSeq() == null) {
            log.warn("Invalid cook recipe response: null or missing RCP_SEQ");
            return false;
        }
        
        String sourceApiId = cookRecipe.getRcpSeq();
        
        // 기존 레시피 확인
        Optional<Recipe> existingRecipe = recipeRepository.findBySourceApiAndSourceApiId(
                RecipeSource.FOOD_SAFETY, sourceApiId);
        
        if (existingRecipe.isPresent()) {
            // 기존 레시피 업데이트
            return updateExistingRecipe(existingRecipe.get(), cookRecipe);
        } else {
            // 새 레시피 생성
            return createNewRecipe(cookRecipe);
        }
    }

    /**
     * 기존 레시피 업데이트 (TheMealDB)
     */
    private boolean updateExistingRecipe(Recipe existingRecipe, MealResponse mealResponse) {
        try {
            log.debug("Updating existing recipe: id={}, mealId={}", 
                     existingRecipe.getId(), mealResponse.getId());
            
            // 기존 재료와 단계 삭제 (orphanRemoval=true로 자동 삭제됨)
            existingRecipe.getIngredients().clear();
            existingRecipe.getSteps().clear();
            
            // 새 데이터로 매핑
            Recipe updatedRecipe = recipeMapper.toEntity(mealResponse);
            
            // 기존 레시피 정보 업데이트 (ID는 유지)
            // TODO: 필요한 필드만 업데이트하도록 개선 가능
            for (var ingredient : updatedRecipe.getIngredients()) {
                existingRecipe.addIngredient(ingredient);
            }
            
            for (var step : updatedRecipe.getSteps()) {
                existingRecipe.addStep(step);
            }
            
            existingRecipe.updateSyncTime();
            
            recipeRepository.save(existingRecipe);
            log.debug("Recipe updated successfully: id={}", existingRecipe.getId());
            
            return true;
            
        } catch (Exception e) {
            log.error("Failed to update recipe: id={}", existingRecipe.getId(), e);
            return false;
        }
    }

    /**
     * 기존 레시피 업데이트 (식품안전나라)
     */
    private boolean updateExistingRecipe(Recipe existingRecipe, CookRecipeResponse cookRecipe) {
        try {
            log.debug("Updating existing recipe: id={}, rcpSeq={}", 
                     existingRecipe.getId(), cookRecipe.getRcpSeq());
            
            // 기존 재료와 단계 삭제
            existingRecipe.getIngredients().clear();
            existingRecipe.getSteps().clear();
            
            // 새 데이터로 매핑
            Recipe updatedRecipe = recipeMapper.toEntity(cookRecipe);
            
            // 기존 레시피 정보 업데이트
            for (var ingredient : updatedRecipe.getIngredients()) {
                existingRecipe.addIngredient(ingredient);
            }
            
            for (var step : updatedRecipe.getSteps()) {
                existingRecipe.addStep(step);
            }
            
            existingRecipe.updateSyncTime();
            
            recipeRepository.save(existingRecipe);
            log.debug("Recipe updated successfully: id={}", existingRecipe.getId());
            
            return true;
            
        } catch (Exception e) {
            log.error("Failed to update recipe: id={}", existingRecipe.getId(), e);
            return false;
        }
    }

    /**
     * 새 레시피 생성 (TheMealDB)
     */
    private boolean createNewRecipe(MealResponse mealResponse) {
        try {
            Recipe recipe = recipeMapper.toEntity(mealResponse);
            
            if (recipe == null) {
                log.warn("Failed to map meal response to entity: mealId={}", mealResponse.getId());
                return false;
            }
            
            recipeRepository.save(recipe);
            log.debug("New recipe created: mealId={}, title={}", 
                     mealResponse.getId(), recipe.getTitle());
            
            return true;
            
        } catch (Exception e) {
            log.error("Failed to create recipe: mealId={}", mealResponse.getId(), e);
            return false;
        }
    }

    /**
     * 새 레시피 생성 (식품안전나라)
     */
    private boolean createNewRecipe(CookRecipeResponse cookRecipe) {
        try {
            Recipe recipe = recipeMapper.toEntity(cookRecipe);
            
            if (recipe == null) {
                log.warn("Failed to map cook recipe response to entity: rcpSeq={}", cookRecipe.getRcpSeq());
                return false;
            }
            
            recipeRepository.save(recipe);
            log.debug("New recipe created: rcpSeq={}, title={}", 
                     cookRecipe.getRcpSeq(), recipe.getTitle());
            
            return true;
            
        } catch (Exception e) {
            log.error("Failed to create recipe: rcpSeq={}", cookRecipe.getRcpSeq(), e);
            return false;
        }
    }

    /**
     * 전체 레시피 동기화 (모든 소스)
     * 
     * @return 총 동기화된 레시피 개수
     */
    @Transactional
    public int syncAllRecipes() {
        log.info("Starting full recipe synchronization");
        
        int totalSynced = 0;
        
        // 식품안전나라 전체 동기화
        totalSynced += syncFoodSafetyRecipes();
        
        // TheMealDB 랜덤 레시피 동기화 (100개)
        totalSynced += syncMealDbRandomRecipes(100);
        
        log.info("Full synchronization completed: total synced={}", totalSynced);
        
        return totalSynced;
    }
}
