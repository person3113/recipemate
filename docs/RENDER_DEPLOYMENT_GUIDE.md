### **사전 준비 사항**

1.  **Render 계정**: Render.com에 가입되어 있어야 합니다.
2.  **GitHub 저장소**: 코드가 GitHub 저장소에 푸시되어 있어야 합니다. Render는 Git 저장소에서 직접 코드를 가져와 빌드합니다.
3.  **`gradlew` 실행 권한 확인**: `Dockerfile`에서 `gradlew`를 사용하므로, Git에 실행 권한이 부여되어 있는지 확인하고, 아닐 경우 아래 명령을 실행해주세요.
    ```shell
    git update-index --chmod=+x gradlew
    git commit -m "feat: Make gradlew executable"
    git push
    ```
4.  **`schema.sql` 확인**: `schema.sql` 파일이 현재 애플리케이션의 JPA 엔티티와 일치하는 최신 상태인지 확인해주세요. 불일치 시 배포 과정에서 오류가 발생할 수 있습니다.

---

## **Render 배포 가이드 (Step-by-Step)**

### **1단계: PostgreSQL 데이터베이스 생성**

먼저 애플리케이션이 사용할 데이터베이스를 Render에 생성합니다.

1.  Render 대시보드에서 **[New]** > **[PostgreSQL]**을 선택합니다.
2.  **Name**: 서비스 이름을 입력합니다 (예: `recipemate-postgres`).
3.  **Region**: 애플리케이션을 배포할 지역을 선택합니다 (예: `Singapore`). **이후 모든 서비스는 같은 지역에 생성해야 합니다.**
4.  **Instance Type**: `Free` 플랜을 선택합니다.
5.  **[Create Database]** 버튼을 클릭합니다.
6.  생성이 완료되면, 데이터베이스 정보 페이지에서 아래 **Connection** 정보를 복사해두세요. **3단계에서 사용됩니다.**
    *   `Username`
    *   `Password`
    *   `Host`
    *   `Port`
    *   `Database`
    *   `Internal Connection URL` (내부 서비스 간 통신에 사용)

#### **DB 스키마 적용**

Render는 데이터베이스를 처음 생성할 때 자동으로 `schema.sql`을 실행해주지 않습니다. 따라서 로컬 PC의 DB 클라이언트(DBeaver, DataGrip, `psql` 등)를 사용하여 수동으로 스키마를 적용해야 합니다.

1.  Render의 PostgreSQL 정보 페이지에서 **External Connection URL**을 복사합니다.
2.  DB 클라이언트를 사용해 해당 정보로 데이터베이스에 연결합니다.
3.  프로젝트의 `schema.sql` 파일 내용을 복사하여 DB 클라이언트에서 실행합니다.

### **2단계: Redis 생성**

다음으로 캐시와 세션 관리에 사용할 Redis를 생성합니다.

1.  Render 대시보드에서 **[New]** > **[Redis]**를 선택합니다.
2.  **Name**: 서비스 이름을 입력합니다 (예: `recipemate-redis`).
3.  **Region**: PostgreSQL과 **동일한 지역**을 선택합니다.
4.  **Instance Type**: `Free` 플랜을 선택합니다.
5.  **[Create Redis]** 버튼을 클릭합니다.
6.  생성이 완료되면, Redis 정보 페이지에서 아래 **Connection** 정보를 복사해두세요. **3단계에서 사용됩니다.**
    *   `Internal Host`
    *   `Port`
    *   `Password`

### **3단계: 웹 서비스(Spring Boot 앱) 배포**

이제 핵심 애플리케이션을 배포합니다.

1.  Render 대시보드에서 **[New]** > **[Web Service]**를 선택합니다.
2.  **Source Code**: 배포할 GitHub 저장소(`recipemate`)를 연결합니다.
3.  **Name**: `recipemate-api` (또는 원하는 이름)
4.  **Language**: `Docker`를 선택합니다.
5.  **Region**: PostgreSQL, Redis와 **동일한 지역**을 선택합니다.
6.  **Branch**: `main` (또는 배포할 브랜치)
7.  **Instance Type**: `Free`를 선택합니다. (참고: Free 플랜은 일정 시간 미사용 시 휴면 상태가 됩니다.)

