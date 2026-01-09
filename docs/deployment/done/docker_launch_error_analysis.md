# Docker 실행 오류 분석 및 해결 가이드

## 1. 실무적 관점의 DB 초기화 전략 (Q&A)

### Q1. 실무에서 `init-db.sql`과 `schema.sql`을 분리하나요?
**네, 분리하는 것이 표준(Best Practice)입니다.**
*   **`init-db.sql` (환경 설정)**: 데이터베이스 생성, 사용자 계정 추가, 권한 부여(`GRANT`), 확장 기능(`Extension`) 활성화 등 **DB 서버 차원**의 설정을 담습니다. 한 번 설정하면 잘 바뀌지 않습니다.
*   **`schema.sql` (구조 정의)**: `CREATE TABLE`, `INDEX`, `CONSTRAINT` 등 **애플리케이션 데이터 구조**를 담습니다. 개발 과정에서 빈번하게 변경됩니다.

### Q2. `init-db.sql`의 위치는 어디가 좋은가요?
**프로젝트 최상위(Root)가 가장 좋습니다.**
*   `Dockerfile`, `docker-compose.yml`과 함께 최상위에 위치시켜, 이 프로젝트가 Docker 환경에서 어떻게 초기화되는지 한눈에 파악할 수 있게 하는 것이 관례입니다.
*   현재처럼 깊은 경로(`docs/references/...`)에 숨겨져 있으면, CI/CD 파이프라인이나 다른 개발자가 설정을 찾기 어렵습니다.

### Q3. `schema.sql` 내용을 `init-db.sql`로 옮겨야 하나요?
**아니요, 옮기지 말고 Docker가 둘 다 실행하도록 설정해야 합니다.**
*   `schema.sql`은 Docker 뿐만 아니라 테스트 코드, 로컬 H2 DB 등 다른 환경에서도 재사용되는 순수한 "설계도"입니다.
*   **해결책**: Docker Compose에서 두 파일을 모두 마운트하되, **파일 이름 앞에 숫자를 붙여 실행 순서를 제어**하는 방식을 사용합니다. (예: `01-init.sql`, `02-schema.sql`)

---

## 2. 발생한 오류 심층 분석

### 오류 1: PostgreSQL 초기화 스크립트 경로 에러
*   **로그**: `psql:/docker-entrypoint-initdb.d/init-db.sql: error: could not read from input file: Is a directory`
*   **원인**: `docker-compose.yml`은 프로젝트 루트(`.`/)에 `init-db.sql`이 있다고 가정했으나, 실제 파일은 없었습니다. Docker는 파일이 없으면 자동으로 디렉토리를 생성해버리기 때문에, `psql`이 디렉토리를 읽으려다 에러가 났습니다.

### 오류 2: Spring Boot 실행 실패 (`missing table`)
*   **로그**: `SchemaManagementException: Schema-validation: missing table [addresses]`
*   **원인**: `init-db.sql`에는 테이블 생성 쿼리가 없고, 정작 테이블 정의가 담긴 `schema.sql`은 Docker에 로드되지 않았습니다. 빈 DB 상태에서 애플리케이션이 켜지면서 검증(`validate`) 에러가 발생했습니다.

---

## 3. 최종 해결 솔루션

파일 위치를 관례에 맞게 정리하고, 실행 순서를 보장하는 설정을 적용합니다.

### 단계 1: 파일 이동 (정리)
깊숙이 있는 초기화 파일을 프로젝트 루트로 꺼내옵니다.
*   **이동 대상**: `docs/references/db/init-db.sql` -> `./init-db.sql` (프로젝트 루트)

### 단계 2: `docker-compose.yml` 수정
두 SQL 파일을 Docker 컨테이너의 초기화 폴더로 마운트합니다. 이때, **숫자 접두어**를 사용하여 실행 순서를 강제합니다.

**변경 전:**
```yaml
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./init-db.sql:/docker-entrypoint-initdb.d/init-db.sql:ro
```

**변경 후:**
```yaml
    volumes:
      - postgres_data:/var/lib/postgresql/data
      # 1. 초기 설정 (계정, 권한 등) - 루트로 이동된 파일 사용
      - ./init-db.sql:/docker-entrypoint-initdb.d/01-init.sql:ro
      # 2. 스키마 정의 (테이블 생성 등)
      - ./schema.sql:/docker-entrypoint-initdb.d/02-schema.sql:ro
```
> **원리**: `/docker-entrypoint-initdb.d/` 폴더 내의 `.sql` 파일들은 알파벳/숫자 순서대로 자동 실행됩니다.

### 단계 3: Docker 환경 재설정 (필수)
잘못된 초기화 데이터가 남아있으므로 반드시 볼륨을 삭제하고 재시작해야 합니다.

1.  **완전 초기화 및 종료**:
    ```bash
    docker compose down -v
    ```
2.  **재빌드 및 실행**:
    ```bash
    docker compose up -d --build
    ```

### 단계 4: 검증
로그에서 두 스크립트가 순서대로 실행되는지 확인합니다.
```bash
docker compose logs -f postgres
```
정상적이라면 `01-init.sql` 실행 후 `02-schema.sql`이 실행되면서 `CREATE TABLE` 로그들이 나타납니다.
