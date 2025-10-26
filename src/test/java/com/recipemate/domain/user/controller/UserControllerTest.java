package com.recipemate.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recipemate.domain.user.dto.ChangePasswordRequest;
import com.recipemate.domain.user.dto.SignupRequest;
import com.recipemate.domain.user.dto.UpdateProfileRequest;
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
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
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
    private ObjectMapper objectMapper;

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
    @DisplayName("내 프로필 조회 성공")
    void getMyProfile_Success() throws Exception {
        mockMvc.perform(get("/users/me")
                        .with(user("test@example.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.nickname").value("테스트유저"))
                .andExpect(jsonPath("$.data.phoneNumber").value("010-1234-5678"))
                .andExpect(jsonPath("$.data.mannerTemperature").value(36.5));
    }

    @Test
    @DisplayName("프로필 수정 성공")
    void updateProfile_Success() throws Exception {
        UpdateProfileRequest request = UpdateProfileRequest.of(
                "수정된닉네임",
                "010-9999-9999",
                "https://example.com/profile.jpg"
        );

        mockMvc.perform(put("/users/me")
                        .with(user("test@example.com").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.nickname").value("수정된닉네임"))
                .andExpect(jsonPath("$.data.phoneNumber").value("010-9999-9999"))
                .andExpect(jsonPath("$.data.profileImageUrl").value("https://example.com/profile.jpg"));

        User user = userRepository.findById(userId).orElseThrow();
        assertThat(user.getNickname()).isEqualTo("수정된닉네임");
        assertThat(user.getPhoneNumber()).isEqualTo("010-9999-9999");
        assertThat(user.getProfileImageUrl()).isEqualTo("https://example.com/profile.jpg");
    }

    @Test
    @DisplayName("프로필 수정 - 닉네임만 수정")
    void updateProfile_NicknameOnly() throws Exception {
        UpdateProfileRequest request = UpdateProfileRequest.of(
                "새닉네임",
                null,
                null
        );

        mockMvc.perform(put("/users/me")
                        .with(user("test@example.com").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.nickname").value("새닉네임"))
                .andExpect(jsonPath("$.data.phoneNumber").value("010-1234-5678"));
    }

    @Test
    @DisplayName("비밀번호 변경 성공")
    void changePassword_Success() throws Exception {
        ChangePasswordRequest request = ChangePasswordRequest.of(
                "password123",
                "newPassword456"
        );

        mockMvc.perform(put("/users/me/password")
                        .with(user("test@example.com").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        User user = userRepository.findById(userId).orElseThrow();
        assertThat(passwordEncoder.matches("newPassword456", user.getPassword())).isTrue();
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 현재 비밀번호 불일치")
    void changePassword_Failure_WrongCurrentPassword() throws Exception {
        ChangePasswordRequest request = ChangePasswordRequest.of(
                "wrongPassword",
                "newPassword456"
        );

        mockMvc.perform(put("/users/me/password")
                        .with(user("test@example.com").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("프로필 조회 실패 - 인증되지 않은 사용자")
    void getMyProfile_Failure_Unauthenticated() throws Exception {
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isForbidden());
    }
}
