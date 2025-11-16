package com.recipemate.domain.user.dto;

import com.recipemate.domain.groupbuy.dto.GroupBuyResponse;
import com.recipemate.global.common.BadgeType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserProfileResponseDto {
    
    private Long id;
    private String nickname;
    private String profileImageUrl;
    private Double mannerTemperature;
    private List<BadgeType> badges;
    private Page<GroupBuyResponse> hostedGroupBuys;
    
    public static UserProfileResponseDto of(Long id, String nickname, String profileImageUrl, 
                                             Double mannerTemperature, List<BadgeType> badges, 
                                             Page<GroupBuyResponse> hostedGroupBuys) {
        return new UserProfileResponseDto(id, nickname, profileImageUrl, mannerTemperature, badges, hostedGroupBuys);
    }
}
