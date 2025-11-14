# 사용자 레시피 CRUD 기능 구현 가능성 분석

**작성일**: 2025년 11월 13일  
**분석 대상**: 첨부된 가이드라인의 구현 가능성 검토

---

## 📋 요약

**결론: ✅ 가이드라인을 따라 구현 가능합니다!**

현재 코드베이스는 사용자가 레시피를 추가하고 CRUD 할 수 있는 기능을 구현하기에 **매우 적합한 구조**를 이미 갖추고 있습니다. 가이드라인에서 제시한 대부분의 내용이 이미 부분적으로 구현되어 있거나, 쉽게 확장 가능한 상태입니다.

---

## 🎯 현재 코드베이스 분석

### 1. 데이터베이스 및 도메인 모델 ✅ (거의 준비됨)

#### ✅ **이미 구현된 부분**

**Recipe 엔티티 (`Recipe.java`)**
```java
@Entity
public class Recipe extends BaseEntity {
    // ... 기본 필드들
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "source_api")
    private RecipeSource sourceApi;  // ✅ 이미 출처 구분 필드 존재!
    
    @Column(length = 100, name = "source_api_id")
    private String sourceApiId;  // ✅ API ID 필드 존재
    
    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecipeIngredient> ingredients = new ArrayList<>();  // ✅ 재료 관계
    
    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecipeStep> steps = new ArrayList<>();  // ✅ 조리 단계 관계
}
```

**RecipeSource Enum (`RecipeSource.java`)**
```java
public enum RecipeSource {
    MEAL_DB,        // TheMealDB API
    FOOD_SAFETY,    // 식품안전나라 API
    USER            // ✅ 사용자 직접 등록 - 이미 정의되어 있음!
}
```

#### ⚠️ **추가 필요한 부분**

**Recipe 엔티티에 작성자 필드 추가**
```java
// Recipe.java에 추가 필요
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "user_id")
private User author;  // ❌ 아직 없음 - 추가 필요!
```

**User 엔티티는 이미 완벽하게 구현되어 있음** ✅
- `src/main/java/com/recipemate/domain/user/entity/User.java`
- ID, email, nickname 등 모든 필드 준비됨

---

### 2. Repository 계층 ✅ (확장만 하면 됨)

#### ✅ **이미 구현된 메서드들**

**RecipeRepository**에 이미 많은 쿼리 메서드가 구현되어 있습니다:
```java
// 소스별 레시피 조회 - 이미 있음!
Page<Recipe> findBySourceApi(RecipeSource sourceApi, Pageable pageable);

// 제목, 카테고리, 지역, 재료로 검색 - 이미 구현됨!
Page<Recipe> findByTitleContainingIgnoreCase(String title, Pageable pageable);
Page<Recipe> findByCategory(String category, Pageable pageable);
// ... 등등
```

#### 📝 **추가 필요한 메서드**

```java
// RecipeRepository.java에 추가
List<Recipe> findByAuthor(User author);  // 사용자가 작성한 레시피 조회
Page<Recipe> findByAuthor(User author, Pageable pageable);  // 페이징 버전
```

---

### 3. Service 계층 ⚠️ (새로운 메서드 추가 필요)

#### ✅ **현재 구조**

`RecipeService.java`는 이미 잘 구조화되어 있으며, API 레시피를 조회하는 다양한 메서드가 구현되어 있습니다.

#### 📝 **추가 필요한 메서드**

```java
// RecipeService.java에 추가 필요
@Transactional
public RecipeDetailResponse createUserRecipe(RecipeCreateRequestDto dto, User currentUser) {
    // 1. sourceApi = USER, author = currentUser로 Recipe 생성
    // 2. 재료(RecipeIngredient)와 조리단계(RecipeStep) 저장
    // 3. return 생성된 레시피
}

@Transactional
public RecipeDetailResponse updateUserRecipe(Long recipeId, RecipeUpdateRequestDto dto, User currentUser) {
    // 1. 레시피 조회
    // 2. 권한 검사: recipe.getAuthor().getId().equals(currentUser.getId())
    // 3. 업데이트 후 저장
}

@Transactional
public void deleteUserRecipe(Long recipeId, User currentUser) {
    // 1. 레시피 조회
    // 2. 권한 검사
    // 3. 삭제 (orphanRemoval=true로 재료/단계도 자동 삭제됨)
}

public boolean canUserModifyRecipe(Long recipeId, User currentUser) {
    // 권한 체크 유틸리티 메서드
}
```

---

### 4. Controller 계층 ⚠️ (엔드포인트 추가 필요)

