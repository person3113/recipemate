package com.recipemate.global.config;

import com.recipemate.domain.user.entity.User;
import com.recipemate.domain.user.repository.UserRepository;
import com.recipemate.global.common.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("dev")
class AdminUserInitializerTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AdminUserInitializer adminUserInitializer;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("어플리케이션 시작 시 어드민 계정이 생성된다")
    void shouldCreateAdminUserOnStartup() throws Exception {
        // when
        adminUserInitializer.run(null);

        // then
        User admin = userRepository.findByEmail("admin@recipemate.com").orElseThrow();
        assertThat(admin.getEmail()).isEqualTo("admin@recipemate.com");
        assertThat(admin.getNickname()).isEqualTo("관리자");
        assertThat(admin.getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(passwordEncoder.matches("admin123", admin.getPassword())).isTrue();
    }

    @Test
    @DisplayName("어드민 계정이 이미 존재하면 중복 생성하지 않는다")
    void shouldNotCreateDuplicateAdminUser() throws Exception {
        // given
        User existingAdmin = User.createAdmin(
                "admin@recipemate.com",
                passwordEncoder.encode("admin123"),
                "관리자",
                "010-0000-0000"
        );
        userRepository.save(existingAdmin);

        // when
        adminUserInitializer.run(null);

        // then
        long adminCount = userRepository.findAll().stream()
                .filter(user -> user.getEmail().equals("admin@recipemate.com"))
                .count();
        assertThat(adminCount).isEqualTo(1);
    }

    @Test
    @DisplayName("어드민 계정은 ADMIN 역할을 가진다")
    void adminUserShouldHaveAdminRole() throws Exception {
        // when
        adminUserInitializer.run(null);

        // then
        User admin = userRepository.findByEmail("admin@recipemate.com").orElseThrow();
        assertThat(admin.getRole()).isEqualTo(UserRole.ADMIN);
    }
}
