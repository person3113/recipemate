package com.recipemate.domain.review.controller;

import com.recipemate.domain.review.dto.ReviewResponse;
import com.recipemate.domain.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * 전체 후기 목록 및 프래그먼트를 제공하는 컨트롤러
 */
@Controller
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewListController {

    private final ReviewService reviewService;

    /**
     * 전체 후기 목록 페이지
     * GET /reviews
     */
    @GetMapping
    public String getReviewListPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ReviewResponse> reviews = reviewService.getAllReviewsPaged(pageable);

        model.addAttribute("reviews", reviews);
        model.addAttribute("currentPage", page);

        return "reviews/list";
    }

    /**
     * 후기 목록 Fragment (페이징 지원, htmx 부분 갱신용)
     * GET /reviews/fragments?groupBuyId={id}&page={page}
     * 
     * 이 엔드포인트는 두 가지 용도로 사용됩니다:
     * 1. groupBuyId가 있을 때: 특정 공구의 후기 목록 (공구 상세 페이지의 후기 탭에서 사용)
     * 2. groupBuyId가 없을 때: 전체 후기 목록 (커뮤니티 페이지의 후기 탭에서 사용)
     */
    @GetMapping("/fragments")
    public String getReviewsFragment(
            @RequestParam(required = false) Long groupBuyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ReviewResponse> reviews;
        Double averageRating;
        Map<Integer, Long> ratingDistribution;

        if (groupBuyId != null) {
            // 특정 공구의 후기 목록
            reviews = reviewService.getReviewsByGroupBuyPaged(groupBuyId, pageable);
            averageRating = reviewService.getAverageRating(groupBuyId);
            ratingDistribution = reviewService.getRatingDistribution(groupBuyId);
        } else {
            // 전체 후기 목록
            reviews = reviewService.getAllReviewsPaged(pageable);
            averageRating = 0.0; // 전체 평균은 계산하지 않음
            ratingDistribution = Map.of(); // 전체 분포는 계산하지 않음
        }

        model.addAttribute("reviews", reviews);
        model.addAttribute("averageRating", averageRating);
        model.addAttribute("ratingDistribution", ratingDistribution);
        model.addAttribute("groupBuyId", groupBuyId);

        return "fragments/reviews :: review-list";
    }
}
