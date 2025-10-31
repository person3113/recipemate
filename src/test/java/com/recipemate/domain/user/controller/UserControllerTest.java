package com.recipemate.domain.user.controller;

import com.recipemate.domain.user.dto.UserResponse;
import com.recipemate.domain.user.entity.User;
import com.recipemate.domain.user.repository.UserRepository;
import com.recipemate.domain.user.service.UserService;
import com.recipemate.domain.wishlist.dto.WishlistResponse;
import com.recipemate.domain.wishlist.service.WishlistService;
import com.recipemate.global.common.DeliveryMethod;
import com.recipemate.global.common.GroupBuyStatus;
import com.recipemate.global.config.TestSecurityConfig;
import com.recipemate.global.exception.CustomException;
import com.recipemate.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import({TestSecurityConfig.class, com.recipemate.global.exception.MvcExceptionHandler.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private UserService userService;
    
    @MockitoBean
    private WishlistService wishlistService;
    
    @MockitoBean
    private UserRepository userRepository;
    
    @MockitoBean
    private com.recipemate.domain.user.service.CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("내 프로필 페이지 렌더링 성공")
    @WithMockUser(username = "test@example.com")
    void myPage_Success() throws Exception {
        // given
        UserResponse mockResponse = UserResponse.builder()
            .id(1L)
            .email("test@example.com")
            .nickname("테스트유저")
            .phoneNumber("010-1234-5678")
            .build();
        
        given(userService.getMyProfile("test@example.com")).willReturn(mockResponse);

        // when & then
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/my-page"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    @DisplayName("프로필 수정 폼 제출 성공 - 리다이렉트")
    @WithMockUser(username = "test@example.com")
    void updateProfile_Success() throws Exception {
        // when & then
        mockMvc.perform(post("/users/me")
                        .with(csrf())
                        .param("nickname", "수정된닉네임")
                        .param("phoneNumber", "010-9999-9999")
                        .param("profileImageUrl", "https://example.com/profile.jpg"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/me"));
    }

    @Test
    @DisplayName("프로필 수정 - 닉네임만 수정")
    @WithMockUser(username = "test@example.com")
    void updateProfile_NicknameOnly() throws Exception {
        // when & then
        mockMvc.perform(post("/users/me")
                        .with(csrf())
                        .param("nickname", "새닉네임"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/me"));
    }

    @Test
    @DisplayName("비밀번호 변경 폼 제출 성공 - 리다이렉트")
    @WithMockUser(username = "test@example.com")
    void changePassword_Success() throws Exception {
        // when & then
        mockMvc.perform(post("/users/me/password")
                        .with(csrf())
                        .param("currentPassword", "password123")
                        .param("newPassword", "newPassword456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/me"));
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 현재 비밀번호 불일치")
    @WithMockUser(username = "test@example.com")
    void changePassword_Failure_WrongCurrentPassword() throws Exception {
        // given
        doThrow(new CustomException(ErrorCode.INVALID_PASSWORD))
            .when(userService).changePassword(eq("test@example.com"), any());

        // when & then
        mockMvc.perform(post("/users/me/password")
                        .with(csrf())
                        .param("currentPassword", "wrongPassword")
                        .param("newPassword", "newPassword456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/me"))
                .andExpect(flash().attribute("errorMessage", ErrorCode.INVALID_PASSWORD.getMessage()));
    }

    // ========== 찜 목록 조회 테스트 ==========

    @Test
    @DisplayName("내 찜 목록 페이지 렌더링 성공")
    @WithMockUser(username = "test@example.com")
    void myWishlistPage_Success() throws Exception {
        // given
        User mockUser = User.create("test@example.com", "password", "테스트유저", "010-1234-5678");
        WishlistResponse mockWishlist = WishlistResponse.builder()
            .wishlistId(1L)
            .wishedAt(LocalDateTime.now())
            .groupBuyId(1L)
            .title("양파 공동구매")
            .category("채소")
            .totalPrice(20000)
            .targetHeadcount(5)
            .currentHeadcount(3)
            .deadline(LocalDateTime.now().plusDays(7))
            .deliveryMethod(DeliveryMethod.BOTH)
            .status(GroupBuyStatus.RECRUITING)
            .hostId(2L)
            .hostNickname("주최자")
            .build();
        Page<WishlistResponse> mockPage = new PageImpl<>(Collections.singletonList(mockWishlist));
        
        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(mockUser));
        given(wishlistService.getMyWishlist(any(), any(Pageable.class))).willReturn(mockPage);

        // when & then
        mockMvc.perform(get("/users/me/bookmarks"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/bookmarks"))
                .andExpect(model().attributeExists("wishlists"));
    }

    @Test
    @DisplayName("내 찜 목록 페이지 렌더링 - 빈 목록")
    @WithMockUser(username = "test@example.com")
    void myWishlistPage_EmptyList() throws Exception {
        // given
        User mockUser = User.create("test@example.com", "password", "테스트유저", "010-1234-5678");
        Page<WishlistResponse> emptyPage = new PageImpl<>(Collections.emptyList());
        
        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(mockUser));
        given(wishlistService.getMyWishlist(any(), any(Pageable.class))).willReturn(emptyPage);

        // when & then
        mockMvc.perform(get("/users/me/bookmarks"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/bookmarks"))
                .andExpect(model().attributeExists("wishlists"));
    }
}

