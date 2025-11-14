# 레시피 관련 경로 일관성 개선 작업 완료

## 작업 일자
2025년 11월 14일

## 작업 배경
팀원 요청사항:
- 다른 "내가 XX한" 페이지들과의 일관성 유지
- 모든 사용자 관련 마이페이지 기능을 `/users/me/` 경로 패턴으로 통일

## 변경 사항

### 1. PostCategory enum 수정 ✅
**파일**: `src/main/java/com/recipemate/global/common/PostCategory.java`

**변경 내용**:
```java
// 변경 전
public enum PostCategory {
    TIPS("팁 공유"),
    FREE("자유"),
    REVIEW("후기");  // ← 삭제됨
}

// 변경 후
public enum PostCategory {
    TIPS("팁 공유"),
    FREE("자유");
}
```

**이유**: 팀원이 이전에 REVIEW 카테고리를 삭제했는데 이 항목이 남아있으면 오류 발생

---

### 2. 컨트롤러 변경 ✅

#### RecipeController.java
**파일**: `src/main/java/com/recipemate/domain/recipe/controller/RecipeController.java`

**변경 내용**:
- `myRecipes()` 메서드 **삭제**
- 경로: `/recipes/my` 제거

#### UserController.java
**파일**: `src/main/java/com/recipemate/domain/user/controller/UserController.java`

**변경 내용**:
- 필요한 import 추가:
  - `RecipeListResponse`
  - `RecipeService`
  - `PageRequest`
- `recipeService` 의존성 추가
- `myRecipesPage()` 메서드 **추가**
- 경로: `/users/me/recipes`
- 반환 템플릿: `"user/my-recipes"`

**새로 추가된 메서드**:
```java
@GetMapping("/users/me/recipes")
public String myRecipesPage(
    @AuthenticationPrincipal UserDetails userDetails,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size,
    Model model,
    RedirectAttributes redirectAttributes) {
    // ... 구현
    return "user/my-recipes";
}
```

---

### 3. 템플릿 파일 변경 ✅

#### 파일 이동
- **기존 위치**: `templates/recipes/my-recipes.html` (유지됨 - 삭제하지 않음)
- **새 위치**: `templates/user/my-recipes.html` (새로 생성)

#### 템플릿 내 URL 변경
`templates/user/my-recipes.html`에서:
- 페이지네이션 링크: `/recipes/my` → `/users/me/recipes`

```html
<!-- 변경 전 -->
th:href="@{/recipes/my(page=${i}, size=${pageSize})}"

<!-- 변경 후 -->
th:href="@{/users/me/recipes(page=${i}, size=${pageSize})}"
```

---

### 4. SecurityConfig 변경 ✅
**파일**: `src/main/java/com/recipemate/global/config/SecurityConfig.java`

**변경 내용**:
```java
// 변경 전
.requestMatchers(
    "/recipes/new",
    "/recipes/*/edit",
    "/recipes/my"  // ← 제거됨
).authenticated()

// 변경 후
.requestMatchers(
    "/recipes/new",
    "/recipes/*/edit"
).authenticated()
```

**이유**: `/users/me/recipes`는 `/users/me/**` 패턴에 의해 이미 인증 필요하므로 별도 설정 불필요

---

### 5. 네비게이션 링크 추가 ✅

#### Header 드롭다운 메뉴
**파일**: `templates/fragments/header.html`

**추가된 항목**:
```html
<li><a th:href="@{/users/me/recipes}" class="dropdown-item">
    <i class="bi bi-book"></i> 내가 작성한 레시피
</a></li>
```

**위치**: "찜 목록"과 "참여중인 공구" 사이

#### 마이페이지 바로가기
**파일**: `templates/user/my-page.html`

**추가된 항목**:
```html
<a th:href="@{/users/me/recipes}" class="list-group-item list-group-item-action">
    <i class="bi bi-book text-primary"></i>
    <span class="ms-2">내가 작성한 레시피</span>
</a>
```

**위치**: "포인트 내역"과 "내가 만든 공구" 사이

---

## 최종 URL 구조

### 사용자 관련 페이지 (일관된 패턴)
- `/users/me` - 마이페이지
- `/users/me/notifications` - 알림
- `/users/me/bookmarks` - 찜 목록
- `/users/me/badges` - 배지
- `/users/me/points` - 포인트 내역
- `/users/me/recipes` - **내가 작성한 레시피** ✨ (새로 추가)
- `/users/me/group-purchases` - 내가 만든 공구
- `/users/me/participations` - 참여중인 공구
- `/users/me/community` - 내 커뮤니티 활동

---

## 테스트 체크리스트

- [ ] `/users/me/recipes` 접속 시 내가 작성한 레시피 목록이 정상적으로 표시되는지 확인
- [ ] 헤더의 "내 메뉴" 드롭다운에서 "내가 작성한 레시피" 링크 클릭 시 정상 이동
- [ ] 마이페이지의 바로가기에서 "내가 작성한 레시피" 링크 클릭 시 정상 이동
- [ ] 페이지네이션이 정상적으로 작동하는지 확인
- [ ] 레시피 보기/수정 버튼이 정상적으로 작동하는지 확인
- [ ] 로그인하지 않은 상태에서 접근 시 로그인 페이지로 리다이렉트되는지 확인

---

## 참고사항

### 기존 파일 처리
`templates/recipes/my-recipes.html` 파일은 삭제하지 않고 남겨두었습니다. 필요시 삭제 가능합니다.

### 컴파일 경고
일부 경고가 있지만 모두 "사용되지 않는 import" 또는 "메서드가 사용되지 않음" 등의 무해한 경고입니다. 실제 에러는 없습니다.

---

## 완료 ✅

모든 변경사항이 성공적으로 적용되었으며, 프로젝트의 일관성이 개선되었습니다.

