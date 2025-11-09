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
     * htmx 요청인 경우: 삭제된 상태의 댓글 fragment 반환 (소프트 삭제 UI)
     * 일반 폼 제출인 경우: 리다이렉트
     */
    @PostMapping("/{commentId}/delete")
    public String deleteComment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long commentId,
            @RequestParam EntityType targetType,
            @RequestParam Long targetId,
            @RequestHeader(value = "HX-Request", required = false) String htmxRequest,
            RedirectAttributes redirectAttributes,
            Model model) {
        
        try {
            // 1. 사용자 조회 및 댓글 삭제
            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
            
            commentService.deleteComment(user.getId(), commentId);
            
            // 2. htmx 요청인 경우 삭제된 상태의 댓글 fragment 반환
            if (htmxRequest != null && htmxRequest.equals("true")) {
                // 삭제된 댓글 데이터를 다시 조회하여 fragment에 전달
                CommentResponse deletedComment = commentService.getCommentById(commentId);
                model.addAttribute("comment", deletedComment);
                model.addAttribute("targetType", targetType);
                model.addAttribute("targetId", targetId);
                return "fragments/comments :: comment-item";
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
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam EntityType targetType,
            @RequestParam Long targetId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable,
            Model model) {
        
        // 현재 로그인한 사용자 ID 조회 (비로그인 시 null)
        Long currentUserId = null;
        if (userDetails != null) {
            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElse(null);
            if (user != null) {
                currentUserId = user.getId();
            }
        }
        
        Page<CommentResponse> comments = commentService.getCommentsByTargetPageable(targetType, targetId, currentUserId, pageable);
        long totalCommentCount = commentService.getTotalCommentCount(targetType, targetId);
        
        model.addAttribute("comments", comments);
        model.addAttribute("totalCommentCount", totalCommentCount);
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
        model.addAttribute("targetType", targetType);
        model.addAttribute("targetId", targetId);
        
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
        
        // 좋아요 정보 추가를 위해 getCommentById 재호출 (비효율적이지만 일관성 유지)
        comment = commentService.getCommentByIdWithLikes(commentId, user.getId());
        
        model.addAttribute("comment", comment);
        model.addAttribute("targetType", targetType);
        model.addAttribute("targetId", targetId);
        
        // 4. 수정된 댓글 아이템 Fragment 반환 (htmx가 outerHTML로 교체)
        return "fragments/comments :: comment-item";
    }

    // ========== 대댓글(Reply) 엔드포인트 ==========

    /**
     * 대댓글 작성 - HTML Fragment 반환 (htmx용)
     * POST /comments/{commentId}/replies
     */
    @PostMapping("/{commentId}/replies")
    public String createReply(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long commentId,
            @RequestParam("targetType") EntityType targetType,
            @RequestParam("targetId") Long targetId,
            @RequestParam("content") String content,
            @RequestParam("type") CommentType type,
            Model model) {
        
        // 1. DTO 생성 (parentId를 commentId로 설정)
        CreateCommentRequest request = CreateCommentRequest.builder()
                .targetType(targetType)
                .targetId(targetId)
                .content(content)
                .type(type)
                .parentId(commentId)
                .build();
        
        // 2. 수동 유효성 검증
        Set<ConstraintViolation<CreateCommentRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        
        // 3. 사용자 조회 및 대댓글 생성
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        CommentResponse reply = commentService.createComment(user.getId(), request);
        model.addAttribute("reply", reply);
        model.addAttribute("targetType", targetType);
        model.addAttribute("targetId", targetId);
        
        // 4. 대댓글 아이템 Fragment 반환
        return "fragments/comments :: reply-item";
    }

    /**
     * 대댓글 수정 - HTML Fragment 반환 (htmx용)
     * PUT /comments/replies/{replyId}
     */
    @PutMapping("/replies/{replyId}")
    public String updateReply(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long replyId,
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
        
        // 3. 사용자 조회 및 대댓글 수정
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        CommentResponse reply = commentService.updateComment(user.getId(), replyId, request);
        model.addAttribute("reply", reply);
        model.addAttribute("targetType", targetType);
        model.addAttribute("targetId", targetId);
        
        // 4. 수정된 대댓글 아이템 Fragment 반환
        return "fragments/comments :: reply-item";
    }

    /**
     * 대댓글 삭제
     * POST /comments/replies/{replyId}/delete
     */
    @PostMapping("/replies/{replyId}/delete")
    public String deleteReply(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long replyId,
            @RequestParam EntityType targetType,
            @RequestParam Long targetId,
            @RequestHeader(value = "HX-Request", required = false) String htmxRequest,
            Model model) {
        
        // 1. 사용자 조회 및 대댓글 삭제
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        commentService.deleteComment(user.getId(), replyId);
        
        // 2. 삭제된 상태의 대댓글 fragment 반환
        CommentResponse deletedReply = commentService.getCommentById(replyId);
        model.addAttribute("reply", deletedReply);
        model.addAttribute("targetType", targetType);
        model.addAttribute("targetId", targetId);
        return "fragments/comments :: reply-item";
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
