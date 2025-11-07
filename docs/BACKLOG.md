# RecipeMate Backlog

> 미구현 기능, 기술 부채, 개선 사항 기록 및 우선순위별 관리

📝 원칙
- **TDD**: 백로그 항목도 테스트 먼저 작성
- **YAGNI**: 필요하지 않으면 구현하지 않기 (과도한 추상화 방지)
- **지속적 리팩터링**: 코드 냄새 느껴질 때 백로그 항목 처리

---

## 다른 팀원에게
-[]  프로필 수정 전화번호만 바꾸고 수정누르면 반영되긴 하는데. 프로필 이미지가 기존에 디폴트 이미지에서 null로 되는 느낌? 한 번 직접 해보고 상황 설명하면서 ai한테 알려주면 해결해줄거임. 

- [] 내가 작성한 댓글, 내가 작성한 포스트 => 이거 마이페이지에서 목록 볼 수 있게. 현재  바로가기 아래  내 찜 목록
  내 배지
  포인트 내역
  내가 만든 공구
  참여중인 공구
    - 이렇게 있는데 거기에 추가하는거지. ui는 다른 비슷한 용도의 템플릿 참고해서 일관성 유지하고.

- http://localhost:8080/recipes -> 이땐 정상인데. 페이지네이션으로 2페이지 누르니까
  http://localhost:8080/recipes?keyword=&category=&area=&source=&ingredients=&maxCalor
  ies=&maxCarbohydrate=&maxProtein=&maxFat=&maxSodium=&page=1&size=20
  이렇게 되어서그런가. 페이지 위에  "카테고리: "필터해제"" 표시 나오네. 불필요한데. 또
  필터해제 누르면 1페이지로 가네. 필터하고 페이지네이션은 별개로 보통 그러지 않나?

- 	FOOD_SAFETY 인 recipe는 5	INFO_WGT	중량(1인분)
     6	INFO_ENG	열량
     7	INFO_CAR	탄수화물
     8	INFO_PRO	단백질
     9	INFO_FAT	지방
     10	INFO_NA	나트륨
- 제공해주는데. db에 값이 있을 때(null이나 "" 아닐 때)만 영양정보에 포함시키는게 좋을 듯. 화면에.

---

## 🔴 HIGH Priority

---

## 🟡 MEDIUM Priority

-[] 포인트 로직 안 되는 느낌인데 한 번 점검해보고. 비즈니스로직에 맞게. 그 다음. 안 되면 왜 안되는지 점검하기.

-[] http://localhost:8080/recipes/food-1082 -> 영유아를 위한 고소한 닭꼬치 이건데. 닭고기(가슴살 - 적당량 / 25g) - 적당량 이렇게 저장되고 표시되네? ',' 기준으로 파싱해서 그런건가? 원래는 닭고기(가슴살, 25g) 이렇게 api가 제공해주는데. 이 부분 현실적으로 개선 가능한가? 
    - 고추잡채 -> 이거도 식용유(재료) - 5g[소스소개] 고기 밑간 양념:후춧가루 0.1g 이렇게 분리되어서 저장되네. api가 "돼지고기 30g, 청피망 30g, 죽순통조림 6g, 홍고추 1.4g, 표고버섯 7g, 소금 0.4g, 참기름 1g, 식용유 5g[소스소개] 고기 밑간 양념:후춧가루 0.1g, 정종 1g, 달걀 4g, 녹말가루 3g" 이렇게 제공해서 그런 듯. 
    - 이 부분은 api 제공 방식의 한계라 어쩔 수 없을 듯.
    - 감자냉채 ->오이- "10g [소스소개 단촛물:물60g" / 소금 -"0.3g 겨자초장:겨자가루 1.5g " 이렇게 분리 저장되네. api는 "감자 40g, 래디쉬 5g, 오이 10g [소스소개 단촛물:물60g, 식초 1.5g, 설탕 1.5g, 파인애플주스 1.5g, 소금 0.3g 겨자초장:겨자가루 1.5g, 물미지근한것 1g, 설탕 2g, 식초 3g, 소금 0.03g, 파인애플주스 1.6g", 이렇게 제공해주고. 

- 참여공구 목록 관련
  - http://localhost:8080/group-purchases/194/participants -> 참여자 관리 버튼 누른 거지. 그럼 No static resource group-purchases/194/participants. 404 이런 오류 페이지가 렌더링됨.
  - http://localhost:8080/api/group-purchases/194 -> 삭제 버튼 누르니까. 404. No static resource api/group-purchases/194. 


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

#### [ ] Task 4-5-7: 실시간 채팅 (선택 - 복잡도 높음)
=> // Option 1: 댓글 기능으로 대체 (권장)
- 구현 시간: 2-4시간
- 기존 지식으로 충분
- 충분히 실용적
- [ ] ~~WebSocket 설정~~
~~- [ ] ChatMessage 엔티티 작성~~
~~- [ ] ChatService 구현~~
~~- [ ] WebSocket 컨트롤러 작성~~
~~- [ ] 프론트엔드 통합~~
~~- [ ] 주의사항~~
    ~~- 복잡도가 높아 시간 여유가 충분할 때만 구현~~
    ~~- 대안: 기본 댓글 기능으로 대체 가능~~