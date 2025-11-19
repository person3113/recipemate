package com.recipemate.global.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * 모든 요청에 대해 현재 URI를 모델에 추가하는 인터셉터
 * Thymeleaf에서 #request 객체를 직접 사용할 수 없게 되면서,
 * 로그인 리다이렉트를 위한 현재 URI를 제공하기 위해 사용
 */
@Component
public class RequestAttributeInterceptor implements HandlerInterceptor {
    
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, 
                          Object handler, ModelAndView modelAndView) {
        if (modelAndView != null) {
            // 현재 요청 URI를 모델에 추가 (쿼리 파라미터 포함)
            String requestURI = request.getRequestURI();
            String queryString = request.getQueryString();
            
            // 쿼리 파라미터가 있으면 URI에 추가
            String fullUri = requestURI;
            if (queryString != null && !queryString.isEmpty()) {
                fullUri = requestURI + "?" + queryString;
            }
            
            modelAndView.addObject("currentUri", fullUri);
        }
    }
}
