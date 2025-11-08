package com.recipemate.domain.comment.repository;

import com.recipemate.domain.comment.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * 공구에 달린 최상위 댓글(부모 댓글)만 조회 (생성일 오름차순)
     * @param groupBuyId 공구 ID
     * @return 최상위 댓글 목록
     */
    List<Comment> findByGroupBuyIdAndParentIsNullOrderByCreatedAtAsc(Long groupBuyId);

    /**
     * 게시글에 달린 최상위 댓글(부모 댓글)만 조회 (생성일 오름차순)
     * @param postId 게시글 ID
     * @return 최상위 댓글 목록
     */
    List<Comment> findByPostIdAndParentIsNullOrderByCreatedAtAsc(Long postId);

    /**
     * 공구에 달린 최상위 댓글 페이지네이션 조회 (삭제된 것 포함)
     * @param groupBuyId 공구 ID
     * @param pageable 페이지 정보
     * @return 최상위 댓글 페이지
     */
    @Query("SELECT c FROM Comment c WHERE c.groupBuy.id = :groupBuyId AND c.parent IS NULL ORDER BY c.createdAt ASC")
    Page<Comment> findByGroupBuyIdAndParentIsNullPageable(@Param("groupBuyId") Long groupBuyId, Pageable pageable);

    /**
     * 게시글에 달린 최상위 댓글 페이지네이션 조회 (삭제된 것 포함)
     * @param postId 게시글 ID
     * @param pageable 페이지 정보
     * @return 최상위 댓글 페이지
     */
    @Query("SELECT c FROM Comment c WHERE c.post.id = :postId AND c.parent IS NULL ORDER BY c.createdAt ASC")
    Page<Comment> findByPostIdAndParentIsNullPageable(@Param("postId") Long postId, Pageable pageable);

    /**
     * 특정 댓글의 대댓글 조회 (생성일 오름차순)
     * @param parentId 부모 댓글 ID
     * @return 대댓글 목록
     */
    List<Comment> findByParentIdOrderByCreatedAtAsc(Long parentId);

    /**
     * 공구에 달린 모든 댓글 조회 (최상위 댓글 + 대댓글, 삭제되지 않은 것만)
     * @param groupBuyId 공구 ID
     * @return 댓글 목록
     */
    @Query("SELECT c FROM Comment c WHERE c.groupBuy.id = :groupBuyId AND c.deletedAt IS NULL ORDER BY c.createdAt ASC")
    List<Comment> findAllByGroupBuyIdAndNotDeleted(@Param("groupBuyId") Long groupBuyId);

    /**
     * 게시글에 달린 모든 댓글 조회 (최상위 댓글 + 대댓글, 삭제되지 않은 것만)
     * @param postId 게시글 ID
     * @return 댓글 목록
     */
    @Query("SELECT c FROM Comment c WHERE c.post.id = :postId AND c.deletedAt IS NULL ORDER BY c.createdAt ASC")
    List<Comment> findAllByPostIdAndNotDeleted(@Param("postId") Long postId);

    /**
     * 작성자가 작성한 댓글 조회
     * @param authorId 작성자 ID
     * @return 댓글 목록
     */
    List<Comment> findByAuthorId(Long authorId);

    /**
     * 공구에 달린 댓글 개수 조회 (삭제된 것 포함 - 소프트 삭제)
     * @param groupBuyId 공구 ID
     * @return 댓글 개수
     */
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.groupBuy.id = :groupBuyId")
    long countByGroupBuyIdAndNotDeleted(@Param("groupBuyId") Long groupBuyId);

    /**
     * 게시글에 달린 댓글 개수 조회 (삭제된 것 포함 - 소프트 삭제)
     * @param postId 게시글 ID
     * @return 댓글 개수
     */
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.post.id = :postId")
    long countByPostIdAndNotDeleted(@Param("postId") Long postId);
}
