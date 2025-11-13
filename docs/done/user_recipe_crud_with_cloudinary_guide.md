# 사용자 레시피 CRUD + Cloudinary 이미지 업로드 구현 가이드

**작성일**: 2025년 1월 13일  
**목표**: Cloudinary API를 이용한 이미지 업로드와 API 레시피와 동일한 형식의 사용자 레시피 작성 기능 구현

---

## 🎯 구현 목표

1. ✅ **Cloudinary를 이용한 이미지 업로드** - 대표 이미지 1장만
2. ✅ **필수 정보만 간단하게** - 제목, 재료, 조리방법(텍스트)
3. ✅ **사용자 레시피 CRUD** - 작성, 수정, 삭제 (본인만 가능)

### 📝 구현 범위 (간소화 버전)
- **대표 이미지**: Cloudinary 업로드 (1장)
- **제목**: 레시피명
- **재료**: name + measure (예: 돼지고기 300g)
- **조리방법**: 텍스트 형식 (TheMealDB처럼)
- **선택사항**: 카테고리, 지역, 팁, YouTube 링크

---

## 📊 현재 코드베이스 상태

### ✅ **이미 완벽하게 준비된 것들**

1. **Cloudinary 설정 완료** ✅
   - `CloudinaryConfig.java` - Bean 설정됨
   - `application.yml` - `cloudinary.url` 환경변수 설정됨
   - `build.gradle` - `com.cloudinary:cloudinary-http5:2.3.0` 의존성 추가됨

2. **이미지 업로드 유틸리티 구현 완료** ✅
   - `ImageUploadUtil.java` - 병렬 업로드, 검증, 최적화 기능 포함
   - 최대 3개 이미지 동시 업로드
   - 5MB 파일 크기 제한
   - JPEG, PNG 지원

3. **Recipe 엔티티 완벽한 구조** ✅
   - `RecipeSource` enum (MEAL_DB, FOOD_SAFETY, USER)
   - `RecipeIngredient` - 재료 (name, measure)
   - `RecipeStep` - 조리 단계 (stepNumber, description, imageUrl)
   - OneToMany 관계 설정 (cascade, orphanRemoval)

4. **파일 업로드 설정 완료** ✅
   - `application.yml`에 multipart 설정됨 (최대 5MB)

---

## 🔧 구현해야 할 것들

### 1. Recipe 엔티티 수정 (author 필드 추가)

**파일**: `src/main/java/com/recipemate/domain/recipe/entity/Recipe.java`

```java
// Recipe.java에 추가
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "user_id")
private User author;  // 사용자가 작성한 레시피만 값이 있음 (API 레시피는 null)

// Getter 메서드 추가
public User getAuthor() {
    return author;
}

// 사용자 레시피인지 확인하는 헬퍼 메서드
public boolean isUserRecipe() {
    return this.sourceApi == RecipeSource.USER;
}

// 특정 사용자가 수정 가능한지 확인
public boolean canModify(User user) {
    return isUserRecipe() && author != null && author.getId().equals(user.getId());
}
```

**DB 마이그레이션 필요**:
```sql
ALTER TABLE recipes ADD COLUMN user_id BIGINT;
ALTER TABLE recipes ADD CONSTRAINT fk_recipe_user 
    FOREIGN KEY (user_id) REFERENCES users(id);
CREATE INDEX idx_recipe_user_id ON recipes(user_id);
```

---

### 2. DTO 생성 (요청/응답)

**파일**: `src/main/java/com/recipemate/domain/recipe/dto/RecipeCreateRequest.java`

```java
package com.recipemate.domain.recipe.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeCreateRequest {
    
    @NotBlank(message = "레시피 제목을 입력해주세요")
    @Size(max = 200, message = "제목은 200자 이내로 입력해주세요")
    private String title;
    
    private String category;  // 카테고리 (예: 한식, 양식, 중식 등)
    
    private String area;  // 지역/국가
    
    @NotBlank(message = "조리 방법을 입력해주세요")
    private String instructions;  // TheMealDB 형식 (텍스트)
    
    private String tips;  // 저감 조리법 팁
    
    private String youtubeUrl;  // YouTube 링크
    
    private String sourceUrl;  // 참고 링크
    
    // 대표 이미지 파일 (업로드)
    private MultipartFile mainImage;
    
    // 재료 목록
    @Valid
    @NotNull(message = "재료를 최소 1개 이상 입력해주세요")
    @Size(min = 1, message = "재료를 최소 1개 이상 입력해주세요")
    @Builder.Default
    private List<IngredientDto> ingredients = new ArrayList<>();
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IngredientDto {
        @NotBlank(message = "재료명을 입력해주세요")
        private String name;
        
        @NotBlank(message = "재료 분량을 입력해주세요")
        private String measure;
    }
}
```

