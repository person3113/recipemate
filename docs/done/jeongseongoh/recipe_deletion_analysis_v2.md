# 레시피 삭제 오류 분석 및 해결 방안 (v2)

이 문서는 레시피 삭제 기능과 관련하여 발생한 세 가지 이슈(찜 목록 조회 오류, 자식 엔티티 삭제, `@Where` 어노테이션 deprecated)를 종합적으로 분석하고, 이에 대한 단계별 해결 방안을 제시합니다.

## 1. 문제 상황 요약

최초 해결책으로 `Recipe` 엔티티에 소프트 삭제(`@SQLDelete`, `@Where`)를 적용한 후, 다음과 같은 2차적인 문제들이 발생했습니다.

1.  **`EntityNotFoundException` 발생 (찜 목록 조회 시)**
    - **현상:** 소프트 삭제된 레시피를 찜한 사용자가 자신의 찜 목록 페이지에 접근하면, 서버에서 `EntityNotFoundException`이 발생하며 페이지가 열리지 않습니다.
    - **원인:** 찜 목록(`RecipeWishlist`)에서 레시피 정보를 가져오려 할 때, `@Where(clause = "deleted_at IS NULL")` 조건 때문에 소프트 삭제된 `Recipe` 엔티티를 찾지 못하여 발생하는 문제입니다.

2.  **자식 엔티티(재료, 조리법)의 물리적 삭제**
    - **현상:** `Recipe`는 소프트 삭제되었지만, 하위 `RecipeIngredient`와 `RecipeStep` 데이터는 데이터베이스에서 물리적으로 삭제(Hard Delete)됩니다.
    - **원인:** `Recipe`의 `delete()` 요청이 `orphanRemoval = true` 옵션에 의해 자식 엔티티로 전파될 때, 자식 엔티티에는 소프트 삭제 설정(`@SQLDelete`)이 없어 기본 동작인 하드 삭제가 실행되기 때문입니다.

3.  **`@Where` 어노테이션의 Deprecated 이슈**
    - **현상:** `org.hibernate.annotations.Where`는 Hibernate 6.3 버전부터 Deprecated 되었습니다.
    - **분석:** 최신 버전의 Hibernate 사용 시 권장되지 않는 기술이며, 향후 유지보수를 위해 대안을 인지하고 있어야 합니다.

## 2. 종합 해결 방안

아래의 단계별 해결책을 통해 위 세 가지 문제를 모두 해결할 수 있습니다.

### 해결 1: `EntityNotFoundException` 방지

`RecipeWishlist`가 참조하는 `Recipe`가 소프트 삭제되어 찾을 수 없더라도, 오류를 발생시키는 대신 `null`을 반환하도록 처리합니다.

- **수정 파일:** `src/main/java/com/recipemate/domain/recipewishlist/entity/RecipeWishlist.java`
- **수정 내용:** `Recipe`와 연결된 `@ManyToOne` 어노테이션에 `@NotFound(action = NotFoundAction.IGNORE)`를 추가합니다.

```java
// import 추가
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

// ...

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "recipe_id", nullable = false, foreignKey = @ForeignKey(name = "fk_recipe_wishlist_recipe"))
@NotFound(action = NotFoundAction.IGNORE) // 이 줄을 추가
private Recipe recipe;
```

- **결과:** 이제 찜 목록을 조회할 때, 소프트 삭제된 레시피는 `null`로 처리됩니다. 프론트엔드에서는 이 `null` 값을 확인하여 "삭제된 레시피입니다."와 같이 사용자에게 안내할 수 있습니다.

### 해결 2: 자식 엔티티 소프트 삭제 일관성 적용

`Recipe`와 생명주기를 같이하는 자식 엔티티(`RecipeIngredient`, `RecipeStep`)에도 소프트 삭제를 동일하게 적용하여 데이터의 일관성을 유지합니다.

- **수정 파일 1:** `src/main/java/com/recipemate/domain/recipe/entity/RecipeIngredient.java`
- **수정 파일 2:** `src/main/java/com/recipemate/domain/recipe/entity/RecipeStep.java`
- **수정 내용:** 두 파일의 클래스 상단에 `@SQLDelete`와 `@Where` 어노테이션을 추가합니다. (`Recipe`에 적용했던 것과 동일)

**`RecipeIngredient.java` 수정 예시:**
```java
// import 추가
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Table(name = "recipe_ingredients", ...)
@SQLDelete(sql = "UPDATE recipe_ingredients SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class RecipeIngredient extends BaseEntity {
    // ...
}
```

**`RecipeStep.java` 수정 예시:**
```java
// import 추가
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Table(name = "recipe_steps", ...)
@SQLDelete(sql = "UPDATE recipe_steps SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class RecipeStep extends BaseEntity {
    // ...
}
```

- **결과:** 이제 `Recipe`가 소프트 삭제될 때, `orphanRemoval`에 의해 자식들에게도 `delete()`가 전파되며, 각 자식 엔티티에 정의된 `@SQLDelete`가 동작하여 모두 일관되게 소프트 삭제됩니다.

### 해결 3: `@Where` Deprecated 이슈에 대한 실용적 접근

`@Where`는 비록 Deprecated 되었지만, 당면한 문제들을 해결하는 가장 간단하고 직관적인 방법입니다. 최신 대안인 `@Filter`는 적용을 위해 코드 베이스 전반에 더 많은 수정(Filter 정의, 세션마다 활성화 등)이 필요하여, 현재 단계에서는 과도한 복잡성을 야기할 수 있습니다.

따라서 다음과 같은 실용적인 접근을 권장합니다.

1.  **현재:** `@Where` 어노테이션을 그대로 사용하여 문제를 해결합니다. 기능은 정상적으로 동작하며, Deprecated 경고는 빌드 실패를 유발하지 않습니다.
2.  **미래:** 향후 대규모 리팩토링이나 Hibernate 메이저 버전 업그레이드 시점에 `@Filter`로 전환하는 것을 고려합니다.

## 3. 최종 결론 및 작업 계획

1.  `RecipeWishlist` 엔티티에 `@NotFound(action = NotFoundAction.IGNORE)`를 추가하여 찜 목록 조회 오류를 해결합니다.
2.  `RecipeIngredient`와 `RecipeStep` 엔티티에 `@SQLDelete`와 `@Where`를 추가하여 데이터 삭제 일관성을 확보합니다.
3.  `@Where`의 Deprecated 이슈는 인지하되, 현재는 기능 구현의 단순성과 효율성을 위해 그대로 사용합니다.

이상의 조치를 통해 레시피 삭제와 관련된 모든 이슈를 해결하고, 안정적이고 일관된 데이터 관리 체계를 구축할 수 있습니다.
