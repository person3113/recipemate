package com.recipemate.domain.groupbuy.dto;

import com.recipemate.global.common.DeliveryMethod;
import com.recipemate.global.common.GroupBuyCategory;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor
@Builder
public class UpdateGroupBuyRequest {

    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 100, message = "제목은 100자 이내여야 합니다")
    private String title;

    @NotBlank(message = "내용은 필수입니다")
    @Size(max = 2000, message = "내용은 2000자 이내여야 합니다")
    private String content;

    @NotNull(message = "카테고리는 필수입니다")
    private GroupBuyCategory category;

    @NotNull(message = "총 금액은 필수입니다")
    @Min(value = 0, message = "총 금액은 0원 이상이어야 합니다")
    private Integer targetAmount;

    @NotNull(message = "목표 인원은 필수입니다")
    @Min(value = 2, message = "목표 인원은 2명 이상이어야 합니다")
    @Max(value = 100, message = "목표 인원은 100명 이하여야 합니다")
    private Integer targetHeadcount;

    @NotNull(message = "마감일은 필수입니다")
    @Future(message = "마감일은 현재보다 이후여야 합니다")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime deadline;

    @NotNull(message = "수령 방법은 필수입니다")
    private DeliveryMethod deliveryMethod;

    @Size(max = 200, message = "직거래 장소는 200자 이내여야 합니다")
    private String meetupLocation;

    private Double latitude;

    private Double longitude;

    @Min(value = 0, message = "택배비는 0원 이상이어야 합니다")
    private Integer parcelFee;

    private Boolean isParticipantListPublic;
    
    // 재료 목록 (레시피 기반 공구 수정 시 사용)
    @Builder.Default
    private List<SelectedIngredient> selectedIngredients = new ArrayList<>();
    
    // JSON 형식의 재료 데이터 (폼 필드 개수 제한 우회)
    private String selectedIngredientsJson;
    
    // 기존 이미지 URL 목록 (유지할 이미지)
    @Builder.Default
    private List<String> existingImageUrls = new ArrayList<>();
    
    // 새로 업로드할 이미지 파일
    @Builder.Default
    private List<MultipartFile> images = new ArrayList<>();
}
