# 엔티티 스키마 변경 관리 가이드

## 🎯 개요

엔티티 설계가 변경될 때 안전하게 DB 스키마를 업데이트하는 방법을 설명합니다.

## 📋 목차
1. [개발 단계별 전략](#개발-단계별-전략)
2. [Hibernate ddl-auto 옵션](#hibernate-ddl-auto-옵션)
3. [Flyway를 이용한 마이그레이션](#flyway-마이그레이션-권장)
4. [실전 시나리오별 대응](#실전-시나리오)

---

## 개발 단계별 전략

### 1️⃣ 초기 개발 단계 (현재 상태)
**목표**: 빠른 프로토타이핑, 엔티티 설계 자유롭게 변경

```yaml
# application-dev.yml
spring:
  jpa:
    hibernate:
      ddl-auto: update  # 현재 설정
```

**특징**:
- ✅ 엔티티 추가/필드 추가 → 자동 반영
- ⚠️ 필드 삭제/이름 변경 → 기존 컬럼 남아있음 (데이터 보존)
- ❌ 타입 변경 → 실패 가능 (수동 처리 필요)

**사용 시나리오**:
```java
// Recipe.java에 필드 추가
@Entity
public class Recipe {
    // 기존 필드들...
    
    // 새로운 필드 추가
    private String videoUrl;  // ← 자동으로 테이블에 컬럼 추가됨
}
```

애플리케이션 재시작 → `ALTER TABLE recipe ADD COLUMN video_url VARCHAR(255)` 자동 실행

---

### 2️⃣ 팀 협업 단계 (중기)
**목표**: 스키마 변경 이력 관리, 팀원 간 동기화

```yaml
# application-dev.yml
spring:
  jpa:
    hibernate:
      ddl-auto: validate  # 검증만 수행
  flyway:
    enabled: true  # Flyway 활성화
```

**특징**:
- ✅ 스키마 변경을 SQL 마이그레이션 파일로 관리
- ✅ Git으로 버전 관리
- ✅ 팀원 간 스키마 동기화 용이
- ✅ 롤백 가능

---

### 3️⃣ 운영 단계 (배포 후)
**목표**: 데이터 손실 없이 안전한 스키마 변경

```yaml
# application-prod.yml
spring:
  jpa:
    hibernate:
      ddl-auto: validate  # 검증만! 절대 자동 변경 X
  flyway:
    enabled: true
    baseline-on-migrate: true
```

**특징**:
- ✅ 모든 변경은 Flyway 마이그레이션 스크립트로만
- ✅ 운영 DB는 수동/자동 절대 변경 금지
- ✅ 배포 전 스테이징 환경에서 검증

---

## Hibernate ddl-auto 옵션

### 옵션 비교

| 옵션 | 설명 | 개발 | 운영 | 데이터 보존 |
|------|------|------|------|-------------|
| `none` | 아무것도 안 함 | ❌ | ✅ | ✅ |
| `validate` | 스키마 검증만 (불일치 시 오류) | ⚠️ | ✅ | ✅ |
| `update` | 스키마 자동 업데이트 (추가만) | ✅ | ❌ | ✅ |
| `create` | 시작 시 테이블 재생성 | ⚠️ | ❌ | ❌ |
| `create-drop` | 종료 시 테이블 삭제 | ⚠️ | ❌ | ❌ |

### 권장 설정

```yaml
# 로컬 개발 (혼자 작업)
spring.jpa.hibernate.ddl-auto: update

# 로컬 개발 (팀 협업)
spring.jpa.hibernate.ddl-auto: validate
spring.flyway.enabled: true

# 운영 환경
spring.jpa.hibernate.ddl-auto: validate
spring.flyway.enabled: true
spring.flyway.baseline-on-migrate: true
```

---

## Flyway 마이그레이션 (권장)

### 설치

#### build.gradle
```gradle
dependencies {
    // Flyway Core
    implementation 'org.flywaydb:flyway-core:9.22.3'
    
    // PostgreSQL용 (운영 환경)
    runtimeOnly 'org.flywaydb:flyway-database-postgresql:9.22.3'
}
```

### 디렉터리 구조

```
src/main/resources/
└── db/
    └── migration/
        ├── V1__init_schema.sql
        ├── V2__add_recipe_tables.sql
        ├── V3__add_video_url_to_recipe.sql
        └── V4__alter_recipe_title_length.sql
```

### 네이밍 규칙

```
V{버전}__{설명}.sql

V: Version (필수)
버전: 숫자 (1, 2, 3 또는 1.0, 1.1, 2.0)
__: 더블 언더스코어 (필수)
설명: snake_case (영문 권장)
```

**예시**:
- ✅ `V1__init_schema.sql`
- ✅ `V2__add_recipe_video_url.sql`
- ✅ `V2.1__fix_recipe_index.sql`
- ❌ `v1_init.sql` (소문자 v)
- ❌ `V1_init.sql` (언더스코어 1개)

### 마이그레이션 파일 작성

#### V1__init_schema.sql (초기 스키마)
```sql
-- RecipeMate 초기 스키마

-- Recipe 테이블
CREATE TABLE recipe (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description VARCHAR(2000),
    full_image_url VARCHAR(500),
    thumbnail_image_url VARCHAR(500),
    category VARCHAR(100),
    area VARCHAR(100),
    source_api VARCHAR(50) NOT NULL,
    source_api_id VARCHAR(100),
    calories INTEGER,
    carbohydrate INTEGER,
    protein INTEGER,
    fat INTEGER,
    sodium INTEGER,
    serving_size VARCHAR(50),
    tips VARCHAR(1000),
    last_synced_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- 인덱스
    CONSTRAINT uk_source_api_id UNIQUE (source_api, source_api_id)
);

CREATE INDEX idx_recipe_title ON recipe(title);
CREATE INDEX idx_recipe_category ON recipe(category);
CREATE INDEX idx_recipe_area ON recipe(area);
CREATE INDEX idx_recipe_calories ON recipe(calories);

-- RecipeIngredient 테이블
CREATE TABLE recipe_ingredient (
    id BIGSERIAL PRIMARY KEY,
    recipe_id BIGINT NOT NULL,
    name VARCHAR(200) NOT NULL,
    measure VARCHAR(200) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_recipe_ingredient_recipe 
        FOREIGN KEY (recipe_id) REFERENCES recipe(id) ON DELETE CASCADE
);

CREATE INDEX idx_recipe_ingredient_name ON recipe_ingredient(name);
CREATE INDEX idx_recipe_ingredient_recipe_id ON recipe_ingredient(recipe_id);

-- RecipeStep 테이블
CREATE TABLE recipe_step (
    id BIGSERIAL PRIMARY KEY,
    recipe_id BIGINT NOT NULL,
    step_number INTEGER NOT NULL,
    description TEXT NOT NULL,
    image_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_recipe_step_recipe 
        FOREIGN KEY (recipe_id) REFERENCES recipe(id) ON DELETE CASCADE
);

CREATE INDEX idx_recipe_step_recipe_id ON recipe_step(recipe_id);
```

#### V2__add_video_url.sql (필드 추가)
```sql
-- Recipe 테이블에 video_url 컬럼 추가

ALTER TABLE recipe 
ADD COLUMN video_url VARCHAR(500);

COMMENT ON COLUMN recipe.video_url IS '조리 영상 URL (YouTube 등)';
```

#### V3__alter_recipe_title_length.sql (타입 변경)
```sql
-- Recipe.title 길이 확장 (200 → 300)

ALTER TABLE recipe 
ALTER COLUMN title TYPE VARCHAR(300);
```

#### V4__rename_column.sql (컬럼 이름 변경)
```sql
-- Recipe.full_image_url → main_image_url로 변경

ALTER TABLE recipe 
RENAME COLUMN full_image_url TO main_image_url;
```

### Flyway 설정

```yaml
# application.yml
spring:
  flyway:
    enabled: true
    # 마이그레이션 파일 위치
    locations: classpath:db/migration
    # 기존 DB에 Flyway 적용 시 (운영 DB)
    baseline-on-migrate: true
    baseline-version: 0
    # 검증 활성화
    validate-on-migrate: true
    # Out of order 실행 허용 (브랜치 병합 시 유용)
    out-of-order: false
```

### Flyway 사용 워크플로우

#### 1. 엔티티 변경
```java
@Entity
public class Recipe {
    // 기존 필드들...
    
    // 새 필드 추가
    private String videoUrl;
}
```

#### 2. 마이그레이션 파일 생성
```sql
-- src/main/resources/db/migration/V5__add_video_url.sql
ALTER TABLE recipe ADD COLUMN video_url VARCHAR(500);
```

#### 3. 애플리케이션 실행
```bash
./gradlew bootRun
```

Flyway가 자동으로:
1. `flyway_schema_history` 테이블 확인
2. 미실행 마이그레이션 감지 (V5)
3. V5 스크립트 실행
4. 이력 기록

#### 4. 확인
```sql
-- Flyway 이력 조회
SELECT * FROM flyway_schema_history ORDER BY installed_rank;

-- 결과
installed_rank | version | description           | success
---------------|---------|----------------------|--------
1              | 1       | init schema          | true
2              | 2       | add video url        | true
```

---

## 실전 시나리오

### 시나리오 1: 필드 추가

#### 요구사항
Recipe에 `servingTime` (조리 시간) 필드 추가

#### 단계별 작업

**1) 엔티티 수정**
```java
@Entity
public class Recipe {
    // ...기존 필드들
    
    @Column(name = "serving_time")
    private Integer servingTime;  // 단위: 분
}
```

**2) 마이그레이션 작성**
```sql
-- V6__add_serving_time_to_recipe.sql
ALTER TABLE recipe 
ADD COLUMN serving_time INTEGER;

COMMENT ON COLUMN recipe.serving_time IS '조리 시간 (분)';

-- 기존 데이터에 기본값 설정 (optional)
UPDATE recipe 
SET serving_time = 30 
WHERE serving_time IS NULL;
```

**3) 실행 및 확인**
```bash
./gradlew bootRun

# 로그 확인
# Flyway: Successfully applied 1 migration to schema "public" (execution time: 00:00.015s)
```

---

### 시나리오 2: 필드 타입 변경

#### 요구사항
Recipe.calories를 Integer → Double로 변경 (소수점 지원)

#### 단계별 작업

**1) 엔티티 수정**
```java
@Entity
public class Recipe {
    // private Integer calories;  // 기존
    private Double calories;  // 변경
}
```

