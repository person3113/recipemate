package com.recipemate.domain.groupbuy.dto;

import com.recipemate.domain.groupbuy.entity.Participation;
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

    public static ParticipantResponse from(Participation participation) {
        return ParticipantResponse.builder()
            .userId(participation.getUser().getId())
            .nickname(participation.getUser().getNickname())
            .mannerTemperature(participation.getUser().getMannerTemperature())
            .participatedAt(participation.getParticipatedAt())
            .quantity(participation.getQuantity())
            .selectedDeliveryMethod(participation.getSelectedDeliveryMethod())
            .isHost(false)
            .build();
    }

    public static ParticipantResponse from(Participation participation, Long hostId) {
        return ParticipantResponse.builder()
            .userId(participation.getUser().getId())
            .nickname(participation.getUser().getNickname())
            .mannerTemperature(participation.getUser().getMannerTemperature())
            .participatedAt(participation.getParticipatedAt())
            .quantity(participation.getQuantity())
            .selectedDeliveryMethod(participation.getSelectedDeliveryMethod())
            .isHost(participation.getUser().getId().equals(hostId))
            .build();
    }
}
