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
     * 식품안전나라의 RCP_PARTS_DTLS 파싱 (고도화 버전)
     * 비정형 데이터를 처리하기 위한 4단계 파싱 전략:
     * 1. 전처리 (HTML 태그 및 섹션 마커 정규화)
     * 2. 줄바꿈 기반 1차 분리
     * 3. 괄호 밖 쉼표 기반 2차 분리
     * 4. 패턴 매칭으로 재료명/용량 추출
     */
    private List<IngredientWithMeasure> parseIngredients(String rcpPartsDtls) {
        List<IngredientWithMeasure> ingredients = new ArrayList<>();

        if (rcpPartsDtls == null || rcpPartsDtls.isEmpty()) {
            return ingredients;
        }

        // 1단계: 전처리 (Normalization)
        String normalized = normalizeIngredientString(rcpPartsDtls);

        // 2단계: 줄바꿈 기반 1차 분리
        String[] lines = normalized.split("\n");

        for (String line : lines) {
            // 3단계: 각 라인에서 괄호 밖 쉼표 기반 2차 분리
            List<String> ingredientTokens = splitByCommaOutsideParentheses(line);

            for (String token : ingredientTokens) {
                String trimmed = token.trim();
                if (trimmed.isEmpty() || isNonIngredientText(trimmed)) {
                    continue;
                }

                // 4단계: 패턴 매칭으로 재료명/용량 추출 (반복 추출 방식)
                List<IngredientWithMeasure> extractedIngredients = extractIngredientsFromToken(trimmed);
                ingredients.addAll(extractedIngredients);
            }
        }

        return ingredients;
    }

    /**
     * 1단계: 비정형 문자열 전처리
     * - HTML 태그 제거/변환
     * - 다양한 섹션 마커를 줄바꿈으로 변환
     * - 불필요한 공백 정리
     */
    private String normalizeIngredientString(String input) {
        String result = input;

        // HTML 태그를 줄바꿈으로 변환
        result = result.replaceAll("<br\\s*/?>", "\n");
        result = result.replaceAll("<[^>]+>", ""); // 나머지 HTML 태그 제거

        // 대괄호 처리: 대괄호와 그 안의 레이블 부분 제거, 하지만 재료는 유지
        // 예: "[소스소개 단촛물:물60g, 식초 1.5g]" 처리
        // 1) 대괄호 여는 부분부터 콜론까지 제거 (콜론이 있는 경우)
        result = result.replaceAll("\\[([^:\\]]+):", "\n");
        // 2) 남은 단순 여는 대괄호와 라벨 제거 (예: "[주재료", "[소스1", "[ 2인분 ]")
        //    대괄호 안의 모든 내용을 제거하도록 개선
        result = result.replaceAll("\\[[^\\]]*\\]", "\n");
        // 3) 남은 닫는 대괄호 제거 (콜론 패턴에서 처리한 것들)
        result = result.replaceAll("\\]", "");
        
        // 특수 기호로 시작하는 섹션 (•, ●, -, *, > 등)
        // 특수문자 + 콜론이 있으면 콜론까지 제거, 콜론이 없으면 특수문자 다음 한글단어까지 제거
        result = result.replaceAll("[•●○◆◇■□▲△▶◀\\-*>]\\s*[^:\n]+:\\s*", "\n");
        result = result.replaceAll("[•●○◆◇■□▲△▶◀\\-*>]\\s*[가-힣]+\\s+", "\n");
        
        // "크림치즈 >" 같이 단어 뒤에 > 마커가 있는 경우 - 마커와 그 뒤 공백 제거하고 줄바꿈으로
        result = result.replaceAll("\\s*>\\s+", "\n");
        
        // "XX양념:", "XX장:", "XX소스:" 패턴 - 콜론까지 제거
        // 단, 줄의 시작이거나 줄바꿈 직후에만 매칭 (중간에 있는 경우 제외)
        result = result.replaceAll("(?:^|\\n)([가-힣\\s]*)(양념|장|소스|드레싱|재료|주재료|육수|반죽|고명|곁들이|앙\\s*금)\\s*:\\s*", "\n");
        
        // 섹션 마커 패턴 추가 처리
        // "완자:", "국물:", "소스1", "소스2" 등
        result = result.replaceAll("(?:^|\\n)([가-힣A-Za-z]+[0-9]*)\\s*:\\s*", "\n");
        
        // 공백 + "재료", "소스" 등 단독 섹션 마커 단어 제거
        // 예: "재료 검은깨" -> "검은깨"
        result = result.replaceAll("(?:^|\\n)(재료|소스|양념|드레싱|주재료)\\s+", "\n");
        
        // 온점 뒤에 공백이 있으면 구분자로 처리 (예: "1g. 마늘기름" -> "1g\n마늘기름")
        result = result.replaceAll("\\.\\.+", "."); // 연속된 온점 정리
        result = result.replaceAll("\\.\\s+(?=[가-힣A-Za-z])", "\n");
        
        // 괄호 잔여물 정리 - 숫자+단위 뒤에 붙은 닫는 괄호만 있는 경우 제거
        // 예: "무 35g(1/20개)" 처리 후 "개)" 남은 것 제거
        result = result.replaceAll("([가-힣A-Za-z]+)\\)\\s+", "$1 ");

        // 연속된 줄바꿈을 하나로
        result = result.replaceAll("\n+", "\n");
        
        // 앞뒤 공백 제거
        result = result.trim();

        return result;
    }

    /**
     * 3단계: 괄호 밖의 쉼표만 구분자로 사용하여 분리
     * 예: "닭고기(가슴살, 25g), 양파 10g" -> ["닭고기(가슴살, 25g)", "양파 10g"]
     */
    private List<String> splitByCommaOutsideParentheses(String line) {
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int parenthesesDepth = 0;

        for (char c : line.toCharArray()) {
            if (c == '(') {
                parenthesesDepth++;
                current.append(c);
            } else if (c == ')') {
                parenthesesDepth--;
                current.append(c);
            } else if (c == ',' && parenthesesDepth == 0) {
                // 괄호 밖의 쉼표 -> 분리
                String token = current.toString().trim();
                if (!token.isEmpty()) {
                    tokens.add(token);
                }
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }

        // 마지막 토큰 추가
        String token = current.toString().trim();
        if (!token.isEmpty()) {
            tokens.add(token);
        }

        return tokens;
    }

    /**
     * 재료가 아닌 텍스트인지 판별
     * (예: "1인분 기준", "5인분" 등)
     */
    private boolean isNonIngredientText(String text) {
        // 너무 짧은 텍스트
        if (text.length() < 2) {
            return true;
        }

        // "1인분 기준", "5인분", "기준" 등
        if (text.matches("^\\d*인분.*$") || text.equals("기준")) {
            return true;
        }

        return false;
    }

    /**
     * 4단계: 패턴 매칭으로 재료명과 용량 추출 (반복 추출 방식)
     * 하나의 토큰에서 여러 재료를 반복적으로 찾아 추출
     * 예: "대파 2.5g 다진 마늘 1.25g" -> ["대파 2.5g", "다진 마늘 1.25g"]
     */
    private List<IngredientWithMeasure> extractIngredientsFromToken(String text) {
        List<IngredientWithMeasure> ingredients = new ArrayList<>();
        
        if (text == null || text.trim().isEmpty()) {
            return ingredients;
        }
        
        String trimmed = text.trim();
        
        // 패턴: "재료명 + 용량" 반복 추출
        // 재료명: 한글/영문/공백 (괄호는 제외하여 괄호 안의 용량에서 멈추도록)
        // 용량: 숫자(소수점 포함) + 단위(g, ml, kg, 큰술, 작은술 등) + 공백 또는 끝
        // 예: "대파 2.5g 다진 마늘 1.25g" -> ["대파", "2.5g"], ["다진 마늘", "1.25g"]
        Pattern ingredientPattern = Pattern.compile(
            "([가-힣A-Za-z\\s]+?)\\s*\\(?(\\d+(?:\\.\\d+)?(?:cc|ml|g|kg|L|개|마리|쪽|개|큰술|작은술|T|t|Ts|ts|컵|공기|포기|줌|cm|장|대|모|봉지)(?:\\([^)]*\\))?)\\)?"
        );
        
        Matcher matcher = ingredientPattern.matcher(trimmed);
        
        // 패턴 매칭이 성공한 경우 반복 추출
        while (matcher.find()) {
            String name = matcher.group(1).trim();
            String measure = matcher.group(2).trim();
            
            // measure에서 괄호 및 부가 정보 제거 - 숫자+단위만 남김
            // 예: "75g(3/4모)" -> "75g", "15g(1큰술)" -> "15g"
            measure = cleanMeasure(measure);
            
            // 이름에서 불필요한 마커 제거 (콜론, 특수문자 등)
            name = name.replaceAll(":$", "").trim();
            name = name.replaceAll("^[*>]+", "").trim(); // 별표, 화살표 제거
            
            // 유효한 재료명이 있는 경우만 추가 (최소 1글자 이상)
            if (!name.isEmpty() && !measure.isEmpty()) {
                ingredients.add(new IngredientWithMeasure(name, measure));
            }
        }
        
        // 반복 추출이 실패한 경우 (패턴이 매칭되지 않음) - 기존 단일 추출 방식 사용
        if (ingredients.isEmpty()) {
            IngredientWithMeasure fallback = extractSingleIngredient(trimmed);
            if (fallback != null) {
                ingredients.add(fallback);
            }
        }
        
        return ingredients;
    }
    
    /**
     * measure 필드에서 숫자+단위만 추출 (괄호 내용 제거)
     * 예: "75g(3/4모)" -> "75g", "15g(1큰술)" -> "15g", "200cc." -> "200cc"
     */
    private String cleanMeasure(String measure) {
        if (measure == null || measure.isEmpty()) {
            return "적당량";
        }
        
        // 괄호와 그 안의 내용 제거 (닫는 괄호가 있는 경우)
        String cleaned = measure.replaceAll("\\([^)]*\\)", "");
        
        // 닫히지 않은 여는 괄호 제거 (예: "75g(" -> "75g")
        cleaned = cleaned.replaceAll("\\(.*$", "");
        
        // 온점, 쉼표 등 끝의 구두점 제거
        cleaned = cleaned.replaceAll("[.,;:]+$", "");
        
        // 공백 정리
        cleaned = cleaned.trim();
        
        // 빈 문자열이면 "적당량" 반환
        return cleaned.isEmpty() ? "적당량" : cleaned;
    }
    
    /**
     * 단일 재료 추출 (fallback 메서드)
     * 반복 추출이 실패한 경우에 사용
     */
    private IngredientWithMeasure extractSingleIngredient(String text) {
        // 패턴 1: "재료명(용량)" - 괄호 안에 용량이 있는 경우
        Pattern pattern1 = Pattern.compile("^(.+?)\\s*\\(([^)]+)\\)\\s*$");
        Matcher matcher1 = pattern1.matcher(text);
        if (matcher1.matches()) {
            String name = matcher1.group(1).trim();
            String measure = matcher1.group(2).trim();
            
            // 괄호 안에 쉼표가 있으면 마지막 부분만 용량으로 추출
            if (measure.contains(",")) {
                String[] parts = measure.split(",");
                String lastPart = parts[parts.length - 1].trim();
                
                // 마지막 부분이 숫자로 시작하면 용량으로 간주
                if (lastPart.matches("^\\d+.*")) {
                    StringBuilder nameBuilder = new StringBuilder(name);
                    nameBuilder.append("(");
                    for (int i = 0; i < parts.length - 1; i++) {
                        if (i > 0) nameBuilder.append(",");
                        nameBuilder.append(parts[i].trim());
                    }
                    nameBuilder.append(")");
                    // measure도 cleanMeasure 적용
                    return new IngredientWithMeasure(nameBuilder.toString(), cleanMeasure(lastPart));
                }
            }
            
            // 괄호 전체가 용량인 경우
            if (measure.matches("^\\d+.*")) {
                // measure에 cleanMeasure 적용하여 괄호 내 부가정보 제거
                return new IngredientWithMeasure(name, cleanMeasure(measure));
            }
            
            // 괄호 안이 용량이 아니면 전체를 이름으로
            return new IngredientWithMeasure(text.replaceAll("\\s+", " "), "적당량");
        }

        // 패턴 2: "재료명 용량" - 공백으로 구분, 용량은 숫자로 시작
        Pattern pattern2 = Pattern.compile("^(.+?)\\s+(\\d+.*)$");
        Matcher matcher2 = pattern2.matcher(text);
        if (matcher2.matches()) {
            String name = matcher2.group(1).trim();
            String measure = matcher2.group(2).trim();
            // measure에 cleanMeasure 적용
            return new IngredientWithMeasure(name, cleanMeasure(measure));
        }

        // 패턴 3: 용량 정보 없음 - 전체를 재료명으로
        return new IngredientWithMeasure(text.trim(), "적당량");
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
