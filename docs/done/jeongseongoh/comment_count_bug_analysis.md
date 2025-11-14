# 대댓글 삭제 시 댓글 수 비정상적 증가 버그 분석

## 1. 문제 현상

커뮤니티 게시글 상세 페이지에서 대댓글을 삭제하면, 화면상의 댓글 수가 1 증가한다. 페이지를 새로고침하면 원래 숫자로 돌아온다.

## 2. 비즈니스 로직 확인

- 댓글과 대댓글은 **소프트 삭제(Soft Delete)** 정책을 따른다.
- 따라서 댓글/대댓글을 삭제하더라도 전체 댓글 개수는 변하지 않고 유지되어야 한다.
- **기대하는 동작**: 대댓글 삭제 시, 댓글 수에 아무런 변화가 없어야 한다.
- **현재 버그**: 대댓글 삭제 시, 댓글 수가 1 증가한다.

## 3. 원인 분석

문제의 원인은 `fragments/comments.html` 파일 내에 있는 `htmx` 이벤트 처리 JavaScript 코드에 있다.

```javascript
// htmx 이벤트 핸들러
document.body.addEventListener('htmx:afterRequest', function(event) {
    // 성공적인 POST 요청만 처리
    if (!event.detail.successful || event.detail.requestConfig.verb !== 'post') {
        return;
    }
    
    const url = event.detail.requestConfig.path;
    
    // ... (댓글 추가 로직)
    
    // 대댓글 추가 로직
    if (url.includes('/comments/') && url.includes('/replies')) {
        console.log('Updating comment count +1 (reply)');
        updateCommentCount(1); // 카운트 1 증가
        // ...
    }
});
```

1.  **삭제와 추가의 모호한 구분**: 대댓글 '추가'와 '삭제' 모두 `POST` HTTP 메서드를 사용하며, 요청 URL 또한 유사한 패턴을 가진다.
    -   **대댓글 추가 URL**: `/comments/{id}/replies`
    -   **대댓글 삭제 URL**: `/comments/replies/{id}/delete`
2.  **지나치게 광범위한 조건문**: 현재 코드는 URL에 `/comments/`와 `/replies`가 포함된 모든 `POST` 요청에 대해 댓글 수를 1 증가시키는 로직(`updateCommentCount(1)`)을 실행한다.
3.  **버그 발생**: 이로 인해, 대댓글 '삭제' 요청(`.../delete`)이 성공했을 때도 '추가' 로직이 잘못 트리거되어, 비즈니스 로직과 달리 댓글 수가 1 증가하는 문제가 발생한다.

## 4. 해결 방안

`htmx:afterRequest` 이벤트 리스너를 수정하여 '추가' 요청과 '삭제' 요청을 명확히 구분해야 한다. 대댓글 추가를 감지하는 조건문에 삭제 URL(`.../delete`)을 명시적으로 제외하는 조건을 추가한다.

### 수정 제안 코드

```javascript
document.body.addEventListener('htmx:afterRequest', function(event) {
    // 성공적인 POST 요청만 처리
    if (!event.detail.successful || event.detail.requestConfig.verb !== 'post') {
        return;
    }
    
    const url = event.detail.requestConfig.path;
    console.log('htmx:afterRequest', { url: url, successful: event.detail.successful });
    
    // 댓글 추가 (/comments/fragment)
    if (url.includes('/comments/fragment')) {
        console.log('Updating comment count +1 (comment)');
        updateCommentCount(1);
        // 폼 리셋
        const form = event.detail.elt;
        if (form && form.tagName === 'FORM') {
            form.reset();
        }
    }
    
    // 대댓글 추가 (/comments/{id}/replies) - 삭제 요청은 제외
    if (url.includes('/comments/') && url.includes('/replies') && !url.includes('/delete')) {
        console.log('Updating comment count +1 (reply)');
        updateCommentCount(1);
        // 폼 리셋 및 숨기기
        const form = event.detail.elt;
        if (form && form.tagName === 'FORM') {
            form.reset();
            const replyForm = form.closest('.reply-form');
            if (replyForm) {
                replyForm.classList.add('d-none');
            }
        }
    }
});

// 댓글 카운트 업데이트 함수
function updateCommentCount(delta) {
    const badge = document.getElementById('total-comment-count');
    if (badge) {
        const currentCount = parseInt(badge.textContent) || 0;
        const newCount = Math.max(0, currentCount + delta);
        badge.textContent = newCount;
    }
}
```

### 변경 사항 요약

-   **대댓글 추가 조건 강화**: 대댓글 추가를 감지하는 `if` 조건문에 `!url.includes('/delete')`를 추가했다.
-   **기대 효과**: 이 변경으로 인해 대댓글 삭제 요청은 더 이상 카운트 증가 로직을 트리거하지 않게 된다. 결과적으로 댓글/대댓글 추가 시에만 카운트가 1씩 증가하고, 삭제 시에는 카운드가 변하지 않는 올바른 동작을 보장한다.
