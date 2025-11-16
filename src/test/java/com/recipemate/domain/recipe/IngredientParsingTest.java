//package com.recipemate.domain.recipe;
//
//import com.recipemate.domain.recipe.dto.CookRecipeResponse;
//import org.junit.jupiter.api.Test;
//
//import java.io.FileWriter;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * FoodSafety API 재료 파싱 테스트
// * 실제 API 데이터를 사용하여 파싱 결과를 검증하고 출력
// */
//public class IngredientParsingTest {
//
//    private static final String[] TEST_RECIPES = {
//        "새우 두부 계란찜||새우두부계란찜\n연두부 75g(3/4모), 칵테일새우 20g(5마리), 달걀 30g(1/2개), 생크림 13g(1큰술), 설탕 5g(1작은술), 무염버터 5g(1작은술)\n고명\n시금치 10g(3줄기)",
//        "효종갱||재료: 배추(20g), 무(10g), 느타리버섯(20g) 육수: 쇠고기(양지 50g, 갈비 50g), 양파(40g), 마늘(10g), 물(300g) 양념: 저염간장(10g)",
//        "순두부 사과 소스 오이무침||●오이무침 :\n오이 70g(1/3개), 다진 땅콩 10g(1큰술)\n●순두부사과 소스 : \n순두부 40g(1/8봉지), 사과 50g(1/3개)",
//        "방울토마토 소박이||●방울토마토 소박이 : \n방울토마토 150g(5개), 양파 10g(3×1cm), 부추 10g(5줄기)\n●양념장 : \n고춧가루 4g(1작은술), 멸치액젓 3g(2/3작은술), 다진 마늘 2.5g(1/2쪽), 매실액 2g(1/3작은술), 설탕 2g(1/3작은술), 물 2ml(1/3작은술), 통깨 약간",
//        "부추 콩가루 찜||[1인분]조선부추 50g, 날콩가루 7g(1⅓작은술)\n·양념장 : 저염간장 3g(2/3작은술), 다진 대파 5g(1작은술), 다진 마늘 2g(1/2쪽), 고춧가루 2g(1/3작은술), 요리당 2g(1/3작은술), 참기름 2g(1/3작은술), 참깨 약간",
//        "롤 삼계탕||●주재료: 마 30g(1/3개), 대추20g(10개), 은행35g(25개), 단호박 40g(1/8개), 아스파라거스 15g(1/3개), 영콘 20g(1개), 마늘 10g(2개), 쌀 200g(1컴)\n●육수 : 닭 550g(1마리), 파 15g(1큰술), 마늘 15(3개)",
//        "연두부 무순 냉국||연두부 75g, 무순 10g, 대파 5g, 청고추 3g, 홍고추 3g, 쑥갓 2g, 통깨 1g, 소금적당량[냉국 국물재료] 다시마육수 90g, 간장 4g, 맛술 2g, 식초 2g, 설탕 3g",
//        "콩비지완자탕||완자: 콩비지 50, 다진 돼지고기(기름기 없는 부위) 25, 깍두기 5, 새우젓 2.5, 계란 5, 감자전분 10, 후추 0.02, 다진 파 4, 다진 마늘 2 국물: 멸치다시마국물(물 300, 건멸치 2.5, 건다시마 2), 청경채 20, 어슷 썬 대파 5, 다진 마늘 1, 국간장 5",
//        "묵은지 밀푀유나베||[ 2인분 ] 묵은지(¼포기), 배추(6장), 깻잎(10장), 소고기(샤브샤브용, 150g), 콩나물(1줌=20g), 북어채(1줌=10g)",
//        "토마토 해물누룽지탕||2인분 기준<br>\n•[간편식 재료] 알배추(40g), 우목심(50g), 깻잎(5g), 청경채(25g), 팽이버섯(20g), 표고버섯(5g), 숙주(30g)<br>\n\n•[추가 재료] 오징어(30g), 새우(40g), 홍합(164g), 베트남 건고추(10g), 토마토(250g), 청양고추(4g), 양파(50g), 대파(흰 부분, 10g), 편 마늘(10g), 시판 누룽지(50g), 토마토 페이스트(20g), 식용유(10g), 간장(5g), 고춧가루(6g), 후추(3g), 다시팩(20g), 물(4컵=800g)",
//        "베이컨밀푀유||알배추 8장(200g), 깻잎 12장(20g), 베이컨 80g\n무 100g, 숙주 120g, 청경재 80g, 백일송이 25g\n국물 : 채소국물 8컵(표고버섯, 다시마, 무, 마늘, 대파뿌리, 양파), 맛간장 15g\n소스 : 멸치간장 15g, 와사비 10g, 레몬즙 15g\n소스 : 칠리소스 30g, 올리고당 15g, 레몬즙 15g",
//        "토마토스프파스타||홀토마토 200g, 토마토 100g, 다진양파 30g, 다진마늘 20g, 먹물파스타 25g, 치즈 25g\n파슬리가루 1g, 함초소금 1g, 올리브오일 30g\n곁들이 야채 : 양파 20g, 가지 20g, 2가지색 파프리카 20g, 애호박 20g",
//        "삼색소면||3가지색 소면(분홍, 초록, 흰색) 각 25g씩, 쪽파 20g, 청고추 20g, 홍고추 15g, 마늘 10g\n국물 : 다시마물 400g, 모시조개 200g, 볶은소금 1g, 청주 15g",
//        "저염보쌈김치||배추 17g, 배 3.0g, 대추슬라이스 0.2g, 깐밤 1.1g, 당근 1.0g \n(저염양념) 조선무 1.1g, 미나리 1.0g, 깐양파 1.1g,  파프리카 1.1g, 황설탕 0.1g, 잣 0.2g,  깐마늘 1.1g, 깐생강 0.2g, 멸치액젓 0.1g, 고춧가루 0.8g, 굵은소금 0.5g, 건표고버섯 0.1g, 국멸치 0.3g , 물 50ml",
//        "케이준 스타일 닭고기 요리||닭가슴살 100g, 식용유 8g, 파프리카가루 2g, 키엔페퍼 1g, 양파가루 1g, 마늘가루 1g, 후추 1g, 흰후춧가루 1g, 오레가노마른것 0.5g, 타임 마른것 0.5g, 바질마른것 1g, 로즈마리마른것 0.5g *선택:황설탕 1g"
//    };
//
//    @Test
//    public void testImprovedParsing() throws IOException {
//        System.out.println("=== 개선된 파싱 로직 테스트 (쉼표 + 개행) ===\n");
//
//        StringBuilder output = new StringBuilder();
//        output.append("=".repeat(80)).append("\n");
//        output.append("FoodSafety API 재료 파싱 테스트 결과 - v1 개선 버전\n");
//        output.append("=".repeat(80)).append("\n\n");
//
//        int totalIngredients = 0;
//
//        for (String testCase : TEST_RECIPES) {
//            String[] parts = testCase.split("\\|\\|");
//            String recipeName = parts[0];
//            String rcpPartsDtls = parts[1];
//
//            List<String> ingredients = parseIngredientsV1(rcpPartsDtls);
//            totalIngredients += ingredients.size();
//
//            output.append("레시피: ").append(recipeName).append("\n");
//            output.append("-".repeat(80)).append("\n");
//            output.append("원본 데이터:\n").append(rcpPartsDtls).append("\n\n");
//            output.append("파싱 결과 (").append(ingredients.size()).append("개):\n");
//
//            for (int i = 0; i < ingredients.size(); i++) {
//                output.append(String.format("  %2d. %s\n", i + 1, ingredients.get(i)));
//            }
//            output.append("\n\n");
//        }
//
//        output.append("=".repeat(80)).append("\n");
//        output.append(String.format("총 %d개 레시피, %d개 재료 파싱됨\n", TEST_RECIPES.length, totalIngredients));
//        output.append("=".repeat(80)).append("\n");
//
//        // 결과를 파일로 저장
//        String outputPath = "docs/api/testResult/parsing_result_v1.txt";
//        Files.createDirectories(Paths.get("docs/api/testResult"));
//        try (FileWriter writer = new FileWriter(outputPath)) {
//            writer.write(output.toString());
//        }
//
//        System.out.println(output.toString());
//        System.out.println("\n결과가 " + outputPath + " 에 저장되었습니다.");
//    }
//
//    /**
//     * v1 개선된 파싱 로직: 쉼표와 개행 문자를 모두 처리
//     */
//    private List<String> parseIngredientsV1(String rcpPartsDtls) {
//        List<String> ingredients = new ArrayList<>();
//
//        if (rcpPartsDtls == null || rcpPartsDtls.trim().isEmpty()) {
//            return ingredients;
//        }
//
//        // 쉼표(,) 또는 개행(\n)을 기준으로 분리
//        String[] parts = rcpPartsDtls.split("[,\\n]+");
//        for (String part : parts) {
//            String trimmed = part.trim();
//            if (!trimmed.isEmpty()) {
//                ingredients.add(trimmed);
//            }
//        }
//
//        return ingredients;
//    }
//
//    /**
//     * v2 개선된 파싱 로직: 재료명과 계량 정보를 분리
//     * "연두부 75g(3/4모)" → NAME: "연두부", MEASURE: "75g(3/4모)"
//     * "배추(20g)" → NAME: "배추", MEASURE: "20g"
//     */
//    private List<IngredientParsed> parseIngredientsV2(String rcpPartsDtls) {
//        List<IngredientParsed> ingredients = new ArrayList<>();
//
//        if (rcpPartsDtls == null || rcpPartsDtls.trim().isEmpty()) {
//            return ingredients;
//        }
//
//        // 쉼표(,) 또는 개행(\n)을 기준으로 분리
//        String[] parts = rcpPartsDtls.split("[,\\n]+");
//
//        for (String part : parts) {
//            String trimmed = part.trim();
//
//            if (trimmed.isEmpty()) {
//                continue;
//            }
//
//            String name;
//            String measure;
//
//            // 패턴 1 (우선순위 높음): "재료명 숫자..." 형식 처리
//            // 예: "연두부 75g(3/4모)", "칵테일새우 20g(5마리)"
//            java.util.regex.Pattern spacePattern = java.util.regex.Pattern.compile("^(.+?)\\s+(\\d+.*)$");
//            java.util.regex.Matcher spaceMatcher = spacePattern.matcher(trimmed);
//
//            if (spaceMatcher.matches()) {
//                // 공백으로 구분된 형식
//                name = spaceMatcher.group(1).trim();
//                measure = spaceMatcher.group(2).trim();
//
//                // 재료명이 비어있지 않은지 확인
//                if (name.isEmpty()) {
//                    name = trimmed;
//                    measure = "적당량";
//                }
//            } else {
//                // 패턴 2: "재료명(계량)" 형식 처리
//                // 예: "배추(20g)", "무(10g)"
//                java.util.regex.Pattern bracketPattern = java.util.regex.Pattern.compile("^([^(]+)\\(([^)]+)\\)$");
//                java.util.regex.Matcher bracketMatcher = bracketPattern.matcher(trimmed);
//
//                if (bracketMatcher.matches()) {
//                    // 괄호 형식: "재료명(계량)"
//                    name = bracketMatcher.group(1).trim();
//                    measure = bracketMatcher.group(2).trim();
//                } else {
//                    // 계량 정보가 없는 경우 (헤더나 라벨일 가능성)
//                    name = trimmed;
//                    measure = "적당량";
//                }
//            }
//
//            ingredients.add(new IngredientParsed(name, measure));
//        }
//
//        return ingredients;
//    }
//
//    /**
//     * 파싱된 재료 정보를 담는 클래스
//     */
//    private static class IngredientParsed {
//        private final String name;
//        private final String measure;
//
//        public IngredientParsed(String name, String measure) {
//            this.name = name;
//            this.measure = measure;
//        }
//
//        public String getName() {
//            return name;
//        }
//
//        public String getMeasure() {
//            return measure;
//        }
//
//        @Override
//        public String toString() {
//            return String.format("%-30s | %s", name, measure);
//        }
//    }
//
//    @Test
//    public void testV2Parsing() throws IOException {
//        System.out.println("=== v2 개선된 파싱 로직 테스트 (재료명/계량 분리) ===\n");
//
//        StringBuilder output = new StringBuilder();
//        output.append("=".repeat(80)).append("\n");
//        output.append("FoodSafety API 재료 파싱 테스트 결과 - v2 개선 버전\n");
//        output.append("재료명과 계량 정보를 분리하여 저장\n");
//        output.append("=".repeat(80)).append("\n\n");
//
//        int totalIngredients = 0;
//
//        for (String testCase : TEST_RECIPES) {
//            String[] parts = testCase.split("\\|\\|");
//            String recipeName = parts[0];
//            String rcpPartsDtls = parts[1];
//
//            List<IngredientParsed> ingredients = parseIngredientsV2(rcpPartsDtls);
//            totalIngredients += ingredients.size();
//
//            output.append("레시피: ").append(recipeName).append("\n");
//            output.append("-".repeat(80)).append("\n");
//            output.append("원본 데이터:\n").append(rcpPartsDtls).append("\n\n");
//            output.append("파싱 결과 (").append(ingredients.size()).append("개):\n");
//            output.append(String.format("  %-4s | %-30s | %s\n", "No", "재료명 (NAME)", "계량 (MEASURE)"));
//            output.append("  ").append("-".repeat(70)).append("\n");
//
//            for (int i = 0; i < ingredients.size(); i++) {
//                IngredientParsed ing = ingredients.get(i);
//                output.append(String.format("  %2d. | %-30s | %s\n",
//                    i + 1, ing.getName(), ing.getMeasure()));
//            }
//            output.append("\n\n");
//        }
//
//        output.append("=".repeat(80)).append("\n");
//        output.append(String.format("총 %d개 레시피, %d개 재료 파싱됨\n", TEST_RECIPES.length, totalIngredients));
//        output.append("=".repeat(80)).append("\n");
//
//        // 결과를 파일로 저장
//        String outputPath = "docs/api/testResult/parsing_result_v2.txt";
//        Files.createDirectories(Paths.get("docs/api/testResult"));
//        try (FileWriter writer = new FileWriter(outputPath)) {
//            writer.write(output.toString());
//        }
//
//        System.out.println(output.toString());
//        System.out.println("\n결과가 " + outputPath + " 에 저장되었습니다.");
//    }
//
//    @Test
//    public void testV1VsV2Comparison() throws IOException {
//        System.out.println("=== v1 vs v2 비교 리포트 ===\n");
//
//        StringBuilder output = new StringBuilder();
//        output.append("=".repeat(80)).append("\n");
//        output.append("FoodSafety API 재료 파싱 v1 vs v2 비교 리포트\n");
//        output.append("v1: 쉼표+개행 처리 (재료 통째로 저장)\n");
//        output.append("v2: 재료명/계량 분리 저장\n");
//        output.append("=".repeat(80)).append("\n\n");
//
//        for (String testCase : TEST_RECIPES) {
//            String[] parts = testCase.split("\\|\\|");
//            String recipeName = parts[0];
//            String rcpPartsDtls = parts[1];
//
//            List<String> v1Ingredients = parseIngredientsV1(rcpPartsDtls);
//            List<IngredientParsed> v2Ingredients = parseIngredientsV2(rcpPartsDtls);
//
//            output.append("레시피: ").append(recipeName).append("\n");
//            output.append("-".repeat(80)).append("\n");
//            output.append(String.format("v1: %d개, v2: %d개\n\n", v1Ingredients.size(), v2Ingredients.size()));
//
//            output.append("비교:\n");
//            int maxSize = Math.max(v1Ingredients.size(), v2Ingredients.size());
//            output.append(String.format("%-4s | %-35s | %-30s | %s\n", "No", "v1 (통째로)", "v2 NAME", "v2 MEASURE"));
//            output.append("-".repeat(80)).append("\n");
//
//            for (int i = 0; i < maxSize; i++) {
//                String v1 = i < v1Ingredients.size() ? v1Ingredients.get(i) : "";
//                String v2Name = i < v2Ingredients.size() ? v2Ingredients.get(i).getName() : "";
//                String v2Measure = i < v2Ingredients.size() ? v2Ingredients.get(i).getMeasure() : "";
//
//                output.append(String.format("%2d. | %-35s | %-30s | %s\n",
//                    i + 1,
//                    v1.length() > 35 ? v1.substring(0, 32) + "..." : v1,
//                    v2Name.length() > 30 ? v2Name.substring(0, 27) + "..." : v2Name,
//                    v2Measure));
//            }
//            output.append("\n\n");
//        }
//
//        // 결과를 파일로 저장
//        String outputPath = "docs/api/testResult/parsing_comparison_v1_v2.txt";
//        Files.createDirectories(Paths.get("docs/api/testResult"));
//        try (FileWriter writer = new FileWriter(outputPath)) {
//            writer.write(output.toString());
//        }
//
//        System.out.println(output.toString());
//        System.out.println("\n결과가 " + outputPath + " 에 저장되었습니다.");
//    }
//}
