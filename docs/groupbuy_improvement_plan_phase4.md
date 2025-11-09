# 공구 기능 개선 계획 (Phase 4)

## 개요
Phase 3까지의 구현 완료 후 발견된 추가 개선 사항 및 최적화 방안

---

## 1. 마감일 제약 조건 추가

### 현재 문제
- 한 달 이상의 마감일을 설정해도 에러 없이 공구가 생성됨
- 지나치게 긴 공구 기간은 실용성이 떨어지며 관리가 어려움

### 해결 방안

#### 옵션 A: 프론트엔드 검증 (권장)
**장점:** 사용자에게 즉각적인 피드백 제공, 불필요한 서버 요청 차단

**구현 방법:**
```html
<!-- form.html의 마감일 입력 필드 -->
<input type="datetime-local" 
       id="deadline" 
       name="deadline"
       class="form-control"
       required
       th:field="*{deadline}">
```

**JavaScript 검증 로직 추가:**
```javascript
// 현재 시각 + 1개월 계산
function getMaxDeadline() {
    const now = new Date();
    const maxDate = new Date(now.setMonth(now.getMonth() + 1));
    return maxDate.toISOString().slice(0, 16); // datetime-local 형식
}

// 마감일 입력 필드에 max 속성 설정
const deadlineInput = document.getElementById('deadline');
deadlineInput.setAttribute('max', getMaxDeadline());

// 실시간 검증 + 경고 메시지
deadlineInput.addEventListener('change', function() {
    const selectedDate = new Date(this.value);
    const maxDate = new Date(getMaxDeadline());
    
    if (selectedDate > maxDate) {
        alert('마감일은 현재로부터 최대 1개월 이내로 설정해주세요.');
        this.value = ''; // 입력값 초기화
        this.focus();
    }
});
```

**파일:** `src/main/resources/templates/group-purchases/form.html`
- **수정 위치:** Line 210-220 (마감일 input 필드)
- **추가 위치:** Line 360-380 (JavaScript 검증 로직)

#### 옵션 B: 백엔드 검증 (보안 강화)
**장점:** 우회 불가능한 서버 측 검증, 데이터 무결성 보장

**구현 방법:**
```java
// CreateGroupBuyRequest.java에 커스텀 검증 추가
@AssertTrue(message = "마감일은 현재로부터 1개월 이내로 설정해야 합니다")
public boolean isDeadlineWithinOneMonth() {
    if (deadline == null) return true;
    LocalDateTime maxDeadline = LocalDateTime.now().plusMonths(1);
    return !deadline.isAfter(maxDeadline);
}
```

**파일:** `src/main/java/com/recipemate/domain/groupbuy/dto/CreateGroupBuyRequest.java`
- **추가 위치:** Line 50-55 (검증 메서드)

#### 권장 사항
**양쪽 모두 구현** (프론트엔드 + 백엔드)
- 프론트엔드: 사용자 경험 개선
- 백엔드: 보안 및 데이터 무결성

---

## 2. 이미지 업로드 속도 최적화

### 현재 문제
- **측정 결과:**
  - 첫 번째 이미지: 8.5초 (인간미 있는 남성.png)
  - 두 번째 이미지: 24초 (프로필.png)
  - 총 소요 시간: **약 32.5초**
- 2개 이미지 업로드에 10~20초 이상 소요
- 순차 업로드 방식으로 인한 비효율

### 원인 분석

#### 1. 순차 업로드 (Sequential Upload)
```java
// ImageUploadUtil.java:47-65
for (int i = 0; i < maxImages; i++) {
    // 이미지를 하나씩 순차적으로 업로드
    String imageUrl = uploadSingleImage(file);
    imageUrls.add(imageUrl);
}
```
- 첫 번째 이미지 업로드 완료 → 두 번째 이미지 업로드 시작
- 네트워크 대기 시간이 누적됨

#### 2. Cloudinary 변환 처리
```java
// ImageUploadUtil.java:79-85
new Transformation()
    .width(800)
    .height(600)
    .crop("limit")
    .quality("auto")
    .fetchFormat("auto")
```
- 서버 측에서 이미지 리사이징 및 최적화 수행
- 변환 시간이 업로드 시간에 포함됨

#### 3. 네트워크 지연
- Cloudinary 서버까지의 왕복 시간 (RTT)
- 파일 크기에 따른 전송 시간

### 해결 방안

