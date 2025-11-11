# 수동 스키마 수정 가이드

## 현재 상황
wishlists 테이블의 스키마가 JPA 엔티티 정의와 맞지 않아 애플리케이션이 시작되지 않습니다.

### 문제점
- **DB 현재 구조**: `entity_id`, `entity_type` (범용 위시리스트)
- **엔티티 요구사항**: `group_buy_id`, `wished_at` (공동구매 전용 북마크)

## 수동 수정 명령어

### 1단계: API 컨테이너 중지
```bash
docker stop recipemate-app
```

### 2단계: wishlists 테이블 재생성
```bash
docker exec -it recipemate-postgres psql -U recipemate -d recipemate
```

PostgreSQL에 접속한 후 다음 명령어를 순서대로 실행:

```sql
-- 기존 테이블 삭제
DROP TABLE IF EXISTS wishlists CASCADE;

-- 새 테이블 생성
CREATE TABLE wishlists (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    group_buy_id BIGINT NOT NULL,
    wished_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_wishlist_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_wishlist_groupbuy FOREIGN KEY (group_buy_id) REFERENCES group_buys(id) ON DELETE CASCADE,
    CONSTRAINT uk_user_groupbuy UNIQUE (user_id, group_buy_id)
);

-- 인덱스 생성
CREATE INDEX idx_user_id_wished_at ON wishlists(user_id, wished_at);
CREATE INDEX idx_group_buy_id ON wishlists(group_buy_id);

-- 테이블 구조 확인
\d wishlists
```

### 3단계: PostgreSQL 종료
```sql
\q
```

### 4단계: API 컨테이너 재시작
```bash
docker start recipemate-app
```

### 5단계: 로그 확인
```bash
docker logs -f recipemate-app
```

## 예상 결과
테이블 구조가 다음과 같아야 함:

```
                                         Table "public.wishlists"
   Column      |            Type             | Collation | Nullable |                Default                
---------------+-----------------------------+-----------+----------+---------------------------------------
 id            | bigint                      |           | not null | nextval('wishlists_id_seq'::regclass)
 user_id       | bigint                      |           | not null | 
 group_buy_id  | bigint                      |           | not null | 
 wished_at     | timestamp without time zone |           | not null | CURRENT_TIMESTAMP

Indexes:
    "wishlists_pkey" PRIMARY KEY, btree (id)
    "uk_user_groupbuy" UNIQUE CONSTRAINT, btree (user_id, group_buy_id)
    "idx_group_buy_id" btree (group_buy_id)
    "idx_user_id_wished_at" btree (user_id, wished_at)

Foreign-key constraints:
    "fk_wishlist_user" FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
    "fk_wishlist_groupbuy" FOREIGN KEY (group_buy_id) REFERENCES group_buys(id) ON DELETE CASCADE
```

## 추가 확인이 필요한 테이블
다음 테이블들도 스키마 검증 오류가 발생할 수 있음:
- comments
- badges
- point_histories
- notifications

## 문제 해결 팁
1. 테이블 삭제가 안 될 경우: API 컨테이너가 완전히 중지되었는지 확인
2. 외래키 오류: 참조되는 테이블(users, group_buys)이 존재하는지 확인
3. 권한 오류: psql 접속 시 `-U recipemate` 사용자로 접속했는지 확인
