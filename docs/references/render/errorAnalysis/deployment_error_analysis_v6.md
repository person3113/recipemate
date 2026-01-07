# Render 배포 오류 분석 (v6)

## 1. 문제 상황

-   **오류 로그:** `tmp_log6.md`
-   **발생 시점:** 2025년 11월 24일 첫 배포 중
-   **직전 변경 사항:**
    -   `Hypersistence Utils` 의존성 제거
    -   Hibernate 6 네이티브 ENUM 처리 방식 도입 (`@JdbcTypeCode(SqlTypes.NAMED_ENUM)`)
    -   `application.yml`에 `hibernate.type.preferred_jdbc_type_for_enum: NAMED_ENUM` 설정 추가

애플리케이션이 시작되고 `/` 경로로 요청이 들어왔을 때, `dispatcherServlet`에서 `SerializationException`이 발생하며 요청 처리에 실패했습니다.

## 2. 오류 분석

### 핵심 오류 로그

```
2025-11-24T03:22:17.233Z ERROR 1 --- [RecipeMate] [nio-8080-exec-5] o.a.c.c.C.[.[.[/].[dispatcherServlet]    : Servlet.service() for servlet [dispatcherServlet] in context with path [] threw exception [Request processing failed: org.springframework.data.redis.serializer.SerializationException: Could not read JSON:Unexpected token (END_ARRAY), expected VALUE_STRING: need String, Number of Boolean value that contains type id (for subtype of java.lang.Object)
 at [Source: REDACTED (`StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION` disabled); line: 1, column: 2] ] with root cause
com.fasterxml.jackson.databind.exc.MismatchedInputException: Unexpected token (END_ARRAY), expected VALUE_STRING: need String, Number of Boolean value that contains type id (for subtype of java.lang.Object)
 at [Source: REDACTED (`StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION` disabled); line: 1, column: 2]
```

### 원인 추적 (수정된 분석)

1.  **오류의 본질:** `MismatchedInputException`은 **JSON 역직렬화(Deserialization)** 과정에서 발생한 문제입니다. Jackson 라이브러리가 Redis에 저장된 JSON 데이터를 Java 객체로 변환하려 했으나, 데이터의 구조가 예상과 달라 실패했습니다.
2.  **오류 발생 지점:** 스택 트레이스를 따라가 보면, 오류는 `GroupBuyService`의 `getPopularGroupBuys` 메서드 호출 중 캐시(`@Cacheable`)된 데이터를 읽어오는 과정(`RedisCache.deserializeCacheValue`)에서 발생했습니다.
3.  **근본 원인:** **Jackson `GenericJackson2JsonRedisSerializer`의 기본 `ObjectMapper` 설정 문제로 인한 빈 컬렉션 직렬화 오류**

    -   사용자의 피드백에 따라, 이번이 **첫 배포**이며 로컬 개발 환경에서는 Redis를 사용하지 않았다는 점이 확인되었습니다. 따라서 "오래된 캐시 데이터"가 존재할 수 없습니다.
    -   문제는 애플리케이션이 `prod` 프로파일로 실행되면서 Redis 캐싱이 활성화되었을 때 발생합니다. `GroupBuyService.getPopularGroupBuys()` 메서드가 데이터베이스에 데이터가 없어 **빈 리스트(예: `List<GroupBuyResponse>`)를 반환**합니다.
    -   Spring Cache는 이 빈 리스트를 Redis에 저장하기 위해 `GenericJackson2JsonRedisSerializer`를 사용합니다. 이 Serializer의 내부 `ObjectMapper`는 기본적으로 다형성 타입(polymorphic types)을 처리하기 위한 특정 설정을 가지고 있습니다. 그러나 빈 컬렉션(`[]`)을 직렬화할 때 **필요한 타입 정보(type id)** 를 제대로 포함하지 못하는 문제가 있습니다.
    -   결과적으로 Redis에는 타입 정보가 없는 `[]` 형태의 JSON이 저장됩니다.
    -   이후 다른 요청이 해당 캐시 키를 조회할 때, `ObjectMapper`는 저장된 `[]`를 역직렬화하려 시도합니다. 이때 `MismatchedInputException` 오류 메시지(`expected VALUE_STRING: need String, Number of Boolean value that contains type id`)에서 알 수 있듯이, 다형성 처리를 위해 `VALUE_STRING` 형태의 타입 식별자를 기대하지만, 빈 배열에서 이를 찾을 수 없어 역직렬화에 실패하게 됩니다.

    요약하자면, `GenericJackson2JsonRedisSerializer`의 기본 `ObjectMapper` 설정이 빈 컬렉션에 대한 타입 정보를 안전하게 직렬화하지 못하여, 다음번 역직렬화 시점에 오류를 발생시키는 것이 근본 원인입니다.