#### ✅ **현재 구조**

`RecipeController.java`는 이미 다음을 구현:
- `GET /recipes` - 레시피 목록 (검색, 필터, 페이징)
- `GET /recipes/{recipeId}` - 레시피 상세

#### 📝 **추가 필요한 엔드포인트**

```java
// RecipeController.java에 추가

@GetMapping("/new")
public String createRecipeForm(Model model) {
    // 빈 폼 반환
    return "recipes/form";
}

@PostMapping
@PreAuthorize("isAuthenticated()")  // 로그인 필수
public String createRecipe(@Valid @ModelAttribute RecipeCreateRequestDto dto,
                          @AuthenticationPrincipal CustomUserDetails userDetails) {
    User currentUser = userDetails.getUser();
    RecipeDetailResponse created = recipeService.createUserRecipe(dto, currentUser);
    return "redirect:/recipes/" + created.getId();
}

@GetMapping("/{id}/edit")
@PreAuthorize("isAuthenticated()")
public String editRecipeForm(@PathVariable Long id,
                            @AuthenticationPrincipal CustomUserDetails userDetails,
                            Model model) {
    User currentUser = userDetails.getUser();
    RecipeDetailResponse recipe = recipeService.getRecipeDetailById(id);
    
    // 권한 체크
    if (!recipeService.canUserModifyRecipe(id, currentUser)) {
        throw new CustomException(ErrorCode.FORBIDDEN);
    }
    
    model.addAttribute("recipe", recipe);
    return "recipes/form";
}

@PostMapping("/{id}/edit")
@PreAuthorize("isAuthenticated()")
public String updateRecipe(@PathVariable Long id,
                          @Valid @ModelAttribute RecipeUpdateRequestDto dto,
                          @AuthenticationPrincipal CustomUserDetails userDetails) {
    User currentUser = userDetails.getUser();
    recipeService.updateUserRecipe(id, dto, currentUser);
    return "redirect:/recipes/" + id;
}

@PostMapping("/{id}/delete")
@PreAuthorize("isAuthenticated()")
public String deleteRecipe(@PathVariable Long id,
                          @AuthenticationPrincipal CustomUserDetails userDetails) {
    User currentUser = userDetails.getUser();
    recipeService.deleteUserRecipe(id, currentUser);
    return "redirect:/recipes";
}
```

---

### 5. DTO 계층 ❌ (새로 생성 필요)

#### 📁 **이미 존재하는 DTO 디렉토리**
`src/main/java/com/recipemate/domain/recipe/dto/`

#### 📝 **새로 생성 필요한 파일들**

```java
// RecipeCreateRequestDto.java - 신규 생성 필요
@Data
public class RecipeCreateRequestDto {
    @NotBlank(message = "레시피 제목은 필수입니다")
    private String title;
    
    private String category;
    private String area;
    private String imageUrl;
    
    private Integer calories;
    private Integer protein;
    private Integer carbohydrate;
    private Integer fat;
    
    private String tips;
    
    @Valid
    private List<IngredientDto> ingredients;  // 재료 목록
    
    @Valid
    private List<StepDto> steps;  // 조리 단계
    
    @Data
    public static class IngredientDto {
        private String name;
        private String measure;
    }
    
    @Data
    public static class StepDto {
        private Integer stepOrder;
        private String description;
    }
}

// RecipeUpdateRequestDto.java - 신규 생성 필요
// (거의 동일한 구조)
```

---

### 6. 프론트엔드 (Templates) ⚠️ (추가/수정 필요)

#### ✅ **현재 존재하는 템플릿**
- `templates/recipes/list.html` - 레시피 목록 페이지
- `templates/recipes/detail.html` - 레시피 상세 페이지
- `templates/recipes/random.html` - 랜덤 레시피

#### 📝 **새로 생성 필요**
```html
<!-- templates/recipes/form.html - 신규 생성 -->
레시피 작성/수정 폼 페이지
- 제목, 카테고리, 지역 입력
- 재료 동적 추가/삭제 (JavaScript)
- 조리 단계 동적 추가/삭제
- 이미지 URL 입력 (향후 업로드 기능 확장 가능)
```

#### ✏️ **수정 필요한 파일**

