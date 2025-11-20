# 레시피 작성 폼 카테고리 옵션 확장

## 작업 일자
2025-01-20

## 작업 내용

### 개요
사용자 레시피 작성 폼의 카테고리 선택 옵션을 확장하여 TheMealDB API와 식품안전나라 API에서 제공하는 모든 카테고리를 선택할 수 있도록 개선했습니다.

### 변경 파일
- `src/main/resources/templates/recipes/form.html`

### 변경 전 카테고리 (6개)
```
- 한식
- 양식
- 중식
- 일식
- 디저트
- 기타
```

### 변경 후 카테고리 (22개)

#### 영문 카테고리 (TheMealDB API)
- Beef
- Breakfast
- Chicken
- Dessert
- Lamb
- Miscellaneous
- Pasta
- Pork
- Seafood
- Side
- Starter
- Vegan
- Vegetarian

#### 한글 카테고리 (식품안전나라 API)
- 국&찌개
- 김치
- 면
- 밥
- 반찬
- 일품
- 죽
- 후식

#### 기타
- 기타

### 구현 세부사항

#### HTML 구조
단순 리스트 형태로 모든 카테고리를 나열:

```html
<select class="form-select" id="category" th:field="*{category}">
    <option value="">선택 안함</option>
    <option value="Beef">Beef</option>
    <option value="Breakfast">Breakfast</option>
    <option value="Chicken">Chicken</option>
    <option value="Dessert">Dessert</option>
    <option value="Lamb">Lamb</option>
    <option value="Miscellaneous">Miscellaneous</option>
    <option value="Pasta">Pasta</option>
    <option value="Pork">Pork</option>
    <option value="Seafood">Seafood</option>
    <option value="Side">Side</option>
    <option value="Starter">Starter</option>
    <option value="Vegan">Vegan</option>
    <option value="Vegetarian">Vegetarian</option>
    <option value="국&찌개">국&찌개</option>
    <option value="김치">김치</option>
    <option value="면">면</option>
    <option value="밥">밥</option>
    <option value="반찬">반찬</option>
    <option value="일품">일품</option>
    <option value="죽">죽</option>
    <option value="후식">후식</option>
    <option value="기타">기타</option>
</select>
```

### 효과

#### 1. 사용성 개선
- 사용자가 다양한 카테고리 중에서 선택 가능
- 영문과 한글 카테고리를 함께 제공하여 선택의 폭 확대
- API 레시피와 동일한 카테고리 사용 가능

#### 2. 데이터 일관성
- TheMealDB API, 식품안전나라 API, 사용자 레시피가 동일한 카테고리 체계 사용
- 레시피 검색 및 필터링 시 통합된 결과 제공

#### 3. 확장성
- 향후 새로운 API 추가 시 카테고리만 추가하면 됨

### 참고 사항

#### 카테고리 선택 가이드
- **영문 카테고리**: TheMealDB API에서 사용하는 표준 카테고리 (Beef, Chicken, Dessert 등)
- **한글 카테고리**: 식품안전나라 API에서 사용하는 한식 중심 카테고리 (밥, 반찬, 국&찌개 등)
- **기타**: 특정 분류에 속하지 않는 레시피

#### DB 저장
- 선택한 카테고리는 `recipes.category` 컬럼에 문자열로 저장
- 최대 100자 제한 (모든 카테고리가 이 범위 내)

#### 레시피 목록 페이지 연동
- 레시피 목록 페이지(`recipes/list.html`)의 카테고리 필터는 DB에서 동적으로 조회
- 사용자가 입력한 카테고리도 자동으로 필터 옵션에 포함됨

### 향후 개선 가능 사항

1. **다국어 지원**
   - 영문 카테고리에 한글 설명 추가 (예: "Beef (소고기)")

2. **카테고리 관리 페이지**
   - 관리자가 카테고리를 추가/수정/삭제할 수 있는 기능

3. **인기 카테고리 우선 표시**
   - 사용 빈도가 높은 카테고리를 상단에 배치

4. **자동완성 기능**
   - 카테고리 검색 시 자동완성 제공

### 테스트 체크리스트

- [x] 모든 카테고리 옵션이 올바르게 표시됨
- [x] 영문과 한글 카테고리가 모두 포함됨
- [x] 기존 레시피 수정 시 카테고리 값이 올바르게 선택됨
- [x] 새 레시피 작성 시 카테고리 선택 및 저장이 정상 동작
- [ ] 레시피 목록 페이지에서 새 카테고리로 필터링 가능 (자동 동작)

