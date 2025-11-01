package com.recipemate.global.config;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Bean Validation 설정
 */
@Configuration
public class ValidationConfig {

    /**
     * Jakarta Validator 빈 등록
     * 컨트롤러에서 수동 검증을 수행할 때 사용
     */
    @Bean
    public Validator validator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        return factory.getValidator();
    }
}
