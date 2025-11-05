package com.recipemate.global.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 로그인 실패 시 입력한 이메일을 유지하기 위한 커스텀 핸들러
 */
@Component
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {
    
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, 
                                       HttpServletResponse response,
                                       AuthenticationException exception) throws IOException, ServletException {
        // 입력한 이메일을 세션에 저장
        String email = request.getParameter("email");
        if (email != null && !email.isEmpty()) {
            request.getSession().setAttribute("lastAttemptedEmail", email);
        }
        
        // 기본 실패 URL로 리다이렉트
        setDefaultFailureUrl("/auth/login?error=true");
        super.onAuthenticationFailure(request, response, exception);
    }
}
