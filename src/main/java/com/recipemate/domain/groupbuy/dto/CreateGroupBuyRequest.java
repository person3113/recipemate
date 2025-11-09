package com.recipemate.domain.groupbuy.dto;

import com.recipemate.global.common.DeliveryMethod;
import com.recipemate.global.common.GroupBuyCategory;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor
@Builder
public class CreateGroupBuyRequest {

    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 100, message = "제목은 100자 이내여야 합니다")
    private String title;

    @NotBlank(message = "내용은 필수입니다")
    @Size(max = 2000, message = "내용은 2000자 이내여야 합니다")
    private String content;
    
    @Size(max = 1000, message = "재료는 1000자 이내여야 합니다")
    private String ingredients; // 재료 목록 (레시피 기반 공구에서만 사용)

    @NotNull(message = "카테고리는 필수입니다")
    private GroupBuyCategory category;

    @NotNull(message = "총 금액은 필수입니다")
    @Min(value = 0, message = "총 금액은 0원 이상이어야 합니다")
    private Integer totalPrice;

    @NotNull(message = "목표 인원은 필수입니다")
    @Min(value = 2, message = "목표 인원은 2명 이상이어야 합니다")
    @Max(value = 100, message = "목표 인원은 100명 이하여야 합니다")
    private Integer targetHeadcount;

    @NotNull(message = "마감일은 필수입니다")
    @Future(message = "마감일은 현재보다 이후여야 합니다")
    private LocalDateTime deadline;

    @NotNull(message = "수령 방법은 필수입니다")
    private DeliveryMethod deliveryMethod;

    @Size(max = 200, message = "직거래 장소는 200자 이내여야 합니다")
    private String meetupLocation;

    @Min(value = 0, message = "택배비는 0원 이상이어야 합니다")
    private Integer parcelFee;

    @Builder.Default
    private Boolean isParticipantListPublic = false;

    @Builder.Default
    private List<MultipartFile> imageFiles = new ArrayList<>();

    // Recipe fields (optional - only for recipe-based group buys)
    @Size(max = 100, message = "레시피 API ID는 100자 이내여야 합니다")
    private String recipeApiId;

    @Size(max = 200, message = "레시피 이름은 200자 이내여야 합니다")
    private String recipeName;

    @Size(max = 500, message = "레시피 이미지 URL은 500자 이내여야 합니다")
    private String recipeImageUrl;

    @Builder.Default
    private List<SelectedIngredient> selectedIngredients = new ArrayList<>();
    
    // JSON 형식의 재료 데이터 (폼 필드 개수 제한 우회)
    private String selectedIngredientsJson;
    
    /**
     * 마감일이 현재로부터 1개월 이내인지 검증
     */
    @AssertTrue(message = "마감일은 현재로부터 1개월 이내로 설정해야 합니다")
    public boolean isDeadlineWithinOneMonth() {
        if (deadline == null) {
            return true; // null은 @NotNull에서 검증
        }
        LocalDateTime maxDeadline = LocalDateTime.now().plusMonths(1);
        return !deadline.isAfter(maxDeadline);
    }
}
