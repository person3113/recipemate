# 사용자 레시피를 RecipeStep으로 마이그레이션

## 작업 일자
2025년 11월 14일

## 배경 및 목적

### 문제 상황
사용자가 작성한 레시피만 `instructions` 필드(TEXT)를 사용하고, API 레시피들은 `RecipeStep` 테이블을 사용하여 일관성 없음

### 개선 목표
모든 레시피가 `RecipeStep`을 사용하도록 통일하여 구조적 일관성 확보

---

## 현재 구조 분석

### ⚠️ 실제 DB 확인 결과

**모든 레시피가 `RecipeStep` 테이블을 사용하고 있음!**
- `instructions` 필드는 **모두 NULL**
- TheMealDB도 `RecipeMapper`에서 `instructions` 텍스트를 파싱하여 `RecipeStep`으로 변환

### Before (변경 전)

| 출처 | 조리법 저장 방식 | 단계별 이미지 | 화면 표시 |
|------|---------------|-------------|---------|
| TheMealDB | `RecipeStep` 테이블 (파싱됨) | ❌ 없음 | `manualSteps` 단계별 |
| 식품안전나라 | `RecipeStep` 테이블 | ✅ 가능 | `manualSteps` 단계별 |
| **사용자 레시피** | `instructions` (TEXT) - **사용 안 함** | ❌ 없음 | 단계별 입력 없음 |

**문제점:**
- 사용자 레시피만 조리법 입력 방식이 다름 (단일 textarea)
- 사용자가 직접 단계를 구분해서 입력해야 함
- 일관성 없는 UX

### After (변경 후)

| 출처 | 조리법 저장 방식 | 단계별 이미지 | 화면 표시 |
|------|---------------|-------------|---------|
| TheMealDB | `RecipeStep` 테이블 (파싱됨) | ❌ 없음 | `manualSteps` 단계별 |
| 식품안전나라 | `RecipeStep` 테이블 | ✅ 가능 | `manualSteps` 단계별 |
| **사용자 레시피** | **`RecipeStep` 테이블** | ❌ 없음 (추가 가능) | **`manualSteps` 단계별** |

**개선 사항:**
- ✅ 모든 레시피가 `RecipeStep` 사용으로 완전히 통일
- ✅ 사용자에게 단계별 입력 UI 제공 (UX 개선)
- ✅ 화면 표시 로직 완전히 통일
- ✅ 향후 사용자 레시피에도 단계별 이미지 추가 가능

---

## 변경 사항

### 1️⃣ DTO 수정

#### RecipeCreateRequest.java
**변경 내용:**
- `instructions` 필드 **제거**
- `steps` 필드 **추가** (List<StepDto>)

```java
// Before
@NotBlank(message = "조리 방법을 입력해주세요")
private String instructions;

// After (제거됨)

// 추가됨
@Valid
@NotNull(message = "조리 단계를 최소 1개 이상 입력해주세요")
@Size(min = 1, message = "조리 단계를 최소 1개 이상 입력해주세요")
@Builder.Default
private List<StepDto> steps = new ArrayList<>();

@Data
@NoArgsConstructor
@AllArgsConstructor
public static class StepDto {
    @NotBlank(message = "조리 단계 설명을 입력해주세요")
    private String description;
}
```

#### RecipeUpdateRequest.java
**동일하게 수정**

---

### 2️⃣ Service 로직 수정

#### RecipeService.java - createUserRecipe()

```java
// Before
.instructions(request.getInstructions())  // 텍스트 조리방법

// After
.instructions(null)  // 사용자 레시피는 instructions 사용 안 함

// 4. 조리 단계 추가 (RecipeStep 사용) - 새로 추가
int stepNumber = 1;
for (RecipeCreateRequest.StepDto stepDto : request.getSteps()) {
    RecipeStep step = RecipeStep.builder()
            .stepNumber(stepNumber++)
            .description(stepDto.getDescription())
            .imageUrl(null)  // 사용자 레시피는 단계별 이미지 없음
            .build();
    recipe.addStep(step);
}
```

#### RecipeService.java - updateUserRecipe()

