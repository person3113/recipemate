package com.recipemate.global.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 로그인 성공 시 SavedRequest를 검증하여 불필요한 URL로의 리디렉션을 방지하는 커스텀 핸들러
 */
@Component
public class CustomAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
    
    private final HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
    
    public CustomAuthenticationSuccessHandler() {
        setDefaultTargetUrl("/");
    }
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                       HttpServletResponse response,
                                       Authentication authentication) throws ServletException, IOException {
        
        SavedRequest savedRequest = requestCache.getRequest(request, response);
        
        if (savedRequest != null) {
            String targetUrl = savedRequest.getRedirectUrl();
            
            // 부적절한 URL로의 리디렉션 방지
            if (isInvalidRedirectUrl(targetUrl)) {
                // SavedRequest를 제거하고 기본 URL로 이동
                requestCache.removeRequest(request, response);
                getRedirectStrategy().sendRedirect(request, response, getDefaultTargetUrl());
                return;
            }
        }
        
        // 정상적인 경우 기본 동작 수행 (SavedRequest가 있으면 해당 URL로, 없으면 기본 URL로)
        super.onAuthenticationSuccess(request, response, authentication);
    }
    
    /**
     * 리디렉션하면 안 되는 URL인지 검증
     */
    private boolean isInvalidRedirectUrl(String url) {
        if (url == null) {
            return false;
        }
        
        // 브라우저 내부 요청 경로들
        if (url.contains("/.well-known/")) {
            return true;
        }
        
        // 로그인/로그아웃 관련 경로
        if (url.contains("/auth/login") || url.contains("/auth/logout")) {
            return true;
        }
        
        // 에러 페이지
        if (url.contains("/error")) {
            return true;
        }
        
        // 정적 리소스 경로
        if (url.contains("/css/") || url.contains("/js/") || url.contains("/images/")) {
            return true;
        }
        
        // continue 파라미터가 있는 경우 (의도하지 않은 리디렉션)
        if (url.contains("?continue") || url.contains("&continue")) {
            return true;
        }
        
        return false;
    }
}