#### 방안 A: 병렬 업로드 (Parallel Upload) ⭐ 권장
**예상 효과:** 업로드 시간 **50-70% 단축** (32.5초 → 10-15초)

**구현 방법:**
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
import java.util.concurrent.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImageUploadUtil {

    private final Cloudinary cloudinary;
    private final ExecutorService executorService = Executors.newFixedThreadPool(3); // 최대 3개 이미지 동시 업로드

    private static final int MAX_IMAGE_COUNT = 3;
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
        "image/jpeg", "image/jpg", "image/png"
    );
    private static final int UPLOAD_TIMEOUT_SECONDS = 30; // 개별 업로드 타임아웃

    /**
     * 이미지 파일 목록 병렬 업로드
     * @param imageFiles 업로드할 이미지 파일 목록
     * @return 업로드된 이미지 URL 목록
     */
    public List<String> uploadImages(List<MultipartFile> imageFiles) {
        if (imageFiles == null || imageFiles.isEmpty()) {
            return new ArrayList<>();
        }

        validateImageCount(imageFiles);

        // 병렬 업로드를 위한 Future 리스트
        List<CompletableFuture<String>> futures = new ArrayList<>();
        int maxImages = Math.min(imageFiles.size(), MAX_IMAGE_COUNT);
        
        for (int i = 0; i < maxImages; i++) {
            MultipartFile file = imageFiles.get(i);
            
            // 빈 파일 건너뛰기
            if (file.isEmpty()) {
                log.warn("Empty file at index {}, skipping", i);
                continue;
            }
            
            // 검증
            try {
                validateImageFile(file);
            } catch (Exception e) {
                log.error("Validation failed for file at index {}: {}", i, e.getMessage());
                continue;
            }
            
            // 병렬 업로드 작업 생성
            CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                try {
                    return uploadSingleImage(file);
                } catch (IOException e) {
                    log.error("Failed to upload image {}: {}", file.getOriginalFilename(), e.getMessage());
                    return null;
                }
            }, executorService);
            
            futures.add(future);
        }
        
        // 모든 업로드 작업 완료 대기
        List<String> imageUrls = futures.stream()
            .map(future -> {
                try {
                    return future.get(UPLOAD_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                } catch (TimeoutException e) {
                    log.error("Image upload timeout after {} seconds", UPLOAD_TIMEOUT_SECONDS);
                    return null;
                } catch (Exception e) {
                    log.error("Image upload failed: {}", e.getMessage());
                    return null;
                }
            })
            .filter(url -> url != null) // null 제거 (실패한 업로드)
            .toList();
        
        log.info("Successfully uploaded {} out of {} images (parallel)", imageUrls.size(), maxImages);
        return imageUrls;
    }

    /**
     * 단일 이미지 업로드 (Cloudinary)
     */
    private String uploadSingleImage(MultipartFile file) throws IOException {
        long startTime = System.currentTimeMillis();
        
        // 1. Cloudinary 업로드 옵션 설정
        Map<String, Object> uploadParams = ObjectUtils.asMap(
            "folder", "recipemate/group-purchases",
            "resource_type", "image",
            "transformation", new Transformation()
                .width(800)
                .height(600)
                .crop("limit")
                .quality("auto")
                .fetchFormat("auto")
        );
        
        // 2. Cloudinary에 업로드
        log.info("Uploading image to Cloudinary: {}", file.getOriginalFilename());
        Map uploadResult = cloudinary.uploader().upload(
            file.getBytes(), 
            uploadParams
        );
        
        // 3. HTTPS URL 반환
        String imageUrl = uploadResult.get("secure_url").toString();
        long elapsedTime = System.currentTimeMillis() - startTime;
        log.info("Image uploaded successfully in {}ms: {}", elapsedTime, imageUrl);
        
        return imageUrl;
    }

    /**
     * 이미지 파일 개수 검증
     */
    private void validateImageCount(List<MultipartFile> imageFiles) {
        long nonEmptyFileCount = imageFiles.stream()
            .filter(file -> !file.isEmpty())
            .count();
        
        if (nonEmptyFileCount > MAX_IMAGE_COUNT) {
            throw new IllegalArgumentException("이미지는 최대 " + MAX_IMAGE_COUNT + "장까지 업로드 가능합니다");
        }
    }

    /**
     * 이미지 파일 유효성 검증
     */
    private void validateImageFile(MultipartFile file) {
        // 파일 크기 검증
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                String.format("이미지 크기는 5MB 이하여야 합니다 (현재: %.2fMB)", 
                    file.getSize() / (1024.0 * 1024.0))
            );
        }

        // 파일 형식 검증
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("JPG 또는 PNG 형식의 이미지만 업로드 가능합니다");
        }
    }
    
    /**
     * 애플리케이션 종료 시 스레드 풀 정리
     */
    @PreDestroy
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
```

**파일:** `src/main/java/com/recipemate/global/util/ImageUploadUtil.java`
- **전체 리팩터링 필요**
- **주요 변경점:**
  - `ExecutorService` 도입 (스레드 풀 3개)
  - `CompletableFuture`로 비동기 업로드
  - 개별 업로드 타임아웃 30초 설정
  - `@PreDestroy`로 리소스 정리

#### 방안 B: 프론트엔드 압축 (추가 최적화)
**예상 효과:** 전송 시간 **30-40% 단축**

**구현 방법:**
```javascript
// form.html의 이미지 업로드 핸들러에 압축 로직 추가
async function compressImage(file, maxWidth = 1024, quality = 0.8) {
    return new Promise((resolve) => {
        const reader = new FileReader();
        reader.onload = (e) => {
            const img = new Image();
            img.onload = () => {
                const canvas = document.createElement('canvas');
                let width = img.width;
                let height = img.height;
                
                // 최대 너비 제한
                if (width > maxWidth) {
                    height = (height * maxWidth) / width;
                    width = maxWidth;
                }
                
                canvas.width = width;
                canvas.height = height;
                
                const ctx = canvas.getContext('2d');
                ctx.drawImage(img, 0, 0, width, height);
                
                // Blob으로 변환 (JPEG, 품질 80%)
                canvas.toBlob((blob) => {
                    resolve(new File([blob], file.name, {
                        type: 'image/jpeg',
                        lastModified: Date.now()
                    }));
                }, 'image/jpeg', quality);
            };
            img.src = e.target.result;
        };
        reader.readAsDataURL(file);
    });
}

