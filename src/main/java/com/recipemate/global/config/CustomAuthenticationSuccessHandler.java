package com.recipemate.global.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 로그인 성공 시 SavedRequest를 검증하여 불필요한 URL로의 리디렉션을 방지하는 커스텀 핸들러
 */
@Slf4j
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
        
        log.info("=== Login Success Handler ===");
        
        // 1. 먼저 continue 파라미터 확인
        String continueUrl = request.getParameter("continue");
        if (continueUrl != null && !continueUrl.isBlank() && !isInvalidRedirectUrl(continueUrl)) {
            log.info("Continue parameter found: {}", continueUrl);
            getRedirectStrategy().sendRedirect(request, response, continueUrl);
            return;
        }
        
        // 2. SavedRequest 확인
        SavedRequest savedRequest = requestCache.getRequest(request, response);
        if (savedRequest != null) {
            String targetUrl = savedRequest.getRedirectUrl();
            log.info("SavedRequest found: {}", targetUrl);
            
            // 부적절한 URL로의 리디렉션 방지
            if (isInvalidRedirectUrl(targetUrl)) {
                log.info("Invalid redirect URL detected, redirecting to default: {}", getDefaultTargetUrl());
                requestCache.removeRequest(request, response);
                getRedirectStrategy().sendRedirect(request, response, getDefaultTargetUrl());
                return;
            }
            log.info("Valid redirect URL, will redirect to: {}", targetUrl);
        } else {
            log.info("No SavedRequest found, will redirect to default: {}", getDefaultTargetUrl());
        }
        
        // 3. 정상적인 경우 기본 동작 수행 (SavedRequest가 있으면 해당 URL로, 없으면 기본 URL로)
        super.onAuthenticationSuccess(request, response, authentication);
    }
    
    /**
     * 리디렉션하면 안 되는 URL인지 검증
     */
    private boolean isInvalidRedirectUrl(String url) {
        if (url == null) {
            return false;
        }
        
        // 외부 URL 차단 (open redirect 방지) - 상대 경로만 허용
        if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("//")) {
            log.warn("External or absolute URL blocked: {}", url);
            return true;
        }
        
        // 상대 경로가 아닌 경우 차단 (반드시 /로 시작해야 함)
        if (!url.startsWith("/")) {
            log.warn("Invalid URL format (must start with /): {}", url);
            return true;
        }
        
        // 브라우저 내부 요청 경로들
        if (url.contains("/.well-known/")) {
            return true;
        }
        
        // 로그인/로그아웃 관련 경로
        if (url.contains("/auth/login") || url.contains("/auth/logout") || url.contains("/auth/signup")) {
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
        
        return false;
    }
}
