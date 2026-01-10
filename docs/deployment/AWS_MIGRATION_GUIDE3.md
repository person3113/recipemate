
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

# EC2에서 PostgreSQL 클라이언트로 RDS 접속
sudo dnf install -y postgresql-client

# RDS 접속 테스트
psql -h recipemate-db.cxxxxxx.ap-northeast-2.rds.amazonaws.com \
     -U postgres \
     -d recipemate

# 스키마 생성 (init-db.sql 실행)
\i ./init-db.sql
\i ./schema.sql
```

***

## 🔄 Step 5: Docker Compose 재시작 + 배포

```bash
# 1. 코드 업데이트 (로컬)
git add docker-compose.yml .env application-prod.yml
git commit -m "feat: Migrate to AWS RDS PostgreSQL"
git push origin main

# 2. EC2에서 반영
cd ~/recipemate
git pull

# 3. 컨테이너 재시작
docker compose down
docker compose up -d --build --force-recreate

# 4. 로그 확인 (DB 연결 확인)
docker compose logs -f app | grep -i "connect\|datasource\|error"

# 5. 헬스체크
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

***

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

- [ ] RDS 인스턴스 생성 및 보안 그룹 설정
- [ ] RDS 엔드포인트 확인
- [ ] docker-compose.yml에서 postgres 서비스 삭제
- [ ] .env에 RDS_ENDPOINT 추가
- [ ] 로컬 테스트: `docker compose up -d --build`
- [ ] EC2에 배포 및 RDS 연결 확인
- [ ] GitHub Actions Workflow 생성
- [ ] Secrets (EC2_HOST, EC2_KEY) 설정
- [ ] git push 후 자동 배포 확인

***

## 💡 비용 정리 (프리티어)

| 서비스 | 비용 | 사유 |
|--------|------|------|
| EC2 t2.micro | 무료 | 750시간/월 |
| RDS db.t4g.micro | 무료 | 750시간/월 |
| Redis (Docker) | 무료 | 컨테이너 운영 |
| Nginx (Docker) | 무료 | 컨테이너 운영 |
| 데이터 전송 | ~1GB | 프리티어 100GB 포함 |

**합계: 무료!** (주의: 프리티어 기간 후 과금 시작)

***

이제 4단계 시작하시겠어요? 먼저 RDS 생성하고 보고하면 docker-compose.yml 최종 버전 제시해드릴게요! 🚀