**파일**: `src/main/java/com/recipemate/domain/recipe/dto/RecipeUpdateRequest.java`

```java
// RecipeCreateRequest와 동일한 구조
// 차이점: 기존 이미지 URL을 유지할지 여부 필드 추가
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeUpdateRequest {
    // ... RecipeCreateRequest와 동일한 필드들
    
    // 추가 필드
    private String existingMainImageUrl;  // 기존 대표 이미지 유지 시
}
```

---

### 3. Service 계층 구현

**파일**: `src/main/java/com/recipemate/domain/recipe/service/RecipeService.java`

```java
// 추가할 메서드들

/**
 * 사용자 레시피 생성
 */
@Transactional
public RecipeDetailResponse createUserRecipe(RecipeCreateRequest request, User currentUser) {
    // 1. 대표 이미지 업로드 (Cloudinary)
    String mainImageUrl = null;
    if (request.getMainImage() != null && !request.getMainImage().isEmpty()) {
        List<String> uploadedUrls = imageUploadUtil.uploadImages(List.of(request.getMainImage()));
        if (!uploadedUrls.isEmpty()) {
            mainImageUrl = uploadedUrls.get(0);
        }
    }
    
    // 2. Recipe 엔티티 생성 (간소화 버전)
    Recipe recipe = Recipe.builder()
            .title(request.getTitle())
            .category(request.getCategory())
            .area(request.getArea())
            .fullImageUrl(mainImageUrl)
            .thumbnailImageUrl(mainImageUrl)  // 같은 이미지 사용
            .sourceApi(RecipeSource.USER)
            .author(currentUser)  // 중요!
            .instructions(request.getInstructions())  // 텍스트 조리방법
            .tips(request.getTips())
            .youtubeUrl(request.getYoutubeUrl())
            .sourceUrl(request.getSourceUrl())
            .lastSyncedAt(LocalDateTime.now())
            .build();
    
    // 3. 재료 추가
    for (RecipeCreateRequest.IngredientDto ingredientDto : request.getIngredients()) {
        RecipeIngredient ingredient = RecipeIngredient.builder()
                .name(ingredientDto.getName())
                .measure(ingredientDto.getMeasure())
                .build();
        recipe.addIngredient(ingredient);
    }
    
    // 4. 저장
    Recipe savedRecipe = recipeRepository.save(recipe);
    
    // 5. 응답 DTO 변환 후 반환
    return convertToDetailResponse(savedRecipe);
}

/**
 * 사용자 레시피 수정
 */
@Transactional
public RecipeDetailResponse updateUserRecipe(Long recipeId, RecipeUpdateRequest request, User currentUser) {
    // 1. 레시피 조회
    Recipe recipe = recipeRepository.findById(recipeId)
            .orElseThrow(() -> new CustomException(ErrorCode.RECIPE_NOT_FOUND));
    
    // 2. 권한 검사
    if (!recipe.canModify(currentUser)) {
        throw new CustomException(ErrorCode.FORBIDDEN);
    }
    
    // 3. 대표 이미지 업데이트 (새 이미지가 있으면)
    if (request.getMainImage() != null && !request.getMainImage().isEmpty()) {
        List<String> uploadedUrls = imageUploadUtil.uploadImages(List.of(request.getMainImage()));
        if (!uploadedUrls.isEmpty()) {
            recipe.updateMainImage(uploadedUrls.get(0));  // Recipe에 setter 메서드 필요
        }
    }
    
    // 4. 기본 정보 업데이트
    recipe.updateBasicInfo(
        request.getTitle(),
        request.getCategory(),
        request.getArea(),
        request.getInstructions(),  // 텍스트 조리방법
        request.getTips(),
        request.getYoutubeUrl(),
        request.getSourceUrl()
    );  // Recipe에 업데이트 메서드 필요
    
    // 5. 재료 업데이트 (기존 재료 삭제 후 새로 추가)
    recipe.getIngredients().clear();
    for (RecipeUpdateRequest.IngredientDto ingredientDto : request.getIngredients()) {
        RecipeIngredient ingredient = RecipeIngredient.builder()
                .name(ingredientDto.getName())
                .measure(ingredientDto.getMeasure())
                .build();
        recipe.addIngredient(ingredient);
    }
    
    // 6. 저장 및 반환
    Recipe updatedRecipe = recipeRepository.save(recipe);
    return convertToDetailResponse(updatedRecipe);
}

/**
 * 사용자 레시피 삭제
 */
@Transactional
public void deleteUserRecipe(Long recipeId, User currentUser) {
    Recipe recipe = recipeRepository.findById(recipeId)
            .orElseThrow(() -> new CustomException(ErrorCode.RECIPE_NOT_FOUND));
    
    if (!recipe.canModify(currentUser)) {
        throw new CustomException(ErrorCode.FORBIDDEN);
    }
    
    recipeRepository.delete(recipe);
    // orphanRemoval = true 설정 덕분에 재료와 조리단계도 자동 삭제됨
}

/**
 * 사용자가 작성한 레시피 목록 조회
 */
@Transactional(readOnly = true)
public Page<RecipeListResponse.RecipeSimpleInfo> getUserRecipes(User user, Pageable pageable) {
    Page<Recipe> recipes = recipeRepository.findByAuthor(user, pageable);
    return recipes.map(this::convertToSimpleInfo);
}
```

