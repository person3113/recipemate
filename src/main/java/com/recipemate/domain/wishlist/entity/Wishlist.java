package com.recipemate.domain.wishlist.entity;

import com.recipemate.domain.groupbuy.entity.GroupBuy;
import com.recipemate.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "wishlists",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_groupbuy", columnNames = {"user_id", "group_buy_id"})
        },
        indexes = {
                @Index(name = "idx_user_id_wished_at", columnList = "user_id, wished_at"),
                @Index(name = "idx_group_buy_id", columnList = "group_buy_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Wishlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_wishlist_user"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_buy_id", nullable = false, foreignKey = @ForeignKey(name = "fk_wishlist_groupbuy"))
    private GroupBuy groupBuy;

    @Column(nullable = false)
    private LocalDateTime wishedAt;

    public static Wishlist create(User user, GroupBuy groupBuy) {
        return new Wishlist(
                null,
                user,
                groupBuy,
                LocalDateTime.now()
        );
    }
}
