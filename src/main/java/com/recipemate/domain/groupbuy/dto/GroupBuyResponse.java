package com.recipemate.domain.groupbuy.dto;

import com.recipemate.global.common.GroupBuyStatus;
import com.recipemate.domain.groupbuy.entity.GroupBuy;
import com.recipemate.global.common.DeliveryMethod;
import com.recipemate.global.common.GroupBuyCategory;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class GroupBuyResponse {

    private Long id;
    private String title;
    private String content;
    private String ingredients; // 재료 목록 (별도 필드)
    private GroupBuyCategory category;
    private Integer totalPrice;
    private Integer targetHeadcount;
    private Integer currentHeadcount;
    private LocalDateTime deadline;
    private DeliveryMethod deliveryMethod;
    private String meetupLocation;
    private Integer parcelFee;
    private Boolean isParticipantListPublic;
    private GroupBuyStatus status;
    
    // 주최자 정보
    private Long hostId;
    private String hostNickname;
    private Double hostMannerTemperature;
    
    // 레시피 정보 (선택적)
    private String recipeApiId;
    private String recipeName;
    private String recipeImageUrl;
    
    // 이미지 목록
    private List<String> imageUrls;
    
    // 타임스탬프
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private boolean deletable;

    // 정적 팩토리 메서드
    public static GroupBuyResponse from(GroupBuy groupBuy, List<String> imageUrls) {
        return GroupBuyResponse.builder()
                .id(groupBuy.getId())
                .title(groupBuy.getTitle())
                .content(groupBuy.getContent())
                .ingredients(groupBuy.getIngredients()) // 재료 목록 추가
                .category(groupBuy.getCategory())
                .totalPrice(groupBuy.getTotalPrice())
                .targetHeadcount(groupBuy.getTargetHeadcount())
                .currentHeadcount(groupBuy.getCurrentHeadcount())
                .deadline(groupBuy.getDeadline())
                .deliveryMethod(groupBuy.getDeliveryMethod())
                .meetupLocation(groupBuy.getMeetupLocation())
                .parcelFee(groupBuy.getParcelFee())
                .isParticipantListPublic(groupBuy.getParticipantListPublic())
                .status(groupBuy.getStatus())
                .hostId(groupBuy.getHost().getId())
                .hostNickname(groupBuy.getHost().getNickname())
                .hostMannerTemperature(groupBuy.getHost().getMannerTemperature())
                .recipeApiId(groupBuy.getRecipeApiId())
                .recipeName(groupBuy.getRecipeName())
                .recipeImageUrl(groupBuy.getRecipeImageUrl())
                .imageUrls(imageUrls)
                .createdAt(groupBuy.getCreatedAt())
                .updatedAt(groupBuy.getUpdatedAt())
                .deletable(groupBuy.isDeletable())
                .build();
    }
}
