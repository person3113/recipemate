//package com.recipemate.domain.search.service;
//
//import com.recipemate.domain.groupbuy.dto.GroupBuyResponse;
//import com.recipemate.domain.groupbuy.entity.GroupBuy;
//import com.recipemate.domain.groupbuy.repository.GroupBuyRepository;
//import com.recipemate.domain.post.dto.PostResponse;
//import com.recipemate.domain.post.entity.Post;
//import com.recipemate.domain.post.repository.PostRepository;
//import com.recipemate.domain.recipe.dto.CookRecipeResponse;
//import com.recipemate.domain.recipe.dto.RecipeListResponse;
//import com.recipemate.domain.recipe.service.RecipeService;
//import com.recipemate.domain.search.dto.SearchResultResponse;
//import com.recipemate.domain.search.dto.UnifiedSearchResponse;
//import com.recipemate.domain.user.entity.User;
//import com.recipemate.global.common.DeliveryMethod;
//import com.recipemate.global.common.GroupBuyStatus;
//import com.recipemate.global.common.PostCategory;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.BDDMockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class SearchServiceTest {
//
//    @InjectMocks
//    private SearchService searchService;
//
//    @Mock
//    private GroupBuyRepository groupBuyRepository;
//
//    @Mock
//    private PostRepository postRepository;
//
//    @Mock
//    private RecipeService recipeService;
//
//    private User testUser;
//    private Pageable defaultPageable;
//
//    @BeforeEach
//    void setUp() {
//        testUser = User.create(
//            "test@example.com",
//            "encodedPassword",
//            "테스터",
//            "010-1234-5678"
//        );
//        defaultPageable = PageRequest.of(0, 10);
//    }
//
//    @Test
//    @DisplayName("통합 검색 - 모든 도메인에서 결과 조회 성공")
//    void unifiedSearch_Success_AllDomains() {
//        // given
//        String keyword = "삼겹살";
//
//        // GroupBuy 목 데이터
//        GroupBuy groupBuy = GroupBuy.createGeneral(
//            testUser,
//            "삼겹살 공동구매",
//            "신선한 삼겹살",
//            "육류",
//            50000,
//            5,
//            LocalDateTime.now().plusDays(7),
//            DeliveryMethod.BOTH,
//            "강남역",
//            3000,
//            true
//        );
//        Page<GroupBuy> groupBuyPage = new PageImpl<>(Arrays.asList(groupBuy));
//        given(groupBuyRepository.searchByKeyword(eq(keyword), eq(GroupBuyStatus.RECRUITING), any(Pageable.class)))
//            .willReturn(groupBuyPage);
//
//        // Post 목 데이터
//        Post post = Post.builder()
//            .author(testUser)
//            .title("삼겹살 맛집 추천")
//            .content("삼겹살 맛집 공유")
//            .category(PostCategory.REVIEW)
//            .viewCount(0)
//            .build();
//        Page<Post> postPage = new PageImpl<>(Arrays.asList(post));
//        given(postRepository.searchByKeyword(eq(keyword), any(Pageable.class)))
//            .willReturn(postPage);
//
//        // Recipe 목 데이터
//        CookRecipeResponse recipe = CookRecipeResponse.builder()
//            .rcpSeq("1234")
//            .rcpNm("삼겹살 김치찜")
//            .rcpWay2("찜")
//            .attFileNoMain("http://example.com/image.jpg")
//            .build();
//        RecipeListResponse recipeListResponse = RecipeListResponse.builder()
//            .recipes(Arrays.asList(
//                RecipeListResponse.RecipeSimpleInfo.builder()
//                    .id("food-1234")
//                    .name("삼겹살 김치찜")
//                    .imageUrl("http://example.com/image.jpg")
//                    .category("찜")
//                    .source("foodsafety")
//                    .build()
//            ))
//            .totalCount(1)
//            .source("foodsafety")
//            .build();
//        given(recipeService.searchRecipes(eq(keyword)))
//            .willReturn(recipeListResponse);
//
//        // when
//        UnifiedSearchResponse response = searchService.unifiedSearch(keyword, defaultPageable);
//
//        // then
//        assertThat(response).isNotNull();
//        assertThat(response.getQuery()).isEqualTo(keyword);
//        assertThat(response.getTotalResults()).isEqualTo(3);
//        assertThat(response.getGroupBuys()).hasSize(1);
//        assertThat(response.getPosts()).hasSize(1);
//        assertThat(response.getRecipes()).hasSize(1);
//
//        verify(groupBuyRepository, times(1)).searchByKeyword(eq(keyword), eq(GroupBuyStatus.RECRUITING), any(Pageable.class));
//        verify(postRepository, times(1)).searchByKeyword(eq(keyword), any(Pageable.class));
//        verify(recipeService, times(1)).searchRecipes(eq(keyword));
//    }
//
//    @Test
//    @DisplayName("통합 검색 - 결과 없음")
//    void unifiedSearch_NoResults() {
//        // given
//        String keyword = "존재하지않는검색어";
//
//        Page<GroupBuy> emptyGroupBuyPage = new PageImpl<>(Collections.emptyList());
//        given(groupBuyRepository.searchByKeyword(eq(keyword), eq(GroupBuyStatus.RECRUITING), any(Pageable.class)))
//            .willReturn(emptyGroupBuyPage);
//
//        Page<Post> emptyPostPage = new PageImpl<>(Collections.emptyList());
//        given(postRepository.searchByKeyword(eq(keyword), any(Pageable.class)))
//            .willReturn(emptyPostPage);
//
//        RecipeListResponse emptyRecipeResponse = RecipeListResponse.builder()
//            .recipes(Collections.emptyList())
//            .totalCount(0)
//            .source("both")
//            .build();
//        given(recipeService.searchRecipes(eq(keyword)))
//            .willReturn(emptyRecipeResponse);
//
//        // when
//        UnifiedSearchResponse response = searchService.unifiedSearch(keyword, defaultPageable);
//
//        // then
//        assertThat(response).isNotNull();
//        assertThat(response.getQuery()).isEqualTo(keyword);
//        assertThat(response.getTotalResults()).isEqualTo(0);
//        assertThat(response.getGroupBuys()).isEmpty();
//        assertThat(response.getPosts()).isEmpty();
//        assertThat(response.getRecipes()).isEmpty();
//    }
//
//    @Test
//    @DisplayName("통합 검색 - 공동구매만 결과 있음")
//    void unifiedSearch_GroupBuyOnly() {
//        // given
//        String keyword = "공구";
//
//        // GroupBuy 목 데이터
//        GroupBuy groupBuy = GroupBuy.createGeneral(
//            testUser,
//            "공구 모집",
//            "신선한 식재료 공구",
//            "채소",
//            30000,
//            5,
//            LocalDateTime.now().plusDays(7),
//            DeliveryMethod.PARCEL,
//            null,
//            3000,
//            true
//        );
//        Page<GroupBuy> groupBuyPage = new PageImpl<>(Arrays.asList(groupBuy));
//        given(groupBuyRepository.searchByKeyword(eq(keyword), eq(GroupBuyStatus.RECRUITING), any(Pageable.class)))
//            .willReturn(groupBuyPage);
//
//        Page<Post> emptyPostPage = new PageImpl<>(Collections.emptyList());
//        given(postRepository.searchByKeyword(eq(keyword), any(Pageable.class)))
//            .willReturn(emptyPostPage);
//
//        RecipeListResponse emptyRecipeResponse = RecipeListResponse.builder()
//            .recipes(Collections.emptyList())
//            .totalCount(0)
//            .source("both")
//            .build();
//        given(recipeService.searchRecipes(eq(keyword)))
//            .willReturn(emptyRecipeResponse);
//
//        // when
//        UnifiedSearchResponse response = searchService.unifiedSearch(keyword, defaultPageable);
//
//        // then
//        assertThat(response).isNotNull();
//        assertThat(response.getTotalResults()).isEqualTo(1);
//        assertThat(response.getGroupBuys()).hasSize(1);
//        assertThat(response.getPosts()).isEmpty();
//        assertThat(response.getRecipes()).isEmpty();
//    }
//
//    @Test
//    @DisplayName("통합 검색 - 빈 키워드 입력 시 예외 발생")
//    void unifiedSearch_EmptyKeyword_ThrowsException() {
//        // given
//        String emptyKeyword = "";
//
//        // when & then
//        assertThatThrownBy(() -> searchService.unifiedSearch(emptyKeyword, defaultPageable))
//            .isInstanceOf(IllegalArgumentException.class)
//            .hasMessage("검색어를 입력해주세요");
//    }
//
//    @Test
//    @DisplayName("통합 검색 - null 키워드 입력 시 예외 발생")
//    void unifiedSearch_NullKeyword_ThrowsException() {
//        // given
//        String nullKeyword = null;
//
//        // when & then
//        assertThatThrownBy(() -> searchService.unifiedSearch(nullKeyword, defaultPageable))
//            .isInstanceOf(IllegalArgumentException.class)
//            .hasMessage("검색어를 입력해주세요");
//    }
//
//    @Test
//    @DisplayName("통합 검색 - 페이지 크기 제한 적용")
//    void unifiedSearch_PageSizeLimit() {
//        // given
//        String keyword = "테스트";
//        Pageable largePage = PageRequest.of(0, 100);
//
//        Page<GroupBuy> groupBuyPage = new PageImpl<>(Collections.emptyList());
//        given(groupBuyRepository.searchByKeyword(eq(keyword), eq(GroupBuyStatus.RECRUITING), any(Pageable.class)))
//            .willReturn(groupBuyPage);
//
//        Page<Post> postPage = new PageImpl<>(Collections.emptyList());
//        given(postRepository.searchByKeyword(eq(keyword), any(Pageable.class)))
//            .willReturn(postPage);
//
//        RecipeListResponse recipeResponse = RecipeListResponse.builder()
//            .recipes(Collections.emptyList())
//            .totalCount(0)
//            .source("both")
//            .build();
//        given(recipeService.searchRecipes(eq(keyword)))
//            .willReturn(recipeResponse);
//
//        // when
//        UnifiedSearchResponse response = searchService.unifiedSearch(keyword, largePage);
//
//        // then
//        assertThat(response).isNotNull();
//        verify(groupBuyRepository, times(1))
//            .searchByKeyword(eq(keyword), eq(GroupBuyStatus.RECRUITING), argThat(pageable ->
//                pageable.getPageSize() <= 20
//            ));
//    }
//}
