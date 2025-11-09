package com.recipemate.domain.post.repository;

import com.recipemate.domain.post.dto.PostWithCountsDto;
import com.recipemate.domain.post.entity.Post;
import com.recipemate.domain.user.entity.User;
import com.recipemate.global.common.PostCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findByAuthorId(Long authorId);

    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.author WHERE p.id = :id")
    Optional<Post> findByIdWithAuthor(@Param("id") Long id);

    // N+1 문제를 해결하기 위해 게시글과 함께 좋아요 수, 댓글 수를 함께 조회
    @Query("SELECT new com.recipemate.domain.post.dto.PostWithCountsDto(" +
            "p, " +
            "(SELECT COUNT(pl.id) FROM PostLike pl WHERE pl.post = p), " +
            "(SELECT COUNT(c.id) FROM Comment c WHERE c.post = p)) " +
            "FROM Post p " +
            "WHERE p.deletedAt IS NULL")
    Page<PostWithCountsDto> findAllWithCounts(Pageable pageable);

    @Query("SELECT new com.recipemate.domain.post.dto.PostWithCountsDto(" +
            "p, " +
            "(SELECT COUNT(pl.id) FROM PostLike pl WHERE pl.post = p), " +
            "(SELECT COUNT(c.id) FROM Comment c WHERE c.post = p)) " +
            "FROM Post p " +
            "WHERE p.category = :category AND p.deletedAt IS NULL")
    Page<PostWithCountsDto> findByCategoryWithCounts(@Param("category") PostCategory category, Pageable pageable);

    @Query("SELECT new com.recipemate.domain.post.dto.PostWithCountsDto(" +
            "p, " +
            "(SELECT COUNT(pl.id) FROM PostLike pl WHERE pl.post = p), " +
            "(SELECT COUNT(c.id) FROM Comment c WHERE c.post = p)) " +
            "FROM Post p " +
            "WHERE p.deletedAt IS NULL " +
            "AND (LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<PostWithCountsDto> searchByKeywordWithCounts(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT new com.recipemate.domain.post.dto.PostWithCountsDto(" +
            "p, " +
            "(SELECT COUNT(pl.id) FROM PostLike pl WHERE pl.post = p), " +
            "(SELECT COUNT(c.id) FROM Comment c WHERE c.post = p)) " +
            "FROM Post p " +
            "WHERE p.category = :category AND p.deletedAt IS NULL " +
            "AND (LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<PostWithCountsDto> searchByCategoryAndKeywordWithCounts(@Param("category") PostCategory category,
                                                               @Param("keyword") String keyword,
                                                               Pageable pageable);

    @Query("SELECT new com.recipemate.domain.user.dto.MyPostDto(" +
            "p.id, p.title, p.createdAt, p.viewCount, " +
            "(SELECT COUNT(c) FROM Comment c WHERE c.post = p), " +
            "(SELECT COUNT(pl) FROM PostLike pl WHERE pl.post = p)) " +
            "FROM Post p " +
            "WHERE p.author = :user AND p.deletedAt IS NULL")
    Page<com.recipemate.domain.user.dto.MyPostDto> findPostsByUserWithCounts(@Param("user") User user, Pageable pageable);

    @Query("SELECT new com.recipemate.domain.user.dto.MyPostDto(" +
            "p.id, p.title, p.createdAt, p.viewCount, " +
            "(SELECT COUNT(c) FROM Comment c WHERE c.post = p), " +
            "(SELECT COUNT(pl) FROM PostLike pl WHERE pl.post = p), " +
            "p.deletedAt) " +
            "FROM Post p " +
            "WHERE p.author = :user")
    Page<com.recipemate.domain.user.dto.MyPostDto> findPostsByUserForMyActivity(@Param("user") User user, Pageable pageable);

    @Query("SELECT new com.recipemate.domain.post.dto.PostWithCountsDto(" +
            "p, " +
            "(SELECT COUNT(pl.id) FROM PostLike pl WHERE pl.post = p), " +
            "(SELECT COUNT(c.id) FROM Comment c WHERE c.post = p)) " +
            "FROM Post p " +
            "WHERE p.id IN :postIds AND p.deletedAt IS NULL")
    List<PostWithCountsDto> findAllWithCountsByIdIn(@Param("postIds") List<Long> postIds);
}