// 이미지 선택 시 자동 압축
document.getElementById('images').addEventListener('change', async function(e) {
    const files = Array.from(e.target.files);
    const compressedFiles = [];
    
    for (const file of files) {
        if (file.type.startsWith('image/')) {
            const compressed = await compressImage(file);
            compressedFiles.push(compressed);
        } else {
            compressedFiles.push(file);
        }
    }
    
    // DataTransfer를 사용해 압축된 파일로 교체
    const dataTransfer = new DataTransfer();
    compressedFiles.forEach(file => dataTransfer.items.add(file));
    e.target.files = dataTransfer.files;
    
    updateImagePreview();
});
```

**파일:** `src/main/resources/templates/group-purchases/form.html`
- **추가 위치:** Line 380-440 (이미지 압축 함수)

#### 방안 C: Cloudinary 설정 최적화
**구현 방법:**
```java
// eager transformation 제거 (업로드 후 필요할 때 변환)
Map<String, Object> uploadParams = ObjectUtils.asMap(
    "folder", "recipemate/group-purchases",
    "resource_type", "image"
    // transformation 제거 - 필요할 때 URL로 동적 변환
);

// 이미지 URL 반환 시 변환 파라미터 추가
String baseUrl = uploadResult.get("secure_url").toString();
String optimizedUrl = baseUrl.replace("/upload/", "/upload/w_800,h_600,c_limit,q_auto,f_auto/");
```

**파일:** `src/main/java/com/recipemate/global/util/ImageUploadUtil.java`
- **수정 위치:** Line 76-85 (uploadSingleImage 메서드)
- **효과:** 업로드 시간 단축, 변환은 CDN에서 캐시됨

### 권장 조합
1. **방안 A (병렬 업로드)** - 필수 ⭐
2. **방안 C (설정 최적화)** - 권장
3. **방안 B (프론트엔드 압축)** - 선택 (모바일 환경에서 효과적)

**예상 총 개선 효과:** 32.5초 → **7-10초** (약 70% 단축)

---

## 3. 추가 개선 사항

### 3.1 공구 상세 페이지에 1인당 가격 표시

**현재 상태:**
- 공구 생성 폼에만 1인당 가격 자동 계산 표시
- 상세 페이지에는 총 가격만 표시

**개선 방안:**
```html
<!-- detail.html의 가격 정보 섹션 -->
<div class="card shadow-sm mb-4">
    <div class="card-body">
        <div class="d-flex justify-content-between align-items-center mb-2">
            <span class="text-muted">총 가격</span>
            <span class="fs-4 fw-bold text-primary" th:text="${#numbers.formatInteger(groupBuy.totalPrice, 0, 'COMMA')} + '원'">50,000원</span>
        </div>
        <div class="d-flex justify-content-between align-items-center">
            <span class="text-muted">1인당 예상 가격</span>
            <span class="fs-5 fw-semibold" 
                  th:text="${#numbers.formatInteger(groupBuy.totalPrice / groupBuy.targetParticipants, 0, 'COMMA')} + '원'">
                5,000원
            </span>
        </div>
    </div>
