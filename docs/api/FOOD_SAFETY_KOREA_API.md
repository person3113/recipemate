# 식품안전나라 Open API 명세

## 개요

- **제공기관**: 식품의약품안전처
- **서비스명**: 조리식품의 레시피 DB (COOKRCP01)
- **서비스 유형**: OPEN API
- **분류**: 식품영양정보
- **업데이트 주기**: 상시
- **API 호출 제한**: 1,000회
- **최종 수정일**: 2021-08-03

조리식품의 레시피를 이미지와 만드는 방법, 열량과 영양성분에 대한 정보를 제공하는 공공 API입니다.

## API 엔드포인트

### Base URL
```
http://openapi.foodsafetykorea.go.kr/api
```

### URL 구조
```
http://openapi.foodsafetykorea.go.kr/api/{인증키}/{서비스명}/{파일타입}/{시작위치}/{종료위치}
```

### 샘플 요청
```
http://openapi.foodsafetykorea.go.kr/api/sample/COOKRCP01/xml/1/5
```

### 필터링 파라미터를 포함한 요청
```
http://openapi.foodsafetykorea.go.kr/api/{인증키}/COOKRCP01/{파일타입}/{시작위치}/{종료위치}/RCP_NM=값&RCP_PARTS_DTLS=값
```

## 요청 파라미터

### 필수 파라미터

| 변수명 | 타입 | 설명 | 예시 |
|--------|------|------|------|
| keyId | STRING | OpenAPI 인증키 | (발급받은 인증키) |
| serviceId | STRING | 서비스명 | COOKRCP01 |
| dataType | STRING | 응답 파일 타입 | xml 또는 json |
| startIdx | STRING | 요청 시작 위치 | 1 |
| endIdx | STRING | 요청 종료 위치 | 100 |

### 선택 파라미터 (필터링)

| 변수명 | 타입 | 설명 | 예시 |
|--------|------|------|------|
| RCP_NM | STRING | 메뉴명 검색 | 김치찌개 |
| RCP_PARTS_DTLS | STRING | 재료 정보 검색 | 돼지고기 |
| CHNG_DT | STRING | 변경일자 이후 데이터 조회 (YYYYMMDD) | 20170101 |
| RCP_PAT2 | STRING | 요리 종류 | 반찬, 국, 후식 등 |

### 요청 제약사항

- 한 번에 최대 1,000건까지 요청 가능
- 종료위치는 시작위치보다 커야 함
- 모든 위치 값은 정수여야 함

## 응답 데이터 구조

### 기본 정보

| 필드명 | 설명 | 타입 |
|--------|------|------|
| RCP_SEQ | 일련번호 (Primary Key) | String |
| RCP_NM | 메뉴명 | String |
| RCP_WAY2 | 조리방법 (굽기, 끓이기 등) | String |
| RCP_PAT2 | 요리종류 (밥, 반찬, 국, 후식 등) | String |
| INFO_WGT | 중량 (1인분) | String |

### 영양 정보

| 필드명 | 설명 | 단위 |
|--------|------|------|
| INFO_ENG | 열량 | kcal |
| INFO_CAR | 탄수화물 | g |
| INFO_PRO | 단백질 | g |
| INFO_FAT | 지방 | g |
| INFO_NA | 나트륨 | mg |

### 메타 정보

| 필드명 | 설명 |
|--------|------|
| HASH_TAG | 해시태그 |
| ATT_FILE_NO_MAIN | 이미지 경로 (소) |
| ATT_FILE_NO_MK | 이미지 경로 (대) |
| RCP_PARTS_DTLS | 재료 정보 |
| RCP_NA_TIP | 저감 조리법 TIP |

### 조리 방법 정보

조리 방법은 최대 20단계까지 제공됩니다.

| 필드명 패턴 | 설명 | 범위 |
|------------|------|------|
| MANUAL{01-20} | 만드는 법 설명 | 01 ~ 20 |
| MANUAL_IMG{01-20} | 만드는 법 이미지 URL | 01 ~ 20 |

