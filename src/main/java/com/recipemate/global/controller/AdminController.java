package com.recipemate.global.controller;

import com.recipemate.domain.recipe.dto.CorrectionProcessRequest;
import com.recipemate.domain.recipe.dto.RecipeCorrectionResponse;
import com.recipemate.domain.recipe.service.RecipeCorrectionService;
import com.recipemate.domain.report.dto.ReportProcessRequest;
import com.recipemate.domain.report.dto.ReportResponse;
import com.recipemate.domain.report.service.ReportService;
import com.recipemate.domain.user.service.CustomUserDetailsService.CustomUserDetails;
import com.recipemate.global.exception.CustomException;
import com.recipemate.global.exception.ErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * 관리자 전용 컨트롤러
 * - 레시피 개선 제안 관리
 * - 신고 관리
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final RecipeCorrectionService correctionService;
    private final ReportService reportService;

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
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @ModelAttribute CorrectionProcessRequest request,
            RedirectAttributes redirectAttributes) {
        try {
            correctionService.approveCorrection(userDetails.getUserId(), id, request);
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
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @ModelAttribute CorrectionProcessRequest request,
            RedirectAttributes redirectAttributes) {
        try {
            correctionService.rejectCorrection(userDetails.getUserId(), id, request);
            redirectAttributes.addFlashAttribute("message", "제안이 기각되었습니다.");
        } catch (CustomException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/corrections/" + id;
        }

        return "redirect:/admin/corrections";
    }

    // ========== 신고 관리 ==========

    /**
     * 신고 목록 페이지
     * GET /admin/reports
     */
    @GetMapping("/reports")
    public String reportsList(Model model) {
        List<ReportResponse> pendingReports = reportService.getPendingReports();
        model.addAttribute("reports", pendingReports);
        model.addAttribute("totalCount", pendingReports.size());
        return "admin/reports-list";
    }

    /**
     * 신고 상세 페이지
     * GET /admin/reports/{id}
     */
    @GetMapping("/reports/{id}")
    public String reportDetail(@PathVariable Long id, Model model) {
        ReportResponse report = reportService.getReport(id);
        model.addAttribute("report", report);
        return "admin/report-detail";
    }

    /**
     * 신고 처리
     * POST /admin/reports/{id}/process
     */
    @PostMapping("/reports/{id}/process")
    public String processReport(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @ModelAttribute ReportProcessRequest request,
            RedirectAttributes redirectAttributes) {
        try {
            if ("PROCESS".equals(request.getAction())) {
                reportService.processReport(userDetails.getUserId(), id, request.getNotes());
                redirectAttributes.addFlashAttribute("message", "신고가 처리되었습니다.");
            } else if ("DISMISS".equals(request.getAction())) {
                reportService.dismissReport(userDetails.getUserId(), id, request.getNotes());
                redirectAttributes.addFlashAttribute("message", "신고가 기각되었습니다.");
            } else {
                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "올바르지 않은 처리 방식입니다.");
            }
        } catch (CustomException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/reports/" + id;
        }

        return "redirect:/admin/reports";
    }

    /**
     * 신고된 콘텐츠 삭제 (POST/COMMENT만 지원)
     * POST /admin/reports/{id}/delete-content
     */
    @PostMapping("/reports/{id}/delete-content")
    public String deleteReportedContent(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(value = "adminNotes", required = false) String adminNotes,
            RedirectAttributes redirectAttributes) {
        try {
            reportService.deleteReportedContent(userDetails.getUserId(), id, adminNotes);
            redirectAttributes.addFlashAttribute("message", "콘텐츠가 삭제되고 신고가 처리되었습니다.");
        } catch (CustomException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/reports/" + id;
        }

        return "redirect:/admin/reports";
    }
}