**2) 마이그레이션 작성**
```sql
-- V7__alter_calories_type.sql

-- PostgreSQL
ALTER TABLE recipe 
ALTER COLUMN calories TYPE DOUBLE PRECISION 
USING calories::DOUBLE PRECISION;

-- H2 (로컬 개발)
-- ALTER TABLE recipe ALTER COLUMN calories DOUBLE;
```

⚠️ **주의**: H2와 PostgreSQL의 타입 변경 구문이 다를 수 있음

---

### 시나리오 3: 필드 삭제

#### 요구사항
Recipe.tips 필드 제거 (더 이상 사용 안 함)

#### 단계별 작업 (안전한 방법)

**1단계: Deprecated 표시 (코드에만)**
```java
@Entity
public class Recipe {
    @Deprecated
    @Column(name = "tips")
    private String tips;  // 일단 유지
}
```

**2단계: 배포 및 모니터링 (1-2주)**
- 운영 환경에서 tips 필드 사용 여부 확인
- 로그/모니터링으로 검증

**3단계: 엔티티에서 제거**
```java
@Entity
public class Recipe {
    // private String tips;  // 삭제
}
```

**4단계: 마이그레이션 (DB에서 제거)**
```sql
-- V8__remove_tips_from_recipe.sql

-- 백업용 테이블 생성 (선택)
CREATE TABLE recipe_tips_backup AS 
SELECT id, tips FROM recipe WHERE tips IS NOT NULL;

-- 컬럼 삭제
ALTER TABLE recipe DROP COLUMN tips;
```

