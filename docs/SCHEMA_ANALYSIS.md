# JPA 엔티티와 schema.sql 비교 분석 보고서

## 1. 개요

본 문서는 현재 프로젝트의 JPA 엔티티(@Entity) 정의와 `schema.sql` 파일에 명시된 데이터베이스 스키마 간의 불일치점을 분석하고, 해결 방안을 제시하는 것을 목적으로 합니다.

분석 결과, `schema.sql` 파일은 프로젝트 초기 단계의 스키마를 반영하고 있으며, 현재 Java 코드베이스의 엔티티 모델과 매우 큰 차이가 있음을 확인했습니다. **따라서 `schema.sql` 파일을 현재 DB 스키마의 기준으로 사용하는 것은 불가능합니다.**

`spring.jpa.hibernate.ddl-auto: validate` 옵션으로 배포 시, 스키마 불일치로 인해 애플리케이션 실행에 실패할 것이 확실합니다.

## 2. 주요 불일치 사항 요약

### 2.1. 신규 테이블 추가 (Entities exist, but Tables are missing in `schema.sql`)

아래 엔티티들에 해당하는 테이블이 `schema.sql`에 존재하지 않습니다. 개발 과정에서 새로 추가된 기능들입니다.

-   `manner_temp_histories` (`MannerTempHistory.java`) - 매너 온도 변경 이력
-   `addresses` (`Address.java`) - 사용자 배송지 목록
-   `search_keywords` (`SearchKeyword.java`) - 인기 검색어
-   `recipe_wishlists` (`RecipeWishlist.java`) - 레시피 찜 목록
-   `reports` (`Report.java`) - 게시물/사용자 신고
-   `recipes` (`Recipe.java`) - 레시피
-   `recipe_ingredients` (`RecipeIngredient.java`) - 레시피 재료
-   `recipe_steps` (`RecipeStep.java`) - 레시피 조리 단계
-   `recipe_corrections` (`RecipeCorrection.java`) - 레시피 정보 수정 제안
-   `comment_likes` (`CommentLike.java`) - 댓글 좋아요
-   `post_likes` (`PostLike.java`) - 게시글 좋아요
-   `direct_messages` (`DirectMessage.java`) - 1:1 쪽지

### 2.2. 테이블 구조의 주요 변경 사항

기존 테이블 중에서도 구조가 크게 변경된 항목들입니다.

-   **`users` 테이블**:
    -   **삭제된 컬럼**: `username`, `is_active`
    -   **추가된 컬럼**: `manner_temperature`, `comment_notification`, `group_purchase_notification`
    -   **변경/이름 변경**: `phone` (varchar 20) -> `phone_number` (varchar 13)

-   **`group_buys` 테이블**:
    -   **개념 변경**: `total_price` 컬럼이 사라지고, 목표 금액(`target_amount`)과 현재 달성 금액(`current_amount`)으로 분리되었습니다.
    -   **추가된 컬럼**: `latitude`, `longitude` (지도 연동을 위한 위경도)
    -   **타입 변경**: `category`, `delivery_method`, `status`가 `VARCHAR`에서 Java Enum 타입으로 변경되었습니다.

-   **`participations` 테이블**:
    -   **추가된 컬럼**: `address_id` (배송지 연결), `status` (참여 상태), `total_payment` (총 결제 금액)
    -   위 컬럼들은 `schema.sql`에 완전히 누락되어 있습니다.

-   **`wishlists` 테이블**:
    -   `schema.sql`은 `entity_type`을 사용한 다형적 관계(게시글, 댓글 등 모두 찜 가능)로 설계되어 있습니다.
    -   하지만 현재 `Wishlist.java` 엔티티는 **공동구매(`GroupBuy`) 전용**으로 구현되어 있어 스키마와 모델이 일치하지 않습니다. (`RecipeWishlist`가 별도로 생성됨)

### 2.3. 사소한 변경 및 불일치

-   **`point_histories`**: `description` 컬럼의 길이가 엔티티(200)와 스키마(255) 간에 다릅니다. (호환성 문제 없음)
-   **`group_buy_images`**: 엔티티에는 `(group_buy_id, displayOrder)`에 대한 `Unique` 제약조건이 있으나, `schema.sql`에는 없습니다.
-   **Enum 타입 사용**: `posts`, `notifications` 등 다수 테이블에서 `VARCHAR`로 정의된 컬럼들이 Java 코드에서는 `Enum` 타입으로 관리되고 있습니다.

## 3. 결론 및 권장 사항

**결론적으로 `schema.sql` 파일은 폐기(obsolete) 상태이며, 현재 시스템의 DB 스키마를 전혀 대변하지 못합니다.**

Render 등 새로운 환경에 배포할 때 다음 단계를 따르는 것을 강력히 권장합니다.

1.  **초기 배포 시 스키마 자동 생성**:
    -   `application.yml`의 `spring.jpa.hibernate.ddl-auto` 설정을 **`create`** 또는 **`update`**로 일시적으로 변경하여 배포합니다.
    -   이를 통해 Hibernate가 현재 JPA 엔티티를 기준으로 최신 DB 스키마를 데이터베이스에 자동으로 생성하게 합니다.

2.  **새로운 스키마 파일 생성 (Schema Dump)**:
    -   자동 생성된 데이터베이스에 DB 클라이언트로 접속하여, `pg_dump` 등의 도구를 사용해 최신 스키마 전체를 SQL 파일로 추출합니다.
    -   이 파일을 새로운 `schema-v2.sql` 또는 `generated-schema.sql` 등의 이름으로 프로젝트에 저장하여, 향후 DB 스키마의 기준으로 삼습니다.

3.  **`ddl-auto` 설정 원복**:
    -   스키마 백업 후에는 `ddl-auto` 설정을 다시 **`validate`** 또는 **`none`**으로 변경하여, 이후 배포에서는 스키마가 실수로 변경되는 것을 방지합니다.

이러한 과정을 통해 DB 스키마와 애플리케이션 코드 간의 정합성을 맞추고 안정적인 운영 환경을 구축할 수 있습니다.
