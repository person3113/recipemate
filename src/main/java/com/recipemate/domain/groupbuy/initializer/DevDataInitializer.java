package com.recipemate.domain.groupbuy.initializer;

import com.recipemate.domain.groupbuy.dto.CreateGroupBuyRequest;
import com.recipemate.domain.groupbuy.service.GroupBuyService;
import com.recipemate.domain.user.entity.User;
import com.recipemate.domain.user.repository.UserRepository;
import com.recipemate.global.common.DeliveryMethod;
import com.recipemate.global.common.GroupBuyCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
// import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * DevDataInitializer는 테스트용으로 추가되었으나 사용자 요청으로 비활성화 처리합니다.
 * 실행을 원하면 파일 상단의 @Component 주석을 해제하세요.
 */
//@Component // Disabled by user request
@RequiredArgsConstructor
@Slf4j
public class DevDataInitializer implements CommandLineRunner {

    private final GroupBuyService groupBuyService;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("DevDataInitializer is disabled. To enable, uncomment @Component in the source file.");
    }
}

