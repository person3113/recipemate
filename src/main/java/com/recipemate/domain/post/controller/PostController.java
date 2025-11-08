package com.recipemate.domain.post.controller;

import com.recipemate.domain.post.dto.CreatePostRequest;
import com.recipemate.domain.post.dto.PostResponse;
import com.recipemate.domain.post.dto.UpdatePostRequest;
import com.recipemate.domain.post.service.PostService;
import com.recipemate.domain.user.entity.User;
import com.recipemate.domain.user.repository.UserRepository;
import com.recipemate.global.common.PostCategory;
import com.recipemate.global.exception.CustomException;
import com.recipemate.global.exception.ErrorCode;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        // 페이징 정보 생성 (최신순 정렬)
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        // 카테고리 파라미터 변환
        PostCategory postCategory = null;
        if (category != null && !category.isEmpty()) {
            try {
                postCategory = PostCategory.valueOf(category.toUpperCase());
            } catch (IllegalArgumentException e) {
                // 잘못된 카테고리인 경우 무시
            }
        }
        
        // 게시글 목록 조회
        Page<PostResponse> posts = postService.getPostList(postCategory, keyword, pageable);
        
        // 모델에 데이터 추가
        model.addAttribute("posts", posts);
        model.addAttribute("currentCategory", category);
        model.addAttribute("keyword", keyword);
        
        return "community-posts/list";
    }
    
    /**
     * 게시글 상세 페이지 렌더링
     * CQS 원칙 적용: 조회와 조회수 증가를 명시적으로 분리
     */
    @GetMapping("/{postId}")
    public String detailPage(@PathVariable Long postId, HttpSession session, Model model) {
        // 1. 게시글 조회 (순수 조회, 상태 변경 없음)
        PostResponse post = postService.getPostDetail(postId);

        // 2. 조회수 증가 (세션 기반 중복 방지)
        postService.increaseViewCount(postId, session);

        // 3. 조회수가 증가된 최신 데이터 다시 조회
        post = postService.getPostDetail(postId);

        model.addAttribute("post", post);
        return "community-posts/detail";
    }
    
    /**
     * 게시글 작성 페이지 렌더링
     */
    @GetMapping("/new")
    public String createPage(Model model) {
        // 빈 폼 객체 추가 (Thymeleaf th:object를 위해 필수)
        model.addAttribute("formData", new CreatePostRequest());
        return "community-posts/form";
    }
    
    /**
     * 게시글 수정 페이지 렌더링
     */
    @GetMapping("/{postId}/edit")
    public String editPage(@PathVariable Long postId, Model model) {
        PostResponse post = postService.getPostDetail(postId);
        model.addAttribute("post", post);
        
        // 기존 데이터로 폼 객체 초기화 (Thymeleaf th:field가 값을 채우기 위해 필요)
        UpdatePostRequest formData = new UpdatePostRequest();
        formData.setTitle(post.getTitle());
        formData.setContent(post.getContent());
        formData.setCategory(post.getCategory());

        model.addAttribute("formData", formData);
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
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        // 1. 유효성 검증 실패 처리
        if (bindingResult.hasErrors()) {
            model.addAttribute("formData", request);
            model.addAttribute("errorMessage", 
                bindingResult.getAllErrors().get(0).getDefaultMessage());
            // 입력된 데이터를 유지하면서 폼 페이지로 직접 반환
            return "community-posts/form";
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
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        // 1. 유효성 검증 실패 처리
        if (bindingResult.hasErrors()) {
            model.addAttribute("formData", request);
            model.addAttribute("errorMessage", 
                bindingResult.getAllErrors().get(0).getDefaultMessage());
            // 기존 게시글 정보 조회해서 모델에 추가
            PostResponse post = postService.getPostDetail(postId);
            model.addAttribute("post", post);
            // 입력된 데이터를 유지하면서 폼 페이지로 직접 반환
            return "community-posts/form";
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
