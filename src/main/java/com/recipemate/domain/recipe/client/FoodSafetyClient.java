package com.recipemate.domain.recipe.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recipemate.domain.recipe.dto.CookRecipeListResponse;
import com.recipemate.domain.recipe.dto.CookRecipeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;

/**
 * 식품안전나라 API 클라이언트
 * COOKRCP01 서비스를 통해 한식 레시피 정보를 조회
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FoodSafetyClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${food.safety.api.key}")
    private String apiKey;

    @Value("${food.safety.api.base-url}")
    private String baseUrl;

    private static final String SERVICE_ID = "COOKRCP01";
    private static final String DATA_TYPE = "json";
    private static final int MAX_REQUEST_SIZE = 1000;

    /**
     * 한식 레시피 목록 조회
     * @param start 시작 위치
     * @param end 종료 위치
     * @return 레시피 목록
     */
    public List<CookRecipeResponse> getKoreanRecipes(int start, int end) {
        return fetchRecipes(start, end, null, null, null);
    }

    /**
     * 레시피 이름으로 검색
     * @param keyword 검색어
     * @param start 시작 위치
     * @param end 종료 위치
     * @return 검색된 레시피 목록
     */
    public List<CookRecipeResponse> searchRecipesByName(String keyword, int start, int end) {
        return fetchRecipes(start, end, keyword, null, null);
    }

    /**
     * 재료로 레시피 검색
     * @param ingredient 재료명
     * @param start 시작 위치
     * @param end 종료 위치
     * @return 검색된 레시피 목록
     */
    public List<CookRecipeResponse> searchRecipesByIngredient(String ingredient, int start, int end) {
        return fetchRecipes(start, end, null, ingredient, null);
    }

    /**
     * 요리 종류로 필터링
     * @param category 요리 종류 (반찬, 국, 후식 등)
     * @param start 시작 위치
     * @param end 종료 위치
     * @return 필터링된 레시피 목록
     */
    public List<CookRecipeResponse> searchRecipesByCategory(String category, int start, int end) {
        return fetchRecipes(start, end, null, null, category);
    }

    /**
     * 레시피 조회 공통 메서드
     * @param start 시작 위치
     * @param end 종료 위치
     * @param recipeName 레시피 이름 (선택)
     * @param ingredient 재료 (선택)
     * @param category 요리 종류 (선택)
     * @return 레시피 목록
     */
    private List<CookRecipeResponse> fetchRecipes(int start, int end, String recipeName, 
                                                   String ingredient, String category) {
        // 유효성 검증
        if (!isValidRange(start, end)) {
            log.warn("Invalid range: start={}, end={}", start, end);
            return Collections.emptyList();
        }

        try {
            String url = buildUrl(start, end, recipeName, ingredient, category);
            log.debug("Requesting Food Safety API: {}", url);

            String response = restTemplate.getForObject(url, String.class);
            
            if (response == null) {
                log.warn("Null response from Food Safety API");
                return Collections.emptyList();
            }

            return parseResponse(response);
            
        } catch (Exception e) {
            log.error("Error fetching recipes: start={}, end={}, recipeName={}, ingredient={}, category={}", 
                      start, end, recipeName, ingredient, category, e);
            return Collections.emptyList();
        }
    }

    /**
     * URL 구성
     * @param start 시작 위치
     * @param end 종료 위치
     * @param recipeName 레시피 이름 (선택)
     * @param ingredient 재료 (선택)
     * @param category 요리 종류 (선택)
     * @return 완성된 URL
     */
    private String buildUrl(int start, int end, String recipeName, String ingredient, String category) {
        // Base URL: {baseUrl}/{apiKey}/{serviceId}/{dataType}/{start}/{end}
        String url = String.format("%s/%s/%s/%s/%d/%d", 
                                   baseUrl, apiKey, SERVICE_ID, DATA_TYPE, start, end);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);

        // 선택적 필터 파라미터 추가
        if (recipeName != null && !recipeName.trim().isEmpty()) {
            builder.queryParam("RCP_NM", recipeName.trim());
        }
        if (ingredient != null && !ingredient.trim().isEmpty()) {
            builder.queryParam("RCP_PARTS_DTLS", ingredient.trim());
        }
        if (category != null && !category.trim().isEmpty()) {
            builder.queryParam("RCP_PAT2", category.trim());
        }

        return builder.build().toUriString();
    }

    /**
     * 응답 파싱
     * @param response JSON 응답 문자열
     * @return 레시피 목록
     */
    private List<CookRecipeResponse> parseResponse(String response) {
        try {
            // JSON 파싱: COOKRCP01 객체 추출
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode cookrcpNode = rootNode.get(SERVICE_ID);

            if (cookrcpNode == null) {
                log.warn("COOKRCP01 node not found in response");
                return Collections.emptyList();
            }

            // CookRecipeListResponse로 변환
            CookRecipeListResponse listResponse = objectMapper.treeToValue(cookrcpNode, CookRecipeListResponse.class);

            // 결과 검증
            if (listResponse == null || listResponse.getResult() == null) {
                log.warn("Invalid response structure");
                return Collections.emptyList();
            }

            // 응답 코드 검증
            if (!listResponse.getResult().isSuccess() && !listResponse.getResult().isNoData()) {
                log.error("API Error: code={}, message={}", 
                         listResponse.getResult().getCode(), 
                         listResponse.getResult().getMessage());
                return Collections.emptyList();
            }

            // 데이터가 없는 경우
            if (listResponse.getResult().isNoData() || listResponse.getRow() == null) {
                log.debug("No data found");
                return Collections.emptyList();
            }

            return listResponse.getRow();

        } catch (Exception e) {
            log.error("Error parsing response", e);
            return Collections.emptyList();
        }
    }

    /**
     * 범위 유효성 검증
     * @param start 시작 위치
     * @param end 종료 위치
     * @return 유효한 범위이면 true
     */
    private boolean isValidRange(int start, int end) {
        // start는 1 이상이어야 함
        if (start < 1) {
            return false;
        }

        // end는 start보다 크거나 같아야 함
        if (end < start) {
            return false;
        }

        // 최대 1000건까지만 요청 가능
        if (end - start + 1 > MAX_REQUEST_SIZE) {
            return false;
        }

        return true;
    }
}
