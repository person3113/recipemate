
현재 상황 정리했습니다:
- ✅ **1~3단계 완료**: EC2 + Docker Compose + Nginx + HTTPS(도메인)
- ✅ **현재**: `https://recipemate.duckdns.org` 실행 중
- ❌ **8080 포트**: 여전히 개방됨 (보안 그룹)

## 🚀 4단계: AWS RDS + CI/CD (프리티어 범위)

### 📊 현재 상태
```
Local Docker: App + PostgreSQL (Docker) + Redis (Docker)
↓
AWS EC2: App + PostgreSQL (Docker) + Redis (Docker)  ← 여기
↓
AWS EC2: App + PostgreSQL (RDS 관리형) + Redis (ElastiCache)  ← 목표
```

***

## ✅ Step 1: AWS RDS PostgreSQL 생성 (프리티어)

### 1. RDS 인스턴스 생성 (AWS Console)

```
RDS 대시보드 → 데이터베이스 생성 → PostgreSQL 선택

[필수 설정]
- Engine: PostgreSQL 16.x (최신)
- Instance class: db.t4g.micro (프리티어)
- Multi-AZ: 비활성화 (비용)
- 스토리지: 20GB (프리티어 최대)
- 퍼블릭 액세스: 아니오 (EC2만 접근)
- DB 이름: recipemate
- 마스터 사용자: postgres
- 마스터 암호: 강력한 비밀번호 입력
```

### 2. 보안 그룹 설정

```bash
# RDS 보안 그룹 생성
RDS Security Group Inbound:
- PostgreSQL (5432) 
- Source: EC2 보안 그룹 (launch-wizard-1)
- 👉 EC2에서만 접근 가능 (외부 차단)
```

### 3. 연결 문자열 복사

```bash
# RDS 콘솔에서 "엔드포인트" 복사 (예: recipemate-db.cxxxxxx.ap-northeast-2.rds.amazonaws.com)
# docker-compose.yml에서 사용할 값
RDS_ENDPOINT=recipemate-db.cxxxxxx.ap-northeast-2.rds.amazonaws.com
```

***

## 🔄 Step 2: docker-compose.yml 수정 (RDS 연동)

**현재 구조:**
```yaml
postgres:
  image: postgres:16-alpine  # ← Docker 컨테이너
  ...
```

**변경 후:**
```yaml
# postgres 서비스 삭제/주석 처리 (RDS 사용)
# RDS에서 자동 관리됨
```

### 수정된 docker-compose.yml

```yaml
services:
  # PostgreSQL은 RDS로 옮김 (Docker 제거)
  # postgres: 삭제

  # Redis는 유지 (ElastiCache는 비용)
  redis:
    image: redis:7-alpine
    container_name: recipemate-redis
    restart: unless-stopped
    command: redis-server --requirepass ${REDIS_PASSWORD} --maxmemory 256mb --maxmemory-policy allkeys-lru
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 3s
      retries: 5
    networks:
      - recipemate-network

  # Spring Boot App (RDS 연동)
  app:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: recipemate-app
    restart: unless-stopped
    depends_on:
      redis:
        condition: service_healthy
    environment:
      SPRING_PROFILES_ACTIVE: ${SPRING_PROFILES_ACTIVE:-prod}
      SPRING_DATASOURCE_URL: jdbc:postgresql://${RDS_ENDPOINT}:5432/recipemate  # ✅ RDS 엔드포인트
      SPRING_DATASOURCE_USERNAME: ${DB_USERNAME:-postgres}
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
      SPRING_JPA_HIBERNATE_DDL_AUTO: validate  # RDS는 이미 테이블 존재
      REDIS_HOST: redis
      REDIS_PORT: 6379
      REDIS_PASSWORD: ${REDIS_PASSWORD}
      # ... 기타 환경변수
    ports:
      - "8080:8080"
    volumes:
      - app_uploads:/app/uploads
    healthcheck:
      test: ["CMD", "wget", "--no-verbose", "--tries=1", "--spider", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
    networks:
      - recipemate-network

  # Nginx (기존 유지)
  nginx:
    image: nginx:1.25-alpine
    container_name: recipemate-nginx
    restart: unless-stopped
    depends_on:
      - app
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx/nginx.conf:/etc/nginx/nginx.conf:ro
      - ./nginx/conf.d:/etc/nginx/conf.d:ro
      - nginx_logs:/var/log/nginx
      - ./nginx/ssl:/etc/nginx/ssl:ro
    healthcheck:
      test: ["CMD", "wget", "--no-verbose", "--tries=1", "--spider", "http://127.0.0.1"]
      interval: 30s
      timeout: 3s
      retries: 3
    networks:
      - recipemate-network

networks:
  recipemate-network:
    driver: bridge

volumes:
  redis_data:
    driver: local
  app_uploads:
    driver: local
  nginx_logs:
    driver: local
```

