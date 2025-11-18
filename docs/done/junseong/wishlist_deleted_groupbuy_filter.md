# 찜목록 - 삭제된 공구 필터링 기능 구현

## 변경 개요
찜목록에서 **삭제된 공구는 보이지 않고**, **취소된 공구는 계속 보이도록** 개선했습니다.

## 변경 날짜
2025년 11월 18일

---

## 문제 상황

기존에는 찜목록 조회 시 공구의 삭제 여부(`deletedAt`)를 체크하지 않아서:
- ❌ 삭제된 공구(`deletedAt != null`)가 찜목록에 계속 표시됨
- ✅ 취소된 공구(`status = CANCELLED`)는 보이는 것이 맞음

---

## 해결 방법

### 1. Repository 쿼리 추가 (`WishlistRepository.java`)

```java
/**
 * 삭제되지 않은 공구만 포함하여 찜목록 조회
 * 취소된 공구(CANCELLED)는 포함, 삭제된 공구(deletedAt != null)는 제외
 */
@Query("SELECT w FROM Wishlist w " +
       "JOIN FETCH w.groupBuy gb " +
       "JOIN FETCH gb.host " +
       "WHERE w.user.id = :userId " +
       "AND gb.deletedAt IS NULL " +
       "ORDER BY w.wishedAt DESC")
List<Wishlist> findByUserIdWithNonDeletedGroupBuys(@Param("userId") Long userId, Pageable pageable);

/**
 * 삭제되지 않은 공구의 찜 개수 조회
 */
@Query("SELECT COUNT(w) FROM Wishlist w " +
       "WHERE w.user.id = :userId " +
       "AND w.groupBuy.deletedAt IS NULL")
long countByUserIdWithNonDeletedGroupBuys(@Param("userId") Long userId);
```

**주요 특징:**
- `JOIN FETCH`를 사용하여 N+1 문제 방지 (공구, 주최자 정보를 한 번에 조회)
- `gb.deletedAt IS NULL` 조건으로 삭제된 공구만 필터링
- `status` 조건은 없으므로 취소된 공구(`CANCELLED`)도 조회됨

---

### 2. Service 메서드 수정 (`WishlistService.java`)

```java
public Page<WishlistResponse> getMyWishlist(Long userId, Pageable pageable) {
    // 사용자 존재 여부 확인
    userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    // 찜 목록 조회 (삭제되지 않은 공구만, 취소된 공구는 포함)
    List<Wishlist> wishlists = wishlistRepository.findByUserIdWithNonDeletedGroupBuys(userId, pageable);
    
    // 전체 개수 조회 (삭제되지 않은 공구만)
    long total = wishlistRepository.countByUserIdWithNonDeletedGroupBuys(userId);
    
    // DTO 변환
    List<WishlistResponse> responses = wishlists.stream()
            .map(WishlistResponse::from)
            .collect(Collectors.toList());

    // Page 객체 생성
    return new PageImpl<>(responses, pageable, total);
}
```

**개선 사항:**
- 기존 `findByUserIdOrderByWishedAtDesc()` → `findByUserIdWithNonDeletedGroupBuys()` 변경
- 전체 개수 조회 로직 추가 (`countByUserIdWithNonDeletedGroupBuys()`)
- 페이징 정보가 정확하게 반영됨

---

## 동작 확인

### 시나리오별 동작

| 공구 상태 | deletedAt | status | 찜목록 표시 여부 |
|----------|-----------|--------|-----------------|
| 정상 공구 | `null` | `RECRUITING` | ✅ 표시됨 |
| 마감 임박 | `null` | `IMMINENT` | ✅ 표시됨 |
| 모집 완료 | `null` | `COMPLETED` | ✅ 표시됨 |
| **취소된 공구** | `null` | `CANCELLED` | ✅ **표시됨** |
| **삭제된 공구** | `2025-11-18 15:30:00` | (무관) | ❌ **표시 안됨** |

---

## 테스트 코드

`WishlistRepositoryDeletedGroupBuyTest.java` 파일을 생성하여 다음을 검증:

### 테스트 케이스

1. **활성 공구와 취소된 공구는 포함됨**
   ```java
   @Test
   void findByUserIdWithNonDeletedGroupBuys_ShouldExcludeDeletedGroupBuys() {
       // 총 3개 찜 (활성, 취소, 삭제)
       List<Wishlist> wishlists = wishlistRepository
           .findByUserIdWithNonDeletedGroupBuys(user.getId(), PageRequest.of(0, 10));
       
       assertThat(wishlists).hasSize(2); // 활성 + 취소 = 2개
   }
   ```