```java
// Before
recipe.updateBasicInfo(
    request.getTitle(),
    request.getCategory(),
    request.getArea(),
    request.getInstructions(),  // ← 제거
    request.getTips(),
    request.getYoutubeUrl(),
    request.getSourceUrl()
);

// After
recipe.updateBasicInfo(
    request.getTitle(),
    request.getCategory(),
    request.getArea(),
    request.getTips(),
    request.getYoutubeUrl(),
    request.getSourceUrl()
);

// 6. 조리 단계 업데이트 - 새로 추가
recipe.getSteps().clear();
int stepNumber = 1;
for (RecipeUpdateRequest.StepDto stepDto : request.getSteps()) {
    RecipeStep step = RecipeStep.builder()
            .stepNumber(stepNumber++)
            .description(stepDto.getDescription())
            .imageUrl(null)
            .build();
    recipe.addStep(step);
}
```

**이미지 삭제 처리도 추가:**
```java
// 기존 이미지 삭제 (새 이미지 업로드 성공 시)
String oldImageUrl = recipe.getFullImageUrl();
if (request.getMainImage() != null && !request.getMainImage().isEmpty()) {
    List<String> uploadedUrls = imageUploadUtil.uploadImages(...);
    if (!uploadedUrls.isEmpty()) {
        recipe.updateMainImage(uploadedUrls.get(0));
        
        if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
            try {
                imageUploadUtil.deleteImages(List.of(oldImageUrl));
            } catch (Exception e) {
                log.warn("Failed to delete old image: {}", oldImageUrl, e);
            }
        }
    }
}
```

---

### 3️⃣ Entity 수정

#### Recipe.java - updateBasicInfo()

```java
// Before
public void updateBasicInfo(String title, String category, String area,
                             String instructions, String tips,
                             String youtubeUrl, String sourceUrl)

// After
public void updateBasicInfo(String title, String category, String area,
                             String tips, String youtubeUrl, String sourceUrl)
```

`instructions` 파라미터 제거

---

### 4️⃣ 프론트엔드 수정

#### form.html - 조리 방법 입력

**Before (textarea 1개):**
```html
<textarea class="form-control"
          id="instructions"
          th:field="*{instructions}"
          rows="10"
          placeholder="조리 방법을 자유롭게 입력하세요..."
          required></textarea>
```

**After (단계별 입력):**
```html
<div id="stepsList">
    <!-- 단계별 입력 폼 (동적 추가/삭제 가능) -->
    <div class="step-item mb-3">
        <div class="d-flex align-items-start">
            <span class="badge bg-primary rounded-circle me-2 mt-1">1</span>
            <div class="flex-grow-1 me-2">
                <textarea class="form-control" 
                          name="steps[0].description" 
                          rows="3" 
                          placeholder="조리 단계를 입력하세요" 
                          required></textarea>
            </div>
            <button type="button" class="btn btn-danger" onclick="removeStep(this)">
                <i class="bi bi-trash"></i>
            </button>
        </div>
    </div>
</div>

<button type="button" class="btn btn-outline-primary btn-sm" onclick="addStep()">
    <i class="bi bi-plus-circle"></i> 단계 추가
</button>
```

#### JavaScript 함수 추가

```javascript
let stepCount = document.querySelectorAll('.step-item').length;

// 조리 단계 추가
function addStep() {
    const stepsList = document.getElementById('stepsList');
    const newStep = document.createElement('div');
    newStep.className = 'step-item mb-3';
    const stepNumber = stepCount + 1;
    newStep.innerHTML = `...`;
    stepsList.appendChild(newStep);
    stepCount++;
}

// 조리 단계 삭제
function removeStep(button) {
    const stepItem = button.closest('.step-item');
    if (document.querySelectorAll('.step-item').length > 1) {
        stepItem.remove();
        reindexSteps();
    } else {
        alert('최소 1개의 조리 단계가 필요합니다.');
    }
}

// 조리 단계 인덱스 및 번호 재정렬
function reindexSteps() {
    const stepItems = document.querySelectorAll('.step-item');
    stepItems.forEach((item, index) => {
        const badge = item.querySelector('.badge');
        if (badge) badge.textContent = index + 1;
        
        const textarea = item.querySelector('textarea[name*=".description"]');
        if (textarea) textarea.name = `steps[${index}].description`;
    });
    stepCount = stepItems.length;
}
```

---

### 5️⃣ Controller 수정

#### RecipeController.java - editRecipeForm()

