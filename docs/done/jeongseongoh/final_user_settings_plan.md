# 마이페이지 설정 기능 최종 구현 계획

`URL_DESIGN.md`와 기존 코드 분석을 통해, 프로젝트의 서버 사이드 렌더링(SSR) 아키텍처에 맞춰 사용자 설정 기능 구현 계획을 최종적으로 수립했습니다. 이 문서는 `POST` 기반의 폼 처리와 Thymeleaf 템플릿 구조를 따릅니다.

---

## 1. 닉네임 변경

### 1.1. 분석

- 기존 코드의 `POST /users/me` 엔드포인트와 `UserService.updateProfile` 메소드는 이미 닉네임, 전화번호, 프로필 이미지를 한 번에 수정하는 기능을 제공하고 있습니다.
- `UserService.updateProfile` 내부에는 닉네임 중복 확인 로직도 이미 구현되어 있습니다.
- 따라서, 가장 간단한 방법은 `settings.html`의 기존 프로필 수정 폼을 그대로 사용하는 것입니다.

### 1.2. 프로젝트 컨벤션에 맞는 URL 설계

만약 닉네임만 독립적으로 변경하는 기능을 구현한다면, `URL_DESIGN.md`의 `POST /users/me/password` 패턴을 따르는 것이 일관성에 좋습니다.

- **URL**: `POST /users/me/nickname`
- **역할**: 닉네임만 수정 처리
- **방식**: Thymeleaf 폼 제출

### 1.3. 구현 방안 (독립 기능으로 구현 시)

1.  **Controller (`UserController`)**:
    - `POST /users/me/nickname` 요청을 처리할 메소드를 추가하고, 성공 또는 실패 메시지를 `RedirectAttributes`를 통해 전달합니다.

2.  **Service (`UserService`)**:
    - `updateNickname` 메소드를 만들어 기존 `updateProfile`의 닉네임 관련 로직을 재사용 또는 분리합니다.

---

## 2. 알림 설정

### 2.1. URL 설계

- **URL**: `POST /users/me/notifications`
- **역할**: 알림 수신 여부 설정 (On/Off)
- **방식**: 비동기 폼 제출 (페이지 새로고침 없이 즉시 반영)

### 2.2. 구현 방안

1.  **Entity (`User.java`)**:
    - 알림 종류별 `boolean` 필드를 추가합니다.

    ```java
    // User.java
    @Builder.Default
    @Column(nullable = false)
    private boolean commentNotification = true;

    @Builder.Default
    @Column(nullable = false)
    private boolean groupPurchaseNotification = true;
    ```

2.  **Controller (`UserController`)**:
    - `POST /users/me/notifications` 요청을 처리할 메소드를 추가합니다.

3.  **Service (`UserService`)**:
    - `updateNotificationSettings` 메소드를 추가하여 사용자의 알림 설정을 업데이트합니다.

4.  **Template (`settings.html`)**:
    - 알림 설정 폼을 추가합니다. "즉시 반영"을 위해 각 토글 스위치에 htmx 속성(`hx-post`, `hx-trigger="change"`)을 추가하여, 변경 시마다 폼이 자동으로 제출되게 합니다.

---

## 3. 계정 탈퇴

### 3.1. URL 설계

- **URL**: `POST /users/me/delete`
- **역할**: 회원 탈퇴 처리
- **분석**: `URL_DESIGN.md`에 이미 정의된 URL이므로 이 패턴을 따릅니다.

### 3.2. 구현 방안

1.  **Entity (`User.java`)**:
    - 소프트 삭제(Soft Deletion)를 위한 `deletedAt` 필드를 추가합니다.
    - `@SQLDelete`와 `@Where`를 사용하여 JPA가 자동으로 소프트 삭제를 처리하도록 설정합니다.

    ```java
    // User.java
    import org.hibernate.annotations.SQLDelete;
    import org.hibernate.annotations.Where;
    import java.time.LocalDateTime;

    @SQLDelete(sql = "UPDATE users SET deleted_at = NOW() WHERE id = ?")
    @Where(clause = "deleted_at IS NULL")
    public class User extends BaseEntity {
        // ... 기존 필드 ...
        private LocalDateTime deletedAt;
    }
    ```

2.  **Controller (`UserController`)**:
    - `POST /users/me/delete`를 처리할 메소드를 추가합니다. 비밀번호 불일치나 탈퇴 불가 조건 시 `RedirectAttributes`로 에러 메시지를 전달합니다.

3.  **Service (`UserService`)**:
    - `deleteAccount` 비즈니스 로직을 구현합니다.
    - **(1) 비밀번호 확인**: 입력된 비밀번호와 DB의 비밀번호가 일치하는지 확인합니다.
    - **(2) 진행 중인 공구 확인**: `GroupPurchaseRepository`를 사용하여 해당 유저가 주최자이고, 상태가 '진행 중'인 공구가 있는지 확인합니다.
    - **(3) 소프트 삭제**: 모든 조건이 충족되면 `UserRepository.deleteById(userId)`를 호출합니다. (`@SQLDelete`에 의해 `UPDATE` 쿼리가 실행됩니다.)

4.  **Template (`settings.html`)**:
    - 계정 탈퇴 버튼과 비밀번호를 입력받는 모달(modal) 창을 추가합니다.
    - 사용자가 '탈퇴' 버튼을 누르면, JavaScript로 최종 확인을 거친 후 폼을 제출합니다.

---

## 4. 삭제된 사용자 처리

소프트 삭제를 적용하면 사용자의 데이터는 DB에 남지만, `@Where(clause = "deleted_at IS NULL")` 조건 때문에 일반적인 조회에서는 제외됩니다. 이로 인한 후속 처리는 다음과 같습니다.

1.  **게시글, 댓글 등 사용자 컨텐츠**:
    - 작성자 정보(`user_id`)는 그대로 유지됩니다.
    - 컨텐츠를 조회할 때 `author` 정보가 `null`일 경우 (JPA 연관관계에서 soft-delete된 유저는 조회되지 않음), 프론트엔드에서 "탈퇴한 사용자"로 표시하도록 처리합니다.
    - 이를 위해 `Post`, `Comment` 등의 DTO에서 사용자 정보를 포함할 때 `User` 객체가 `null`이 될 수 있음을 고려해야 합니다.

2.  **프로필 조회, 쪽지 보내기 등 직접적인 상호작용**:
    - 특정 사용자의 프로필을 조회하거나 쪽지를 보내는 기능의 서비스 로직 초입에서 대상 사용자가 존재하는지 `UserRepository`를 통해 확인합니다.
    - `@Where` 절 덕분에 `findById` 등으로 조회 시 탈퇴한 사용자는 조회되지 않으므로, `UserNotFoundException` 등을 발생시켜 "존재하지 않는 사용자입니다." 와 같은 메시지를 자연스럽게 반환할 수 있습니다.

---

## 5. 주요 수정/추가 파일 요약

- **Entity**: `src/main/java/com/recipemate/domain/user/entity/User.java`
- **Repository**: `src/main/java/com/recipemate/repository/GroupPurchaseRepository.java` (진행 중 공구 확인용 쿼리 추가)
- **Service**: `src/main/java/com/recipemate/domain/user/service/UserService.java`
- **Controller**: `src/main/java/com/recipemate/domain/user/controller/UserController.java`
- **Exception**: `src/main/java/com/recipemate/global/exception/ErrorCode.java` (필요 시 에러 코드 추가)
- **Template**: `src/main/resources/templates/user/settings.html`
