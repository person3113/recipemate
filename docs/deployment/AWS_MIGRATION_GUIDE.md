# AWS EC2 배포 및 마이그레이션 가이드 (Docker 기반)

이 문서는 Render(PaaS) 환경에서 AWS EC2(IaaS) 환경으로 서비스를 이관하며 실무 경험을 쌓기 위한 단계별 가이드입니다.
**단계적 접근(Step-by-Step)**을 통해 학습 효과를 높이고 배포 성공 확률을 극대화하는 것을 목표로 합니다.

## 📅 배포 로드맵

| 단계 | 목표 | 주요 구성 | 특징 |
| :--- | :--- | :--- | :--- |
| **1단계** | **MVP 배포 (1차 목표)** | **EC2 + Docker Compose (App, DB, Redis)** | **Nginx 없음.** 8080 포트 직접 노출. 빠르고 단순한 배포 경험. |
| **2단계** | **인프라 고도화** | + Nginx (Reverse Proxy) | 80/443 포트 사용, 정적 파일 처리 효율화. |
| **3단계** | **보안/운영 강화** | + HTTPS (SSL), 도메인 연결 | 실무 수준의 보안 구성. |
| **4단계** | **확장성 확보** | AWS RDS (DB 분리), CI/CD | 관리형 DB 사용 및 자동 배포 파이프라인 구축. |

---

## 🛠️ 1단계: 로컬 Docker (Pre-migration)

AWS에 올리기 전, 로컬에서 완벽하게 동작하는 Docker 환경을 만듭니다. **1차 배포를 위해 구성을 단순화**합니다.

### 1. Dockerfile 개선
빌드 일관성과 속도를 위해 로컬의 Gradle Wrapper를 사용하도록 수정합니다.

- [x] **Gradle Wrapper 활용:** `gradle` 이미지 대신 JDK 이미지 기반에서 `./gradlew`를 사용하도록 변경.
- [x] **빌드 테스트:** `docker build -t recipemate-app .` 명령어로 이미지 생성 확인.

### 2. docker-compose.yml 경량화
1차 배포에서는 복잡도를 낮추기 위해 **Nginx를 제거**하고 3개의 컨테이너(App, Postgres, Redis)만 실행합니다.

- [x] **Nginx 제거:** `nginx` 서비스 블록 및 관련 볼륨/네트워크 설정 주석 처리.
- [x] **포트 노출:** App 컨테이너의 `8080:8080` 포트 매핑 유지.
- [x] **로컬 검증:**
    ```bash
    # .env 파일 준비
    docker compose up -d --build
    # http://localhost:8080 접속하여 로그인, DB 조회 등 기능 확인
    ```

### 3. 환경변수 전략 수립
**"키(Key)는 코드에, 값(Value)은 환경에"** 원칙을 따릅니다.
복잡하게 파일을 나누기보다, **각 환경에 맞는 단일 `.env` 파일**을 사용하는 것이 가장 단순하고 확실합니다. (상세 내용은 `docs/deployment/ENV_VAR_MANAGEMENT.md` 참조)

- [x] **공통 변수 확인:** `application.yml`에서 사용하는 키(`DB_USERNAME`, `SPRING_DATASOURCE_URL` 등) 확인.
- [x] **단일 `.env` 파일 유지:**
    - **로컬 개발 시:** 프로젝트 루트의 `.env` 파일에 로컬용 값(예: `DB_HOST=postgres`) 입력.
    - **AWS 배포 시:** EC2 서버의 `.env` 파일에 운영용 값(예: `DB_HOST=rds-endpoint...` 또는 `postgres`) 입력.
    - *Tip: `.env.local`, `.env.prod` 등으로 파일을 쪼개기보다, 실행 환경마다 그에 맞는 `.env` 파일 하나만 두는 것을 권장합니다.*

### 4. 로컬 최종 점검
- [x] docker로 띄워서 접속은 돼. 근데 psql? 뭐 그런 걸로 docker의 postgresql에 뭐가 저장되어있는지 확인 가능해?
- [x] db에 데이터 없을 시 recipe api 호출이나 어드민 계정 생성 등으로 초기 데이터 채워지는지 확인 및 테스트.

---

## ☁️ 2단계: AWS EC2 인프라 구축 (Console)

### 1. EC2 인스턴스 생성
- **Region:** 서울 (ap-northeast-2)
- **OS:** Amazon Linux 2023 (또는 Ubuntu Server 24.04 LTS)
- **Type:** `t2.micro` (Free Tier 지원)
- **Key Pair:** 키 페어(`.pem`) 선택 및 안전한 곳에 보관
- **Storage:** 30GB (Free Tier 최대 용량 활용)

### 2. 보안 그룹 (Security Group) 설정
1차 배포는 Nginx 없이 8080 포트를 직접 사용하므로 **8080 포트 개방**이 필수입니다.

| Type | Port | Source | Description |
| :--- | :--- | :--- | :--- |
| SSH | 22 | My IP | 내 PC에서만 접속 (보안 필수) |
| Custom TCP | **8080** | Anywhere (0.0.0.0/0) | Spring Boot 직접 접속용 |
| HTTP | 80 | Anywhere | (2차 Nginx 배포용 미리 개방) |
| HTTPS | 443 | Anywhere | (2차 HTTPS 배포용 미리 개방) |

### 3. 탄력적 IP (Elastic IP)
- [x] EIP 할당 및 EC2 인스턴스 연결 (서버 재시작 시 IP 변경 방지).

---

## 🚀 3단계: 서버 설정 및 배포 (Terminal)