2. **삭제된 공구는 제외됨**
   ```java
   @Test
   void deletedGroupBuy_ShouldBeExcluded() {
       List<Wishlist> wishlists = wishlistRepository
           .findByUserIdWithNonDeletedGroupBuys(user.getId(), PageRequest.of(0, 10));
       
       boolean hasDeletedGroupBuy = wishlists.stream()
           .anyMatch(w -> w.getGroupBuy().isDeleted());
       
       assertThat(hasDeletedGroupBuy).isFalse(); // 삭제된 공구 없음
   }
   ```

3. **개수 조회도 정확함**
   ```java
   @Test
   void countByUserIdWithNonDeletedGroupBuys_ShouldReturnCorrectCount() {
       long count = wishlistRepository.countByUserIdWithNonDeletedGroupBuys(user.getId());
       
       assertThat(count).isEqualTo(2); // 활성 + 취소 = 2개
   }
   ```

---

## 영향 범위

### 수정된 파일
1. `WishlistRepository.java` - 쿼리 메서드 추가
2. `WishlistService.java` - 조회 로직 수정

### 영향 받는 기능
- **찜목록 페이지** (`/users/me/bookmarks?tab=groupbuy`)
  - 삭제된 공구가 더 이상 표시되지 않음
  - 페이징 처리가 정확해짐 (전체 개수 계산 개선)

### 영향 없는 기능
- 찜 추가/삭제 기능 (`addWishlist`, `removeWishlist`)
- 찜 여부 확인 (`isWishlisted`)
- 레시피 찜목록 (별도 기능)

---

## 참고: 취소 vs 삭제 구분

| 구분 | 취소(CANCELLED) | 삭제(Soft Delete) |
|------|----------------|-------------------|
| `status` | `CANCELLED` | 원래 상태 유지 |
| `deletedAt` | `null` | `현재 시간` |
| 데이터 보존 | ✅ 모두 유지 | ✅ DB에 유지 (조회 불가) |
| 이미지 보존 | ✅ 유지 | ❌ 완전 삭제 |
| 찜목록 표시 | ✅ **표시됨** | ❌ **표시 안됨** |
| 복구 가능성 | ✅ 상태만 변경 | ⚠️ 이미지는 복구 불가 |

**핵심:**
- **취소**: 주최자가 공구를 포기했지만 기록은 남김 (사용자가 왜 취소되었는지 볼 수 있음)
- **삭제**: 공구 자체를 제거하여 일반 사용자에게는 보이지 않음 (관리자만 복구 가능)

---

## 쿼리 성능

### 최적화 포인트
1. **JOIN FETCH 사용** - N+1 문제 방지
2. **인덱스 활용** - `group_buys` 테이블의 `idx_groupbuy_deleted_created` 인덱스 활용
3. **COUNT 쿼리 분리** - 정확한 페이징 정보 제공

### 실행 쿼리 예시
```sql
-- 찜목록 조회
SELECT w.*, gb.*, u.*
FROM wishlists w
INNER JOIN group_buys gb ON w.group_buy_id = gb.id
INNER JOIN users u ON gb.host_id = u.id
WHERE w.user_id = ?
  AND gb.deleted_at IS NULL
ORDER BY w.wished_at DESC
LIMIT 20;

-- 전체 개수 조회
SELECT COUNT(*)
FROM wishlists w
INNER JOIN group_buys gb ON w.group_buy_id = gb.id
WHERE w.user_id = ?
  AND gb.deleted_at IS NULL;
```

---

## 추가 고려사항

### 1. 캐싱
- 현재는 캐싱 미적용
- 필요시 `@Cacheable` 추가 고려

### 2. 삭제된 공구 찜 정리
- 현재는 찜 데이터 자체는 그대로 유지됨 (조회만 필터링)
- 배치 작업으로 주기적으로 정리 가능

### 3. UI 개선
- 취소된 공구는 "취소됨" 배지 표시 가능
- 삭제된 공구는 아예 보이지 않음

---

## 결론

✅ **구현 완료**
- 찜목록에서 삭제된 공구(`deletedAt != null`)는 제외됨
- 취소된 공구(`status = CANCELLED`)는 계속 보임
- 페이징 처리가 정확해짐
- N+1 문제 방지 (JOIN FETCH 사용)

✅ **사용자 경험 개선**
- 더 이상 존재하지 않는 공구가 찜목록에 표시되지 않음
- 취소된 공구는 상태 확인 가능

