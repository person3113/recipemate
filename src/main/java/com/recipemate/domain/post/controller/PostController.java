package com.recipemate.domain.post.controller;

import com.recipemate.domain.post.dto.CreatePostRequest;
import com.recipemate.domain.post.dto.PostResponse;
import com.recipemate.domain.post.dto.UpdatePostRequest;
import com.recipemate.domain.post.service.PostService;
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
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 커뮤니티 게시글 웹 컨트롤러 (Thymeleaf 기반)
 */
@Controller
@RequestMapping("/community-posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final UserRepository userRepository;

    // ========== 페이지 렌더링 엔드포인트 ==========
    
    /**
     * 게시글 목록 페이지 렌더링
     */
    @GetMapping("/list")
    public String listPage(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            Model model
    ) {
        // TODO: 목록 조회 기능 구현 예정
        return "community-posts/list";
    }
    
    /**
     * 게시글 상세 페이지 렌더링
     */
    @GetMapping("/{postId}")
    public String detailPage(@PathVariable Long postId, Model model) {
        PostResponse post = postService.getPostDetail(postId);
        model.addAttribute("post", post);
        return "community-posts/detail";
    }
    
    /**
     * 게시글 작성 페이지 렌더링
     */
    @GetMapping("/new")
    public String createPage() {
        return "community-posts/form";
    }
    
    /**
     * 게시글 수정 페이지 렌더링
     */
    @GetMapping("/{postId}/edit")
    public String editPage(@PathVariable Long postId, Model model) {
        PostResponse post = postService.getPostDetail(postId);
        model.addAttribute("post", post);
        return "community-posts/form";
    }

    // ========== 폼 처리 엔드포인트 ==========

    /**
     * 게시글 생성 폼 제출
     */
    @PostMapping
    public String createPost(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @ModelAttribute CreatePostRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        // 1. 유효성 검증 실패 처리
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/community-posts/new";
        }
        
        User user = userRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        PostResponse response = postService.createPost(user.getId(), request);
        redirectAttributes.addFlashAttribute("successMessage", "게시글이 성공적으로 작성되었습니다.");
        return "redirect:/community-posts/" + response.getId();
    }

    /**
     * 게시글 수정 폼 제출
     */
    @PostMapping("/{postId}")
    public String updatePost(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long postId,
            @Valid @ModelAttribute UpdatePostRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        // 1. 유효성 검증 실패 처리
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/community-posts/" + postId + "/edit";
        }
        
        User user = userRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        postService.updatePost(user.getId(), postId, request);
        redirectAttributes.addFlashAttribute("successMessage", "게시글이 성공적으로 수정되었습니다.");
        return "redirect:/community-posts/" + postId;
    }

    /**
     * 게시글 삭제 폼 제출
     */
    @PostMapping("/{postId}/delete")
    public String deletePost(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long postId,
            RedirectAttributes redirectAttributes
    ) {
        User user = userRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        postService.deletePost(user.getId(), postId);
        redirectAttributes.addFlashAttribute("successMessage", "게시글이 성공적으로 삭제되었습니다.");
        return "redirect:/community-posts/list";
    }
    
    // ========== htmx용 HTML Fragment 엔드포인트 (향후 추가) ==========
    // TODO: htmx 통합 시 아래 엔드포인트 구현
    // @GetMapping("/search-fragment") - 검색 결과 HTML 조각 (리스트 아이템들)
}
