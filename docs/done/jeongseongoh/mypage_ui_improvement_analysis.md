# 마이페이지 UI/UX 개선 분석 및 최종 구현 계획

## 현재 문제점 분석

### 1. 배지 개수 표시 문제 ❌
**문제**: 배지 카드에 "--" 하드코딩되어 있음
```html
<h5 class="mb-0 fw-bold">--</h5>
<small class="text-muted">보유 배지</small>
```

**개선 방안**:
```html
<!-- 실제 배지 개수를 동적으로 표시 -->
<h5 class="mb-0 fw-bold" th:text="${#lists.size(user.badges) ?: 0} + '개'">0개</h5>
<small class="text-muted">보유 배지</small>
```

---

### 2. "설정" 버튼 레이아웃 문제 ❌
**문제**: 버튼이 아이콘과 텍스트가 세로로 배치되어 불균형
```html
<a th:href="@{/users/me/settings}" class="btn btn-outline-primary btn-lg w-100">
    <i class="bi bi-gear"></i>
    <div class="small mt-1">설정</div>
</a>
```

**개선 방안**: 다른 통계 카드와 동일한 스타일 적용
```html
<div class="card border-0 bg-light h-100 text-center p-3 hover-shadow">
    <a th:href="@{/users/me/settings}" class="text-decoration-none text-dark">
        <div class="text-secondary mb-2">
            <i class="bi bi-gear" style="font-size: 2rem;"></i>
        </div>
        <h5 class="mb-0 fw-bold">설정</h5>
        <small class="text-muted">계정 관리</small>
    </a>
</div>
```

---

### 3. "관심 목록"과 "알림 & 메시지" 섹션의 필요성 ❌
**문제**: 중복 네비게이션 제공

**현재 구조**:
```html
<!-- Quick Links Section -->
<div class="row g-3 mt-4">
    <div class="col-md-6">
        <div class="card">
            <h6>관심 목록</h6>
            <a href="/users/me/bookmarks">내 찜 목록</a>
        </div>
    </div>
    <div class="col-md-6">
        <div class="card">
            <h6>알림 & 메시지</h6>
            <a href="/users/me/notifications">알림</a>
            <a href="/direct-messages/contacts">쪽지함</a>
        </div>
    </div>
</div>
```

**문제점**:
- 헤더에 이미 알림/쪽지 아이콘 네비게이션 존재
- 프로필 드롭다운에 찜 목록 메뉴 존재
- 화면 공간 낭비
- 사용자 혼란 유발 가능 (같은 기능 여러 곳 존재)

**결론**: **제거**
- 중복 기능 제거로 UI 간소화
- 헤더 네비게이션만으로 충분한 접근성 제공

---

### 4. "내 활동" 탭 UI/UX 불일치 ❌
**핵심 문제**: 현재는 각 탭이 **별도 페이지로 이동**하는 링크인데, **탭 UI**를 사용하고 있음

**현재 동작**:
```html
<ul class="nav nav-tabs mb-3" role="tablist">
    <li class="nav-item">
        <a class="nav-link" onclick="location.href='/users/me/community'">커뮤니티 활동</a>
    </li>
    <!-- ... -->
</ul>

<!-- 플레이스홀더만 표시 -->
<div class="text-center py-5 text-muted">
    <i class="bi bi-cursor" style="font-size: 3rem;"></i>
    <p class="mt-3">위 탭을 클릭하여 활동 내역을 확인하세요.</p>
</div>
```

**문제점**:
1. **탭 UI**: 동일 페이지 내에서 콘텐츠를 동적으로 전환할 때 사용
2. **현재 구현**: 각 탭이 다른 페이지로 리다이렉트
3. **사용자 혼란**: 탭처럼 보이지만 새 페이지로 이동 → UX 일관성 저하
4. **플레이스홀더**: 의미 없는 공간 차지

---

### 5. 헤더 알림/쪽지 아이콘에 미읽음 배지 미구현 ❌
**문제**: 미읽은 알림/쪽지가 있어도 시각적 피드백 없음

