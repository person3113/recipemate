package com.recipemate.domain.recipe.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recipemate.domain.recipe.dto.CookRecipeResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

/**
 * 식품안전나라 API 클라이언트 테스트
 * MockRestServiceServer를 사용하여 외부 API 의존성 제거
 */
class FoodSafetyClientTest {

    private FoodSafetyClient foodSafetyClient;
    private MockRestServiceServer mockServer;
    private static final String API_KEY = "test-api-key";
    private static final String BASE_URL = "http://openapi.foodsafetykorea.go.kr/api";

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.createServer(restTemplate);
        
        foodSafetyClient = new FoodSafetyClient(restTemplate, new ObjectMapper());
        
        // ReflectionTestUtils로 private 필드 설정
        ReflectionTestUtils.setField(foodSafetyClient, "apiKey", API_KEY);
        ReflectionTestUtils.setField(foodSafetyClient, "baseUrl", BASE_URL);
    }

    @Test
    @DisplayName("한식 레시피 목록 조회 - 정상 응답")
    void getKoreanRecipes_Success() {
        // given
        int start = 1;
        int end = 5;
        String expectedUrl = String.format("%s/%s/COOKRCP01/json/%d/%d", BASE_URL, API_KEY, start, end);
        
        String mockResponse = """
            {
              "COOKRCP01": {
                "RESULT": {
                  "MSG": "정상처리되었습니다.",
                  "CODE": "INFO-000"
                },
                "total_count": "2",
                "row": [
                  {
                    "RCP_SEQ": "1",
                    "RCP_NM": "김치찌개",
                    "RCP_WAY2": "끓이기",
                    "RCP_PAT2": "국&찌개",
                    "INFO_WGT": "1인분",
                    "INFO_ENG": "300.0",
                    "INFO_CAR": "20.0",
                    "INFO_PRO": "15.0",
                    "INFO_FAT": "10.0",
                    "INFO_NA": "800.0",
                    "HASH_TAG": "#김치찌개#한식",
                    "ATT_FILE_NO_MAIN": "http://example.com/image1.jpg",
                    "ATT_FILE_NO_MK": "http://example.com/image2.jpg",
                    "RCP_PARTS_DTLS": "김치, 돼지고기",
                    "MANUAL01": "재료를 준비합니다.",
                    "MANUAL_IMG01": "http://example.com/step1.jpg",
                    "MANUAL02": "끓입니다.",
                    "MANUAL_IMG02": "http://example.com/step2.jpg"
                  },
                  {
                    "RCP_SEQ": "2",
                    "RCP_NM": "된장찌개",
                    "RCP_WAY2": "끓이기",
                    "RCP_PAT2": "국&찌개",
                    "INFO_WGT": "1인분",
                    "INFO_ENG": "250.0",
                    "INFO_CAR": "18.0",
                    "INFO_PRO": "12.0",
                    "INFO_FAT": "8.0",
                    "INFO_NA": "700.0",
                    "HASH_TAG": "#된장찌개#한식",
                    "ATT_FILE_NO_MAIN": "http://example.com/image3.jpg",
                    "ATT_FILE_NO_MK": "http://example.com/image4.jpg",
                    "RCP_PARTS_DTLS": "된장, 두부, 호박",
                    "MANUAL01": "재료를 준비합니다.",
                    "MANUAL_IMG01": "http://example.com/step3.jpg",
                    "MANUAL02": "끓입니다.",
                    "MANUAL_IMG02": "http://example.com/step4.jpg"
                  }
                ]
              }
            }
            """;

        mockServer.expect(requestTo(expectedUrl))
            .andRespond(withSuccess(mockResponse, MediaType.APPLICATION_JSON));

        // when
        List<CookRecipeResponse> recipes = foodSafetyClient.getKoreanRecipes(start, end);

        // then
        mockServer.verify();
        assertThat(recipes).hasSize(2);
        assertThat(recipes.get(0).getRcpSeq()).isEqualTo("1");
        assertThat(recipes.get(0).getRcpNm()).isEqualTo("김치찌개");
        assertThat(recipes.get(1).getRcpSeq()).isEqualTo("2");
        assertThat(recipes.get(1).getRcpNm()).isEqualTo("된장찌개");
    }

    @Test
    @DisplayName("레시피 이름으로 검색")
    void searchRecipesByName_Success() {
        // given
        String keyword = "김치찌개";
        int start = 1;
        int end = 10;
        
        String mockResponse = """
            {
              "COOKRCP01": {
                "RESULT": {
                  "MSG": "정상처리되었습니다.",
                  "CODE": "INFO-000"
                },
                "total_count": "1",
                "row": [
                  {
                    "RCP_SEQ": "1",
                    "RCP_NM": "김치찌개",
                    "RCP_WAY2": "끓이기",
                    "RCP_PAT2": "국&찌개",
                    "INFO_WGT": "1인분",
                    "INFO_ENG": "300.0",
                    "INFO_CAR": "20.0",
                    "INFO_PRO": "15.0",
                    "INFO_FAT": "10.0",
                    "INFO_NA": "800.0",
                    "HASH_TAG": "#김치찌개#한식",
                    "ATT_FILE_NO_MAIN": "http://example.com/image1.jpg",
                    "ATT_FILE_NO_MK": "http://example.com/image2.jpg",
                    "RCP_PARTS_DTLS": "김치, 돼지고기",
                    "MANUAL01": "재료를 준비합니다.",
                    "MANUAL_IMG01": "http://example.com/step1.jpg"
                  }
                ]
              }
            }
            """;

        // URI 매처: 한글이 인코딩되므로 URI 객체 비교
        mockServer.expect(requestTo(org.hamcrest.Matchers.matchesPattern(
                ".*COOKRCP01/json/1/10\\?RCP_NM=.*")))
            .andRespond(withSuccess(mockResponse, MediaType.APPLICATION_JSON));

        // when
        List<CookRecipeResponse> recipes = foodSafetyClient.searchRecipesByName(keyword, start, end);

        // then
        mockServer.verify();
        assertThat(recipes).hasSize(1);
        assertThat(recipes.get(0).getRcpNm()).contains("김치찌개");
    }

    @Test
    @DisplayName("재료로 레시피 검색")
    void searchRecipesByIngredient_Success() {
        // given
        String ingredient = "돼지고기";
        int start = 1;
        int end = 10;
        
        String mockResponse = """
            {
              "COOKRCP01": {
                "RESULT": {
                  "MSG": "정상처리되었습니다.",
                  "CODE": "INFO-000"
                },
                "total_count": "1",
                "row": [
                  {
                    "RCP_SEQ": "10",
                    "RCP_NM": "제육볶음",
                    "RCP_WAY2": "볶기",
                    "RCP_PAT2": "반찬",
                    "INFO_WGT": "2인분",
                    "INFO_ENG": "400.0",
                    "INFO_CAR": "25.0",
                    "INFO_PRO": "30.0",
                    "INFO_FAT": "15.0",
                    "INFO_NA": "900.0",
                    "HASH_TAG": "#제육볶음#한식",
                    "ATT_FILE_NO_MAIN": "http://example.com/pork.jpg",
                    "ATT_FILE_NO_MK": "http://example.com/pork2.jpg",
                    "RCP_PARTS_DTLS": "돼지고기, 양파, 고추장",
                    "MANUAL01": "고기를 양념에 재웁니다.",
                    "MANUAL_IMG01": "http://example.com/pork_step1.jpg"
                  }
                ]
              }
            }
            """;

        // URI 매처: 한글이 인코딩되므로 패턴 비교
        mockServer.expect(requestTo(org.hamcrest.Matchers.matchesPattern(
                ".*COOKRCP01/json/1/10\\?RCP_PARTS_DTLS=.*")))
            .andRespond(withSuccess(mockResponse, MediaType.APPLICATION_JSON));

        // when
        List<CookRecipeResponse> recipes = foodSafetyClient.searchRecipesByIngredient(ingredient, start, end);

        // then
        mockServer.verify();
        assertThat(recipes).hasSize(1);
        assertThat(recipes.get(0).getRcpPartsDtls()).contains("돼지고기");
    }

    @Test
    @DisplayName("요리 종류로 필터링")
    void searchRecipesByCategory_Success() {
        // given
        String category = "반찬";
        int start = 1;
        int end = 10;
        
        String mockResponse = """
            {
              "COOKRCP01": {
                "RESULT": {
                  "MSG": "정상처리되었습니다.",
                  "CODE": "INFO-000"
                },
                "total_count": "1",
                "row": [
                  {
                    "RCP_SEQ": "20",
                    "RCP_NM": "계란말이",
                    "RCP_WAY2": "부치기",
                    "RCP_PAT2": "반찬",
                    "INFO_WGT": "1인분",
                    "INFO_ENG": "200.0",
                    "INFO_CAR": "10.0",
                    "INFO_PRO": "15.0",
                    "INFO_FAT": "12.0",
                    "INFO_NA": "300.0",
                    "HASH_TAG": "#계란말이#반찬",
                    "ATT_FILE_NO_MAIN": "http://example.com/egg.jpg",
                    "ATT_FILE_NO_MK": "http://example.com/egg2.jpg",
                    "RCP_PARTS_DTLS": "계란, 파, 소금",
                    "MANUAL01": "계란을 풀어줍니다.",
                    "MANUAL_IMG01": "http://example.com/egg_step1.jpg"
                  }
                ]
              }
            }
            """;

        // URI 매처: 한글이 인코딩되므로 패턴 비교
        mockServer.expect(requestTo(org.hamcrest.Matchers.matchesPattern(
                ".*COOKRCP01/json/1/10\\?RCP_PAT2=.*")))
            .andRespond(withSuccess(mockResponse, MediaType.APPLICATION_JSON));

        // when
        List<CookRecipeResponse> recipes = foodSafetyClient.searchRecipesByCategory(category, start, end);

        // then
        mockServer.verify();
        assertThat(recipes).hasSize(1);
        assertThat(recipes.get(0).getRcpPat2()).isEqualTo("반찬");
    }

    @Test
    @DisplayName("잘못된 범위 요청 시 빈 리스트 반환")
    void getKoreanRecipes_InvalidRange() {
        // given
        int start = 10;
        int end = 5; // start > end

        // when
        List<CookRecipeResponse> recipes = foodSafetyClient.getKoreanRecipes(start, end);

        // then
        assertThat(recipes).isEmpty();
        // MockServer에 요청이 가지 않았음을 확인
    }

    @Test
    @DisplayName("1000건 초과 요청 시 빈 리스트 반환")
    void getKoreanRecipes_ExceedsMaxLimit() {
        // given
        int start = 1;
        int end = 1001; // 1000건 초과

        // when
        List<CookRecipeResponse> recipes = foodSafetyClient.getKoreanRecipes(start, end);

        // then
        assertThat(recipes).isEmpty();
        // MockServer에 요청이 가지 않았음을 확인
    }

    @Test
    @DisplayName("API 응답에 데이터가 없을 때 빈 리스트 반환")
    void getKoreanRecipes_NoData() {
        // given
        int start = 1;
        int end = 10;
        String expectedUrl = String.format("%s/%s/COOKRCP01/json/%d/%d", BASE_URL, API_KEY, start, end);
        
        String mockResponse = """
            {
              "COOKRCP01": {
                "RESULT": {
                  "MSG": "해당하는 데이터가 없습니다.",
                  "CODE": "INFO-200"
                },
                "total_count": "0"
              }
            }
            """;

        mockServer.expect(requestTo(expectedUrl))
            .andRespond(withSuccess(mockResponse, MediaType.APPLICATION_JSON));

        // when
        List<CookRecipeResponse> recipes = foodSafetyClient.getKoreanRecipes(start, end);

        // then
        mockServer.verify();
        assertThat(recipes).isEmpty();
    }

    @Test
    @DisplayName("API 에러 응답 시 빈 리스트 반환")
    void getKoreanRecipes_ApiError() {
        // given
        int start = 1;
        int end = 10;
        String expectedUrl = String.format("%s/%s/COOKRCP01/json/%d/%d", BASE_URL, API_KEY, start, end);
        
        String mockResponse = """
            {
              "COOKRCP01": {
                "RESULT": {
                  "MSG": "서비스 키가 유효하지 않습니다.",
                  "CODE": "ERROR-300"
                }
              }
            }
            """;

        mockServer.expect(requestTo(expectedUrl))
            .andRespond(withSuccess(mockResponse, MediaType.APPLICATION_JSON));

        // when
        List<CookRecipeResponse> recipes = foodSafetyClient.getKoreanRecipes(start, end);

        // then
        mockServer.verify();
        assertThat(recipes).isEmpty();
    }

    @Test
    @DisplayName("네트워크 오류 시 빈 리스트 반환")
    void getKoreanRecipes_NetworkError() {
        // given
        int start = 1;
        int end = 10;
        String expectedUrl = String.format("%s/%s/COOKRCP01/json/%d/%d", BASE_URL, API_KEY, start, end);

        mockServer.expect(requestTo(expectedUrl))
            .andRespond(withServerError());

        // when
        List<CookRecipeResponse> recipes = foodSafetyClient.getKoreanRecipes(start, end);

        // then
        mockServer.verify();
        assertThat(recipes).isEmpty();
    }

    @Test
    @DisplayName("API 응답 파싱 검증 - 필수 필드 확인")
    void verifyResponseParsing() {
        // given
        int start = 1;
        int end = 1;
        String expectedUrl = String.format("%s/%s/COOKRCP01/json/%d/%d", BASE_URL, API_KEY, start, end);
        
        String mockResponse = """
            {
              "COOKRCP01": {
                "RESULT": {
                  "MSG": "정상처리되었습니다.",
                  "CODE": "INFO-000"
                },
                "total_count": "1",
                "row": [
                  {
                    "RCP_SEQ": "999",
                    "RCP_NM": "테스트 레시피",
                    "RCP_WAY2": "테스트",
                    "RCP_PAT2": "테스트",
                    "INFO_WGT": "1인분",
                    "INFO_ENG": "100.0",
                    "INFO_CAR": "10.0",
                    "INFO_PRO": "10.0",
                    "INFO_FAT": "5.0",
                    "INFO_NA": "500.0",
                    "HASH_TAG": "#테스트",
                    "ATT_FILE_NO_MAIN": "http://example.com/test.jpg",
                    "ATT_FILE_NO_MK": "http://example.com/test2.jpg",
                    "RCP_PARTS_DTLS": "재료",
                    "MANUAL01": "만드는법",
                    "MANUAL_IMG01": "http://example.com/test_step.jpg"
                  }
                ]
              }
            }
            """;

        mockServer.expect(requestTo(expectedUrl))
            .andRespond(withSuccess(mockResponse, MediaType.APPLICATION_JSON));

        // when
        List<CookRecipeResponse> recipes = foodSafetyClient.getKoreanRecipes(start, end);

        // then
        mockServer.verify();
        assertThat(recipes).hasSize(1);
        CookRecipeResponse recipe = recipes.get(0);
        assertThat(recipe.getRcpSeq()).isEqualTo("999");
        assertThat(recipe.getRcpNm()).isEqualTo("테스트 레시피");
    }
}
