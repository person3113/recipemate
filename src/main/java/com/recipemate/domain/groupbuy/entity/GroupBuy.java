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
    private Integer targetAmount;

    @Builder.Default
    @Column(nullable = false)
    private Integer currentAmount = 0;

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
        User host, String title, String content, String ingredients, GroupBuyCategory category, Integer targetAmount,
        Integer targetHeadcount, LocalDateTime deadline, DeliveryMethod deliveryMethod,
        String meetupLocation, Integer parcelFee, boolean isParticipantListPublic
    ) {
        validateCreateArgs(targetAmount, targetHeadcount, deadline);
        return GroupBuy.builder()
            .host(host)
            .title(title)
            .content(content)
            .ingredients(ingredients)
            .category(category)
            .targetAmount(targetAmount)
            .currentAmount(0)
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
        User host, String title, String content, String ingredients, GroupBuyCategory category, Integer targetAmount,
        Integer targetHeadcount, LocalDateTime deadline, DeliveryMethod deliveryMethod,
        String meetupLocation, Integer parcelFee, boolean isParticipantListPublic,
        String recipeApiId, String recipeName, String recipeImageUrl
    ) {
        validateCreateArgs(targetAmount, targetHeadcount, deadline);
        return GroupBuy.builder()
            .host(host)
            .title(title)
            .content(content)
            .ingredients(ingredients)
            .category(category)
            .targetAmount(targetAmount)
            .currentAmount(0)
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

    private static void validateCreateArgs(Integer targetAmount, Integer targetHeadcount, LocalDateTime deadline) {
        if (deadline == null || !deadline.isAfter(LocalDateTime.now())) {
            throw new CustomException(ErrorCode.INVALID_DEADLINE);
        }
        if (targetHeadcount == null || targetHeadcount < 2) {
            throw new CustomException(ErrorCode.INVALID_TARGET_HEADCOUNT);
        }
        if (targetAmount == null || targetAmount < 0) {
            throw new CustomException(ErrorCode.INVALID_TOTAL_PRICE);
        }
    }

    //== 수정 메서드 ==//
    public void update(
        String title, String content, GroupBuyCategory category, Integer targetAmount,
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
        this.targetAmount = targetAmount;
        this.targetHeadcount = targetHeadcount;
        this.deadline = deadline;
        this.deliveryMethod = deliveryMethod;
        this.meetupLocation = meetupLocation;
        this.parcelFee = parcelFee;
        this.isParticipantListPublic = isParticipantListPublic;
    }
    
    /**
     * 재료 목록 업데이트 (레시피 기반 공구 수정 시 사용)
     */
    public void updateIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    //== 비즈니스 로직 ==//


    public Participation addParticipant(User participant, int quantity, DeliveryMethod selectedDeliveryMethod, com.recipemate.domain.user.entity.Address address, Integer totalPayment) {
        // 1. 목표 인원 도달 여부 검증
        if (isTargetReached()) {
            throw new CustomException(ErrorCode.MAX_PARTICIPANTS_EXCEEDED);
        }
        // 2. 참여 가능 상태인지 검증 (RECRUITING 또는 IMMINENT 상태만 허용)
        if (this.status != GroupBuyStatus.RECRUITING && this.status != GroupBuyStatus.IMMINENT) {
            throw new CustomException(ErrorCode.GROUP_BUY_CLOSED);
        }

        // 3. Participation 엔티티 생성
        Participation newParticipation = Participation.create(
            participant,
            this,
            quantity,
            selectedDeliveryMethod,
            address,
            totalPayment
        );

        // 4. 참여자 추가, 금액 증가 및 상태 변경
        this.participations.add(newParticipation);
        increaseParticipant();
        
        // current_amount에는 택배비를 제외한 순수 재료 비용만 누적
        // (목표 금액 달성 여부는 재료 비용 기준으로 판단)
        Integer itemAmount = calculateItemAmount(quantity, selectedDeliveryMethod, totalPayment);
        increaseCurrentAmount(itemAmount);
        
        if (isTargetReached()) {
            close();
        }

        return newParticipation;
    }
    
    /**
     * 총 결제 금액에서 택배비를 제외한 순수 재료 비용 계산
     */
    private Integer calculateItemAmount(int quantity, DeliveryMethod selectedDeliveryMethod, Integer totalPayment) {
        // 택배 선택 시 택배비 제외, 직거래 선택 시 전액 반영
        if (selectedDeliveryMethod == DeliveryMethod.PARCEL && this.parcelFee != null && this.parcelFee > 0) {
            return totalPayment - this.parcelFee;
        }
        return totalPayment;
    }

    public void cancelParticipation(Participation participation) {
        // 1. 최종 상태(CLOSED, COMPLETED, CANCELLED)에서는 참여 취소 불가
        if (this.status == GroupBuyStatus.CLOSED || 
            this.status == GroupBuyStatus.COMPLETED || 
            this.status == GroupBuyStatus.CANCELLED) {
            throw new CustomException(ErrorCode.CANNOT_MODIFY_GROUP_BUY);
        }
        
        // 2. 마감 1일 전 취소 제한 검증
        if (LocalDateTime.now().isAfter(this.deadline.minusDays(1))) {
            throw new CustomException(ErrorCode.CANCELLATION_DEADLINE_PASSED);
        }

        // 3. 참여 정보 제거 (RECRUITING 또는 IMMINENT 상태에서만 가능)
        this.participations.remove(participation);
        decreaseParticipant();
    }

    public void forceRemoveParticipant(Participation participation) {
        // 최종 상태(CLOSED, COMPLETED, CANCELLED)에서는 참여자 제거 불가
        if (this.status == GroupBuyStatus.CLOSED || 
            this.status == GroupBuyStatus.COMPLETED || 
            this.status == GroupBuyStatus.CANCELLED) {
            throw new CustomException(ErrorCode.CANNOT_MODIFY_GROUP_BUY);
        }
        
        // 주최자 권한으로 강제 탈퇴 처리 (RECRUITING 또는 IMMINENT 상태에서만 가능, 마감 기한 제한 없음)
        this.participations.remove(participation);
        decreaseParticipant();
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

    public void increaseCurrentAmount(Integer amount) {
        if (amount == null || amount < 0) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        // 0원인 경우 금액 증가 없이 정상 처리
        if (amount == 0) {
            return;
        }
        this.currentAmount += amount;
    }

    public void decreaseCurrentAmount(Integer amount) {
        if (amount == null || amount < 0) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        // 0원인 경우 금액 감소 없이 정상 처리
        if (amount == 0) {
            return;
        }
        if (this.currentAmount - amount < 0) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        this.currentAmount -= amount;
    }

    public boolean isTargetReached() {
        return this.currentHeadcount >= this.targetHeadcount;
    }

    public boolean isTargetAmountReached() {
        return this.currentAmount >= this.targetAmount;
    }

    public int getAchievementRate() {
        if (this.targetAmount == 0) {
            return 0;
        }
        return (int) ((double) this.currentAmount / this.targetAmount * 100);
    }

    /**
     * 목표 인원 달성 시 호출 - COMPLETED 상태로 변경 (후기 작성 가능)
     */
    public void close() {
        this.status = GroupBuyStatus.COMPLETED;
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
        return (this.status == GroupBuyStatus.RECRUITING || this.status == GroupBuyStatus.IMMINENT) && !isTargetReached();
    }

    public boolean isRecipeBased() {
        return this.recipeApiId != null;
    }

    public Boolean getParticipantListPublic() {
        return isParticipantListPublic;
    }
}
