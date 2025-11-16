//package com.recipemate.domain.user.controller;
//
//import com.recipemate.domain.user.dto.UserResponse;
//import com.recipemate.domain.user.service.AuthService;
//import com.recipemate.domain.user.service.UserService;
//import com.recipemate.global.config.TestSecurityConfig;
//import com.recipemate.global.exception.CustomException;
//import com.recipemate.global.exception.ErrorCode;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.context.annotation.Import;
//import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
//import org.springframework.security.test.context.support.WithMockUser;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.test.web.servlet.MockMvc;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.BDDMockito.given;
//import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@WebMvcTest(AuthController.class)
//@Import({TestSecurityConfig.class, com.recipemate.global.exception.MvcExceptionHandler.class})
//class AuthControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockitoBean
//    private UserService userService;
//
//    @MockitoBean
//    private AuthService authService;
//
//    @MockitoBean
//    private JpaMetamodelMappingContext jpaMetamodelMappingContext;
//
//    @MockitoBean
//    private com.recipemate.domain.user.service.CustomUserDetailsService customUserDetailsService;
//
//    @Test
//    @DisplayName("로그인 페이지 렌더링")
//    void loginPage() throws Exception {
//        mockMvc.perform(get("/auth/login"))
//                .andExpect(status().isOk())
//                .andExpect(view().name("auth/login"));
//    }
//
//    @Test
//    @DisplayName("회원가입 페이지 렌더링")
//    void signupPage() throws Exception {
//        mockMvc.perform(get("/auth/signup"))
//                .andExpect(status().isOk())
//                .andExpect(view().name("auth/signup"))
//                .andExpect(model().attributeExists("formData"))
//                .andExpect(model().attributeExists("signupRequest"));
//    }
//
//    @Test
//    @DisplayName("회원가입 폼 제출 성공 - 로그인 페이지로 리다이렉트")
//    void signup_Success() throws Exception {
//        // given
//        UserResponse mockResponse = UserResponse.builder()
//            .id(1L)
//            .email("new@example.com")
//            .nickname("새유저")
//            .phoneNumber("010-9999-9999")
//            .build();
//
//        given(userService.signup(any())).willReturn(mockResponse);
//
//        // when & then
//        mockMvc.perform(post("/auth/signup")
//                        .with(csrf())
//                        .param("email", "new@example.com")
//                        .param("password", "password123")
//                        .param("nickname", "새유저")
//                        .param("phoneNumber", "010-9999-9999"))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(redirectedUrl("/auth/login"))
//                .andExpect(flash().attributeExists("message"));
//    }
//
//    @Test
//    @DisplayName("회원가입 실패 - 중복 이메일")
//    void signup_Failure_DuplicateEmail() throws Exception {
//        // given
//        given(userService.signup(any()))
//            .willThrow(new CustomException(ErrorCode.DUPLICATE_EMAIL));
//
//        // when & then - 비즈니스 로직 에러는 redirect로 처리
//        mockMvc.perform(post("/auth/signup")
//                        .with(csrf())
//                        .param("email", "test@example.com")  // 이미 존재하는 이메일
//                        .param("password", "password123")
//                        .param("nickname", "새유저")
//                        .param("phoneNumber", "010-9999-9999"))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(redirectedUrl("/auth/signup"))
//                .andExpect(flash().attributeExists("errorMessage"));
//    }
//
//    @Test
//    @DisplayName("회원가입 실패 - 유효성 검증 실패")
//    void signup_Failure_ValidationError() throws Exception {
//        // when & then - 유효성 검증 실패 시 폼 페이지로 직접 반환 (redirect 없음)
//        mockMvc.perform(post("/auth/signup")
//                        .with(csrf())
//                        .param("email", "invalid-email")  // 잘못된 이메일 형식
//                        .param("password", "short")  // 최소 길이 미달
//                        .param("nickname", "a")  // 최소 길이 미달
//                        .param("phoneNumber", "123"))  // 잘못된 형식
//                .andExpect(status().isOk())
//                .andExpect(view().name("auth/signup"))
//                .andExpect(model().attributeExists("signupRequest"))
//                .andExpect(model().attributeHasFieldErrors("signupRequest", "email", "password", "nickname", "phoneNumber"));
//    }
//
//    @Test
//    @WithMockUser
//    @DisplayName("로그아웃 성공 - 로그인 페이지로 리다이렉트")
//    void logout_Success() throws Exception {
//        mockMvc.perform(post("/auth/logout")
//                        .with(csrf()))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(redirectedUrl("/auth/login"));
//    }
//}
