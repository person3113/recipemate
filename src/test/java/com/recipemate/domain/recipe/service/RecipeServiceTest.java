package com.recipemate.domain.recipe.service;

import com.recipemate.domain.recipe.client.FoodSafetyClient;
import com.recipemate.domain.recipe.client.TheMealDBClient;
import com.recipemate.domain.recipe.dto.*;
import com.recipemate.global.exception.CustomException;
import com.recipemate.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecipeService 테스트")
class RecipeServiceTest {

    @Mock
    private TheMealDBClient theMealDBClient;

    @Mock
    private FoodSafetyClient foodSafetyClient;

    @InjectMocks
    private RecipeService recipeService;

    @Nested
    @DisplayName("레시피 검색")
    class SearchRecipes {

        @Test
        @DisplayName("두 API의 결과를 통합하여 반환한다")
        void searchRecipes_BothAPIs_Success() {
            // given
            String keyword = "chicken";
            
            // TheMealDB 응답
            MealResponse mealResponse = MealResponse.builder()
                    .id("52772")
                    .name("Teriyaki Chicken Casserole")
                    .thumbnail("https://www.themealdb.com/images/media/meals/wvpsxx1468256321.jpg")
                    .category("Chicken")
                    .build();
            
            // 식품안전나라 응답
            CookRecipeResponse cookRecipeResponse = CookRecipeResponse.builder()
                    .rcpSeq("1")
                    .rcpNm("닭볶음탕")
                    .attFileNoMain("http://example.com/image.jpg")
                    .rcpPat2("반찬")
                    .build();

            when(theMealDBClient.searchRecipes(keyword))
                    .thenReturn(Arrays.asList(mealResponse));
            when(foodSafetyClient.searchRecipesByName(keyword, 1, 100))
                    .thenReturn(Arrays.asList(cookRecipeResponse));

            // when
            RecipeListResponse result = recipeService.searchRecipes(keyword);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getRecipes()).hasSize(2);
            assertThat(result.getTotalCount()).isEqualTo(2);
            assertThat(result.getSource()).isEqualTo("both");
            
            // TheMealDB 결과 검증
            RecipeListResponse.RecipeSimpleInfo mealResult = result.getRecipes().stream()
                    .filter(r -> r.getSource().equals("themealdb"))
                    .findFirst()
                    .orElseThrow();
            assertThat(mealResult.getId()).isEqualTo("meal-52772");
            assertThat(mealResult.getName()).isEqualTo("Teriyaki Chicken Casserole");
            assertThat(mealResult.getCategory()).isEqualTo("Chicken");
            
            // 식품안전나라 결과 검증
            RecipeListResponse.RecipeSimpleInfo foodResult = result.getRecipes().stream()
                    .filter(r -> r.getSource().equals("foodsafety"))
                    .findFirst()
                    .orElseThrow();
            assertThat(foodResult.getId()).isEqualTo("food-1");
            assertThat(foodResult.getName()).isEqualTo("닭볶음탕");
            assertThat(foodResult.getCategory()).isEqualTo("반찬");
        }