---

### 4. Repository 메서드 추가

**파일**: `src/main/java/com/recipemate/domain/recipe/repository/RecipeRepository.java`

```java
// 추가할 메서드들

/**
 * 사용자가 작성한 레시피 목록 조회
 */
Page<Recipe> findByAuthor(User author, Pageable pageable);

/**
 * 사용자가 작성한 레시피 개수
 */
long countByAuthor(User author);

/**
 * ID와 작성자로 조회 (권한 체크용)
 */
Optional<Recipe> findByIdAndAuthor(Long id, User author);
```

---

### 5. Controller 엔드포인트 추가

**파일**: `src/main/java/com/recipemate/domain/recipe/controller/RecipeController.java`

```java
// 추가할 엔드포인트들

/**
 * 레시피 작성 폼 페이지
 */
@GetMapping("/new")
public String createRecipeForm(Model model, 
                               @AuthenticationPrincipal UserDetails userDetails) {
    // 비로그인 시 로그인 페이지로 리다이렉트
    if (userDetails == null) {
        return "redirect:/auth/login";
    }
    
    model.addAttribute("recipe", new RecipeCreateRequest());
    model.addAttribute("isEdit", false);
    return "recipes/form";
}

/**
 * 레시피 작성 처리
 */
@PostMapping
public String createRecipe(@Valid @ModelAttribute RecipeCreateRequest request,
                          BindingResult bindingResult,
                          @AuthenticationPrincipal UserDetails userDetails,
                          RedirectAttributes redirectAttributes) {
    
    if (userDetails == null) {
        return "redirect:/auth/login";
    }
    
    if (bindingResult.hasErrors()) {
        return "recipes/form";
    }
    
    try {
        User currentUser = userService.findByEmail(userDetails.getUsername());
        RecipeDetailResponse created = recipeService.createUserRecipe(request, currentUser);
        
        redirectAttributes.addFlashAttribute("message", "레시피가 성공적으로 등록되었습니다.");
        return "redirect:/recipes/" + created.getId();
        
    } catch (Exception e) {
        log.error("Failed to create recipe", e);
        redirectAttributes.addFlashAttribute("error", "레시피 등록에 실패했습니다: " + e.getMessage());
        return "redirect:/recipes/new";
    }
}

/**
 * 레시피 수정 폼 페이지
 */
@GetMapping("/{id}/edit")
public String editRecipeForm(@PathVariable Long id,
                            Model model,
                            @AuthenticationPrincipal UserDetails userDetails) {
    
    if (userDetails == null) {
        return "redirect:/auth/login";
    }
    
    User currentUser = userService.findByEmail(userDetails.getUsername());
    RecipeDetailResponse recipe = recipeService.getRecipeDetailById(id);
    
    // 권한 체크
    Recipe recipeEntity = recipeRepository.findById(id)
            .orElseThrow(() -> new CustomException(ErrorCode.RECIPE_NOT_FOUND));
    
    if (!recipeEntity.canModify(currentUser)) {
        throw new CustomException(ErrorCode.FORBIDDEN);
    }
    
    model.addAttribute("recipe", recipe);
    model.addAttribute("isEdit", true);
    return "recipes/form";
}

/**
 * 레시피 수정 처리
 */
@PostMapping("/{id}/edit")
public String updateRecipe(@PathVariable Long id,
                          @Valid @ModelAttribute RecipeUpdateRequest request,
                          BindingResult bindingResult,
                          @AuthenticationPrincipal UserDetails userDetails,
                          RedirectAttributes redirectAttributes) {
    
    if (userDetails == null) {
        return "redirect:/auth/login";
    }
    
    if (bindingResult.hasErrors()) {
        return "recipes/form";
    }
    
    try {
        User currentUser = userService.findByEmail(userDetails.getUsername());
        recipeService.updateUserRecipe(id, request, currentUser);
        
        redirectAttributes.addFlashAttribute("message", "레시피가 성공적으로 수정되었습니다.");
        return "redirect:/recipes/" + id;
        
    } catch (Exception e) {
        log.error("Failed to update recipe", e);
        redirectAttributes.addFlashAttribute("error", "레시피 수정에 실패했습니다: " + e.getMessage());
        return "redirect:/recipes/" + id + "/edit";
    }
}

/**
 * 레시피 삭제 처리
 */
@PostMapping("/{id}/delete")
public String deleteRecipe(@PathVariable Long id,
                          @AuthenticationPrincipal UserDetails userDetails,
                          RedirectAttributes redirectAttributes) {
    
    if (userDetails == null) {
        return "redirect:/auth/login";
    }
    
    try {
        User currentUser = userService.findByEmail(userDetails.getUsername());
        recipeService.deleteUserRecipe(id, currentUser);
        
        redirectAttributes.addFlashAttribute("message", "레시피가 성공적으로 삭제되었습니다.");
        return "redirect:/recipes";
        
    } catch (Exception e) {
        log.error("Failed to delete recipe", e);
        redirectAttributes.addFlashAttribute("error", "레시피 삭제에 실패했습니다: " + e.getMessage());
        return "redirect:/recipes/" + id;
    }
}

/**
 * 내가 작성한 레시피 목록
 */
@GetMapping("/my")
public String myRecipes(@AuthenticationPrincipal UserDetails userDetails,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "20") int size,
                       Model model) {
    
    if (userDetails == null) {
        return "redirect:/auth/login";
    }
    
    User currentUser = userService.findByEmail(userDetails.getUsername());
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    Page<RecipeListResponse.RecipeSimpleInfo> recipes = recipeService.getUserRecipes(currentUser, pageable);
    
    model.addAttribute("recipes", recipes);
    model.addAttribute("currentPage", page);
    return "recipes/my-recipes";
}
```

