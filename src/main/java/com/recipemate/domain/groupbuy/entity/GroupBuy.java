package com.recipemate.domain.groupbuy.entity;

import com.recipemate.domain.user.entity.User;
import com.recipemate.global.common.BaseEntity;
import com.recipemate.global.common.DeliveryMethod;
import com.recipemate.global.common.GroupBuyStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "group_buys", indexes = {
        @Index(name = "idx_groupbuy_status_deadline", columnList = "status, deadline"),
        @Index(name = "idx_groupbuy_recipe_api_id", columnList = "recipe_api_id"),
        @Index(name = "idx_groupbuy_category", columnList = "category"),
        @Index(name = "idx_groupbuy_host_id", columnList = "host_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GroupBuy extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id", nullable = false, foreignKey = @ForeignKey(name = "fk_groupbuy_host"))
    private User host;

    @Version
    private Long version;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 2000)
    private String content;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(nullable = false)
    private Integer totalPrice;

    @Column(nullable = false)
    private Integer targetHeadcount;

    @Column(nullable = false)
    private Integer currentHeadcount;

    @Column(nullable = false)
    private LocalDateTime deadline;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeliveryMethod deliveryMethod;

    @Column(length = 200)
    private String meetupLocation;

    @Column
    private Integer parcelFee;

    @Column(nullable = false)
    private Boolean participantListPublic;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GroupBuyStatus status;

    @Column(length = 100)
    private String recipeApiId;

    @Column(length = 200)
    private String recipeName;

    @Column(length = 500)
    private String recipeImageUrl;

    public static GroupBuy createGeneral(
            User host,
            String title,
            String content,
            String category,
            Integer totalPrice,
            Integer targetHeadcount,
            LocalDateTime deadline,
            DeliveryMethod deliveryMethod,
            String meetupLocation,
            Integer parcelFee,
            Boolean participantListPublic
    ) {
        validateCreation(totalPrice, targetHeadcount, deadline);

        return new GroupBuy(
                null,
                host,
                0L,
                title,
                content,
                category,
                totalPrice,
                targetHeadcount,
                0,
                deadline,
                deliveryMethod,
                meetupLocation,
                parcelFee,
                participantListPublic,
                GroupBuyStatus.RECRUITING,
                null,
                null,
                null
        );
    }

    public static GroupBuy createRecipeBased(
            User host,
            String title,
            String content,
            String category,
            Integer totalPrice,
            Integer targetHeadcount,
            LocalDateTime deadline,
            DeliveryMethod deliveryMethod,
            String meetupLocation,
            Integer parcelFee,
            Boolean participantListPublic,
            String recipeApiId,
            String recipeName,
            String recipeImageUrl
    ) {
        validateCreation(totalPrice, targetHeadcount, deadline);

        return new GroupBuy(
                null,
                host,
                0L,
                title,
                content,
                category,
                totalPrice,
                targetHeadcount,
                0,
                deadline,
                deliveryMethod,
                meetupLocation,
                parcelFee,
                participantListPublic,
                GroupBuyStatus.RECRUITING,
                recipeApiId,
                recipeName,
                recipeImageUrl
        );
    }

    private static void validateCreation(Integer totalPrice, Integer targetHeadcount, LocalDateTime deadline) {
        if (totalPrice < 0) {
            throw new IllegalArgumentException("총 금액은 0원 이상이어야 합니다");
        }
        if (targetHeadcount < 2) {
            throw new IllegalArgumentException("목표 인원은 2명 이상이어야 합니다");
        }
        if (deadline.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("마감일은 현재보다 이후여야 합니다");
        }
    }

    public void increaseParticipant() {
        if (currentHeadcount >= targetHeadcount) {
            throw new IllegalStateException("목표 인원에 도달했습니다");
        }
        this.currentHeadcount++;
    }

    public void decreaseParticipant() {
        if (currentHeadcount <= 0) {
            throw new IllegalStateException("참여 인원이 0명입니다");
        }
        this.currentHeadcount--;
    }

    public boolean isTargetReached() {
        return currentHeadcount >= targetHeadcount;
    }

    public void close() {
        this.status = GroupBuyStatus.CLOSED;
    }

    public void markAsImminent() {
        this.status = GroupBuyStatus.IMMINENT;
    }

    public void update(
            String title,
            String content,
            String category,
            Integer totalPrice,
            Integer targetHeadcount,
            LocalDateTime deadline,
            DeliveryMethod deliveryMethod,
            String meetupLocation,
            Integer parcelFee,
            Boolean participantListPublic
    ) {
        if (totalPrice < 0) {
            throw new IllegalArgumentException("총 금액은 0원 이상이어야 합니다");
        }
        if (targetHeadcount < 2) {
            throw new IllegalArgumentException("목표 인원은 2명 이상이어야 합니다");
        }
        if (deadline.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("마감일은 현재보다 이후여야 합니다");
        }

        this.title = title;
        this.content = content;
        this.category = category;
        this.totalPrice = totalPrice;
        this.targetHeadcount = targetHeadcount;
        this.deadline = deadline;
        this.deliveryMethod = deliveryMethod;
        this.meetupLocation = meetupLocation;
        this.parcelFee = parcelFee;
        this.participantListPublic = participantListPublic;
    }

    public boolean isHost(User user) {
        return this.host.equals(user);
    }

    public boolean canParticipate() {
        return status == GroupBuyStatus.RECRUITING && !isTargetReached();
    }

    public boolean isRecipeBased() {
        return recipeApiId != null;
    }
}