- 환경변수 관련해서 몇 가지 설명:
  - 필수 추가 아님 → 없어도 동작함.
  - 표준화하고 싶으면 기존 `DB_USERNAME`/`DB_PASSWORD`를 지우지 말고 겹쳐서 `SPRING_DATASOURCE_USERNAME/PASSWORD`를 “추가”하는 방식이 안전하다.
  - `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `SPRING_JPA_HIBERNATE_DDL_AUTO`는 “있으면 좋은 옵션”이지 필수는 아니다.[1][2]
  - 지금처럼 `SPRING_DATASOURCE_URL` + `DB_USERNAME` + `DB_PASSWORD` 조합으로 이미 잘 되고 있다면, 굳이 바꿀 필요 없다.

1. 이 옵션들이 의미하는 것

- `SPRING_DATASOURCE_USERNAME` / `SPRING_DATASOURCE_PASSWORD`
    - Spring Boot가 `spring.datasource.username`, `spring.datasource.password`를 외부 환경변수로 받을 때 쓰는 공식 키 이름이다.[2][1]
    - 즉, `application.yml`에서
      ```yaml
      spring:
        datasource:
          url: ${SPRING_DATASOURCE_URL}
          username: ${SPRING_DATASOURCE_USERNAME}
          password: ${SPRING_DATASOURCE_PASSWORD}
      ```  
      이런 식으로 매핑할 때 보통 사용한다.[1]

- `SPRING_JPA_HIBERNATE_DDL_AUTO`
    - `spring.jpa.hibernate.ddl-auto`에 대응되는 환경변수.
    - RDS에 이미 테이블 만들어놨으면 `validate`로 두는 게 안전하고, 처음 자동 생성하고 싶으면 `update`/`create` 등을 쓴다.[3]

2. 지금 상황에서 어떻게 할지

현재 docker-compose.yml (요약)

```yaml
environment:
  SPRING_DATASOURCE_URL: jdbc:postgresql://recipemate....rds.amazonaws.com:5432/recipemate
  DB_USERNAME: ${DB_USERNAME}
  DB_PASSWORD: ${DB_PASSWORD}
```

이 구조라면 보통은:

- `application-prod.yml`에서 직접 `DB_USERNAME`, `DB_PASSWORD`를 읽거나
- 자바 코드에서 `System.getenv("DB_USERNAME")` 이런 식으로 읽도록 되어 있을 가능성이 높다.[4]

그래서:

- 이미 잘 돌아간다면 → 건들지 말고 그대로 사용해도 된다.
- Spring의 표준 방식으로 정리하고 싶다면 → 아래처럼 “추가”하는 식으로 점진적 변경 추천.

추천하는 점진적 변경

1) 기존 것 유지 + 새 키 추가 (겹쳐서 넣기)

```yaml
environment:
  SPRING_DATASOURCE_URL: jdbc:postgresql://recipemate....rds.amazonaws.com:5432/recipemate
  SPRING_DATASOURCE_USERNAME: ${DB_USERNAME}
  SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
  SPRING_JPA_HIBERNATE_DDL_AUTO: validate
  DB_USERNAME: ${DB_USERNAME}
  DB_PASSWORD: ${DB_PASSWORD}
