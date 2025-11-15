package com.recipemate.global.config;

import com.recipemate.domain.recipe.repository.RecipeRepository;
import com.recipemate.domain.recipe.service.RecipeSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 레시피 초기 데이터 로더
 * 애플리케이션 시작 시 외부 API로부터 레시피 데이터를 가져와 DB에 초기화
 * 
 * 실행 조건:
 * 1. recipe.init.enabled=true 설정
 * 2. recipe.init.force=true이거나 DB에 레시피가 없는 경우
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
    name = "recipe.init.enabled",
    havingValue = "true",
    matchIfMissing = false
)
public class RecipeDataInitializer implements ApplicationRunner {

    private final RecipeSyncService recipeSyncService;
    private final RecipeRepository recipeRepository;
    
    @Value("${recipe.init.force:false}")
    private boolean forceInit;

    @Override
    public void run(ApplicationArguments args) {
        // DB에 이미 데이터가 있고 강제 초기화가 아니면 스킵
        long existingCount = recipeRepository.count();
        if (existingCount > 0 && !forceInit) {
            log.info("Recipe data already exists ({} recipes). Skipping initialization. " +
                     "Set recipe.init.force=true to reload data.", existingCount);
            return;
        }
        
        log.info("Starting initial recipe data synchronization (force={})", forceInit);
        
        try {
            // 식품안전나라 전체 데이터 동기화
            log.info("Syncing FoodSafety recipes...");
            int foodSafetyCount = recipeSyncService.syncFoodSafetyRecipes();
            log.info("FoodSafety sync completed: {} recipes", foodSafetyCount);

            // TheMealDB 랜덤 레시피 100개 동기화
            log.info("Syncing TheMealDB random recipes...");
            int mealDbCount = recipeSyncService.syncMealDbRandomRecipes(100);
            log.info("TheMealDB sync completed: {} recipes", mealDbCount);
            
            int totalCount = foodSafetyCount + mealDbCount;
            log.info("Initial recipe data synchronization completed: total {} recipes loaded", totalCount);
        } catch (Exception e) {
            log.error("Failed to initialize recipe data", e);
            // 초기화 실패해도 애플리케이션은 계속 실행
            // 추후 스케줄러를 통해 재시도 가능
        }
    }
}
