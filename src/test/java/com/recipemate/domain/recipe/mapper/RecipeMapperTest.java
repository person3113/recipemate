package com.recipemate.domain.recipe.mapper;

import com.recipemate.domain.recipe.dto.CookRecipeResponse;
import com.recipemate.domain.recipe.entity.Recipe;
import com.recipemate.domain.recipe.entity.RecipeIngredient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RecipeMapper 테스트
 * 특히 식품안전나라 API의 비정형 재료 데이터 파싱 로직을 중점적으로 테스트
 */
class RecipeMapperTest {

    private RecipeMapper recipeMapper;

    @BeforeEach
    void setUp() {
        recipeMapper = new RecipeMapper();
    }

    @Test
    @DisplayName("하나의 토큰에 여러 재료가 포함된 경우 - 대파와 다진 마늘")
    void testMultipleIngredientsInOneToken_OnionAndGarlic() {
        // given
        CookRecipeResponse response = createResponseWithIngredients("대파 2.5g 다진 마늘 1.25g");

        // when
        Recipe recipe = recipeMapper.toEntity(response);

        // then
        List<RecipeIngredient> ingredients = recipe.getIngredients();
        assertThat(ingredients).hasSize(2);
        assertThat(ingredients.get(0).getName()).isEqualTo("대파");
        assertThat(ingredients.get(0).getMeasure()).isEqualTo("2.5g");
        assertThat(ingredients.get(1).getName()).isEqualTo("다진 마늘");
        assertThat(ingredients.get(1).getMeasure()).isEqualTo("1.25g");
    }

    @Test
    @DisplayName("하나의 토큰에 여러 재료가 포함된 경우 - 연속된 재료")
    void testMultipleIngredientsInOneToken_Consecutive() {
        // given
        CookRecipeResponse response = createResponseWithIngredients("다진양파 15g 빵가루 30g");

        // when
        Recipe recipe = recipeMapper.toEntity(response);

        // then
        List<RecipeIngredient> ingredients = recipe.getIngredients();
        assertThat(ingredients).hasSize(2);
        assertThat(ingredients.get(0).getName()).isEqualTo("다진양파");
        assertThat(ingredients.get(0).getMeasure()).isEqualTo("15g");
        assertThat(ingredients.get(1).getName()).isEqualTo("빵가루");
        assertThat(ingredients.get(1).getMeasure()).isEqualTo("30g");
    }

    @Test
    @DisplayName("섹션 마커와 여러 재료가 포함된 경우 - 물과 홍시드레싱")
    void testMultipleIngredientsWithSectionMarker() {
        // given
        CookRecipeResponse response = createResponseWithIngredients("물 100g 홍시드레싱 : 홍시 30g");

        // when
        Recipe recipe = recipeMapper.toEntity(response);

        // then
        List<RecipeIngredient> ingredients = recipe.getIngredients();
        assertThat(ingredients).hasSizeGreaterThanOrEqualTo(2);
        
        // 물이 첫 번째로 추출되어야 함
        assertThat(ingredients.get(0).getName()).isEqualTo("물");
        assertThat(ingredients.get(0).getMeasure()).isEqualTo("100g");
        
        // 홍시가 추출되어야 함 (홍시드레싱: 마커는 제거됨)
        boolean hasHongsi = ingredients.stream()
            .anyMatch(ing -> ing.getName().contains("홍시"));
        assertThat(hasHongsi).isTrue();
    }

    @Test
    @DisplayName("서빙 마커 제거 - [ 2인분 ] 같은 대괄호 마커")
    void testServingSizeMarkerRemoval() {
        // given
        CookRecipeResponse response = createResponseWithIngredients("[ 2인분 ] 묵은지(¼포기)");

        // when
        Recipe recipe = recipeMapper.toEntity(response);

        // then
        List<RecipeIngredient> ingredients = recipe.getIngredients();
        assertThat(ingredients).hasSizeGreaterThanOrEqualTo(1);
        
        // "2인분"이라는 텍스트가 재료명에 포함되지 않아야 함
        boolean hasServingInName = ingredients.stream()
            .anyMatch(ing -> ing.getName().contains("인분"));
        assertThat(hasServingInName).isFalse();
        
        // 묵은지가 재료로 추출되어야 함
        boolean hasMukeunji = ingredients.stream()
            .anyMatch(ing -> ing.getName().contains("묵은지"));
        assertThat(hasMukeunji).isTrue();
    }

