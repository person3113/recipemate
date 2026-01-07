# 데이터베이스 스키마 관리 및 생성 가이드

## 1. `schema.sql` 파일, 꼭 필요한가요?

**결론부터 말하면, 프로덕션(운영) 환경 배포 시에는 반드시 필요합니다.**

현재 프로젝트는 환경에 따라 DB 스키마 관리 방식이 다릅니다.

-   **개발 환경 (`dev` 프로필):**
    -   **DB:** H2 (파일 기반)
    -   **`ddl-auto` 설정:** `update`
    -   **동작:** 애플리케이션 실행 시 Hibernate가 **자동으로** JPA 엔티티와 DB 스키마의 차이를 비교하여 컬럼 추가/변경 등을 반영합니다. 따라서 `schema.sql` 파일이 없거나 오래된 버전이라도 로컬 개발에는 큰 문제가 없습니다.

-   **운영 환경 (`prod` 프로필):**
    -   **DB:** PostgreSQL
    -   **`ddl-auto` 설정:** `validate`
    -   **동작:** 애플리케이션 실행 시 Hibernate가 JPA 엔티티와 DB 스키마가 **정확히 일치하는지 검증**합니다. 만약 일치하지 않으면, **애플리케이션은 시작되지 않고 즉시 종료됩니다.**

Render 배포 가이드에서는 운영 환경(`prod` 프로필) 사용을 전제로 하므로, 사전에 DB에 **완벽하고 올바른 스키마가 생성되어 있어야만 합니다.** 바로 이 역할을 `schema.sql` 파일이 수행합니다.

> **요약:** `schema.sql`은 운영 환경에서 `validate` 옵션을 통과하기 위한 필수적인 '설계도'입니다. 이전에 분석했듯이 현재 프로젝트의 `schema.sql`은 매우 오래된 버전이므로 반드시 최신 버전으로 새로 만들어야 합니다.

---

## 2. 올바른 `schema.sql` 생성 방법

가장 간단하고 확실한 두 가지 방법을 안내해 드립니다. **첫 번째 방법을 가장 추천합니다.**

### 방법 1: H2 데이터베이스에서 스키마 추출 (가장 간단하고 확실한 방법)

로컬 개발 환경에서 Hibernate가 자동으로 만들어준 최신 스키마를 그대로 추출하는 방식입니다.

1.  **로컬에서 애플리케이션 실행**
    -   터미널에서 `./gradlew bootRun` 명령을 실행하여 `dev` 프로필로 애플리케이션을 시작합니다.
    -   이 과정에서 Hibernate가 최신 엔티티 코드에 맞춰 `./data/` 폴더 안의 H2 데이터베이스 파일 스키마를 자동으로 업데이트합니다.

2.  **H2 콘솔 접속**
    -   애플리케이션 실행 후, 웹 브라우저에서 `http://localhost:8080/h2-console` 로 접속합니다.

3.  **데이터베이스 연결**
    -   H2 콘솔 로그인 화면에서 **JDBC URL**을 `application.yml`에 있는 값과 동일하게 수정합니다.
    -   **JDBC URL:** `jdbc:h2:file:./data/recipemate`
    -   `Connect` 버튼을 클릭하여 연결합니다.

4.  **스키마 스크립트 생성**
    -   연결 후 나타나는 SQL 편집기 화면에 아래 명령어를 입력하고 `Run` 버튼을 클릭합니다.
        ```sql
        SCRIPT TO 'schema_new.sql'
        ```

5.  **`schema_new.sql` 파일 확인**
    -   프로젝트의 루트 디렉토리(최상위 폴더)에 `schema_new.sql` 파일이 생성됩니다.
    -   이 파일에는 현재 코드베이스와 100% 일치하는 모든 `CREATE TABLE`, `CREATE INDEX`, 제약조건 등이 포함되어 있습니다.

6.  **파일 정리 및 교체**
    -   `schema_new.sql` 파일을 열어 H2 데이터베이스 전용 명령어(예: `SET ...`)나 불필요한 주석 등을 정리합니다.
    -   정리된 내용을 기존의 오래된 `schema.sql` 파일에 덮어쓰거나, 이 파일을 새로운 `schema.sql`로 사용합니다. 이 파일이 바로 Render의 PostgreSQL에 적용할 최신 스키마입니다.

### 방법 2: Hibernate DDL 생성 기능 사용 (고급 방법)

애플리케이션을 완전히 실행하지 않고, Hibernate의 스키마 생성 기능을 이용해 DDL을 파일로 직접 만드는 방법입니다.

1.  **`application.yml` 임시 수정**
    -   `src/main/resources/application.yml` 파일의 `dev` 프로필(`on-profile: dev`) 설정 부분에 아래 속성을 **임시로 추가**합니다.
        ```yaml
        # --- 기존 dev 프로필 설정 아래에 추가 ---
        jpa:
          properties:
            javax:
              persistence:
                schema-generation:
                  scripts:
                    action: create
                    create-target: generated-schema.sql
        ```
    > **주의:** `spring.jpa.hibernate.ddl-auto`가 `update` 또는 `create`로 설정되어 있어야 동작합니다. (현재 `dev` 프로필은 `update`이므로 OK)

2.  **애플리케이션 1회 실행**
    -   `./gradlew bootRun`으로 애플리케이션을 실행합니다. 시작 과정에서 Hibernate가 `generated-schema.sql` 파일을 프로젝트 루트에 생성합니다.
    -   파일이 생성된 것을 확인했으면 애플리케이션을 바로 종료해도 됩니다.

3.  **`application.yml` 원상 복구**
    -   **매우 중요:** 추가했던 `jpa.properties.javax.persistence...` 설정을 **반드시 다시 삭제**하여 `application.yml`을 원래 상태로 되돌립니다. 그대로 두면 실행할 때마다 파일이 계속 생성됩니다.

---

## 3. 최종 결론 및 추천 워크플로우

1.  **결론:** 운영 배포를 위해 **최신 `schema.sql` 파일은 반드시 필요**합니다.
2.  **추천 방법:** **"방법 1: H2 데이터베이스에서 스키마 추출"** 방식을 사용하세요. 가장 직관적이고 실수가 적으며, 현재 프로젝트 환경에 가장 잘 맞는 방법입니다.
3.  **워크플로우:**
    1.  H2 콘솔(`SCRIPT TO ...`)을 통해 최신 스키마를 `schema_new.sql` 파일로 추출합니다.
    2.  이 파일의 내용을 기존 `schema.sql`에 덮어쓰기하여 최신화합니다.
    3.  향후 Render에 PostgreSQL을 배포할 때, 이 최신화된 `schema.sql`을 사용하여 데이터베이스를 초기 설정합니다.