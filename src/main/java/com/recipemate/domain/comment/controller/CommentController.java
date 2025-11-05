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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
     * htmx 요청인 경우: 빈 응답 반환 (클라이언트에서 DOM 요소 제거)
     * 일반 폼 제출인 경우: 리다이렉트
     */
    @PostMapping("/{commentId}/delete")
    public String deleteComment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long commentId,
            @RequestParam EntityType targetType,
            @RequestParam Long targetId,
            @RequestHeader(value = "HX-Request", required = false) String htmxRequest,
            RedirectAttributes redirectAttributes) {
        
        try {
            // 1. 사용자 조회 및 댓글 삭제
            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
            
            commentService.deleteComment(user.getId(), commentId);
            
            // 2. htmx 요청인 경우 빈 fragment 반환 (클라이언트에서 hx-swap="outerHTML"로 요소 제거)
            if (htmxRequest != null && htmxRequest.equals("true")) {
                return "fragments/comments :: empty";
            }
            
            // 3. 일반 폼 제출인 경우 리다이렉트
            redirectAttributes.addFlashAttribute("successMessage", "댓글이 성공적으로 삭제되었습니다.");
        } catch (CustomException e) {
            // 4. 예외 발생 시 에러 메시지와 함께 리다이렉트
            redirectAttributes.addFlashAttribute("errorMessage", e.getErrorCode().getMessage());
        }
        
        String redirectUrl = determineRedirectUrl(targetType, targetId);
        return "redirect:" + redirectUrl;
    }

    // ========== htmx Fragment 엔드포인트 ==========

    /**
     * 댓글 목록 HTML Fragment 조회 (페이지네이션 지원)
     * GET /comments/fragments?targetType=POST&targetId=1&page=0&size=10
     * htmx에서 동적으로 댓글 목록을 로드하기 위해 사용
     */
    @GetMapping("/fragments")
    public String getCommentsFragment(
            @RequestParam EntityType targetType,
            @RequestParam Long targetId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable,
            Model model) {
        
        Page<CommentResponse> comments = commentService.getCommentsByTargetPageable(targetType, targetId, pageable);
        model.addAttribute("comments", comments);
        model.addAttribute("targetType", targetType);
        model.addAttribute("targetId", targetId);
        
        return "fragments/comments :: comment-list";
    }

    /**
     * 댓글 작성 - HTML Fragment 반환 (htmx용)
     * POST /comments/fragment
     */
    @PostMapping("/fragment")
    public String createCommentFragment(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("targetType") EntityType targetType,
            @RequestParam("targetId") Long targetId,
            @RequestParam("content") String content,
            @RequestParam("type") CommentType type,
            @RequestParam(value = "parentId", required = false) Long parentId,
            Model model) {
        
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
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        
        // 3. 사용자 조회 및 댓글 생성
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        CommentResponse comment = commentService.createComment(user.getId(), request);
        model.addAttribute("comment", comment);
        
        // 4. 단일 댓글 아이템 Fragment 반환
        return "fragments/comments :: comment-item";
    }

    /**
     * 댓글 수정 - HTML Fragment 반환 (htmx용)
     * POST /comments/{commentId}/fragment
     */
    @PostMapping("/{commentId}/fragment")
    public String updateCommentFragment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long commentId,
            @RequestParam("targetType") EntityType targetType,
            @RequestParam("targetId") Long targetId,
            @RequestParam("content") String content,
            Model model) {
        
        // 1. DTO 생성
        UpdateCommentRequest request = UpdateCommentRequest.builder()
                .content(content)
                .targetType(targetType)
                .targetId(targetId)
                .build();
        
        // 2. 수동 유효성 검증
        Set<ConstraintViolation<UpdateCommentRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        
        // 3. 사용자 조회 및 댓글 수정
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        CommentResponse comment = commentService.updateComment(user.getId(), commentId, request);
        model.addAttribute("comment", comment);
        
        // 4. 수정된 댓글 아이템 Fragment 반환 (htmx가 outerHTML로 교체)
        return "fragments/comments :: comment-item";
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
