# RecipeMate Backlog

> 미구현 기능, 기술 부채, 개선 사항 기록 및 우선순위별 관리

📝 원칙
- **TDD**: 백로그 항목도 테스트 먼저 작성
- **YAGNI**: 필요하지 않으면 구현하지 않기 (과도한 추상화 방지)
- **지속적 리팩터링**: 코드 냄새 느껴질 때 백로그 항목 처리

---

## 다른 팀원에게

---

## 🔴 HIGH Priority

---

## 🟡 MEDIUM Priority


---

## 🟢 LOW Priority

- 3.4. 성능 문제 (N+1)
  - **현황**: `application.yml`에 `default_batch_fetch_size: 100`이 이미 설정되어 있는 것을 확인했습니다. 이는 지연 로딩 시 N+1 문제를 완화하는 매우 효과적인 설정입니다.
  - **문제점**: 하지만 이 설정만으로 모든 N+1 문제가 해결되지는 않습니다. `UserService`의 `getMyGroupBuys`와 같이, `Page` 객체를 조회한 후 연관된 이미지 목록을 가져오기 위해 `groupBuyImageRepository.findByGroupBuyIdIn...`을 추가로 호출하는 패턴은 `batch_fetch_size`의 직접적인 적용 대상이 아니며, 여전히 별도의 쿼리를 발생시킵니다.
  - **제안**:
      1. **- [ ] `@EntityGraph` 활용**: Repository 메서드에 `@EntityGraph`를 사용하여 조회 시점에 함께 가져올 연관 엔티티를 명시적으로 지정할 수 있습니다. 이는 `batch_fetch_size`보다 더 명시적이고 확실한 해결책이 될 수 있습니다.

- **`ImageUploadUtil.java` & `ImageOptimizationService.java`**
    - **문제점**: 이미지 업로드 시 `ImageOptimizationService`를 통해 동기적으로 이미지를 최적화하고 있습니다. 이미지 처리(특히 리사이징, 압축)는 CPU를 많이 사용하는 작업이므로, 요청량이 많아지거나 큰 이미지를 처리할 때 요청 처리 스레드를 오래 점유하여 전체 애플리케이션의 응답성을 저하시킬 수 있습니다.
    - **제안**: 
        - [ ] 이미지 업로드 및 최적화 과정을 비동기(Asynchronous)로 처리하는 것을 권장합니다. `@Async` 어노테이션과 별도의 스레드 풀을 사용하여 이미지 처리를 백그라운드에서 수행하면, 사용자는 더 빠른 응답을 받을 수 있고 시스템의 처리량이 향상됩니다.
        - [ ] 원본 + 썸네일 2종 저장 (향후 개선)
        - [ ] 실제 WebP 형식 변환 (webp-imageio 라이브러리 추가)
        - [ ] CDN 연동 고려 (향후 개선)

- **`CacheConfig.java`**
    - **문제점**: `PostService`의 게시글 목록 캐시 키가 `category`, `keyword`, `pageNumber`, `pageSize`를 모두 조합하여 생성됩니다. 이는 잠재적으로 수많은 캐시 키를 생성하여 메모리를 비효율적으로 사용하고, 캐시 적중률(Hit Ratio)을 떨어뜨릴 수 있습니다.
    - **제안**: **- [ ] 검색 결과처럼 파라미터 조합이 다양한 경우, 캐싱의 효율이 떨어지므로 전략을 재검토해야 합니다.** 예를 들어, 자주 바뀌지 않는 첫 페이지만 캐싱하거나, 캐시 키를 단순화(예: 키워드의 해시값 사용)하는 방안을 고려해야 합니다. 또한 `@CacheEvict(allEntries = true)`는 관련 없는 캐시까지 모두 삭제하므로, 특정 캐시 항목만 선별적으로 제거하는 로직으로 개선하는 것이 좋습니다.

## ⚪ VERY LOW Priority

#### [ ] Task 4-5-6: 번역 API 연동 (선택)
시간 빠듯해서 못할 듯?
~~- [ ] 테스트 작성~~
    ~~- TheMealDB 영문 레시피명 → 한글 번역~~
    ~~- 재료명 번역 캐싱~~
~~- [ ] Google Translation API 설정~~
    ~~- API Key 발급 및 환경변수 설정~~
    ~~- 번역 클라이언트 작성~~
~~- [ ] TranslationService 구현~~
    ~~- `translateRecipeName(String englishName)`~~
    ~~- `translateIngredient(String ingredient)`~~
    ~~- 번역 결과 캐싱 (Redis 또는 로컬 캐시)~~
~~- [ ] RecipeService에 번역 로직 통합~~
    ~~- 레시피 조회 시 자동 번역 옵션~~
    ~~- 사용자 언어 설정에 따라 번역 제공~~

