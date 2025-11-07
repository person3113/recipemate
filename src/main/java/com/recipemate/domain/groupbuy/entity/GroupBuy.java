package com.recipemate.domain.groupbuy.entity;

import com.recipemate.domain.user.entity.User;
import com.recipemate.global.common.BaseEntity;
import com.recipemate.global.common.DeliveryMethod;
import com.recipemate.global.common.GroupBuyCategory;
import com.recipemate.global.common.GroupBuyStatus;
import com.recipemate.global.exception.CustomException;
import com.recipemate.global.exception.ErrorCode;
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
        @Index(name = "idx_groupbuy_host_id", columnList = "host_id"),
        @Index(name = "idx_groupbuy_deleted_created", columnList = "deleted_at, created_at"),
        @Index(name = "idx_groupbuy_status_deleted", columnList = "status, deleted_at, created_at"),
        @Index(name = "idx_groupbuy_category_deleted", columnList = "category, deleted_at, created_at")
})
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GroupBuy extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id", nullable = false, foreignKey = @ForeignKey(name = "fk_groupbuy_host"))
    private final User host;

    @Version
    private Long version;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 2000)
    private String content;
    
    @Column(length = 1000)
    private String ingredients; // 구조화된 재료 목록 (별도 저장)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private GroupBuyCategory category;

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
    private final String recipeApiId;

    @Column(length = 200)
    private final String recipeName;

    @Column(length = 500)
    private final String recipeImageUrl;

    @Builder.Default
    @OneToMany(mappedBy = "groupBuy", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Participation> participations = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "groupBuy", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder asc")
    private List<GroupBuyImage> images = new ArrayList<>();

    //== 생성 메서드 ==//
    public static GroupBuy createGeneral(
        User host, String title, String content, GroupBuyCategory category, Integer totalPrice,
        Integer targetHeadcount, LocalDateTime deadline, DeliveryMethod deliveryMethod,
        String meetupLocation, Integer parcelFee, boolean isParticipantListPublic
    ) {
        validateCreateArgs(totalPrice, targetHeadcount, deadline);
        return GroupBuy.builder()
            .host(host)
            .title(title)
            .content(content)
            .ingredients(null) // 일반 공구는 재료 없음
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
        User host, String title, String content, String ingredients, GroupBuyCategory category, Integer totalPrice,
        Integer targetHeadcount, LocalDateTime deadline, DeliveryMethod deliveryMethod,
        String meetupLocation, Integer parcelFee, boolean isParticipantListPublic,
        String recipeApiId, String recipeName, String recipeImageUrl
    ) {
        validateCreateArgs(totalPrice, targetHeadcount, deadline);
        return GroupBuy.builder()
            .host(host)
            .title(title)
            .content(content)
            .ingredients(ingredients) // 레시피 기반 공구는 재료 목록 별도 저장
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
            throw new CustomException(ErrorCode.INVALID_DEADLINE);
        }
        if (targetHeadcount == null || targetHeadcount < 2) {
            throw new CustomException(ErrorCode.INVALID_TARGET_HEADCOUNT);
        }
        if (totalPrice == null || totalPrice < 0) {
            throw new CustomException(ErrorCode.INVALID_TOTAL_PRICE);
        }
    }

    //== 수정 메서드 ==//
    public void update(
        String title, String content, GroupBuyCategory category, Integer totalPrice,
        Integer targetHeadcount, LocalDateTime deadline, DeliveryMethod deliveryMethod,
        String meetupLocation, Integer parcelFee, boolean isParticipantListPublic
    ) {
        // 목표 인원 검증: 현재 참여 인원보다 작을 수 없음
        if (targetHeadcount != null && targetHeadcount < this.currentHeadcount) {
            throw new CustomException(ErrorCode.TARGET_HEADCOUNT_BELOW_CURRENT);
        }
        
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


    public Participation addParticipant(User participant, int quantity, DeliveryMethod selectedDeliveryMethod) {
        // 1. 목표 인원 도달 여부 검증
        if (isTargetReached()) {
            throw new CustomException(ErrorCode.MAX_PARTICIPANTS_EXCEEDED);
        }
        // 2. 참여 가능 상태인지 검증
        if (this.status != GroupBuyStatus.RECRUITING) {
            throw new CustomException(ErrorCode.GROUP_BUY_CLOSED);
        }
        // 3. 주최자 본인 참여 불가 검증
        if (isHost(participant)) {
            throw new CustomException(ErrorCode.HOST_CANNOT_PARTICIPATE);
        }

        // 4. Participation 엔티티 생성
        Participation newParticipation = Participation.create(
            participant,
            this,
            quantity,
            selectedDeliveryMethod
        );

        // 5. 참여자 추가 및 상태 변경
        this.participations.add(newParticipation);
        increaseParticipant();
        if (isTargetReached()) {
            close();
        }

        return newParticipation;
    }

    public void cancelParticipation(Participation participation) {
        // 1. 마감 1일 전 취소 제한 검증
        if (LocalDateTime.now().isAfter(this.deadline.minusDays(1))) {
            throw new CustomException(ErrorCode.CANCELLATION_DEADLINE_PASSED);
        }

        // 2. 참여 정보 제거 및 상태 변경
        this.participations.remove(participation);
        decreaseParticipant();
        if (this.status == GroupBuyStatus.CLOSED && !isTargetReached()) {
            reopen();
        }
    }

    private void increaseParticipant() {
        if (this.currentHeadcount + 1 > this.targetHeadcount) {
            throw new CustomException(ErrorCode.MAX_PARTICIPANTS_EXCEEDED);
        }
        this.currentHeadcount++;
    }

    private void decreaseParticipant() {
        if (this.currentHeadcount <= 0) {
            throw new CustomException(ErrorCode.NO_PARTICIPANTS);
        }
        this.currentHeadcount--;
    }

    public boolean isTargetReached() {
        return this.currentHeadcount >= this.targetHeadcount;
    }

    public void close() {
        this.status = GroupBuyStatus.CLOSED;
    }

    public void reopen() {
        if (this.status == GroupBuyStatus.CLOSED) {
            this.status = GroupBuyStatus.RECRUITING;
        }
    }

    public void markAsImminent() {
        if (this.status == GroupBuyStatus.RECRUITING) {
            this.status = GroupBuyStatus.IMMINENT;
        }
    }

    public void updateStatus(GroupBuyStatus newStatus) {
        if (newStatus == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        this.status = newStatus;
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
