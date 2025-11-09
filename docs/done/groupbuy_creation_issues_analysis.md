# 공구 만들기 기능 문제점 분석 및 개선 계획

## 1. 개요

공구 만들기 폼(`/group-purchases/new`)의 현재 구현 상태를 분석하고, GUIDELINE.md에 따라 단계적으로 개선할 계획을 수립합니다.

---

## 2. 발견된 문제점들

### 2.1 이미지 업로드 - 실제 파일 저장 미구현 ⚠️ **Critical**

**현상:**
- 업로드된 이미지가 화면에 표시되지 않음
- `<img>` 태그의 alt 텍스트("공구 사진")만 보임
- 예시: `<img src="/images/d90ad864-7ecf-4fa2-9454-45ec45e37415.png">` → 이미지 대신 "공구 사진" 텍스트만 표시

**원인:**
- `ImageUploadUtil.java:67` - 실제 파일 저장 로직이 구현되지 않음
```java
// TODO: 실제 파일 저장 로직 구현
// 현재는 URL만 생성하고 파일을 디스크에 저장하지 않음
```

**현재 구현 상태:**
- ✅ 다중 이미지 업로드 UI 구현됨 (`form.html:288` - `multiple` 속성)
- ✅ DTO에서 다중 파일 수신 가능 (`List<MultipartFile> imageFiles`)
- ✅ 최대 3장 제한 UI 표시
- ❌ 실제 파일 저장 로직 없음

**기획 스펙:**
- **업로드:** 최대 3장, JPG/PNG, 각 **5MB 이하**
- **레시피 기반:** 레시피 이미지 자동 첨부, 추가 이미지 업로드 가능
- **삭제:** 각 이미지에 [X] 버튼

**영향도:** 높음 (핵심 기능 미작동)

---

### 2.2 해결 방안: Cloudinary 사용 (권장 ⭐)

#### 2.2.1 Cloudinary 선택 이유

**장점:**
- ✅ **개발/배포 환경 통일**: 동일한 설정으로 모든 환경에서 작동
- ✅ **자동 이미지 최적화**: 5MB 원본 업로드 → 자동 리사이징/압축 → 웹 최적화 파일 제공
- ✅ **CDN 기본 제공**: 빠른 이미지 로딩 속도
- ✅ **파일 시스템 관리 불필요**: 로컬 디스크 용량 걱정 없음
- ✅ **팀 협업 용이**: 모든 개발자가 동일한 이미지 접근 가능

**이미지 크기 처리 방식:**
```
사용자 업로드 (최대 5MB) 
  → Cloudinary 업로드 
  → 자동 리사이징 (800x600 max)
  → 자동 압축 (WebP/JPEG 최적화)
  → 최종 제공 (200-300KB) via CDN
```

> **💡 5MB vs 리사이징 차이점:**
> - **5MB**: 사용자가 업로드할 수 있는 **원본 파일의 최대 크기**
> - **리사이징**: Cloudinary가 업로드 후 자동으로 **웹 최적화된 작은 파일**로 변환
> - 사용자는 큰 파일(5MB)을 업로드할 수 있지만, 실제 웹에서 로딩되는 것은 최적화된 작은 파일

---

#### 2.2.2 구현 단계

**Step 1: 의존성 추가**

`build.gradle`에 Cloudinary SDK 추가:
```gradle
dependencies {
    // 기존 의존성들...
    
    // Cloudinary for image upload
    implementation 'com.cloudinary:cloudinary-http45:1.39.0'
}
```

---

**Step 2: 환경 변수 설정 (.env 파일 사용)**

프로젝트는 이미 `.env` 파일로 환경 변수를 관리하고 있으므로, `.env` 파일에 Cloudinary 설정을 추가합니다.