**`templates/recipes/detail.html`**
```html
<!-- 기존 코드에 추가 -->
<div th:if="${recipe.sourceApi == 'USER' and recipe.authorId == currentUser?.id}">
    <a th:href="@{/recipes/{id}/edit(id=${recipe.id})}" 
       class="btn btn-warning">
        <i class="bi bi-pencil"></i> 수정
    </a>
    <form th:action="@{/recipes/{id}/delete(id=${recipe.id})}" 
          method="post" 
          style="display:inline;"
          onsubmit="return confirm('정말 삭제하시겠습니까?');">
        <button type="submit" class="btn btn-danger">
            <i class="bi bi-trash"></i> 삭제
        </button>
    </form>
</div>
```

**`templates/recipes/list.html`**
```html
<!-- 상단에 버튼 추가 -->
<div class="mb-3">
    <a th:href="@{/recipes/new}" class="btn btn-primary">
        <i class="bi bi-plus-lg"></i> 내 레시피 작성하기
    </a>
</div>
```

**`templates/user/my-page.html` 또는 관련 탭**
```html
<!-- 새 탭 추가 -->
<li class="nav-item">
    <a class="nav-link" th:href="@{/user/my-recipes}">
        <i class="bi bi-book"></i> 내가 작성한 레시피
    </a>
</li>
```

---

## 🔍 추가 고려사항

### 1. **인증/권한 체크**
현재 프로젝트에 Spring Security가 설정되어 있는지 확인 필요:
- `@AuthenticationPrincipal` 사용 가능한지
- `CustomUserDetails` 클래스가 존재하는지
- 세션 기반 인증인지 확인

### 2. **이미지 업로드**
가이드라인에서는 이미지 URL만 다루지만, 향후 확장 가능:
- 파일 업로드 기능 추가
- S3 또는 로컬 스토리지 연동
- 썸네일 자동 생성

### 3. **Validation**
- JSR-303 Bean Validation 사용
- 재료와 조리단계는 최소 1개 이상 필수
- XSS 방지를 위한 입력 sanitization

### 4. **데이터베이스 마이그레이션**
Recipe 테이블에 `user_id` 컬럼 추가:
```sql
ALTER TABLE recipes ADD COLUMN user_id BIGINT;
ALTER TABLE recipes ADD CONSTRAINT fk_recipe_user 
    FOREIGN KEY (user_id) REFERENCES users(id);
```

---

## ✅ 구현 체크리스트

### Phase 1: 백엔드 핵심 로직
- [ ] Recipe 엔티티에 `author` 필드 추가
- [ ] RecipeRepository에 `findByAuthor()` 메서드 추가
- [ ] RecipeCreateRequestDto, RecipeUpdateRequestDto 생성
- [ ] RecipeService에 CRUD 메서드 구현
  - [ ] createUserRecipe()
  - [ ] updateUserRecipe()
  - [ ] deleteUserRecipe()
  - [ ] canUserModifyRecipe()

### Phase 2: 컨트롤러 및 엔드포인트
- [ ] RecipeController에 새 엔드포인트 추가
  - [ ] GET /recipes/new
  - [ ] POST /recipes
  - [ ] GET /recipes/{id}/edit
  - [ ] POST /recipes/{id}/edit
  - [ ] POST /recipes/{id}/delete

### Phase 3: 프론트엔드
- [ ] templates/recipes/form.html 생성
- [ ] templates/recipes/detail.html 수정 (수정/삭제 버튼)
- [ ] templates/recipes/list.html 수정 (작성 버튼)
- [ ] templates/user/my-recipes.html 생성 (선택사항)

### Phase 4: 테스트 및 검증
- [ ] 단위 테스트 작성
- [ ] 통합 테스트 작성
- [ ] 권한 체크 테스트
- [ ] 수동 E2E 테스트

---

## 🎉 최종 결론

**가이드라인은 현재 코드베이스와 매우 잘 맞습니다!**

### 강점:
1. ✅ RecipeSource에 `USER` 타입이 **이미 정의**되어 있음
2. ✅ Recipe 엔티티가 **이미 재료/단계와 연관관계** 설정됨
3. ✅ Repository에 **다양한 쿼리 메서드**가 준비됨
4. ✅ User 엔티티가 **완벽하게 구현**되어 있음
5. ✅ Controller와 Service가 **잘 구조화**되어 있음

### 필요한 작업:
1. Recipe에 `author` 필드만 추가하면 됨
2. Service에 CRUD 메서드 추가
3. Controller에 엔드포인트 추가
4. DTO 2개와 템플릿 1개 새로 생성
5. 기존 템플릿 2개 수정

### 예상 작업 시간:
- **숙련된 개발자**: 4-6시간
- **초급 개발자**: 1-2일

**가이드라인을 따라 단계별로 구현하면 안전하고 깔끔하게 기능을 추가할 수 있습니다!** 🚀