```java
// 조리 단계 변환 (manualSteps -> steps) - 새로 추가
if (recipeDetail.getManualSteps() != null) {
    java.util.List<RecipeUpdateRequest.StepDto> steps = new java.util.ArrayList<>();
    for (RecipeDetailResponse.ManualStep step : recipeDetail.getManualSteps()) {
        RecipeUpdateRequest.StepDto stepDto = new RecipeUpdateRequest.StepDto();
        stepDto.setDescription(step.getDescription());
        steps.add(stepDto);
## instructions 필드는 유지 (하지만 실제로는 미사용)
    recipe.setSteps(steps);
}
```

- DB 스키마에 존재 (마이그레이션 복잡도 방지)
- `RecipeDetailResponse` DTO에서 하위 호환성 유지
- 실제로는 **모든 레시피가 NULL**
## instructions 필드는 유지
**실제 사용 현황 (DB 확인 결과):**
### ⚠️ 중요: instructions 필드는 삭제하지 않음

| `instructions` | ❌ null | ❌ null | ❌ null |
| `RecipeStep` | ✅ 사용 (파싱됨) | ✅ 사용 | ✅ 사용 |

**RecipeMapper 처리:**
- TheMealDB API의 `instructions` 텍스트를 받아서
- 줄바꿈(`\r\n` 또는 `\n`) 기준으로 파싱
- 각 줄을 `RecipeStep`으로 변환하여 저장
- DB의 `instructions` 필드는 null로 유지
- 텍스트 형식의 조리법을 저장

**사용 현황:**
<!-- 모든 레시피가 RecipeStep을 가지므로 항상 단계별 표시 -->
|------|----------|------------|------------|
| `instructions` | ✅ 사용 | ❌ null | ❌ null |
| `RecipeStep` | ❌ 없음 | ✅ 사용 | ✅ 사용 |

<!-- instructions는 항상 null이므로 이 분기는 실행되지 않음 -->
```html
<!-- RecipeStep이 있으면 단계별 표시 -->
<div th:if="${recipe.manualSteps != null and !recipe.manualSteps.isEmpty()}">
    <!-- 단계별 표시 -->
</div>

<!-- RecipeStep이 없으면 instructions 텍스트 표시 (TheMealDB용) -->
<p th:if="${recipe.manualSteps == null or recipe.manualSteps.isEmpty()}"
   th:text="${recipe.instructions}">조리 방법</p>
```

---

## 테스트 체크리스트

### 레시피 작성
- [ ] 조리 단계를 추가/삭제할 수 있는지 확인
- [ ] 단계 번호가 자동으로 재정렬되는지 확인
- [ ] 최소 1개 단계가 필요한지 검증되는지 확인
- [ ] 레시피 작성 후 DB에 RecipeStep이 저장되는지 확인

### 레시피 수정
- [ ] 기존 단계가 정상적으로 로드되는지 확인
- [ ] 단계를 추가/삭제/수정할 수 있는지 확인
- [ ] 수정 후 DB에 변경사항이 반영되는지 확인
- [ ] 이미지를 변경하면 기존 이미지가 Cloudinary에서 삭제되는지 확인

### 레시피 상세
- [ ] 사용자 레시피가 단계별로 표시되는지 확인
- [ ] 식품안전나라 레시피도 정상 표시되는지 확인
- [ ] TheMealDB 레시피도 정상 표시되는지 확인

---

## 기술적 세부사항

### RecipeStep 저장
- `orphanRemoval = true` 설정으로 자동 삭제
- 기존 steps를 `clear()`하고 새로 추가하는 방식
- 트랜잭션 내에서 처리되어 안전

### 이미지 삭제
- 새 이미지 업로드 **성공 후**에만 기존 이미지 삭제
- 삭제 실패 시 로그만 남기고 계속 진행
- DB 업데이트는 성공적으로 완료

### 빈 값 필터링
재료와 조리 단계 모두 빈 값 필터링 적용:
```java
if (request.getSteps() != null) {
    request.setSteps(
        request.getSteps().stream()
            .filter(step -> step.getDescription() != null 
                         && !step.getDescription().trim().isEmpty())
            .collect(Collectors.toList())
    );
}
```

---

## 완료 ✅

사용자 레시피가 이제 RecipeStep을 사용하여 일관된 구조로 저장됩니다!

### 추가 개선 가능 사항 (향후)
- [ ] 사용자 레시피에도 단계별 이미지 업로드 기능 추가
- [ ] 드래그 앤 드롭으로 단계 순서 변경 기능
- [ ] 조리 시간, 난이도 등 추가 정보 입력