        @Test
        @DisplayName("TheMealDB 결과만 있을 때 정상 반환한다")
        void searchRecipes_OnlyTheMealDB_Success() {
            // given
            String keyword = "pasta";
            
            MealResponse mealResponse = MealResponse.builder()
                    .id("52771")
                    .name("Spicy Arrabiata Penne")
                    .thumbnail("https://www.themealdb.com/images/media/meals/ustsqw1468250014.jpg")
                    .category("Pasta")
                    .build();

            when(theMealDBClient.searchRecipes(keyword))
                    .thenReturn(Arrays.asList(mealResponse));
            when(foodSafetyClient.searchRecipesByName(keyword, 1, 100))
                    .thenReturn(Collections.emptyList());

            // when
            RecipeListResponse result = recipeService.searchRecipes(keyword);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getRecipes()).hasSize(1);
            assertThat(result.getTotalCount()).isEqualTo(1);
            assertThat(result.getSource()).isEqualTo("themealdb");
            assertThat(result.getRecipes().get(0).getSource()).isEqualTo("themealdb");
        }

        @Test
        @DisplayName("식품안전나라 결과만 있을 때 정상 반환한다")
        void searchRecipes_OnlyFoodSafety_Success() {
            // given
            String keyword = "김치";
            
            CookRecipeResponse cookRecipeResponse = CookRecipeResponse.builder()
                    .rcpSeq("2")
                    .rcpNm("김치찌개")
                    .attFileNoMain("http://example.com/kimchi.jpg")
                    .rcpPat2("국")
                    .build();

            when(theMealDBClient.searchRecipes(keyword))
                    .thenReturn(Collections.emptyList());
            when(foodSafetyClient.searchRecipesByName(keyword, 1, 100))
                    .thenReturn(Arrays.asList(cookRecipeResponse));

            // when
            RecipeListResponse result = recipeService.searchRecipes(keyword);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getRecipes()).hasSize(1);
            assertThat(result.getTotalCount()).isEqualTo(1);
            assertThat(result.getSource()).isEqualTo("foodsafety");
            assertThat(result.getRecipes().get(0).getSource()).isEqualTo("foodsafety");
        }

        @Test
        @DisplayName("검색 결과가 없을 때 빈 리스트를 반환한다")
        void searchRecipes_NoResults_ReturnEmptyList() {
            // given
            String keyword = "nonexistent";
            
            when(theMealDBClient.searchRecipes(keyword))
                    .thenReturn(Collections.emptyList());
            when(foodSafetyClient.searchRecipesByName(keyword, 1, 100))
                    .thenReturn(Collections.emptyList());

            // when
            RecipeListResponse result = recipeService.searchRecipes(keyword);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getRecipes()).isEmpty();
            assertThat(result.getTotalCount()).isEqualTo(0);
            assertThat(result.getSource()).isEqualTo("both");
        }

        @Test
        @DisplayName("검색어가 null이면 예외가 발생한다")
        void searchRecipes_NullKeyword_ThrowException() {
            // when & then
            assertThatThrownBy(() -> recipeService.searchRecipes(null))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT_VALUE);
        }

        @Test
        @DisplayName("검색어가 빈 문자열이면 예외가 발생한다")
        void searchRecipes_EmptyKeyword_ThrowException() {
            // when & then
            assertThatThrownBy(() -> recipeService.searchRecipes(""))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    @Nested
    @DisplayName("레시피 상세 조회")
    class GetRecipeDetail {

        @Test
        @DisplayName("TheMealDB ID로 상세 정보를 조회한다")
        void getRecipeDetail_TheMealDBId_Success() {
            // given
            String apiId = "meal-52772";
            
            MealResponse mealResponse = MealResponse.builder()
                    .id("52772")
                    .name("Teriyaki Chicken Casserole")
                    .thumbnail("https://www.themealdb.com/images/media/meals/wvpsxx1468256321.jpg")
                    .category("Chicken")
                    .area("Japanese")
                    .instructions("Preheat oven to 350° F...")
                    .youtubeUrl("https://www.youtube.com/watch?v=abc123")
                    .sourceUrl("https://www.example.com")
                    .ingredient1("chicken")
                    .measure1("500g")
                    .ingredient2("soy sauce")
                    .measure2("2 tbsp")
                    .build();

            when(theMealDBClient.getRecipeById("52772"))
                    .thenReturn(mealResponse);

            // when
            RecipeDetailResponse result = recipeService.getRecipeDetail(apiId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("meal-52772");
            assertThat(result.getName()).isEqualTo("Teriyaki Chicken Casserole");
            assertThat(result.getCategory()).isEqualTo("Chicken");
            assertThat(result.getArea()).isEqualTo("Japanese");
            assertThat(result.getInstructions()).startsWith("Preheat oven to 350° F...");
            assertThat(result.getYoutubeUrl()).isEqualTo("https://www.youtube.com/watch?v=abc123");
            assertThat(result.getSourceUrl()).isEqualTo("https://www.example.com");
            assertThat(result.getSource()).isEqualTo("themealdb");
            assertThat(result.getIngredients()).hasSize(2);
            assertThat(result.getIngredients().get(0).getName()).isEqualTo("chicken");
            assertThat(result.getIngredients().get(0).getMeasure()).isEqualTo("500g");
        }

        @Test
        @DisplayName("식품안전나라 ID로 상세 정보를 조회한다")
        void getRecipeDetail_FoodSafetyId_Success() {
            // given
            String apiId = "food-1";
            
            CookRecipeResponse cookRecipeResponse = CookRecipeResponse.builder()
                    .rcpSeq("1")
                    .rcpNm("닭볶음탕")
                    .attFileNoMain("http://example.com/image.jpg")
                    .rcpPat2("반찬")
                    .rcpPartsDtls("닭고기, 감자, 당근, 양파")
                    .infoWgt("1000g")
                    .infoEng("350 kcal")
                    .infoCar("30g")
                    .infoPro("40g")
                    .infoFat("10g")
                    .infoNa("800mg")
                    .manual01("닭고기를 손질한다")
                    .manualImg01("http://example.com/step1.jpg")
                    .manual02("야채를 썬다")
                    .manualImg02("http://example.com/step2.jpg")
                    .build();

            when(foodSafetyClient.getKoreanRecipes(1, 1))
                    .thenReturn(Arrays.asList(cookRecipeResponse));

            // when
            RecipeDetailResponse result = recipeService.getRecipeDetail(apiId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("food-1");
            assertThat(result.getName()).isEqualTo("닭볶음탕");
            assertThat(result.getCategory()).isEqualTo("반찬");
            assertThat(result.getSource()).isEqualTo("foodsafety");
            
            // 재료 검증
            assertThat(result.getIngredients()).hasSize(4);
            assertThat(result.getIngredients()).extracting("name")
                    .containsExactly("닭고기", "감자", "당근", "양파");
            
            // 조리 단계 검증
            assertThat(result.getManualSteps()).hasSize(2);
            assertThat(result.getManualSteps().get(0).getStepNumber()).isEqualTo(1);
            assertThat(result.getManualSteps().get(0).getDescription()).isEqualTo("닭고기를 손질한다");
            assertThat(result.getManualSteps().get(0).getImageUrl()).isEqualTo("http://example.com/step1.jpg");
            
            // 영양 정보 검증
            assertThat(result.getNutritionInfo()).isNotNull();
            assertThat(result.getNutritionInfo().getWeight()).isEqualTo("1000g");
            assertThat(result.getNutritionInfo().getEnergy()).isEqualTo("350 kcal");
        }

        @Test
        @DisplayName("존재하지 않는 레시피 ID면 예외가 발생한다")
        void getRecipeDetail_NotFound_ThrowException() {
            // given
            String apiId = "meal-99999";
            
            when(theMealDBClient.getRecipeById("99999"))
                    .thenReturn(null);

            // when & then
            assertThatThrownBy(() -> recipeService.getRecipeDetail(apiId))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RECIPE_NOT_FOUND);
        }

        @Test
        @DisplayName("잘못된 ID 형식이면 예외가 발생한다")
        void getRecipeDetail_InvalidFormat_ThrowException() {
            // when & then
            assertThatThrownBy(() -> recipeService.getRecipeDetail("invalid-id"))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT_VALUE);
        }

        @Test
        @DisplayName("ID가 null이면 예외가 발생한다")
        void getRecipeDetail_NullId_ThrowException() {
            // when & then
            assertThatThrownBy(() -> recipeService.getRecipeDetail(null))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    @Nested
    @DisplayName("랜덤 레시피 조회")
    class GetRandomRecipes {

        @Test
        @DisplayName("지정한 개수만큼 랜덤 레시피를 반환한다")
        void getRandomRecipes_ValidCount_Success() {
            // given
            int count = 3;
            
            MealResponse meal1 = MealResponse.builder()
                    .id("1")
                    .name("Recipe 1")
                    .thumbnail("http://example.com/1.jpg")
                    .category("Category1")
                    .build();
            
            MealResponse meal2 = MealResponse.builder()
                    .id("2")
                    .name("Recipe 2")
                    .thumbnail("http://example.com/2.jpg")
                    .category("Category2")
                    .build();
            
            MealResponse meal3 = MealResponse.builder()
                    .id("3")
                    .name("Recipe 3")
                    .thumbnail("http://example.com/3.jpg")
                    .category("Category3")
                    .build();

            when(theMealDBClient.getRandomRecipes(count))
                    .thenReturn(Arrays.asList(meal1, meal2, meal3));

            // when
            RecipeListResponse result = recipeService.getRandomRecipes(count);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getRecipes()).hasSize(3);
            assertThat(result.getTotalCount()).isEqualTo(3);
            assertThat(result.getSource()).isEqualTo("themealdb");
            assertThat(result.getRecipes()).extracting("name")
                    .containsExactly("Recipe 1", "Recipe 2", "Recipe 3");
        }

        @Test
        @DisplayName("개수가 0이면 빈 리스트를 반환한다")
        void getRandomRecipes_ZeroCount_ReturnEmptyList() {
            // given
            int count = 0;
            
            when(theMealDBClient.getRandomRecipes(count))
                    .thenReturn(Collections.emptyList());

            // when
            RecipeListResponse result = recipeService.getRandomRecipes(count);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getRecipes()).isEmpty();
            assertThat(result.getTotalCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("개수가 음수면 예외가 발생한다")
        void getRandomRecipes_NegativeCount_ThrowException() {
            // when & then
            assertThatThrownBy(() -> recipeService.getRandomRecipes(-1))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT_VALUE);
        }

        @Test
        @DisplayName("개수가 100을 초과하면 예외가 발생한다")
        void getRandomRecipes_CountExceedsMax_ThrowException() {
            // when & then
            assertThatThrownBy(() -> recipeService.getRandomRecipes(101))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    @Nested
    @DisplayName("카테고리 목록 조회")
    class GetCategories {

        @Test
        @DisplayName("TheMealDB 카테고리 목록을 반환한다")
        void getCategories_Success() {
            // given
            CategoryResponse category1 = new CategoryResponse(
                    "1",
                    "Beef",
                    "http://example.com/beef.jpg",
                    "Beef dishes"
            );
            
            CategoryResponse category2 = new CategoryResponse(
                    "2",
                    "Chicken",
                    "http://example.com/chicken.jpg",
                    "Chicken dishes"
            );

            when(theMealDBClient.getCategories())
                    .thenReturn(Arrays.asList(category1, category2));

            // when
            List<CategoryResponse> result = recipeService.getCategories();

            // then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(2);
            assertThat(result).extracting("name")
                    .containsExactly("Beef", "Chicken");
        }

        @Test
        @DisplayName("카테고리가 없으면 빈 리스트를 반환한다")
        void getCategories_NoCategories_ReturnEmptyList() {
            // given
            when(theMealDBClient.getCategories())
                    .thenReturn(Collections.emptyList());

            // when
            List<CategoryResponse> result = recipeService.getCategories();

            // then
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
        }
    }
}
