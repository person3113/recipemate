# 레시피 삭제 오류 분석 및 해결 방안

## 1. 요약

사용자가 작성한 레시피를 삭제할 때 발생하는 서버 오류는, 데이터베이스의 외래 키 제약조건(Foreign Key Constraint)을 위반하기 때문에 발생합니다.

문제의 근본 원인은 **애플리케이션 아키텍처가 의도한 '소프트 삭제(Soft Delete)' 방식과 실제 구현된 '하드 삭제(Hard Delete)' 방식의 불일치**에 있습니다.

본 문서는 이 문제의 원인을 분석하고, JPA의 `@SQLDelete`와 `@Where` 어노테이션을 사용하여 아키텍처에 부합하는 올바른 소프트 삭제를 구현하는 해결 방안을 제시합니다.

## 2. 오류 상세 분석

### 가. 발생 현상

- **API Endpoint:** `DELETE /recipes/{recipeId}`
- **오류 로그:**
  ```sql
  Referential integrity constraint violation: "FK_RECIPE_WISHLIST_RECIPE: PUBLIC.RECIPE_WISHLISTS FOREIGN KEY(RECIPE_ID) REFERENCES PUBLIC.RECIPES(ID)"
  ...
  delete from recipes where id=?
  ```

### 나. 오류 원인

1.  **직접적 원인: 외래 키 제약조건 위반**
    - `RECIPE_WISHLISTS` 테이블(찜 목록)에는 `RECIPE_ID`라는 외래 키가 존재하며, 이는 `RECIPES` 테이블의 `ID`를 참조합니다.
    - 오류는 `RECIPES` 테이블에서 특정 레시피 데이터를 물리적으로 삭제(`DELETE`)하려고 할 때, `RECIPE_WISHLISTS` 테이블에 해당 레시피를 참조하는 데이터가 여전히 남아있어 발생합니다. 데이터베이스는 데이터 무결성을 지키기 위해 이 삭제 요청을 거부합니다.

2.  **근본적 원인: 소프트 삭제 아키텍처와 하드 삭제 구현의 불일치**
    - `BaseEntity` 클래스에는 `deletedAt` 필드가 정의되어 있습니다. 이는 엔티티를 물리적으로 삭제하지 않고, 삭제된 시간을 기록하여 비활성화하는 **소프트 삭제(Soft Delete)** 방식을 채택했음을 의미합니다.
    - 하지만 `RecipeService`의 `deleteUserRecipe` 메서드 내에서 호출되는 `recipeRepository.delete(recipe)`는 JPA의 기본 동작인 **하드 삭제(Hard Delete)**, 즉 `DELETE` SQL 구문을 실행합니다.
    - 결국, 소프트 삭제를 하도록 설계된 시스템에서 하드 삭제가 실행되면서 예기치 않은 제약조건 위반 오류가 발생한 것입니다.

## 3. 현재 구현의 문제점

- **`Recipe` 엔티티:** `BaseEntity`를 상속하여 `deletedAt` 필드를 가지고 있지만, 소프트 삭제를 자동으로 처리해주는 `@SQLDelete`나 `@Where` 같은 어노테이션이 누락되어 있습니다.
- **`RecipeService`:** `recipeRepository.delete(recipe)` 호출 시, `@SQLDelete`가 없으므로 JPA는 이를 하드 삭제로 해석하여 실행합니다.
- **`orphanRemoval = true`의 오해:** `Recipe` 엔티티의 `ingredients`와 `steps`에 적용된 `orphanRemoval = true`는 레시피와 생명주기를 같이하는 자식 엔티티(재료, 조리단계)에만 유효합니다. 찜 목록(`RecipeWishlist`)처럼 독립적인 생명주기를 가지며 레시피를 참조하는 다른 엔티티에는 영향을 주지 못합니다.

## 4. 해결 방안 (권장)

