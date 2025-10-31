package com.recipemate.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate 설정
 * 외부 API 호출 시 사용되는 RestTemplate을 빈으로 등록
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);  // 연결 타임아웃: 5초
        factory.setReadTimeout(10000);     // 읽기 타임아웃: 10초
        
        return new RestTemplate(factory);
    }
}