**현재 상태**:
```html
<li sec:authorize="isAuthenticated()" class="nav-item ms-2">
    <a th:href="@{/users/me/notifications}" class="nav-link position-relative" title="알림">
        <i class="bi bi-bell fs-5"></i>
        <!-- Badge for unread notifications (implement count logic if needed) -->
        <!-- <span class="position-absolute...">3</span> -->
    </a>
</li>

<li sec:authorize="isAuthenticated()" class="nav-item">
    <a th:href="@{/direct-messages/contacts}" class="nav-link position-relative" title="쪽지">
        <i class="bi bi-envelope fs-5"></i>
        <!-- Badge for unread messages (implement count logic if needed) -->
        <!-- <span class="position-absolute...">5</span> -->
    </a>
</li>
```

**개선 방안**:
- NotificationService에 `getUnreadCount(userId)` 메소드 이미 존재 ✅
- DirectMessageService에 `getUnreadCount(userId)` 메소드 이미 존재 ✅
- Controller에서 모델에 추가하고 템플릿에서 표시만 하면 됨

---

## UI 디자이너 관점: 최종 결정

### 프로젝트 분석
1. **현재 UI 스타일**: 카드 기반 대시보드 (프로필 요약, 통계 카드)
2. **타겟 사용자**: 요리 레시피와 공동구매를 이용하는 일반 사용자
3. **네비게이션 패턴**: 명확한 링크 기반 페이지 이동
4. **반응형**: Bootstrap 그리드 시스템 활용

### 최종 선택: **리스트 기반 네비게이션 (제안 2)** ⭐

**선택 이유**:

1. **공간 효율성** 🎯
   - 카드 방식은 4개의 활동 유형을 표시하기에 공간을 많이 차지
   - 현재 페이지가 이미 프로필 요약 + 통계 카드로 상단부가 꽉 참
   - 리스트 방식은 간결하면서도 명확한 정보 전달

2. **시각적 일관성** 🎨
   - 현재 마이페이지 하단에 이미 "찜 목록", "알림", "쪽지함"이 리스트 형태로 구현됨
   - 같은 페이지 내에서 동일한 UI 패턴 유지 → 학습 비용 감소
   - "관심 목록", "알림 & 메시지" 섹션 제거 후 동일한 위치에 "내 활동" 배치 가능

3. **모바일 최적화** 📱
   - 리스트 형태는 좁은 화면에서 자연스럽게 세로로 배치
   - 카드 4개는 모바일에서 스크롤이 길어짐
   - 터치 영역이 넓어 모바일 사용성 우수

4. **확장성** 🔧
   - 향후 "내가 쓴 후기", "내 질문" 등 활동 유형 추가 시 리스트에 쉽게 추가 가능
   - 카드는 4개 이상이 되면 레이아웃이 복잡해짐

5. **정보 계층 구조** 📊
   - 상단: 프로필 요약 (큰 카드)
   - 중상단: 통계 (작은 카드 3개 + 설정)
   - 중하단: 내 활동 (리스트) ← 시각적 휴식 공간
   - 하단: 추가 정보 (필요시)

**버튼 방식(제안 3)을 선택하지 않은 이유**:
- 버튼 스타일이 너무 강조되어 시각적 피로감
- 모든 항목이 같은 중요도로 보임 (실제로는 사용 빈도 차이 있음)

**카드 방식(제안 1)을 선택하지 않은 이유**:
- 이미 상단에 카드가 많아 페이지가 무거워 보임
- 공간 효율성 낮음
- 통계 카드와 혼동 가능성

---

## 최종 구현 코드

### 1. "내 활동" 섹션 - 리스트 기반 네비게이션

