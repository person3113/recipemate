package com.recipemate.domain.groupbuy.dto;

import com.recipemate.domain.groupbuy.entity.Participation;
import com.recipemate.domain.user.entity.Address;
import com.recipemate.global.common.DeliveryMethod;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ParticipantResponse {

    private Long userId;
    private String nickname;
    private Double mannerTemperature;
    private LocalDateTime participatedAt;
    private Integer quantity;
    private DeliveryMethod selectedDeliveryMethod;
    private Boolean isHost;
    
    // 배송지 정보 (택배 수령 시에만 사용)
    private String addressName;
    private String recipientName;
    private String recipientPhoneNumber;
    private String fullAddress;

    public static ParticipantResponse from(Participation participation) {
        Address address = participation.getAddress();
        
        return ParticipantResponse.builder()
            .userId(participation.getUser().getId())
            .nickname(participation.getUser().getNickname())
            .mannerTemperature(participation.getUser().getMannerTemperature())
            .participatedAt(participation.getParticipatedAt())
            .quantity(participation.getQuantity())
            .selectedDeliveryMethod(participation.getSelectedDeliveryMethod())
            .isHost(false)
            .addressName(address != null ? address.getAddressName() : null)
            .recipientName(address != null ? address.getRecipientName() : null)
            .recipientPhoneNumber(address != null ? address.getRecipientPhoneNumber() : null)
            .fullAddress(address != null ? address.getFullAddress() : null)
            .build();
    }

    public static ParticipantResponse from(Participation participation, Long hostId) {
        Address address = participation.getAddress();
        
        return ParticipantResponse.builder()
            .userId(participation.getUser().getId())
            .nickname(participation.getUser().getNickname())
            .mannerTemperature(participation.getUser().getMannerTemperature())
            .participatedAt(participation.getParticipatedAt())
            .quantity(participation.getQuantity())
            .selectedDeliveryMethod(participation.getSelectedDeliveryMethod())
            .isHost(participation.getUser().getId().equals(hostId))
            .addressName(address != null ? address.getAddressName() : null)
            .recipientName(address != null ? address.getRecipientName() : null)
            .recipientPhoneNumber(address != null ? address.getRecipientPhoneNumber() : null)
            .fullAddress(address != null ? address.getFullAddress() : null)
            .build();
    }
}
