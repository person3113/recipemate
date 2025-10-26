package com.recipemate.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recipemate.global.exception.CustomException;
import com.recipemate.global.exception.ErrorCode;
import com.recipemate.domain.user.dto.LoginRequest;
import com.recipemate.domain.user.dto.SignupRequest;
import com.recipemate.domain.user.dto.UserResponse;
import com.recipemate.domain.user.service.AuthService;
import com.recipemate.domain.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
    @DisplayName("로그인 성공 - 세션이 생성되어야 한다")
    void login_Success() throws Exception {
        LoginRequest request = LoginRequest.of("test@example.com", "password123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.nickname").value("테스트유저"));
    }

    @Test
    @DisplayName("로그인 실패 - 잘못된 비밀번호")
    void login_Failure_WrongPassword() throws Exception {
        LoginRequest request = LoginRequest.of("test@example.com", "wrongpassword");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 이메일")
    void login_Failure_UserNotFound() throws Exception {
        LoginRequest request = LoginRequest.of("notfound@example.com", "password123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    @DisplayName("로그아웃 성공 - 세션이 무효화되어야 한다")
    void logout_Success() throws Exception {
        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
