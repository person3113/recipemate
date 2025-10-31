package com.recipemate.domain.groupbuy.dto;

import com.recipemate.domain.groupbuy.entity.Participation;
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

    private String nickname;
    private Double mannerTemperature;
    private LocalDateTime participatedAt;

    public static ParticipantResponse from(Participation participation) {
        return ParticipantResponse.builder()
            .nickname(participation.getUser().getNickname())
            .mannerTemperature(participation.getUser().getMannerTemperature())
            .participatedAt(participation.getParticipatedAt())
            .build();
    }
}