</div>
```

**파일:** `src/main/resources/templates/group-purchases/detail.html`
- **추가 위치:** Line 80-95 (가격 정보 카드)

---

### 3.2 기존 공구 이미지 수정 기능

**현재 상태:**
- 공구 수정 페이지에서 기존 이미지 확인/관리 불가
- 이미지를 추가하려면 모든 이미지를 다시 업로드해야 함

**실무 Best Practice:**
1. **기존 이미지 표시**: 썸네일로 현재 이미지 목록 표시
2. **개별 삭제**: 각 이미지에 삭제 버튼 제공
3. **새 이미지 추가**: 빈 슬롯에 새 이미지 업로드
4. **Cloudinary 삭제**: 삭제된 이미지는 Cloudinary에서도 제거

**구현 계획:**

#### 백엔드 수정
```java
// GroupBuyController.java - 수정 페이지
@GetMapping("/{purchaseId}/edit")
public String editPage(
    @PathVariable Long purchaseId,
    @AuthenticationPrincipal UserDetails userDetails,
    Model model
) {
    GroupBuyResponse groupBuy = groupBuyService.getGroupBuyDetail(purchaseId);
    
    // 기존 이미지 URL 목록을 모델에 추가
    model.addAttribute("existingImages", groupBuy.getImageUrls());
    model.addAttribute("groupBuy", groupBuy);
    model.addAttribute("categories", GroupBuyCategory.values());
    
    return "group-purchases/form";
}

// GroupBuyController.java - 수정 처리
@PostMapping("/{purchaseId}/edit")
public String updateGroupBuy(
    @PathVariable Long purchaseId,
    @Valid @ModelAttribute UpdateGroupBuyRequest request,
    @RequestParam(required = false) List<String> deletedImages, // 삭제할 이미지 URL
    BindingResult bindingResult,
    @AuthenticationPrincipal UserDetails userDetails,
    RedirectAttributes redirectAttributes
) {
    // 1. 삭제할 이미지를 Cloudinary에서 제거
    if (deletedImages != null && !deletedImages.isEmpty()) {
        imageUploadUtil.deleteImages(deletedImages);
    }
    
    // 2. 기존 이미지 + 새 이미지 병합
    List<String> finalImages = new ArrayList<>(request.getExistingImageUrls());
    if (request.getImages() != null && !request.getImages().isEmpty()) {
        List<String> newImageUrls = imageUploadUtil.uploadImages(request.getImages());
        finalImages.addAll(newImageUrls);
    }
    
    // 3. 공구 정보 업데이트
    groupBuyService.updateGroupBuy(purchaseId, userId, request, finalImages);
    
    return "redirect:/group-purchases/" + purchaseId;
}
```

**파일:** 
- `src/main/java/com/recipemate/domain/groupbuy/controller/GroupBuyController.java`
- 수정 위치: Line 340-380

#### Cloudinary 삭제 기능 추가
```java
// ImageUploadUtil.java에 삭제 메서드 추가
/**
 * Cloudinary에서 이미지 삭제
 * @param imageUrls 삭제할 이미지 URL 목록
 */
public void deleteImages(List<String> imageUrls) {
    if (imageUrls == null || imageUrls.isEmpty()) {
        return;
    }
    
    for (String imageUrl : imageUrls) {
        try {
            // URL에서 public_id 추출
            // 예: https://res.cloudinary.com/.../recipemate/group-purchases/abc123.jpg
            // -> recipemate/group-purchases/abc123
            String publicId = extractPublicIdFromUrl(imageUrl);
            
            // Cloudinary에서 삭제
            Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("Image deleted from Cloudinary: {} (result: {})", publicId, result.get("result"));
        } catch (Exception e) {
            log.error("Failed to delete image from Cloudinary: {}", imageUrl, e);
            // 삭제 실패해도 계속 진행 (DB에서는 제거)
        }
    }
}

