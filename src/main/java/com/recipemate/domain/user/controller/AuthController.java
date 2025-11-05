package com.recipemate.domain.user.controller;

import com.recipemate.domain.user.dto.SignupRequest;
import com.recipemate.domain.user.service.AuthService;
import com.recipemate.domain.user.service.UserService;
import com.recipemate.global.exception.CustomException;
import com.recipemate.global.exception.ErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthService authService;

    /**
     * 로그인 페이지 렌더링
     * GET /auth/login
     */
    @GetMapping("/login")
    public String loginPage(
            @RequestParam(value = "error", required = false) Boolean error,
            Model model) {
        if (error != null && error) {
            model.addAttribute("error", "이메일 또는 비밀번호가 올바르지 않습니다.");
        }
        return "auth/login";
    }
    
    /**
     * 회원가입 페이지 렌더링
     * GET /auth/signup
     */
    @GetMapping("/signup")
    public String signupPage() {
        return "auth/signup";
    }

    /**
     * 회원가입 폼 제출 처리
     * POST /auth/signup
     */
    @PostMapping("/signup")
    public String signup(
            @Valid @ModelAttribute SignupRequest request,
            RedirectAttributes redirectAttributes) {
        userService.signup(request);
        redirectAttributes.addFlashAttribute("message", "회원가입이 완료되었습니다. 로그인해주세요.");
        return "redirect:/auth/login";
    }

    /**
     * 로그아웃 처리
     * POST /auth/logout
     * 
     * Note: 실제 로그아웃은 Spring Security의 LogoutFilter가 처리합니다.
     * 이 메서드는 커스텀 로그아웃 로직이 필요한 경우에만 사용됩니다.
     */
    @PostMapping("/logout")
    public String logout(RedirectAttributes redirectAttributes) {
        authService.logout();
        redirectAttributes.addFlashAttribute("message", "로그아웃되었습니다.");
        return "redirect:/auth/login";
    }
    
    // ========== htmx용 HTML Fragment 엔드포인트 (향후 추가) ==========
    // TODO: htmx 통합 시 아래 엔드포인트 구현
    // @PostMapping("/signup-fragment") - 회원가입 폼 처리 후 HTML 조각 반환 (인라인 검증 등)
    // @PostMapping("/login-fragment") - 로그인 폼 처리 후 HTML 조각 반환
}
