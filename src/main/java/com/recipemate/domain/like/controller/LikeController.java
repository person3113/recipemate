package com.recipemate.domain.like.controller;

import com.recipemate.domain.like.dto.LikeResponseDto;
import com.recipemate.domain.like.service.LikeService;
import com.recipemate.domain.post.entity.Post;
import com.recipemate.domain.post.repository.PostRepository;
import com.recipemate.domain.user.entity.User;
import com.recipemate.domain.user.repository.UserRepository;
import com.recipemate.global.exception.CustomException;
import com.recipemate.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    @PostMapping("/post/{postId}")
    public String togglePostLike(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model
    ) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        LikeResponseDto response = likeService.togglePostLike(user.getId(), postId);
        
        // 헤더 통계 업데이트를 위해 viewCount도 함께 전달
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        model.addAttribute("isLiked", response.isLiked());
        model.addAttribute("likeCount", response.likeCount());
        model.addAttribute("targetType", "post");
        model.addAttribute("targetId", postId);
        model.addAttribute("viewCount", post.getViewCount());

        return "fragments/like-response :: like-response";
    }

    @PostMapping("/comment/{commentId}")
    public String toggleCommentLike(
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model
    ) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        LikeResponseDto response = likeService.toggleCommentLike(user.getId(), commentId);

        model.addAttribute("isLiked", response.isLiked());
        model.addAttribute("likeCount", response.likeCount());
        model.addAttribute("targetType", "comment");
        model.addAttribute("targetId", commentId);

        return "fragments/like :: like-button";
    }
}
