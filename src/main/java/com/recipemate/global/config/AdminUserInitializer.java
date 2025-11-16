package com.recipemate.global.config;

import com.recipemate.domain.user.entity.User;
import com.recipemate.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class AdminUserInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String ADMIN_EMAIL = "admin@recipemate.com";
    private static final String ADMIN_PASSWORD = "admin123";
    private static final String ADMIN_NICKNAME = "관리자";
    private static final String ADMIN_PHONE = "010-0000-0000";

    private static final String TEST_USER1_EMAIL = "test1@recipemate.com";
    private static final String TEST_USER1_PASSWORD = "test123";
    private static final String TEST_USER1_NICKNAME = "테스트유저1";
    private static final String TEST_USER1_PHONE = "010-1111-1111";

    private static final String TEST_USER2_EMAIL = "test2@recipemate.com";
    private static final String TEST_USER2_PASSWORD = "test123";
    private static final String TEST_USER2_NICKNAME = "테스트유저2";
    private static final String TEST_USER2_PHONE = "010-2222-2222";

    @Override
    public void run(ApplicationArguments args) {
        createAdminUser();
        createTestUsers();
    }

    private void createAdminUser() {
        if (userRepository.existsByEmailIncludingDeleted(ADMIN_EMAIL)) {
            log.info("어드민 계정이 이미 존재합니다: {}", ADMIN_EMAIL);
            return;
        }

        String encodedPassword = passwordEncoder.encode(ADMIN_PASSWORD);
        User admin = User.createAdmin(
                ADMIN_EMAIL,
                encodedPassword,
                ADMIN_NICKNAME,
                ADMIN_PHONE
        );

        userRepository.save(admin);
        log.info("========================================");
        log.info("로컬 개발 환경 어드민 계정이 생성되었습니다");
        log.info("이메일: {}", ADMIN_EMAIL);
        log.info("비밀번호: {}", ADMIN_PASSWORD);
        log.info("========================================");
    }

    private void createTestUsers() {
        createTestUser(TEST_USER1_EMAIL, TEST_USER1_PASSWORD, TEST_USER1_NICKNAME, TEST_USER1_PHONE);
        createTestUser(TEST_USER2_EMAIL, TEST_USER2_PASSWORD, TEST_USER2_NICKNAME, TEST_USER2_PHONE);
    }

    private void createTestUser(String email, String password, String nickname, String phone) {
        if (userRepository.existsByEmailIncludingDeleted(email)) {
            log.info("테스트 계정이 이미 존재합니다: {}", email);
            return;
        }

        String encodedPassword = passwordEncoder.encode(password);
        User testUser = User.create(
                email,
                encodedPassword,
                nickname,
                phone
        );

        userRepository.save(testUser);
        log.info("========================================");
        log.info("테스트 계정이 생성되었습니다");
        log.info("이메일: {}", email);
        log.info("비밀번호: {}", password);
        log.info("닉네임: {}", nickname);
        log.info("========================================");
    }
}
