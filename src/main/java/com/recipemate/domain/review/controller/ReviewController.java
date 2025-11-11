package com.recipemate.domain.review.controller;

import com.recipemate.domain.groupbuy.dto.GroupBuyResponse;
import com.recipemate.domain.groupbuy.repository.ParticipationRepository;
import com.recipemate.domain.groupbuy.service.GroupBuyService;
import com.recipemate.domain.review.dto.CreateReviewRequest;
import com.recipemate.domain.review.dto.ReviewResponse;
import com.recipemate.domain.review.dto.UpdateReviewRequest;
import com.recipemate.domain.review.entity.Review;
import com.recipemate.domain.review.repository.ReviewRepository;
import com.recipemate.domain.review.service.ReviewService;
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

@Controller
@RequestMapping("/group-purchases/{purchaseId}/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final UserRepository userRepository;
    private final GroupBuyService groupBuyService;
    private final ParticipationRepository participationRepository;
    private final ReviewRepository reviewRepository;

    /**
     * 후기 작성 폼 렌더링
     * GET /group-purchases/{purchaseId}/reviews/new
     */
    @GetMapping("/new")
    public String showCreateReviewForm(
            @PathVariable Long purchaseId,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model
    ) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 1. 공구 정보 조회
        GroupBuyResponse groupBuy = groupBuyService.getGroupBuyDetail(purchaseId);
        
        // 2. 참여자 권한 확인
        if (!participationRepository.existsByUserIdAndGroupBuyId(user.getId(), purchaseId)) {
            throw new CustomException(ErrorCode.NOT_PARTICIPATED);
        }
        
        // 3. 중복 후기 확인
        if (reviewRepository.existsByReviewerIdAndGroupBuyId(user.getId(), purchaseId)) {
            throw new CustomException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }
        
        // 4. 빈 폼 데이터 객체 추가
        model.addAttribute("formData", CreateReviewRequest.builder().build());
        model.addAttribute("groupBuy", groupBuy);
        model.addAttribute("groupBuyId", purchaseId);
        model.addAttribute("isEditMode", false);
        return "reviews/form";
    }

    /**
     * 후기 수정 폼 렌더링
     * GET /group-purchases/{purchaseId}/reviews/{reviewId}/edit
     */
    @GetMapping("/{reviewId}/edit")
    public String showEditReviewForm(
            @PathVariable Long purchaseId,
            @PathVariable Long reviewId,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model
    ) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 1. 공구 정보 조회
        GroupBuyResponse groupBuy = groupBuyService.getGroupBuyDetail(purchaseId);
        
        // 2. 후기 조회
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));
        
        // 3. 작성자 권한 확인
        if (!review.isReviewedBy(user)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_REVIEW_ACCESS);
        }
        
        // 4. 폼 데이터 객체 생성 (기존 데이터로 채움)
        UpdateReviewRequest formData = UpdateReviewRequest.builder()
                .rating(review.getRating())
                .content(review.getContent())
                .build();
        
        model.addAttribute("formData", formData);
        model.addAttribute("groupBuy", groupBuy);
        model.addAttribute("groupBuyId", purchaseId);
        model.addAttribute("review", ReviewResponse.from(review));
        model.addAttribute("isEditMode", true);
        return "reviews/form";
    }

    /**
     * 후기 작성 (폼 처리)
     * POST /group-purchases/{purchaseId}/reviews
     */
    @PostMapping
    public String createReview(
            @PathVariable Long purchaseId,
            @Valid @ModelAttribute CreateReviewRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        reviewService.createReview(user.getId(), purchaseId, request);
        
        redirectAttributes.addFlashAttribute("message", "후기가 작성되었습니다.");
        return "redirect:/group-purchases/" + purchaseId;
    }

    /**
     * 후기 수정 (폼 처리)
     * POST /group-purchases/{purchaseId}/reviews/{reviewId}/edit
     */
    @PostMapping("/{reviewId}/edit")
    public String updateReview(
            @PathVariable Long purchaseId,
            @PathVariable Long reviewId,
            @Valid @ModelAttribute UpdateReviewRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        reviewService.updateReview(user.getId(), reviewId, request);
        
        redirectAttributes.addFlashAttribute("message", "후기가 수정되었습니다.");
        return "redirect:/group-purchases/" + purchaseId;
    }

    /**
     * 후기 삭제 (폼 처리)
     * POST /group-purchases/{purchaseId}/reviews/{reviewId}/delete
     */
    @PostMapping("/{reviewId}/delete")
    public String deleteReview(
            @PathVariable Long purchaseId,
            @PathVariable Long reviewId,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        reviewService.deleteReview(user.getId(), reviewId);
        
        redirectAttributes.addFlashAttribute("message", "후기가 삭제되었습니다.");
        return "redirect:/group-purchases/" + purchaseId;
    }

    /**
     * 후기 목록 Fragment (htmx 부분 갱신용)
     * GET /group-purchases/{purchaseId}/fragments/reviews
     */
    @GetMapping("/../fragments/reviews")
    public String getReviewsFragment(@PathVariable Long purchaseId, Model model) {
        List<ReviewResponse> reviews = reviewService.getReviewsByGroupBuy(purchaseId);
        model.addAttribute("reviews", reviews);
        return "fragments/reviews :: reviewList";
    }
}
