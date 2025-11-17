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
import org.springframework.web.util.UriUtils;

import java.beans.PropertyEditorSupport;
import java.nio.charset.StandardCharsets;

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
            
            // 신고 대상에 따라 리다이렉트 URL 결정
            String redirectUrl = getRedirectUrl(request);
            return "redirect:" + redirectUrl;
            
        } catch (CustomException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/";
        }
    }
    
    /**
     * 신고 제출 후 리다이렉트 URL 결정
     * 한글 닉네임의 경우 URL 인코딩 처리
     */
    private String getRedirectUrl(ReportCreateRequest request) {
        // 사용자 신고인 경우 닉네임으로 프로필 URL 생성
        if (request.getReportedEntityType() == ReportedEntityType.USER) {
            User reportedUser = userRepository.findById(request.getReportedEntityId())
                    .orElse(null);
            if (reportedUser != null) {
                // 한글 닉네임을 URL 안전하게 인코딩
                String encodedNickname = UriUtils.encodePath(reportedUser.getNickname(), StandardCharsets.UTF_8);
                return "/users/profile/" + encodedNickname;
            }
        }
        
        // 다른 타입은 기본 getEntityUrl() 사용
        String entityUrl = request.getReportedEntityType().getEntityUrl(request.getReportedEntityId());
        return (entityUrl != null) ? entityUrl : "/";
    }
}