```html
<!-- Activity List Section -->
<div class="card shadow-sm mb-4">
    <div class="card-header bg-white">
        <h5 class="mb-0">
            <i class="bi bi-activity"></i> 내 활동
        </h5>
    </div>
    <div class="list-group list-group-flush">
        <a th:href="@{/users/me/community}" 
           class="list-group-item list-group-item-action d-flex justify-content-between align-items-center">
            <span>
                <i class="bi bi-chat-dots text-primary me-2"></i>
                커뮤니티 활동
            </span>
            <i class="bi bi-chevron-right text-muted"></i>
        </a>
        
        <a th:href="@{/users/me/recipes}" 
           class="list-group-item list-group-item-action d-flex justify-content-between align-items-center">
            <span>
                <i class="bi bi-book text-success me-2"></i>
                작성한 레시피
            </span>
            <i class="bi bi-chevron-right text-muted"></i>
        </a>
        
        <a th:href="@{/users/me/group-purchases}" 
           class="list-group-item list-group-item-action d-flex justify-content-between align-items-center">
            <span>
                <i class="bi bi-shop text-warning me-2"></i>
                주최한 공구
            </span>
            <i class="bi bi-chevron-right text-muted"></i>
        </a>
        
        <a th:href="@{/users/me/participations}" 
           class="list-group-item list-group-item-action d-flex justify-content-between align-items-center">
            <span>
                <i class="bi bi-cart-check text-info me-2"></i>
                참여한 공구
            </span>
            <i class="bi bi-chevron-right text-muted"></i>
        </a>
    </div>
</div>
```

---

### 2. 통계 카드 영역 개선 (설정 버튼 포함)

```html
<!-- Stats Grid (통계 + 설정) -->
<div class="col-md-9">
    <div class="row g-3">
        <!-- 매너온도 카드 -->
        <div class="col-md-3">
            <a th:href="@{/users/me/manner-histories}" class="text-decoration-none">
                <div class="card border-0 bg-light h-100 text-center p-3 hover-shadow">
                    <div class="text-warning mb-2">
                        <i class="bi bi-thermometer-half" style="font-size: 2rem;"></i>
                    </div>
                    <h5 class="mb-0 fw-bold" th:text="*{mannerTemperature} + '°C'">36.5°C</h5>
                    <small class="text-muted">매너온도</small>
                </div>
            </a>
        </div>
        
        <!-- 포인트 카드 -->
        <div class="col-md-3">
            <a th:href="@{/users/me/points}" class="text-decoration-none">
                <div class="card border-0 bg-light h-100 text-center p-3 hover-shadow">
                    <div class="text-primary mb-2">
                        <i class="bi bi-coin" style="font-size: 2rem;"></i>
                    </div>
                    <h5 class="mb-0 fw-bold" th:text="${#numbers.formatInteger(currentPoints, 0, 'COMMA')} + 'P'">0P</h5>
                    <small class="text-muted">포인트</small>
                </div>
            </a>
        </div>
        
        <!-- 배지 카드 (개수 동적 표시) -->
        <div class="col-md-3">
            <a th:href="@{/users/me/badges}" class="text-decoration-none">
                <div class="card border-0 bg-light h-100 text-center p-3 hover-shadow">
                    <div class="text-success mb-2">
                        <i class="bi bi-trophy" style="font-size: 2rem;"></i>
                    </div>
                    <h5 class="mb-0 fw-bold" th:text="${#lists.size(user.badges) ?: 0} + '개'">0개</h5>
                    <small class="text-muted">보유 배지</small>
                </div>
            </a>
        </div>
        
        <!-- 설정 카드 (통계 카드와 동일한 스타일) -->
        <div class="col-md-3">
            <a th:href="@{/users/me/settings}" class="text-decoration-none">
                <div class="card border-0 bg-light h-100 text-center p-3 hover-shadow">
                    <div class="text-secondary mb-2">
                        <i class="bi bi-gear" style="font-size: 2rem;"></i>
                    </div>
                    <h5 class="mb-0 fw-bold">설정</h5>
                    <small class="text-muted">계정 관리</small>
                </div>
            </a>
        </div>
    </div>
</div>
```

---

### 3. 헤더 - 미읽음 배지 표시

