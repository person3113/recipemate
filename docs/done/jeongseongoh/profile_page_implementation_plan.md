# 프로필 페이지 구현 계획

## 1. 개요

사용자 닉네임이나 프로필 이미지를 클릭하여 해당 사용자의 프로필 페이지로 이동하는 기능을 구현합니다.
프로필 페이지에는 사용자의 기본 정보(매너 온도, 획득 배지)와 해당 사용자가 주최한 공동구매 목록이 표시됩니다.

## 2. 코드베이스 분석 결과

- **프로필 페이지**: 현재 프로젝트에 사용자 프로필을 표시하는 페이지가 존재하지 않음을 확인했습니다.
- **관련 도메인**:
    - `User`: `mannerTemperature`, `nickname` 등 사용자 정보를 포함하고 있습니다.
    - `GroupBuy`: `host` 필드를 통해 주최자(`User`)와 `ManyToOne` 관계로 연결되어 있습니다.
    - `Badge`: `user` 필드를 통해 사용자(`User`)와 `ManyToOne` 관계로 연결되어 있습니다.
- **데이터 접근**:
    - `UserRepository`: `findByNickname()` 메소드를 통해 닉네임으로 사용자를 조회할 수 있습니다.
    - `GroupBuyRepository`: `findByHostIdAndNotDeleted()` 메소드를 통해 특정 사용자가 주최한 공동구매 목록을 조회할 수 있습니다.
    - `BadgeRepository`: `findByUser()` 메소드를 통해 사용자가 획득한 배지 목록을 조회할 수 있습니다.
- **구현 위치**:
    - **Backend**: `UserController` 와 `UserService` 에 프로필 조회 관련 로직을 추가하는 것이 적합합니다.
    - **Frontend**: `src/main/resources/templates/user/` 경로에 새로운 프로필 페이지 템플릿을 추가하고, 기존 여러 템플릿에서 사용자 닉네임에 링크를 추가해야 합니다.

## 3. 구현 계획

### 3.1. 백엔드 (Backend)

1.  **DTO 생성**: 프로필 페이지에 필요한 데이터를 담을 `UserProfileResponseDto`를 생성합니다.
    - `com.recipemate.domain.user.dto.UserProfileResponseDto`
    ```java
    @Getter
    @AllArgsConstructor
    public class UserProfileResponseDto {
        private String nickname;
        private Double mannerTemperature;
        private List<BadgeType> badges; // BadgeType Enum 또는 Badge DTO
        private Page<GroupBuySummaryDto> hostedGroupBuys; // 공동구매 목록 DTO
    }
    ```

2.  **Service 로직 추가**: `UserService`에 프로필 데이터를 조회하는 메소드를 추가합니다.
    - `com.recipemate.domain.user.service.UserService`
    ```java
    public UserProfileResponseDto getUserProfile(String nickname, Pageable pageable) {
        // 1. nickname으로 User 조회
        User user = userRepository.findByNickname(nickname)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 2. 사용자가 획득한 Badge 목록 조회
        List<Badge> badges = badgeRepository.findByUser(user);
        List<BadgeType> badgeTypes = badges.stream().map(Badge::getBadgeType).collect(Collectors.toList());

        // 3. 사용자가 주최한 공동구매 목록 조회 (Page)
        Page<GroupBuy> groupBuys = groupBuyRepository.findByHostIdAndNotDeleted(user.getId(), pageable);
        Page<GroupBuySummaryDto> hostedGroupBuysDto = groupBuys.map(GroupBuySummaryDto::from);

        // 4. UserProfileResponseDto로 변환하여 반환
        return new UserProfileResponseDto(user.getNickname(), user.getMannerTemperature(), badgeTypes, hostedGroupBuysDto);
    }
    ```

3.  **Controller 엔드포인트 추가**: `UserController`에 프로필 페이지를 반환하는 GET 엔드포인트를 추가합니다.
    - `com.recipemate.global.web.controller.user.UserController`
    ```java
    @GetMapping("/profile/{nickname}")
    public String userProfile(@PathVariable String nickname, Model model, @PageableDefault(size = 10) Pageable pageable) {
        UserProfileResponseDto userProfile = userService.getUserProfile(nickname, pageable);
        model.addAttribute("userProfile", userProfile);
        return "user/profile";
    }
    ```

### 3.2. 프론트엔드 (Frontend)

1.  **프로필 페이지 템플릿 생성**: 조회된 프로필 데이터를 화면에 표시할 `profile.html` 파일을 생성합니다.
    - `src/main/resources/templates/user/profile.html`
    - 이 파일은 `base.html` 레이아웃을 상속받아 구성합니다.
    - 사용자 정보(닉네임, 매너온도), 배지 목록, 주최한 공동구매 목록(카드 형태) 및 페이징 처리를 포함합니다.

2.  **UI/UX 고려사항**: 향후 1:1 쪽지 기능이 추가될 가능성을 고려하여, 프로필 페이지에 '쪽지 보내기' 버튼을 추가할 공간을 미리 확보하는 디자인을 적용하는 것을 권장합니다.

3.  **기존 템플릿 수정**: 다른 페이지에 표시되는 사용자 닉네임에 프로필 페이지로 이동하는 링크를 추가합니다.
    - 대상 파일 예시:
        - `src/main/resources/templates/group-purchases/detail.html` (주최자 닉네임)
        - `src/main/resources/templates/community-posts/detail.html` (작성자 닉네임)
        - `src/main/resources/templates/fragments/comments.html` (댓글 작성자 닉네임)
    - 수정 방식:
        ```html
        <!-- 기존 코드 -->
        <span th:text="${comment.authorNickname}"></span>

        <!-- 변경 후 코드 -->
        <a th:href="@{/profile/{nickname}(nickname=${comment.authorNickname})}" th:text="${comment.authorNickname}"></a>
        ```

## 4. 예상 파일 변경/추가 목록

- **새 파일**:
    - `src/main/java/com/recipemate/domain/user/dto/UserProfileResponseDto.java`
    - `src/main/resources/templates/user/profile.html`

- **수정될 파일**:
    - `src/main/java/com/recipemate/domain/user/service/UserService.java`
    - `src/main/java/com/recipemate/global/web/controller/user/UserController.java`
    - `src/main/resources/templates/group-purchases/detail.html`
    - `src/main/resources/templates/community-posts/detail.html`
    - `src/main/resources/templates/fragments/comments.html`
    - (기타 닉네임이 표시되는 모든 템플릿)
