# GroupBuy 목록 조회 시 발생하는 IllegalAccessException 분석 및 해결 방안

## 1. 문제 현황

공구 참여 후 `/group-purchases/list` 페이지에 접근할 때 간헐적으로 서버에서 500 에러가 발생하며, 로그에는 다음과 같은 예외가 기록됩니다.

```
com.querydsl.core.types.ExpressionException: class com.querydsl.core.types.ConstructorExpression cannot access a member of class com.recipemate.domain.groupbuy.repository.GroupBuyRepositoryImpl$ReviewStatsProjection with modifiers "public"
Caused by: java.lang.IllegalAccessException: class com.querydsl.core.types.ConstructorExpression cannot access a member of class com.recipemate.domain.groupbuy.repository.GroupBuyRepositoryImpl$ReviewStatsProjection with modifiers "public"
```

이 문제는 `GroupBuyRepositoryImpl`의 `searchGroupBuysWithReviewStats` 메소드 내에서 QueryDSL을 사용하여 리뷰 통계를 프로젝션(projection)하는 과정에서 발생합니다.

## 2. 원인 분석

에러 로그의 핵심은 `IllegalAccessException`입니다. 이는 특정 클래스나 멤버에 접근할 권한이 없을 때 발생합니다. 문제의 원인은 QueryDSL이 동적으로 객체를 생성하는 방식과 관련이 있습니다.

1.  **QueryDSL의 프로젝션 방식**:
    `Projections.constructor(...)`는 실행 시점에 리플렉션(reflection)을 사용하여 지정된 클래스(`ReviewStatsProjection`)의 생성자를 호출하고, 그 결과로 객체를 인스턴스화합니다.

2.  **`ReviewStatsProjection` 클래스의 접근 제한**:
    해당 클래스는 `GroupBuyRepositoryImpl` 내부에 `private static` 내부 클래스(inner class)로 선언되어 있습니다.
    - `static`으로 선언한 것은 좋으나, `private` 접근 제어자가 문제의 원인입니다.
    - 클래스가 `private`으로 선언되면, 해당 클래스를 감싸고 있는 `GroupBuyRepositoryImpl` 외부에서는 이 클래스의 존재 자체를 알 수 없습니다.

3.  **리플렉션과 접근 제어**:
    QueryDSL 라이브러리는 `GroupBuyRepositoryImpl` 클래스 외부에서 동작하는 코드입니다. 따라서 리플렉션을 통해 `ReviewStatsProjection` 클래스를 찾고 생성자를 호출하려고 시도할 때, `private`으로 설정된 접근 제어자 때문에 접근이 차단되어 `IllegalAccessException`이 발생합니다.

생성자가 `public`일지라도, 클래스 자체가 `private`이므로 외부에서는 접근이 불가능합니다.

## 3. 해결 방안

이 문제를 해결하기 위해서는 QueryDSL의 리플렉션 메커니즘이 `ReviewStatsProjection` 클래스에 접근할 수 있도록 **접근 제어자를 `private`에서 `public`으로 변경**해야 합니다.

### 수정 대상 파일

-   `src/main/java/com/recipemate/domain/groupbuy/repository/GroupBuyRepositoryImpl.java`

### 수정 전 코드

```java
    /**
     * 리뷰 통계 조회용 내부 클래스
     * QueryDSL Projection을 위한 간단한 DTO
     */
    private static class ReviewStatsProjection {
        // ...
        public ReviewStatsProjection(Long groupBuyId, Double avgRating, Long reviewCount) {
            // ...
        }
        // ...
    }
```

### 수정 후 코드

```java
    /**
     * 리뷰 통계 조회용 내부 클래스
     * QueryDSL Projection을 위한 간단한 DTO
     */
    public static class ReviewStatsProjection { // private -> public 으로 변경
        // ...
        public ReviewStatsProjection(Long groupBuyId, Double avgRating, Long reviewCount) {
            // ...
        }
        // ...
    }
```

이와 같이 수정하면 `ReviewStatsProjection` 클래스가 외부에서도 접근 가능해져 QueryDSL이 정상적으로 프로젝션 객체를 생성할 수 있게 되고, `IllegalAccessException` 문제가 해결됩니다.