```

- 이렇게 하면:
    - 기존 코드에서 `DB_USERNAME` / `DB_PASSWORD`를 쓰고 있으면 그대로 동작.
    - 나중에 `application-prod.yml` 쪽을 정리할 때 `SPRING_DATASOURCE_*`만 쓰도록 옮길 수 있다.[5][2]

2) 이후에 설정 파일을 정리할 때:

```yaml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: ${SPRING_JPA_HIBERNATE_DDL_AUTO:validate}
```

이렇게 되면 완전히 Spring 표준 키로 통일되고, 그때 가서 `DB_USERNAME`, `DB_PASSWORD` 환경변수는 제거해도 된다.[2][1]

3. 지금 당장 해도 되는 최소 변경안

당장 에러 없이, RDS용으로 조금 더 안전하게만 하려면:

```yaml
environment:
  SPRING_PROFILES_ACTIVE: ${SPRING_PROFILES_ACTIVE:-prod}
  SPRING_CACHE_TYPE: ${SPRING_CACHE_TYPE:-none}
  SPRING_DATASOURCE_URL: jdbc:postgresql://recipemate.c3qcycyic4eb.ap-northeast-2.rds.amazonaws.com:5432/recipemate
  SPRING_JPA_HIBERNATE_DDL_AUTO: validate  # 이 정도만 추가
  DB_USERNAME: ${DB_USERNAME}
  DB_PASSWORD: ${DB_PASSWORD}
  ...
```

- `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`는 지금 프로젝트 설정을 안 봐서는 꼭 쓸 필요는 없다.
- 대신 `.env`에서 `DB_USERNAME`, `DB_PASSWORD`만 RDS 계정으로 맞춰 두면 된다.

### Step 3: .env 파일 수정

```bash
# EC2에서
vim .env

# 추가/수정할 내용
RDS_ENDPOINT=recipemate-db.cxxxxxx.ap-northeast-2.rds.amazonaws.com
DB_USERNAME=postgres
DB_PASSWORD=your_strong_password_123!
SPRING_PROFILES_ACTIVE=prod
# ... 기타 변수
```

### Step 4: RDS 초기화 (스키마 생성)

```bash
# 로컬에서 RDS 스키마 생성 (한 번만)
# application-prod.yml에서 spring.jpa.hibernate.ddl-auto: create 설정
# OR RDS에 직접 접속해서 init-db.sql, schema.sql 실행

# 패키지 목록/캐시 최신화
sudo dnf update -y

# PostgreSQL 15 클라이언트 (psql 포함) 설치
sudo dnf install -y postgresql15

# 설치 확인
psql --version
# psql (PostgreSQL) 15.x 형태로 나오면 성공

# 1. postgres DB로 접속 (기본 DB)
psql -h recipemate.c3qcycyic4eb.ap-northeast-2.rds.amazonaws.com \
     -U postgres \
     -d postgres  # recipemate → postgres 변경!

# 2. recipemate DB 생성
CREATE DATABASE recipemate;
GRANT ALL PRIVILEGES ON DATABASE recipemate TO postgres;
\q  # 종료

# RDS 접속 테스트
psql -h recipemate.c3qcycyic4eb.ap-northeast-2.rds.amazonaws.com \
     -U postgres \
     -d recipemate

# 스키마 생성 (init-db.sql 실행)
\i ./init-db.sql
\i ./schema.sql
```

## 🔄 Step 5: 데이터 백업 + Docker Compose 재시작 + 배포

```bash
# EC2에서 (docker compose 실행 중일 때)