⚠️ **주의**: 운영 환경에서는 삭제 전 반드시 백업!

---

### 시나리오 4: 테이블 관계 추가

#### 요구사항
Recipe에 Tag 기능 추가 (N:M 관계)

**1) 엔티티 작성**
```java
@Entity
public class Tag {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    
    @ManyToMany(mappedBy = "tags")
    private Set<Recipe> recipes = new HashSet<>();
}

@Entity
public class Recipe {
    // ... 기존 필드들
    
    @ManyToMany
    @JoinTable(
        name = "recipe_tag",
        joinColumns = @JoinColumn(name = "recipe_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();
}
```

**2) 마이그레이션**
```sql
-- V9__add_tag_feature.sql

-- Tag 테이블 생성
CREATE TABLE tag (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 중간 테이블 생성
CREATE TABLE recipe_tag (
    recipe_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (recipe_id, tag_id),
    
    CONSTRAINT fk_recipe_tag_recipe 
        FOREIGN KEY (recipe_id) REFERENCES recipe(id) ON DELETE CASCADE,
    CONSTRAINT fk_recipe_tag_tag 
        FOREIGN KEY (tag_id) REFERENCES tag(id) ON DELETE CASCADE
);

CREATE INDEX idx_recipe_tag_recipe_id ON recipe_tag(recipe_id);
CREATE INDEX idx_recipe_tag_tag_id ON recipe_tag(tag_id);

-- 초기 태그 데이터
INSERT INTO tag (name) VALUES 
    ('간편요리'), ('한식'), ('양식'), ('중식'), ('일식'),
    ('디저트'), ('다이어트'), ('고단백'), ('저염');
```

