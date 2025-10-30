# 프로젝트 실행 및 개발 환경 가이드

프로젝트를 로컬 및 서버 환경에서 실행하고 관리하는 방법을 총정리한 가이드입니다. 
Docker, WSL, 로컬 IDE 환경을 모두 다룹니다.

---

## 2. 실행 방법 (택 1)

### 2.1. Docker Compose 사용 (권장)

이 방법은 **가장 간편하며, 프로덕션 환경과 거의 동일한 환경**을 구성합니다. WSL2 또는 Docker Desktop이 설치되어 있어야 합니다.

1.  **WSL2 터미널 또는 PowerShell/CMD 실행**
    프로젝트 루트 디렉토리로 이동합니다.

2.  **Docker Compose 실행**
    아래 명령어를 입력하면 `docker-compose.yml`에 정의된 모든 서비스(Spring Boot 앱, MySQL DB)가 실행됩니다.

    ```bash
    docker-compose up -d
    ```
    -   `-d` 옵션은 백그라운드에서 실행하라는 의미입니다.

3.  **애플리케이션 접속**
    -   웹 브라우저에서 `http://localhost:8080`으로 접속합니다.

4.  **실행 중인 서비스 확인 및 로그 보기**
    ```bash
    # 실행 중인 컨테이너 목록 확인
    docker-compose ps

    # 애플리케이션 로그 실시간 확인
    docker-compose logs -f app

    # MySQL 로그 실시간 확인
    docker-compose logs -f mysql
    ```

5.  **서비스 종료**
    ```bash
    # 컨테이너 중지 및 삭제
    docker-compose down

    # 볼륨(데이터)까지 모두 삭제 (주의!)
    docker-compose down -v
    ```

### 2.2. 로컬 IDE에서 직접 실행

이 방법은 순수하게 백엔드 로직만 빠르게 테스트하고 싶을 때 유용합니다. MySQL, Redis 등 외부 서비스 설정 없이 H2 인메모리 DB를 사용합니다.

1.  **IntelliJ 등 IDE에서 프로젝트 열기**

2.  **`BoardApplication.java` 실행**
    `src/main/java/com/board/BoardApplication.java` 파일을 찾아 `main` 메소드를 실행합니다.
    -   기본적으로 `dev` 프로파일이 활성화되어 H2 데이터베이스를 사용합니다.

3.  **애플리케이션 접속**
    -   웹 브라우저에서 `http://localhost:8080`으로 접속합니다.

> **참고**: 이 방식은 `application.yml`에 `dev` 프로파일이 활성화되어 있을 때를 가정합니다. 만약 `prod` 프로파일로 실행하려면 MySQL DB가 로컬에 실행 중이어야 합니다.

---

## 3. Redis 연동 가이드

`VIEW_COUNT_LOGIC.md`에서 제안된 것처럼 조회수 로직 개선 등을 위해 Redis를 도입할 경우, 아래와 같이 환경을 구성해야 합니다.

### 3.1. Docker Compose에 Redis 추가

프로덕션/개발 환경에서 Docker를 사용한다면 `docker-compose.yml` 파일에 Redis 서비스를 추가해야 합니다.

1.  **`docker-compose.yml` 파일 수정**
    `services` 섹션에 `redis`를 추가하고, `app` 서비스가 Redis에 의존하도록 수정합니다.

    ```yaml
    services:
      # ... 기존 mysql 서비스

      # Redis 서비스 추가
      redis:
        image: redis:7-alpine  # 경량화된 redis 이미지 사용
        container_name: board-redis
        restart: unless-stopped
        ports:
          - "6379:6379"
        networks:
          - board-network
        healthcheck:
          test: ["CMD", "redis-cli", "ping"]
          interval: 10s
          timeout: 5s
          retries: 5

      # app 서비스 수정
      app:
        build: .
        container_name: board-app
        restart: unless-stopped
        environment:
          SPRING_PROFILES_ACTIVE: prod
          DB_USERNAME: board_user
          DB_PASSWORD: board_password
          DB_URL: jdbc:mysql://mysql:3306/board_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul&characterEncoding=UTF-8
          # Redis 호스트 정보 추가
          SPRING_DATA_REDIS_HOST: redis
        ports:
          - "8080:8080"
        depends_on:
          mysql:
            condition: service_healthy
          # Redis 의존성 추가
          redis:
            condition: service_healthy
        volumes:
          - app_logs:/app/logs
        networks:
          - board-network
        # ... 기존 healthcheck
    
    # ... 기존 volumes, networks
    ```

