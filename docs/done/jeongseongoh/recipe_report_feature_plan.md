# 레시피 신고 및 개선 제안 기능 구현 계획

## 1. 목표 (Goal)

-   사용자가 레시피의 오류(오타, 잘못된 정보 등)나 개선사항을 제안하는 기능 추가.
-   관리자가 제안을 검토하고 처리(반영/기각)할 수 있는 전용 관리자 페이지 구현.
-   제안 처리 결과에 따라 제안자에게 보상(매너온도, 포인트)을 지급하고, 원작성자에게 패널티를 적용하는 시스템 구축.

## 2. 아키텍처 및 설계 (Architecture & Design)

### 신규 엔티티 추가
-   **`RecipeCorrection` 엔티티**를 `com.recipemate.domain.recipe.entity` 패키지에 생성합니다.
    -   **주요 필드**:
        -   `recipe` (ManyToOne, `Recipe`): 대상 레시피
        -   `proposer` (ManyToOne, `User`): 제안자
        -   `correctionType` (Enum): 제안 종류
        -   `proposedChange` (TEXT): 제안 내용
        -   `status` (Enum): 처리 상태
        -   `adminReason` (TEXT): 관리자 처리 사유
        -   `resolver` (ManyToOne, `User`): 처리한 관리자
    -   **`CorrectionType` Enum**: `TYPO`, `INCORRECT_INFO`, `SUGGESTION`, `OTHER`
    -   **`CorrectionStatus` Enum**: `PENDING`, `APPROVED`, `REJECTED`

### 신규 Repository 추가
-   `RecipeCorrectionRepository` 인터페이스를 `com.recipemate.domain.recipe.repository`에 생성 (`JpaRepository<RecipeCorrection, Long>` 상속).

### 서비스 계층 (Service Layer)
-   **`RecipeCorrectionService`** 클래스를 `com.recipemate.domain.recipe.service`에 생성합니다.
    -   `createCorrection(user, recipeId, dto)`: 제안 생성 로직.
    -   `getPendingCorrections()`: `PENDING` 상태의 모든 제안 조회 (관리자용).
    -   `approveCorrection(admin, correctionId, reason)`: 제안 승인 로직.
        -   `RecipeCorrection` 상태를 `APPROVED`로 변경.
        -   제안자(`proposer`)에게 매너온도/포인트 지급 (`UserService` 또는 `MannerTempService`와 협력).
        -   알림 발송 (`NotificationService`와 협력).
        -   **주의**: 실제 레시피 수정은 관리자가 수동으로 진행하도록 유도. 시스템이 자동으로 레시피를 변경하는 것은 위험하므로, 관리자 페이지에서 수정 페이지로 바로 가는 링크를 제공합니다.
    -   `rejectCorrection(admin, correctionId, reason)`: 제안 기각 로직.
        -   `RecipeCorrection` 상태를 `REJECTED`로 변경하고 `adminReason` 저장.
        -   제안자에게 기각 알림 발송.

### 컨트롤러 계층 (Controller Layer)
-   **`RecipeCorrectionController`**: 사용자 기능
    -   `GET /recipes/{id}/correction/new`: 제안서 작성 폼 페이지.
    -   `POST /recipes/{id}/correction`: 제안서 제출 처리.
-   **`AdminController`**: 관리자 기능
    -   `GET /admin/corrections`: `PENDING` 상태의 제안 목록 페이지.
    -   `GET /admin/corrections/{id}`: 제안 상세 및 처리 페이지.
    -   `POST /admin/corrections/{id}/approve`: 제안 승인 처리.
    -   `POST /admin/corrections/{id}/reject`: 제안 기각 처리.
    -   모든 `AdminController`의 메서드는 `UserRole.ADMIN` 권한을 가진 사용자만 접근할 수 있도록 Spring Security로 보호합니다.