**`.env` 파일 수정:**
```bash
# RecipeMate API Environment Variables
# WARNING: This file contains sensitive information. Never commit to Git!

# =====================================================
# Cloudinary Configuration (Image Upload Service)
# =====================================================
# Get your credentials from: https://cloudinary.com/console
# Cloud name: dt9xgsr2z
# API Key: zxxxxxxxxxxxxxxxxxx (mediaflows) or yyyyyyyy (Root)
# API Secret: (obtain from Cloudinary dashboard - keep it secret!)
CLOUDINARY_URL=cloudinary://xxxxxxxxxxx:<your_api_secret>@dt9xgsr2z

# Alternative format (if you prefer separate variables):
# CLOUDINARY_CLOUD_NAME=dt9xgsr2z
# CLOUDINARY_API_KEY=xxxxxxxxxxxxxxxx
# CLOUDINARY_API_SECRET=<your_api_secret>

# =====================================================
# Application Configuration
# =====================================================
SPRING_PROFILES_ACTIVE=prod
```

**`.env.example` 파일도 업데이트:**
```bash
# (기존 내용 유지)

# =====================================================
# Cloudinary Configuration
# =====================================================
# Sign up at https://cloudinary.com (free tier available)
# Go to Dashboard and copy your credentials
CLOUDINARY_URL=cloudinary://<api_key>:<api_secret>@<cloud_name>
```

> **💡 환경 변수 설정 방법 비교:**
> 
> 프로젝트가 이미 `.env` 파일을 사용하고 있으므로, **`.env` 파일에 추가만 하면 됩니다.**
> **Docker Compose 배포 시:** `docker-compose.yml`에서 자동으로 `.env` 파일을 읽으므로 추가 설정 불필요

---

**Step 3: Spring Boot에서 .env 파일 읽기**

Spring Boot는 기본적으로 `.env` 파일을 직접 읽지 않으므로, `application.yml`에서 환경 변수를 참조합니다.

**`application.yml` 수정:**
```yaml
spring:
  application:
    name: recipemate-api
  
  # 파일 업로드 설정
  servlet:
    multipart:
      max-file-size: 5MB      # 개별 파일 최대 크기
      max-request-size: 15MB  # 전체 요청 최대 크기 (3장 × 5MB)
      enabled: true

# Cloudinary 설정
cloudinary:
  url: ${CLOUDINARY_URL}  # .env 파일의 CLOUDINARY_URL 환경 변수 참조
```

---

**Step 4: Cloudinary Config 클래스 작성**

`src/main/java/com/recipemate/global/config/CloudinaryConfig.java`:
```java
package com.recipemate.global.config;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    @Value("${cloudinary.url}")
    private String cloudinaryUrl;

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(cloudinaryUrl);
    }
}
```

---

**Step 5: ImageUploadUtil 수정**

