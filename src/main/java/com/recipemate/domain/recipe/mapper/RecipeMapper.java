package com.recipemate.domain.recipe.mapper;

import com.recipemate.domain.recipe.dto.CookRecipeResponse;
import com.recipemate.domain.recipe.dto.MealResponse;
import com.recipemate.domain.recipe.entity.Recipe;
import com.recipemate.domain.recipe.entity.RecipeIngredient;
import com.recipemate.domain.recipe.entity.RecipeSource;
import com.recipemate.domain.recipe.entity.RecipeStep;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * API 응답을 Recipe 엔티티로 변환하는 Mapper
 */
@Slf4j
@Component
public class RecipeMapper {

    /**
     * TheMealDB API 응답을 Recipe 엔티티로 변환
     */
    public Recipe toEntity(MealResponse mealResponse) {
        if (mealResponse == null) {
            return null;
        }

        Recipe recipe = Recipe.builder()
                .title(mealResponse.getName() != null ? mealResponse.getName().trim() : null)
                .fullImageUrl(mealResponse.getThumbnail())
                .thumbnailImageUrl(mealResponse.getThumbnail())
                .category(mealResponse.getCategory())
                .area(mealResponse.getArea())
                .sourceApi(RecipeSource.MEAL_DB)
                .sourceApiId(mealResponse.getId())
                .youtubeUrl(nullIfEmpty(mealResponse.getYoutubeUrl()))
                .sourceUrl(nullIfEmpty(mealResponse.getSourceUrl()))
                .lastSyncedAt(LocalDateTime.now())
                .build();

        // 재료 추가
        List<MealResponse.IngredientInfo> ingredientInfos = mealResponse.getIngredients();
        for (MealResponse.IngredientInfo info : ingredientInfos) {
            RecipeIngredient ingredient = RecipeIngredient.builder()
                    .name(info.getName())
                    .measure(info.getMeasure())
                    .build();
            recipe.addIngredient(ingredient);
        }

        // 조리 단계 파싱 및 추가
        List<RecipeStep> steps = parseInstructionsToSteps(mealResponse.getInstructions());
        for (RecipeStep step : steps) {
            recipe.addStep(step);
        }

        return recipe;
    }

    /**
     * 식품안전나라 API 응답을 Recipe 엔티티로 변환
     */
    public Recipe toEntity(CookRecipeResponse cookResponse) {
        if (cookResponse == null) {
            return null;
        }

        Recipe recipe = Recipe.builder()
                .title(cookResponse.getRcpNm() != null ? cookResponse.getRcpNm().trim() : null)
                .fullImageUrl(cookResponse.getAttFileNoMk())
                .thumbnailImageUrl(cookResponse.getAttFileNoMain())
                .category(cookResponse.getRcpPat2())
                .area(null) // 식품안전나라는 area 정보 없음
                .sourceApi(RecipeSource.FOOD_SAFETY)
                .sourceApiId(cookResponse.getRcpSeq())
                .calories(parseInteger(cookResponse.getInfoEng()))
                .carbohydrate(parseInteger(cookResponse.getInfoCar()))
                .protein(parseInteger(cookResponse.getInfoPro()))
                .fat(parseInteger(cookResponse.getInfoFat()))
                .sodium(parseInteger(cookResponse.getInfoNa()))
                .servingSize(cookResponse.getInfoWgt())
                .tips(cookResponse.getRcpNaTip())
                .lastSyncedAt(LocalDateTime.now())
                .build();

        // 재료 파싱 및 추가
        List<IngredientWithMeasure> parsedIngredients = parseIngredients(cookResponse.getRcpPartsDtls());
        for (IngredientWithMeasure parsed : parsedIngredients) {
            RecipeIngredient ingredient = RecipeIngredient.builder()
                    .name(parsed.getName())
                    .measure(parsed.getMeasure())
                    .build();
            recipe.addIngredient(ingredient);
        }

        // 조리 단계 추가
        List<CookRecipeResponse.ManualStep> manualSteps = cookResponse.getManualSteps();
        for (CookRecipeResponse.ManualStep manual : manualSteps) {
            RecipeStep step = RecipeStep.builder()
                    .stepNumber(manual.getStepNumber())
                    .description(manual.getDescription())
                    .imageUrl(manual.getImageUrl())
                    .build();
            recipe.addStep(step);
        }

        return recipe;
    }