    @Test
    @DisplayName("괄호 안에 쉼표가 있는 경우 - 닭고기(가슴살, 25g)")
    void testIngredientWithParenthesesAndComma() {
        // given
        CookRecipeResponse response = createResponseWithIngredients("닭고기(가슴살, 25g)");

        // when
        Recipe recipe = recipeMapper.toEntity(response);

        // then
        List<RecipeIngredient> ingredients = recipe.getIngredients();
        assertThat(ingredients).hasSize(1);
        assertThat(ingredients.get(0).getName()).isEqualTo("닭고기(가슴살)");
        assertThat(ingredients.get(0).getMeasure()).isEqualTo("25g");
    }

    @Test
    @DisplayName("괄호 밖 쉼표로 구분된 여러 재료")
    void testMultipleIngredientsWithComma() {
        // given
        CookRecipeResponse response = createResponseWithIngredients("양파 10g, 당근 15g, 감자 20g");

        // when
        Recipe recipe = recipeMapper.toEntity(response);

        // then
        List<RecipeIngredient> ingredients = recipe.getIngredients();
        assertThat(ingredients).hasSize(3);
        assertThat(ingredients.get(0).getName()).isEqualTo("양파");
        assertThat(ingredients.get(0).getMeasure()).isEqualTo("10g");
        assertThat(ingredients.get(1).getName()).isEqualTo("당근");
        assertThat(ingredients.get(1).getMeasure()).isEqualTo("15g");
        assertThat(ingredients.get(2).getName()).isEqualTo("감자");
        assertThat(ingredients.get(2).getMeasure()).isEqualTo("20g");
    }

    @Test
    @DisplayName("HTML 태그 제거 - br 태그로 구분된 재료")
    void testHtmlTagRemoval() {
        // given
        CookRecipeResponse response = createResponseWithIngredients("대파 10g<br/>마늘 5g<br>생강 3g");

        // when
        Recipe recipe = recipeMapper.toEntity(response);

        // then
        List<RecipeIngredient> ingredients = recipe.getIngredients();
        assertThat(ingredients).hasSize(3);
        assertThat(ingredients.get(0).getName()).isEqualTo("대파");
        assertThat(ingredients.get(1).getName()).isEqualTo("마늘");
        assertThat(ingredients.get(2).getName()).isEqualTo("생강");
    }

    @Test
    @DisplayName("대괄호 섹션 마커 제거 - [주재료] 양파 10g")
    void testBracketSectionMarkerRemoval() {
        // given
        CookRecipeResponse response = createResponseWithIngredients("[주재료] 양파 10g, 대파 5g");

        // when
        Recipe recipe = recipeMapper.toEntity(response);

        // then
        List<RecipeIngredient> ingredients = recipe.getIngredients();
        assertThat(ingredients).hasSizeGreaterThanOrEqualTo(2);
        
        // "주재료"가 재료명에 포함되지 않아야 함
        boolean hasMarkerInName = ingredients.stream()
            .anyMatch(ing -> ing.getName().contains("주재료"));
        assertThat(hasMarkerInName).isFalse();
    }

    @Test
    @DisplayName("대괄호 콜론 패턴 - [소스: 물 10g, 간장 5g]")
    void testBracketColonPattern() {
        // given
        CookRecipeResponse response = createResponseWithIngredients("[소스: 물 10g, 간장 5g]");

        // when
        Recipe recipe = recipeMapper.toEntity(response);

        // then
        List<RecipeIngredient> ingredients = recipe.getIngredients();
        assertThat(ingredients).hasSizeGreaterThanOrEqualTo(2);
        
        // "소스"가 재료명에 포함되지 않아야 함
        boolean hasSauceInName = ingredients.stream()
            .anyMatch(ing -> ing.getName().contains("소스:"));
        assertThat(hasSauceInName).isFalse();
        
        // 실제 재료들이 추출되어야 함
        boolean hasWater = ingredients.stream()
            .anyMatch(ing -> ing.getName().equals("물"));
        boolean hasSoySauce = ingredients.stream()
            .anyMatch(ing -> ing.getName().equals("간장"));
        assertThat(hasWater).isTrue();
        assertThat(hasSoySauce).isTrue();
    }

