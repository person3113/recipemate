package com.recipemate.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 스케줄링 설정
 * @Scheduled 어노테이션을 사용한 작업 실행을 활성화
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