---

### 6. 프론트엔드 - 레시피 작성/수정 폼

**파일**: `src/main/resources/templates/recipes/form.html` (새로 생성)

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title th:text="${isEdit ? '레시피 수정' : '레시피 작성'} + ' - RecipeMate'">레시피 작성 - RecipeMate</title>
    
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css">
    <link rel="stylesheet" th:href="@{/css/styles.css}">
</head>
<body class="bg-light">
    <div th:replace="~{fragments/header :: header}"></div>
    
    <main class="container my-5">
        <div class="row">
            <div class="col-lg-8 mx-auto">
                <div class="card shadow-sm">
                    <div class="card-header bg-primary text-white">
                        <h3 class="mb-0">
                            <i class="bi bi-pencil-square"></i>
                            <span th:text="${isEdit ? '레시피 수정' : '레시피 작성'}">레시피 작성</span>
                        </h3>
                    </div>
                    
                    <div class="card-body p-4">
                        <!-- 폼 시작 -->
                        <form th:action="${isEdit ? '/recipes/' + recipe.id + '/edit' : '/recipes'}"
                              method="post"
                              enctype="multipart/form-data"
                              th:object="${recipe}">
                            
                            <!-- 기본 정보 -->
                            <div class="mb-4">
                                <h5 class="border-bottom pb-2 mb-3">기본 정보</h5>
                                
                                <!-- 제목 -->
                                <div class="mb-3">
                                    <label for="title" class="form-label">레시피 제목 <span class="text-danger">*</span></label>
                                    <input type="text" 
                                           class="form-control" 
                                           id="title" 
                                           th:field="*{title}" 
                                           placeholder="예: 김치찌개"
                                           required>
                                    <div th:if="${#fields.hasErrors('title')}" class="text-danger small" th:errors="*{title}"></div>
                                </div>
                                
                                <!-- 카테고리 + 지역 -->
                                <div class="row">
                                    <div class="col-md-6 mb-3">
                                        <label for="category" class="form-label">카테고리</label>
                                        <select class="form-select" id="category" th:field="*{category}">
                                            <option value="">선택 안함</option>
                                            <option value="한식">한식</option>
                                            <option value="양식">양식</option>
                                            <option value="중식">중식</option>
                                            <option value="일식">일식</option>
                                            <option value="디저트">디저트</option>
                                            <option value="기타">기타</option>
                                        </select>
                                    </div>
                                    <div class="col-md-6 mb-3">
                                        <label for="area" class="form-label">지역/국가</label>
                                        <input type="text" class="form-control" id="area" th:field="*{area}" placeholder="예: 한국">
                                    </div>
                                </div>
                                
                                <!-- 대표 이미지 -->
                                <div class="mb-3">
                                    <label for="mainImage" class="form-label">대표 이미지</label>
                                    <input type="file" 
                                           class="form-control" 
                                           id="mainImage" 
                                           name="mainImage" 
                                           accept="image/jpeg,image/jpg,image/png"
                                           onchange="previewMainImage(event)">
                                    <small class="text-muted">최대 5MB, JPG/PNG 형식</small>
                                    
                                    <!-- 이미지 미리보기 -->
                                    <div id="mainImagePreview" class="mt-2" style="display:none;">
                                        <img id="mainImagePreviewImg" src="" alt="미리보기" class="img-thumbnail" style="max-height: 200px;">
                                    </div>
                                    
                                    <!-- 수정 시 기존 이미지 표시 -->
                                    <div th:if="${isEdit and recipe.imageUrl != null}" class="mt-2">
                                        <p class="small text-muted">현재 이미지:</p>
                                        <img th:src="${recipe.imageUrl}" alt="현재 이미지" class="img-thumbnail" style="max-height: 200px;">
                                    </div>
                                </div>
                            </div>
                            
                            <!-- 재료 -->
                            <div class="mb-4">
                                <h5 class="border-bottom pb-2 mb-3">
                                    <i class="bi bi-basket3"></i> 재료 <span class="text-danger">*</span>
                                </h5>
                                
                                <div id="ingredientsList">
                                    <!-- 재료 항목 템플릿 (JavaScript로 동적 추가) -->
                                    <div class="ingredient-item mb-2">
                                        <div class="row">
                                            <div class="col-md-5">
                                                <input type="text" 
                                                       class="form-control" 
                                                       name="ingredients[0].name" 
                                                       placeholder="재료명 (예: 돼지고기)" 
                                                       required>
                                            </div>
                                            <div class="col-md-5">
                                                <input type="text" 
                                                       class="form-control" 
                                                       name="ingredients[0].measure" 
                                                       placeholder="분량 (예: 300g)" 
                                                       required>
                                            </div>
                                            <div class="col-md-2">
                                                <button type="button" class="btn btn-danger" onclick="removeIngredient(this)">
                                                    <i class="bi bi-trash"></i>
                                                </button>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                
                                <button type="button" class="btn btn-outline-primary btn-sm" onclick="addIngredient()">
                                    <i class="bi bi-plus-circle"></i> 재료 추가
                                </button>
                            </div>
                            
                            <!-- 조리 방법 (텍스트) -->
                            <div class="mb-4">
                                <h5 class="border-bottom pb-2 mb-3">
                                    <i class="bi bi-list-ol"></i> 조리 방법 <span class="text-danger">*</span>
                                </h5>
                                
                                <textarea class="form-control" 
                                          id="instructions" 
                                          th:field="*{instructions}" 
                                          rows="10" 
                                          placeholder="조리 방법을 자유롭게 입력하세요.&#10;&#10;예시:&#10;1. 돼지고기를 한입 크기로 썰어주세요.&#10;2. 김치는 적당한 크기로 썰어주세요.&#10;3. 냄비에 김치를 넣고 볶다가 돼지고기를 넣어 함께 볶아주세요.&#10;4. 물을 넣고 끓여주세요.&#10;5. 두부와 양념을 넣고 10분간 더 끓이면 완성!"
                                          required></textarea>
                                <small class="text-muted">단계별로 자유롭게 작성해주세요.</small>
                            </div>
                            
                            <!-- 추가 정보 -->
                            <div class="mb-4">
                                <h5 class="border-bottom pb-2 mb-3">추가 정보 (선택사항)</h5>
                                
                                <div class="mb-3">
                                    <label for="tips" class="form-label">요리 팁</label>
                                    <textarea class="form-control" id="tips" th:field="*{tips}" rows="3" placeholder="저감 조리법 팁이나 주의사항을 입력하세요"></textarea>
                                </div>
                                
                                <div class="mb-3">
                                    <label for="youtubeUrl" class="form-label">YouTube 링크</label>
                                    <input type="url" class="form-control" id="youtubeUrl" th:field="*{youtubeUrl}" placeholder="https://www.youtube.com/watch?v=...">
                                </div>
                                
                                <div class="mb-3">
                                    <label for="sourceUrl" class="form-label">참고 링크</label>
                                    <input type="url" class="form-control" id="sourceUrl" th:field="*{sourceUrl}" placeholder="https://...">
                                </div>
                            </div>
                            
                            <!-- 버튼 -->
                            <div class="d-flex justify-content-between">
                                <button type="button" class="btn btn-secondary" onclick="history.back()">
                                    <i class="bi bi-arrow-left"></i> 취소
                                </button>
                                <button type="submit" class="btn btn-primary btn-lg">
                                    <i class="bi bi-check-circle"></i>
                                    <span th:text="${isEdit ? '수정 완료' : '등록하기'}">등록하기</span>
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </main>
    
    <div th:replace="~{fragments/footer :: footer}"></div>
    
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
    
    <script>
        // 재료 카운터
        let ingredientCount = 1;
        
        // 재료 추가
        function addIngredient() {
            const ingredientsList = document.getElementById('ingredientsList');
            const newIngredient = document.createElement('div');
            newIngredient.className = 'ingredient-item mb-2';
            newIngredient.innerHTML = `
                <div class="row">
                    <div class="col-md-5">
                        <input type="text" class="form-control" name="ingredients[${ingredientCount}].name" placeholder="재료명" required>
                    </div>
                    <div class="col-md-5">
                        <input type="text" class="form-control" name="ingredients[${ingredientCount}].measure" placeholder="분량" required>
                    </div>
                    <div class="col-md-2">
                        <button type="button" class="btn btn-danger" onclick="removeIngredient(this)">
                            <i class="bi bi-trash"></i>
                        </button>
                    </div>
                </div>
            `;
            ingredientsList.appendChild(newIngredient);
            ingredientCount++;
        }
        
        // 재료 삭제
        function removeIngredient(button) {
            const ingredientItem = button.closest('.ingredient-item');
            if (document.querySelectorAll('.ingredient-item').length > 1) {
                ingredientItem.remove();
            } else {
                alert('최소 1개의 재료가 필요합니다.');
            }
        }
        
        // 대표 이미지 미리보기
        function previewMainImage(event) {
            const file = event.target.files[0];
            if (file) {
                const reader = new FileReader();
                reader.onload = function(e) {
                    document.getElementById('mainImagePreviewImg').src = e.target.result;
                    document.getElementById('mainImagePreview').style.display = 'block';
                };
                reader.readAsDataURL(file);
            }
        }
    </script>
