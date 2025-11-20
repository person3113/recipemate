# 요리 팁(tips) 필드 레시피 상세 페이지 표시 계획

## 1. 분석 (Analysis)

- **목표:** 레시피 상세 페이지에 `tips`(요리 팁) 필드 내용을 표시합니다.
- **현상:** `codebase_investigator`를 통해 분석한 결과, `Recipe` 엔티티에는 `tips` 필드가 존재하여 데이터베이스에는 관련 정보가 저장되고 있습니다. 하지만 이 데이터가 프론트엔드로 전달되는 과정에서 누락되어 화면에 표시되지 않고 있습니다.
- **핵심 원인:** `RecipeService`가 `Recipe` 엔티티를 `RecipeDetailResponse` DTO로 변환할 때 `tips` 필드를 포함시키지 않고 있습니다. 따라서 Thymeleaf 템플릿에서는 해당 데이터에 접근할 수 없습니다.

### 관련 파일
- **Entity:** `src/main/java/com/recipemate/domain/recipe/entity/Recipe.java`
  - `tips` 필드가 정의되어 있음을 확인했습니다.
- **DTO:** `src/main/java/com/recipemate/domain/recipe/dto/RecipeDetailResponse.java`
  - **수정이 필요한 파일**입니다. `tips` 필드가 누락되어 있습니다.
- **Service:** `src/main/java/com/recipemate/domain/recipe/service/RecipeService.java`
  - **수정이 필요한 파일**입니다. 엔티티를 DTO로 변환하는 로직에 `tips` 필드 매핑을 추가해야 합니다.
- **Controller:** `src/main/java/com/recipemate/domain/recipe/controller/RecipeController.java`
  - `recipeDetailPage` 메서드에서 `RecipeDetailResponse` DTO를 모델에 담아 뷰로 전달하고 있음을 확인했습니다.
- **Template:** `src/main/resources/templates/recipes/detail.html`
  - **수정이 필요한 파일**입니다. `tips` 필드를 표시할 UI 코드를 추가해야 합니다.

## 2. 실행 계획 (Implementation Plan)

### 단계 1: DTO 필드 추가

`src/main/java/com/recipemate/domain/recipe/dto/RecipeDetailResponse.java` 파일에 `tips` 필드를 추가합니다.

```java
// 기존 필드들...
private String tips;
```

### 단계 2: 서비스 로직 수정

`src/main/java/com/recipemate/domain/recipe/service/RecipeService.java` 파일에서 `Recipe` 엔티티를 `RecipeDetailResponse` DTO로 변환하는 생성자 또는 빌더 로직을 수정합니다. `tips` 필드 값을 엔티티에서 DTO로 복사하는 코드를 추가합니다.

**예상 수정 위치:** `RecipeDetailResponse`의 생성자 또는 `of` 와 같은 정적 팩토리 메서드 내부

```java
// 예시: RecipeDetailResponse 생성자 또는 빌더 부분
.tips(recipe.getTips()) // 이와 같은 라인을 추가
```

### 단계 3: 템플릿 수정

`src/main/resources/templates/recipes/detail.html` 파일에 `tips` 정보를 표시하는 UI를 추가합니다. `th:if`를 사용하여 `tips` 필드에 내용이 있을 때만 해당 영역이 보이도록 처리합니다.

```html
<!-- 레시피 재료와 조리 과정 사이 또는 적절한 위치에 추가 -->
<div th:if="${recipe.tips != null and !recipe.tips.isEmpty()}" class="mt-4">
    <h5 class="fw-bold">요리 팁</h5>
    <div class="card p-3">
        <p th:text="${recipe.tips}" class="mb-0"></p>
    </div>
</div>
```

## 3. 기대 효과 (Expected Outcome)

- 레시피 상세 페이지에서 '요리 팁' 정보가 있는 경우, 해당 내용이 사용자에게 표시됩니다.
- '요리 팁' 정보가 없는 레시피의 경우, UI에 불필요한 공간이 생기지 않아 깔끔한 화면을 유지합니다.
