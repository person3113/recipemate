package com.recipemate.domain.user.controller;

import com.recipemate.domain.user.dto.ChangePasswordRequest;
import com.recipemate.domain.user.dto.UpdateProfileRequest;
import com.recipemate.domain.user.dto.UserResponse;
import com.recipemate.domain.user.service.UserService;
import com.recipemate.global.exception.CustomException;
import com.recipemate.global.exception.ErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 마이페이지 렌더링
     * GET /users/me
     */
    @GetMapping("/me")
    public String myPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        UserResponse userResponse = userService.getMyProfile(userDetails.getUsername());
        model.addAttribute("user", userResponse);
        return "user/my-page";
    }

    /**
     * 프로필 수정 폼 제출 처리
     * POST /users/me
     */
    @PostMapping("/me")
    public String updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @ModelAttribute UpdateProfileRequest request,
            RedirectAttributes redirectAttributes) {
        try {
            userService.updateProfile(userDetails.getUsername(), request);
            redirectAttributes.addFlashAttribute("message", "프로필이 수정되었습니다.");
            return "redirect:/users/me";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "프로필 수정에 실패했습니다.");
            return "redirect:/users/me";
        }
    }

    /**
     * 비밀번호 변경 폼 제출 처리
     * POST /users/me/password
     */
    @PostMapping("/me/password")
    public String changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @ModelAttribute ChangePasswordRequest request,
            RedirectAttributes redirectAttributes) {
        try {
            userService.changePassword(userDetails.getUsername(), request);
            redirectAttributes.addFlashAttribute("message", "비밀번호가 변경되었습니다.");
            return "redirect:/users/me";
        } catch (CustomException e) {
            if (e.getErrorCode() == ErrorCode.INVALID_PASSWORD) {
                redirectAttributes.addFlashAttribute("error", "현재 비밀번호가 일치하지 않습니다.");
            } else {
                redirectAttributes.addFlashAttribute("error", "비밀번호 변경에 실패했습니다.");
            }
            return "redirect:/users/me";
        }
    }
    
    // ========== htmx용 HTML Fragment 엔드포인트 (향후 추가) ==========
    // TODO: htmx 통합 시 아래 엔드포인트 구현
    // @GetMapping("/me/fragments/profile") - 프로필 정보 HTML 조각
    // @PostMapping("/me/fragments/profile") - 프로필 수정 폼 처리 후 HTML 조각 반환
    // @GetMapping("/me/fragments/group-purchases") - 내 공구 목록 HTML 조각
    // @GetMapping("/me/fragments/participations") - 내 참여 공구 목록 HTML 조각
}
