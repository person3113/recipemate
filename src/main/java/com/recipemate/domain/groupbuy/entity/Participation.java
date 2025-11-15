package com.recipemate.domain.groupbuy.entity;

import com.recipemate.domain.user.entity.Address;
import com.recipemate.domain.user.entity.User;
import com.recipemate.global.common.BaseEntity;
import com.recipemate.global.common.DeliveryMethod;
import com.recipemate.global.common.ParticipationStatus;
import com.recipemate.global.exception.CustomException;
import com.recipemate.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "participations",
    indexes = {
        @Index(name = "idx_participation_user_id", columnList = "user_id"),
        @Index(name = "idx_participation_group_buy_id", columnList = "group_buy_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_participation_user_groupbuy", columnNames = {"user_id", "group_buy_id"})
    }
)
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Participation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_participation_user"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_buy_id", nullable = false, foreignKey = @ForeignKey(name = "fk_participation_groupbuy"))
    private GroupBuy groupBuy;

    @Column(nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeliveryMethod selectedDeliveryMethod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id", foreignKey = @ForeignKey(name = "fk_participation_address"))
    private Address address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ParticipationStatus status;

    @Column(nullable = false)
    private Integer totalPayment;

    @Column(nullable = false)
    private LocalDateTime participatedAt;

    //== 생성 메서드 ==//
    public static Participation create(User user, GroupBuy groupBuy, Integer quantity, DeliveryMethod selectedDeliveryMethod, Address address, Integer totalPayment) {
        validateCreateArgs(groupBuy, quantity, selectedDeliveryMethod, address);
        
        return Participation.builder()
            .user(user)
            .groupBuy(groupBuy)
            .quantity(quantity)
            .selectedDeliveryMethod(selectedDeliveryMethod)
            .address(address)
            .status(ParticipationStatus.PAYMENT_COMPLETED)
            .totalPayment(totalPayment)
            .participatedAt(LocalDateTime.now())
            .build();
    }

    private static void validateCreateArgs(GroupBuy groupBuy, Integer quantity, DeliveryMethod selectedDeliveryMethod, Address address) {
        // BOTH는 선택 불가 (DIRECT 또는 PARCEL만 선택 가능)
        if (selectedDeliveryMethod == DeliveryMethod.BOTH) {
            throw new CustomException(ErrorCode.INVALID_SELECTED_DELIVERY_METHOD);
        }

        // 택배 선택 시 배송지 필수
        if (selectedDeliveryMethod == DeliveryMethod.PARCEL && address == null) {
            throw new CustomException(ErrorCode.ADDRESS_REQUIRED_FOR_PARCEL);
        }

        // 수량 검증
        if (quantity == null || quantity < 1) {
            throw new CustomException(ErrorCode.INVALID_QUANTITY);
        }

        // 공구의 수령 방법과 선택한 수령 방법의 호환성 검증
        validateDeliveryMethodCompatibility(groupBuy.getDeliveryMethod(), selectedDeliveryMethod);
    }

    private static void validateDeliveryMethodCompatibility(DeliveryMethod groupBuyMethod, DeliveryMethod selectedMethod) {
        // BOTH인 경우 모든 선택 가능
        if (groupBuyMethod == DeliveryMethod.BOTH) {
            return;
        }
        
        // 직거래만 가능한 공구에 택배 선택 불가, 택배만 가능한 공구에 직거래 선택 불가
        if (groupBuyMethod != selectedMethod) {
            throw new CustomException(ErrorCode.DELIVERY_METHOD_INCOMPATIBLE);
        }
    }

    //== 수정 메서드 ==//
    public void updateQuantity(Integer quantity) {
        if (quantity == null || quantity < 1) {
            throw new CustomException(ErrorCode.INVALID_QUANTITY);
        }
        this.quantity = quantity;
    }

    public void updateDeliveryMethod(DeliveryMethod deliveryMethod) {
        if (deliveryMethod == DeliveryMethod.BOTH) {
            throw new CustomException(ErrorCode.INVALID_SELECTED_DELIVERY_METHOD);
        }
        validateDeliveryMethodCompatibility(this.groupBuy.getDeliveryMethod(), deliveryMethod);
        this.selectedDeliveryMethod = deliveryMethod;
    }

    public void updateAddress(Address address) {
        if (this.selectedDeliveryMethod == DeliveryMethod.PARCEL && address == null) {
            throw new CustomException(ErrorCode.ADDRESS_REQUIRED_FOR_PARCEL);
        }
        this.address = address;
    }

    public void cancel() {
        this.status = ParticipationStatus.CANCELLED;
    }

    //== 편의 메서드 (템플릿용) ==//
    public Long getGroupBuyId() {
        return groupBuy != null ? groupBuy.getId() : null;
    }

    public String getGroupBuyTitle() {
        return groupBuy != null ? groupBuy.getTitle() : null;
    }

    public String getGroupBuyContent() {
        return groupBuy != null ? groupBuy.getContent() : null;
    }

    public String getGroupBuyStatus() {
        return groupBuy != null && groupBuy.getStatus() != null ? groupBuy.getStatus().name() : null;
    }

    public Integer getCurrentHeadcount() {
        return groupBuy != null ? groupBuy.getCurrentHeadcount() : null;
    }

    public Integer getTargetHeadcount() {
        return groupBuy != null ? groupBuy.getTargetHeadcount() : null;
    }

    public boolean getHasReview() {
        // 템플릿 호환성을 위해 유지되는 메서드
        // 실제 후기 존재 여부는 컨트롤러에서 reviewExistsMap으로 확인
        return false;
    }
}