`src/main/java/com/recipemate/global/util/ImageUploadUtil.java`:
```java
package com.recipemate.global.util;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImageUploadUtil {

    private final Cloudinary cloudinary;

    /**
     * 단일 이미지 업로드
     * 
     * @param file 업로드할 이미지 파일
     * @return Cloudinary CDN URL
     * @throws IOException 파일 업로드 실패 시
     */
    public String uploadImage(MultipartFile file) throws IOException {
        // 1. 파일 유효성 검증
        validateImage(file);
        
        // 2. Cloudinary 업로드 옵션 설정
        Map<String, Object> uploadParams = ObjectUtils.asMap(
            "folder", "recipemate/group-purchases",      // 저장 폴더
            "resource_type", "image",                    // 리소스 타입
            "transformation", new Transformation()       // 이미지 변환 설정
                .width(800)                              // 최대 너비
                .height(600)                             // 최대 높이
                .crop("limit")                           // 비율 유지하며 크기 제한
                .quality("auto")                         // 자동 품질 최적화
                .fetchFormat("auto")                     // 브라우저에 따라 WebP/JPEG 자동 선택
        );
        
        // 3. Cloudinary에 업로드
        log.info("Uploading image to Cloudinary: {}", file.getOriginalFilename());
        Map uploadResult = cloudinary.uploader().upload(
            file.getBytes(), 
            uploadParams
        );
        
        // 4. HTTPS URL 반환
        String imageUrl = uploadResult.get("secure_url").toString();
        log.info("Image uploaded successfully: {}", imageUrl);
        
        return imageUrl;
    }

    /**
     * 다중 이미지 업로드 (최대 3장)
     * 
     * @param files 업로드할 이미지 파일 리스트
     * @return Cloudinary CDN URL 리스트
     * @throws IOException 파일 업로드 실패 시
     */
    public List<String> uploadImages(List<MultipartFile> files) throws IOException {
        List<String> imageUrls = new ArrayList<>();
        
        if (files == null || files.isEmpty()) {
            return imageUrls;
        }
        
        // 최대 3장 제한
        int maxImages = Math.min(files.size(), 3);
        log.info("Uploading {} images (max 3)", maxImages);
        
        for (int i = 0; i < maxImages; i++) {
            MultipartFile file = files.get(i);
            
            // 빈 파일 건너뛰기
            if (file.isEmpty()) {
                log.warn("Empty file at index {}, skipping", i);
                continue;
            }
            
            try {
                String imageUrl = uploadImage(file);
                imageUrls.add(imageUrl);
            } catch (Exception e) {
                log.error("Failed to upload image at index {}: {}", i, e.getMessage());
                // 일부 이미지 업로드 실패 시에도 계속 진행
                // 전체 실패를 원하면 throw new IOException(...)로 변경
            }
        }
        
        log.info("Successfully uploaded {} out of {} images", imageUrls.size(), maxImages);
        return imageUrls;
    }

    /**
     * 이미지 파일 유효성 검증
     * 
     * @param file 검증할 파일
     * @throws IllegalArgumentException 유효하지 않은 파일인 경우
     */
    private void validateImage(MultipartFile file) {
        // 파일이 비어있는지 확인
        if (file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다");
        }
        
        // 파일 크기 체크 (5MB 제한)
        long maxSize = 5 * 1024 * 1024; // 5MB in bytes
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException(
                String.format("이미지 크기는 5MB 이하여야 합니다 (현재: %.2fMB)", 
                    file.getSize() / (1024.0 * 1024.0))
            );
        }
        
        // MIME 타입 체크
        String contentType = file.getContentType();
        if (contentType == null || 
            (!contentType.equals("image/jpeg") && 
             !contentType.equals("image/png") &&
             !contentType.equals("image/jpg"))) {
            throw new IllegalArgumentException(
                "JPG 또는 PNG 형식의 이미지만 업로드 가능합니다"
            );
        }
    }
}
```

---

**Step 6: GroupBuyService에서 ImageUploadUtil 사용**

`GroupBuyService.java`의 공구 생성 메서드를 수정하여 실제로 이미지를 업로드하도록 합니다.

```java
@Service
@RequiredArgsConstructor
public class GroupBuyService {
    
    private final ImageUploadUtil imageUploadUtil;
    // ... 다른 의존성들
    
    @Transactional
    public Long createGroupBuy(CreateGroupBuyRequest request, String username) {
        // 1. 이미지 업로드 처리
        List<String> imageUrls = new ArrayList<>();
        if (request.getImageFiles() != null && !request.getImageFiles().isEmpty()) {
            try {
                imageUrls = imageUploadUtil.uploadImages(request.getImageFiles());
            } catch (IOException e) {
                log.error("Failed to upload images", e);
                throw new RuntimeException("이미지 업로드에 실패했습니다: " + e.getMessage());
            }
        }
        
        // 2. GroupBuy 엔티티 생성 (imageUrls 포함)
        GroupBuy groupBuy = GroupBuy.builder()
            .title(request.getTitle())
            .content(request.getContent())
            .imageUrls(imageUrls)  // Cloudinary URL 저장
            // ... 나머지 필드
            .build();
        
        // 3. 저장
        return groupBuyRepository.save(groupBuy).getId();
    }
}
```

---

### 2.3 이미지 업로드 UI 개선사항 🎨

