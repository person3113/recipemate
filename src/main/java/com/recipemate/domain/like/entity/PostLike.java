package com.recipemate.domain.like.entity;

import com.recipemate.domain.post.entity.Post;
import com.recipemate.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "post_likes",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "post_like_uk",
            columnNames = {"user_id", "post_id"}
        )
    },
    indexes = {
        @Index(name = "idx_post_like_post_id", columnList = "post_id"),
        @Index(name = "idx_post_like_user_id", columnList = "user_id")
    }
)
public class PostLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_post_like_user"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false, foreignKey = @ForeignKey(name = "fk_post_like_post"))
    private Post post;

    public PostLike(User user, Post post) {
        this.user = user;
        this.post = post;
    }
}