**예시:**
- `MANUAL01`: 첫 번째 조리 단계 설명
- `MANUAL_IMG01`: 첫 번째 조리 단계 이미지
- `MANUAL02`: 두 번째 조리 단계 설명
- `MANUAL_IMG02`: 두 번째 조리 단계 이미지

## 응답 메시지 코드

### 성공

| 코드 | 설명 |
|------|------|
| INFO-000 | 정상 처리되었습니다 |
| INFO-200 | 해당하는 데이터가 없습니다 |

### 인증 오류

| 코드 | 설명 |
|------|------|
| INFO-100 | 인증키가 유효하지 않습니다 |
| INFO-300 | 유효 호출건수를 이미 초과하셨습니다 |
| INFO-400 | 권한이 없습니다 |

### 요청 오류

| 코드 | 설명 |
|------|------|
| ERROR-300 | 필수 값이 누락되어 있습니다 |
| ERROR-301 | 파일타입 값이 누락 혹은 유효하지 않습니다 |
| ERROR-310 | 해당하는 서비스를 찾을 수 없습니다 |
| ERROR-331 | 요청시작위치 값을 확인하십시오 |
| ERROR-332 | 요청종료위치 값을 확인하십시오 |
| ERROR-334 | 종료위치보다 시작위치가 더 큽니다 |
| ERROR-336 | 데이터요청은 한번에 최대 1000건을 넘을 수 없습니다 |

### 서버 오류

| 코드 | 설명 |
|------|------|
| ERROR-500 | 서버 오류입니다 |
| ERROR-601 | SQL 문장 오류입니다 |

## 응답 예시

### XML 응답 예시

```xml
<COOKRCP01>
  <row>
    <RCP_SEQ>17</RCP_SEQ>
    <RCP_NM>칼륨 듬뿍 고구마죽</RCP_NM>
    <RCP_WAY2>끓이기</RCP_WAY2>
    <RCP_PAT2>후식</RCP_PAT2>
    <INFO_WGT></INFO_WGT>
    <INFO_ENG>205</INFO_ENG>
    <INFO_CAR>35</INFO_CAR>
    <INFO_PRO>3</INFO_PRO>
    <INFO_FAT>6</INFO_FAT>
    <INFO_NA>68</INFO_NA>
    <HASH_TAG></HASH_TAG>
    <ATT_FILE_NO_MAIN>http://www.foodsafetykorea.go.kr/uploadimg/cook/10_00017_2.png</ATT_FILE_NO_MAIN>
    <ATT_FILE_NO_MK>http://www.foodsafetykorea.go.kr/uploadimg/cook/10_00017_1.png</ATT_FILE_NO_MK>
    <RCP_PARTS_DTLS>고구마 100g(2/3개), 설탕 2g(1/3작은술), 찹쌀가루 3g(2/3작은술), 물 200ml(1컵), 잣 8g(8알)</RCP_PARTS_DTLS>
    <MANUAL01>1. 고구마는 깨끗이 씻어서 껍질을 벗기고 4cm 정도로 잘라준다.</MANUAL01>
    <MANUAL_IMG01>http://www.foodsafetykorea.go.kr/uploadimg/cook/20_00017_1.png</MANUAL_IMG01>
    <MANUAL02>2. 찜기에 고구마를 넣고 20~30분 정도 삶아 주고, 블렌더나 체를 이용하여 잘 으깨어 곱게 만든다.</MANUAL02>
    <MANUAL_IMG02>http://www.foodsafetykorea.go.kr/uploadimg/cook/20_00017_2.png</MANUAL_IMG02>
    <MANUAL03>3. 고구마와 물을 섞어 끓이면서 찹쌀가루로 농도를 맞추고 설탕을 넣어 맛을 낸다.</MANUAL03>
    <MANUAL_IMG03>http://www.foodsafetykorea.go.kr/uploadimg/cook/20_00017_3.png</MANUAL_IMG03>
    <MANUAL04>4. 잣을 팬에 노릇하게 볶아 다져서 고구마 죽에 섞는다.</MANUAL04>
    <MANUAL_IMG04></MANUAL_IMG04>
    <!-- MANUAL05~20 생략 가능 -->
    <RCP_NA_TIP></RCP_NA_TIP>
  </row>
</COOKRCP01>
```