</body>
</html>
```

---

### 7. 레시피 상세 페이지 수정 (수정/삭제 버튼 추가)

**파일**: `src/main/resources/templates/recipes/detail.html`

```html
<!-- 기존 코드에서 Sidebar의 Actions Card 안에 추가 -->
<div class="card-body">
    <div class="d-grid gap-2">
        
        <!-- 사용자가 작성한 레시피인 경우 수정/삭제 버튼 표시 -->
        <div th:if="${recipe.sourceApi == 'USER' and recipe.authorId == currentUser?.id}" class="mb-3">
            <div class="alert alert-info small mb-2">
                <i class="bi bi-info-circle"></i> 내가 작성한 레시피입니다
            </div>
            <a th:href="@{/recipes/{id}/edit(id=${recipe.id})}" class="btn btn-warning w-100 mb-2">
                <i class="bi bi-pencil-square"></i> 수정하기
            </a>
            <form th:action="@{/recipes/{id}/delete(id=${recipe.id})}" 
                  method="post" 
                  onsubmit="return confirm('정말 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.');">
                <button type="submit" class="btn btn-danger w-100">
                    <i class="bi bi-trash"></i> 삭제하기
                </button>
            </form>
            <hr class="my-3">
        </div>
        
        <!-- 기존 공구 만들기 버튼 등... -->
        
    </div>