    /**
     * TheMealDB의 strInstructions를 RecipeStep으로 파싱
     * 줄바꿈(\r\n 또는 \n) 기준으로 분리
     */
    private List<RecipeStep> parseInstructionsToSteps(String instructions) {
        List<RecipeStep> steps = new ArrayList<>();

        if (instructions == null || instructions.isEmpty()) {
            return steps;
        }

        // 줄바꿈으로 분리
        String[] lines = instructions.split("\\r?\\n");
        int stepNumber = 1;

        for (String line : lines) {
            String trimmed = line.trim();
            
            // 빈 줄이거나 너무 짧은 줄은 건너뛰기
            if (trimmed.isEmpty() || trimmed.length() < 10) {
                continue;
            }

            // "STEP 1:", "1.", "Step 1:" 등의 패턴 제거
            trimmed = trimmed.replaceFirst("^(?i)(step\\s*\\d+[:.)]?|\\d+[:.)]?)\\s*", "");

            steps.add(RecipeStep.builder()
                    .stepNumber(stepNumber++)
                    .description(trimmed)
                    .imageUrl(null)
                    .build());
        }

        // 파싱된 단계가 없으면 전체를 하나의 단계로
        if (steps.isEmpty()) {
            steps.add(RecipeStep.builder()
                    .stepNumber(1)
                    .description(instructions.trim())
                    .imageUrl(null)
                    .build());
        }

        return steps;
    }

    /**
     * 식품안전나라의 RCP_PARTS_DTLS 파싱
     * 형식: "재료명(계량), 재료명(계량), ..." 또는 "재료명 계량, 재료명 계량, ..."
     */
    private List<IngredientWithMeasure> parseIngredients(String rcpPartsDtls) {
        List<IngredientWithMeasure> ingredients = new ArrayList<>();

        if (rcpPartsDtls == null || rcpPartsDtls.isEmpty()) {
            return ingredients;
        }

        // 쉼표로 분리
        String[] parts = rcpPartsDtls.split(",");

        // 패턴 1: "재료명(계량)" 형식
        Pattern pattern1 = Pattern.compile("^([^(]+)\\(([^)]+)\\)$");
        // 패턴 2: "재료명 계량" 형식 (공백으로 구분)
        Pattern pattern2 = Pattern.compile("^(.+?)\\s+(\\d+.*)$");

        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) {
                continue;
            }

            String name;
            String measure;

            // 패턴 1 시도: "재료명(계량)"
            Matcher matcher1 = pattern1.matcher(trimmed);
            if (matcher1.matches()) {
                name = matcher1.group(1).trim();
                measure = matcher1.group(2).trim();
            }
            // 패턴 2 시도: "재료명 계량"
            else {
                Matcher matcher2 = pattern2.matcher(trimmed);
                if (matcher2.matches()) {
                    name = matcher2.group(1).trim();
                    measure = matcher2.group(2).trim();
                } else {
                    // 계량 정보가 없는 경우
                    name = trimmed;
                    measure = "적당량";
                }
            }

            ingredients.add(new IngredientWithMeasure(name, measure));
        }

        return ingredients;
    }

    /**
     * 문자열을 Integer로 파싱 (실패 시 null)
     */
    private Integer parseInteger(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            // 소수점이 있는 경우 반올림
            return (int) Math.round(Double.parseDouble(value.trim()));
        } catch (NumberFormatException e) {
            log.warn("Failed to parse integer: {}", value);
            return null;
        }
    }

    /**
     * 빈 문자열을 null로 변환 (유효한 값이 있으면 trim하여 반환)
     */
    private String nullIfEmpty(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    /**
     * 재료명과 계량 정보를 담는 내부 클래스
     */
    private static class IngredientWithMeasure {
        private final String name;
        private final String measure;

        public IngredientWithMeasure(String name, String measure) {
            this.name = name;
            this.measure = measure;
        }

        public String getName() {
            return name;
        }

        public String getMeasure() {
            return measure;
        }
    }
}
