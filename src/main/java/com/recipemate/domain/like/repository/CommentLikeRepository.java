package com.recipemate.domain.like.repository;

import com.recipemate.domain.comment.entity.Comment;
import com.recipemate.domain.like.entity.CommentLike;
import com.recipemate.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    Optional<CommentLike> findByUserAndComment(User user, Comment comment);

    long countByComment(Comment comment);

    boolean existsByUserAndComment(User user, Comment comment);
}
