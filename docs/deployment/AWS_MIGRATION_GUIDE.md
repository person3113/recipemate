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
- **OS:** Ubuntu Server 22.04 LTS 또는 24.04 LTS
- **Type:** `t3.micro` (Free Tier 지원 확인)
- **Key Pair:** 새 키 페어(`.pem`) 생성 및 안전한 곳에 보관
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
- [ ] EIP 할당 및 EC2 인스턴스 연결 (서버 재시작 시 IP 변경 방지).

---

## 🚀 3단계: 서버 설정 및 배포 (Terminal)

### 1. 기본 설정 & Docker 설치
```bash
# SSH 접속
ssh -i "path/to/key.pem" ubuntu@<Elastic-IP>

# 패키지 업데이트 및 Docker 설치
sudo apt update && sudo apt install -y docker.io docker-compose-plugin
sudo usermod -aG docker ubuntu
# (로그아웃 후 재로그인하여 그룹 적용)
```

### 2. Swap Memory 설정 (필수)
`t3.micro`는 RAM이 1GB라 빌드/실행 시 메모리 부족으로 멈출 수 있습니다. **반드시 설정하세요.**
```bash
sudo fallocate -l 2G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
# (선택) 재부팅 후에도 유지되도록 /etc/fstab 등록
```

### 3. 프로젝트 배포
- [ ] **코드 복제:** `git clone <your-repo-url>`
- [ ] **환경변수 설정:** `.env` 파일 생성 (AWS용 값 입력)
    ```bash
    nano .env
    # 내용 붙여넣기 -> Ctrl+O -> Enter -> Ctrl+X
    ```
- [ ] **실행:**
    ```bash
    docker compose up -d --build
    ```
- [ ] **로그 확인:** `docker compose logs -f`
- [ ] **접속 확인:** 브라우저에서 `http://<Elastic-IP>:8080` 접속.

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
4.  **보안 그룹 수정:** 8080 포트 인바운드 규칙 삭제 (Nginx를 통해서만 접속하도록 제한).