# 현재 Docker DB 백업 (단일 DB만 백업)
# psql -h recipemate-db... -U postgres -d recipemate < recipemate_backup.sql
docker compose exec -T postgres pg_dump -U ${DB_USERNAME} recipemate > recipemate_backup.sql

# 백업 확인
ls -lh recipemate_backup.sql  # 1-10MB 예상
head -20 recipemate_backup.sql  # CREATE TABLE 확인

# 코드 업데이트 (로컬)
git add docker-compose.yml .env application-prod.yml
git commit -m "feat: Migrate to AWS RDS PostgreSQL"
git push origin main

# EC2에서 반영
cd ~/recipemate
git pull

# 중단 (볼륨 보존)
docker compose down

# RDS에 복원 (선택)
psql -h $RDS_ENDPOINT -U postgres -d recipemate < recipemate_backup.sql

# 재시작
docker compose up -d --build

# 로그 확인 (DB 연결 확인)
docker compose logs -f app | grep -i "connect\|datasource\|error"

# 헬스체크
curl https://recipemate.duckdns.org/actuator/health
```

***

## 📦 Step 6: CI/CD (GitHub Actions)

**목표:** Git push → 자동으로 EC2 배포

### 1. GitHub Actions Workflow 생성

로컬에서 프로젝트 루트에 파일 생성:

```bash
mkdir -p .github/workflows
cat > .github/workflows/deploy.yml << 'EOF'
name: Deploy to AWS EC2

on:
  push:
    branches:
      - main

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: SSH Deploy to EC2
        uses: appleboy/ssh-action@master
        with:
          host: ${{ secrets.EC2_HOST }}
          username: ec2-user
          key: ${{ secrets.EC2_KEY }}
          script: |
            cd ~/recipemate
            git pull origin main
            docker compose down
            docker compose up -d --build
            docker compose logs -f app | head -20
EOF
```

### 2. GitHub Secrets 설정

```
GitHub 저장소 → Settings → Secrets and variables → Actions → New repository secret

EC2_HOST: 13.125.48.36 (Elastic IP)
EC2_KEY: (EC2 키 페어 .pem 파일 내용 전체 복사)
  C:\Users\UESR\.ssh 폴더에 있음
```


1. Secrets and variables 위치
   1. GitHub에서 레포지토리 페이지로 이동
   2. 상단 탭에서 Settings 클릭
      - 안 보이면 오른쪽의 `…` 드롭다운 눌러서 Settings 선택[1]
   3. 왼쪽 사이드바에서:
      - Security 섹션 아래 Secrets and variables 클릭
        - 그 안에서 Actions 선택[1]
   4. 상단에 탭이 두 개 보임:
      - Secrets
      - Variables
      - 여기서 Secrets 탭에 있는 New repository secret 버튼을 누르면 됨.[3][1]
2. Secret 값 설정은 이렇게 이름/값은 이렇게 두 개 만들면 됨 (예시 기준):
   - `EC2_HOST`
     - 값: `13.125.48.36` (Elastic IP)
   - `EC2_KEY`
     - 값: EC2 .pem 파일 전체 내용
     - 로컬에서 메모장/VS Code로 `.pem` 열고 전부 복사해서 붙여넣기
     - `-----BEGIN OPENSSH PRIVATE KEY-----` 또는 `-----BEGIN RSA PRIVATE KEY-----` 같은 헤더부터 끝까지 그대로

워크플로에서 참조는 이렇게 쓰면 됨:

```yaml
with:
  host: ${{ secrets.EC2_HOST }}
  username: ec2-user
  key: ${{ secrets.EC2_KEY }}
```

### 3. 배포 테스트

```bash
# 로컬에서 작은 변경 후
echo "# Updated" >> README.md
git add README.md
git commit -m "test: CI/CD trigger"
git push origin main