    @Test
    @DisplayName("특수 기호 섹션 마커 - • 양념장: 간장 10g")
    void testSpecialCharacterSectionMarker() {
        // given
        CookRecipeResponse response = createResponseWithIngredients("• 양념장: 간장 10g, 설탕 5g");

        // when
        Recipe recipe = recipeMapper.toEntity(response);

        // then
        List<RecipeIngredient> ingredients = recipe.getIngredients();
        assertThat(ingredients).hasSizeGreaterThanOrEqualTo(2);
        
        // "양념장:"이 재료명에 포함되지 않아야 함
        boolean hasMarkerInName = ingredients.stream()
            .anyMatch(ing -> ing.getName().contains("양념장"));
        assertThat(hasMarkerInName).isFalse();
    }

    @Test
    @DisplayName("용량 없이 재료명만 있는 경우")
    void testIngredientWithoutMeasure() {
        // given
        CookRecipeResponse response = createResponseWithIngredients("소금, 후추");

        // when
        Recipe recipe = recipeMapper.toEntity(response);

        // then
        List<RecipeIngredient> ingredients = recipe.getIngredients();
        assertThat(ingredients).hasSize(2);
        assertThat(ingredients.get(0).getName()).isEqualTo("소금");
        assertThat(ingredients.get(0).getMeasure()).isEqualTo("적당량");
        assertThat(ingredients.get(1).getName()).isEqualTo("후추");
        assertThat(ingredients.get(1).getMeasure()).isEqualTo("적당량");
    }

    @Test
    @DisplayName("복잡한 실제 케이스 - 차돌박이볶음")
    void testRealCase_BeefBrisketStirFry() {
        // given
        String ingredients = "[주재료]차돌박이 200g,양파 1/2개<br/>[양념]고추장 1큰술,고춧가루 1/2큰술,간장 1큰술,설탕 1작은술,다진마늘 1작은술,참기름 1작은술";
        CookRecipeResponse response = createResponseWithIngredients(ingredients);

        // when
        Recipe recipe = recipeMapper.toEntity(response);

        // then
        List<RecipeIngredient> ingredients_result = recipe.getIngredients();
        assertThat(ingredients_result).hasSizeGreaterThanOrEqualTo(8);
        
        // 주요 재료들이 추출되었는지 확인
        boolean hasChadolbagi = ingredients_result.stream()
            .anyMatch(ing -> ing.getName().contains("차돌박이"));
        boolean hasOnion = ingredients_result.stream()
            .anyMatch(ing -> ing.getName().contains("양파"));
        boolean hasGochujang = ingredients_result.stream()
            .anyMatch(ing -> ing.getName().contains("고추장"));
        
        assertThat(hasChadolbagi).isTrue();
        assertThat(hasOnion).isTrue();
        assertThat(hasGochujang).isTrue();
    }

    @Test
    @DisplayName("null 또는 빈 문자열 처리")
    void testNullOrEmptyIngredients() {
        // given
        CookRecipeResponse response1 = createResponseWithIngredients(null);
        CookRecipeResponse response2 = createResponseWithIngredients("");
        CookRecipeResponse response3 = createResponseWithIngredients("   ");

        // when
        Recipe recipe1 = recipeMapper.toEntity(response1);
        Recipe recipe2 = recipeMapper.toEntity(response2);
        Recipe recipe3 = recipeMapper.toEntity(response3);

        // then
        assertThat(recipe1.getIngredients()).isEmpty();
        assertThat(recipe2.getIngredients()).isEmpty();
        assertThat(recipe3.getIngredients()).isEmpty();
    }

    @Test
    @DisplayName("단위가 괄호 안에 있는 경우 - 소금(1g)")
    void testMeasureInParentheses() {
        // given
        CookRecipeResponse response = createResponseWithIngredients("소금(1g), 후추(0.5g)");

        // when
        Recipe recipe = recipeMapper.toEntity(response);

        // then
        List<RecipeIngredient> ingredients = recipe.getIngredients();
        assertThat(ingredients).hasSize(2);
        assertThat(ingredients.get(0).getName()).isEqualTo("소금");
        assertThat(ingredients.get(0).getMeasure()).isEqualTo("1g");
        assertThat(ingredients.get(1).getName()).isEqualTo("후추");
        assertThat(ingredients.get(1).getMeasure()).isEqualTo("0.5g");
    }

