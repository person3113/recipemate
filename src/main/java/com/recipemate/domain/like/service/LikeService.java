package com.recipemate.domain.like.service;

import com.recipemate.domain.comment.entity.Comment;
import com.recipemate.domain.comment.repository.CommentRepository;
import com.recipemate.domain.like.dto.LikeResponseDto;
import com.recipemate.domain.like.entity.CommentLike;
import com.recipemate.domain.like.entity.PostLike;
import com.recipemate.domain.like.repository.CommentLikeRepository;
import com.recipemate.domain.like.repository.PostLikeRepository;
import com.recipemate.domain.post.entity.Post;
import com.recipemate.domain.post.repository.PostRepository;
import com.recipemate.domain.user.entity.User;
import com.recipemate.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LikeService {

    private final PostLikeRepository postLikeRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    @Transactional
    public LikeResponseDto togglePostLike(Long userId, Long postId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다: " + postId));

        boolean isLiked = postLikeRepository.findByUserAndPost(user, post)
                .map(postLike -> {
                    postLikeRepository.delete(postLike);
                    return false;
                })
                .orElseGet(() -> {
                    postLikeRepository.save(new PostLike(user, post));
                    return true;
                });

        long likeCount = postLikeRepository.countByPost(post);
        return new LikeResponseDto(isLiked, likeCount);
    }

    @Transactional
    public LikeResponseDto toggleCommentLike(Long userId, Long commentId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다: " + commentId));

        boolean isLiked = commentLikeRepository.findByUserAndComment(user, comment)
                .map(commentLike -> {
                    commentLikeRepository.delete(commentLike);
                    return false;
                })
                .orElseGet(() -> {
                    commentLikeRepository.save(new CommentLike(user, comment));
                    return true;
                });

        long likeCount = commentLikeRepository.countByComment(comment);
        return new LikeResponseDto(isLiked, likeCount);
    }
}
