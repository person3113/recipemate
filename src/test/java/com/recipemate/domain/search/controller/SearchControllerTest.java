//package com.recipemate.domain.search.controller;
//
//import com.recipemate.domain.search.dto.SearchResultResponse;
//import com.recipemate.domain.search.dto.UnifiedSearchResponse;
//import com.recipemate.domain.search.service.SearchService;
//import com.recipemate.global.common.EntityType;
//import com.recipemate.global.config.TestSecurityConfig;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.context.annotation.Import;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.util.Arrays;
//import java.util.Collections;
//
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.BDDMockito.given;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@WebMvcTest(SearchController.class)
//@Import({TestSecurityConfig.class, com.recipemate.global.exception.MvcExceptionHandler.class})
//class SearchControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockitoBean
//    private SearchService searchService;
//
//    @MockitoBean
//    private JpaMetamodelMappingContext jpaMetamodelMappingContext;
//
//    @MockitoBean
//    private com.recipemate.domain.user.service.CustomUserDetailsService customUserDetailsService;
//
//    // ========== 페이지 렌더링 테스트 ==========
//
//    @Test
//    @DisplayName("검색 결과 페이지 렌더링 성공 - 모든 도메인 결과 있음")
//    void searchResultsPage_Success_WithAllResults() throws Exception {
//        // given
//        String keyword = "삼겹살";
//
//        SearchResultResponse groupBuyResult = SearchResultResponse.builder()
//            .id(1L)
//            .title("삼겹살 공동구매")
//            .content("신선한 삼겹살")
//            .entityType(EntityType.GROUP_BUY)
//            .build();
//
//        SearchResultResponse postResult = SearchResultResponse.builder()
//            .id(2L)
//            .title("삼겹살 맛집 추천")
//            .content("삼겹살 맛집 공유")
//            .entityType(EntityType.POST)
//            .build();
//
//        SearchResultResponse recipeResult = SearchResultResponse.builder()
//            .apiId("food-1234")
//            .title("삼겹살 김치찜")
//            .content("찜")
//            .entityType(EntityType.RECIPE)
//            .imageUrl("http://example.com/image.jpg")
//            .build();
//
//        UnifiedSearchResponse mockResponse = UnifiedSearchResponse.builder()
//            .query(keyword)
//            .groupBuys(Arrays.asList(groupBuyResult))
//            .posts(Arrays.asList(postResult))
//            .recipes(Arrays.asList(recipeResult))
//            .totalResults(3)
//            .build();
//
//        given(searchService.unifiedSearch(eq(keyword), any(Pageable.class)))
//            .willReturn(mockResponse);
//
//        // when & then
//        mockMvc.perform(get("/search")
//                .param("query", keyword))
//            .andExpect(status().isOk())
//            .andExpect(view().name("search/results"))
//            .andExpect(model().attributeExists("searchResults"))
//            .andExpect(model().attribute("query", keyword))
//            .andExpect(model().attribute("totalResults", 3));
//    }
//
//    @Test
//    @DisplayName("검색 결과 페이지 렌더링 성공 - 결과 없음")
//    void searchResultsPage_Success_NoResults() throws Exception {
//        // given
//        String keyword = "존재하지않는검색어";
//
//        UnifiedSearchResponse mockResponse = UnifiedSearchResponse.builder()
//            .query(keyword)
//            .groupBuys(Collections.emptyList())
//            .posts(Collections.emptyList())
//            .recipes(Collections.emptyList())
//            .totalResults(0)
//            .build();
//
//        given(searchService.unifiedSearch(eq(keyword), any(Pageable.class)))
//            .willReturn(mockResponse);
//
//        // when & then
//        mockMvc.perform(get("/search")
//                .param("query", keyword))
//            .andExpect(status().isOk())
//            .andExpect(view().name("search/results"))
//            .andExpect(model().attributeExists("searchResults"))
//            .andExpect(model().attribute("query", keyword))
//            .andExpect(model().attribute("totalResults", 0));
//    }
//
//    @Test
//    @DisplayName("검색 결과 페이지 렌더링 실패 - 빈 검색어")
//    void searchResultsPage_Fail_EmptyQuery() throws Exception {
//        // given
//        String emptyKeyword = "";
//
//        given(searchService.unifiedSearch(eq(emptyKeyword), any(Pageable.class)))
//            .willThrow(new IllegalArgumentException("검색어를 입력해주세요"));
//
//        // when & then
//        mockMvc.perform(get("/search")
//                .param("query", emptyKeyword))
//            .andExpect(status().is4xxClientError());
//    }
//
//    @Test
//    @DisplayName("검색 결과 페이지 렌더링 실패 - 검색어 파라미터 없음")
//    void searchResultsPage_Fail_NoQueryParameter() throws Exception {
//        // when & then
//        mockMvc.perform(get("/search"))
//            .andExpect(status().is4xxClientError());
//    }
//
//    @Test
//    @DisplayName("검색 결과 페이지 렌더링 성공 - 페이지네이션")
//    void searchResultsPage_Success_WithPagination() throws Exception {
//        // given
//        String keyword = "테스트";
//        int page = 1;
//        int size = 10;
//
//        UnifiedSearchResponse mockResponse = UnifiedSearchResponse.builder()
//            .query(keyword)
//            .groupBuys(Collections.emptyList())
//            .posts(Collections.emptyList())
//            .recipes(Collections.emptyList())
//            .totalResults(0)
//            .build();
//
//        given(searchService.unifiedSearch(eq(keyword), any(Pageable.class)))
//            .willReturn(mockResponse);
//
//        // when & then
//        mockMvc.perform(get("/search")
//                .param("query", keyword)
//                .param("page", String.valueOf(page))
//                .param("size", String.valueOf(size)))
//            .andExpect(status().isOk())
//            .andExpect(view().name("search/results"))
//            .andExpect(model().attributeExists("searchResults"));
//    }
//
//    // ========== htmx 프래그먼트 테스트 ==========
//
//    @Test
//    @DisplayName("검색 프래그먼트 - GROUP_BUY 타입만 조회")
//    void searchFragments_GroupBuyOnly() throws Exception {
//        // given
//        String keyword = "삼겹살";
//        String type = "GROUP_BUY";
//
//        SearchResultResponse groupBuyResult = SearchResultResponse.builder()
//            .id(1L)
//            .title("삼겹살 공동구매")
//            .content("신선한 삼겹살")
//            .entityType(EntityType.GROUP_BUY)
//            .build();
//
//        UnifiedSearchResponse mockResponse = UnifiedSearchResponse.builder()
//            .query(keyword)
//            .groupBuys(Arrays.asList(groupBuyResult))
//            .posts(Collections.emptyList())
//            .recipes(Collections.emptyList())
//            .totalResults(1)
//            .build();
//
//        given(searchService.unifiedSearch(eq(keyword), any(Pageable.class)))
//            .willReturn(mockResponse);
//
//        // when & then
//        mockMvc.perform(get("/search/fragments")
//                .param("query", keyword)
//                .param("type", type))
//            .andExpect(status().isOk())
//            .andExpect(view().name("search/fragments :: groupBuyResults"))
//            .andExpect(model().attribute("results", mockResponse.getGroupBuys()));
//    }
//
//    @Test
//    @DisplayName("검색 프래그먼트 - POST 타입만 조회")
//    void searchFragments_PostOnly() throws Exception {
//        // given
//        String keyword = "맛집";
//        String type = "POST";
//
//        SearchResultResponse postResult = SearchResultResponse.builder()
//            .id(2L)
//            .title("맛집 추천")
//            .content("맛집 공유")
//            .entityType(EntityType.POST)
//            .build();
//
//        UnifiedSearchResponse mockResponse = UnifiedSearchResponse.builder()
//            .query(keyword)
//            .groupBuys(Collections.emptyList())
//            .posts(Arrays.asList(postResult))
//            .recipes(Collections.emptyList())
//            .totalResults(1)
//            .build();
//
//        given(searchService.unifiedSearch(eq(keyword), any(Pageable.class)))
//            .willReturn(mockResponse);
//
//        // when & then
//        mockMvc.perform(get("/search/fragments")
//                .param("query", keyword)
//                .param("type", type))
//            .andExpect(status().isOk())
//            .andExpect(view().name("search/fragments :: postResults"))
//            .andExpect(model().attribute("results", mockResponse.getPosts()));
//    }
//
//    @Test
//    @DisplayName("검색 프래그먼트 - RECIPE 타입만 조회")
//    void searchFragments_RecipeOnly() throws Exception {
//        // given
//        String keyword = "김치찜";
//        String type = "RECIPE";
//
//        SearchResultResponse recipeResult = SearchResultResponse.builder()
//            .apiId("food-1234")
//            .title("삼겹살 김치찜")
//            .content("찜")
//            .entityType(EntityType.RECIPE)
//            .imageUrl("http://example.com/image.jpg")
//            .build();
//
//        UnifiedSearchResponse mockResponse = UnifiedSearchResponse.builder()
//            .query(keyword)
//            .groupBuys(Collections.emptyList())
//            .posts(Collections.emptyList())
//            .recipes(Arrays.asList(recipeResult))
//            .totalResults(1)
//            .build();
//
//        given(searchService.unifiedSearch(eq(keyword), any(Pageable.class)))
//            .willReturn(mockResponse);
//
//        // when & then
//        mockMvc.perform(get("/search/fragments")
//                .param("query", keyword)
//                .param("type", type))
//            .andExpect(status().isOk())
//            .andExpect(view().name("search/fragments :: recipeResults"))
//            .andExpect(model().attribute("results", mockResponse.getRecipes()));
//    }
//
//    @Test
//    @DisplayName("검색 프래그먼트 - 타입 파라미터 없을 때 실패")
//    void searchFragments_Fail_NoTypeParameter() throws Exception {
//        // when & then
//        mockMvc.perform(get("/search/fragments")
//                .param("query", "테스트"))
//            .andExpect(status().is4xxClientError());
//    }
//
//    @Test
//    @DisplayName("검색 프래그먼트 - 잘못된 타입 파라미터")
//    void searchFragments_Fail_InvalidType() throws Exception {
//        // when & then
//        mockMvc.perform(get("/search/fragments")
//                .param("query", "테스트")
//                .param("type", "INVALID_TYPE"))
//            .andExpect(status().is4xxClientError());
//    }
//}
