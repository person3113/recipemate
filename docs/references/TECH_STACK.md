# 핵심 기술 스택

- **Backend**: Spring Boot, Java 21
- **Frontend**: Thymeleaf, Bootstrap, Vanilla JS
- **Database**: PostgreSQL 16 (프로덕션), H2 (개발)
- **Cache/Session**: Redis (조회수, 세션 등 확장 시 사용)
- **Container**: Docker, Docker Compose
- **Reverse Proxy**: Nginx
- **Host OS**: Windows + WSL2 (개발 환경) 또는 Linux (서버 환경)

# 📋 기술적 의사결정 사항

## 프론트엔드
- **템플릿 엔진**: Thymeleaf
- **스타일링**: Bootstrap (CDN)
- **JS**: htmx, Alpine.js, Vanilla JS + AJAX
- **JS 라이브러리 도입 결정**:
    - **배경**: 순수 Vanilla JS + AJAX 방식은 실시간 UI 변경 시 코드 복잡성 증가 및 생산성 저하 우려.
    - **결정**: 가벼운 JS 라이브러리를 도입하여 생산성 향상 및 코드 간소화.
        - **htmx (주력)**: 서버가 렌더링한 HTML 조각으로 페이지 일부를 교체하는 방식으로 AJAX를 간소화. 백엔드 중심 개발 흐름 유지.
        - **Alpine.js (보조)**: 서버 통신이 필요 없는 클라이언트 측 상호작용을 간결하게 처리.

## 데이터베이스
- **개발**: H2 (인메모리)
- **배포**: PostgreSQL 16

## 보안
- Spring Security + 세션 기반 인증

## 테스트 전략
- **단위 테스트**: 도메인 서비스 레벨
- **통합 테스트**: Repository, Controller 레벨
- **E2E 테스트**: 주요 플로우만

## 성능 최적화
- **페이징**: Offset 기반 (초기 구현)
- **캐싱**: Redis 활용 (API 응답, 자주 찾는 데이터)
- **인덱스**: 필요시 주요 조회 조건에 추가 (FK, 검색 필드 등)
