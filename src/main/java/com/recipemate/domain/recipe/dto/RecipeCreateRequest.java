package com.recipemate.domain.recipe.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * 레시피 생성 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeCreateRequest {

    @NotBlank(message = "레시피 제목을 입력해주세요")
    @Size(max = 200, message = "제목은 200자 이내로 입력해주세요")
    private String title;

    private String category;  // 카테고리 (예: 한식, 양식, 중식 등)

    private String area;  // 지역/국가


    private String tips;  // 저감 조리법 팁

    private String youtubeUrl;  // YouTube 링크

    private String sourceUrl;  // 참고 링크

    // 대표 이미지 파일 (업로드)
    private MultipartFile mainImage;

    // 재료 목록
    @Valid
    @NotNull(message = "재료를 최소 1개 이상 입력해주세요")
    @Size(min = 1, message = "재료를 최소 1개 이상 입력해주세요")
    @Builder.Default
    private List<IngredientDto> ingredients = new ArrayList<>();

    // 조리 단계 목록
    @Valid
    @NotNull(message = "조리 단계를 최소 1개 이상 입력해주세요")
    @Size(min = 1, message = "조리 단계를 최소 1개 이상 입력해주세요")
    @Builder.Default
    private List<StepDto> steps = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IngredientDto {
        @NotBlank(message = "재료명을 입력해주세요")
        private String name;

        @NotBlank(message = "재료 분량을 입력해주세요")
        private String measure;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StepDto {
        @NotBlank(message = "조리 단계 설명을 입력해주세요")
        private String description;
    }
}