#### **환경 변수 설정**

가장 중요한 단계입니다. `.env` 파일과 각 서비스의 연결 정보를 여기에 입력합니다. **[Add Environment Variable]**을 클릭하여 아래 변수들을 추가합니다.

| Name | Value | 설명 |
| :--- | :--- | :--- |
| `SPRING_PROFILES_ACTIVE` | `prod` | 운영 환경 프로필을 활성화합니다. |
| `DB_USERNAME` | `[1단계 DB 사용자명]` | 1단계에서 복사한 PostgreSQL 사용자명 |
| `DB_PASSWORD` | `[1단계 DB 비밀번호]` | 1단계에서 복사한 PostgreSQL 비밀번호 |
| `SPRING_DATASOURCE_URL`| `jdbc:postgresql://[1단계 DB 호스트]:[1단계 DB 포트]/[1단계 DB 이름]` | 1단계 PostgreSQL의 **Internal Connection URL**을 참고하여 만듭니다. |
| `REDIS_HOST` | `[2단계 Redis Internal Host]` | 2단계에서 복사한 Redis의 **Internal Host** |
| `REDIS_PORT` | `[2단계 Redis Port]` | 2단계에서 복사한 Redis 포트 |
| `REDIS_PASSWORD` | `[2단계 Redis 비밀번호]` | 2단계에서 복사한 Redis 비밀번호 |
| `FOOD_SAFETY_API_KEY` | `af518f75192542be86d1` | `.env` 파일의 API 키 |
| `CLOUDINARY_URL` | `cloudinary://454229...` | `.env` 파일의 Cloudinary URL |
| `KAKAO_JAVASCRIPT_KEY`| `66c5fa41222d3e3fe6582b...` | `.env` 파일의 Kakao JS 키 |
| `JAVA_OPTS` | `-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0` | `Dockerfile`에 정의된 것과 같이 컨테이너 환경에 최적화된 JVM 옵션을 설정합니다. |

**`Advanced` 섹션을 확장하여 아래 내용을 확인합니다.**

*   **Health Check Path**: `/actuator/health` (Spring Boot Actuator의 기본 상태 체크 경로)
*   **Dockerfile Path**: `./Dockerfile` (기본값이므로 수정할 필요 없음)
*   **Docker Build Context Directory**: `.` (기본값이므로 수정할 필요 없음)

### **4단계: 배포 및 확인**

1.  모든 설정이 완료되었으면, 페이지 하단의 **[Create Web Service]** 버튼을 클릭하여 배포를 시작합니다.
2.  **[Events]** 또는 **[Logs]** 탭에서 빌드 및 배포 과정을 실시간으로 확인할 수 있습니다. 최초 빌드는 Gradle 의존성을 다운로드하므로 시간이 다소 걸릴 수 있습니다.
3.  배포가 성공적으로 완료되면 ("Your service is live") 상단에 제공되는 `https://<서비스이름>.onrender.com` URL로 접속하여 애플리케이션이 정상적으로 동작하는지 확인합니다.

---

### **주요 참고사항 및 문제 해결**

*   **`docker-compose.yml`과의 차이**: `docker-compose.yml`에 정의된 `nginx` 서비스는 Render에서 필요하지 않습니다. Render가 리버스 프록시 및 SSL/TLS 인증서 발급을 자동으로 처리해줍니다.
*   **배포 실패 시**:
    *   **로그 확인**: 가장 먼저 **[Logs]** 탭에서 오류 메시지를 확인하세요.
    *   **환경 변수**: 환경 변수가 정확히 입력되었는지, 특히 DB/Redis 연결 정보에 오타가 없는지 다시 확인하세요.
    *   **DB 스키마 불일치**: `ddl-auto: validate` 설정 때문에 `schema.sql`과 엔티티가 다르면 앱 실행에 실패합니다. `schema.sql`을 최신화하거나, 임시방편으로 `application.yml`의 `ddl-auto` 값을 `update`로 변경 후 재배포해볼 수 있습니다.
*   **Free 플랜 제약**: Free 플랜 서비스들은 15분간 트래픽이 없으면 휴면 상태(sleep)에 들어갑니다. 다시 요청이 오면 깨어나는 데 수십 초가 소요될 수 있습니다.