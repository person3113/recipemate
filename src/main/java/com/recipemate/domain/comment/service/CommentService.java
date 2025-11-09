package com.recipemate.domain.comment.service;

import com.recipemate.domain.comment.dto.CommentResponse;
import com.recipemate.domain.comment.dto.CreateCommentRequest;
import com.recipemate.domain.comment.dto.UpdateCommentRequest;
import com.recipemate.domain.comment.entity.Comment;
import com.recipemate.domain.comment.repository.CommentRepository;
import com.recipemate.domain.groupbuy.entity.GroupBuy;
import com.recipemate.domain.groupbuy.repository.GroupBuyRepository;
import com.recipemate.domain.like.repository.CommentLikeRepository;
import com.recipemate.domain.post.entity.Post;
import com.recipemate.domain.post.repository.PostRepository;
import com.recipemate.domain.user.entity.User;
import com.recipemate.domain.user.repository.UserRepository;
import com.recipemate.global.common.EntityType;
import com.recipemate.global.event.CommentCreatedEvent;
import com.recipemate.global.exception.CustomException;
import com.recipemate.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final CommentLikeRepository commentLikeRepository;
    private final ApplicationEventPublisher eventPublisher;

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

        // 6. 댓글 생성 이벤트 발행 (알림 등)
        eventPublisher.publishEvent(new CommentCreatedEvent(savedComment));

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
        
        // updatedAt 필드가 즉시 갱신되도록 명시적으로 flush
        Comment updatedComment = commentRepository.saveAndFlush(comment);
        
        // 대댓글도 함께 조회하여 반환
        List<Comment> replies = commentRepository.findByParentIdOrderByCreatedAtAsc(updatedComment.getId());
        return CommentResponse.fromWithReplies(updatedComment, replies);
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
     * 댓글 단건 조회 (삭제된 댓글 포함)
     */
    public CommentResponse getCommentById(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));
        
        // 대댓글도 함께 조회
        List<Comment> replies = commentRepository.findByParentIdOrderByCreatedAtAsc(comment.getId());
        return CommentResponse.fromWithReplies(comment, replies);
    }

    /**
     * 댓글 단건 조회 with 좋아요 정보 (삭제된 댓글 포함)
     */
    public CommentResponse getCommentByIdWithLikes(Long commentId, Long currentUserId) {
        CommentResponse response = getCommentById(commentId);
        return enrichWithLikeInfo(response, currentUserId);
    }


    
    /**
     * 특정 대상의 전체 댓글 개수 조회 (부모 댓글 + 대댓글, 삭제되지 않은 것만)
     */
    public long getTotalCommentCount(EntityType targetType, Long targetId) {
        if (targetType == EntityType.GROUP_BUY) {
            return commentRepository.countByGroupBuyIdAndNotDeleted(targetId);
        } else if (targetType == EntityType.POST) {
            return commentRepository.countByPostIdAndNotDeleted(targetId);
        } else {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    /**
     * 댓글에 좋아요 정보 추가 (현재 사용자 기준)
     */
    private CommentResponse enrichWithLikeInfo(CommentResponse response, Long currentUserId) {
        // 댓글 엔티티 조회
        Comment comment = commentRepository.findById(response.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));
        
        // 좋아요 개수 조회
        long likeCount = commentLikeRepository.countByComment(comment);
        
        // 현재 사용자의 좋아요 여부 조회
        boolean isLiked = false;
        if (currentUserId != null) {
            User user = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
            isLiked = commentLikeRepository.existsByUserAndComment(user, comment);
        }
        
        // Builder를 사용하여 좋아요 정보가 포함된 새 응답 생성
        return CommentResponse.builder()
                .id(response.getId())
                .authorId(response.getAuthorId())
                .authorNickname(response.getAuthorNickname())
                .authorEmail(response.getAuthorEmail())
                .content(response.getContent())
                .type(response.getType())
                .parentId(response.getParentId())
                .createdAt(response.getCreatedAt())
                .updatedAt(response.getUpdatedAt())
                .isDeleted(response.isDeleted())
                .likeCount(likeCount)
                .isLiked(isLiked)
                .replies(response.getReplies().stream()
                        .map(reply -> enrichWithLikeInfo(reply, currentUserId))
                        .collect(Collectors.toList()))
                .build();
    }

    /**
     * 특정 대상의 댓글 목록 조회 with 좋아요 정보 (htmx fragment용)
     */
    public List<CommentResponse> getCommentsByTarget(EntityType targetType, Long targetId, Long currentUserId) {
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
                .map(response -> enrichWithLikeInfo(response, currentUserId))
                .collect(Collectors.toList());
    }

    /**
     * 특정 대상의 댓글 목록 페이지네이션 조회 with 좋아요 정보 (htmx fragment용)
     */
    public Page<CommentResponse> getCommentsByTargetPageable(EntityType targetType, Long targetId, Long currentUserId, Pageable pageable) {
        Page<Comment> commentsPage;

        if (targetType == EntityType.GROUP_BUY) {
            commentsPage = commentRepository.findByGroupBuyIdAndParentIsNullPageable(targetId, pageable);
        } else if (targetType == EntityType.POST) {
            commentsPage = commentRepository.findByPostIdAndParentIsNullPageable(targetId, pageable);
        } else {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        // 각 댓글에 대한 대댓글 조회 및 좋아요 정보 설정
        return commentsPage.map(comment -> {
            List<Comment> replies = commentRepository.findByParentIdOrderByCreatedAtAsc(comment.getId());
            CommentResponse response = CommentResponse.fromWithReplies(comment, replies);
            return enrichWithLikeInfo(response, currentUserId);
        });
    }
}