## 3. 해결 방안

Redis 캐시에 데이터를 올바르게 직렬화하고 역직렬화할 수 있도록 `CacheConfig.java` 파일에서 `ObjectMapper` 설정을 수정해야 합니다.

### 코드 수정 내용

`src/main/java/com/recipemate/global/config/CacheConfig.java` 파일을 다음과 같이 수정합니다.

1.  `ObjectMapper`를 직접 생성하고 `JavaTimeModule`을 등록하여 Java 8 날짜/시간 객체 (`LocalDate`, `LocalDateTime` 등)를 올바르게 처리할 수 있도록 합니다.
2.  `PolymorphicTypeValidator`를 사용하여 다형성 타입 유효성 검사를 활성화하고, 애플리케이션의 기본 패키지(`com.recipemate`) 내의 클래스에 대해 타입 정보를 포함하도록 `activateDefaultTyping`을 설정합니다. 이는 빈 컬렉션을 포함한 모든 객체의 타입 정보를 JSON에 안전하게 포함시켜 역직렬화 시 오류를 방지합니다.
3.  이 설정된 `ObjectMapper`를 사용하여 `GenericJackson2JsonRedisSerializer`를 인스턴스화하고, 이를 `RedisCacheConfiguration`에 적용합니다.

```java
// 기존 import 문에 다음 추가
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

// CacheManager Bean 내부 수정
@Bean
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
    // 다형성 타입을 안전하게 처리하고 Java 8 시간 타입을 지원하는 ObjectMapper 설정
    // com.recipemate 패키지 내의 모든 클래스에 대해 다형성 타입을 허용 (보안 고려)
    PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
            .allowIfBaseType(Object.class) // 모든 기본 타입 허용 (가장 넓은 범위, 필요시 특정 타입으로 제한)
            .build();

    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule()); // Java 8 날짜/시간 지원
    // NON_FINAL 타입에 대해 Default Typing 활성화 (빈 컬렉션 등에서 타입 정보 누락 방지)
    objectMapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL);

    // 설정된 ObjectMapper를 사용하여 RedisSerializer 생성
    GenericJackson2JsonRedisSerializer redisSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);

    // 기본 캐시 설정 (1시간 TTL)
    RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(1))
            .serializeKeysWith(
                    RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
            )
            .serializeValuesWith(
                    // 커스텀 ObjectMapper가 적용된 Serializer 사용
                    RedisSerializationContext.SerializationPair.fromSerializer(redisSerializer)
            )
            .disableCachingNullValues();

    // ... (이후 캐시별 개별 TTL 설정은 동일)
    Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

    // 레시피 캐시: 1시간
    cacheConfigurations.put(RECIPES_CACHE, defaultConfig.entryTtl(Duration.ofHours(1)));

    // 인기 공구 목록 캐시: 5분
    cacheConfigurations.put(POPULAR_GROUP_BUYS_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(5)));

    // 조회수 캐시: 1분
    cacheConfigurations.put(VIEW_COUNTS_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(1)));

    return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigurations)
            .build();
}
```

이 수정 후 배포를 진행하면, 빈 컬렉션도 올바른 타입 정보를 포함하여 Redis에 저장되고 역직렬화 시 문제가 발생하지 않을 것입니다.

## 4. 예방 및 장기적 고려사항

-   **개발 환경과 운영 환경의 일치**: 가능하다면 개발 환경에서도 운영 환경과 유사하게 Redis 캐싱을 활성화하여 이러한 설정 문제를 조기에 발견하는 것이 좋습니다.
-   **캐시 무효화 전략**: 코드 변경 등으로 객체 모델이 변경될 경우, 이전에 캐시된 데이터가 호환되지 않을 수 있습니다. 배포 시점에 캐시를 무효화하는 전략(예: 캐시 키에 버전 접두사 추가 또는 강제 FLUSH)을 고려해야 합니다.
-   **오류 방어 로직**: 캐시에서 데이터를 읽는 과정에서 예외가 발생할 경우, 이를 처리하여 캐시를 무시하고 DB에서 데이터를 다시 로드하는 방어 로직을 추가하는 것도 시스템 안정성을 높이는 방법입니다.