package com.recipemate.domain.recipe.service;

import com.recipemate.domain.recipe.client.FoodSafetyClient;
import com.recipemate.domain.recipe.client.TheMealDBClient;
import com.recipemate.domain.recipe.dto.CategoryResponse;
import com.recipemate.domain.recipe.dto.MealResponse;
import com.recipemate.domain.recipe.dto.RecipeDetailResponse;
import com.recipemate.domain.recipe.dto.RecipeListResponse;
import com.recipemate.global.config.CacheConfig;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.TestPropertySource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * RecipeService 캐싱 테스트
 * Redis를 사용한 캐싱 동작 검증
 * 
 * 이 테스트는 Redis 서버가 실행 중일 때만 실행됩니다.
 * Redis가 없는 환경에서는 건너뜁니다.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "spring.cache.type=redis",
        "spring.data.redis.host=localhost",
        "spring.data.redis.port=6379"
})
@DisplayName("RecipeService 캐싱 테스트")
@EnabledIfSystemProperty(named = "test.redis.enabled", matches = "true", disabledReason = "Redis 서버가 실행 중이지 않아 테스트를 건너뜁니다")
class RecipeServiceCacheTest {

    @Autowired
    private RecipeService recipeService;

    @MockBean
    private TheMealDBClient theMealDBClient;

    @MockBean
    private FoodSafetyClient foodSafetyClient;

    @Autowired
    private CacheManager cacheManager;

    @Nested
    @DisplayName("레시피 상세 조회 캐싱")
    class GetRecipeDetailCaching {

        @Test
        @DisplayName("같은 레시피 ID로 두 번 조회 시 캐시에서 반환한다")
        void getRecipeDetail_SameId_UseCache() {
            // given
            String apiId = "meal-52772";
            
            MealResponse mealResponse = MealResponse.builder()
                    .id("52772")
                    .name("Teriyaki Chicken Casserole")
                    .thumbnail("https://www.themealdb.com/images/media/meals/wvpsxx1468256321.jpg")
                    .category("Chicken")
                    .area("Japanese")
                    .instructions("Preheat oven to 350° F...")
                    .ingredient1("chicken")
                    .measure1("500g")
                    .build();

            when(theMealDBClient.getRecipeById("52772"))
                    .thenReturn(mealResponse);

            // 캐시 초기화
            Objects.requireNonNull(cacheManager.getCache(CacheConfig.RECIPES_CACHE)).clear();

            // when - 첫 번째 호출
            RecipeDetailResponse firstResult = recipeService.getRecipeDetail(apiId);

            // then - API가 한 번 호출됨
            verify(theMealDBClient, times(1)).getRecipeById("52772");
            assertThat(firstResult.getId()).isEqualTo("meal-52772");

            // when - 두 번째 호출
            RecipeDetailResponse secondResult = recipeService.getRecipeDetail(apiId);

            // then - API가 추가로 호출되지 않음 (여전히 1번)
            verify(theMealDBClient, times(1)).getRecipeById("52772");
            assertThat(secondResult.getId()).isEqualTo("meal-52772");
            assertThat(secondResult.getName()).isEqualTo(firstResult.getName());
        }

        @Test
        @DisplayName("다른 레시피 ID로 조회 시 캐시를 공유하지 않는다")
        void getRecipeDetail_DifferentIds_SeparateCaches() {
            // given
            String apiId1 = "meal-52772";
            String apiId2 = "meal-52771";
            
            MealResponse meal1 = MealResponse.builder()
                    .id("52772")
                    .name("Teriyaki Chicken Casserole")
                    .thumbnail("http://example.com/meal1.jpg")
                    .category("Chicken")
                    .ingredient1("chicken")
                    .measure1("500g")
                    .build();

            MealResponse meal2 = MealResponse.builder()
                    .id("52771")
                    .name("Spicy Arrabiata Penne")
                    .thumbnail("http://example.com/meal2.jpg")
                    .category("Pasta")
                    .ingredient1("penne")
                    .measure1("200g")
                    .build();

            when(theMealDBClient.getRecipeById("52772")).thenReturn(meal1);
            when(theMealDBClient.getRecipeById("52771")).thenReturn(meal2);

            // 캐시 초기화
            Objects.requireNonNull(cacheManager.getCache(CacheConfig.RECIPES_CACHE)).clear();

            // when - 첫 번째 레시피 조회
            RecipeDetailResponse result1 = recipeService.getRecipeDetail(apiId1);
            
            // then
            verify(theMealDBClient, times(1)).getRecipeById("52772");
            assertThat(result1.getName()).isEqualTo("Teriyaki Chicken Casserole");

            // when - 두 번째 레시피 조회
            RecipeDetailResponse result2 = recipeService.getRecipeDetail(apiId2);
            
            // then - 다른 레시피이므로 API 호출됨
            verify(theMealDBClient, times(1)).getRecipeById("52771");
            assertThat(result2.getName()).isEqualTo("Spicy Arrabiata Penne");
        }