2.  **`build.gradle` 의존성 추가**
    `VIEW_COUNT_LOGIC.md` 가이드에 따라 `spring-boot-starter-data-redis` 의존성을 추가해야 합니다.

    ```groovy
    // build.gradle
    dependencies {
        // ...
        implementation 'org.springframework.boot:spring-boot-starter-data-redis'
    }
    ```

### 3.2. 로컬 개발/테스트 환경에서 Redis

**결론부터 말하면, "네, 필요합니다."**

애플리케이션 코드(`HybridViewDuplicationChecker` 등)가 Redis에 직접 연결을 시도하기 때문에, 로컬에서 IDE로 앱을 실행할 때도 Redis 서버가 떠 있어야 합니다. 그렇지 않으면 앱 구동 시 연결 오류가 발생하며 시작되지 않습니다.

**가장 쉬운 해결책은 로컬에서도 Docker를 사용해 Redis만 실행하는 것입니다.**

1.  **로컬에 Redis 서버 띄우기 (Docker 사용)**
    WSL2 터미널이나 PowerShell에서 아래 명령어를 실행하세요.

    ```bash
    docker run --name local-redis -d -p 6379:6379 redis
    ```
    -   이렇게 하면 `localhost:6379`에서 Redis 서버가 실행됩니다.
    -   `application.yml`의 `dev` 프로파일에 `spring.data.redis.host=localhost` 설정을 추가하면 IDE에서 앱을 실행할 때 이 Redis를 사용하게 됩니다.

2.  **테스트 코드 환경**
    -   통합 테스트(`@SpringBootTest`) 실행 시에도 실제 Redis가 필요합니다. 위에서 띄운 로컬 Redis를 사용하거나, Testcontainers 라이브러리를 사용해 테스트가 실행될 때만 Redis 컨테이너를 자동으로 띄우고 종료하게 만들 수 있습니다.

---

## 4. 필수 명령어 모음

### 4.1. WSL 주요 명령어

Windows에서 Linux 환경을 사용하기 위한 WSL 관련 필수 명령어입니다.

```bash
# 설치된 Linux 배포판 목록 및 상태 확인
wsl -l -v

# WSL 및 모든 배포판 즉시 종료
wsl --shutdown

# 특정 배포판 실행
wsl -d <DistributionName> # 예: wsl -d Ubuntu-22.04

# Windows 파일 탐색기에서 현재 WSL 디렉토리 열기
explorer.exe .
```

### 4.2. Docker 주요 명령어

`docker-compose` 외에 알아두면 유용한 Docker 명령어입니다.

```bash
# 실행 중인 모든 컨테이너 확인
docker ps

# 모든 컨테이너 (중지된 것 포함) 확인
docker ps -a

# 다운로드된 이미지 목록 확인
docker images

# 컨테이너 중지
docker stop <container_name_or_id>

# 컨테이너 삭제
docker rm <container_name_or_id>

# 이미지 삭제
docker rmi <image_name_or_id>

# 실행 중인 컨테이너의 셸에 접속 (디버깅용)
docker exec -it <container_name_or_id> /bin/sh  # Alpine 이미지의 경우
docker exec -it <container_name_or_id> /bin/bash # 일반 Linux 이미지의 경우
```

---

## 5. 개발 워크플로우 및 설정 가이드

### 5.1. `application.yml` 프로필 설정 분석

우리 프로젝트는 Spring Profile을 사용해 실행 환경별로 다른 설정을 적용합니다. 각 파일의 역할은 다음과 같습니다.

-   **`src/main/resources/application.yml`**: 공통 설정 및 `dev`, `prod` 프로필별 설정을 포함합니다.
-   **`src/test/resources/application-test.yml`**: 테스트 실행 시 적용되는 전용 설정입니다. (`test` 프로필)

