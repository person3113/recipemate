package com.recipemate.domain.comment.controller;

import com.recipemate.domain.comment.dto.CommentResponse;
import com.recipemate.domain.comment.dto.CreateCommentRequest;
import com.recipemate.domain.comment.dto.UpdateCommentRequest;
import com.recipemate.domain.comment.service.CommentService;
import com.recipemate.domain.user.entity.User;
import com.recipemate.domain.user.repository.UserRepository;
import com.recipemate.global.common.CommentType;
import com.recipemate.global.common.EntityType;
import com.recipemate.global.exception.CustomException;
import com.recipemate.global.exception.ErrorCode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Set;

/**
 * 댓글 웹 컨트롤러 (Thymeleaf + htmx 기반)
 */
@Controller
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final UserRepository userRepository;
    private final Validator validator;

    // ========== 폼 제출 엔드포인트 ==========

    /**
     * 댓글 작성 폼 제출
     * POST /comments
     */
    @PostMapping
    public String createComment(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("targetType") EntityType targetType,
            @RequestParam("targetId") Long targetId,
            @RequestParam("content") String content,
            @RequestParam("type") CommentType type,
            @RequestParam(value = "parentId", required = false) Long parentId,
            RedirectAttributes redirectAttributes) {
        
        // 리다이렉트 URL 결정
        String redirectUrl = determineRedirectUrl(targetType, targetId);
        
        // 1. DTO 생성
        CreateCommentRequest request = CreateCommentRequest.builder()
                .targetType(targetType)
                .targetId(targetId)
                .content(content)
                .type(type)
                .parentId(parentId)
                .build();
        
        // 2. 수동 유효성 검증
        Set<ConstraintViolation<CreateCommentRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    violations.iterator().next().getMessage());
            return "redirect:" + redirectUrl;
        }
        
        // 3. 사용자 조회 및 댓글 생성
        try {
            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
            
            commentService.createComment(user.getId(), request);
            redirectAttributes.addFlashAttribute("successMessage", "댓글이 성공적으로 작성되었습니다.");
            
        } catch (CustomException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getErrorCode().getMessage());
        }
        
        return "redirect:" + redirectUrl;
    }

    /**
     * 댓글 수정 폼 제출
     * POST /comments/{commentId}/edit
     */
    @PostMapping("/{commentId}/edit")
    public String updateComment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long commentId,
            @RequestParam("targetType") EntityType targetType,
            @RequestParam("targetId") Long targetId,
            @RequestParam("content") String content,
            RedirectAttributes redirectAttributes) {
        
        // 리다이렉트 URL 결정
        String redirectUrl = determineRedirectUrl(targetType, targetId);
        
        // 1. DTO 생성
        UpdateCommentRequest request = UpdateCommentRequest.builder()
                .content(content)
                .targetType(targetType)
                .targetId(targetId)
                .build();
        
        // 2. 수동 유효성 검증
        Set<ConstraintViolation<UpdateCommentRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    violations.iterator().next().getMessage());
            return "redirect:" + redirectUrl;
        }
        
        // 3. 사용자 조회 및 댓글 수정
        try {
            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
            
            commentService.updateComment(user.getId(), commentId, request);
            redirectAttributes.addFlashAttribute("successMessage", "댓글이 성공적으로 수정되었습니다.");
            
        } catch (CustomException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getErrorCode().getMessage());
        }
        
        return "redirect:" + redirectUrl;
    }

    /**
     * 댓글 삭제 폼 제출
     * POST /comments/{commentId}/delete
     */
    @PostMapping("/{commentId}/delete")
    public String deleteComment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long commentId,
            @RequestParam EntityType targetType,
            @RequestParam Long targetId,
            RedirectAttributes redirectAttributes) {
        
        // 리다이렉트 URL 결정
        String redirectUrl = determineRedirectUrl(targetType, targetId);
        
        // 1. 사용자 조회 및 댓글 삭제
        try {
            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
            
            commentService.deleteComment(user.getId(), commentId);
            redirectAttributes.addFlashAttribute("successMessage", "댓글이 성공적으로 삭제되었습니다.");
            
        } catch (CustomException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getErrorCode().getMessage());
        }
        
        return "redirect:" + redirectUrl;
    }

    // ========== htmx Fragment 엔드포인트 ==========

    /**
     * 댓글 목록 HTML Fragment 조회
     * GET /comments/fragments?targetType=POST&targetId=1
     * htmx에서 동적으로 댓글 목록을 로드하기 위해 사용
     */
    @GetMapping("/fragments")
    public String getCommentsFragment(
            @RequestParam EntityType targetType,
            @RequestParam Long targetId,
            Model model) {
        
        List<CommentResponse> comments = commentService.getCommentsByTarget(targetType, targetId);
        model.addAttribute("comments", comments);
        
        return "fragments/comments :: comment-list";
    }

    // ========== Helper Methods ==========

    /**
     * targetType과 targetId를 기반으로 리다이렉트 URL 결정
     */
    private String determineRedirectUrl(EntityType targetType, Long targetId) {
        // null 체크 추가 (validation 실패 시 null일 수 있음)
        if (targetType == null || targetId == null) {
            return "/";
        }
        
        if (targetType == EntityType.GROUP_BUY) {
            return "/group-purchases/" + targetId;
        } else if (targetType == EntityType.POST) {
            return "/community-posts/" + targetId;
        }
        // 기본값
        return "/";
    }
}
