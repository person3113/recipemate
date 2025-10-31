package com.recipemate.domain.user.controller;

import com.recipemate.domain.user.dto.SignupRequest;
import com.recipemate.domain.user.dto.UserResponse;
import com.recipemate.domain.user.entity.User;
import com.recipemate.domain.user.repository.UserRepository;
import com.recipemate.domain.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Long userId;

    @BeforeEach
    void setUp() {
        SignupRequest signupRequest = SignupRequest.of(
                "test@example.com",
                "password123",
                "테스트유저",
                "010-1234-5678"
        );
        UserResponse response = userService.signup(signupRequest);
        userId = response.getId();
    }

    @Test
    @DisplayName("내 프로필 페이지 렌더링 성공")
    void myPage_Success() throws Exception {
        mockMvc.perform(get("/users/me")
                        .with(user("test@example.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("user/my-page"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    @DisplayName("프로필 수정 폼 제출 성공 - 리다이렉트")
    void updateProfile_Success() throws Exception {
        mockMvc.perform(post("/users/me")
                        .with(user("test@example.com").roles("USER"))
                        .with(csrf())
                        .param("nickname", "수정된닉네임")
                        .param("phoneNumber", "010-9999-9999")
                        .param("profileImageUrl", "https://example.com/profile.jpg"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/me"));

        User user = userRepository.findById(userId).orElseThrow();
        assertThat(user.getNickname()).isEqualTo("수정된닉네임");
        assertThat(user.getPhoneNumber()).isEqualTo("010-9999-9999");
        assertThat(user.getProfileImageUrl()).isEqualTo("https://example.com/profile.jpg");
    }

    @Test
    @DisplayName("프로필 수정 - 닉네임만 수정")
    void updateProfile_NicknameOnly() throws Exception {
        mockMvc.perform(post("/users/me")
                        .with(user("test@example.com").roles("USER"))
                        .with(csrf())
                        .param("nickname", "새닉네임"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/me"));

        User user = userRepository.findById(userId).orElseThrow();
        assertThat(user.getNickname()).isEqualTo("새닉네임");
        assertThat(user.getPhoneNumber()).isEqualTo("010-1234-5678"); // 기존 값 유지
    }

    @Test
    @DisplayName("비밀번호 변경 폼 제출 성공 - 리다이렉트")
    void changePassword_Success() throws Exception {
        mockMvc.perform(post("/users/me/password")
                        .with(user("test@example.com").roles("USER"))
                        .with(csrf())
                        .param("currentPassword", "password123")
                        .param("newPassword", "newPassword456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/me"));

        User user = userRepository.findById(userId).orElseThrow();
        assertThat(passwordEncoder.matches("newPassword456", user.getPassword())).isTrue();
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 현재 비밀번호 불일치")
    void changePassword_Failure_WrongCurrentPassword() throws Exception {
        mockMvc.perform(post("/users/me/password")
                        .with(user("test@example.com").roles("USER"))
                        .with(csrf())
                        .param("currentPassword", "wrongPassword")
                        .param("newPassword", "newPassword456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/me"))
                .andExpect(flash().attributeExists("error"));
    }

    // TODO: Security 설정 확인 후 재활성화
    // @Test
    // @DisplayName("프로필 페이지 조회 실패 - 인증되지 않은 사용자")
    // void myPage_Failure_Unauthenticated() throws Exception {
    //     mockMvc.perform(get("/users/me"))
    //             .andExpect(status().is3xxRedirection()); // Spring Security가 로그인 페이지로 리다이렉트
    // }
}
