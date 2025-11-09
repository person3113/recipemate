package com.recipemate.domain.like.repository;

import com.recipemate.domain.like.entity.PostLike;
import com.recipemate.domain.post.entity.Post;
import com.recipemate.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    Optional<PostLike> findByUserAndPost(User user, Post post);

    long countByPost(Post post);

    boolean existsByUserAndPost(User user, Post post);

    @Query("SELECT pl FROM PostLike pl JOIN FETCH pl.post p WHERE pl.user = :user AND p.deletedAt IS NULL ORDER BY pl.id DESC")
    Page<PostLike> findByUserWithPost(@Param("user") User user, Pageable pageable);
}
