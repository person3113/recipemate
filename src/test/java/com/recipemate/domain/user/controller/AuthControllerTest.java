package com.recipemate.domain.user.controller;

import com.recipemate.domain.user.dto.SignupRequest;
import com.recipemate.domain.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @BeforeEach
    void setUp() {
        SignupRequest signupRequest = SignupRequest.of(
                "test@example.com",
                "password123",
                "테스트유저",
                "010-1234-5678"
        );
        userService.signup(signupRequest);
    }

    @Test
    @DisplayName("로그인 페이지 렌더링")
    void loginPage() throws Exception {
        mockMvc.perform(get("/auth/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"));
    }

    @Test
    @DisplayName("회원가입 페이지 렌더링")
    void signupPage() throws Exception {
        mockMvc.perform(get("/auth/signup"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/signup"));
    }

    @Test
    @DisplayName("회원가입 폼 제출 성공 - 로그인 페이지로 리다이렉트")
    void signup_Success() throws Exception {
        mockMvc.perform(post("/auth/signup")
                        .with(csrf())
                        .param("email", "new@example.com")
                        .param("password", "password123")
                        .param("nickname", "새유저")
                        .param("phoneNumber", "010-9999-9999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login"))
                .andExpect(flash().attributeExists("message"));
    }

    @Test
    @DisplayName("회원가입 실패 - 중복 이메일")
    void signup_Failure_DuplicateEmail() throws Exception {
        mockMvc.perform(post("/auth/signup")
                        .with(csrf())
                        .param("email", "test@example.com")  // 이미 존재하는 이메일
                        .param("password", "password123")
                        .param("nickname", "새유저")
                        .param("phoneNumber", "010-9999-9999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/signup"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    @WithMockUser
    @DisplayName("로그아웃 성공 - 로그인 페이지로 리다이렉트")
    void logout_Success() throws Exception {
        mockMvc.perform(post("/auth/logout")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login"));
    }
}
