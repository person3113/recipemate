package com.recipemate.domain.comment.service;

import com.recipemate.domain.comment.dto.CommentResponse;
import com.recipemate.domain.comment.dto.CreateCommentRequest;
import com.recipemate.domain.comment.dto.UpdateCommentRequest;
import com.recipemate.domain.comment.entity.Comment;
import com.recipemate.domain.comment.repository.CommentRepository;
import com.recipemate.domain.groupbuy.entity.GroupBuy;
import com.recipemate.domain.groupbuy.repository.GroupBuyRepository;
import com.recipemate.domain.post.entity.Post;
import com.recipemate.domain.post.repository.PostRepository;
import com.recipemate.domain.user.entity.User;
import com.recipemate.domain.user.repository.UserRepository;
import com.recipemate.global.common.EntityType;
import com.recipemate.global.exception.CustomException;
import com.recipemate.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final GroupBuyRepository groupBuyRepository;
    private final PostRepository postRepository;

    /**
     * 댓글 작성
     */
    @Transactional
    public CommentResponse createComment(Long userId, CreateCommentRequest request) {
        // 1. 사용자 조회
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2. 대상 엔티티 조회
        GroupBuy groupBuy = null;
        Post post = null;

        if (request.getTargetType() == EntityType.GROUP_BUY) {
            groupBuy = groupBuyRepository.findById(request.getTargetId())
                    .orElseThrow(() -> new CustomException(ErrorCode.GROUP_BUY_NOT_FOUND));
        } else if (request.getTargetType() == EntityType.POST) {
            post = postRepository.findById(request.getTargetId())
                    .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
        }

        // 3. 부모 댓글 조회 (대댓글인 경우)
        Comment parent = null;
        if (request.getParentId() != null) {
            parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

            // 대댓글의 대댓글 방지 (깊이 제한)
            if (parent.getParent() != null) {
                throw new CustomException(ErrorCode.COMMENT_DEPTH_EXCEEDED);
            }
        }

        // 4. 댓글 생성 및 저장
        Comment comment = Comment.builder()
                .author(author)
                .groupBuy(groupBuy)
                .post(post)
                .parent(parent)
                .content(request.getContent())
                .type(request.getType())
                .build();

        // 5. 유효성 검증
        comment.validateTarget();

        Comment savedComment = commentRepository.save(comment);
        return CommentResponse.from(savedComment);
    }

    /**
     * 댓글 수정
     */
    @Transactional
    public CommentResponse updateComment(Long userId, Long commentId, UpdateCommentRequest request) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        // 삭제된 댓글 체크
        if (comment.getDeletedAt() != null) {
            throw new CustomException(ErrorCode.COMMENT_NOT_FOUND);
        }

        // 작성자 권한 체크
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_COMMENT_UPDATE);
        }

        comment.updateContent(request.getContent());
        return CommentResponse.from(comment);
    }

    /**
     * 댓글 삭제 (소프트 삭제)
     */
    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        // 삭제된 댓글 체크
        if (comment.getDeletedAt() != null) {
            throw new CustomException(ErrorCode.COMMENT_NOT_FOUND);
        }

        // 작성자 권한 체크
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_COMMENT_DELETE);
        }

        comment.markAsDeleted();
    }

    /**
     * 특정 대상의 댓글 목록 조회 (htmx fragment용)
     */
    public List<CommentResponse> getCommentsByTarget(EntityType targetType, Long targetId) {
        List<Comment> comments;

        if (targetType == EntityType.GROUP_BUY) {
            comments = commentRepository.findByGroupBuyIdAndParentIsNullOrderByCreatedAtAsc(targetId);
        } else if (targetType == EntityType.POST) {
            comments = commentRepository.findByPostIdAndParentIsNullOrderByCreatedAtAsc(targetId);
        } else {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        return comments.stream()
                .map(CommentResponse::from)
                .collect(Collectors.toList());
    }
}
