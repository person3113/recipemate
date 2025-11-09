package com.recipemate.domain.like.entity;

import com.recipemate.domain.comment.entity.Comment;
import com.recipemate.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "comment_likes",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "comment_like_uk",
            columnNames = {"user_id", "comment_id"}
        )
    },
    indexes = {
        @Index(name = "idx_comment_like_comment_id", columnList = "comment_id"),
        @Index(name = "idx_comment_like_user_id", columnList = "user_id")
    }
)
public class CommentLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_comment_like_user"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false, foreignKey = @ForeignKey(name = "fk_comment_like_comment"))
    private Comment comment;

    public CommentLike(User user, Comment comment) {
        this.user = user;
        this.comment = comment;
    }
}
