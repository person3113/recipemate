# 커뮤니티 정렬 및 회원탈퇴 기능 오류 수정

**작업일**: 2025-01-24  
**작성자**: junseong

## 수정한 문제 목록

### 1. 커뮤니티 탭 정렬 500 에러
- **문제**: 좋아요순, 댓글많은순으로 정렬 시 500 에러 발생
- **원인**: `@Query` 서브쿼리로 계산된 값은 정렬 기준으로 사용 불가
- **해결**: QueryDSL 기반의 동적 정렬 지원 메서드로 변경

### 2. 회원탈퇴 모달 미표시
- **문제**: 회원탈퇴 버튼 클릭 시 확인 모달(팝업)이 나타나지 않음
- **원인**: 모달이 Thymeleaf fragment 범위 밖에 위치
- **해결**: 모달을 `<main>` 태그 안으로 이동

### 3. 회원탈퇴 후 500 에러
- **문제**: 회원탈퇴 후 페이지 접근 시 500 에러 발생
- **원인**: 세션이 남아있어 삭제된 사용자 조회 시도
- **해결**: GlobalControllerAdvice에 예외 처리 및 자동 로그아웃 추가

---

## 수정한 파일

### 1. PostService.java
**경로**: `src/main/java/com/recipemate/domain/post/service/PostService.java`

**변경 내용**:
```java
// 기존: 조건별로 다른 메서드 호출
if (category != null && trimmedKeyword != null) {
    postsWithCounts = postRepository.searchByCategoryAndKeywordWithCounts(...);
} else if (category != null) {
    postsWithCounts = postRepository.findByCategoryWithCounts(...);
} else if (trimmedKeyword != null) {
    postsWithCounts = postRepository.searchByKeywordWithCounts(...);
} else {
    postsWithCounts = postRepository.findAllWithCounts(...);
}

// 수정: 동적 정렬 지원 메서드 사용
postsWithCounts = postRepository.findAllWithCountsDynamic(
    category, 
    trimmedKeyword, 
    pageable
);
```

**효과**: 좋아요순, 댓글많은순 정렬 정상 작동

---

### 2. settings.html
**경로**: `src/main/resources/templates/user/settings.html`

**변경 내용**:
- 회원탈퇴 모달을 `</main>` 태그 안으로 이동
- Thymeleaf fragment에 포함되도록 구조 변경

```html
<!-- 기존 -->
        </div>
    </main>
    <div class="modal fade" id="deleteAccountModal">...</div>

<!-- 수정 -->
        </div>
        <div class="modal fade" id="deleteAccountModal">...</div>
    </main>
```

**효과**: 회원탈퇴 버튼 클릭 시 모달 정상 표시

---

### 3. GlobalControllerAdvice.java
**경로**: `src/main/java/com/recipemate/global/config/GlobalControllerAdvice.java`

**변경 내용**:
```java
@ModelAttribute
public void addUnreadCounts(Model model, HttpServletRequest request) {
    // ...existing code...
    
    try {
        Long unreadNotificationCount = notificationService.getUnreadCount(user.getId());
        long unreadMessageCount = directMessageService.getUnreadCount(user.getId());
        
        model.addAttribute("unreadNotificationCount", unreadNotificationCount);
        model.addAttribute("unreadMessageCount", unreadMessageCount);
    } catch (Exception e) {
        // 사용자를 찾을 수 없는 경우 (삭제된 경우 등) 로그아웃 처리
        new SecurityContextLogoutHandler().logout(request, null, auth);
        
        model.addAttribute("unreadNotificationCount", 0L);
        model.addAttribute("unreadMessageCount", 0L);
    }
}
```

**효과**: 회원탈퇴 후 페이지 접근 시 자동 로그아웃 처리

---

## 테스트 결과

- ✅ 커뮤니티 탭에서 최신순/조회순/좋아요순/댓글많은순 정렬 정상 작동
- ✅ 회원탈퇴 버튼 클릭 시 확인 모달 정상 표시
- ✅ 회원탈퇴 처리 정상 완료
- ✅ 회원탈퇴 후 페이지 접근 시 자동 로그아웃 및 에러 없음

---

## 관련 기술

- Spring Data JPA
- QueryDSL (동적 정렬)
- Thymeleaf Fragment
- Spring Security (자동 로그아웃)
- @ControllerAdvice 예외 처리

