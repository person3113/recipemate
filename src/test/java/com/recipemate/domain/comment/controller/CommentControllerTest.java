package com.recipemate.domain.comment.controller;

import com.recipemate.domain.comment.dto.CommentResponse;
import com.recipemate.domain.comment.service.CommentService;
import com.recipemate.domain.user.entity.User;
import com.recipemate.domain.user.repository.UserRepository;
import com.recipemate.global.common.CommentType;
import com.recipemate.global.common.EntityType;
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
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CommentController.class)
@Import({TestSecurityConfig.class, com.recipemate.global.exception.MvcExceptionHandler.class, com.recipemate.global.config.ValidationConfig.class})
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private CommentService commentService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private com.recipemate.domain.user.service.CustomUserDetailsService customUserDetailsService;

    // ========== 댓글 작성 폼 제출 테스트 ==========

    @Test
    @DisplayName("공구에 댓글 작성 폼 제출 성공")
    @WithMockUser(username = "test@example.com")
    void createCommentOnGroupBuy_formSubmit_success() throws Exception {
        // given
        User mockUser = createMockUser();
        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(mockUser));

        CommentResponse mockResponse = CommentResponse.builder()
                .id(1L)
                .authorId(1L)
                .authorNickname("테스터")
                .content("참여하고 싶습니다!")
                .type(CommentType.Q_AND_A)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isDeleted(false)
                .build();

        given(commentService.createComment(eq(1L), any())).willReturn(mockResponse);

        // when & then
        mockMvc.perform(post("/comments")
                        .with(csrf())
                        .param("targetType", "GROUP_BUY")
                        .param("targetId", "1")
                        .param("content", "참여하고 싶습니다!")
                        .param("type", "Q_AND_A"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/group-purchases/1"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @DisplayName("게시글에 댓글 작성 폼 제출 성공")
    @WithMockUser(username = "test@example.com")
    void createCommentOnPost_formSubmit_success() throws Exception {
        // given
        User mockUser = createMockUser();
        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(mockUser));

        CommentResponse mockResponse = CommentResponse.builder()
                .id(1L)
                .authorId(1L)
                .authorNickname("테스터")
                .content("좋은 정보 감사합니다!")
                .type(CommentType.GENERAL)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isDeleted(false)
                .build();

        given(commentService.createComment(eq(1L), any())).willReturn(mockResponse);

        // when & then
        mockMvc.perform(post("/comments")
                        .with(csrf())
                        .param("targetType", "POST")
                        .param("targetId", "1")
                        .param("content", "좋은 정보 감사합니다!")
                        .param("type", "GENERAL"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/community-posts/1"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @DisplayName("대댓글 작성 폼 제출 성공")
    @WithMockUser(username = "test@example.com")
    void createReplyComment_formSubmit_success() throws Exception {
        // given
        User mockUser = createMockUser();
        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(mockUser));

        CommentResponse mockResponse = CommentResponse.builder()
                .id(2L)
                .authorId(1L)
                .authorNickname("테스터")
                .content("답변드립니다")
                .type(CommentType.GENERAL)
                .parentId(1L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isDeleted(false)
                .build();

        given(commentService.createComment(eq(1L), any())).willReturn(mockResponse);

        // when & then
        mockMvc.perform(post("/comments")
                        .with(csrf())
                        .param("targetType", "POST")
                        .param("targetId", "1")
                        .param("content", "답변드립니다")
                        .param("type", "GENERAL")
                        .param("parentId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/community-posts/1"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @DisplayName("댓글 작성 폼 제출 실패 - 유효성 검증 실패 (빈 내용)")
    @WithMockUser(username = "test@example.com")
    void createComment_formSubmit_failure_validation() throws Exception {
        // given
        User mockUser = createMockUser();
        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(mockUser));

        // when & then
        mockMvc.perform(post("/comments")
                        .with(csrf())
                        .param("targetType", "POST")
                        .param("targetId", "1")
                        .param("content", "")
                        .param("type", "GENERAL"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/community-posts/1"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    @DisplayName("댓글 작성 폼 제출 실패 - 대댓글 깊이 초과")
    @WithMockUser(username = "test@example.com")
    void createComment_formSubmit_failure_depthExceeded() throws Exception {
        // given
        User mockUser = createMockUser();
        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(mockUser));

        given(commentService.createComment(eq(1L), any()))
                .willThrow(new CustomException(ErrorCode.COMMENT_DEPTH_EXCEEDED));

        // when & then
        mockMvc.perform(post("/comments")
                        .with(csrf())
                        .param("targetType", "POST")
                        .param("targetId", "1")
                        .param("content", "대댓글의 대댓글")
                        .param("type", "GENERAL")
                        .param("parentId", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/community-posts/1"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    @DisplayName("댓글 작성 폼 제출 실패 - 사용자를 찾을 수 없음")
    @WithMockUser(username = "notfound@example.com")
    void createComment_formSubmit_failure_userNotFound() throws Exception {
        // given
        given(userRepository.findByEmail("notfound@example.com")).willReturn(Optional.empty());

        // when & then
        mockMvc.perform(post("/comments")
                        .with(csrf())
                        .param("targetType", "POST")
                        .param("targetId", "1")
                        .param("content", "댓글 내용")
                        .param("type", "GENERAL"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/community-posts/1"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    // ========== 댓글 수정 폼 제출 테스트 ==========

    @Test
    @DisplayName("댓글 수정 폼 제출 성공")
    @WithMockUser(username = "test@example.com")
    void updateComment_formSubmit_success() throws Exception {
        // given
        User mockUser = createMockUser();
        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(mockUser));

        CommentResponse mockResponse = CommentResponse.builder()
                .id(1L)
                .authorId(1L)
                .authorNickname("테스터")
                .content("수정된 내용")
                .type(CommentType.GENERAL)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isDeleted(false)
                .build();

        given(commentService.updateComment(eq(1L), eq(1L), any())).willReturn(mockResponse);

        // when & then
        mockMvc.perform(post("/comments/1/edit")
                        .with(csrf())
                        .param("content", "수정된 내용")
                        .param("targetType", "POST")
                        .param("targetId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/community-posts/1"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @DisplayName("댓글 수정 폼 제출 실패 - 권한 없음")
    @WithMockUser(username = "test@example.com")
    void updateComment_formSubmit_failure_unauthorized() throws Exception {
        // given
        User mockUser = createMockUser();
        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(mockUser));

        given(commentService.updateComment(eq(1L), eq(1L), any()))
                .willThrow(new CustomException(ErrorCode.UNAUTHORIZED_COMMENT_UPDATE));

        // when & then
        mockMvc.perform(post("/comments/1/edit")
                        .with(csrf())
                        .param("content", "수정 시도")
                        .param("targetType", "POST")
                        .param("targetId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/community-posts/1"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    @DisplayName("댓글 수정 폼 제출 실패 - 존재하지 않는 댓글")
    @WithMockUser(username = "test@example.com")
    void updateComment_formSubmit_failure_notFound() throws Exception {
        // given
        User mockUser = createMockUser();
        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(mockUser));

        given(commentService.updateComment(eq(1L), eq(999L), any()))
                .willThrow(new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        // when & then
        mockMvc.perform(post("/comments/999/edit")
                        .with(csrf())
                        .param("content", "수정 시도")
                        .param("targetType", "POST")
                        .param("targetId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/community-posts/1"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    // ========== 댓글 삭제 폼 제출 테스트 ==========

    @Test
    @DisplayName("댓글 삭제 폼 제출 성공")
    @WithMockUser(username = "test@example.com")
    void deleteComment_formSubmit_success() throws Exception {
        // given
        User mockUser = createMockUser();
        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(mockUser));

        doNothing().when(commentService).deleteComment(1L, 1L);

        // when & then
        mockMvc.perform(post("/comments/1/delete")
                        .with(csrf())
                        .param("targetType", "POST")
                        .param("targetId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/community-posts/1"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @DisplayName("댓글 삭제 폼 제출 실패 - 권한 없음")
    @WithMockUser(username = "test@example.com")
    void deleteComment_formSubmit_failure_unauthorized() throws Exception {
        // given
        User mockUser = createMockUser();
        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(mockUser));

        doThrow(new CustomException(ErrorCode.UNAUTHORIZED_COMMENT_DELETE))
                .when(commentService).deleteComment(1L, 1L);

        // when & then
        mockMvc.perform(post("/comments/1/delete")
                        .with(csrf())
                        .param("targetType", "POST")
                        .param("targetId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/community-posts/1"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    @DisplayName("댓글 삭제 폼 제출 실패 - 존재하지 않는 댓글")
    @WithMockUser(username = "test@example.com")
    void deleteComment_formSubmit_failure_notFound() throws Exception {
        // given
        User mockUser = createMockUser();
        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(mockUser));

        doThrow(new CustomException(ErrorCode.COMMENT_NOT_FOUND))
                .when(commentService).deleteComment(1L, 999L);

        // when & then
        mockMvc.perform(post("/comments/999/delete")
                        .with(csrf())
                        .param("targetType", "POST")
                        .param("targetId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/community-posts/1"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    // ========== htmx Fragment 엔드포인트 테스트 ==========

    @Test
    @DisplayName("댓글 목록 HTML Fragment 조회 성공")
    void getCommentsFragment_success() throws Exception {
        // given
        CommentResponse comment1 = CommentResponse.builder()
                .id(1L)
                .authorId(1L)
                .authorNickname("작성자1")
                .content("첫 번째 댓글")
                .type(CommentType.GENERAL)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isDeleted(false)
                .replies(Arrays.asList())
                .build();

        CommentResponse comment2 = CommentResponse.builder()
                .id(2L)
                .authorId(2L)
                .authorNickname("작성자2")
                .content("두 번째 댓글")
                .type(CommentType.GENERAL)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isDeleted(false)
                .replies(Arrays.asList())
                .build();

        given(commentService.getCommentsByTarget(EntityType.POST, 1L))
                .willReturn(Arrays.asList(comment1, comment2));

        // when & then
        mockMvc.perform(get("/comments/fragments")
                        .param("targetType", "POST")
                        .param("targetId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/comments :: comment-list"))
                .andExpect(model().attributeExists("comments"));
    }

    // === Helper Methods ===

    private User createMockUser() {
        return User.builder()
                .id(1L)
                .email("test@example.com")
                .password("password")
                .nickname("테스터")
                .phoneNumber("010-1234-5678")
                .role(UserRole.USER)
                .build();
    }
}