# GitHub Actions 탭 확인
# → 자동으로 EC2에 배포됨!
```

### 4. 실패 및 완료

> 더 진행하려고 했지만 aws free tier 한도 초과로 인해 중단함. 

지금 에러는 키 내용 문제가 아니라, EC2에 아예 접속이 안 돼서(i/o timeout) 나는 상황입니다.  
GitHub → EC2로의 SSH 트래픽이 막혀 있어서 그래요.

1. 에러 원인 정확히 짚기

로그에서 핵심 부분:

> `dial tcp *:22: i/o timeout`  
→ GitHub Actions 러너가 `EC2_HOST:22`로 접속을 시도했는데 응답이 없어서 타임아웃.[3]

이건 보통 두 가지 케이스입니다.

1. EC2가 꺼져 있거나, 네트워크 안 됨
2. EC2 보안 그룹에서 22번 포트를 GitHub IP에 대해 허용 안 됨 (지금 이거일 확률 거의 100%)

현재 보안 그룹 인바운드 규칙은:

- 22/TCP → (네 PC IP만 허용)
  GitHub Actions 러너는 전 세계 어딘가의 IP에서 접속하므로, 이 규칙에 걸려서 SSH 불가입니다.

2. 해결 전략 선택 (둘 중 하나)

옵션 A. 임시로 22 포트를 0.0.0.0/0 열기 (가장 간단, 연습용)

> 실무에서는 비추지만, 학습·연습용으로는 괜찮은 방법.

1. AWS 콘솔 → EC2 → 좌측 보안 그룹 → `recipemate-sg` 선택[4]
2. 인바운드 규칙 편집:
  - 기존 `22 / TCP / 내 pc ip` 규칙 수정:
    - 유형: SSH
    - 포트: 22
    - 소스: `0.0.0.0/0`
3. 저장

그 다음, GitHub Actions 다시 트리거:

```bash
# 아무 커밋이나 푸시
git commit --allow-empty -m "chore: retrigger deploy"
git push origin main
```

성공하면 나중에 이렇게 다시 잠그면 됩니다:

- 소스를 다시 `My IP`로 바꾸거나
- 아예 이 CI/CD 구조 대신 다른 방법으로 보완

옵션 B. GitHub Actions 대신, PC → EC2 직접 배포 (보안 유지)

EC2를 굳이 GitHub에서 직접 SSH로 연결하지 않고, 로컬 PC에서 배포 스크립트로만 관리하는 방식.

예:
```bash
# 로컬에서 배포 스크립트
ssh -i ~/.ssh/recipemate-ec2-key.pem ec2-user@13.125.48.36 << 'EOF'
cd ~/recipemate
git pull origin main
docker compose down
docker compose up -d --build
EOF
```

이 경우:
- 보안 그룹 22포트는 계속 내 IP만 허용 유지 가능
- GitHub Actions는 굳이 EC2에 SSH 안 함 (대신 테스트/빌드까지만)

### 5. 대안 메모

실무에서는 둘 다 쓰는데, 규모/보안 요구에 따라 선택이 달라집니다.

실무에서는 보통 어떻게 하냐
보통은 세 가지 레벨로 나뉩니다.

1. 초기/소규모 팀
  - SSH로 EC2 들어가서 직접 `git pull && docker compose up -d` 하는 경우 많음.
  - 나중에 불편해지면 GitHub Actions 같은 CI/CD를 붙임.
2. 중·소규모 서비스
  - 지금 너가 하려는 것처럼  
    GitHub Actions → SSH로 EC2에 배포하는 패턴이 가장 흔함.
  - 이때 SSH 보안은:
    - 전용 배포 키(EC2용) 따로 만들고
    - 보안 그룹은 고정된 배포 서버 IP만 열어두거나
    - EC2에 self-hosted runner를 띄워서, 내부에서만 배포 돌리기도 함.
3. 대규모/정식 서비스
  - EC2 단독보다는 ECS/EKS + CodeDeploy/CodePipeline + ALB 같은 조합 사용.
  - 블루/그린 배포, 롤백, 헬스체크 자동화까지 모두 파이프라인에 포함.

---

중·소규모에서 말한 그 세 가지는, “자동 배포는 하되 SSH를 막 열어두지 않는 방법들”입니다.

1. 전용 배포 키 (Deploy Key)
  - 개발자 개인 키랑 다른, CI/CD 전용 SSH 키를 하나 더 만드는 방식입니다.[3]
  - 구조:
    - 로컬/CI에서 `ssh-keygen`으로 `deploy-key` 생성
    - 공개키(.pub) → EC2의 `~/.ssh/authorized_keys`에 등록
    - 비밀키 → GitHub Actions Secret(예: `EC2_DEPLOY_KEY`)에 저장
  - 장점:
    - 키가 유출돼도 “배포용 계정”만 위험하고, 개인 개발자 키는 안전.
    - 필요하면 이 키만 `authorized_keys`에서 지우고 교체 가능.
2. 고정된 배포 서버 IP만 열기
  - SSH 포트(22)를 0.0.0.0/0로 열지 않고, 특정 IP만 허용하는 방법입니다.[4]
  - 예:
    - 회사 VPN, 사무실 고정 IP, 혹은 별도의 “배포용 서버” IP만 보안 그룹 인바운드에 등록.
    - GitHub Actions는 그 배포 서버에만 접속 → 배포 서버가 다시 EC2에 접속.
  - 장점:
    - EC2에 외부에서 바로 SSH 들어올 수 있는 IP가 제한됨.
    - GitHub 러너의 불특정 IP를 직접 허용할 필요가 없어짐.
3. EC2에 self-hosted runner 띄우기
  - GitHub Actions 러너를 EC2 안에 직접 설치해서 돌리는 방식입니다.[5]
  - 구조:
    - EC2에서 `./config.sh`로 GitHub self-hosted runner 등록
    - 이후 Actions job은 “GitHub 클라우드 러너”가 아니라 “EC2 내부 러너”에서 실행
  - 특징:
    - 배포 스크립트가 `ssh`가 아니라 로컬 명령처럼 동작:
    - 외부에서 EC2로 SSH 접속할 필요가 없어서, 보안 그룹 22 포트를 My IP만 열어놔도 됨.[6]
    ```yaml
    runs-on: self-hosted

    steps:
      - uses: actions/checkout@v3
      - run: |
          cd ~/recipemate
          git pull
          docker compose down
          docker compose up -d --build
    ```

정리하면:

- 전용 배포 키: “사람용 키”랑 “CI용 키” 분리해서 리스크 줄이기
- 고정 배포 서버 IP: SSH를 열긴 여는데 “딱 한 군데”만
- self-hosted runner: 아예 Actions를 EC2 안에서 돌려서 SSH 자체를 없애기

지금 개인 프로젝트 + 1대 EC2 상황이면:

- CI/CD 경험용으로는 지금 구조 + 전용 배포 키 정도가 딱 좋고,
- 보안까지 챙기고 싶으면 self-hosted runner까지 가면 “실무 감각”이랑 가장 비슷합니다.[7][8]

중·소규모 방법 중 하나만 고르라면, “EC2에 self-hosted runner 띄우는 방식”이 더 보안 좋고 실무에 가깝습니다.[2][3]

- 왜 self-hosted runner가 더 좋은지
  - SSH 포트를 GitHub에 안 열어도 됨
    - 배포는 EC2 안에서 실행되므로, 외부에서 EC2로 SSH 들어올 필요가 없음.[2]
  - Actions에서 바로 로컬 명령 실행
    - `ssh` 액션 대신 `docker compose up -d`를 그냥 실행하는 구조라 심플함.[3]
  - 실무 패턴과 유사
    - 많은 팀이 “Kubernetes/ECS 전에” 이 구조로 상당히 오래 운영함.[4]

self-hosted runner 기반 CI/CD 구조 (추천)

대략 이런 그림입니다:

- GitHub main에 push
- GitHub Actions가 EC2 안의 runner에 job 전달
- EC2 내부에서:
  ```bash
  cd ~/recipemate
  git pull
  docker compose down
  docker compose up -d --build
  ```

SSH 키를 GitHub에 주지 않아도 되고,  
EC2 보안 그룹도 22 포트는 “내 IP만” 유지 가능해서 안전합니다.[5][6]

### 6. self-hosted runner 개요만 메모

EC2에 self-hosted runner 설치 (한 번만 하면 됨)

0. 사전 조건

- EC2에 SSH 접속 가능해야 함:
  ```bash
  ssh -i ~/.ssh/recipemate-ec2-key.pem ec2-user@13.125.48.36
  ```

- GitHub에서 레포지토리 열고:
  1. Settings → Actions → Runners → New runner → New self-hosted runner 클릭[3]
  2. OS: Linux, Architecture: x64 선택
  3. 거기서 나오는 `./config.sh --url ... --token ...` 명령이 핵심이라, 아래에 `<YOUR_URL>`, `<YOUR_TOKEN>` 자리에 그 값만 넣으면 됨.

1. EC2에서 runner 설치

EC2 쉘에서 아래 명령어 그대로 실행:

```bash
# 1. 러너용 디렉터리 생성
mkdir -p ~/actions-runner
cd ~/actions-runner

