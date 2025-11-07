package com.recipemate.domain.recipe.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 레시피 동기화 스케줄러
 * 정기적으로 외부 API로부터 레시피 데이터를 동기화
 *
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
    name = "recipe.sync.enabled",
    havingValue = "true",
    matchIfMissing = false
)
public class RecipeSyncScheduler {

    private final RecipeSyncService recipeSyncService;

    /**
     * 정기 레시피 동기화
     * 기본: 매주 일요일 새벽 3시
     * 
     * Cron 표현식: 초 분 시 일 월 요일
     * - 0 0 3 * * SUN = 매주 일요일 03:00:00
     * - 0 0 2 * * * = 매일 02:00:00 (비권장: API 호출 낭비)
     * 
     * 실무 권장:
     * - 정적 데이터(식품안전나라): 월 1회 (0 0 3 1 * *)
     * - 동적 데이터(TheMealDB): 주 1회 (0 0 3 * * SUN)
     */
    @Scheduled(cron = "${recipe.sync.cron:0 0 3 * * SUN}")
    public void scheduledSync() {
        log.info("Starting scheduled recipe synchronization");
        
        try {
            // TheMealDB만 동기화 (새 레시피 추가 가능성)
            log.info("Syncing TheMealDB recipes...");
            int mealDbCount = recipeSyncService.syncMealDbRandomRecipes(50);
            log.info("TheMealDB sync completed: {} recipes", mealDbCount);
            
            // 식품안전나라는 필요 시에만 (설정 변경 가능)
            // int foodSafetyCount = recipeSyncService.syncFoodSafetyRecipes();
            
            log.info("Scheduled sync completed successfully");
            
        } catch (Exception e) {
            log.error("Scheduled sync failed", e);
        }
    }

    /**
     * TheMealDB 시간별 동기화 (선택적, 기본 비활성화)
     * 실무에서는 거의 사용하지 않음 - API 호출 낭비
     * 
     * @deprecated 실무에서는 사용 권장하지 않음
     */
    @Deprecated
    @Scheduled(cron = "${recipe.sync.mealdb.cron:0 0 * * * *}")
    @ConditionalOnProperty(
        name = "recipe.sync.mealdb.hourly.enabled",
        havingValue = "true",
        matchIfMissing = false
    )
    public void scheduledMealDbHourlySync() {
        log.info("Starting scheduled TheMealDB hourly synchronization");
        
        try {
            int syncedCount = recipeSyncService.syncMealDbRandomRecipes(10);
            log.info("Scheduled TheMealDB hourly sync completed: synced={}", syncedCount);
            
        } catch (Exception e) {
            log.error("Scheduled TheMealDB hourly sync failed", e);
        }
    }
}
