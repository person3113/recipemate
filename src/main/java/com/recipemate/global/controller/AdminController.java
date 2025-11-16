package com.recipemate.global.controller;

import com.recipemate.domain.recipe.dto.CorrectionProcessRequest;
import com.recipemate.domain.recipe.dto.RecipeCorrectionResponse;
import com.recipemate.domain.recipe.service.RecipeCorrectionService;
import com.recipemate.domain.user.entity.User;
import com.recipemate.domain.user.repository.UserRepository;
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

import java.util.List;

/**
 * 관리자 전용 컨트롤러
 * - 레시피 개선 제안 관리
 * - 신고 관리 (향후 구현)
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final RecipeCorrectionService correctionService;
    private final UserRepository userRepository;

    /**
     * 관리자 대시보드 (향후 구현)
     * GET /admin
     */
    @GetMapping
    public String adminDashboard() {
        return "admin/dashboard";
    }

    /**
     * 레시피 개선 제안 목록 페이지
     * GET /admin/corrections
     */
    @GetMapping("/corrections")
    public String correctionsList(Model model) {
        List<RecipeCorrectionResponse> pendingCorrections = correctionService.getPendingCorrections();
        model.addAttribute("corrections", pendingCorrections);
        model.addAttribute("totalCount", pendingCorrections.size());
        return "admin/corrections-list";
    }

    /**
     * 레시피 개선 제안 상세 페이지
     * GET /admin/corrections/{id}
     */
    @GetMapping("/corrections/{id}")
    public String correctionDetail(@PathVariable Long id, Model model) {
        RecipeCorrectionResponse correction = correctionService.getCorrection(id);
        model.addAttribute("correction", correction);
        return "admin/correction-detail";
    }

    /**
     * 레시피 개선 제안 승인 처리
     * POST /admin/corrections/{id}/approve
     */
    @PostMapping("/corrections/{id}/approve")
    public String approveCorrection(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @ModelAttribute CorrectionProcessRequest request,
            RedirectAttributes redirectAttributes) {
        try {
            User admin = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

            correctionService.approveCorrection(admin.getId(), id, request);
            redirectAttributes.addFlashAttribute("message", "제안이 승인되었습니다.");
        } catch (CustomException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/corrections/" + id;
        }

        return "redirect:/admin/corrections";
    }

    /**
     * 레시피 개선 제안 기각 처리
     * POST /admin/corrections/{id}/reject
     */
    @PostMapping("/corrections/{id}/reject")
    public String rejectCorrection(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @ModelAttribute CorrectionProcessRequest request,
            RedirectAttributes redirectAttributes) {
        try {
            User admin = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

            correctionService.rejectCorrection(admin.getId(), id, request);
            redirectAttributes.addFlashAttribute("message", "제안이 기각되었습니다.");
        } catch (CustomException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/corrections/" + id;
        }

        return "redirect:/admin/corrections";
    }

    // ========== 향후 구현: 신고 관리 ==========
    // @GetMapping("/reports")
    // @GetMapping("/reports/{id}")
    // @PostMapping("/reports/{id}/process")
}
