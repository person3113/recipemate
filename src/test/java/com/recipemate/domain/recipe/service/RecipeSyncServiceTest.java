//package com.recipemate.domain.recipe.service;
//
//import com.recipemate.domain.recipe.client.FoodSafetyClient;
//import com.recipemate.domain.recipe.client.TheMealDBClient;
//import com.recipemate.domain.recipe.dto.CookRecipeResponse;
//import com.recipemate.domain.recipe.dto.MealResponse;
//import com.recipemate.domain.recipe.entity.Recipe;
//import com.recipemate.domain.recipe.entity.RecipeSource;
//import com.recipemate.domain.recipe.mapper.RecipeMapper;
//import com.recipemate.domain.recipe.repository.RecipeRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.time.LocalDateTime;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.List;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.BDDMockito.given;
//import static org.mockito.Mockito.*;
//
///**
// * RecipeSyncService 테스트
// */
//@ExtendWith(MockitoExtension.class)
//class RecipeSyncServiceTest {
//
//    @Mock
//    private TheMealDBClient theMealDBClient;
//
//    @Mock
//    private FoodSafetyClient foodSafetyClient;
//
//    @Mock
//    private RecipeRepository recipeRepository;
//
//    @Mock
//    private RecipeMapper recipeMapper;
//
//    @InjectMocks
//    private RecipeSyncService recipeSyncService;
//
//    private MealResponse sampleMealResponse;
//    private CookRecipeResponse sampleCookRecipeResponse;
//    private Recipe sampleRecipe;
//
//    @BeforeEach
//    void setUp() {
//        // TheMealDB 샘플 응답
//        sampleMealResponse = new MealResponse();
//        sampleMealResponse.setId("52772");
//        sampleMealResponse.setName("Teriyaki Chicken Casserole");
//        sampleMealResponse.setCategory("Chicken");
//        sampleMealResponse.setArea("Japanese");
//
//        // 식품안전나라 샘플 응답
//        sampleCookRecipeResponse = new CookRecipeResponse();
//        sampleCookRecipeResponse.setRcpSeq("1");
//        sampleCookRecipeResponse.setRcpNm("된장찌개");
//        sampleCookRecipeResponse.setRcpPat2("국");
//
//        // 샘플 Recipe 엔티티
//        sampleRecipe = Recipe.builder()
//                .id(1L)
//                .title("Teriyaki Chicken Casserole")
//                .category("Chicken")
//                .area("Japanese")
//                .sourceApi(RecipeSource.MEAL_DB)
//                .sourceApiId("52772")
//                .lastSyncedAt(LocalDateTime.now())
//                .build();
//    }
//
//    @Test
//    @DisplayName("TheMealDB 랜덤 레시피 동기화 - 새 레시피 생성")
//    void syncMealDbRandomRecipes_CreateNew_Success() {
//        // given
//        int count = 2;
//        List<MealResponse> mealResponses = Arrays.asList(sampleMealResponse, sampleMealResponse);
//
//        given(theMealDBClient.getRandomRecipes(count)).willReturn(mealResponses);
//        given(recipeRepository.findBySourceApiAndSourceApiId(eq(RecipeSource.MEAL_DB), anyString()))
//                .willReturn(Optional.empty());
//        given(recipeMapper.toEntity(any(MealResponse.class))).willReturn(sampleRecipe);
//        given(recipeRepository.save(any(Recipe.class))).willReturn(sampleRecipe);
//
//        // when
//        int syncedCount = recipeSyncService.syncMealDbRandomRecipes(count);
//
//        // then
//        assertThat(syncedCount).isEqualTo(2);
//        verify(theMealDBClient, times(1)).getRandomRecipes(count);
//        verify(recipeRepository, times(2)).save(any(Recipe.class));
//    }
//
//    @Test
//    @DisplayName("TheMealDB 랜덤 레시피 동기화 - 기존 레시피 업데이트")
//    void syncMealDbRandomRecipes_UpdateExisting_Success() {
//        // given
//        int count = 1;
//        List<MealResponse> mealResponses = Collections.singletonList(sampleMealResponse);
//
//        given(theMealDBClient.getRandomRecipes(count)).willReturn(mealResponses);
//        given(recipeRepository.findBySourceApiAndSourceApiId(RecipeSource.MEAL_DB, "52772"))
//                .willReturn(Optional.of(sampleRecipe));
//        given(recipeMapper.toEntity(any(MealResponse.class))).willReturn(sampleRecipe);
//        given(recipeRepository.save(any(Recipe.class))).willReturn(sampleRecipe);
//
//        // when
//        int syncedCount = recipeSyncService.syncMealDbRandomRecipes(count);
//
//        // then
//        assertThat(syncedCount).isEqualTo(1);
//        verify(recipeRepository, times(1)).save(any(Recipe.class));
//    }
//
//    @Test
//    @DisplayName("TheMealDB 랜덤 레시피 동기화 - 빈 응답")
//    void syncMealDbRandomRecipes_EmptyResponse_ReturnsZero() {
//        // given
//        int count = 5;
//        given(theMealDBClient.getRandomRecipes(count)).willReturn(Collections.emptyList());
//
//        // when
//        int syncedCount = recipeSyncService.syncMealDbRandomRecipes(count);
//
//        // then
//        assertThat(syncedCount).isEqualTo(0);
//        verify(recipeRepository, never()).save(any(Recipe.class));
//    }
//
//    @Test
//    @DisplayName("식품안전나라 레시피 동기화 - 성공")
//    void syncFoodSafetyRecipes_Success() {
//        // given
//        List<CookRecipeResponse> cookRecipes = Arrays.asList(
//                sampleCookRecipeResponse,
//                sampleCookRecipeResponse
//        );
//
//        Recipe cookRecipe = Recipe.builder()
//                .id(2L)
//                .title("된장찌개")
//                .category("국")
//                .sourceApi(RecipeSource.FOOD_SAFETY)
//                .sourceApiId("1")
//                .lastSyncedAt(LocalDateTime.now())
//                .build();
//
//        given(foodSafetyClient.getKoreanRecipes(1, 1000)).willReturn(cookRecipes);
//        given(recipeRepository.findBySourceApiAndSourceApiId(eq(RecipeSource.FOOD_SAFETY), anyString()))
//                .willReturn(Optional.empty());
//        given(recipeMapper.toEntity(any(CookRecipeResponse.class))).willReturn(cookRecipe);
//        given(recipeRepository.save(any(Recipe.class))).willReturn(cookRecipe);
//
//        // when
//        int syncedCount = recipeSyncService.syncFoodSafetyRecipes();
//
//        // then
//        assertThat(syncedCount).isEqualTo(2);
//        verify(foodSafetyClient, times(1)).getKoreanRecipes(1, 1000);
//        verify(recipeRepository, times(2)).save(any(Recipe.class));
//    }
//
//    @Test
//    @DisplayName("식품안전나라 배치 동기화 - 성공")
//    void syncFoodSafetyRecipesBatch_Success() {
//        // given
//        int start = 1;
//        int end = 100;
//        List<CookRecipeResponse> cookRecipes = Collections.singletonList(sampleCookRecipeResponse);
//
//        Recipe cookRecipe = Recipe.builder()
//                .id(2L)
//                .title("된장찌개")
//                .sourceApi(RecipeSource.FOOD_SAFETY)
//                .sourceApiId("1")
//                .lastSyncedAt(LocalDateTime.now())
//                .build();
//
//        given(foodSafetyClient.getKoreanRecipes(start, end)).willReturn(cookRecipes);
//        given(recipeRepository.findBySourceApiAndSourceApiId(RecipeSource.FOOD_SAFETY, "1"))
//                .willReturn(Optional.empty());
//        given(recipeMapper.toEntity(any(CookRecipeResponse.class))).willReturn(cookRecipe);
//        given(recipeRepository.save(any(Recipe.class))).willReturn(cookRecipe);
//
//        // when
//        int syncedCount = recipeSyncService.syncFoodSafetyRecipesBatch(start, end);
//
//        // then
//        assertThat(syncedCount).isEqualTo(1);
//        verify(foodSafetyClient, times(1)).getKoreanRecipes(start, end);
//    }
//
//    @Test
//    @DisplayName("전체 레시피 동기화 - 성공")
//    void syncAllRecipes_Success() {
//        // given
//        List<CookRecipeResponse> cookRecipes = Collections.singletonList(sampleCookRecipeResponse);
//        List<MealResponse> mealResponses = Collections.singletonList(sampleMealResponse);
//
//        Recipe cookRecipe = Recipe.builder()
//                .id(2L)
//                .title("된장찌개")
//                .sourceApi(RecipeSource.FOOD_SAFETY)
//                .sourceApiId("1")
//                .lastSyncedAt(LocalDateTime.now())
//                .build();
//
//        given(foodSafetyClient.getKoreanRecipes(1, 1000)).willReturn(cookRecipes);
//        given(theMealDBClient.getRandomRecipes(100)).willReturn(mealResponses);
//        given(recipeRepository.findBySourceApiAndSourceApiId(any(RecipeSource.class), anyString()))
//                .willReturn(Optional.empty());
//        given(recipeMapper.toEntity(any(CookRecipeResponse.class))).willReturn(cookRecipe);
//        given(recipeMapper.toEntity(any(MealResponse.class))).willReturn(sampleRecipe);
//        given(recipeRepository.save(any(Recipe.class))).willReturn(sampleRecipe);
//
//        // when
//        int totalSynced = recipeSyncService.syncAllRecipes();
//
//        // then
//        assertThat(totalSynced).isEqualTo(2);
//        verify(foodSafetyClient, times(1)).getKoreanRecipes(1, 1000);
//        verify(theMealDBClient, times(1)).getRandomRecipes(100);
//    }
//
//    @Test
//    @DisplayName("카테고리별 TheMealDB 레시피 동기화 - 성공")
//    void syncMealDbRecipesByCategory_Success() {
//        // given
//        String category = "Chicken";
//        List<MealResponse> mealResponses = Arrays.asList(sampleMealResponse, sampleMealResponse);
//
//        given(theMealDBClient.getRecipesByCategory(category)).willReturn(mealResponses);
//        given(recipeRepository.findBySourceApiAndSourceApiId(eq(RecipeSource.MEAL_DB), anyString()))
//                .willReturn(Optional.empty());
//        given(recipeMapper.toEntity(any(MealResponse.class))).willReturn(sampleRecipe);
//        given(recipeRepository.save(any(Recipe.class))).willReturn(sampleRecipe);
//
//        // when
//        int syncedCount = recipeSyncService.syncMealDbRecipesByCategory(category);
//
//        // then
//        assertThat(syncedCount).isEqualTo(2);
//        verify(theMealDBClient, times(1)).getRecipesByCategory(category);
//        verify(recipeRepository, times(2)).save(any(Recipe.class));
//    }
//
//    @Test
//    @DisplayName("레시피 동기화 중 예외 발생 - 계속 진행")
//    void syncMealDbRandomRecipes_WithException_ContinuesProcessing() {
//        // given
//        int count = 2;
//        MealResponse validMeal = new MealResponse();
//        validMeal.setId("52772");
//        validMeal.setName("Valid Recipe");
//
//        MealResponse invalidMeal = new MealResponse();
//        invalidMeal.setId(null); // Invalid: null ID
//
//        List<MealResponse> mealResponses = Arrays.asList(validMeal, invalidMeal);
//
//        given(theMealDBClient.getRandomRecipes(count)).willReturn(mealResponses);
//        given(recipeRepository.findBySourceApiAndSourceApiId(RecipeSource.MEAL_DB, "52772"))
//                .willReturn(Optional.empty());
//        given(recipeMapper.toEntity(validMeal)).willReturn(sampleRecipe);
//        given(recipeRepository.save(any(Recipe.class))).willReturn(sampleRecipe);
//
//        // when
//        int syncedCount = recipeSyncService.syncMealDbRandomRecipes(count);
//
//        // then
//        assertThat(syncedCount).isEqualTo(1); // Only valid recipe synced
//        verify(recipeRepository, times(1)).save(any(Recipe.class));
//    }
//}
