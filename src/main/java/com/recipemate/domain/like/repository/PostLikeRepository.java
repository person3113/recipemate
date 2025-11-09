package com.recipemate.domain.like.repository;

import com.recipemate.domain.like.entity.PostLike;
import com.recipemate.domain.post.entity.Post;
import com.recipemate.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    Optional<PostLike> findByUserAndPost(User user, Post post);

    long countByPost(Post post);

    boolean existsByUserAndPost(User user, Post post);
}