### 뷰 계층 (View/Template - Thymeleaf)
-   `templates/recipes/detail.html` (또는 유사 파일): "오류 신고/개선 제안" 버튼 추가.
-   `templates/corrections/form.html`: 제안 작성 폼.
-   `templates/admin/corrections/list.html`: 관리자용 제안 목록.
-   `templates/admin/corrections/detail.html`: 관리자용 제안 상세 및 처리 폼.

## 3. 개발 단계 (Development Steps)

1.  **[Backend] 도메인 모델링:**
    1.  `CorrectionType` 및 `CorrectionStatus` Enum 생성.
    2.  `RecipeCorrection` 엔티티 및 `RecipeCorrectionRepository` 인터페이스 생성.

2.  **[Backend] 서비스 로직 구현:**
    1.  `RecipeCorrectionService` 클래스 및 기본 CRUD 메서드 틀 구현.
    2.  `createCorrection` 메서드 구현.
    3.  `approveCorrection`, `rejectCorrection` 메서드에 상태 변경, 매너온도/포인트/알림 연동 로직 구현.

3.  **[Backend] 컨트롤러 구현:**
    1.  `RecipeCorrectionController` (`/recipes/{id}/correction`) 및 `AdminController` (`/admin/corrections`) 구현.
    2.  Spring Security 설정에 `/admin/**` 경로의 `ADMIN` 역할 접근 제한 추가.

4.  **[Frontend] 사용자 UI 구현:**
    1.  레시피 상세 페이지에 '오류 신고' 버튼 추가.
    2.  `corrections/form.html` 템플릿을 구현하여 제안 내용 입력 및 제출 기능 제공.

5.  **[Frontend] 관리자 UI 구현:**
    1.  관리자 메뉴에 '레시피 제안 관리' 링크 추가.
    2.  `admin/corrections/list.html` 템플릿에 `PENDING` 상태의 제안 목록 표시.
    3.  `admin/corrections/detail.html` 템플릿에 제안 상세 내용, 레시피 정보, '승인'/'기각' 버튼 및 사유 입력 필드 구현.

6.  **[ETC] 연동 작업:**
    1.  `UserService`에 매너온도/포인트 변경 로직 추가.
    2.  `NotificationService`에 제안 처리 결과 알림 로직 추가 (`NotificationType` enum 확장 필요).
    3.  `MannerTempHistory`에 변경 이력을 남기는 로직 연동.

## 4. 고려사항 (Considerations)

-   **수동 레시피 수정**: 사용자의 제안을 그대로 자동 반영할 경우 발생할 수 있는 데이터 오염을 방지하기 위해, 관리자가 제안 내용을 확인하고 직접 레시피를 수정하는 프로세스를 채택합니다.
-   **보상/패널티 정책**: 제안의 종류(단순 오타, 중요 정보 오류 등)에 따라 보상 수준을 차등화하는 정책을 수립합니다. 원작성자에 대한 패널티는 신중하게 적용해야 합니다.
-   **직관적인 UI/UX**: 사용자와 관리자 모두가 기능을 쉽고 편리하게 사용할 수 있도록 직관적인 UI를 설계합니다.

---

## 5. 악성 콘텐츠/사용자 신고 기능 (추가 요구사항)

### 5.1. 목표 (Goal)
-   사용자가 레시피, 게시글, 댓글, 공구, 특정 사용자 등 부적절한 콘텐츠나 비매너 행위를 신고할 수 있는 범용적인 신고 시스템을 구축합니다.
-   관리자는 신고된 내역을 검토하고, 콘텐츠 강제 삭제, 사용자 제재(매너온도 차감 등) 조치를 취할 수 있습니다.
-   관리자가 사용자의 요청에 따라 '별점 테러' 등으로 훼손된 매너온도를 복구해줄 수 있는 절차를 마련합니다.

### 5.2. 아키텍처 및 설계 (Architecture & Design)