| 프로필 | 사용 환경 | 주요 특징 | 데이터베이스 | `ddl-auto` | 로깅 레벨 |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `dev` | 로컬 IDE 실행 (기본값) | 빠른 개발 및 디버깅 | H2 (In-Memory) | `create-drop` | `DEBUG` |
| `prod` | Docker Compose 실행 | 프로덕션 환경과 동일 | MySQL | `update` | `INFO` |
| `test` | `./gradlew test` 실행 | 테스트 격리 | H2 (In-Memory, UUID) | `create-drop` | `WARN` |

#### Redis 설정 추가 시 고려사항

Redis를 도입하면 `application.yml`에 프로필별로 Redis 호스트를 지정해야 합니다.

```yaml
# src/main/resources/application.yml

--- 
# 개발 환경
spring:
  config:
    activate:
      on-profile: dev
  data:
    redis:
      host: localhost # 로컬에서 `docker run`으로 띄운 Redis
      port: 6379

--- 
# 프로덕션 환경
spring:
  config:
    activate:
      on-profile: prod
  data:
    redis:
      host: redis # docker-compose.yml에 정의된 서비스 이름
      port: 6379
```

`test` 환경에서는 Testcontainers나 Embedded Redis를 사용하는 것이 일반적이므로, 보통 yml 파일보다는 테스트 코드 내에서 설정을 제어합니다.

### 5.2. 개발 워크플로우 추천: IDE vs Docker

"IDE로 개발할까, Docker로 할까?"는 많은 개발자의 고민입니다. 결론부터 말하면 **"두 가지를 모두 사용하는 하이브리드 방식"** 이 가장 이상적입니다.

| 구분 | 로컬 IDE 실행 (`dev` 프로필) | Docker Compose 실행 (`prod` 프로필) |
| :--- | :--- | :--- |
| **장점** | ◉ **빠른 속도**: 코드 수정 후 재시작이 매우 빠름<br>◉ **강력한 디버깅**: 중단점(breakpoint) 사용이 자유로움<br>◉ **단순함**: 인프라 걱정 없이 순수 비즈니스 로직에 집중 | ◉ **환경 일치성**: 실제 운영 환경과 거의 100% 동일<br>◉ **전체 시스템 테스트**: DB, Redis 등 모든 서비스가 함께 동작<br>◉ **배포 안정성**: "내 PC에선 됐는데..." 문제 원천 차단 |
| **단점** | ◉ **환경 불일치**: H2와 MySQL의 미묘한 차이로 버그 발생 가능<br>◉ **의존성 부재**: Redis 등 외부 서비스가 필요한 기능 테스트 불가 | ◉ **느린 속도**: 이미지 빌드 및 컨테이너 실행에 시간 소요<br>◉ **리소스 사용량**: PC의 CPU, 메모리 자원을 많이 사용<br>◉ **복잡한 디버깅**: 원격 디버깅 설정이 번거로움 |

#### 추천! 하이브리드 워크플로우

1.  **1단계 (코딩 & 단위 테스트): 로컬 IDE 사용**
    -   새로운 기능 구현, 리팩토링, 버그 수정 등 대부분의 코딩 작업을 IDE에서 진행합니다.
    -   `dev` 프로필을 사용하며, H2 DB를 기반으로 빠르게 개발합니다.
    -   JUnit을 이용한 단위/통합 테스트를 IDE에서 직접 실행하여 코드 품질을 확인합니다.
    -   **(필요시)** Redis 등 외부 서비스가 필요하다면 `docker run redis` 명령으로 개별 컨테이너만 띄워놓고 개발합니다.

2.  **2단계 (통합 & 최종 검증): Docker Compose 사용**
    -   하나의 기능 개발이 완료되었거나, Pull Request를 보내기 전에 `docker-compose up`으로 전체 시스템을 실행합니다.
    -   `prod` 프로필 환경(MySQL, Redis 등)에서 기능이 의도대로 완벽하게 동작하는지 최종 검증합니다.
    -   이 단계를 통해 다른 서비스와의 연동 문제나 환경 차이로 인한 버그를 사전에 발견할 수 있습니다.

