package com.recipemate.domain.recipe.controller;

import com.recipemate.domain.recipe.dto.CategoryResponse;
import com.recipemate.domain.recipe.dto.RecipeDetailResponse;
import com.recipemate.domain.recipe.dto.RecipeListResponse;
import com.recipemate.domain.recipe.service.RecipeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("RecipeController 테스트")
class RecipeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RecipeService recipeService;

    @Test
    @WithMockUser
    @DisplayName("레시피 검색 페이지 렌더링 - 성공")
    void searchRecipesPage_Success() throws Exception {
        // given
        String keyword = "chicken";
        
        RecipeListResponse.RecipeSimpleInfo recipe1 = RecipeListResponse.RecipeSimpleInfo.builder()
                .id("meal-1")
                .name("Chicken Curry")
                .imageUrl("http://example.com/1.jpg")
                .category("Chicken")
                .source("themealdb")
                .build();
        
        RecipeListResponse response = RecipeListResponse.builder()
                .recipes(Arrays.asList(recipe1))
                .totalCount(1)
                .source("both")
                .build();

        when(recipeService.searchRecipes(keyword)).thenReturn(response);

        // when & then
        mockMvc.perform(get("/recipes")
                        .param("keyword", keyword)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("recipes/list"))
                .andExpect(model().attributeExists("recipes"))
                .andExpect(model().attribute("keyword", keyword));
    }

    @Test
    @WithMockUser
    @DisplayName("레시피 검색 - 키워드 없으면 전체 목록")
    void searchRecipesPage_NoKeyword_ShowsEmpty() throws Exception {
        // when & then
        mockMvc.perform(get("/recipes")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("recipes/list"))
                .andExpect(model().attributeDoesNotExist("recipes"));
    }

    @Test
    @WithMockUser
    @DisplayName("레시피 상세 페이지 렌더링 - 성공")
    void recipeDetailPage_Success() throws Exception {
        // given
        String recipeId = "meal-52772";
        
        RecipeDetailResponse response = RecipeDetailResponse.builder()
                .id(recipeId)
                .name("Teriyaki Chicken")
                .imageUrl("http://example.com/chicken.jpg")
                .category("Chicken")
                .area("Japanese")
                .instructions("Cook the chicken...")
                .youtubeUrl("https://youtube.com/watch?v=abc")
                .sourceUrl("https://example.com")
                .ingredients(Arrays.asList(
                        RecipeDetailResponse.IngredientInfo.builder()
                                .name("Chicken")
                                .measure("500g")
                                .build()
                ))
                .source("themealdb")
                .build();

        when(recipeService.getRecipeDetail(recipeId)).thenReturn(response);

        // when & then
        mockMvc.perform(get("/recipes/{recipeId}", recipeId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("recipes/detail"))
                .andExpect(model().attributeExists("recipe"))
                .andExpect(model().attribute("recipe", response));
    }

    @Test
    @WithMockUser
    @DisplayName("랜덤 레시피 조회 - 성공")
    void randomRecipes_Success() throws Exception {
        // given
        int count = 5;
        
        RecipeListResponse.RecipeSimpleInfo recipe1 = RecipeListResponse.RecipeSimpleInfo.builder()
                .id("meal-1")
                .name("Random Recipe 1")
                .imageUrl("http://example.com/1.jpg")
                .category("Dessert")
                .source("themealdb")
                .build();
        
        RecipeListResponse response = RecipeListResponse.builder()
                .recipes(Arrays.asList(recipe1))
                .totalCount(1)
                .source("themealdb")
                .build();

        when(recipeService.getRandomRecipes(count)).thenReturn(response);

        // when & then
        mockMvc.perform(get("/recipes/random")
                        .param("count", String.valueOf(count))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("recipes/random"))
                .andExpect(model().attributeExists("recipes"))
                .andExpect(model().attribute("count", count));
    }

    @Test
    @WithMockUser
    @DisplayName("랜덤 레시피 조회 - 개수 미지정 시 기본값 5")
    void randomRecipes_DefaultCount() throws Exception {
        // given
        int defaultCount = 5;
        
        RecipeListResponse response = RecipeListResponse.builder()
                .recipes(Collections.emptyList())
                .totalCount(0)
                .source("themealdb")
                .build();

        when(recipeService.getRandomRecipes(defaultCount)).thenReturn(response);

        // when & then
        mockMvc.perform(get("/recipes/random")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("recipes/random"))
                .andExpect(model().attribute("count", defaultCount));
    }

    @Test
    @WithMockUser
    @DisplayName("카테고리 목록 조회 - 성공")
    void categories_Success() throws Exception {
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

        List<CategoryResponse> categories = Arrays.asList(category1, category2);
        when(recipeService.getCategories()).thenReturn(categories);

        // when & then
        mockMvc.perform(get("/recipes/categories")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("recipes/categories"))
                .andExpect(model().attributeExists("categories"))
                .andExpect(model().attribute("categories", categories));
    }
}
