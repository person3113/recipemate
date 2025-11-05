package com.recipemate.global.config;

import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 웹 서버 설정
 * Tomcat의 멀티파트 요청 제한 등을 커스터마이징
 */
@Configuration
public class WebConfig {

    /**
     * Tomcat 멀티파트 요청 제한 설정
     * 레시피 기반 공동구매 폼에서 많은 재료 필드를 처리하기 위해
     * 파일 개수 제한을 확장
     */
    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatCustomizer() {
        return factory -> factory.addConnectorCustomizers(connector -> {
            connector.setProperty("maxParameterCount", "1000");
            connector.setMaxPostSize(31457280); // 30MB
        });
    }
}