**결론: 빠른 개발은 IDE에서, 안정적인 통합 검증은 Docker에서 진행하는 것이 가장 효율적이고 안전한 방법입니다.**

---

## 6. Docker & 배포 핵심 요약

> 이 섹션은 "Docker로 띄우면 외부에서 접속 가능한가?", "로컬 개발 vs 간단 운영" 같은 가장 헷갈리는 부분을 초보자 기준으로 정리한 **실전 생존 요약**입니다. (자세한 전체 절차는 `docs/references/DEPLOYMENT.md` 참고)

### 6.1 Docker 컨테이너 = 작게 격리된 프로세스
- 내 컴퓨터에서 Docker로 실행했다고 해서 자동으로 전 세계에 공개되는 것 아님.
- 기본 개념: (1) 컨테이너 내부 포트 → (2) 호스트(내 PC) 포트 매핑 → (3) 네트워크/방화벽 허용 → (4) 다른 기기에서 접속.
- 예: `-p 8080:8080` 옵션이 있어야 호스트 8080 포트가 열리고, 같은 LAN 에 있는 다른 PC가 `http://내_로컬_IP:8080` 으로 접근 가능.

### 6.2 외부(다른 PC, 노트북)에서 접속 가능 조건 체크리스트
| 체크 | 항목 | 설명 |
| ---- | ---- | ---- |
| [ ] | 포트 매핑 | docker-compose.yml 에 `8080:8080` 이 있다 |
| [ ] | 실행 상태 | `docker-compose ps` 로 app 컨테이너 Up 확인 |
| [ ] | IP 확인 | `ipconfig` (Windows) 또는 `ip addr` 로 192.168.x.x 확인 |
| [ ] | 방화벽 | 로컬 방화벽/보안툴이 8080 허용 |
| [ ] | 동일 네트워크 | 접속 시도하는 기기가 같은 Wi-Fi / LAN |
| (선택) | 공인 공개 | 라우터 포트포워딩 또는 클라우드 서버 사용 + 도메인/SSL 구성 |

### 6.3 언제 어떤 방식으로 실행할까?
| 목적 | 가장 빠른 선택 | 이유 |
| ---- | -------------- | ---- |
| 단순 코드 수정/디버깅 | IDE + `dev` 프로필 | H2, 빠른 재시작 |
| Redis/MySQL 영향 검증 | Docker Compose | 실제 환경 모사 |
| 팀원/지인에게 데모 | 집/사무실 PC Docker 실행 후 IP 공유 (LAN 한정) | 빠른 시연 |
| 외부(인터넷) 공개 | 클라우드 VM + Docker Compose | 고정 IP/방화벽 제어 용이 |
| 장기 안정 운영 | 소규모: Docker Compose / 성장: K8s 고려 | 규모에 따라 단계적 확장 |

### 6.4 최소 배포 절차 (Ubuntu 예시 – 진짜 핵심만)
```bash
# 1. 패키지 업데이트 & Docker 설치
sudo apt update && sudo apt upgrade -y
sudo apt install -y docker.io docker-compose git

# 2. 프로젝트 가져오기
git clone <repo-url>
cd 7ddev_board

# 3. (선택) 관리자 비밀번호 환경변수로 재정의
export ADMIN_PASSWORD='Strong!Passw0rd'

# 4. 실행
docker-compose up -d

# 5. 상태/로그
docker-compose ps
docker-compose logs -f app
```
접속: `http://서버_IP:8080`

### 6.5 꼭 바로 변경/점검해야 하는 것 (실수 많이 함)
1. 기본 관리자 비밀번호: 로그인 후 즉시 변경.
2. DB 포트 외부 노출: 운영 환경에서는 `ports: "3306:3306"` 제거(동일 호스트 app 만 접근하도록).
3. 환경 변수/비밀: `docker-compose.yml` 에 하드코딩 지양 → `.env` 파일 또는 서버 환경 변수 사용.
4. 로그 로테이션: 현재 `logs/` 로 쌓임 → 디스크 용량 모니터링.
5. 시간대(TZ): 컨테이너/DB 모두 `Asia/Seoul` 유지 확인.

