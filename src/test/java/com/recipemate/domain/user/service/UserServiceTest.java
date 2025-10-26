package com.recipemate.domain.user.service;

import com.recipemate.global.exception.CustomException;
import com.recipemate.domain.user.dto.SignupRequest;
import com.recipemate.domain.user.dto.UserResponse;
import com.recipemate.domain.user.entity.User;
import com.recipemate.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("회원가입에 성공한다")
    void signup_success() {
        SignupRequest request = SignupRequest.of(
                "test@example.com",
                "password123",
                "테스터",
                "010-1234-5678"
        );

        UserResponse response = userService.signup(request);

        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getNickname()).isEqualTo("테스터");
        assertThat(response.getMannerTemperature()).isEqualTo(36.5);
    }

    @Test
    @DisplayName("회원가입 시 비밀번호가 암호화된다")
    void signup_passwordEncoded() {
        SignupRequest request = SignupRequest.of(
                "test@example.com",
                "password123",
                "테스터",
                "010-1234-5678"
        );

        UserResponse response = userService.signup(request);

        User user = userRepository.findById(response.getId()).orElseThrow();
        assertThat(passwordEncoder.matches("password123", user.getPassword())).isTrue();
    }

    @Test
    @DisplayName("이메일이 중복되면 회원가입에 실패한다")
    void signup_duplicateEmail() {
        SignupRequest request1 = SignupRequest.of(
                "test@example.com",
                "password123",
                "테스터1",
                "010-1111-1111"
        );
        userService.signup(request1);

        SignupRequest request2 = SignupRequest.of(
                "test@example.com",
                "password456",
                "테스터2",
                "010-2222-2222"
        );

        assertThatThrownBy(() -> userService.signup(request2))
                .isInstanceOf(CustomException.class)
                .hasMessage("이미 존재하는 이메일입니다.");
    }

    @Test
    @DisplayName("닉네임이 중복되면 회원가입에 실패한다")
    void signup_duplicateNickname() {
        SignupRequest request1 = SignupRequest.of(
                "test1@example.com",
                "password123",
                "테스터",
                "010-1111-1111"
        );
        userService.signup(request1);

        SignupRequest request2 = SignupRequest.of(
                "test2@example.com",
                "password456",
                "테스터",
                "010-2222-2222"
        );

        assertThatThrownBy(() -> userService.signup(request2))
                .isInstanceOf(CustomException.class)
                .hasMessage("이미 존재하는 닉네임입니다.");
    }
}