/**
 * Cloudinary URL에서 public_id 추출
 */
private String extractPublicIdFromUrl(String imageUrl) {
    // https://res.cloudinary.com/dt9xgsr2z/image/upload/v1234567890/recipemate/group-purchases/abc123.jpg
    // -> recipemate/group-purchases/abc123
    String[] parts = imageUrl.split("/upload/");
    if (parts.length < 2) {
        throw new IllegalArgumentException("Invalid Cloudinary URL: " + imageUrl);
    }
    
    String path = parts[1];
    // 버전 정보(v1234567890) 제거
    path = path.replaceFirst("v\\d+/", "");
    // 확장자 제거
    return path.substring(0, path.lastIndexOf('.'));
}
```

**파일:** `src/main/java/com/recipemate/global/util/ImageUploadUtil.java`
- **추가 위치:** Line 135-180

#### 프론트엔드 수정
```html
<!-- form.html - 기존 이미지 표시 섹션 추가 -->
<div class="mb-4" th:if="${existingImages != null and !existingImages.isEmpty()}">
    <label class="form-label fw-semibold">기존 이미지</label>
    <div class="row g-3">
        <div th:each="imageUrl, iterStat : ${existingImages}" class="col-md-4">
            <div class="card">
                <img th:src="${imageUrl}" class="card-img-top" alt="기존 이미지">
                <div class="card-body p-2 text-center">
                    <button type="button" 
                            class="btn btn-sm btn-outline-danger"
                            onclick="markImageForDeletion(this, '[[${imageUrl}]]')">
                        <i class="bi bi-trash"></i> 삭제
                    </button>
                </div>
            </div>
        </div>
    </div>
    <!-- 삭제할 이미지 URL을 저장할 hidden input -->
    <input type="hidden" name="deletedImages" id="deletedImages" value="">
</div>

<script>
// 삭제할 이미지 URL 목록
let deletedImageUrls = [];

function markImageForDeletion(button, imageUrl) {
    if (!confirm('이 이미지를 삭제하시겠습니까?')) {
        return;
    }
    
    // 삭제 목록에 추가
    deletedImageUrls.push(imageUrl);
    document.getElementById('deletedImages').value = deletedImageUrls.join(',');
    
    // UI에서 제거
    const card = button.closest('.col-md-4');
    card.remove();
}
</script>
```

**파일:** `src/main/resources/templates/group-purchases/form.html`
- **추가 위치:** Line 145-180 (기존 이미지 섹션)

---

### 3.3 참여자 목록 주최자 표시 버그 수정

**현재 문제:**
- 참여자 목록에서 첫 번째 참여자를 무조건 "주최자"로 표시
- 실제 주최자가 아닌 사용자가 주최자로 표시됨

**원인 분석:**
```html
<!-- detail.html:166 -->
<span th:if="${iterStat.index == 0}" class="badge bg-primary ms-2">주최자</span>
```
- `iterStat.index == 0` 조건은 **배열의 첫 번째 요소**를 의미
- 참여자 목록의 정렬 순서가 `participatedAt ASC` (참여일 오름차순)
- 주최자가 먼저 참여했다는 보장이 없음

**해결 방법 A: ParticipantResponse에 주최자 여부 필드 추가** ⭐ 권장

```java
// ParticipantResponse.java
@Getter
@Builder
public class ParticipantResponse {
    private Long userId;
    private String nickname;
    private Double mannerTemperature;
    private Integer quantity;
    private LocalDateTime participatedAt;
    private Boolean isHost; // 추가
    
