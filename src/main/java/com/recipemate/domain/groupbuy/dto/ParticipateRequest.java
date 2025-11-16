package com.recipemate.domain.groupbuy.dto;

import com.recipemate.global.common.DeliveryMethod;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ParticipateRequest {

    @NotNull(message = "수령 방법은 필수입니다")
    private DeliveryMethod selectedDeliveryMethod;

    @NotNull(message = "수량은 필수입니다")
    @Min(value = 1, message = "수량은 1 이상이어야 합니다")
    private Integer quantity;

    // 택배 선택 시 필수
    private Long addressId;

    @NotNull(message = "결제 금액은 필수입니다")
    @Min(value = 0, message = "결제 금액은 0 이상이어야 합니다")
    private Integer totalPayment;
}
