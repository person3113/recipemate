# Render 배포 오류 분석 (v7) - 메모리 부족

## 1. 문제 상황

- **오류 로그:** `tmp_log7.md`
- **핵심 오류:** `==> Out of memory (used over 512Mi)`
- **발생 시점:** 2025년 11월 24일, `CacheConfig.java` 수정 후 재배포 시도 중

이전의 `SerializationException`을 해결하기 위해 `CacheConfig.java`를 수정한 후 배포했지만, 이번에는 Render의 512MB 메모리 한계를 초과하여 배포에 실패했습니다.

## 2. 오류 분석

### 근본 원인: Spring Boot 애플리케이션 메모리 사용량 > Render 무료 티어 할당 메모리 (512MB)

로그에서 명확히 확인된 `Out of memory` 오류는 애플리케이션이 필요로 하는 메모리가 Render에서 할당된 메모리보다 많다는 것을 의미합니다.

1.  **Spring Boot의 기본 메모리 사용량**: Spring Boot 애플리케이션은 내장 웹 서버(Tomcat), JPA(Hibernate), 각종 자동 설정 및 라이브러리 등으로 인해 시작 시점에 이미 상당한 양의 메모리(일반적으로 300-400MB 이상)를 차지합니다.
2.  **`CacheConfig` 수정으로 인한 메모리 증가**: 이전 `SerializationException`을 해결하기 위해 `CacheConfig.java`에서 `ObjectMapper`를 직접 생성하고 다형성 처리(`PolymorphicTypeValidator`)를 설정했습니다. 이 작업은 올바른 해결책이었으나, 애플리케이션 시작 시점에 더 많은 객체를 생성하고 메모리에 상주시키게 만들어 전반적인 메모리 사용량을 증가시켰습니다.
3.  **한계점 도달**: 이 추가적인 메모리 부담이 더해져, 가용 메모리가 빠듯한 Render 무료 티어의 512MB 한계를 초과하게 된 것으로 판단됩니다.

결론적으로, **직렬화 문제를 해결하기 위한 코드 수정이 의도치 않게 메모리 사용량을 늘려 새로운 문제인 메모리 부족을 야기**했습니다.

## 3. 해결 방안

메모리 사용량을 줄이거나, 더 많은 메모리를 확보하는 방향으로 접근해야 합니다.

---

### **옵션 1: `CacheConfig` 설정 최적화 (권장 시도)**

현재 `CacheConfig`의 다형성 타입 검사기(`PolymorphicTypeValidator`) 설정이 `allowIfBaseType(Object.class)`로 되어 있어 매우 광범위합니다. 이 범위를 애플리케이션의 실제 DTO가 포함된 패키지로 좁혀주면, 검사기의 내부 동작을 최적화하고 메모리 사용량을 약간이나마 줄일 수 있습니다.

**수정 제안:**

`CacheConfig.java`에서 `PolymorphicTypeValidator` 부분을 다음과 같이 수정합니다.

```java
// 수정 전
PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
        .allowIfBaseType(Object.class)
        .build();

// 수정 후
PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
        .allowIfSubType("com.recipemate") // 베이스 패키지를 지정하여 범위를 좁힘
        .build();
```
이 방법은 메모리 절감 효과가 크지 않을 수 있으나, 보안적으로 더 안전하며 시도해 볼 가치가 있는 최적화입니다.

---

### **옵션 2: JVM 메모리 설정(`JAVA_OPTS`) 미세 조정**

Render 환경 변수에 설정된 `JAVA_OPTS`를 좀 더 타이트하게 조정하여 메모리를 확보하는 방법입니다.

-   **현재 설정**: `JAVA_OPTS="-Xmx300m -XX:MaxMetaspaceSize=128m ..."`
-   **조정 시도**: 최대 힙 메모리(`-Xmx`)나 Metaspace 크기를 약간 줄여볼 수 있습니다. 하지만 이미 300MB로 설정되어 있어 더 줄이면 애플리케이션 실행 자체가 불안정해질 수 있습니다. 예를 들어, `-Xmx280m -XX:MaxMetaspaceSize=100m` 등으로 변경해볼 수 있으나 성공을 보장하기는 어렵습니다.

---

### **옵션 3: 캐싱 기능 임시 비활성화 후 배포**

가장 확실하게 메모리 사용량을 줄여 일단 배포를 성공시키는 방법입니다. `prod` 프로파일에서도 캐싱을 사용하지 않도록 설정하여, `CacheConfig` 빈 자체가 생성되지 않도록 하는 것입니다.

**방법**:
`application.yml`의 `prod` 프로파일 부분에 `spring.cache.type: none`을 추가하거나, Render 환경변수 `SPRING_CACHE_TYPE`를 `none`으로 설정합니다.

-   **장점**: `CacheConfig` 관련 메모리 오버헤드가 사라져 배포에 성공할 확률이 높습니다.
-   **단점**: 캐싱 기능이 동작하지 않아 DB 부하가 증가하고 성능이 저하됩니다. 근본적인 해결책이 아닌 임시 방편입니다.

---

### **옵션 4: Render 요금제 업그레이드 (가장 현실적인 장기 해결책)**

**512MB는 상용 서비스를 고려하는 Spring Boot 애플리케이션에게는 매우 부족한 메모리**입니다. 개발 및 테스트 단계에서는 각종 기능을 비활성화하여 겨우 실행할 수 있지만, 실제 트래픽을 감당하기는 어렵습니다.

-   **해결책**: Render의 유료 플랜(예: 1GB 메모리 플랜)으로 업그레이드하는 것이 가장 안정적이고 확실한 해결책입니다. 이를 통해 메모리 제약에서 벗어나 애플리케이션의 모든 기능을 안정적으로 운영할 수 있습니다.

## 4. 다음 단계 제안

1.  **먼저 `옵션 1`(`CacheConfig` 최적화)을 시도해 보시는 것을 권장합니다.** 코드 수정이 간단하고, 성공 시 추가 비용 없이 문제를 해결할 수 있습니다.
2.  `옵션 1` 실패 시, 장기적인 관점에서 `옵션 4`(요금제 업그레이드)를 가장 우선적으로 고려하시는 것이 좋습니다.
3.  `옵션 3`은 다른 기능이 정상적으로 배포되는지 확인하기 위한 테스트 용도로만 사용하시는 것을 추천합니다.