# 2. 최신 러너 패키지 다운로드 (GitHub 공식 예시 버전)
curl -o actions-runner-linux-x64-2.321.0.tar.gz -L \
  https://github.com/actions/runner/releases/download/v2.321.0/actions-runner-linux-x64-2.321.0.tar.gz

# 3. 압축 해제
tar xzf ./actions-runner-linux-x64-2.321.0.tar.gz
```

2. GitHub 레포와 연결 (config.sh)

GitHub에서 복사한 값으로 실행 (예시는 형태만):

```bash
cd ~/actions-runner

./config.sh --url https://github.com/person3113/recipemate \
            --token <GITHUB가_화면에_보여준_런너_토큰>
```

실행 중에 물어보는 것들:

- `Enter the name of the runner` → 그냥 `recipemate-ec2-runner` 정도
- `Enter any additional labels` → `recipemate-ec2` 같이 하나 넣어두면 좋음
- 나머진 기본값 Enter

끝나면 `Runner successfully added` 비슷한 메시지가 나올 거야.[4]

3. 러너 실행 + 서비스 등록

```bash
cd ~/actions-runner

# 1회성 테스트 실행 (터미널 붙잡고 있음)
./run.sh
```

- 이 상태에서 GitHub → Settings → Actions → Runners 에 보면  
  해당 러너가 Online 상태로 떠야 함.[3]

테스트까지 OK면, 서비스로 등록해서 자동 실행되게 만들기:

```bash
cd ~/actions-runner

