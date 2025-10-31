package com.recipemate.domain.recipe.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 식품안전나라 API 응답 결과 DTO
 * RESULT 객체를 매핑하여 API 호출 성공/실패 여부를 판별
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FoodSafetyApiResult {

    /**
     * 응답 코드
     * - INFO-000: 정상 처리
     * - INFO-200: 데이터 없음
     * - ERROR-***: 오류
     */
    @JsonProperty("CODE")
    private String code;

    /**
     * 응답 메시지
     */
    @JsonProperty("MSG")
    private String message;

    /**
     * 정상 처리 여부 확인
     * @return INFO-000인 경우 true
     */
    public boolean isSuccess() {
        return "INFO-000".equals(code);
    }

    /**
     * 데이터 없음 여부 확인
     * @return INFO-200인 경우 true
     */
    public boolean isNoData() {
        return "INFO-200".equals(code);
    }
}
