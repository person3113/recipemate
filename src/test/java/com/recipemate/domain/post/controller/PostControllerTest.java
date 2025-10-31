package com.recipemate.domain.post.controller;

import com.recipemate.domain.post.dto.PostResponse;
import com.recipemate.domain.post.service.PostService;
import com.recipemate.domain.user.entity.User;
import com.recipemate.domain.user.repository.UserRepository;
import com.recipemate.global.common.PostCategory;
import com.recipemate.global.common.UserRole;
import com.recipemate.global.config.TestSecurityConfig;
import com.recipemate.global.exception.CustomException;
import com.recipemate.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PostController.class)
@Import({TestSecurityConfig.class, com.recipemate.global.exception.MvcExceptionHandler.class})
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private PostService postService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private com.recipemate.domain.user.service.CustomUserDetailsService customUserDetailsService;

    // ========== 페이지 렌더링 테스트 ==========

    @Test
    @DisplayName("게시글 목록 페이지 렌더링 성공")
    void listPage_success() throws Exception {
        // when & then
        mockMvc.perform(get("/community-posts/list"))
                .andExpect(status().isOk())
                .andExpect(view().name("community-posts/list"));
    }

    @Test
    @DisplayName("게시글 상세 페이지 렌더링 성공")
    void detailPage_success() throws Exception {
        // given
        Long postId = 1L;
        PostResponse mockResponse = PostResponse.builder()
                .id(postId)
                .title("공구 후기입니다")
                .content("지난번 참여한 공구 정말 좋았어요!")
                .category(PostCategory.REVIEW)
                .viewCount(6)
                .authorId(1L)
                .authorNickname("작성자")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(postService.getPostDetail(postId)).willReturn(mockResponse);

        // when & then
        mockMvc.perform(get("/community-posts/" + postId))
                .andExpect(status().isOk())
                .andExpect(view().name("community-posts/detail"))
                .andExpect(model().attributeExists("post"))
                .andExpect(model().attribute("post", mockResponse));
    }

    @Test
    @DisplayName("게시글 상세 페이지 렌더링 실패 - 존재하지 않는 게시글")
    void detailPage_failure_notFound() throws Exception {
        // given
        given(postService.getPostDetail(999L)).willThrow(new CustomException(ErrorCode.POST_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/community-posts/" + 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("게시글 작성 페이지 렌더링 성공")
    @WithMockUser
    void createPage_success() throws Exception {
        mockMvc.perform(get("/community-posts/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("community-posts/form"));
    }

    @Test
    @DisplayName("게시글 수정 페이지 렌더링 성공")
    @WithMockUser
    void editPage_success() throws Exception {
        // given
        Long postId = 1L;
        PostResponse mockResponse = PostResponse.builder()
                .id(postId)
                .title("수정할 게시글")
                .content("내용")
                .category(PostCategory.FREE)
                .viewCount(5)
                .authorId(1L)
                .authorNickname("작성자")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(postService.getPostDetail(postId)).willReturn(mockResponse);

        // when & then
        mockMvc.perform(get("/community-posts/" + postId + "/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("community-posts/form"))
                .andExpect(model().attributeExists("post"));
    }

    // ========== 폼 처리 테스트 ==========

    @Test
    @DisplayName("게시글 작성 폼 제출 성공")
    @WithMockUser(username = "test@example.com")
    void createPost_formSubmit_success() throws Exception {
        // given
        User mockUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("password")
                .nickname("테스터")
                .phoneNumber("010-1234-5678")
                .role(UserRole.USER)
                .build();

        PostResponse mockResponse = PostResponse.builder()
                .id(1L)
                .title("맛있는 레시피 공유합니다")
                .content("이번에 만든 파스타 레시피가 정말 맛있어서 공유합니다.")
                .category(PostCategory.TIPS)
                .viewCount(0)
                .authorId(1L)
                .authorNickname("테스터")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(mockUser));
        given(postService.createPost(any(), any())).willReturn(mockResponse);

        // when & then
        mockMvc.perform(post("/community-posts")
                        .with(csrf())
                        .param("title", "맛있는 레시피 공유합니다")
                        .param("content", "이번에 만든 파스타 레시피가 정말 맛있어서 공유합니다.")
                        .param("category", "TIPS"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/community-posts/1"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @DisplayName("게시글 작성 폼 제출 실패 - 유효성 검증 실패 (제목 누락)")
    @WithMockUser(username = "test@example.com")
    void createPost_formSubmit_failure_validation() throws Exception {
        // given
        User mockUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("password")
                .nickname("테스터")
                .phoneNumber("010-1234-5678")
                .role(UserRole.USER)
                .build();

        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(mockUser));

        // when & then
        mockMvc.perform(post("/community-posts")
                        .with(csrf())
                        .param("content", "내용만 있음")
                        .param("category", "FREE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/community-posts/new"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    @DisplayName("게시글 작성 폼 제출 실패 - 사용자를 찾을 수 없음")
    @WithMockUser(username = "notfound@example.com")
    void createPost_formSubmit_failure_userNotFound() throws Exception {
        // given
        given(userRepository.findByEmail("notfound@example.com")).willReturn(Optional.empty());

        // when & then
        mockMvc.perform(post("/community-posts")
                        .with(csrf())
                        .param("title", "제목")
                        .param("content", "내용")
                        .param("category", "FREE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/community-posts/new"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    @DisplayName("게시글 수정 폼 제출 성공")
    @WithMockUser(username = "test@example.com")
    void updatePost_formSubmit_success() throws Exception {
        // given
        Long postId = 1L;
        User mockUser = User.create("test@example.com", "password", "테스터", "010-1234-5678");

        PostResponse mockResponse = PostResponse.builder()
                .id(postId)
                .title("수정된 제목")
                .content("수정된 내용")
                .category(PostCategory.FREE)
                .viewCount(10)
                .authorId(1L)
                .authorNickname("테스터")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(mockUser));
        given(postService.updatePost(any(), eq(postId), any())).willReturn(mockResponse);

        // when & then
        mockMvc.perform(post("/community-posts/" + postId)
                        .with(csrf())
                        .param("title", "수정된 제목")
                        .param("content", "수정된 내용"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/community-posts/" + postId))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @DisplayName("게시글 수정 폼 제출 실패 - 작성자가 아님")
    @WithMockUser(username = "other@example.com")
    void updatePost_formSubmit_failure_notAuthor() throws Exception {
        // given
        Long postId = 1L;
        User mockUser = User.builder()
                .id(2L)
                .email("other@example.com")
                .password("password")
                .nickname("다른사람")
                .phoneNumber("010-9999-9999")
                .role(UserRole.USER)
                .build();

        given(userRepository.findByEmail("other@example.com")).willReturn(Optional.of(mockUser));
        given(postService.updatePost(eq(2L), eq(postId), any()))
                .willThrow(new CustomException(ErrorCode.UNAUTHORIZED_POST_ACCESS));

        // when & then
        mockMvc.perform(post("/community-posts/" + postId)
                        .with(csrf())
                        .param("title", "수정 시도")
                        .param("content", "수정 시도"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/community-posts/" + postId))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    @DisplayName("게시글 수정 폼 제출 실패 - 유효성 검증 실패")
    @WithMockUser(username = "test@example.com")
    void updatePost_formSubmit_failure_validation() throws Exception {
        // given
        Long postId = 1L;
        User mockUser = User.create("test@example.com", "password", "테스터", "010-1234-5678");

        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(mockUser));

        // when & then
        mockMvc.perform(post("/community-posts/" + postId)
                        .with(csrf())
                        .param("content", "내용만 있음"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/community-posts/" + postId + "/edit"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    @DisplayName("게시글 삭제 폼 제출 성공")
    @WithMockUser(username = "test@example.com")
    void deletePost_formSubmit_success() throws Exception {
        // given
        Long postId = 1L;
        User mockUser = User.create("test@example.com", "password", "테스터", "010-1234-5678");

        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(mockUser));

        // when & then
        mockMvc.perform(post("/community-posts/" + postId + "/delete")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/community-posts/list"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @DisplayName("게시글 삭제 폼 제출 실패 - 작성자가 아님")
    @WithMockUser(username = "other@example.com")
    void deletePost_formSubmit_failure_notAuthor() throws Exception {
        // given
        Long postId = 1L;
        User mockUser = User.builder()
                .id(2L)
                .email("other@example.com")
                .password("password")
                .nickname("다른사람")
                .phoneNumber("010-9999-9999")
                .role(UserRole.USER)
                .build();

        given(userRepository.findByEmail("other@example.com")).willReturn(Optional.of(mockUser));
        doThrow(new CustomException(ErrorCode.UNAUTHORIZED_POST_ACCESS))
                .when(postService).deletePost(eq(2L), eq(postId));

        // when & then
        mockMvc.perform(post("/community-posts/" + postId + "/delete")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/community-posts/" + postId))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    @DisplayName("게시글 삭제 폼 제출 실패 - 존재하지 않는 게시글")
    @WithMockUser(username = "test@example.com")
    void deletePost_formSubmit_failure_notFound() throws Exception {
        // given
        Long postId = 999L;
        User mockUser = User.create("test@example.com", "password", "테스터", "010-1234-5678");

        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(mockUser));
        doThrow(new CustomException(ErrorCode.POST_NOT_FOUND))
                .when(postService).deletePost(any(), eq(postId));

        // when & then
        mockMvc.perform(post("/community-posts/" + postId + "/delete")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/community-posts/list"))
                .andExpect(flash().attributeExists("errorMessage"));
    }
}