**현재 구현:**
- 다중 업로드 지원 (`multiple` 속성)
- 최대 3장 안내 문구

**추가 필요 기능:**
- [ ] 이미지 미리보기 기능
- [ ] 각 이미지별 삭제 버튼 [X]
- [ ] 업로드 중 로딩 인디케이터
- [ ] 5MB 초과 시 에러 메시지

**구현 예시 (form.html):**
```html
<div class="mb-3">
    <label for="imageFiles" class="form-label fw-semibold">
        이미지 업로드 <span class="text-muted small">(최대 3장, 각 5MB 이하)</span>
    </label>
    <input type="file" 
           id="imageFiles" 
           name="imageFiles" 
           class="form-control" 
           multiple 
           accept="image/jpeg,image/png,image/jpg"
           onchange="previewImages(event)">
    <div class="form-text">
        <i class="bi bi-image"></i> JPG, PNG 형식의 이미지를 업로드할 수 있습니다.
    </div>
    
    <!-- 이미지 미리보기 영역 -->
    <div id="imagePreviewContainer" class="mt-3 d-flex gap-2 flex-wrap"></div>
</div>

<script>
// 이미지 미리보기 및 삭제 기능
function previewImages(event) {
    const container = document.getElementById('imagePreviewContainer');
    container.innerHTML = '';
    
    const files = Array.from(event.target.files).slice(0, 3); // 최대 3장
    const maxSize = 5 * 1024 * 1024; // 5MB
    
    files.forEach((file, index) => {
        // 파일 크기 체크
        if (file.size > maxSize) {
            alert(`${file.name}의 크기가 5MB를 초과합니다 (${(file.size / (1024 * 1024)).toFixed(2)}MB)`);
            return;
        }
        
        // MIME 타입 체크
        if (!file.type.match('image/(jpeg|jpg|png)')) {
            alert(`${file.name}은(는) JPG 또는 PNG 형식이 아닙니다`);
            return;
        }
        
        const reader = new FileReader();
        reader.onload = function(e) {
            const previewDiv = document.createElement('div');
            previewDiv.className = 'position-relative';
            previewDiv.innerHTML = `
                <img src="${e.target.result}" 
                     class="img-thumbnail" 
                     style="width: 150px; height: 150px; object-fit: cover;"
                     alt="미리보기 ${index + 1}">
                <button type="button" 
                        class="btn btn-sm btn-danger position-absolute top-0 end-0 m-1"
                        onclick="removeImage(${index})"
                        title="이미지 삭제">
                    <i class="bi bi-x-lg"></i>
                </button>
                <div class="small text-muted text-center mt-1">
                    ${(file.size / 1024).toFixed(0)}KB
                </div>
            `;
            container.appendChild(previewDiv);
        };
        reader.readAsDataURL(file);
    });
}

// 이미지 삭제 기능
function removeImage(index) {
    const input = document.getElementById('imageFiles');
    const dt = new DataTransfer();
    const files = Array.from(input.files);
    
    files.forEach((file, i) => {
        if (i !== index) dt.items.add(file);
    });
    
    input.files = dt.files;
    previewImages({ target: input });
}
</script>
```

---

### 2.4 1인당 가격 자동 계산 기능 누락 📊

**현상:**
- 총 가격과 인원수 입력 필드는 있지만, 1인당 가격이 자동으로 계산되지 않음
- 사용자가 직접 계산해야 함

**원인:**
- `form.html`에 계산 로직(JavaScript)이 없음

**영향도:** 중간 (사용성 문제)

