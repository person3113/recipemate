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

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.existsByEmail(ADMIN_EMAIL)) {
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
}
