# 홈화면 인기 공동구매 및 레시피 디폴트 이미지 통일

## 문제 상황

### 1. 홈화면 인기 공동구매 이미지 깨짐
홈화면의 "인기 공동구매" 섹션에서 공동구매 이미지와 레시피 이미지가 모두 없을 때, 레시피 이미지가 제대로 렌더링되지 않는 문제가 발생했습니다.

- 공동구매 목록 페이지에서는 디폴트 이미지(`bi-image` 아이콘)가 정상적으로 표시됨
- 홈화면 인기 공동구매에서만 이미지가 깨진 상태로 표시됨

### 2. 레시피 디폴트 이미지 불일치
레시피 관련 페이지마다 디폴트 이미지 스타일이 달랐습니다:
- **레시피 상세 페이지**: `bi-egg-fried` + 보라색 그라데이션
- **레시피 목록 페이지**: `bi-image` + 회색 배경
- **홈화면 인기/추천 레시피**: `bi-egg-fried` + 주황색 그라데이션

## 원인 분석

**기존 코드 (index.html - 인기 공동구매 섹션)**:
```html
<!-- 공구 이미지도 레시피 이미지도 없는 경우 placeholder -->
<div th:if="${(groupBuy.imageUrls == null or groupBuy.imageUrls.isEmpty()) and groupBuy.recipeImageUrl == null}" 
     class="card-img-top image-placeholder p-5 text-center text-white" style="height: 250px;">
    <i class="bi bi-basket3 display-1"></i>
</div>
```

**문제점**:
- `groupBuy.recipeImageUrl == null` 조건만 확인
- `recipeImageUrl`이 빈 문자열(`""`)인 경우는 처리하지 못함
- 따라서 레시피 이미지가 null이 아닌 빈 문자열일 때, 이미지 태그가 렌더링되지만 src가 비어있어 깨진 이미지로 표시됨

## 해결 방법

### 1. 홈화면 인기 공동구매 - 빈 문자열 처리 추가

**수정된 코드 (index.html)**:
```html
<!-- 공구 이미지도 레시피 이미지도 없는 경우 디폴트 이미지 -->
<div th:if="${(groupBuy.imageUrls == null or groupBuy.imageUrls.isEmpty()) and (groupBuy.recipeImageUrl == null or #strings.isEmpty(groupBuy.recipeImageUrl))}" 
     class="card-img-top image-placeholder p-5 text-center text-white" style="height: 250px;">
    <i class="bi bi-basket3 display-1"></i>
</div>
```

**수정 내용**:
1. `#strings.isEmpty(groupBuy.recipeImageUrl)` 조건 추가
2. 레시피 이미지 표시 조건에도 동일하게 `!#strings.isEmpty()` 검사 추가

### 2. 레시피 디폴트 이미지 통일 - 주황색 그라데이션

전체 레시피 페이지에서 일관된 디폴트 이미지를 표시하도록 통일했습니다.

**공통 스타일 (styles.css)**:
```css
.image-placeholder {
    background: linear-gradient(135deg, var(--primary-color) 0%, #FF8C5A 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
}
```

**레시피 상세 페이지 (detail.html)** - 중복 스타일 제거:
```css
/* 변경 전 */
.image-placeholder {
    height: 400px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);  /* 보라색 */
    display: flex;
    align-items: center;
    justify-content: center;
}

/* 변경 후 - 공통 스타일 활용 */
.recipe-detail-placeholder {
    height: 400px;
}
```

**레시피 목록 페이지 (list.html)** - 아이콘 및 배경 변경:
```html
<!-- 변경 전 -->
<div class="card-img-top bg-secondary d-flex align-items-center justify-content-center recipe-img" 
     th:if="${recipe.imageUrl == null}">
    <i class="bi bi-image text-white" style="font-size: 3rem;"></i>  <!-- 회색 배경 + 이미지 아이콘 -->
</div>

<!-- 변경 후 -->
<div class="card-img-top image-placeholder recipe-img text-white d-flex align-items-center justify-content-center" 
     th:if="${recipe.imageUrl == null}">
    <i class="bi bi-egg-fried" style="font-size: 3rem;"></i>  <!-- 주황색 그라데이션 + 계란 아이콘 -->
</div>
```

### 3. 홈화면 인기 공구 디폴트 이미지 통일 - 공구 목록과 동일하게

홈화면의 인기 공동구매 디폴트 이미지를 공구 목록과 동일하게 변경했습니다.