```html
<!-- Authenticated User - Icon Navigation with Unread Badges -->
<li sec:authorize="isAuthenticated()" class="nav-item ms-2">
    <a th:href="@{/users/me/notifications}" class="nav-link position-relative" title="알림">
        <i class="bi bi-bell fs-5"></i>
        <!-- Unread notification badge -->
        <span th:if="${unreadNotificationCount > 0}" 
              class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger" 
              style="font-size: 0.6rem;"
              th:text="${unreadNotificationCount > 99 ? '99+' : unreadNotificationCount}">
            3
        </span>
    </a>
</li>

<li sec:authorize="isAuthenticated()" class="nav-item">
    <a th:href="@{/direct-messages/contacts}" class="nav-link position-relative" title="쪽지">
        <i class="bi bi-envelope fs-5"></i>
        <!-- Unread message badge -->
        <span th:if="${unreadMessageCount > 0}" 
              class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger" 
              style="font-size: 0.6rem;"
              th:text="${unreadMessageCount > 99 ? '99+' : unreadMessageCount}">
            5
        </span>
    </a>
</li>
```

---

## Backend 구현 사항

### 1. UserController 수정 - 미읽음 카운트 추가

**파일**: `src/main/java/com/recipemate/domain/user/controller/UserController.java`

```java
@GetMapping("/me")
public String myPage(
        @AuthenticationPrincipal UserDetails userDetails,
        Model model
) {
    User user = userService.findUserByEmail(userDetails.getUsername());
    UserProfileResponseDto userProfile = userService.getUserProfile(user.getId());
    Integer currentPoints = userService.getCurrentPoints(user.getId());
    
    // 미읽은 알림 개수
    Long unreadNotificationCount = notificationService.getUnreadCount(user.getId());
    
    // 미읽은 쪽지 개수
    long unreadMessageCount = directMessageService.getUnreadCount(user.getId());
    
    model.addAttribute("user", userProfile);
    model.addAttribute("currentPoints", currentPoints);
    model.addAttribute("unreadNotificationCount", unreadNotificationCount);
    model.addAttribute("unreadMessageCount", unreadMessageCount);
    
    return "user/my-page";
}
```

### 2. 모든 페이지에서 헤더 미읽음 배지 표시

**방법 1**: `@ControllerAdvice` 사용 (추천)

**파일**: `src/main/java/com/recipemate/global/config/GlobalControllerAdvice.java` (신규)

```java
package com.recipemate.global.config;

import com.recipemate.domain.directmessage.service.DirectMessageService;
import com.recipemate.domain.notification.service.NotificationService;
import com.recipemate.domain.user.entity.User;
import com.recipemate.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {
    
    private final NotificationService notificationService;
    private final DirectMessageService directMessageService;
    private final UserService userService;
    
    @ModelAttribute
    public void addUnreadCounts(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            User user = userService.findUserByEmail(userDetails.getUsername());
            
            Long unreadNotificationCount = notificationService.getUnreadCount(user.getId());
            long unreadMessageCount = directMessageService.getUnreadCount(user.getId());
            
            model.addAttribute("unreadNotificationCount", unreadNotificationCount);
            model.addAttribute("unreadMessageCount", unreadMessageCount);
        } else {
            model.addAttribute("unreadNotificationCount", 0L);
            model.addAttribute("unreadMessageCount", 0L);
        }
    }
}
```

**장점**:
- 모든 컨트롤러에 자동 적용
- 코드 중복 제거
- 헤더가 포함된 모든 페이지에서 자동으로 미읽음 배지 표시

---

## 레이아웃 변경 사항 요약

### Before (기존)
```
┌─────────────────────────────────┐
│ 프로필 요약 카드                │
│ (이미지, 닉네임, 이메일)        │
│ [매너온도] [포인트] [배지]      │
│ [설정 버튼]                     │
└─────────────────────────────────┘
┌─────────────────────────────────┐
│ 내 활동                         │
│ [탭1] [탭2] [탭3] [탭4]         │
│                                 │
│ 위 탭을 클릭하여... (플레이스홀더)│
└─────────────────────────────────┘
┌─────────────────┬───────────────┐
│ 관심 목록       │ 알림 & 메시지 │
│ - 내 찜 목록    │ - 알림        │
│                 │ - 쪽지함      │
└─────────────────┴───────────────┘
```

### After (개선)
```
┌─────────────────────────────────┐
│ 프로필 요약 카드                │
│ (이미지, 닉네임, 이메일)        │
│ [매너온도] [포인트] [배지] [설정]│
└─────────────────────────────────┘
┌─────────────────────────────────┐
│ 내 활동                         │
│ ▸ 커뮤니티 활동              →  │
│ ▸ 작성한 레시피              →  │
│ ▸ 주최한 공구                →  │
│ ▸ 참여한 공구                →  │
└─────────────────────────────────┘
```

