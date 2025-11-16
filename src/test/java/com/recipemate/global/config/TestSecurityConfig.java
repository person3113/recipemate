//package com.recipemate.global.config;
//
//import org.springframework.boot.test.context.TestConfiguration;
//import org.springframework.context.annotation.Bean;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
//import org.springframework.security.web.SecurityFilterChain;
//
///**
// * 컨트롤러 단위 테스트용 Security 설정
// * 모든 엔드포인트를 permitAll로 설정하여 Security 인증 없이 컨트롤러 로직만 테스트
// */
//@TestConfiguration
//public class TestSecurityConfig {
//
//    @Bean
//    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
//        http
//            .csrf(AbstractHttpConfigurer::disable)
//            .authorizeHttpRequests(auth -> auth
//                .anyRequest().permitAll()
//            );
//
//        return http.build();
//    }
//}