**홈화면 인기 공구 (index.html)**:
```html
<!-- 변경 전 -->
<div th:if="${(groupBuy.imageUrls == null or groupBuy.imageUrls.isEmpty()) and (groupBuy.recipeImageUrl == null or #strings.isEmpty(groupBuy.recipeImageUrl))}" 
     class="card-img-top image-placeholder p-5 text-center text-white" style="height: 250px;">
    <i class="bi bi-basket3 display-1"></i>  <!-- 주황색 그라데이션 + 장바구니 아이콘 -->
</div>

<!-- 변경 후 -->
<div th:if="${(groupBuy.imageUrls == null or groupBuy.imageUrls.isEmpty()) and (groupBuy.recipeImageUrl == null or #strings.isEmpty(groupBuy.recipeImageUrl))}" 
     class="card-img-top bg-light d-flex align-items-center justify-content-center" style="height: 250px;">
    <i class="bi bi-image text-muted" style="font-size: 3rem;"></i>  <!-- 회색 배경 + 이미지 아이콘 -->
</div>
```

## 개선 효과

1. **일관된 UI**: 
   - 공구 목록과 홈화면에서 동일한 방식으로 디폴트 이미지 표시
   - 모든 레시피 페이지에서 통일된 디폴트 이미지 스타일 적용 (`bi-egg-fried` + 주황색 그라데이션)
   
2. **빈 문자열 처리**: 
   - null뿐만 아니라 빈 문자열도 올바르게 처리
   
3. **사용자 경험 향상**: 
   - 깨진 이미지 대신 의미 있는 아이콘 표시
   - 브랜드 컬러(주황색)를 활용한 시각적 일관성 확보

4. **코드 개선**:
   - 중복 스타일 제거로 유지보수성 향상
   - 공통 스타일 클래스 활용

## 디폴트 이미지 정리

| 페이지 | 아이콘 | 배경 |
|--------|--------|------|
| 레시피 상세 | `bi-egg-fried` | 주황색 그라데이션 |
| 레시피 목록 | `bi-egg-fried` | 주황색 그라데이션 |
| 홈 - 인기 레시피 | `bi-egg-fried` | 주황색 그라데이션 |
| 홈 - 추천 레시피 | `bi-egg-fried` | 주황색 그라데이션 |
| 공구 목록 | `bi-image` | 회색 (`bg-light`) |
| 홈 - 인기 공구 | `bi-image` | 회색 (`bg-light`) |

## 참고: 공구 목록 페이지의 올바른 구현

**list.html**에서는 이미 올바르게 처리되고 있었음:
```html
<!-- 업로드 이미지가 없으면 레시피 이미지 표시 -->
<img th:if="${(groupBuy.imageUrls == null or groupBuy.imageUrls.isEmpty()) and groupBuy.recipeImageUrl != null and !#strings.isEmpty(groupBuy.recipeImageUrl)}"
     th:src="${groupBuy.recipeImageUrl}"
     class="w-100 h-100 rounded-start"
     style="object-fit: cover;"
     th:alt="${groupBuy.title}">
<!-- 기본 이미지 (둘 다 없는 경우) -->
<div th:if="${(groupBuy.imageUrls == null or groupBuy.imageUrls.isEmpty()) and (groupBuy.recipeImageUrl == null or #strings.isEmpty(groupBuy.recipeImageUrl))}"
     class="bg-light w-100 h-100 rounded-start d-flex align-items-center justify-content-center">
    <i class="bi bi-image text-muted" style="font-size: 3rem;"></i>
</div>
```

## 수정 파일

1. `src/main/resources/templates/index.html`
   - 인기 공동구매 섹션의 이미지 렌더링 조건 수정

2. `src/main/resources/templates/recipes/detail.html`
   - 중복된 스타일 정의 제거
   - 공통 `image-placeholder` 클래스 활용

3. `src/main/resources/templates/recipes/list.html`
   - 디폴트 이미지 아이콘을 `bi-image`에서 `bi-egg-fried`로 변경
   - 배경색을 회색에서 주황색 그라데이션(`image-placeholder` 클래스)으로 변경

## 테스트 방법

### 홈화면 인기 공동구매
1. 홈화면 접속
2. 인기 공동구매 섹션 확인
3. 공구 이미지와 레시피 이미지가 모두 없는 공동구매에서 디폴트 이미지(이미지 아이콘 + 회색 배경) 표시 확인

### 레시피 페이지들
1. 레시피 목록 페이지 접속
2. 이미지가 없는 레시피에서 계란 프라이 아이콘 + 주황색 그라데이션 표시 확인
3. 레시피 상세 페이지 접속
4. 이미지가 없는 레시피에서 계란 프라이 아이콘 + 주황색 그라데이션 표시 확인
5. 홈화면의 인기 레시피, 추천 레시피에서도 동일한 디폴트 이미지 확인

## 수정 일자

2025-01-27

