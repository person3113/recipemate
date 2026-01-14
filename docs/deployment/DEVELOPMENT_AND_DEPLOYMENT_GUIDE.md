# 프로젝트 실행, Docker 및 배포 통합 가이드

이 문서는 RecipeMate 프로젝트를 로컬 및 서버 환경에서 실행, 관리, 배포하는 방법을 다룹니다. 개발 워크플로우부터 Docker 상세 명령어, 운영 배포 시 고려사항 및 트러블슈팅까지 포함합니다.

---

## 1. 개발 환경 및 워크플로우 (Development Workflow)

"IDE로 개발할까, Docker로 할까?"는 많은 개발자의 고민입니다. **"두 가지를 모두 사용하는 하이브리드 방식"**이 가장 이상적입니다.

### 1.1 Development vs Production 모드

- **Development Mode (로컬 IDE)**
  - Spring Boot `dev` 프로필 사용 (H2 Database)
  - 실행: `./gradlew bootRun`
  - **장점**: 빠른 재시작, 강력한 디버깅, 단순함
  - **단점**: 운영 환경(MySQL/Redis)과의 미세한 차이 발생 가능

- **Production Mode (Docker)**
  - `prod` 프로필 사용 (PostgreSQL, Redis, Nginx, 최적화된 JVM)
  - 실행: `docker-compose up -d`
  - **장점**: 운영 환경과 거의 100% 동일, 전체 시스템 통합 테스트
  - **단점**: 빌드 속도 느림, 리소스 사용량 높음

### 1.2 추천 워크플로우

1.  **코딩 & 단위 테스트 (로컬 IDE)**
    -   새로운 기능 구현, 리팩토링, 버그 수정은 IDE에서 `dev` 프로필(H2)로 빠르게 진행합니다.
    -   필요 시 `docker run redis` 등으로 개별 서비스만 띄워놓고 개발합니다.

2.  **통합 & 최종 검증 (Docker Compose)**
    -   기능 개발 완료 후, `docker-compose up`으로 전체 시스템을 실행합니다.
    -   `prod` 환경에서 DB, Redis, Nginx 연동이 완벽한지 최종 검증합니다.

3.  **배포 (AWS EC2 등)**
    -   검증된 `docker-compose.yml`을 사용하여 서버에 배포합니다.

---

## 2. Docker 환경 설정 및 기본 운영 (Docker Operations)

### 시작(ssh 접속)
```bash
# SSH 접속(Git Bash 또는 WSL에서)
### ubuntu
# ssh -i "path/to/key.pem" ubuntu@<Elastic-IP>

### Amazon Linux
# ssh -i "path/to/key.pem" ec2-user@<Elastic-IP>
ssh -i "C:\Users\UESR\.ssh\recipemate-ec2-key.pem" ec2-user@13.125.48.36
```

### 2.1 환경 설정 (Environment Setup)
1. `.env` 파일 생성 및 환경 변수 설정
2. 애플리케이션 접속:
   - Nginx 경유: `http://localhost`
   - 직접 접속: `http://localhost:8080`

### 2.2 WSL (Windows Subsystem for Linux) 관리
```bash
# 설치된 Linux 배포판 목록 및 상태 확인
wsl -l -v

# WSL 및 모든 배포판 즉시 종료 (메모리 확보 등)
wsl --shutdown

# 특정 배포판 실행
wsl -d Ubuntu-22.04
```

### 2.3 Docker 빌드 및 실행 명령어

**서비스 실행 및 빌드**
```bash
# 모든 서비스 실행 (이미지 없으면 다운로드/빌드)
docker-compose up -d

# 코드 변경 후 재빌드 및 실행 
docker-compose up -d --build app

# 전체 재빌드 및 실행 (초기 설정 시)
docker-compose up -d --build

# 빌드 없음. 단순히 서비스(기존 컨테이너) 재시작만. 변경사항 미반영
docker-compose restart app
```
- `docker-compose up -d` 예시: RecipeMate Spring Boot Application (port 8080), PostgreSQL 16 (port 5432), Redis 7 (port 6379), Nginx Reverse Proxy (port 80)
- `docker-compose up -d --build app`: 새 이미지 빌드(적용). 기존 컨테이너 중지→제거→신규 생성. 코드/설정 변경 완전 반영
- `docker-compose up -d --build`: 첫 실행 시 필요한 이미지 다운로드 및 모든 컨테이너 시작
  - 예시) PostgreSQL 16 이미지 다운로드 (약 100MB), Redis 7 이미지 다운로드 (약 40MB), Nginx 이미지 다운로드 (약 40MB), spring Boot 애플리케이션 빌드 (Gradle 의존성 다운로드 + 컴파일, 3-5분)

**서비스 상태 확인**
```bash
# 현재 프로젝트의 컨테이너 상태 확인
docker-compose ps

# 실행 중인 모든 도커 컨테이너 확인
docker ps

# 모든 컨테이너 (중지된 것 포함) 확인
docker ps -a 

# 로그 확인 (실시간)
docker-compose logs -f app       # 앱 로그
docker-compose logs -f postgres  # DB 로그
docker-compose logs -f nginx     # Nginx 로그
```