---

### 시나리오 5: 인덱스 추가/삭제

#### 요구사항
Recipe.description에 Full-text 검색 인덱스 추가

```sql
-- V10__add_fulltext_index_on_description.sql

-- PostgreSQL (GIN 인덱스 사용)
CREATE INDEX idx_recipe_description_fulltext 
ON recipe 
USING GIN (to_tsvector('korean', description));

-- H2 (Fulltext 인덱스)
-- CREATE FULLTEXT INDEX idx_recipe_description_fulltext ON recipe(description);
```

---

## 🔄 기존 프로젝트에 Flyway 도입

### 현재 상태
- ddl-auto: update로 개발 중
- DB에 이미 데이터 있음
- Flyway 없음

### 도입 방법

#### 1단계: 현재 스키마 Export
```sql
-- H2 Console에서 실행
SCRIPT TO 'schema_current.sql';

-- 또는 PostgreSQL
pg_dump -U recipemate --schema-only recipemate > schema_current.sql
```

#### 2단계: V1 마이그레이션 생성
```bash
# 파일 생성
mkdir -p src/main/resources/db/migration
```

`schema_current.sql`을 정리하여 `V1__init_schema.sql`로 저장

#### 3단계: build.gradle에 Flyway 추가
```gradle
dependencies {
    implementation 'org.flywaydb:flyway-core:9.22.3'
}
```

#### 4단계: application.yml 설정
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate  # update → validate로 변경
  flyway:
    enabled: true
    baseline-on-migrate: true  # 중요!
    baseline-version: 0
```

#### 5단계: 실행
```bash
./gradlew bootRun
```

Flyway가:
1. 기존 DB 스키마 감지
2. baseline (버전 0) 설정
3. V1 스크립트는 스킵 (이미 존재하는 스키마)
4. 향후 V2부터 적용

---

## ⚠️ 주의사항

### 1. 절대 하지 말아야 할 것

❌ **운영 DB에서 ddl-auto: update**
```yaml
# 운영 환경
spring.jpa.hibernate.ddl-auto: update  # ← 절대 금지!
```

❌ **마이그레이션 파일 수정**
```bash
# 이미 실행된 마이그레이션 파일은 절대 수정 금지!
# V3__add_field.sql 수정 → Checksum 오류 발생
```

❌ **마이그레이션 건너뛰기**
```bash
# V2 → V4로 건너뛰면 안 됨
# V1, V2, V3 순차 실행 필수
```

### 2. 롤백 전략

Flyway는 기본적으로 **롤백을 지원하지 않음** (유료 버전만 가능)

**대안**:
```sql
-- V11__add_column.sql (정방향)
ALTER TABLE recipe ADD COLUMN new_field VARCHAR(100);

-- U11__rollback_add_column.sql (역방향, 수동 실행)
ALTER TABLE recipe DROP COLUMN new_field;
```

문제 발생 시 수동으로 U11 실행

### 3. 환경별 마이그레이션

```
db/migration/
├── common/           # 공통 마이그레이션
│   ├── V1__init.sql
│   └── V2__add_field.sql
├── dev/              # 개발 전용
│   └── V99__test_data.sql
└── prod/             # 운영 전용
    └── V100__prod_indexes.sql
```

```yaml
spring:
  flyway:
    locations: classpath:db/migration/common,classpath:db/migration/${spring.profiles.active}
```

---

## 📚 추가 자료

- [Flyway 공식 문서](https://flywaydb.org/documentation/)
- [Liquibase vs Flyway 비교](https://www.liquibase.org/get-started/liquibase-vs-flyway)
- [JPA ddl-auto 상세 설명](https://docs.spring.io/spring-boot/docs/current/reference/html/howto.html#howto.data-initialization.using-hibernate)

---

## 🎓 요약

| 단계 | ddl-auto | Flyway | 추천 대상 |
|------|----------|--------|----------|
| 초기 개발 | `update` | ❌ | 혼자 프로토타이핑 |
| 팀 협업 | `validate` | ✅ | 2명 이상 협업 |
| 운영 배포 | `validate` | ✅ 필수 | 실제 서비스 |

**RecipeMate 현재 권장 설정**:
- **지금 (혼자 개발)**: `ddl-auto: update` 유지
- **팀 협업 시작**: Flyway 도입
- **배포 전**: 반드시 Flyway로 전환
