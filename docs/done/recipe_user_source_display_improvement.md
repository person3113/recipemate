# 사용자 레시피 출처 표시 개선

## 작업 일자
2025년 11월 14일

## 문제 상황
사용자가 작성한 레시피의 출처란에 "user"라고만 표시되어 누가 작성했는지 알 수 없음

### Before (문제 상황)
```
출처: user
```

## 해결 방법
사용자가 작성한 레시피의 경우 작성자의 실제 닉네임을 표시하도록 변경

### After (수정 후)
```
출처: 홍길동 (사용자)
```

## 수정 내용

### 파일: `RecipeService.java`

#### 1. RecipeDetailResponse 변환 로직 수정 ✅
**위치**: `convertRecipeEntityToDetailResponse()` 메서드

**변경 내용**:
```java
// 출처 결정: 사용자 레시피는 작성자 닉네임, API 레시피는 소스명
String source;
if (recipe.getSourceApi() == RecipeSource.USER && recipe.getAuthor() != null) {
    source = recipe.getAuthor().getNickname() + " (사용자)";
} else {
    source = recipe.getSourceApi().name().toLowerCase();
}
```

**적용 위치**:
- 레시피 상세 페이지 (`/recipes/{id}`)
- 출처 필드에 작성자 닉네임 + " (사용자)" 형태로 표시

#### 2. RecipeSimpleInfo 변환 로직 수정 ✅
**위치**: `convertRecipeEntityToSimpleInfo()` 메서드

**변경 내용**:
```java
// 출처 결정: 사용자 레시피는 작성자 닉네임, API 레시피는 소스명
String source;
if (recipe.getSourceApi() == RecipeSource.USER && recipe.getAuthor() != null) {
    source = recipe.getAuthor().getNickname();
} else {
    source = recipe.getSourceApi().name().toLowerCase();
}
```

**적용 위치**:
- 레시피 목록 페이지 (`/recipes/list`)
- 내가 작성한 레시피 페이지 (`/users/me/recipes`)
- 랜덤 레시피 페이지 (`/recipes/random`)
- 검색 결과 페이지

## 표시 형태

### 레시피 상세 페이지
- **사용자 레시피**: "출처: 홍길동 (사용자)"
- **TheMealDB**: "출처: themealdb"
- **식품안전나라**: "출처: foodsafety"

### 레시피 목록 페이지
- **사용자 레시피**: "출처: 홍길동"
- **TheMealDB**: "출처: themealdb"
- **식품안전나라**: "출처: foodsafety"

## 기술적 세부사항

### Lazy Loading 처리
- `recipe.getAuthor()`는 `@ManyToOne(fetch = FetchType.LAZY)`로 설정
- 하지만 변환 메서드는 트랜잭션 내에서 호출되므로 프록시 객체 접근 가능
- `getNickname()` 호출 시 자동으로 DB에서 로드됨

### Null 체크
```java
if (recipe.getSourceApi() == RecipeSource.USER && recipe.getAuthor() != null)
```
- `sourceApi`가 USER인지 확인
- `author`가 null이 아닌지 확인 (방어적 프로그래밍)

## 영향 범위

### 적용되는 페이지
1. ✅ 레시피 상세 페이지 (`/recipes/{id}`)
2. ✅ 레시피 목록 페이지 (`/recipes/list`)
3. ✅ 내가 작성한 레시피 페이지 (`/users/me/recipes`)
4. ✅ 랜덤 레시피 페이지 (`/recipes/random`)
5. ✅ 검색 결과 페이지 (`/search`)

### API 레시피는 영향 없음
- TheMealDB, 식품안전나라 API 레시피는 기존과 동일하게 표시

## 테스트 체크리스트

- [ ] 사용자가 작성한 레시피 상세 페이지에서 "닉네임 (사용자)" 형태로 표시되는지 확인
- [ ] 레시피 목록에서 사용자 레시피의 출처에 작성자 닉네임이 표시되는지 확인
- [ ] 내가 작성한 레시피 페이지에서 본인 닉네임이 표시되는지 확인
- [ ] TheMealDB, 식품안전나라 레시피는 기존과 동일하게 표시되는지 확인
- [ ] 여러 사용자가 작성한 레시피를 볼 때 각각의 닉네임이 정확히 표시되는지 확인

## 추가 개선 가능 사항 (향후)

### 1. 사용자 프로필 링크 추가
현재는 닉네임만 표시하지만, 클릭 시 해당 사용자 프로필로 이동하는 링크 추가 가능
```html
<a th:href="@{/users/{userId}(userId=${recipe.authorId})}">
    <span th:text="${recipe.source}"></span>
</a>
```

### 2. 사용자 아바타 이미지 추가
출처란에 사용자 프로필 이미지 썸네일 추가
```html
<div class="d-flex align-items-center">
    <img th:src="${recipe.authorProfileImage}" class="rounded-circle me-2" width="24" height="24">
    <span th:text="${recipe.source}"></span>
</div>
```

### 3. 작성 일시 표시
사용자 레시피의 경우 작성 일시 추가 표시
```html
<span>출처: 홍길동 (사용자) · 2025.01.14 작성</span>
```

## 완료 ✅

사용자가 작성한 레시피의 출처란에 작성자의 실제 닉네임이 표시되도록 개선 완료