    public static ParticipantResponse from(Participation participation, Long hostId) {
        return ParticipantResponse.builder()
            .userId(participation.getUser().getId())
            .nickname(participation.getUser().getNickname())
            .mannerTemperature(participation.getUser().getMannerTemperature())
            .quantity(participation.getQuantity())
            .participatedAt(participation.getParticipatedAt())
            .isHost(participation.getUser().getId().equals(hostId)) // 주최자 여부
            .build();
    }
}
```

```java
// ParticipationService.java:173-192 수정
public List<ParticipantResponse> getParticipants(Long groupBuyId, Long currentUserId) {
    // 1. 공구 조회 (주최자 정보 포함)
    GroupBuy groupBuy = groupBuyRepository.findByIdWithHost(groupBuyId)
        .orElseThrow(() -> new CustomException(ErrorCode.GROUP_BUY_NOT_FOUND));

    // 2. 현재 사용자 조회
    User currentUser = userRepository.findById(currentUserId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    // 3. 접근 권한 검증
    if (!groupBuy.getParticipantListPublic() && !groupBuy.isHost(currentUser)) {
        throw new CustomException(ErrorCode.UNAUTHORIZED_PARTICIPANT_LIST_ACCESS);
    }

    // 4. 참여자 목록 조회 및 DTO 변환 (주최자 ID 전달)
    Long hostId = groupBuy.getHost().getId();
    List<Participation> participations = participationRepository.findByGroupBuyIdWithUser(groupBuyId);
    return participations.stream()
        .map(p -> ParticipantResponse.from(p, hostId)) // 주최자 ID 전달
        .collect(Collectors.toList());
}
```

```html
<!-- detail.html:166 수정 -->
<span th:if="${participant.isHost}" class="badge bg-primary ms-2">주최자</span>
```

**파일:**
1. `src/main/java/com/recipemate/domain/groupbuy/dto/ParticipantResponse.java`
   - `isHost` 필드 추가
   - `from()` 메서드에 `hostId` 파라미터 추가

2. `src/main/java/com/recipemate/domain/groupbuy/service/ParticipationService.java`
   - Line 173-192: `getParticipants()` 메서드 수정

3. `src/main/resources/templates/group-purchases/detail.html`
   - Line 166: 조건 수정

**해결 방법 B: 프론트엔드에서 groupBuy.hostId와 비교** (간단)

```html
<!-- detail.html:166 수정 -->
<span th:if="${participant.userId == groupBuy.hostId}" class="badge bg-primary ms-2">주최자</span>
```

**파일:** `src/main/resources/templates/group-purchases/detail.html`
- Line 166: 조건 수정

**권장 방안:** 
- **방법 A** 사용 (백엔드에서 명확하게 처리, 재사용성 높음)

---

### 3.4 비로그인 사용자도 공개 참여자 목록 확인 가능하도록 개선

**현재 상태:**
```java
// GroupBuyController.java:109
if (groupBuy.getIsParticipantListPublic() && userDetails != null) {
    // 로그인한 경우에만 참여자 목록 로드
}
```

**개선 방안:**
```java
// GroupBuyController.java:108-121 수정
// 참여자 목록 공개 여부만 확인 (로그인 조건 제거)
if (groupBuy.getIsParticipantListPublic()) {
    try {
        Long currentUserId = null;
        if (userDetails != null) {
            User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
            currentUserId = user.getId();
        }
        
        java.util.List<com.recipemate.domain.groupbuy.dto.ParticipantResponse> participants = 
            participationService.getParticipants(groupBuyId, currentUserId);
        model.addAttribute("participants", participants);
    } catch (Exception e) {
        log.warn("Failed to load participants list for group buy {}: {}", purchaseId, e.getMessage());
    }
}
```

```java
// ParticipationService.java:173-192 수정
public List<ParticipantResponse> getParticipants(Long groupBuyId, Long currentUserId) {
    GroupBuy groupBuy = groupBuyRepository.findByIdWithHost(groupBuyId)
        .orElseThrow(() -> new CustomException(ErrorCode.GROUP_BUY_NOT_FOUND));

    // 접근 권한 검증 (비로그인 허용)
    if (!groupBuy.getParticipantListPublic()) {
        // 비공개인 경우 주최자만 볼 수 있음
        if (currentUserId == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_PARTICIPANT_LIST_ACCESS);
        }
        User currentUser = userRepository.findById(currentUserId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        if (!groupBuy.isHost(currentUser)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_PARTICIPANT_LIST_ACCESS);
        }
    }

    Long hostId = groupBuy.getHost().getId();
    List<Participation> participations = participationRepository.findByGroupBuyIdWithUser(groupBuyId);
    return participations.stream()
        .map(p -> ParticipantResponse.from(p, hostId))
        .collect(Collectors.toList());
}
```

```html
<!-- detail.html:157-194 수정 -->
<div th:if="${participants != null and !participants.isEmpty()}">
    <!-- 참여자 목록 표시 (로그인 조건 제거) -->
</div>