**해결 방안:**
```javascript
// form.html에 추가할 스크립트
document.addEventListener('DOMContentLoaded', function() {
    const totalPriceInput = document.getElementById('totalPrice');
    const targetHeadcountInput = document.getElementById('targetHeadcount');
    
    // 1인당 가격 표시 요소 생성
    let pricePerPersonDiv = document.getElementById('pricePerPerson');
    if (!pricePerPersonDiv) {
        pricePerPersonDiv = document.createElement('div');
        pricePerPersonDiv.id = 'pricePerPerson';
        pricePerPersonDiv.className = 'alert alert-info mt-2';
        targetHeadcountInput.parentElement.appendChild(pricePerPersonDiv);
    }
    
    function calculatePricePerPerson() {
        const total = parseInt(totalPriceInput.value) || 0;
        const headcount = parseInt(targetHeadcountInput.value) || 1;
        
        if (headcount === 0 || headcount < 2) {
            pricePerPersonDiv.innerHTML = '<i class="bi bi-exclamation-triangle"></i> 인원수를 2명 이상 입력해주세요';
            pricePerPersonDiv.className = 'alert alert-warning mt-2';
            return;
        }
        
        const perPerson = Math.floor(total / headcount);
        pricePerPersonDiv.innerHTML = `
            <i class="bi bi-calculator"></i> 
            <strong>1인당 가격: ${perPerson.toLocaleString()}원</strong>
            <span class="text-muted small ms-2">(${total.toLocaleString()}원 ÷ ${headcount}명)</span>
        `;
        pricePerPersonDiv.className = 'alert alert-info mt-2';
    }
    
    // 이벤트 리스너 등록
    totalPriceInput.addEventListener('input', calculatePricePerPerson);
    targetHeadcountInput.addEventListener('input', calculatePricePerPerson);
    
    // 초기 계산 (수정 모드 대응)
    calculatePricePerPerson();
});
```

---

### 2.5 마감 기한 제약 조건 미적용 📅

**현상:**
- 사용자가 어떤 미래 날짜든 선택 가능
- 기획에는 "오늘 이후 ~ 한 달 이내" 제한 있음

**원인:**
- `form.html` - `<input type="datetime-local">` 필드에 `min`, `max` 속성 없음
- 백엔드에는 `@Future` 검증만 존재

**영향도:** 낮음 (백엔드 검증은 작동하지만 UX 개선 필요)

**해결 방안:**

```html
<div class="mb-3">
    <label for="deadline" class="form-label fw-semibold">마감 기한</label>
    <input type="datetime-local" 
           id="deadline" 
           name="deadline" 
           class="form-control" 
           required>
    <div class="form-text">
        <i class="bi bi-calendar-event"></i> 오늘부터 최대 30일 이내로 설정 가능합니다.
    </div>
</div>

<script>
document.addEventListener('DOMContentLoaded', function() {
    const deadlineInput = document.getElementById('deadline');
    
    // 현재 시간 + 1일 (최소값)
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    
    // 현재 시간 + 30일 (최대값)
    const maxDate = new Date();
    maxDate.setDate(maxDate.getDate() + 30);
    
    // datetime-local 형식으로 변환 (YYYY-MM-DDTHH:mm)
    deadlineInput.min = tomorrow.toISOString().slice(0, 16);
    deadlineInput.max = maxDate.toISOString().slice(0, 16);
});
</script>
```

---

### 2.6 카테고리 옵션 사라짐 (검증 오류 시) 🔄

**현상:**
- 폼 제출 시 검증 오류 발생 (예: 마감일이 과거)
- 페이지가 다시 로드될 때 카테고리 드롭다운이 비어있음

**원인:**
- `GroupBuyController.createRecipeBasedGroupBuy()` 메서드가 검증 실패 시 모델에 `categories`를 다시 추가하지 않음

**영향도:** 중간 (사용성 문제 - 사용자가 폼을 다시 작성할 수 없음)

**해결 방안:**