### 1. 기본 설정 & Docker와 Java 설치
```bash
# SSH 접속(Git Bash 또는 WSL에서)
### ubuntu
# ssh -i "path/to/key.pem" ubuntu@<Elastic-IP>

### Amazon Linux
# ssh -i "path/to/key.pem" ec2-user@<Elastic-IP>
ssh -i "C:\Users\UESR\.ssh\recipemate-ec2-key.pem" ec2-user@13.125.48.36

# 패키지 업데이트 및 Docker 설치
### Ubuntu 기준
# sudo apt update && sudo apt install -y docker.io docker-compose-plugin
# sudo usermod -aG docker ubuntu

### Amazon Linux 2023 기준
sudo dnf update -y
sudo dnf install -y git docker java-21-amazon-corretto-headless
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -aG docker ec2-user
java --version  # 21.x.x 확인
git --version  # git version 2.43.x 등 출력됨

# docker compose 설치
# 플러그인 디렉토리 생성
sudo mkdir -p /usr/local/lib/docker/cli-plugins

# 최신 버전 다운로드 (자동)
sudo curl -SL "https://github.com/docker/compose/releases/latest/download/docker-compose-linux-$(uname -m)" \
  -o /usr/local/lib/docker/cli-plugins/docker-compose

# 실행 권한 부여
sudo chmod +x /usr/local/lib/docker/cli-plugins/docker-compose

# 설치 확인
docker compose version

# (로그아웃 후 재로그인하여 그룹 적용)
### 로그아웃
exit

### 다시 접속 (docker 명령어 이제 sudo 없이 사용 가능)
ssh -i "C:\Users\UESR\.ssh\recipemate-ec2-key.pem" ec2-user@13.125.48.36
docker --version  # 정상 동작 확인
```

### 2. Swap Memory 설정 (필수)
`t2.micro`는 RAM이 1GB라 빌드/실행 시 메모리 부족으로 멈출 수 있습니다. **반드시 설정하세요.**
```bash
sudo fallocate -l 2G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
# (선택) 재부팅 후에도 유지되도록 /etc/fstab 등록
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
free -h  # 확인 (Swap 2.0G 표시됨)
```

### 3. 프로젝트 배포
```bash
# 1. Git 클론
# git clone <your-repo-url>
git clone https://github.com/person3113/recipemate.git
cd recipemate  # 프로젝트 폴더로 이동

# 2. 환경변수 설정 (.env 파일 생성) (AWS용 값 입력)
vim .env
# i 눌러 삽입 모드 진입
# .env 내용 전체 복사 (Ctrl+A → Ctrl+C)
# Vim 창에 붙여넣기 (오른쪽 클릭 → 붙여넣기 또는 Shift+Insert)
# Esc 눌러 명령 모드로 나가기
# :wq 입력 → Enter (저장 후 종료)
cat .env  # 내용 확인

# 3. Docker Compose 실행 (build 포함)
docker compose up -d --build

# Docker Buildx 버전 문제입니다. Amazon Linux 2023의 Docker 기본 buildx가 구버전이라 docker compose up --build가 실패합니다
# Buildx 플러그인 다운로드 (최신 버전)
DOCKER_BUILDX_VERSION=$(curl -s https://api.github.com/repos/docker/buildx/releases/latest | grep '"tag_name"' | cut -d'"' -f4)
sudo curl -L "https://github.com/docker/buildx/releases/download/${DOCKER_BUILDX_VERSION}/buildx-${DOCKER_BUILDX_VERSION}.linux-amd64" \
  -o /usr/local/lib/docker/cli-plugins/docker-buildx

sudo chmod +x /usr/local/lib/docker/cli-plugins/docker-buildx
docker buildx version  # 0.17.1 확인

# 재빌드
docker compose down --volumes --rmi all --remove-orphans # 1. 기존 정리 (볼륨/DB 초기화)
docker compose up -d --build

# 4. 상태 확인
docker compose ps     # 모든 서비스 running 확인
docker compose logs -f app  # 앱 로그 실시간 확인

# 5. 초기화 스크립트 실행 (필요시)
# docker compose exec app ./gradlew flywayMigrate  # DB 마이그레이션

# 헬스체크
curl http://localhost:8080/actuator/health
```

### 4. docker-compose 수정했을 때
```bash
# 모든 컨테이너 중지 & 볼륨 확인 (DB 데이터 보존)
docker compose down

# 3. 이미지 완전 삭제 (캐시 무시)
docker compose build --no-cache --pull
# 또는 한 번에: docker compose up -d --build --force-recreate --pull

# 4. 재시작 (최신 코드 + docker-compose.yml 적용)
docker compose up -d --build --force-recreate

# 5. 상태 점검
docker compose ps          # Up 상태 확인
docker compose logs -f app # 앱 시작 로그 실시간 확인
```

- [x] **접속 확인:** 브라우저에서 `http://<Elastic-IP>:8080` 접속.
  - 예시: http://13.125.48.36:8080

---

## 🆙 4단계: 고도화 (Nginx & HTTPS) - *Next Step*

1차 배포 성공 후 진행하는 **레벨업 과제**입니다.

### 왜 필요한가요?
- **보안:** 8080 포트를 숨기고 표준 포트(80/443)만 노출.
- **HTTPS:** 보안 연결(SSL) 필수 시대 (Let's Encrypt 무료 인증서).
- **도메인:** IP 주소 대신 `recipemate.com` 같은 도메인 사용.

### 진행 가이드
1.  **도메인 구입 및 연결:** 가비아/Route53 등에서 도메인 구매 후 EIP와 연결.
2.  **Nginx 복구:** `docker-compose.yml`에서 Nginx 서비스 주석 해제.
3.  **Certbot 설정:** SSL 인증서 발급 자동화.