        @Test
        @DisplayName("캐시 초기화 후 다시 API를 호출한다")
        void getRecipeDetail_AfterCacheClear_CallsAPI() {
            // given
            String apiId = "meal-52772";
            
            MealResponse mealResponse = MealResponse.builder()
                    .id("52772")
                    .name("Teriyaki Chicken Casserole")
                    .thumbnail("http://example.com/meal.jpg")
                    .category("Chicken")
                    .ingredient1("chicken")
                    .measure1("500g")
                    .build();

            when(theMealDBClient.getRecipeById("52772"))
                    .thenReturn(mealResponse);

            // 캐시 초기화
            Objects.requireNonNull(cacheManager.getCache(CacheConfig.RECIPES_CACHE)).clear();

            // when - 첫 번째 호출
            recipeService.getRecipeDetail(apiId);
            verify(theMealDBClient, times(1)).getRecipeById("52772");

            // when - 캐시 초기화
            Objects.requireNonNull(cacheManager.getCache(CacheConfig.RECIPES_CACHE)).clear();

            // when - 두 번째 호출
            recipeService.getRecipeDetail(apiId);
            
            // then - 캐시가 초기화되었으므로 API 다시 호출됨
            verify(theMealDBClient, times(2)).getRecipeById("52772");
        }
    }

    @Nested
    @DisplayName("랜덤 레시피 조회 캐싱")
    class GetRandomRecipesCaching {

        @Test
        @DisplayName("같은 개수로 두 번 조회 시 캐시에서 반환한다")
        void getRandomRecipes_SameCount_UseCache() {
            // given
            int count = 3;
            
            List<MealResponse> meals = Arrays.asList(
                    MealResponse.builder().id("1").name("Recipe 1").thumbnail("http://example.com/1.jpg").category("Cat1").build(),
                    MealResponse.builder().id("2").name("Recipe 2").thumbnail("http://example.com/2.jpg").category("Cat2").build(),
                    MealResponse.builder().id("3").name("Recipe 3").thumbnail("http://example.com/3.jpg").category("Cat3").build()
            );

            when(theMealDBClient.getRandomRecipes(count))
                    .thenReturn(meals);

            // 캐시 초기화
            Objects.requireNonNull(cacheManager.getCache(CacheConfig.RECIPES_CACHE)).clear();

            // when - 첫 번째 호출
            RecipeListResponse firstResult = recipeService.getRandomRecipes(count);

            // then - API가 한 번 호출됨
            verify(theMealDBClient, times(1)).getRandomRecipes(count);
            assertThat(firstResult.getRecipes()).hasSize(3);

            // when - 두 번째 호출
            RecipeListResponse secondResult = recipeService.getRandomRecipes(count);

            // then - API가 추가로 호출되지 않음 (여전히 1번)
            verify(theMealDBClient, times(1)).getRandomRecipes(count);
            assertThat(secondResult.getRecipes()).hasSize(3);
        }

