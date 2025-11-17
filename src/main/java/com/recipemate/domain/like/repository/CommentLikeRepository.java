package com.recipemate.domain.like.repository;

import com.recipemate.domain.comment.entity.Comment;
import com.recipemate.domain.like.entity.CommentLike;
import com.recipemate.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    Optional<CommentLike> findByUserAndComment(User user, Comment comment);

    long countByComment(Comment comment);

    boolean existsByUserAndComment(User user, Comment comment);
    
    /**
     * 여러 댓글의 좋아요 수를 일괄 조회 (N+1 방지)
     * @param commentIds 댓글 ID 목록
     * @return 댓글 ID별 좋아요 수 맵
     */
    @Query("SELECT cl.comment.id as commentId, COUNT(cl) as likeCount FROM CommentLike cl WHERE cl.comment.id IN :commentIds GROUP BY cl.comment.id")
    List<CommentLikeCount> countByCommentIds(@Param("commentIds") List<Long> commentIds);
    
    /**
     * 특정 사용자가 여러 댓글에 좋아요를 눌렀는지 일괄 조회 (N+1 방지)
     * @param userId 사용자 ID
     * @param commentIds 댓글 ID 목록
     * @return 좋아요를 누른 댓글 ID 목록
     */
    @Query("SELECT cl.comment.id FROM CommentLike cl WHERE cl.user.id = :userId AND cl.comment.id IN :commentIds")
    List<Long> findLikedCommentIdsByUserAndCommentIds(@Param("userId") Long userId, @Param("commentIds") List<Long> commentIds);
    
    /**
     * 좋아요 수 프로젝션 인터페이스
     */
    interface CommentLikeCount {
        Long getCommentId();
        Long getLikeCount();
    }
    
    /**
     * CommentLikeCount 리스트를 Map으로 변환하는 유틸리티 메서드
     */
    default Map<Long, Long> toLikeCountMap(List<CommentLikeCount> counts) {
        return counts.stream()
                .collect(Collectors.toMap(
                        CommentLikeCount::getCommentId,
                        CommentLikeCount::getLikeCount
                ));
    }
}