JPA 기능을 활용하여 `Recipe` 엔티티가 소프트 삭제를 올바르게 수행하도록 수정합니다. 이 방식은 서비스 로직 변경 없이 엔티티 설정만으로 문제를 해결할 수 있는 가장 이상적인 방법입니다.

**수정 대상 파일:** `src/main/java/com/recipemate/domain/recipe/entity/Recipe.java`

`Recipe` 엔티티 클래스에 아래 두 어노테이션을 추가합니다.

```java
// import 추가
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Table(name = "recipes", ...)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
@SQLDelete(sql = "UPDATE recipes SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?") // 1. SQLDelete 추가
@Where(clause = "deleted_at IS NULL") // 2. Where 추가
public class Recipe extends BaseEntity {
    // ... 기존 필드 ...
}
```

### 각 어노테이션의 역할

1.  **`@SQLDelete(sql = ...)`**
    - JPA의 `delete` 요청을 가로채서 `DELETE` SQL 대신 명시된 `UPDATE` SQL을 실행하도록 합니다.
    - 이제 `recipeRepository.delete(recipe)`가 호출되면, 실제로 레코드가 삭제되는 대신 `deleted_at` 필드에 현재 시간이 기록됩니다.

2.  **`@Where(clause = "deleted_at IS NULL")`**
    - 해당 엔티티에 대한 모든 `SELECT` 쿼리 실행 시, `WHERE deleted_at IS NULL` 조건을 자동으로 추가합니다.
    - 이로써 애플리케이션의 모든 곳(목록 조회, 상세 조회 등)에서 별도의 조건 추가 없이 삭제된 레시피가 자연스럽게 제외됩니다.

이 두 어노테이션을 적용하면, `RecipeService`의 `deleteUserRecipe` 메서드를 수정할 필요 없이 기존 코드가 그대로 소프트 삭제를 수행하게 됩니다.

## 5. 연관 데이터의 비즈니스적 처리

소프트 삭제를 도입하면, 레시피와 연관된 다른 데이터(찜, 공동구매)를 잃지 않고 비즈니스 로직을 우아하게 처리할 수 있습니다.

- **찜 목록 (`RecipeWishlist`)**
  - 레시피가 소프트 삭제되어도 찜 목록 데이터는 그대로 유지됩니다.
  - UI/프론트엔드에서는 사용자의 찜 목록을 보여줄 때, 연결된 레시피가 삭제된 상태(`deletedAt != null`)인지 확인하여 "삭제된 레시피입니다" 와 같이 표시할 수 있습니다. 데이터가 보존되므로 사용자는 자신의 활동 기록을 잃지 않습니다.

- **공동구매 (`GroupBuy`)**
  - `GroupBuy`는 `Recipe`를 직접 참조(FK)하는 대신 `recipeApiId`라는 문자열 필드로 연결되므로, 레시피 삭제가 공동구매 데이터에 직접적인 영향을 주지는 않습니다.
  - 소프트 삭제가 적용되면, 공동구매 상세 페이지에서 `recipeApiId`로 레시피를 조회했을 때 `@Where` 절에 의해 레시피가 조회되지 않을 것입니다.
  - 이를 통해 "원본 레시피가 삭제되었습니다"와 같은 안내 문구를 표시할 수 있으며, 공동구매 기록은 안전하게 보존됩니다.

## 6. 결론

`Recipe` 엔티티에 `@SQLDelete`와 `@Where` 어노테이션을 추가하여, 프레임워크 레벨에서 소프트 삭제 로직을 완성하는 것을 권장합니다. 이 방법은 다음과 같은 장점을 가집니다.

- **안전성:** 데이터 무결성을 보장하며, 연관된 데이터를 유실하지 않습니다.
- **일관성:** `BaseEntity`를 통한 소프트 삭제 아키텍처 설계를 일관되게 따릅니다.
- **단순성:** 서비스 로직의 변경 없이, 엔티티 선언만으로 문제를 해결할 수 있습니다.