- Check if all services are running: `docker-compose ps`
  - 현재 디렉토리의 docker-compose.yml 파일에 정의된 프로젝트의 컨테이너만 표시하며, 서비스 이름(예: web, db)을 중심으로 보여줌
- `docker ps`
    - 도커 엔진 전체에서 실행 중인 모든 컨테이너를 나열하며, Compose 프로젝트와 무관한 컨테이너도 포함

**서비스 중지 및 정리**
```bash
# 컨테이너 중지 (데이터/상태 보존)
docker-compose stop

# 컨테이너 중지 및 삭제 (데이터 볼륨은 유지)
docker-compose down

# 컨테이너, 볼륨까지 모두 삭제 (데이터 삭제됨 - 주의!)
docker-compose down -v

# 이미지까지 모두 삭제 (완전 초기화)
docker-compose down --rmi all -v
```

- `docker-compose stop`
  - 컨테이너 중지만. 네트워크 유지. 볼륨 유지. 데이터 보존
  - 임시 중지하고 나중에 docker-compose start로 재개할 때
- `docker-compose down`
  - 컨테이너 중지+삭제. 네트워크 삭제. 볼륨 유지. 데이터 유지 x (컨테이너 초기화)
  - 완전 정리하거나 다음 실행 시 깨끗한 상태로 시작하려면
- 컨테이너를 임시 중지할 때는 stop을, 정리할 때는 rm 후 필요시 rmi를 순차 실행
  - 컨테이너 중지: `docker stop <container_name_or_id>`
  - 실행 중인 컨테이너를 정상적으로 중지시켜 데이터 손실 없이 종료하며, 컨테이너 자체는 남아 재시작 가능
  - 컨테이너 삭제: `docker rm <container_name_or_id>`
  - 중지된 컨테이너를 완전히 삭제해 디스크 공간을 확보하고, 실행 중 컨테이너는 -f 옵션으로 강제 삭제 가능
  - 이미지 삭제: `docker rmi <image_name_or_id>`
  - 컨테이너에서 사용되지 않는 Docker 이미지를 제거하여 저장 공간을 정리
---

## 3. 서비스 관리 및 데이터 접근 (Service Management)

### 3.1 Database (PostgreSQL)
데이터는 `postgres_data` 볼륨에 영구 저장됩니다.

```bash
### docker psql 접속
# PostgreSQL 쉘 접속 (컨테이너 안에서 바로 psql 실행)
docker exec -it recipemate-postgres psql -U recipemate -d recipemate

# `5432:5432`로 포트 열어놨으니, 로컬에 psql 설치돼 있으면 도커 안 들어가고도 접속 가능
# 암호 입력하라고 나오면 `.env`에 넣은 `${DB_PASSWORD}` 입력.
psql -h localhost -p 5432 -U recipemate -d recipemate

### aws rds psql 접속
psql -h recipemate.c3qcycyic4eb.ap-northeast-2.rds.amazonaws.com \
     -U postgres \
     -d recipemate

###
# schema.sql 실행 (테이블 생성)
docker exec -i recipemate-postgres psql -U recipemate -d recipemate < schema.sql

# DB 백업 (Backup)
docker exec recipemate-postgres pg_dump -U recipemate recipemate > backup_$(date +%Y%m%d).sql

# DB 복구 (Restore)
docker exec -i recipemate-postgres psql -U recipemate -d recipemate < backup_20250105.sql
```

테이블 / 데이터 확인
```sql
-- `psql` 셸 안에서(docker exec -it recipemate-postgres psql -U recipemate -d recipemate):

-- 현재 DB의 테이블 목록
\dt

-- 특정 테이블 구조
\d member   -- 예시
 
 -- 데이터 몇 줄만 확인
SELECT * FROM member LIMIT 10;

-- psql 종료
\q
```

**초기 데이터 삽입**
```bash
# 관리자 계정 (schema.sql에 포함됨)
# Username: admin / Password: admin123 / Email: admin@recipemate.com / Role: ADMIN

# 테스트 데이터 SQL 파일 실행
docker exec -i recipemate-postgres psql -U recipemate -d recipemate < test-data.sql
```

### 3.2 Redis (Cache)
캐시는 `redis_data` 볼륨에 저장됩니다.

```bash
# Redis CLI 접속
docker exec -it recipemate-redis redis-cli -a redis2024!secure

# Check Redis connection
docker exec recipemate-redis redis-cli -a redis2024!secure ping

# 모든 캐시 키 조회
docker exec recipemate-redis redis-cli -a recipemate2024!secure KEYS "*"

# 전체 캐시 삭제 (초기화)
docker exec recipemate-redis redis-cli -a recipemate2024!secure FLUSHALL

# 실시간 모니터링
docker exec -it recipemate-redis redis-cli -a redis2024!secure monitor

# 캐시 통계
docker exec recipemate-redis redis-cli -a recipemate2024!secure INFO stats
```