    @Test
    @DisplayName("복잡한 단위 표현 - 1/2개, 1큰술 등")
    void testComplexMeasureExpressions() {
        // given
        CookRecipeResponse response = createResponseWithIngredients("양파 1/2개, 간장 1큰술, 설탕 2작은술");

        // when
        Recipe recipe = recipeMapper.toEntity(response);

        // then
        List<RecipeIngredient> ingredients = recipe.getIngredients();
        assertThat(ingredients).hasSize(3);
        assertThat(ingredients.get(0).getName()).isEqualTo("양파");
        assertThat(ingredients.get(0).getMeasure()).isEqualTo("1/2개");
        assertThat(ingredients.get(1).getName()).isEqualTo("간장");
        assertThat(ingredients.get(1).getMeasure()).isEqualTo("1큰술");
        assertThat(ingredients.get(2).getName()).isEqualTo("설탕");
        assertThat(ingredients.get(2).getMeasure()).isEqualTo("2작은술");
    }

    @Test
    @DisplayName("실제 API 케이스 1 - 새우 두부 계란찜 (괄호 내 부가 정보)")
    void testRealCase_ShrimpTofuEggSteam() {
        // given
        String ingredients = "새우두부계란찜\n연두부 75g(3/4모), 칵테일새우 20g(5마리), 달걀 30g(1/2개), 생크림 13g(1큰술), 설탕 5g(1작은술), 무염버터 5g(1작은술)\n고명\n시금치 10g(3줄기)";
        CookRecipeResponse response = createResponseWithIngredients(ingredients);

        // when
        Recipe recipe = recipeMapper.toEntity(response);

        // then
        List<RecipeIngredient> ingredients_result = recipe.getIngredients();
        
        // 연두부 체크 - 괄호 내 "3/4모"는 제거되고 75g만 measure로
        RecipeIngredient tofu = ingredients_result.stream()
            .filter(ing -> ing.getName().contains("연두부"))
            .findFirst()
            .orElse(null);
        assertThat(tofu).isNotNull();
        assertThat(tofu.getMeasure()).isEqualTo("75g");
        
        // 시금치 체크 - 괄호 내 "3줄기"는 제거되고 10g만 measure로
        RecipeIngredient spinach = ingredients_result.stream()
            .filter(ing -> ing.getName().contains("시금치"))
            .findFirst()
            .orElse(null);
        assertThat(spinach).isNotNull();
        assertThat(spinach.getMeasure()).isEqualTo("10g");
    }

    @Test
    @DisplayName("실제 API 케이스 2 - 무 현미밥 (괄호 잔여물 문제)")
    void testRealCase_RadishBrownRice() {
        // given
        String ingredients = "●주재료 : 현미 140g(2/3컵), 찹쌀 140g(2/3컵), 무 35g(1/20개), 표고버섯 30g(2개) 근대 10g, 올리브오일 15g(1큰술), 참기름 15g(1큰술)\n●육수 :뒤포리(밴댕이) 50g(5개), 다시마 15g(5cm), 간장 15g(1큰술)";
        CookRecipeResponse response = createResponseWithIngredients(ingredients);

        // when
        Recipe recipe = recipeMapper.toEntity(response);

        // then
        List<RecipeIngredient> ingredients_result = recipe.getIngredients();
        
        // 근대가 올바르게 추출되었는지 확인 ("개) 근대" 같은 잔여물이 없어야 함)
        RecipeIngredient geunde = ingredients_result.stream()
            .filter(ing -> ing.getName().contains("근대"))
            .findFirst()
            .orElse(null);
        assertThat(geunde).isNotNull();
        assertThat(geunde.getName()).doesNotContain("개)");
        assertThat(geunde.getMeasure()).isEqualTo("10g");
    }