```java
@PostMapping("/recipe-based")
public String createRecipeBasedGroupBuy(
    @Valid @ModelAttribute("request") CreateGroupBuyRequest request,
    BindingResult bindingResult,
    @AuthenticationPrincipal UserDetails userDetails,
    Model model,
    RedirectAttributes redirectAttributes) {
    
    // 검증 실패 시 필요한 데이터 복구
    if (bindingResult.hasErrors()) {
        // 카테고리 목록 다시 추가
        model.addAttribute("categories", GroupBuyCategory.values());
        
        // 레시피 정보 다시 조회 (recipeApiId가 있을 경우)
        if (request.getRecipeApiId() != null && !request.getRecipeApiId().isEmpty()) {
            try {
                RecipeDetailResponse recipe = recipeService.getRecipeDetailByApiId(request.getRecipeApiId());
                model.addAttribute("recipe", recipe);
            } catch (Exception e) {
                log.warn("레시피 조회 실패: {}", request.getRecipeApiId());
            }
        }
        
        // 오류 메시지 설정
        model.addAttribute("errorMessage", "입력 내용을 확인해주세요.");
        
        return "group-purchases/form";
    }
    
    // ... 정상 처리 로직
}
```

---

### 2.7 참여자 목록 공개 여부 기능 미구현 👥

**현상:**
- `isParticipantListPublic` 필드가 DB에 존재하지만, 실제 공개 보기 기능이 없음
- 현재는 호스트만 "참여자 관리" 페이지에서 확인 가능

**원인:**
- 공개 참여자 목록 보기 화면이 `detail.html`에 구현되지 않음

**영향도:** 낮음 (선택적 기능)

**해결 방안:**

**1단계: detail.html 수정**
```html
<!-- 참여 현황 섹션 아래에 추가 -->
<div th:if="${groupBuy.isParticipantListPublic}" class="card mb-4">
    <div class="card-header bg-light">
        <h5 class="mb-0">
            <i class="bi bi-people"></i> 참여자 목록 (공개)
        </h5>
    </div>
    <div class="card-body">
        <div th:if="${participants != null and !participants.isEmpty()}">
            <div class="list-group list-group-flush">
                <div th:each="participant, stat : ${participants}" 
                     class="list-group-item d-flex justify-content-between align-items-center">
                    <div>
                        <i class="bi bi-person-circle"></i>
                        <span th:text="${participant.user.nickname}">닉네임</span>
                        <span th:if="${stat.index == 0}" class="badge bg-primary ms-2">주최자</span>
                    </div>
                    <small class="text-muted" 
                           th:text="${#temporals.format(participant.joinedAt, 'yyyy-MM-dd')}">
                        참여일
                    </small>
                </div>
            </div>
        </div>
        <div th:if="${participants == null or participants.isEmpty()}" 
             class="text-muted text-center py-3">
            <i class="bi bi-inbox"></i> 아직 참여자가 없습니다.
        </div>
    </div>
</div>

<!-- 비공개인 경우 -->
<div th:if="${!groupBuy.isParticipantListPublic}" class="alert alert-secondary">
    <i class="bi bi-lock"></i> 참여자 목록이 비공개입니다.
</div>
```

**2단계: GroupBuyController.java 수정**
```java
@GetMapping("/{id}")
public String getGroupBuyDetail(
    @PathVariable Long id,
    @AuthenticationPrincipal UserDetails userDetails,
    Model model) {
    
    GroupBuyDetailResponse groupBuy = groupBuyService.getGroupBuyDetail(id);
    model.addAttribute("groupBuy", groupBuy);
    
    // 참여자 목록 공개 설정인 경우 참여자 정보 추가
    if (groupBuy.isParticipantListPublic()) {
        List<ParticipationResponse> participants = groupBuyService.getParticipants(id);
        model.addAttribute("participants", participants);
    }
    
    // ... 나머지 로직
    
    return "group-purchases/detail";
}
```

---

## 3. 우선순위 및 구현 계획

#### **Phase 1: 핵심 버그 수정** 
- [ ] Cloudinary 의존성 추가 (`build.gradle`)
- [ ] `.env` 파일에 `CLOUDINARY_URL` 추가
- [ ] `application.yml`에 파일 업로드 크기 설정
- [ ] `CloudinaryConfig` 클래스 작성
- [ ] `ImageUploadUtil` 수정 (실제 업로드 구현)
- [ ] `GroupBuyService`에서 이미지 업로드 호출
- [ ] 카테고리 옵션 사라짐 버그 수정