# 서비스 설치 (Amazon Linux는 root 필요)
sudo ./svc.sh install ec2-user
sudo ./svc.sh start

# 상태 확인
sudo ./svc.sh status
```

이제 EC2 재부팅해도 자동으로 올라오는 상주 러너가 됨.

GitHub Actions deploy.yml (self-hosted용)

이제 워크플로에서 ssh-action 제거하고, 그냥 “EC2 안에서 로컬 명령”처럼 실행하면 됨.[3]

`.github/workflows/deploy.yml` 를 아래처럼 바꾸면 된다:

```yaml
name: Deploy to AWS EC2 (Self-hosted)

on:
  push:
    branches:
      - main

jobs:
  deploy:
    # 🔥 여기 중요한 부분: self-hosted + 레이블
    runs-on: [self-hosted, linux, recipemate-ec2]  # config.sh에서 넣은 label

    steps:
      - name: Checkout repository
        uses: actions/checkout@v4

      - name: Show current runner info (debug)
        run: |
          echo "Runner name: $RUNNER_NAME"
          echo "Runner OS: $RUNNER_OS"
          echo "Runner labels: $RUNNER_LABELS"
          pwd
          ls

      - name: Deploy with Docker Compose
        run: |
          cd ~/recipemate
          git pull origin main
          docker compose down
          docker compose up -d --build
          docker compose ps