        @Test
        @DisplayName("다른 개수로 조회 시 캐시를 공유하지 않는다")
        void getRandomRecipes_DifferentCounts_SeparateCaches() {
            // given
            List<MealResponse> meals1 = Arrays.asList(
                    MealResponse.builder().id("1").name("Recipe 1").thumbnail("http://example.com/1.jpg").category("Cat1").build()
            );

            List<MealResponse> meals2 = Arrays.asList(
                    MealResponse.builder().id("2").name("Recipe 2").thumbnail("http://example.com/2.jpg").category("Cat2").build(),
                    MealResponse.builder().id("3").name("Recipe 3").thumbnail("http://example.com/3.jpg").category("Cat3").build()
            );

            when(theMealDBClient.getRandomRecipes(1)).thenReturn(meals1);
            when(theMealDBClient.getRandomRecipes(2)).thenReturn(meals2);

            // 캐시 초기화
            Objects.requireNonNull(cacheManager.getCache(CacheConfig.RECIPES_CACHE)).clear();

            // when - count=1로 조회
            RecipeListResponse result1 = recipeService.getRandomRecipes(1);
            verify(theMealDBClient, times(1)).getRandomRecipes(1);
            assertThat(result1.getRecipes()).hasSize(1);

            // when - count=2로 조회
            RecipeListResponse result2 = recipeService.getRandomRecipes(2);
            verify(theMealDBClient, times(1)).getRandomRecipes(2);
            assertThat(result2.getRecipes()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("카테고리 목록 조회 캐싱")
    class GetCategoriesCaching {

        @Test
        @DisplayName("두 번 조회 시 캐시에서 반환한다")
        void getCategories_MultipleCalls_UseCache() {
            // given
            List<CategoryResponse> categories = Arrays.asList(
                    new CategoryResponse("1", "Beef", "http://example.com/beef.jpg", "Beef dishes"),
                    new CategoryResponse("2", "Chicken", "http://example.com/chicken.jpg", "Chicken dishes")
            );

            when(theMealDBClient.getCategories())
                    .thenReturn(categories);

            // 캐시 초기화
            Objects.requireNonNull(cacheManager.getCache(CacheConfig.RECIPES_CACHE)).clear();

            // when - 첫 번째 호출
            List<CategoryResponse> firstResult = recipeService.getCategories();

            // then - API가 한 번 호출됨
            verify(theMealDBClient, times(1)).getCategories();
            assertThat(firstResult).hasSize(2);

            // when - 두 번째 호출
            List<CategoryResponse> secondResult = recipeService.getCategories();

            // then - API가 추가로 호출되지 않음 (여전히 1번)
            verify(theMealDBClient, times(1)).getCategories();
            assertThat(secondResult).hasSize(2);
        }
    }

    @Nested
    @DisplayName("레시피 검색 캐싱")
    class SearchRecipesCaching {

        @Test
        @DisplayName("같은 검색어로 두 번 검색 시 캐시에서 반환한다")
        void searchRecipes_SameKeyword_UseCache() {
            // given
            String keyword = "chicken";
            
            MealResponse mealResponse = MealResponse.builder()
                    .id("52772")
                    .name("Teriyaki Chicken Casserole")
                    .thumbnail("http://example.com/chicken.jpg")
                    .category("Chicken")
                    .build();

            when(theMealDBClient.searchRecipes(keyword))
                    .thenReturn(Arrays.asList(mealResponse));
            when(foodSafetyClient.searchRecipesByName(anyString(), anyInt(), anyInt()))
                    .thenReturn(Collections.emptyList());

            // 캐시 초기화
            Objects.requireNonNull(cacheManager.getCache(CacheConfig.RECIPES_CACHE)).clear();

            // when - 첫 번째 호출
            RecipeListResponse firstResult = recipeService.searchRecipes(keyword);

            // then - API가 한 번 호출됨
            verify(theMealDBClient, times(1)).searchRecipes(keyword);
            assertThat(firstResult.getRecipes()).hasSize(1);

            // when - 두 번째 호출
            RecipeListResponse secondResult = recipeService.searchRecipes(keyword);

            // then - API가 추가로 호출되지 않음 (여전히 1번)
            verify(theMealDBClient, times(1)).searchRecipes(keyword);
            assertThat(secondResult.getRecipes()).hasSize(1);
        }

        @Test
        @DisplayName("다른 검색어로 검색 시 캐시를 공유하지 않는다")
        void searchRecipes_DifferentKeywords_SeparateCaches() {
            // given
            MealResponse meal1 = MealResponse.builder()
                    .id("52772")
                    .name("Teriyaki Chicken Casserole")
                    .thumbnail("http://example.com/chicken.jpg")
                    .category("Chicken")
                    .build();

            MealResponse meal2 = MealResponse.builder()
                    .id("52771")
                    .name("Spicy Arrabiata Penne")
                    .thumbnail("http://example.com/pasta.jpg")
                    .category("Pasta")
                    .build();

            when(theMealDBClient.searchRecipes("chicken")).thenReturn(Arrays.asList(meal1));
            when(theMealDBClient.searchRecipes("pasta")).thenReturn(Arrays.asList(meal2));
            when(foodSafetyClient.searchRecipesByName(anyString(), anyInt(), anyInt()))
                    .thenReturn(Collections.emptyList());

            // 캐시 초기화
            Objects.requireNonNull(cacheManager.getCache(CacheConfig.RECIPES_CACHE)).clear();

            // when - "chicken"으로 검색
            RecipeListResponse result1 = recipeService.searchRecipes("chicken");
            verify(theMealDBClient, times(1)).searchRecipes("chicken");
            assertThat(result1.getRecipes().get(0).getName()).contains("Chicken");

            // when - "pasta"로 검색
            RecipeListResponse result2 = recipeService.searchRecipes("pasta");
            verify(theMealDBClient, times(1)).searchRecipes("pasta");
            assertThat(result2.getRecipes().get(0).getName()).contains("Penne");
        }
    }
}
