package com.recipemate.domain.post.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import lombok.extern.slf4j.Slf4j;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 커뮤니티 게시글 웹 컨트롤러 (Thymeleaf 기반)
 */
@Controller
@RequestMapping("/community-posts")
@RequiredArgsConstructor
@Slf4j
public class PostController {

    private final PostService postService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

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
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "desc") String dir,
            Model model
    ) {
        // 정렬 정보 생성
        Sort.Direction direction = "asc".equalsIgnoreCase(dir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortProperty;
        switch (sort) {
            case "views":
                sortProperty = "viewCount";
                break;
            case "likes":
                sortProperty = "likeCount";
                break;
            case "comments":
                sortProperty = "commentCount";
                break;
            default:
                sortProperty = "createdAt";
                break;
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortProperty));
        
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
        model.addAttribute("currentSort", sort);
        model.addAttribute("currentDir", dir);
        
        return "community-posts/list";
    }
    
    /**
     * 게시글 상세 페이지 렌더링
     * HttpSession을 이용하여 중복 조회수 증가 방지
     */
    @GetMapping("/{postId}")
    public String detailPage(
            @PathVariable Long postId, 
            @AuthenticationPrincipal UserDetails userDetails,
            HttpSession session,
            jakarta.servlet.http.HttpServletRequest request,
            Model model
    ) {
        // 세션에서 조회한 게시글 ID 목록 가져오기
        @SuppressWarnings("unchecked")
        Set<Long> viewedPostIds = (Set<Long>) session.getAttribute("viewedPostIds");
        if (viewedPostIds == null) {
            viewedPostIds = new HashSet<>();
        }

        // 현재 게시글을 처음 조회하는 경우, 조회수 증가 처리
        if (!viewedPostIds.contains(postId)) {
            postService.increasePostViewCount(postId);
            viewedPostIds.add(postId);
            session.setAttribute("viewedPostIds", viewedPostIds);
        }

        // 게시글 데이터 조회
        PostResponse post;
        if (userDetails != null) {
            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
            post = postService.getPostDetail(postId, user.getId());
        } else {
            post = postService.getPostDetail(postId);
        }
        
        // 현재 URL을 모델에 추가 (리다이렉션에 사용)
        String currentUrl = request.getRequestURI();
        if (request.getQueryString() != null) {
            currentUrl += "?" + request.getQueryString();
        }
        
        model.addAttribute("post", post);
        model.addAttribute("currentUrl", currentUrl);
        return "community-posts/detail";
    }
    
    /**
     * 게시글 작성 페이지 렌더링
     */
    @GetMapping("/new")
    public String createPage(Model model) {
        // Flash 속성으로 전달된 폼 데이터가 있는지 확인 (PRG 패턴)
        if (!model.containsAttribute("formData")) {
            // 빈 폼 객체 추가 (Thymeleaf th:object를 위해 필수)
            model.addAttribute("formData", new CreatePostRequest());
        }
        // PostCategory enum 값들을 모델에 추가
        model.addAttribute("categories", PostCategory.values());
        return "community-posts/form";
    }
    
    /**
     * 게시글 수정 페이지 렌더링
     */
    @GetMapping("/{postId}/edit")
    public String editPage(
            @PathVariable Long postId,
            @RequestParam(required = false) String redirectUrl,
            Model model) {
        PostResponse post = postService.getPostDetail(postId);
        model.addAttribute("post", post);
        
        // Flash 속성으로 전달된 폼 데이터가 있는지 확인 (PRG 패턴)
        if (!model.containsAttribute("formData")) {
            // 기존 데이터로 폼 객체 초기화 (Thymeleaf th:field가 값을 채우기 위해 필요)
            UpdatePostRequest formData = new UpdatePostRequest();
            formData.setTitle(post.getTitle());
            formData.setContent(post.getContent());
            formData.setCategory(post.getCategory());
            model.addAttribute("formData", formData);
        }
        
        // redirectUrl 전달 (수정 후 돌아갈 페이지)
        if (redirectUrl != null && !redirectUrl.isBlank()) {
            model.addAttribute("redirectUrl", redirectUrl);
        }
        
        // PostCategory enum 값들을 모델에 추가
        model.addAttribute("categories", PostCategory.values());
        return "community-posts/form";
    }

    // ========== 폼 처리 엔드포인트 ==========

    /**
     * 게시글 생성 폼 제출
     * PRG (Post-Redirect-Get) 패턴 적용: 유효성 검증 실패 시에도 리다이렉트
     */
    @PostMapping
    public String createPost(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @ModelAttribute CreatePostRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        // 1. 유효성 검증 실패 처리 (PRG 패턴 적용)
        if (bindingResult.hasErrors()) {
            // Flash 속성에 폼 데이터와 검증 오류 저장
            redirectAttributes.addFlashAttribute("formData", request);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.formData", bindingResult);
            redirectAttributes.addFlashAttribute("errorMessage", 
                bindingResult.getAllErrors().get(0).getDefaultMessage());
            // 게시글 작성 페이지로 리다이렉트 (새로고침 시 폼 재제출 방지)
            return "redirect:/community-posts/new";
        }
        
        try {
            User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
            
            PostResponse response = postService.createPost(user.getId(), request);
            redirectAttributes.addFlashAttribute("successMessage", "게시글이 성공적으로 작성되었습니다.");
            return "redirect:/community-posts/" + response.getId();
        } catch (IllegalArgumentException e) {
            // 이미지 업로드 오류 처리 (PRG 패턴 적용)
            redirectAttributes.addFlashAttribute("formData", request);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/community-posts/new";
        }
    }

    /**
     * 게시글 수정 폼 제출
     * PRG (Post-Redirect-Get) 패턴 적용: 유효성 검증 실패 시에도 리다이렉트
     * redirectUrl 파라미터 지원: 수정 완료 후 원래 페이지로 이동 가능
     */
    @PostMapping("/{postId}")
    public String updatePost(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long postId,
            @RequestParam(required = false) String redirectUrl,
            @RequestParam(required = false) String deletedImages,
            @Valid @ModelAttribute UpdatePostRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        // 1. 유효성 검증 실패 처리 (PRG 패턴 적용)
        if (bindingResult.hasErrors()) {
            // Flash 속성에 폼 데이터와 검증 오류 저장
            redirectAttributes.addFlashAttribute("formData", request);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.formData", bindingResult);
            redirectAttributes.addFlashAttribute("errorMessage", 
                bindingResult.getAllErrors().get(0).getDefaultMessage());
            // redirectUrl이 있으면 함께 전달
            if (redirectUrl != null && !redirectUrl.isBlank()) {
                redirectAttributes.addAttribute("redirectUrl", redirectUrl);
            }
            // 게시글 수정 페이지로 리다이렉트 (새로고침 시 폼 재제출 방지)
            return "redirect:/community-posts/" + postId + "/edit";
        }
        
        // 2. deletedImages JSON 파싱
        List<String> deletedImagesList = new ArrayList<>();
        if (deletedImages != null && !deletedImages.isBlank()) {
            try {
                deletedImagesList = objectMapper.readValue(
                    deletedImages,
                    new TypeReference<List<String>>() {}
                );
                log.info("Parsed {} deleted images for post update", deletedImagesList.size());
            } catch (JsonProcessingException e) {
                log.warn("Failed to parse deletedImages JSON: {}", e.getMessage());
                // deletedImages 파싱 실패는 무시하고 계속 진행
            }
        }
        
        // 3. DTO에 삭제할 이미지 목록 설정
        request.setDeletedImages(deletedImagesList);
        
        try {
            User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
            
            postService.updatePost(user.getId(), postId, request);
            redirectAttributes.addFlashAttribute("successMessage", "게시글이 성공적으로 수정되었습니다.");
            
            // redirectUrl이 있으면 해당 페이지로 이동, 없으면 상세 페이지로 이동
            if (redirectUrl != null && !redirectUrl.isBlank()) {
                return "redirect:" + redirectUrl;
            }
            return "redirect:/community-posts/" + postId;
        } catch (IllegalArgumentException e) {
            // 이미지 업로드 오류 처리 (PRG 패턴 적용)
            redirectAttributes.addFlashAttribute("formData", request);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            if (redirectUrl != null && !redirectUrl.isBlank()) {
                redirectAttributes.addAttribute("redirectUrl", redirectUrl);
            }
            return "redirect:/community-posts/" + postId + "/edit";
        }
    }

    /**
     * 게시글 삭제 폼 제출
     * redirectUrl 파라미터 지원: 삭제 완료 후 원래 페이지로 이동 가능
     */
    @PostMapping("/{postId}/delete")
    public String deletePost(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long postId,
            @RequestParam(required = false) String redirectUrl,
            RedirectAttributes redirectAttributes
    ) {
        User user = userRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        postService.deletePost(user.getId(), postId);
        redirectAttributes.addFlashAttribute("successMessage", "게시글이 성공적으로 삭제되었습니다.");
        
        // redirectUrl이 있으면 해당 페이지로 이동, 없으면 목록 페이지로 이동
        if (redirectUrl != null && !redirectUrl.isBlank()) {
            return "redirect:" + redirectUrl;
        }
        return "redirect:/community-posts/list";
    }
    
    // ========== htmx용 HTML Fragment 엔드포인트 (향후 추가) ==========
    // TODO: htmx 통합 시 아래 엔드포인트 구현
    // @GetMapping("/search-fragment") - 검색 결과 HTML 조각 (리스트 아이템들)
}