```

핵심 포인트:

- `ru
  - `recipemate-ec2`는ns-on: [self-hosted, linux, recipemate-ec2]` config.sh 할 때 입력한 label이랑 같아야 함.[3]
- SSH 키, EC2_HOST 같은 Secret 필요 없음
- Actions job이 EC2 안에서 실행되기 때문에 `cd ~/recipemate`가 바로 먹음

최종 체크 흐름

1. EC2에서 `svc.sh start`까지 해서 runner Online 상태인지 확인
2. `deploy.yml` 커밋 & 푸시:

```bash
git add .github/workflows/deploy.yml
git commit -m "chore: add self-hosted runner deploy workflow"
git push origin main
```

3. GitHub → Actions 탭에서 `Deploy to AWS EC2 (Self-hosted)` 실행 확인
4. 성공하면 EC2에서:

```bash
docker compose ps
```

로 상태 보고,  
`https://recipemate.duckdns.org` 접속해서 최신 코드 반영된 것까지 확인.

이렇게 하면:

- 22 포트는 계속 내 IP만 열어두고
- GitHub에는 SSH 키 안 주고
- 그래도 git push → 자동 배포가 되는 구조라  
  지금 상황에서 가장 “실무스럽고 안전한” 구성이야.

중간에 막히는 단계 (config.sh 실행화면, 러너 Online 상태, Actions 에러 등) 나오면 그 부분 로그만 붙여서 다시 보내주면 거기서부터 이어서 잡아줄게.


## 📊 최종 아키텍처 (4단계 완료)

```
┌─────────────┐
│  Local Dev  │
│  (Develop)  │
└──────┬──────┘
       │ git push
       ↓
┌─────────────────────┐
│  GitHub Repository  │
│   (main branch)     │
└──────┬──────────────┘
       │ GitHub Actions Webhook
       ↓
┌────────────────────────────────────────┐
│         AWS EC2 (t2.micro)             │
│  ┌──────────────────────────────────┐  │
│  │  Spring Boot App (Docker)        │  │
│  │  Nginx Reverse Proxy             │  │
│  │  Redis (Docker)                  │  │
│  └──────────────────────────────────┘  │
│              ↓                          │
│  ┌──────────────────────────────────┐  │
│  │  AWS RDS (PostgreSQL db.t4g)     │◄─┼─ Managed DB
│  └──────────────────────────────────┘  │
└────────────────────────────────────────┘
       ↑                ↑
  HTTPS + Domain  Auto-scaling Ready
  (DuckDNS)       (근데 1단계로 충분)
```

***

## 🎯 체크리스트

-  RDS 인스턴스 생성 및 보안 그룹 설정
-  RDS 엔드포인트 확인
-  docker-compose.yml에서 postgres 서비스 삭제
-  .env에 RDS_ENDPOINT 추가
-  로컬 테스트: `docker compose up -d --build`
-  EC2에 배포 및 RDS 연결 확인
-  GitHub Actions Workflow 생성
-  Secrets (EC2_HOST, EC2_KEY) 설정
-  git push 후 자동 배포 확인

## 💡 비용 정리 (프리티어)

| 서비스 | 비용 | 사유 |
|--------|------|------|
| EC2 t2.micro | 무료 | 750시간/월 |
| RDS db.t4g.micro | 무료 | 750시간/월 |
| Redis (Docker) | 무료 | 컨테이너 운영 |
| Nginx (Docker) | 무료 | 컨테이너 운영 |
| 데이터 전송 | ~1GB | 프리티어 100GB 포함 |

**합계: 무료!** (주의: 프리티어 기간 후 과금 시작)