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
            Model model,
            jakarta.servlet.http.HttpSession session) {
        if (error != null && error) {
            model.addAttribute("error", "이메일 또는 비밀번호가 올바르지 않습니다.");
            
            // 세션에서 마지막으로 시도한 이메일 가져오기
            String lastAttemptedEmail = (String) session.getAttribute("lastAttemptedEmail");
            if (lastAttemptedEmail != null) {
                model.addAttribute("lastEmail", lastAttemptedEmail);
                // 사용 후 세션에서 제거
                session.removeAttribute("lastAttemptedEmail");
            }
        }
        return "auth/login";
    }
    
    /**
     * 회원가입 페이지 렌더링
     * GET /auth/signup
     */
    @GetMapping("/signup")
    public String signupPage(Model model) {
        // Flash 속성으로 전달된 폼 데이터가 있는지 확인 (PRG 패턴)
        if (!model.containsAttribute("formData")) {
            // 빈 폼 객체 추가 (Thymeleaf th:object를 위해 필수)
            SignupRequest formData = new SignupRequest();
            model.addAttribute("formData", formData);
            model.addAttribute("signupRequest", formData); // POST 핸들러와의 호환성
        } else {
            // Flash 속성에서 전달된 formData가 있으면 signupRequest에도 동일하게 설정
            model.addAttribute("signupRequest", model.getAttribute("formData"));
        }
        return "auth/signup";
    }

    /**
     * 회원가입 폼 제출 처리
     * POST /auth/signup
     * PRG (Post-Redirect-Get) 패턴 적용: 유효성 검증 실패 시에도 리다이렉트
     */
    @PostMapping("/signup")
    public String signup(
            @Valid @ModelAttribute("signupRequest") SignupRequest request,
            org.springframework.validation.BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        // 유효성 검증 실패 처리 (PRG 패턴 적용)
        if (bindingResult.hasErrors()) {
            // Flash 속성에 폼 데이터와 검증 오류 저장
            redirectAttributes.addFlashAttribute("formData", request);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.formData", bindingResult);
            redirectAttributes.addFlashAttribute("error", 
                bindingResult.getAllErrors().get(0).getDefaultMessage());
            // 회원가입 페이지로 리다이렉트 (새로고침 시 폼 재제출 방지)
            return "redirect:/auth/signup";
        }
        
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
    
    /**
     * 이메일 중복 확인 (htmx용)
     * POST /auth/check-email
     */
    @PostMapping("/check-email")
    public String checkEmail(@RequestParam("email") String email, Model model) {
        try {
            userService.validateEmailAvailability(email);
            model.addAttribute("available", true);
            model.addAttribute("message", "사용 가능한 이메일입니다.");
        } catch (CustomException e) {
            model.addAttribute("available", false);
            model.addAttribute("message", e.getMessage());
        }
        return "auth/fragments/validation-message";
    }
    
    /**
     * 닉네임 중복 확인 (htmx용)
     * POST /auth/check-nickname
     */
    @PostMapping("/check-nickname")
    public String checkNickname(@RequestParam("nickname") String nickname, Model model) {
        try {
            userService.validateNicknameAvailability(nickname);
            model.addAttribute("available", true);
            model.addAttribute("message", "사용 가능한 닉네임입니다.");
        } catch (CustomException e) {
            model.addAttribute("available", false);
            model.addAttribute("message", e.getMessage());
        }
        return "auth/fragments/validation-message";
    }
    
    // ========== htmx용 HTML Fragment 엔드포인트 (향후 추가) ==========
    // TODO: htmx 통합 시 아래 엔드포인트 구현
    // @PostMapping("/signup-fragment") - 회원가입 폼 처리 후 HTML 조각 반환 (인라인 검증 등)
    // @PostMapping("/login-fragment") - 로그인 폼 처리 후 HTML 조각 반환
}
