package com.recipemate.domain.recipe.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 식품안전나라 API의 단일 레시피 응답 DTO
 * COOKRCP01 서비스의 row 객체를 매핑
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CookRecipeResponse {

    // 기본 정보
    @JsonProperty("RCP_SEQ")
    private String rcpSeq;

    @JsonProperty("RCP_NM")
    private String rcpNm;

    @JsonProperty("RCP_WAY2")
    private String rcpWay2;

    @JsonProperty("RCP_PAT2")
    private String rcpPat2;

    @JsonProperty("INFO_WGT")
    private String infoWgt;

    // 영양 정보
    @JsonProperty("INFO_ENG")
    private String infoEng;

    @JsonProperty("INFO_CAR")
    private String infoCar;

    @JsonProperty("INFO_PRO")
    private String infoPro;

    @JsonProperty("INFO_FAT")
    private String infoFat;

    @JsonProperty("INFO_NA")
    private String infoNa;

    // 메타 정보
    @JsonProperty("HASH_TAG")
    private String hashTag;

    @JsonProperty("ATT_FILE_NO_MAIN")
    private String attFileNoMain;

    @JsonProperty("ATT_FILE_NO_MK")
    private String attFileNoMk;

    @JsonProperty("RCP_PARTS_DTLS")
    private String rcpPartsDtls;

    @JsonProperty("RCP_NA_TIP")
    private String rcpNaTip;

    // 조리 단계 01-20 (텍스트)
    @JsonProperty("MANUAL01")
    private String manual01;
    @JsonProperty("MANUAL02")
    private String manual02;
    @JsonProperty("MANUAL03")
    private String manual03;
    @JsonProperty("MANUAL04")
    private String manual04;
    @JsonProperty("MANUAL05")
    private String manual05;
    @JsonProperty("MANUAL06")
    private String manual06;
    @JsonProperty("MANUAL07")
    private String manual07;
    @JsonProperty("MANUAL08")
    private String manual08;
    @JsonProperty("MANUAL09")
    private String manual09;
    @JsonProperty("MANUAL10")
    private String manual10;
    @JsonProperty("MANUAL11")
    private String manual11;
    @JsonProperty("MANUAL12")
    private String manual12;
    @JsonProperty("MANUAL13")
    private String manual13;
    @JsonProperty("MANUAL14")
    private String manual14;
    @JsonProperty("MANUAL15")
    private String manual15;
    @JsonProperty("MANUAL16")
    private String manual16;
    @JsonProperty("MANUAL17")
    private String manual17;
    @JsonProperty("MANUAL18")
    private String manual18;
    @JsonProperty("MANUAL19")
    private String manual19;
    @JsonProperty("MANUAL20")
    private String manual20;

    // 조리 단계 이미지 01-20
    @JsonProperty("MANUAL_IMG01")
    private String manualImg01;
    @JsonProperty("MANUAL_IMG02")
    private String manualImg02;
    @JsonProperty("MANUAL_IMG03")
    private String manualImg03;
    @JsonProperty("MANUAL_IMG04")
    private String manualImg04;
    @JsonProperty("MANUAL_IMG05")
    private String manualImg05;
    @JsonProperty("MANUAL_IMG06")
    private String manualImg06;
    @JsonProperty("MANUAL_IMG07")
    private String manualImg07;
    @JsonProperty("MANUAL_IMG08")
    private String manualImg08;
    @JsonProperty("MANUAL_IMG09")
    private String manualImg09;
    @JsonProperty("MANUAL_IMG10")
    private String manualImg10;
    @JsonProperty("MANUAL_IMG11")
    private String manualImg11;
    @JsonProperty("MANUAL_IMG12")
    private String manualImg12;
    @JsonProperty("MANUAL_IMG13")
    private String manualImg13;
    @JsonProperty("MANUAL_IMG14")
    private String manualImg14;
    @JsonProperty("MANUAL_IMG15")
    private String manualImg15;
    @JsonProperty("MANUAL_IMG16")
    private String manualImg16;
    @JsonProperty("MANUAL_IMG17")
    private String manualImg17;
    @JsonProperty("MANUAL_IMG18")
    private String manualImg18;
    @JsonProperty("MANUAL_IMG19")
    private String manualImg19;
    @JsonProperty("MANUAL_IMG20")
    private String manualImg20;

    /**
     * 조리 단계를 파싱하여 리스트로 반환
     * 빈 값이나 공백은 제외
     */
    public List<ManualStep> getManualSteps() {
        List<ManualStep> steps = new ArrayList<>();
        String[] manuals = {
            manual01, manual02, manual03, manual04, manual05,
            manual06, manual07, manual08, manual09, manual10,
            manual11, manual12, manual13, manual14, manual15,
            manual16, manual17, manual18, manual19, manual20
        };
        String[] manualImages = {
            manualImg01, manualImg02, manualImg03, manualImg04, manualImg05,
            manualImg06, manualImg07, manualImg08, manualImg09, manualImg10,
            manualImg11, manualImg12, manualImg13, manualImg14, manualImg15,
            manualImg16, manualImg17, manualImg18, manualImg19, manualImg20
        };

        for (int i = 0; i < 20; i++) {
            String manual = manuals[i];
            String manualImage = manualImages[i];

            // 조리 단계 설명이 null이거나 빈 문자열이면 건너뛰기
            if (manual == null || manual.trim().isEmpty()) {
                continue;
            }

            steps.add(new ManualStep(
                i + 1,
                manual.trim(),
                (manualImage != null && !manualImage.trim().isEmpty()) ? manualImage.trim() : null
            ));
        }

        return steps;
    }

    /**
     * 재료 정보를 파싱하여 리스트로 반환
     * RCP_PARTS_DTLS는 쉼표 또는 개행 문자로 구분된 문자열
     */
    public List<String> getIngredients() {
        List<String> ingredients = new ArrayList<>();
        
        if (rcpPartsDtls == null || rcpPartsDtls.trim().isEmpty()) {
            return ingredients;
        }

        // 쉼표(,) 또는 개행(\n)을 기준으로 분리. 구분자가 여러 개 연속되어도 처리.
        String[] parts = rcpPartsDtls.split("[,\\n]+");
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                ingredients.add(trimmed);
            }
        }

        return ingredients;
    }

    /**
     * 조리 단계 정보를 담는 내부 클래스
     */
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ManualStep {
        private int stepNumber;
        private String description;
        private String imageUrl;
    }
}
