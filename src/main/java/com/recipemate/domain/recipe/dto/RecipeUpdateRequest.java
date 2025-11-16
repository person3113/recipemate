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
 * 레시피 수정 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeUpdateRequest {

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

    // 기존 대표 이미지 URL (이미지를 변경하지 않을 때)
    private String existingMainImageUrl;

    // 재료 목록
    @Valid
    @Builder.Default
    private List<IngredientDto> ingredients = new ArrayList<>();

    // 조리 단계 목록
    @Valid
    @Builder.Default
    private List<StepDto> steps = new ArrayList<>();

    // JSON 형식의 재료 데이터 (Tomcat 폼 필드 개수 제한 우회)
    private String ingredientsJson;

    // JSON 형식의 조리 단계 데이터 (Tomcat 폼 필드 개수 제한 우회)
    private String stepsJson;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IngredientDto {
        private String name;
        private String measure;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StepDto {
        private String description;
    }
}