</div>
```

---

### 8. SecurityConfig 수정 (엔드포인트 권한 설정)

**파일**: `src/main/java/com/recipemate/global/config/SecurityConfig.java`

```java
// authorizeHttpRequests 부분에 추가
.requestMatchers(
    "/recipes/new",           // 레시피 작성 폼 (로그인 필요)
    "/recipes/*/edit",        // 레시피 수정 폼 (로그인 필요)
    "/recipes/my"             // 내 레시피 목록 (로그인 필요)
).authenticated()  // 인증 필요

.requestMatchers(
    "/recipes",               // POST: 레시피 생성
    "/recipes/*/delete"       // POST: 레시피 삭제
).authenticated()
```

---

## 🎉 완료 체크리스트

### Phase 1: 백엔드 기반 작업
- [ ] Recipe 엔티티에 `author` 필드 추가
- [ ] Recipe 엔티티에 업데이트 메서드 추가 (updateBasicInfo, updateNutritionInfo 등)
- [ ] RecipeRepository에 `findByAuthor` 등 메서드 추가
- [ ] RecipeCreateRequest DTO 생성
- [ ] RecipeUpdateRequest DTO 생성
- [ ] DB 마이그레이션 실행 (user_id 컬럼 추가)

### Phase 2: Service 계층
- [ ] RecipeService에 `createUserRecipe()` 구현
- [ ] RecipeService에 `updateUserRecipe()` 구현
- [ ] RecipeService에 `deleteUserRecipe()` 구현
- [ ] RecipeService에 `getUserRecipes()` 구현
- [ ] ImageUploadUtil 활용한 이미지 업로드 로직 통합

### Phase 3: Controller 계층
- [ ] GET /recipes/new 엔드포인트 추가
- [ ] POST /recipes 엔드포인트 추가
- [ ] GET /recipes/{id}/edit 엔드포인트 추가
- [ ] POST /recipes/{id}/edit 엔드포인트 추가
- [ ] POST /recipes/{id}/delete 엔드포인트 추가
- [ ] GET /recipes/my 엔드포인트 추가

### Phase 4: 프론트엔드
- [ ] `recipes/form.html` 생성 (작성/수정 폼)
- [ ] `recipes/detail.html` 수정 (수정/삭제 버튼)
- [ ] `recipes/list.html` 수정 (작성 버튼)
- [ ] `recipes/my-recipes.html` 생성 (내 레시피 목록)
- [ ] JavaScript 재료 동적 추가/삭제 기능
- [ ] 이미지 미리보기 기능

### Phase 5: 보안 및 권한
- [ ] SecurityConfig 엔드포인트 권한 설정
- [ ] Service 계층 권한 검사 로직
- [ ] Controller에서 UserDetails 주입

### Phase 6: 테스트
- [ ] 레시피 생성 테스트
- [ ] 레시피 수정 테스트 (본인 + 타인)
- [ ] 레시피 삭제 테스트 (본인 + 타인)
- [ ] 이미지 업로드 통합 테스트
- [ ] 권한 검사 테스트

---

## 🚀 핵심 구현 포인트

1. **이미지 업로드는 이미 준비됨** ✅
   - `ImageUploadUtil`을 그대로 사용
   - 대표 이미지 1장만 업로드

2. **간소화된 형식** ✅
   - RecipeIngredient (name, measure) - 재료
   - instructions (텍스트) - 조리방법
   - 단계별 이미지는 없음 (TheMealDB 형식과 동일)

3. **권한 관리 철저히** ⚠️
   - Recipe.canModify(User) 메서드 활용
   - Service 계층에서 권한 체크
   - 403 Forbidden 예외 처리

4. **사용자 경험 고려** ✨
   - JavaScript로 재료 동적 추가/삭제
   - 이미지 미리보기 기능
   - 유효성 검사 메시지
   - Flash 메시지로 성공/실패 알림

---

이 가이드를 따라 단계별로 구현하면, Cloudinary 이미지 업로드와 함께 **간단하고 실용적인** 사용자 레시피 CRUD 기능을 구현할 수 있습니다! 🎉