#### 신규 엔티티 추가: `Report`
-   **`Report` 엔티티**를 `com.recipemate.domain.report.entity` 라는 신규 패키지에 생성합니다.
-   **주요 필드**:
    -   `reporter` (ManyToOne, `User`): 신고자
    -   `reportedEntityType` (Enum): 신고 대상의 타입 (`RECIPE`, `POST`, `COMMENT`, `GROUP_PURCHASE`, `USER`)
    -   `reportedEntityId` (Long): 신고 대상의 ID
    -   `reportType` (Enum): 신고 사유 (`INAPPROPRIATE_CONTENT`, `SPAM`, `ABUSE`, `ETC`)
    -   `content` (TEXT): 신고 상세 내용
    -   `status` (Enum): 처리 상태 (`PENDING`, `PROCESSED`, `DISMISSED`)
    -   `adminNotes` (TEXT): 관리자 처리 메모
    -   `resolver` (ManyToOne, `User`): 처리한 관리자
-   **신규 Repository**: `ReportRepository` 추가.
-   **신규 Enum**: `ReportedEntityType`, `ReportType`, `ReportStatus` 추가.

#### 서비스 계층 (Service Layer)
-   **`ReportService`** 클래스를 `com.recipemate.domain.report.service`에 생성합니다.
    -   `createReport(reporter, dto)`: 신고 접수 로직.
    -   `getPendingReports()`: 처리 대기 중인 신고 목록 조회.
    -   `processReport(admin, reportId, action, notes)`: 신고 처리 로직.
        -   **Action: 콘텐츠 삭제**: `PostService`, `CommentService` 등 각 도메인 서비스를 호출하여 해당 콘텐츠를 관리자 권한으로 삭제.
        -   **Action: 사용자 제재**: `UserService`를 호출하여 피신고자(reported user)의 매너온도를 차감. `MannerTempHistory`에 기록.
        -   **Action: 신고 기각**: `Report`의 상태를 `DISMISSED`로 변경.

#### 컨트롤러 및 뷰 (Controller & View)
-   **UI 변경**: 각 콘텐츠(게시글, 댓글 등) 영역에 '신고' 버튼 및 신고 모달(또는 페이지) 추가.
-   **`ReportController`**: `POST /reports` - 신고 제출을 처리.
-   **`AdminController`**:
    -   `GET /admin/reports`: 신고 목록 페이지.
    -   `GET /admin/reports/{id}`: 신고 상세 정보 및 처리 UI 제공.
        -   신고된 콘텐츠 내용을 표시.
        -   '콘텐츠 삭제', '사용자 제재', '신고 기각' 등 처리 버튼 제공.
    -   `POST /admin/reports/{id}/process`: 신고 처리 요청을 받아 `ReportService` 호출.

### 5.3. 매너온도 복구 프로세스
-   '매너온도 복구'는 별도의 기능으로 제공하기보다, 관리자가 사용자의 문의/요청을 받고 **관리자 페이지에서 수동으로 조정**하는 방식으로 구현합니다.
-   **`AdminController`**에 다음 기능을 추가합니다.
    -   `GET /admin/users/{userId}/manner-temp`: 특정 사용자의 매너온도 조정 페이지.
    -   `POST /admin/users/{userId}/manner-temp`: 매너온도 수치와 조정 사유를 입력받아 `UserService`를 통해 온도를 변경하고, `MannerTempHistory`에 `ADMIN_ADJUSTMENT`와 같은 사유로 기록을 남깁니다.
-   이는 시스템의 복잡성을 낮추고 관리자의 판단에 따라 유연하게 대처할 수 있게 합니다.

### 5.4. 기존 계획과의 통합
-   **`RecipeCorrection` (레시피 개선 제안)**: 긍정적이고 건설적인 제안을 위한 기능으로 유지합니다.
-   **`Report` (신고)**: 욕설, 스팸, 비방 등 부정적이고 유해한 활동을 제재하기 위한 기능으로 분리하여 구현합니다.
-   관리자 페이지에서는 '개선 제안 관리'와 '신고 관리' 메뉴를 별도로 제공하여 역할을 명확히 구분합니다.