**변경 사항**:
1. 설정 버튼을 통계 카드 영역으로 이동 (4개 카드 그리드)
2. 탭 UI → 리스트 UI로 변경
3. 플레이스홀더 제거
4. "관심 목록", "알림 & 메시지" 섹션 제거 (중복)
5. 전체 페이지 길이 단축 → 스크롤 감소

---

## 구현 우선순위

### 높음 (즉시 수정) 🔴
1. **배지 개수 동적 표시**
   - 파일: `my-page.html`
   - 작업: `--` → `th:text="${#lists.size(user.badges) ?: 0} + '개'"`

2. **"내 활동" 탭 → 리스트로 변경**
   - 파일: `my-page.html`
   - 작업: 탭 UI 코드 → 리스트 UI 코드로 교체

3. **설정 버튼 레이아웃 개선**
   - 파일: `my-page.html`
   - 작업: 설정 버튼을 통계 카드 영역으로 이동 및 스타일 통일

### 중간 (개선 권장) 🟡
4. **"관심 목록"/"알림 & 메시지" 섹션 제거**
   - 파일: `my-page.html`
   - 작업: Quick Links Section 전체 제거 (약 40줄)

5. **헤더 미읽음 배지 구현**
   - 파일: 
     - `GlobalControllerAdvice.java` (신규)
     - `header.html`
   - 작업: 
     - ControllerAdvice 생성
     - 헤더 템플릿 주석 해제 및 동적 데이터 바인딩

### 낮음 (선택적) 🟢
6. **호버 애니메이션 통일**
   - 파일: `my-page.html`
   - 작업: `.hover-shadow` 스타일이 모든 카드에 일관되게 적용되는지 확인

7. **반응형 레이아웃 테스트**
   - 작업: 모바일/태블릿/데스크톱에서 레이아웃 확인

---

## 예상 효과

### 사용자 경험 (UX)
- ✅ **명확성**: 링크 동작과 UI가 일치 (탭 혼란 해소)
- ✅ **일관성**: 페이지 내 동일한 UI 패턴 (리스트) 사용
- ✅ **효율성**: 중복 네비게이션 제거로 의사결정 단순화
- ✅ **가시성**: 미읽은 알림/쪽지 즉시 확인 가능
- ✅ **접근성**: 스크롤 감소로 정보 접근성 향상

### 개발 관점
- ✅ **유지보수성**: 단순한 구조로 버그 감소
- ✅ **확장성**: 리스트에 새로운 활동 유형 추가 용이
- ✅ **성능**: 불필요한 플레이스홀더 제거
- ✅ **일관성**: `@ControllerAdvice`로 전역 데이터 관리

### UI 디자인
- ✅ **시각적 균형**: 카드와 리스트의 적절한 조합
- ✅ **정보 계층**: 중요도에 따른 시각적 강조 차이
- ✅ **모바일 최적화**: 좁은 화면에서도 자연스러운 레이아웃
- ✅ **브랜딩**: Bootstrap 기반의 깔끔하고 전문적인 디자인

---

## 다음 단계

1. ✅ 이 분석 문서 검토 및 승인
2. 🔨 코드 수정 시작
   - Phase 1: 템플릿 수정 (my-page.html, header.html)
   - Phase 2: Backend 로직 추가 (GlobalControllerAdvice)
   - Phase 3: 테스트 및 버그 수정
3. 🧪 테스트
   - 기능 테스트 (링크 동작, 배지 표시)
   - 반응형 테스트 (모바일/태블릿/데스크톱)
   - 크로스 브라우저 테스트
4. 📝 사용자 피드백 수집 및 추가 개선

---

## 참고사항

### 기존 서비스 메소드 확인 완료 ✅
- `NotificationService.getUnreadCount(Long userId)`: line 160
- `DirectMessageService.getUnreadCount(Long userId)`: line 88

두 메소드 모두 이미 구현되어 있어 추가 Backend 작업 최소화!
