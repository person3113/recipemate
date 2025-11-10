package com.recipemate.domain.recipe.mapper;

import com.recipemate.domain.recipe.dto.CookRecipeResponse;
import com.recipemate.domain.recipe.entity.Recipe;
import com.recipemate.domain.recipe.entity.RecipeIngredient;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Regex 디버깅용 테스트
 */
public class RecipeMapperDebugTest {

    @Test
    public void testRegexPattern() {
        String text = "대파 2.5g 다진 마늘 1.25g";
        
        System.out.println("Testing text: " + text);
        
        // 패턴 1: 원본
        System.out.println("\n--- Pattern 1 (original) ---");
        Pattern pattern1 = Pattern.compile(
            "([가-힣A-Za-z()\\s]+?)\\s+(\\d+(?:[^가-힣A-Za-z\\d]|\\d)*?)(?=\\s+[가-힣A-Za-z]|$)"
        );
        testPattern(pattern1, text);
        
        // 패턴 2: 단순 버전
        System.out.println("\n--- Pattern 2 (simple) ---");
        Pattern pattern2 = Pattern.compile("([가-힣A-Za-z()\\s]+?)\\s+(\\d+[^\\s가-힣A-Za-z]*)");
        testPattern(pattern2, text);
        
        // 패턴 3: 용량이 끝날 때까지 (공백이나 한글 알파벳 전까지)
        System.out.println("\n--- Pattern 3 (measure until space or korean/alpha) ---");
        Pattern pattern3 = Pattern.compile("([가-힣A-Za-z()\\s]+?)\\s+(\\d+[^\\s가-힣A-Za-z]+)");
        testPattern(pattern3, text);
        
        // 패턴 4: 용량을 숫자+단위로 명확히 (g, ml, 큰술 등)
        System.out.println("\n--- Pattern 4 (explicit units) ---");
        Pattern pattern4 = Pattern.compile("([가-힣A-Za-z()\\s]+?)\\s+(\\d+(?:\\.\\d+)?(?:g|ml|kg|L|개|큰술|작은술|T|t|컵|공기|포기|줌|약간|적당량|[^가-힣A-Za-z]*?)(?:\\s|$))");
        testPattern(pattern4, text);
        
        // 패턴 5: 재료명(한글/영문 최소 1개) + 공백 + 숫자 + 단위(숫자가 아닌 것들)
        System.out.println("\n--- Pattern 5 (name with at least 1 char + measure) ---");
        Pattern pattern5 = Pattern.compile("([가-힣A-Za-z()]+(?:\\s+[가-힣A-Za-z()]+)*)\\s+(\\d+(?:[^가-힣A-Za-z\\s]|[가-힣]+(?![가-힣A-Za-z]*\\s+\\d))*)");
        testPattern(pattern5, text);
    }
    
    @Test
    public void testSectionMarkerCase() {
        System.out.println("=== Testing Section Marker Case ===");
        String text = "물 100g 홍시드레싱 : 홍시 30g";
        System.out.println("Original text: " + text);
        
        RecipeMapper mapper = new RecipeMapper();
        CookRecipeResponse response = CookRecipeResponse.builder()
                .rcpSeq("TEST")
                .rcpNm("테스트")
                .rcpPartsDtls(text)
                .attFileNoMk("http://example.com/image.jpg")
                .attFileNoMain("http://example.com/thumb.jpg")
                .rcpPat2("반찬")
                .build();
        
        Recipe recipe = mapper.toEntity(response);
        List<RecipeIngredient> ingredients = recipe.getIngredients();
        
        System.out.println("\nExtracted ingredients (" + ingredients.size() + "):");
        for (int i = 0; i < ingredients.size(); i++) {
            RecipeIngredient ing = ingredients.get(i);
            System.out.println("  " + (i+1) + ". name='" + ing.getName() + "', measure='" + ing.getMeasure() + "'");
        }
        
        // 패턴 4로 직접 테스트
        System.out.println("\n--- Direct Pattern Test ---");
        Pattern pattern4 = Pattern.compile("([가-힣A-Za-z()\\s]+?)\\s+(\\d+(?:\\.\\d+)?(?:g|ml|kg|L|개|큰술|작은술|T|t|컵|공기|포기|줌|약간|적당량|[^가-힣A-Za-z]*?)(?:\\s|$))");
        testPattern(pattern4, text);
    }
    
    private void testPattern(Pattern pattern, String text) {
        System.out.println("Pattern: " + pattern.pattern());
        Matcher matcher = pattern.matcher(text);
        
        int count = 0;
        while (matcher.find()) {
            count++;
            System.out.println("  Match " + count + ": name='" + matcher.group(1).trim() + "', measure='" + matcher.group(2).trim() + "'");
        }
        
        if (count == 0) {
            System.out.println("  NO MATCHES!");
        }
    }
}
