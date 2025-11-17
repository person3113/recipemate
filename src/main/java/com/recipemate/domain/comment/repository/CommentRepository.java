package com.recipemate.domain.comment.repository;

import com.recipemate.domain.comment.entity.Comment;
import com.recipemate.domain.post.entity.Post;
import com.recipemate.domain.user.entity.User;
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
    @Query("SELECT c FROM Comment c JOIN FETCH c.author WHERE c.groupBuy.id = :groupBuyId AND c.parent IS NULL ORDER BY c.createdAt ASC")
    List<Comment> findByGroupBuyIdAndParentIsNullOrderByCreatedAtAsc(@Param("groupBuyId") Long groupBuyId);

    /**
     * 게시글에 달린 최상위 댓글(부모 댓글)만 조회 (생성일 오름차순)
     * @param postId 게시글 ID
     * @return 최상위 댓글 목록
     */
    @Query("SELECT c FROM Comment c JOIN FETCH c.author WHERE c.post.id = :postId AND c.parent IS NULL ORDER BY c.createdAt ASC")
    List<Comment> findByPostIdAndParentIsNullOrderByCreatedAtAsc(@Param("postId") Long postId);

    /**
     * 공구에 달린 최상위 댓글 페이지네이션 조회 (삭제된 것 포함)
     * @param groupBuyId 공구 ID
     * @param pageable 페이지 정보
     * @return 최상위 댓글 페이지
     */
    @Query("SELECT c FROM Comment c JOIN FETCH c.author WHERE c.groupBuy.id = :groupBuyId AND c.parent IS NULL ORDER BY c.createdAt ASC")
    Page<Comment> findByGroupBuyIdAndParentIsNullPageable(@Param("groupBuyId") Long groupBuyId, Pageable pageable);

    /**
     * 게시글에 달린 최상위 댓글 페이지네이션 조회 (삭제된 것 포함)
     * @param postId 게시글 ID
     * @param pageable 페이지 정보
     * @return 최상위 댓글 페이지
     */
    @Query("SELECT c FROM Comment c JOIN FETCH c.author WHERE c.post.id = :postId AND c.parent IS NULL ORDER BY c.createdAt ASC")
    Page<Comment> findByPostIdAndParentIsNullPageable(@Param("postId") Long postId, Pageable pageable);

    /**
     * 특정 댓글의 대댓글 조회 (생성일 오름차순)
     * @param parentId 부모 댓글 ID
     * @return 대댓글 목록
     */
    List<Comment> findByParentIdOrderByCreatedAtAsc(Long parentId);

    /**
     * 여러 부모 댓글의 대댓글 일괄 조회 (N+1 방지)
     * @param parentIds 부모 댓글 ID 목록
     * @return 대댓글 목록
     */
    @Query("SELECT c FROM Comment c JOIN FETCH c.author WHERE c.parent.id IN :parentIds ORDER BY c.createdAt ASC")
    List<Comment> findByParentIdIn(@Param("parentIds") List<Long> parentIds);

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
    long countByGroupBuyId(@Param("groupBuyId") Long groupBuyId);

    /**
     * 게시글에 달린 댓글 개수 조회 (삭제된 것 포함 - 소프트 삭제)
     * @param postId 게시글 ID
     * @return 댓글 개수
     */
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.post.id = :postId")
    long countByPostId(@Param("postId") Long postId);

    /**
     * 작성자가 작성한 댓글 페이징 조회 (게시글 댓글만, 삭제되지 않은 것)
     * @param author 작성자
     * @param pageable 페이지 정보
     * @return 댓글 페이지
     */
    @Query("SELECT c FROM Comment c JOIN FETCH c.post p WHERE c.author = :author AND c.post IS NOT NULL AND c.deletedAt IS NULL ORDER BY c.createdAt DESC")
    Page<Comment> findByAuthorAndPostIsNotNull(@Param("author") User author, Pageable pageable);

    /**
     * 작성자가 작성한 댓글 페이징 조회 (게시글 댓글만, 삭제된 것 포함) - '내 커뮤니티 활동' 전용
     * @param author 작성자
     * @param pageable 페이지 정보
     * @return 댓글 페이지
     */
    @Query("SELECT c FROM Comment c JOIN FETCH c.post p WHERE c.author = :author AND c.post IS NOT NULL ORDER BY c.createdAt DESC")
    Page<Comment> findByAuthorForMyActivity(@Param("author") User author, Pageable pageable);
}
