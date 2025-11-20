# 레시피 상세 페이지 액션 버튼 UI 개선 계획

## 1. 현황 및 문제 제기

- **현황:** 현재 레시피 상세 페이지(`src/main/resources/templates/recipes/detail.html`)의 우측 사이드바 '빠른 실행' 카드에는 여러 액션 버튼들이 `d-grid` 레이아웃 안에 함께 배치되어 있습니다.
- **포함된 액션:**
    1.  **공구 만들기** (핵심 CTA)
    2.  **찜하기** (주요 기능)
    3.  **공유하기** (부가 기능)
    4.  **레시피 개선 제안하기** (보조/기여 기능)
    5.  **신고하기** (관리/안전 기능)

- **문제점:** 위 액션들은 기능의 중요도와 사용 빈도가 각기 다릅니다. 하지만 현재 UI는 이 모두에게 거의 동등한 시각적 비중을 부여하고 있습니다. 특히 '개선 제안'과 '신고하기'는 '공구 만들기'나 '찜하기'처럼 사용자가 레시피를 소비하며 즉각적으로 취하는 핵심 행동이라기보다는, 필요할 때 찾는 보조적인 기능입니다. 이들을 나란히 배치하는 것은 사용자의 주의를 분산시키고 핵심 액션을 방해할 수 있습니다.

## 2. 개선 목표

- 액션의 중요도에 따라 시각적 계층(Visual Hierarchy)을 명확히 구분한다.
- 사용자가 가장 중요한 핵심 기능(공구 만들기, 찜하기)에 집중할 수 있도록 유도한다.
- 보조 기능(개선 제안, 신고)은 쉽게 찾을 수 있으면서도 핵심 기능의 시각적 흐름을 방해하지 않는 위치와 형태로 변경한다.
- 전체적으로 더 정돈되고 직관적인 UI를 제공하여 사용자 경험(UX)을 향상시킨다.

## 3. 개선 방안

'빠른 실행' 카드 내부 구조를 변경하여 핵심 액션과 보조 액션을 명확히 분리하는 것을 제안합니다.

### 구현 계획

1.  **핵심 액션 그룹 유지:**
    - '공구 만들기', '찜하기', '공유하기' 버튼은 현재의 큰 버튼 형태와 `d-grid` 구조를 유지하여 주목도를 높게 유지합니다. 이들은 사용자의 주요 상호작용이므로 눈에 잘 띄어야 합니다.

2.  **보조 액션 그룹 분리 및 재디자인:**
    - '레시피 개선 제안하기'와 '신고하기'는 기존 `d-grid`에서 분리합니다.
    - 핵심 액션 그룹 하단에 수평선(`<hr>`)을 추가하여 시각적 구분을 명확히 합니다.
    - 새로운 `div`에 두 액션을 배치하고, 전체적인 버튼 형태가 아닌 아이콘과 텍스트로 구성된 작은 링크 형태로 변경합니다.
    - Flexbox 유틸리티(`d-flex`, `justify-content-end`, `gap-3`)를 사용하여 오른쪽으로 정렬하고 적절한 간격을 둡니다. 이는 '부가적인 옵션'이라는 인상을 줍니다.
    - '신고하기'는 `text-danger` 클래스를 유지하여 경고의 의미를 미묘하게 전달하고, '개선 제안'은 `text-muted`를 사용하여 차분한 톤을 유지합니다.

### 코드 예시 (수정 후)

`src/main/resources/templates/recipes/detail.html`의 사이드바 카드 부분입니다.

```html
<!-- Actions Card -->
<div class="card shadow-sm mb-4 sticky-top floating-buttons">
    <div class="card-header bg-white">
        <h5 class="mb-0 fw-bold">빠른 실행</h5>
    </div>
    <div class="card-body">
        <!-- 1. 핵심 액션 그룹 -->
        <div class="d-grid gap-2">
            <!-- 공구 만들기 버튼 -->
            <a sec:authorize="isAuthenticated()"
               th:href="@{/group-purchases/new(recipeApiId=${recipe.id}, redirectUrl=${currentUrl})}"
               class="btn btn-primary btn-lg">
                <i class="bi bi-basket3"></i> 이 레시피로 공구 만들기
            </a>
            <a sec:authorize="!isAuthenticated()" ... >
                <i class="bi bi-box-arrow-in-right"></i> 로그인하고 공구 만들기
            </a>

            <!-- 찜 버튼 -->
            <button sec:authorize="isAuthenticated()" type="button" id="wishlistBtn" ...>
                <i id="wishlistIcon" class="bi bi-heart"></i>
                <span id="wishlistText">찜 추가</span>
            </button>
            <a sec:authorize="!isAuthenticated()" ... >
                <i class="bi bi-heart"></i> 로그인하고 찜하기
            </a>

            <!-- 공유하기 버튼 -->
            <button type="button" class="btn btn-outline-secondary" onclick="shareRecipe()">
                <i class="bi bi-share"></i> 공유하기
            </button>
        </div>

        <!-- 2. 시각적 구분선 -->
        <hr class="my-3">

        <!-- 3. 보조 액션 그룹 (우측 정렬된 작은 링크) -->
        <div class="d-flex justify-content-end gap-3">
            <!-- 레시피 개선 제안 링크 -->
            <a sec:authorize="isAuthenticated()"
               th:href="@{/recipes/{id}/corrections/new(id=${recipe.dbId})}"
               class="text-muted small text-decoration-none">
                <i class="bi bi-lightbulb"></i> 개선 제안
            </a>

            <!-- 신고하기 링크 -->
            <a sec:authorize="isAuthenticated()"
               th:if="${!isOwner}"
               th:href="@{/reports/new(entityType='RECIPE', entityId=${recipe.dbId}, entityName=${recipe.name})}"
               class="text-danger small text-decoration-none">
                <i class="bi bi-flag"></i> 신고하기
            </a>
        </div>
    </div>
</div>
```

## 4. 기대 효과

- **직관성 향상:** 사용자는 가장 중요한 액션에 즉시 집중할 수 있습니다.
- **클러터 감소:** UI가 시각적으로 정돈되어 복잡함이 줄어들고 깔끔한 인상을 줍니다.
- **사용성 개선:** 기능의 중요도와 맥락에 맞는 디자인을 제공함으로써 사용자가 더 쉽고 편안하게 서비스를 이용할 수 있습니다.
- **확장성:** 추후 다른 보조 기능이 추가되더라도 동일한 패턴을 적용하여 일관성을 유지하기 용이합니다.
