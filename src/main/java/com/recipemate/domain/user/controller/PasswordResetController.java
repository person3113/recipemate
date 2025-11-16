package com.recipemate.domain.user.controller;

import com.recipemate.domain.user.dto.PasswordResetFormDto;
import com.recipemate.domain.user.dto.PasswordResetRequestDto;
import com.recipemate.domain.user.entity.User;
import com.recipemate.domain.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Controller
@RequestMapping("/auth/password")
@RequiredArgsConstructor
public class PasswordResetController {

    private final UserService userService;
    private static final String SESSION_RESET_EMAIL = "resetUserEmail";
    private static final String SESSION_RESET_TOKEN = "resetToken";
    private static final long TOKEN_VALIDITY_MS = 10 * 60 * 1000; // 10분

    /**
     * 비밀번호 찾기 요청 페이지
     * GET /auth/password/reset-request
     */
    @GetMapping("/reset-request")
    public String resetRequestPage(Model model) {
        model.addAttribute("formData", new PasswordResetRequestDto());
        return "auth/password-reset-request";
    }

    /**
     * 비밀번호 찾기 요청 처리
     * POST /auth/password/reset-request
     */
    @PostMapping("/reset-request")
    public String processResetRequest(
            @Valid @ModelAttribute("formData") PasswordResetRequestDto request,
            BindingResult bindingResult,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "auth/password-reset-request";
        }

        Optional<User> userOpt = userService.verifyUserByEmailAndPhoneNumber(
                request.getEmail(),
                request.getPhoneNumber()
        );

        if (userOpt.isEmpty()) {
            model.addAttribute("error", "입력하신 이메일과 전화번호가 일치하는 계정이 없습니다.");
            return "auth/password-reset-request";
        }

        // 토큰 생성 및 세션 저장
        String token = UUID.randomUUID().toString();
        session.setAttribute(SESSION_RESET_EMAIL, request.getEmail());
        session.setAttribute(SESSION_RESET_TOKEN, token);
        session.setAttribute("tokenCreatedAt", System.currentTimeMillis());
        
        log.info("Password reset token created - Session ID: {}, Token: {}", session.getId(), token);
        log.info("Session attributes after setting: email={}, token={}, createdAt={}", 
                session.getAttribute(SESSION_RESET_EMAIL), 
                session.getAttribute(SESSION_RESET_TOKEN),
                session.getAttribute("tokenCreatedAt"));

        // 리다이렉트 대신 직접 비밀번호 재설정 폼으로 이동
        model.addAttribute("formData", new PasswordResetFormDto());
        model.addAttribute("token", token);
        return "auth/password-reset-form";
    }

    /**
     * 비밀번호 재설정 폼 페이지
     * GET /auth/password/reset
     */
    @GetMapping("/reset")
    public String resetFormPage(
            @RequestParam("token") String token,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        log.info("Password reset form requested - Session ID: {}, Token: {}", session.getId(), token);
        log.info("Session attributes: email={}, token={}, createdAt={}", 
                session.getAttribute(SESSION_RESET_EMAIL), 
                session.getAttribute(SESSION_RESET_TOKEN),
                session.getAttribute("tokenCreatedAt"));

        if (!validateToken(session, token)) {
            log.warn("Token validation failed - Session ID: {}, Token: {}", session.getId(), token);
            redirectAttributes.addFlashAttribute("error", "유효하지 않거나 만료된 링크입니다.");
            return "redirect:/auth/login";
        }

        model.addAttribute("formData", new PasswordResetFormDto());
        model.addAttribute("token", token);
        return "auth/password-reset-form";
    }

    /**
     * 비밀번호 재설정 처리
     * POST /auth/password/reset
     */
    @PostMapping("/reset")
    public String processPasswordReset(
            @Valid @ModelAttribute("formData") PasswordResetFormDto formDto,
            BindingResult bindingResult,
            @RequestParam(value = "token", required = false) String token,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        log.info("Password reset POST - Session ID: {}, Received Token: {}", session.getId(), token);
        log.info("Session attributes on POST: email={}, token={}, createdAt={}", 
                session.getAttribute(SESSION_RESET_EMAIL), 
                session.getAttribute(SESSION_RESET_TOKEN),
                session.getAttribute("tokenCreatedAt"));

        if (!validateToken(session, token)) {
            redirectAttributes.addFlashAttribute("error", "유효하지 않거나 만료된 링크입니다.");
            return "redirect:/auth/login";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("token", token);
            return "auth/password-reset-form";
        }

        // 비밀번호 일치 확인
        if (!formDto.getNewPassword().equals(formDto.getConfirmPassword())) {
            model.addAttribute("error", "비밀번호가 일치하지 않습니다.");
            model.addAttribute("token", token);
            return "auth/password-reset-form";
        }

        // 비밀번호 변경
        String email = (String) session.getAttribute(SESSION_RESET_EMAIL);
        userService.resetPassword(email, formDto.getNewPassword());

        // 세션 정보 삭제
        session.removeAttribute(SESSION_RESET_EMAIL);
        session.removeAttribute(SESSION_RESET_TOKEN);
        session.removeAttribute("tokenCreatedAt");

        redirectAttributes.addFlashAttribute("message", "비밀번호가 성공적으로 변경되었습니다. 새 비밀번호로 로그인해주세요.");
        return "redirect:/auth/login";
    }

    /**
     * 토큰 유효성 검증
     */
    private boolean validateToken(HttpSession session, String token) {
        String sessionToken = (String) session.getAttribute(SESSION_RESET_TOKEN);
        Long tokenCreatedAt = (Long) session.getAttribute("tokenCreatedAt");

        log.debug("Validating token - Requested: {}, Session: {}, CreatedAt: {}", 
                token, sessionToken, tokenCreatedAt);

        if (sessionToken == null || tokenCreatedAt == null) {
            log.warn("Token validation failed: sessionToken or tokenCreatedAt is null");
            return false;
        }

        // 토큰 일치 여부 확인
        if (!sessionToken.equals(token)) {
            log.warn("Token validation failed: token mismatch");
            return false;
        }

        // 토큰 만료 여부 확인 (10분)
        long currentTime = System.currentTimeMillis();
        boolean isValid = (currentTime - tokenCreatedAt) <= TOKEN_VALIDITY_MS;
        
        if (!isValid) {
            log.warn("Token validation failed: token expired");
        }
        
        return isValid;
    }
}