### 6.6 간단 보안 체크리스트 (최소 필수)
| 항목 | 해야 하는 이유 | 빠른 점검 |
| ---- | -------------- | --------- |
| 관리자 비번 변경 | 기본 정보 노출 위험 | 로그인 후 프로필 수정 |
| DB 외부 접근 차단 | 무차별 대입 방지 | `netstat -tulpn | grep 3306` |
| 방화벽 적용 | 불필요 포트 차단 | `ufw status` |
| SSL 적용(인터넷 공개 시) | 평문 패킷 노출 방지 | certbot 인증 후 443 사용 |
| 애플리케이션 헬스체크 | 다운되면 알 수 없음 | `/actuator/health` 200 OK |

### 6.7 운영 중 자주 쓰는 명령 핵심 6개
```bash
# 상태
docker-compose ps
# 앱 로그
docker-compose logs -f app
# 재시작 (코드/설정 반영)
docker-compose restart app
# 전체 중지 (데이터 유지)
docker-compose down
# 백업 (MySQL)
docker-compose exec mysql mysqldump -u board_user -pboard_password board_db > backup.sql
# 헬스 직접 확인
curl -f http://localhost:8080/actuator/health
```

### 6.8 백업 & 복원 최소 패턴
```bash
# 백업 (날짜 포함)
docker-compose exec mysql mysqldump -u board_user -pboard_password board_db > backup_$(date +%Y%m%d).sql

# 복원 (주의: 대상 DB 비워져 있다고 가정)
mysql -u board_user -pboard_password -h 127.0.0.1 board_db < backup_20250101.sql
```
(MySQL 포트를 외부로 열지 않았다면 `mysql` 클라이언트 명령은 컨테이너 내부에서 실행: `docker-compose exec mysql bash` 후 수행)

### 6.9 메모리/성능 최소 설정
- 기본 JVM 옵션 없이 실행하면 메모리를 과도하게 잡을 수 있음.
- 소규모 서버(2GB RAM)라면 Dockerfile/Compose 실행 옵션에 아래 예시 고려:
```bash
JAVA_OPTS="-Xms512m -Xmx512m"
```
Compose 예:
```yaml
environment:
  JAVA_TOOL_OPTIONS: "-Xms512m -Xmx512m"
```

### 6.10 장애 발생 시 가장 먼저 할 5단계
1. 컨테이너 상태: `docker-compose ps` (Exited 인지)
2. 앱 로그: `docker-compose logs --tail=200 app`
3. DB 연결: `docker-compose exec mysql mysql -u board_user -pboard_password -e "SELECT 1;"`
4. 포트 점유: `sudo lsof -i:8080` (다른 프로세스 충돌 여부)
5. 메모리 부족: `free -h` (Swap/OutOfMemory 여부)

### 6.11 "로컬 Docker" vs "실제 운영" 가장 큰 차이 3가지
| 구분 | 로컬 Docker | 운영 서버 Docker |
| ---- | ----------- | --------------- |
| 접근 범위 | 내 PC + 같은 LAN 기기 | 인터넷 사용자(방화벽/포트 개방 전제) |
| 신뢰 가정 | 혼자 사용 | 외부 공격 가정 (보안 강화 필요) |
| 데이터 중요도 | 깨져도 재생성 | 백업·복구 전략 필수 |

### 6.12 언제 Kubernetes 생각할까? (지금은 안 써도 되는 기준)
> 아래 조건 중 2개 이상 해당하면 점진적으로 검토.
- 트래픽이 수시로 급증하고 자동 수평 확장이 필요
- 여러 인스턴스를 무중단 롤링 업데이트해야 함
- 서비스/작업(Job, Batch) 종류가 늘어나 관리 포인트 증가
- 팀 인원이 늘어나 인프라 표준화가 과제

현재 단일 게시판 학습/소규모 수준 → Docker Compose 로 충분.

### 6.13 지금 당장 기억해야 할 1줄 요약
> 개발은 IDE(H2)로 빠르게, 최종 검증/공유는 Docker Compose(MySQL), 외부 공개 시에는 포트/비밀번호/백업/로그 만 제대로 챙겨라.

---

