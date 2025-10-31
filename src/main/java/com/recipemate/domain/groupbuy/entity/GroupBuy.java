package com.recipemate.domain.groupbuy.entity;

import com.recipemate.domain.user.entity.User;
import com.recipemate.global.common.BaseEntity;
import com.recipemate.global.common.DeliveryMethod;
import com.recipemate.global.common.GroupBuyStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "group_buys", indexes = {
        @Index(name = "idx_groupbuy_status_deadline", columnList = "status, deadline"),
        @Index(name = "idx_groupbuy_recipe_api_id", columnList = "recipe_api_id"),
        @Index(name = "idx_groupbuy_category", columnList = "category"),
        @Index(name = "idx_groupbuy_host_id", columnList = "host_id")
})
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GroupBuy extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id", nullable = false, foreignKey = @ForeignKey(name = "fk_groupbuy_host"))
    private User host;

    @Builder.Default
    @Version
    private Long version = 0L;

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

    @Builder.Default
    @Column(nullable = false)
    private Integer currentHeadcount = 0;

    @Column(nullable = false)
    private LocalDateTime deadline;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeliveryMethod deliveryMethod = DeliveryMethod.BOTH;

    @Column(length = 200)
    private String meetupLocation;

    @Column
    private Integer parcelFee;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isParticipantListPublic = false;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GroupBuyStatus status = GroupBuyStatus.RECRUITING;

    @Column(length = 100)
    private String recipeApiId;

    @Column(length = 200)
    private String recipeName;

    @Column(length = 500)
    private String recipeImageUrl;

    @Builder.Default
    @OneToMany(mappedBy = "groupBuy", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder asc")
    private List<GroupBuyImage> images = new ArrayList<>();

    //== 생성 메서드 ==//
    public static GroupBuy createGeneral(
        User host, String title, String content, String category, Integer totalPrice,
        Integer targetHeadcount, LocalDateTime deadline, DeliveryMethod deliveryMethod,
        String meetupLocation, Integer parcelFee, boolean isParticipantListPublic
    ) {
        validateCreateArgs(totalPrice, targetHeadcount, deadline);
        return GroupBuy.builder()
            .host(host)
            .title(title)
            .content(content)
            .category(category)
            .totalPrice(totalPrice)
            .targetHeadcount(targetHeadcount)
            .currentHeadcount(0)
            .deadline(deadline)
            .deliveryMethod(deliveryMethod)
            .meetupLocation(meetupLocation)
            .parcelFee(parcelFee)
            .isParticipantListPublic(isParticipantListPublic)
            .status(GroupBuyStatus.RECRUITING)
            .build();
    }

    public static GroupBuy createRecipeBased(
        User host, String title, String content, String category, Integer totalPrice,
        Integer targetHeadcount, LocalDateTime deadline, DeliveryMethod deliveryMethod,
        String meetupLocation, Integer parcelFee, boolean isParticipantListPublic,
        String recipeApiId, String recipeName, String recipeImageUrl
    ) {
        validateCreateArgs(totalPrice, targetHeadcount, deadline);
        return GroupBuy.builder()
            .host(host)
            .title(title)
            .content(content)
            .category(category)
            .totalPrice(totalPrice)
            .targetHeadcount(targetHeadcount)
            .currentHeadcount(0)
            .deadline(deadline)
            .deliveryMethod(deliveryMethod)
            .meetupLocation(meetupLocation)
            .parcelFee(parcelFee)
            .isParticipantListPublic(isParticipantListPublic)
            .status(GroupBuyStatus.RECRUITING)
            .recipeApiId(recipeApiId)
            .recipeName(recipeName)
            .recipeImageUrl(recipeImageUrl)
            .build();
    }

    private static void validateCreateArgs(Integer totalPrice, Integer targetHeadcount, LocalDateTime deadline) {
        if (deadline == null || !deadline.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("마감일은 현재보다 이후여야 합니다");
        }
        if (targetHeadcount == null || targetHeadcount < 2) {
            throw new IllegalArgumentException("목표 인원은 2명 이상이어야 합니다");
        }
        if (totalPrice == null || totalPrice < 0) {
            throw new IllegalArgumentException("총 금액은 0원 이상이어야 합니다");
        }
    }

    //== 수정 메서드 ==//
    public void update(
        String title, String content, String category, Integer totalPrice,
        Integer targetHeadcount, LocalDateTime deadline, DeliveryMethod deliveryMethod,
        String meetupLocation, Integer parcelFee, boolean isParticipantListPublic
    ) {
        // 레시피 기반 공구인 경우, 레시피 관련 필드는 수정 불가
        // (이미 recipeApiId, recipeName, recipeImageUrl은 변경되지 않음)
        this.title = title;
        this.content = content;
        this.category = category;
        this.totalPrice = totalPrice;
        this.targetHeadcount = targetHeadcount;
        this.deadline = deadline;
        this.deliveryMethod = deliveryMethod;
        this.meetupLocation = meetupLocation;
        this.parcelFee = parcelFee;
        this.isParticipantListPublic = isParticipantListPublic;
    }

    //== 비즈니스 로직 ==//
    public void increaseParticipant() {
        if (this.currentHeadcount + 1 > this.targetHeadcount) {
            throw new IllegalStateException("목표 인원에 도달했습니다.");
        }
        this.currentHeadcount++;
    }

    public void decreaseParticipant() {
        if (this.currentHeadcount <= 0) {
            throw new IllegalStateException("참여 인원이 1명(주최자)입니다.");
        }
        this.currentHeadcount--;
    }

    public boolean isTargetReached() {
        return this.currentHeadcount >= this.targetHeadcount;
    }

    public void close() {
        this.status = GroupBuyStatus.CLOSED;
    }

    public void markAsImminent() {
        if (this.status == GroupBuyStatus.RECRUITING) {
            this.status = GroupBuyStatus.IMMINENT;
        }
    }

    public boolean isHost(User user) {
        return this.host.equals(user);
    }

    public boolean canParticipate() {
        return this.status == GroupBuyStatus.RECRUITING && !isTargetReached();
    }

    public boolean isRecipeBased() {
        return this.recipeApiId != null;
    }

    public Boolean getParticipantListPublic() {
        return isParticipantListPublic;
    }
}