#### **Phase 2: 사용성 개선** 
- [ ] 1인당 가격 자동 계산 JavaScript 추가
- [ ] 이미지 미리보기 및 삭제 기능 구현
- [ ] 마감 기한 입력 제약 추가 (min/max)
- [ ] 5MB 파일 크기 검증 추가

#### **Phase 3: 추가 기능** (선택사항)
- [ ] 참여자 목록 공개 기능 구현

---

## 4. 기대 동작 

### 4.1 이미지 업로드
- [ ] 이미지 1~3장 업로드 → Cloudinary에 저장 확인
- [ ] 업로드된 이미지 URL로 접근 가능 (CDN)
- [ ] 공구 상세 페이지에서 이미지 정상 표시
- [ ] 5MB 초과 파일 업로드 시 에러 메시지
- [ ] JPG/PNG 외 파일 업로드 시 에러 메시지
- [ ] 미리보기에서 이미지 삭제 후 재업로드 가능

### 4.2 카테고리 옵션
- [ ] 빈 폼 제출 시 카테고리 옵션 유지
- [ ] 잘못된 마감일 입력 후 제출 → 카테고리 선택 유지
- [ ] 레시피 기반 공구에서 오류 발생 시 레시피 정보 유지

### 4.3 1인당 가격 계산
- [ ] 총 가격 60,000원, 인원 5명 입력 → "1인당 12,000원" 표시
- [ ] 인원수 변경 시 실시간 재계산
- [ ] 인원수 0 또는 1 입력 시 경고 메시지

### 4.4 마감 기한
- [ ] 오늘 이전 날짜 선택 불가능
- [ ] 30일 이후 날짜 선택 불가능
- [ ] 유효 범위 외 날짜 선택 시 안내 메시지

### 4.5 참여자 목록
- [ ] 공개 설정 시 누구나 참여자 닉네임 확인 가능
- [ ] 비공개 설정 시 "비공개" 메시지 표시
- [ ] 주최자에게는 "주최자" 배지 표시

---

## 5. 참고 자료

### 5.1 관련 파일
- `build.gradle` - Cloudinary 의존성 추가
- `.env` - Cloudinary 환경 변수 설정
- `application.yml` - 파일 업로드 크기 설정
- `src/main/java/com/recipemate/global/config/CloudinaryConfig.java` - 생성 필요
- `src/main/java/com/recipemate/global/util/ImageUploadUtil.java` - 수정 필요
- `src/main/java/com/recipemate/domain/groupbuy/controller/GroupBuyController.java`
- `src/main/java/com/recipemate/domain/groupbuy/service/GroupBuyService.java`
- `src/main/resources/templates/group-purchases/form.html`
- `src/main/resources/templates/group-purchases/detail.html`

### 5.2 외부 문서
- [Cloudinary Java SDK 문서](https://cloudinary.com/documentation/java_integration)
- [Cloudinary Transformation 가이드](https://cloudinary.com/documentation/image_transformations)
- [Spring Boot File Upload 가이드](https://spring.io/guides/gs/uploading-files/)

---

## 6. FAQ

### Q1. 왜 로컬 저장이 아닌 Cloudinary를 사용하나요?
**A:** 개발/배포 환경 통일, 자동 이미지 최적화, CDN 제공, 파일 관리 자동화 등의 이유로 Cloudinary를 권장합니다. 특히 팀 프로젝트나 클라우드 배포 시 환경 차이로 인한 문제를 방지할 수 있습니다.

### Q2. 5MB 업로드인데 리사이징은 왜 하나요?
**A:** 사용자는 5MB 원본을 업로드하지만, Cloudinary가 자동으로 800x600 크기로 리사이징하고 WebP/JPEG로 최적화하여 최종적으로 200-300KB 정도의 파일을 CDN으로 제공합니다. 이를 통해 웹 로딩 속도를 크게 개선할 수 있습니다.

