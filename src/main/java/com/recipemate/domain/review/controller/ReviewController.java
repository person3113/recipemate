package com.recipemate.domain.review.controller;

import com.recipemate.domain.review.dto.CreateReviewRequest;
import com.recipemate.domain.review.dto.ReviewResponse;
import com.recipemate.domain.review.dto.UpdateReviewRequest;
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