<div th:if="${participants == null or participants.isEmpty()}" class="text-center py-4">
    <i class="bi bi-inbox fs-1 text-muted"></i>
    <p class="text-muted mt-2 mb-0">아직 참여자가 없습니다.</p>
</div>

<!-- 비로그인 사용자 안내 제거 또는 수정 -->
```

**파일:**
1. `src/main/java/com/recipemate/domain/groupbuy/controller/GroupBuyController.java`
   - Line 108-121: 로그인 조건 제거

2. `src/main/java/com/recipemate/domain/groupbuy/service/ParticipationService.java`
   - Line 173-192: 접근 권한 로직 수정

3. `src/main/resources/templates/group-purchases/detail.html`
   - Line 157-194: 로그인 조건 제거

---

## 4. 우선순위 및 구현 순서

### Phase 4-A (긴급 버그 수정)
1. **참여자 목록 주최자 표시 버그** (Section 3.3) - 높음 🔴
   - 예상 시간: 30분
   - 영향도: 높음 (잘못된 정보 표시)

### Phase 4-B (필수 개선)
2. **마감일 제약 조건** (Section 1) - 높음 🔴
   - 예상 시간: 1-2시간
   - 프론트엔드 + 백엔드 검증 모두 구현

3. **이미지 업로드 병렬 처리** (Section 2 - 방안 A) - 높음 🔴
   - 예상 시간: 2-3시간
   - 사용자 경험에 큰 영향

### Phase 4-C (사용성 개선)
4. **공구 상세 페이지 1인당 가격** (Section 3.1) - 중간 🟡
   - 예상 시간: 30분
   - 간단하지만 유용한 개선

5. **비로그인 사용자 참여자 목록** (Section 3.4) - 중간 🟡
   - 예상 시간: 1시간
   - 투명성 증대

### Phase 4-D (고급 기능)
6. **기존 이미지 수정 기능** (Section 3.2) - 낮음 🟢
   - 예상 시간: 4-5시간
   - 복잡하지만 완성도를 높임

7. **Cloudinary 설정 최적화** (Section 2 - 방안 C) - 낮음 🟢
   - 예상 시간: 1시간
   - 병렬 처리와 함께 구현 시 효과적

8. **프론트엔드 이미지 압축** (Section 2 - 방안 B) - 낮음 🟢
   - 예상 시간: 2시간
   - 모바일 환경에서 효과적

---

## 5. 기술적 고려사항

### 5.1 병렬 업로드 주의사항
- **스레드 풀 크기:** 3개 (이미지 최대 개수와 동일)
- **타임아웃 설정:** 개별 업로드당 30초
- **실패 처리:** 일부 실패 시에도 성공한 이미지는 저장
- **리소스 정리:** `@PreDestroy`로 스레드 풀 정리 필수

### 5.2 Cloudinary 비용 관리
- **삭제 API 사용:** 불필요한 이미지는 즉시 삭제하여 저장 공간 관리
- **Transformation 캐싱:** CDN 캐시 활용으로 반복 변환 방지
- **Upload Preset:** 업로드 정책을 Cloudinary 대시보드에서 미리 설정 가능

### 5.3 보안 고려사항
- **이미지 삭제 권한:** 주최자만 이미지 삭제 가능하도록 검증
- **Public ID 추출:** URL 파싱 시 예외 처리 필수
- **CSRF 토큰:** 이미지 삭제 요청에 CSRF 토큰 포함

---

## 6. 기대 동작

### 6.1 마감일 제약
- [ ] 1개월 초과 날짜 선택 시 경고 표시
- [ ] 백엔드 검증 실패 시 에러 메시지
- [ ] 유효한 마감일 설정 시 정상 생성

### 6.2 병렬 업로드
- [ ] 3개 이미지 동시 업로드 시간 측정
- [ ] 네트워크 오류 시 일부 이미지만 업로드
- [ ] 타임아웃 발생 시 적절한 처리
- [ ] 스레드 풀 리소스 정리 확인

### 6.3 참여자 목록
- [ ] 주최자가 첫 참여자가 아닌 경우에도 정상 표시
- [ ] 비로그인 사용자도 공개 목록 확인 가능
- [ ] 비공개 설정 시 주최자만 확인 가능

### 6.4 이미지 수정
- [ ] 기존 이미지 삭제 시 Cloudinary에서도 제거
- [ ] 새 이미지 추가 시 기존 이미지 유지
- [ ] 이미지 개수 제한 (최대 3개) 검증