### JSON 응답 예시

```json
{
  "COOKRCP01": {
    "total_count": "1",
    "row": [
      {
        "RCP_SEQ": "17",
        "RCP_NM": "칼륨 듬뿍 고구마죽",
        "RCP_WAY2": "끓이기",
        "RCP_PAT2": "후식",
        "INFO_WGT": "",
        "INFO_ENG": "205",
        "INFO_CAR": "35",
        "INFO_PRO": "3",
        "INFO_FAT": "6",
        "INFO_NA": "68",
        "HASH_TAG": "",
        "ATT_FILE_NO_MAIN": "http://www.foodsafetykorea.go.kr/uploadimg/cook/10_00017_2.png",
        "ATT_FILE_NO_MK": "http://www.foodsafetykorea.go.kr/uploadimg/cook/10_00017_1.png",
        "RCP_PARTS_DTLS": "고구마 100g(2/3개), 설탕 2g(1/3작은술), 찹쌀가루 3g(2/3작은술), 물 200ml(1컵), 잣 8g(8알)",
        "MANUAL01": "1. 고구마는 깨끗이 씻어서 껍질을 벗기고 4cm 정도로 잘라준다.",
        "MANUAL_IMG01": "http://www.foodsafetykorea.go.kr/uploadimg/cook/20_00017_1.png",
        "MANUAL02": "2. 찜기에 고구마를 넣고 20~30분 정도 삶아 주고, 블렌더나 체를 이용하여 잘 으깨어 곱게 만든다.",
        "MANUAL_IMG02": "http://www.foodsafetykorea.go.kr/uploadimg/cook/20_00017_2.png",
        "MANUAL03": "3. 고구마와 물을 섞어 끓이면서 찹쌀가루로 농도를 맞추고 설탕을 넣어 맛을 낸다.",
        "MANUAL_IMG03": "http://www.foodsafetykorea.go.kr/uploadimg/cook/20_00017_3.png",
        "MANUAL04": "4. 잣을 팬에 노릇하게 볶아 다져서 고구마 죽에 섞는다.",
        "MANUAL_IMG04": "",
        "RCP_NA_TIP": ""
      }
    ],
    "RESULT": {
      "MSG": "정상 처리되었습니다.",
      "CODE": "INFO-000"
    }
  }
}
```

## 구현 시 고려사항

### 1. 데이터 타입
- JSON 응답 형식 사용 권장
- 모든 수치 데이터가 문자열(String)로 반환되므로 파싱 필요

### 2. 페이징 처리
- 한 번에 최대 1,000건까지만 요청 가능
- 대량 데이터 조회 시 페이징 처리 필수

### 3. 조리 단계 처리
- MANUAL01~MANUAL20까지 동적으로 처리
- 빈 값이 나올 때까지 순회하며 조리 단계 수집

### 4. 이미지 처리
- 이미지 URL은 절대 경로로 제공됨
- 일부 이미지 필드가 빈 문자열일 수 있음

### 5. 에러 핸들링
- API 응답의 RESULT 객체에서 CODE 확인
- INFO-000이 아닌 경우 적절한 에러 처리 필요

### 6. 인증키 관리
- 환경 변수로 관리 권장
- 하루 1,000회 호출 제한 고려

### 7. 재료 정보 파싱
- RCP_PARTS_DTLS는 쉼표로 구분된 문자열
- 필요시 파싱하여 개별 재료로 분리

## 참고 링크

- API 포털: https://www.foodsafetykorea.go.kr/api
- 인증키 발급: 식품안전나라 Open API 포털에서 신청
