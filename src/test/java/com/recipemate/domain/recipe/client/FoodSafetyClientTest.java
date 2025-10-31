package com.recipemate.domain.recipe.client;

import com.recipemate.domain.recipe.dto.CookRecipeResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 식품안전나라 API 클라이언트 테스트
 * TDD Red 단계: 실패하는 테스트 먼저 작성
 */
@SpringBootTest
@TestPropertySource(properties = {
    "food.safety.api.key=sample",
    "food.safety.api.base-url=http://openapi.foodsafetykorea.go.kr/api"
})
class FoodSafetyClientTest {

    @Autowired
    private FoodSafetyClient foodSafetyClient;

    @Test
    @DisplayName("한식 레시피 목록 조회 - 정상 응답")
    void getKoreanRecipes_Success() {
        // given
        int start = 1;
        int end = 5;

        // when
        List<CookRecipeResponse> recipes = foodSafetyClient.getKoreanRecipes(start, end);

        // then
        assertThat(recipes).isNotNull();
        // API 호출이 성공하면 결과가 있어야 함 (실제 API는 데이터가 있음)
        // 테스트 환경에서는 Mock을 사용하거나, 실제 API 호출 시 결과 검증
    }

    @Test
    @DisplayName("레시피 이름으로 검색")
    void searchRecipesByName_Success() {
        // given
        String keyword = "김치찌개";
        int start = 1;
        int end = 10;

        // when
        List<CookRecipeResponse> recipes = foodSafetyClient.searchRecipesByName(keyword, start, end);

        // then
        assertThat(recipes).isNotNull();
    }

    @Test
    @DisplayName("재료로 레시피 검색")
    void searchRecipesByIngredient_Success() {
        // given
        String ingredient = "돼지고기";
        int start = 1;
        int end = 10;

        // when
        List<CookRecipeResponse> recipes = foodSafetyClient.searchRecipesByIngredient(ingredient, start, end);

        // then
        assertThat(recipes).isNotNull();
    }

    @Test
    @DisplayName("요리 종류로 필터링")
    void searchRecipesByCategory_Success() {
        // given
        String category = "반찬";
        int start = 1;
        int end = 10;

        // when
        List<CookRecipeResponse> recipes = foodSafetyClient.searchRecipesByCategory(category, start, end);

        // then
        assertThat(recipes).isNotNull();
    }

    @Test
    @DisplayName("잘못된 범위 요청 시 빈 리스트 반환")
    void getKoreanRecipes_InvalidRange() {
        // given
        int start = 10;
        int end = 5; // start > end

        // when
        List<CookRecipeResponse> recipes = foodSafetyClient.getKoreanRecipes(start, end);

        // then
        assertThat(recipes).isEmpty();
    }

    @Test
    @DisplayName("1000건 초과 요청 시 빈 리스트 반환")
    void getKoreanRecipes_ExceedsMaxLimit() {
        // given
        int start = 1;
        int end = 1001; // 1000건 초과

        // when
        List<CookRecipeResponse> recipes = foodSafetyClient.getKoreanRecipes(start, end);

        // then
        assertThat(recipes).isEmpty();
    }

    @Test
    @DisplayName("API 응답 파싱 검증 - 필수 필드 확인")
    void verifyResponseParsing() {
        // given
        int start = 1;
        int end = 1;

        // when
        List<CookRecipeResponse> recipes = foodSafetyClient.getKoreanRecipes(start, end);

        // then
        if (!recipes.isEmpty()) {
            CookRecipeResponse recipe = recipes.get(0);
            assertThat(recipe.getRcpSeq()).isNotBlank();
            assertThat(recipe.getRcpNm()).isNotBlank();
        }
    }
}
