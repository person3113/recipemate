# 캐시 비활성화 실패 분석 및 Docker/설정 업데이트 가이드

## 1. 문제 상황 요약

* **시도한 조치**: Redis 사용을 중단하기 위해 `.env` 파일과 환경변수 설정을 통해 `SPRING_CACHE_TYPE=none`으로 설정했습니다.
* **기대 결과**: Redis 연결 시도가 중단되고, 애플리케이션 내 캐시 관련 기능이 완전히 꺼져야 합니다.
* **실제 결과 (실패)**:
1. **연결 지속**: 로그 상 `Bootstrapping Spring Data Redis repositories` 메시지가 뜨며 여전히 Redis 연결을 시도합니다.
2. **오류 재발**: `500 Internal Server Error` 발생 시 `org.springframework.data.redis.serializer.SerializationException` 에러가 여전히 발생합니다.
3. **결론**: 설정값(`none`)이 무시되고 있으며, 불변 리스트(Immutable List) 직렬화 문제도 해결되지 않은 상태입니다.

## 2. 원인 심층 분석

### A. `SPRING_CACHE_TYPE=none`이 무시된 이유

Spring Boot의 `spring.cache.type` 설정은 자동 설정에 의한 **Spring Cache 추상화 레이어**만 제어합니다. 하지만 현재 프로젝트처럼 `CacheConfig`와 같은 설정 클래스에서 **Redis 관련 빈(Bean)을 수동으로 등록**(`@Bean`)하고 있다면, Spring은 자동 설정보다 수동 설정을 우선시합니다. 즉, 설정 파일의 `none` 값과 상관없이 Redis가 강제로 켜지게 됩니다.

### B. `CacheConfig.java`의 설정 문제 (핵심)

현재 `CacheConfig.java`는 조건 없이 무조건 로드되도록 작성되어 있을 가능성이 높습니다.

* **문제 코드 패턴**:
```java
@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public RedisCacheManager cacheManager(...) { ... } // 무조건 실행됨
}
```

이 코드가 존재하면 외부에서 `none`이라고 외쳐도, 내부에서는 Redis 관리자를 생성해버립니다.

### C. 직렬화 에러의 근본 원인

`Could not resolve type id 'java.util.ImmutableCollections$ListN'` 에러는 **Java 9+의 `List.of()`로 생성된 불변 리스트**가 Redis에 저장되려 할 때 발생합니다. `RecipeService` 등에서 반환하는 데이터가 수정 불가능한 리스트(Immutable List)이기 때문에, Redis 라이브러리(Jackson)가 이를 처리하지 못해 발생하는 문제입니다.

## 3. 해결 및 적용 가이드 (Action Item)

이 문제를 해결하기 위해 **Java 코드 수정**과 **Docker 설정 업데이트**를 동시에 진행해야 합니다.

### Step 1. `CacheConfig.java` 수정 (조건부 로딩 적용)

Redis 설정 클래스가 `spring.cache.type` 값이 `redis`일 때만 작동하도록 `@ConditionalOnProperty` 어노테이션을 추가합니다. 이것이 있어야 `none` 설정 시 완벽하게 비활성화됩니다.

```java
@Configuration
@EnableCaching
// [핵심 수정] 설정값이 'redis'일 때만 이 클래스를 읽어들이도록 제한
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis") 
public class CacheConfig {
    // 기존 Redis 설정 코드 유지...
}
```

### Step 2. `docker-compose.yml` 수정 (환경변수 매핑)

외부(`.env`)에서 설정한 캐시 타입이 컨테이너 내부로 잘 전달되도록 환경변수를 연결합니다. 안전을 위해 기본값을 `none`으로 설정합니다.

**변경 전:**

```yaml
    environment:
      # (SPRING_CACHE_TYPE 누락됨)
```

**변경 후:**

```yaml
    environment:
      # .env에 값이 없으면 기본적으로 캐시를 끄도록 설정 (안전 모드)
      SPRING_CACHE_TYPE: ${SPRING_CACHE_TYPE:-none} 
```

### Step 3. (선택 권장) 서비스 코드 수정

추후 다시 Redis를 켤 때를 대비하여, 직렬화 오류의 근본 원인인 불변 리스트 문제를 해결해두는 것이 좋습니다. `RecipeService.java` 등의 반환값을 수정 가능한 `ArrayList`로 감싸서 반환하세요.

* **변경**: `return List.of(...);` → `return new ArrayList<>(...);`

## 4. 설정 값 가이드 (`.env`)

수정 후에는 `.env` 파일만 수정하여 캐시를 켜고 끌 수 있습니다.

### 캐시를 끄고 싶을 때 (현재 권장 - 안전 모드)

Redis 연결을 아예 시도하지 않으며, 에러도 발생하지 않습니다.

```properties
SPRING_CACHE_TYPE=none
```

### 캐시를 켜고 싶을 때 (추후 안정화 시)

Redis 서버가 정상 작동 중이고, 직렬화 문제가 해결되었을 때 사용합니다.

```properties
SPRING_CACHE_TYPE=redis
```

## 5. 적용 방법 (배포)

Java 코드와 Docker 설정이 변경되었으므로, 이미지를 다시 빌드해야 적용됩니다.

```bash
docker compose up -d --build
```
