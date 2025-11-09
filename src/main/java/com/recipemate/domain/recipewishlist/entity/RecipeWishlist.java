package com.recipemate.domain.recipewishlist.entity;

import com.recipemate.domain.recipe.entity.Recipe;
import com.recipemate.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "recipe_wishlists",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_recipe", columnNames = {"user_id", "recipe_id"})
        },
        indexes = {
                @Index(name = "idx_recipe_wishlist_user_wished_at", columnList = "user_id, wished_at"),
                @Index(name = "idx_recipe_wishlist_recipe_id", columnList = "recipe_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RecipeWishlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_recipe_wishlist_user"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false, foreignKey = @ForeignKey(name = "fk_recipe_wishlist_recipe"))
    private Recipe recipe;

    @Column(nullable = false)
    private LocalDateTime wishedAt;

    public static RecipeWishlist create(User user, Recipe recipe) {
        return new RecipeWishlist(
                null,
                user,
                recipe,
                LocalDateTime.now()
        );
    }
}
