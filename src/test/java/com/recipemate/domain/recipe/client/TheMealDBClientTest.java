//package com.recipemate.domain.recipe.client;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.recipemate.domain.recipe.dto.CategoryListResponse;
//import com.recipemate.domain.recipe.dto.CategoryResponse;
//import com.recipemate.domain.recipe.dto.MealListResponse;
//import com.recipemate.domain.recipe.dto.MealResponse;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.client.MockRestServiceServer;
//import org.springframework.web.client.RestTemplate;
//
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
//import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
//
///**
// * TheMealDBClient 테스트
// * MockRestServiceServer를 사용하여 실제 API 호출 없이 테스트
// */
//class TheMealDBClientTest {
//
//    private TheMealDBClient client;
//    private MockRestServiceServer mockServer;
//    private ObjectMapper objectMapper;
//
//    private static final String BASE_URL = "https://www.themealdb.com/api/json/v1/1";
//
//    @BeforeEach
//    void setUp() {
//        RestTemplate restTemplate = new RestTemplate();
//        mockServer = MockRestServiceServer.createServer(restTemplate);
//        client = new TheMealDBClient(restTemplate);
//        objectMapper = new ObjectMapper();
//    }
//
//    @Test
//    @DisplayName("레시피 이름으로 검색")
//    void searchRecipesByName() throws Exception {
//        // given
//        String keyword = "chicken";
//        MealResponse meal = MealResponse.builder()
//                .id("52940")
//                .name("Brown Stew Chicken")
//                .category("Chicken")
//                .area("Jamaican")
//                .instructions("Season the chicken with salt, pepper, and herbs...")
//                .thumbnail("https://www.themealdb.com/images/media/meals/sypxpx1515365095.jpg")
//                .ingredient1("Chicken")
//                .ingredient2("Tomato")
//                .ingredient3("Onion")
//                .measure1("1 whole")
//                .measure2("2")
//                .measure3("1 chopped")
//                .build();
//
//        MealListResponse response = new MealListResponse(List.of(meal));
//        String jsonResponse = objectMapper.writeValueAsString(response);
//
//        mockServer.expect(requestTo(BASE_URL + "/search.php?s=" + keyword))
//                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));
//
//        // when
//        List<MealResponse> meals = client.searchRecipes(keyword);
//
//        // then
//        assertThat(meals).hasSize(1);
//        assertThat(meals.get(0).getName()).isEqualTo("Brown Stew Chicken");
//        assertThat(meals.get(0).getCategory()).isEqualTo("Chicken");
//        assertThat(meals.get(0).getIngredients()).hasSize(3);
//        assertThat(meals.get(0).getIngredients().get(0).getName()).isEqualTo("Chicken");
//        assertThat(meals.get(0).getIngredients().get(0).getMeasure()).isEqualTo("1 whole");
//
//        mockServer.verify();
//    }
//
//    @Test
//    @DisplayName("레시피 이름으로 검색 - 결과 없음")
//    void searchRecipesReturnsEmpty() throws Exception {
//        // given
//        String keyword = "nonexistent";
//        MealListResponse response = new MealListResponse(null);
//        String jsonResponse = objectMapper.writeValueAsString(response);
//
//        mockServer.expect(requestTo(BASE_URL + "/search.php?s=" + keyword))
//                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));
//
//        // when
//        List<MealResponse> meals = client.searchRecipes(keyword);
//
//        // then
//        assertThat(meals).isEmpty();
//        mockServer.verify();
//    }
//
//    @Test
//    @DisplayName("레시피 ID로 상세 조회")
//    void getRecipeById() throws Exception {
//        // given
//        String mealId = "52940";
//        MealResponse meal = MealResponse.builder()
//                .id(mealId)
//                .name("Brown Stew Chicken")
//                .category("Chicken")
//                .area("Jamaican")
//                .instructions("Season the chicken with salt, pepper, and herbs...")
//                .thumbnail("https://www.themealdb.com/images/media/meals/sypxpx1515365095.jpg")
//                .youtubeUrl("https://www.youtube.com/watch?v=xxxxx")
//                .sourceUrl("https://www.africanbites.com/brown-stew-chicken/")
//                .ingredient1("Chicken")
//                .ingredient2("Tomato")
//                .measure1("1 whole")
//                .measure2("2")
//                .build();
//
//        MealListResponse response = new MealListResponse(List.of(meal));
//        String jsonResponse = objectMapper.writeValueAsString(response);
//
//        mockServer.expect(requestTo(BASE_URL + "/lookup.php?i=" + mealId))
//                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));
//
//        // when
//        MealResponse result = client.getRecipeById(mealId);
//
//        // then
//        assertThat(result).isNotNull();
//        assertThat(result.getId()).isEqualTo(mealId);
//        assertThat(result.getName()).isEqualTo("Brown Stew Chicken");
//        assertThat(result.getYoutubeUrl()).isEqualTo("https://www.youtube.com/watch?v=xxxxx");
//        assertThat(result.getSourceUrl()).isEqualTo("https://www.africanbites.com/brown-stew-chicken/");
//        assertThat(result.getIngredients()).hasSize(2);
//
//        mockServer.verify();
//    }
//
//    @Test
//    @DisplayName("레시피 ID로 상세 조회 - 결과 없음")
//    void getRecipeByIdReturnsNull() throws Exception {
//        // given
//        String mealId = "999999";
//        MealListResponse response = new MealListResponse(null);
//        String jsonResponse = objectMapper.writeValueAsString(response);
//
//        mockServer.expect(requestTo(BASE_URL + "/lookup.php?i=" + mealId))
//                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));
//
//        // when
//        MealResponse result = client.getRecipeById(mealId);
//
//        // then
//        assertThat(result).isNull();
//        mockServer.verify();
//    }
//
//    @Test
//    @DisplayName("랜덤 레시피 조회")
//    void getRandomRecipe() throws Exception {
//        // given
//        MealResponse meal = MealResponse.builder()
//                .id("52772")
//                .name("Teriyaki Chicken Casserole")
//                .category("Chicken")
//                .area("Japanese")
//                .instructions("Preheat oven to 350° F...")
//                .thumbnail("https://www.themealdb.com/images/media/meals/wvpsxx1468256321.jpg")
//                .ingredient1("Chicken")
//                .ingredient2("Soy Sauce")
//                .ingredient3("Rice")
//                .measure1("750g")
//                .measure2("3/4 cup")
//                .measure3("2 cups")
//                .build();
//
//        MealListResponse response = new MealListResponse(List.of(meal));
//        String jsonResponse = objectMapper.writeValueAsString(response);
//
//        mockServer.expect(requestTo(BASE_URL + "/random.php"))
//                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));
//
//        // when
//        MealResponse result = client.getRandomRecipe();
//
//        // then
//        assertThat(result).isNotNull();
//        assertThat(result.getName()).isEqualTo("Teriyaki Chicken Casserole");
//        assertThat(result.getCategory()).isEqualTo("Chicken");
//        assertThat(result.getIngredients()).hasSize(3);
//
//        mockServer.verify();
//    }
//
//    @Test
//    @DisplayName("여러 개의 랜덤 레시피 조회")
//    void getRandomRecipes() throws Exception {
//        // given
//        int count = 3;
//
//        // 첫 번째 랜덤 레시피
//        MealResponse meal1 = MealResponse.builder()
//                .id("52772")
//                .name("Teriyaki Chicken Casserole")
//                .category("Chicken")
//                .build();
//        MealListResponse response1 = new MealListResponse(List.of(meal1));
//
//        // 두 번째 랜덤 레시피
//        MealResponse meal2 = MealResponse.builder()
//                .id("52940")
//                .name("Brown Stew Chicken")
//                .category("Chicken")
//                .build();
//        MealListResponse response2 = new MealListResponse(List.of(meal2));
//
//        // 세 번째 랜덤 레시피
//        MealResponse meal3 = MealResponse.builder()
//                .id("52795")
//                .name("Chicken Handi")
//                .category("Chicken")
//                .build();
//        MealListResponse response3 = new MealListResponse(List.of(meal3));
//
//        String jsonResponse1 = objectMapper.writeValueAsString(response1);
//        String jsonResponse2 = objectMapper.writeValueAsString(response2);
//        String jsonResponse3 = objectMapper.writeValueAsString(response3);
//
//        mockServer.expect(requestTo(BASE_URL + "/random.php"))
//                .andRespond(withSuccess(jsonResponse1, MediaType.APPLICATION_JSON));
//        mockServer.expect(requestTo(BASE_URL + "/random.php"))
//                .andRespond(withSuccess(jsonResponse2, MediaType.APPLICATION_JSON));
//        mockServer.expect(requestTo(BASE_URL + "/random.php"))
//                .andRespond(withSuccess(jsonResponse3, MediaType.APPLICATION_JSON));
//
//        // when
//        List<MealResponse> meals = client.getRandomRecipes(count);
//
//        // then
//        assertThat(meals).hasSize(3);
//        assertThat(meals.get(0).getName()).isEqualTo("Teriyaki Chicken Casserole");
//        assertThat(meals.get(1).getName()).isEqualTo("Brown Stew Chicken");
//        assertThat(meals.get(2).getName()).isEqualTo("Chicken Handi");
//
//        mockServer.verify();
//    }
//
//    @Test
//    @DisplayName("카테고리 목록 조회")
//    void getCategories() throws Exception {
//        // given
//        CategoryResponse category1 = new CategoryResponse("1", "Beef",
//                "https://www.themealdb.com/images/category/beef.png",
//                "Beef is the culinary name for meat from cattle...");
//        CategoryResponse category2 = new CategoryResponse("2", "Chicken",
//                "https://www.themealdb.com/images/category/chicken.png",
//                "Chicken is a type of domesticated fowl...");
//        CategoryResponse category3 = new CategoryResponse("3", "Dessert",
//                "https://www.themealdb.com/images/category/dessert.png",
//                "Dessert is a course that concludes a meal...");
//
//        CategoryListResponse response = new CategoryListResponse(
//                List.of(category1, category2, category3)
//        );
//        String jsonResponse = objectMapper.writeValueAsString(response);
//
//        mockServer.expect(requestTo(BASE_URL + "/categories.php"))
//                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));
//
//        // when
//        List<CategoryResponse> categories = client.getCategories();
//
//        // then
//        assertThat(categories).hasSize(3);
//        assertThat(categories.get(0).getName()).isEqualTo("Beef");
//        assertThat(categories.get(1).getName()).isEqualTo("Chicken");
//        assertThat(categories.get(2).getName()).isEqualTo("Dessert");
//
//        mockServer.verify();
//    }
//
//    @Test
//    @DisplayName("재료가 빈 값인 경우 파싱 테스트")
//    void parseIngredientsWithEmptyValues() {
//        // given
//        MealResponse meal = MealResponse.builder()
//                .id("123")
//                .name("Test Meal")
//                .ingredient1("Chicken")
//                .ingredient2("Tomato")
//                .ingredient3("")  // 빈 문자열
//                .ingredient4(null)  // null
//                .ingredient5("Onion")
//                .measure1("1 kg")
//                .measure2("2")
//                .measure3("")
//                .measure4(null)
//                .measure5("1 chopped")
//                .build();
//
//        // when
//        List<MealResponse.IngredientInfo> ingredients = meal.getIngredients();
//
//        // then
//        assertThat(ingredients).hasSize(3);  // 빈 값과 null은 제외
//        assertThat(ingredients.get(0).getName()).isEqualTo("Chicken");
//        assertThat(ingredients.get(0).getMeasure()).isEqualTo("1 kg");
//        assertThat(ingredients.get(1).getName()).isEqualTo("Tomato");
//        assertThat(ingredients.get(1).getMeasure()).isEqualTo("2");
//        assertThat(ingredients.get(2).getName()).isEqualTo("Onion");
//        assertThat(ingredients.get(2).getMeasure()).isEqualTo("1 chopped");
//    }
//}