    @Test
    @DisplayName("실제 API 케이스 3 - 검은깨라면크로켓 (복잡한 괄호)")
    void testRealCase_BlackSesameRamenCroquette() {
        // given
        String ingredients = "재료 검은깨(8g), 빵가루(80g), 라면(50g), 달걀(45g), 녹말가루(15g)\n다진 쇠고기(40g), 다진 땅콩(5g), 어린잎채소(2g), 방울토마토(20g) 소스 통계피(20g), 생강(10g), 조청(10g), 물엿(20g), 흑설탕(20g)\n발사믹드레싱 발사믹식초(15g), 올리브유(5g)";
        CookRecipeResponse response = createResponseWithIngredients(ingredients);

        // when
        Recipe recipe = recipeMapper.toEntity(response);

        // then
        List<RecipeIngredient> ingredients_result = recipe.getIngredients();
        
        // 디버깅: 모든 재료 출력
        try {
            java.io.PrintWriter writer = new java.io.PrintWriter("debug_blacksesame.txt", "UTF-8");
            writer.println("=== 검은깨라면크로켓 재료 목록 ===");
            ingredients_result.forEach(ing -> 
                writer.println("재료: " + ing.getName() + " | 용량: " + ing.getMeasure())
            );
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // 방울토마토가 추출되어야 하고, "소스 통계피"는 분리되어야 함
        RecipeIngredient tomato = ingredients_result.stream()
            .filter(ing -> ing.getName().contains("방울토마토"))
            .findFirst()
            .orElse(null);
        assertThat(tomato).isNotNull();
        assertThat(tomato.getMeasure()).isEqualTo("20g");
        
        RecipeIngredient tonggye = ingredients_result.stream()
            .filter(ing -> ing.getName().contains("통계피"))
            .findFirst()
            .orElse(null);
        assertThat(tonggye).isNotNull();
        assertThat(tonggye.getName()).doesNotContain("소스");
    }

    @Test
    @DisplayName("실제 API 케이스 4 - 한입꼬치구이 (온점 구분자)")
    void testRealCase_OneBiteSkewer() {
        // given
        String ingredients = "통삼겹 200g, 고구마 100g,\n대파 40g, 2가지색 미니파프리카 40g, 청피망 40g, 양송이 40g, 건고추 3g,\n어간장 10g, 통후추 1g, 로즈마리 1g. 마늘기름 15g\n소스 : 오렌지주스 100g, 레몬즙 15g, 올리고당 15g, 후추 1g, 다진마늘 10g";
        CookRecipeResponse response = createResponseWithIngredients(ingredients);

        // when
        Recipe recipe = recipeMapper.toEntity(response);

        // then
        List<RecipeIngredient> ingredients_result = recipe.getIngredients();
        
        // 로즈마리와 마늘기름이 분리되어야 함
        RecipeIngredient rosemary = ingredients_result.stream()
            .filter(ing -> ing.getName().contains("로즈마리"))
            .findFirst()
            .orElse(null);
        assertThat(rosemary).isNotNull();
        assertThat(rosemary.getMeasure()).isEqualTo("1g");
        
        RecipeIngredient garlicOil = ingredients_result.stream()
            .filter(ing -> ing.getName().contains("마늘기름"))
            .findFirst()
            .orElse(null);
        assertThat(garlicOil).isNotNull();
        assertThat(garlicOil.getMeasure()).isEqualTo("15g");
    }

    @Test
    @DisplayName("실제 API 케이스 5 - 케이준 스타일 닭고기 (별표 선택 마커)")
    void testRealCase_CajunChicken() {
        // given
        String ingredients = "닭가슴살 100g, 식용유 8g, 파프리카가루 2g, 키엔페퍼 1g, 양파가루 1g, 마늘가루 1g, 후추 1g, 흰후춧가루 1g, 오레가노마른것 0.5g, 타임 마른것 0.5g, 바질마른것 1g, 로즈마리마른것 0.5g *선택:황설탕 1g";
        CookRecipeResponse response = createResponseWithIngredients(ingredients);

        // when
        Recipe recipe = recipeMapper.toEntity(response);

        // then
        List<RecipeIngredient> ingredients_result = recipe.getIngredients();
        
        // 로즈마리마른것이 올바르게 추출되고, "*선택:" 마커가 제거되어야 함
        RecipeIngredient rosemary = ingredients_result.stream()
            .filter(ing -> ing.getName().contains("로즈마리"))
            .findFirst()
            .orElse(null);
        assertThat(rosemary).isNotNull();
        assertThat(rosemary.getMeasure()).isEqualTo("0.5g");
        assertThat(rosemary.getName()).doesNotContain("*선택");
    }

    @Test
    @DisplayName("실제 API 케이스 6 - 콩비지완자탕 (섹션 마커 처리)")
    void testRealCase_BeanCurdMeatballSoup() {
        // given
        String ingredients = "완자: 콩비지 50, 다진 돼지고기(기름기 없는 부위) 25, 깍두기 5, 새우젓 2.5, 계란 5, 감자전분 10, 후추 0.02, 다진 파 4, 다진 마늘 2 국물: 멸치다시마국물(물 300, 건멸치 2.5, 건다시마 2), 청경채 20, 어슷 썬 대파 5, 다진 마늘 1, 국간장 5";
        CookRecipeResponse response = createResponseWithIngredients(ingredients);

        // when
        Recipe recipe = recipeMapper.toEntity(response);

        // then
        List<RecipeIngredient> ingredients_result = recipe.getIngredients();
        
        // "완자:"와 "국물:" 마커가 재료명에 포함되지 않아야 함
        boolean hasWanja = ingredients_result.stream()
            .anyMatch(ing -> ing.getName().contains("완자:"));
        boolean hasGukmul = ingredients_result.stream()
            .anyMatch(ing -> ing.getName().contains("국물:"));
        assertThat(hasWanja).isFalse();
        assertThat(hasGukmul).isFalse();
        
        // 콩비지가 추출되어야 함
        boolean hasKongbiji = ingredients_result.stream()
            .anyMatch(ing -> ing.getName().contains("콩비지"));
        assertThat(hasKongbiji).isTrue();
    }

    @Test
    @DisplayName("실제 API 케이스 7 - 초코치즈크림케이크 (> 마커)")
    void testRealCase_ChocoCheeseCreamCake() {
        // given
        String ingredients = "크림치즈 > 우유 875ml, 식초 20ml, 알룰로스① 56g, 소금 1g, 초코크림 > 아몬드가루 85g, 무가당 코코아가루① 20g, 알룰로스② 70g, 두유① 25ml\n반 죽 > 바나나 175g, 달걀 105g, 두유② 25ml, 무가당 코코아가루② 45g, 땅콩버터 18g";
        CookRecipeResponse response = createResponseWithIngredients(ingredients);

        // when
        Recipe recipe = recipeMapper.toEntity(response);

        // then
        List<RecipeIngredient> ingredients_result = recipe.getIngredients();
        
        // 디버깅: 모든 재료 출력
        try {
            java.io.PrintWriter writer = new java.io.PrintWriter("debug_chococheese.txt", "UTF-8");
            writer.println("=== 초코치즈크림케이크 재료 목록 ===");
            ingredients_result.forEach(ing -> 
                writer.println("재료: " + ing.getName() + " | 용량: " + ing.getMeasure())
            );
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // "크림치즈 >" 같은 마커가 재료명에 포함되지 않아야 함
        boolean hasMarker = ingredients_result.stream()
            .anyMatch(ing -> ing.getName().contains(">"));
        assertThat(hasMarker).isFalse();
        
        // 우유가 추출되어야 함
        RecipeIngredient milk = ingredients_result.stream()
            .filter(ing -> ing.getName().contains("우유"))
            .findFirst()
            .orElse(null);
        assertThat(milk).isNotNull();
        assertThat(milk.getMeasure()).isEqualTo("875ml");
    }

    /**
     * 테스트용 CookRecipeResponse 생성 헬퍼 메서드
     */
    private CookRecipeResponse createResponseWithIngredients(String ingredients) {
        return CookRecipeResponse.builder()
                .rcpSeq("TEST001")
                .rcpNm("테스트 레시피")
                .rcpPartsDtls(ingredients)
                .attFileNoMk("http://example.com/image.jpg")
                .attFileNoMain("http://example.com/thumb.jpg")
                .rcpPat2("반찬")
                .build();
    }
}
