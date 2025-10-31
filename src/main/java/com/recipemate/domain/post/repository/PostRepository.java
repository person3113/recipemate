package com.recipemate.domain.post.repository;

import com.recipemate.domain.post.entity.Post;
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

    Page<Post> findByCategoryOrderByCreatedAtDesc(PostCategory category, Pageable pageable);

    List<Post> findByAuthorId(Long authorId);

    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.author WHERE p.id = :id")
    Optional<Post> findByIdWithAuthor(@Param("id") Long id);

    // 게시글 목록 조회 - 삭제되지 않은 게시글만
    Page<Post> findAllByDeletedAtIsNull(Pageable pageable);

    // 카테고리별 조회 - 삭제되지 않은 게시글만
    Page<Post> findByCategoryAndDeletedAtIsNull(PostCategory category, Pageable pageable);

    // 키워드 검색 (제목 + 내용) - 삭제되지 않은 게시글만
    @Query("SELECT p FROM Post p WHERE p.deletedAt IS NULL " +
            "AND (LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Post> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // 카테고리 + 키워드 검색 - 삭제되지 않은 게시글만
    @Query("SELECT p FROM Post p WHERE p.category = :category AND p.deletedAt IS NULL " +
            "AND (LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Post> searchByCategoryAndKeyword(@Param("category") PostCategory category,
                                          @Param("keyword") String keyword,
                                          Pageable pageable);
}
