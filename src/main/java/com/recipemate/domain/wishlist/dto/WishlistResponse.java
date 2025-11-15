package com.recipemate.domain.wishlist.dto;

import com.recipemate.domain.groupbuy.entity.GroupBuy;
import com.recipemate.domain.wishlist.entity.Wishlist;
import com.recipemate.global.common.DeliveryMethod;
import com.recipemate.global.common.GroupBuyCategory;
import com.recipemate.global.common.GroupBuyStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class WishlistResponse {

    // 찜 정보
    private Long wishlistId;
    private LocalDateTime wishedAt;
    
    // 공구 정보
    private Long groupBuyId;
    private String title;
    private GroupBuyCategory category;
    private Integer targetAmount;
    private Integer targetHeadcount;
    private Integer currentHeadcount;
    private LocalDateTime deadline;
    private DeliveryMethod deliveryMethod;
    private GroupBuyStatus status;
    
    // 주최자 정보
    private Long hostId;
    private String hostNickname;
    
    // 레시피 정보 (선택적)
    private String recipeApiId;
    private String recipeName;
    private String recipeImageUrl;
    
    // 정적 팩토리 메서드
    public static WishlistResponse from(Wishlist wishlist) {
        GroupBuy groupBuy = wishlist.getGroupBuy();
        
        return WishlistResponse.builder()
                .wishlistId(wishlist.getId())
                .wishedAt(wishlist.getWishedAt())
                .groupBuyId(groupBuy.getId())
                .title(groupBuy.getTitle())
                .category(groupBuy.getCategory())
                .targetAmount(groupBuy.getTargetAmount())
                .targetHeadcount(groupBuy.getTargetHeadcount())
                .currentHeadcount(groupBuy.getCurrentHeadcount())
                .deadline(groupBuy.getDeadline())
                .deliveryMethod(groupBuy.getDeliveryMethod())
                .status(groupBuy.getStatus())
                .hostId(groupBuy.getHost().getId())
                .hostNickname(groupBuy.getHost().getNickname())
                .recipeApiId(groupBuy.getRecipeApiId())
                .recipeName(groupBuy.getRecipeName())
                .recipeImageUrl(groupBuy.getRecipeImageUrl())
                .build();
    }
}