---

## 4. 실전 배포 가이드 (Deployment Guide)

### 4.1 배포 핵심 개념
- **접근 범위**: 로컬 Docker는 내 PC에서만, 운영 서버 Docker는 전 세계에서 접근 가능합니다.
- **포트 매핑**: `-p 8080:8080` 옵션이 있어야 외부에서 접근 가능합니다.
- **보안**: 운영 환경에서는 방화벽, 비밀번호 변경, 백업 전략이 필수입니다.

### 4.2 외부 접속 체크리스트
| 항목 | 확인 내용 |
| ---- | ---- |
| **포트 매핑** | `docker-compose.yml`에 `8080:8080` (혹은 `80:80`) 설정 확인 |
| **실행 상태** | `docker-compose ps`로 State가 Up인지 확인 |
| **방화벽** | 서버(또는 PC)의 방화벽에서 해당 포트 허용 확인 |
| **네트워크** | 클라이언트가 서버와 통신 가능한 네트워크(같은 LAN 또는 공인 IP)인지 확인 |

### 4.3 최소 배포 절차 (Ubuntu 예시)
```bash
# 1. 패키지 업데이트 & Docker 설치
sudo apt update && sudo apt upgrade -y
sudo apt install -y docker.io docker-compose git

# 2. 프로젝트 클론
git clone <repo-url>
cd recipemate-api

# 3. 환경 설정 (비밀번호 등 변경)
# .env 파일 생성 또는 수정

# 4. 실행
docker-compose up -d

# 5. 로그 확인
docker-compose logs -f app
```

### 4.4 배포 시 필수 점검 (보안)
1.  **관리자 비밀번호 변경**: 기본 설정된 비밀번호는 즉시 변경하세요.
2.  **DB 포트 차단**: 운영 환경에서는 `ports: "5432:5432"`를 제거하거나, 방화벽으로 외부 접근을 막고 `app` 컨테이너만 내부 네트워크로 접근하게 하세요.
3.  **SSL 적용**: 인터넷 공개 시 Let's Encrypt 등을 통해 HTTPS를 적용해야 합니다.
4.  **헬스체크**: `/actuator/health` 엔드포인트를 모니터링에 등록하세요.

---

## 5. 성능 최적화 (Performance Tuning)

### 5.1 JVM 옵션 (Memory)
`docker-compose.yml`에서 `JAVA_OPTS`를 조정하여 메모리 사용량을 제어합니다.
```yaml
environment:
  JAVA_OPTS: >-
    -XX:+UseContainerSupport
    -XX:MaxRAMPercentage=75.0
    -XX:+UseG1GC
```
*소규모 서버(2GB RAM 이하)인 경우 `-Xms512m -Xmx512m` 등으로 명시적 제한을 고려하세요.*
*기본 JVM 옵션 없이 실행하면 메모리를 과도하게 잡을 수 있음.*

### 5.2 PostgreSQL 튜닝
```yaml
command: >
  -c shared_buffers=256MB
  -c max_connections=200
  -c effective_cache_size=1GB
```

---

## 7. 모니터링 (Monitoring)

### Health Checks
- Application: http://localhost:8080/actuator/health
- Nginx: http://localhost/health

### Resource Usage
```bash
# 실시간 리소스 사용량
docker stats

# 특정 컨테이너만
docker stats recipemate-app
```

## 6. 트러블슈팅 (Troubleshooting)

장애 발생 시 다음 5단계를 순서대로 확인하세요.

1.  **컨테이너 상태 확인**: `docker-compose ps` (State가 Exit인지 Up인지)
2.  **로그 확인**: `docker-compose logs --tail=200 app` (에러 메시지 확인)
3.  **DB 연결 확인**: `docker exec app ping postgres` 또는 DB 컨테이너 로그 확인
4.  **포트 점유 확인**: `sudo lsof -i:8080` (다른 프로세스와 충돌 여부)
5.  **리소스 확인**: `free -h` (메모리 부족/OOM 여부)

### 자주 발생하는 문제
-   **App이 시작되지 않음**: DB가 아직 준비되지 않았을 수 있습니다. (Health check 대기)
-   **DB 접속 오류**: `.env`의 DB 정보와 `docker-compose.yml` 설정이 일치하는지 확인하세요.
-   **Redis Connection Refused**: Redis 컨테이너가 실행 중인지, 비밀번호가 맞는지 확인하세요.
-   **메모리 부족 (OOM)**: Docker Desktop의 리소스 할당을 늘리거나, JVM Heap 설정을 줄이세요.

## 번외: Kubernetes는 언제 고려할까?

소규모 수준 → Docker Compose 로 충분.

> 아래 조건 중 2개 이상 해당하면 점진적으로 검토.
- 트래픽이 수시로 급증하고 자동 수평 확장이 필요
- 여러 인스턴스를 무중단 롤링 업데이트해야 함
- 서비스/작업(Job, Batch) 종류가 늘어나 관리 포인트 증가
- 팀 인원이 늘어나 인프라 표준화가 과제

