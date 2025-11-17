package com.recipemate.domain.report.controller;

import com.recipemate.domain.report.dto.ReportCreateRequest;
import com.recipemate.domain.report.dto.ReportResponse;
import com.recipemate.domain.report.entity.ReportType;
import com.recipemate.domain.report.entity.ReportedEntityType;
import com.recipemate.domain.report.service.ReportService;
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
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.beans.PropertyEditorSupport;

/**
 * 신고 컨트롤러 (사용자용)
 */
@Controller
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final UserRepository userRepository;

    /**
     * Enum 타입 바인딩 설정
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(ReportedEntityType.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                setValue(ReportedEntityType.valueOf(text));
            }
        });
        
        binder.registerCustomEditor(ReportType.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                setValue(ReportType.valueOf(text));
            }
        });
    }

    /**
     * 신고 작성 페이지
     * GET /reports/new
     */
    @GetMapping("/new")
    public String reportForm(
            @RequestParam("entityType") ReportedEntityType entityType,
            @RequestParam("entityId") Long entityId,
            @RequestParam(value = "entityName", required = false, defaultValue = "콘텐츠") String entityName,
            Model model) {
        
        model.addAttribute("entityType", entityType);
        model.addAttribute("entityId", entityId);
        model.addAttribute("entityName", entityName);
        
        return "reports/form";
    }

    /**
     * 신고 제출
     * POST /reports
     */
    @PostMapping
    public String createReport(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @ModelAttribute ReportCreateRequest request,
            RedirectAttributes redirectAttributes) {
        try {
            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

            ReportResponse response = reportService.createReport(user.getId(), request);

            redirectAttributes.addFlashAttribute("message", "신고가 접수되었습니다. 검토 후 조치하겠습니다.");
            
            String redirectUrl = request.getReportedEntityType().getEntityUrl(request.getReportedEntityId());
            if (redirectUrl != null) {
                return "redirect:" + redirectUrl;
            }
            return "redirect:/";
            
        } catch (CustomException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/";
        }
    }
}
