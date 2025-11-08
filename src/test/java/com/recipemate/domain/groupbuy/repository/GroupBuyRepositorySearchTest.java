//package com.recipemate.domain.groupbuy.repository;
//
//import com.recipemate.domain.groupbuy.dto.GroupBuySearchCondition;
//import com.recipemate.domain.groupbuy.entity.GroupBuy;
//import com.recipemate.domain.user.entity.User;
//import com.recipemate.domain.user.repository.UserRepository;
//import com.recipemate.global.common.DeliveryMethod;
//import com.recipemate.global.common.GroupBuyStatus;
//import com.recipemate.global.common.UserRole;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.context.annotation.Import;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//
//import java.time.LocalDateTime;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@DataJpaTest
//@Import(com.recipemate.global.config.QueryDslConfig.class)
//@DisplayName("GroupBuyRepository 검색 기능 테스트")
//class GroupBuyRepositorySearchTest {
//
//    @Autowired
//    private GroupBuyRepository groupBuyRepository;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    private User host;
//    private Pageable pageable;
//
//    @BeforeEach
//    void setUp() {
//        // 테스트용 사용자 생성
//        host = User.builder()
//                .email("host@test.com")
//                .password("password123")
//                .nickname("테스트호스트")
//                .phoneNumber("010-1234-5678")
//                .mannerTemperature(36.5)
//                .role(UserRole.USER)
//                .build();
//        userRepository.save(host);
//
//        pageable = PageRequest.of(0, 10);
//
//        // 테스트 데이터 생성
//        createTestGroupBuys();
//    }
//
//    private void createTestGroupBuys() {
//        // 1. 제목에 "김치" 포함, 카테고리 "식재료", 모집중
//        GroupBuy groupBuy1 = GroupBuy.builder()
//                .host(host)
//                .title("맛있는 김치 공동구매")
//                .content("품질 좋은 김치를 함께 구매해요")
//                .category("식재료")
//                .totalPrice(50000)
//                .targetHeadcount(5)
//                .currentHeadcount(2)
//                .deadline(LocalDateTime.now().plusDays(7))
//                .deliveryMethod(DeliveryMethod.BOTH)
//                .meetupLocation("학교 정문")
//                .parcelFee(3000)
//                .isParticipantListPublic(true)
//                .status(GroupBuyStatus.RECRUITING)
//                .build();
//
//        // 2. 내용에 "김치" 포함, 카테고리 "식재료", 모집중
//        GroupBuy groupBuy2 = GroupBuy.builder()
//                .host(host)
//                .title("배추 공동구매")
//                .content("김치 담그실 분들 모여요!")
//                .category("식재료")
//                .totalPrice(30000)
//                .targetHeadcount(4)
//                .currentHeadcount(1)
//                .deadline(LocalDateTime.now().plusDays(5))
//                .deliveryMethod(DeliveryMethod.DIRECT)
//                .meetupLocation("학교 후문")
//                .isParticipantListPublic(true)
//                .status(GroupBuyStatus.RECRUITING)
//                .build();
//
//        // 3. 제목에 "과자" 포함, 카테고리 "간식", 마감임박
//        GroupBuy groupBuy3 = GroupBuy.builder()
//                .host(host)
//                .title("수입 과자 공동구매")
//                .content("해외 직구 과자 모집")
//                .category("간식")
//                .totalPrice(80000)
//                .targetHeadcount(10)
//                .currentHeadcount(8)
//                .deadline(LocalDateTime.now().plusDays(1))
//                .deliveryMethod(DeliveryMethod.PARCEL)
//                .parcelFee(5000)
//                .isParticipantListPublic(true)
//                .status(GroupBuyStatus.IMMINENT)
//                .build();
//
//        // 4. 레시피 기반 공구, 카테고리 "식재료", 모집중
//        GroupBuy groupBuy4 = GroupBuy.builder()
//                .host(host)
//                .title("파스타 재료 공동구매")
//                .content("카르보나라 레시피 재료")
//                .category("식재료")
//                .totalPrice(40000)
//                .targetHeadcount(6)
//                .currentHeadcount(3)
//                .deadline(LocalDateTime.now().plusDays(10))
//                .deliveryMethod(DeliveryMethod.BOTH)
//                .meetupLocation("학교 정문")
//                .parcelFee(3000)
//                .isParticipantListPublic(true)
//                .status(GroupBuyStatus.RECRUITING)
//                .recipeApiId("52771")
//                .recipeName("Spicy Arrabiata Penne")
//                .recipeImageUrl("https://example.com/pasta.jpg")
//                .build();
//
//        // 5. 마감된 공구
//        GroupBuy groupBuy5 = GroupBuy.builder()
//                .host(host)
//                .title("치킨 공동구매")
//                .content("치킨 먹고싶은 사람")
//                .category("식품")
//                .totalPrice(60000)
//                .targetHeadcount(3)
//                .currentHeadcount(3)
//                .deadline(LocalDateTime.now().minusDays(1))
//                .deliveryMethod(DeliveryMethod.DIRECT)
//                .meetupLocation("학교 앞")
//                .isParticipantListPublic(true)
//                .status(GroupBuyStatus.CLOSED)
//                .build();
//
//        groupBuyRepository.save(groupBuy1);
//        groupBuyRepository.save(groupBuy2);
//        groupBuyRepository.save(groupBuy3);
//        groupBuyRepository.save(groupBuy4);
//        groupBuyRepository.save(groupBuy5);
//    }
//
//    @Test
//    @DisplayName("제목으로 검색 - '김치' 키워드")
//    void searchByTitleKeyword() {
//        // given
//        GroupBuySearchCondition condition = GroupBuySearchCondition.builder()
//                .keyword("김치")
//                .build();
//
//        // when
//        Page<GroupBuy> result = groupBuyRepository.searchGroupBuys(condition, pageable);
//
//        // then
//        assertThat(result.getContent()).hasSize(2); // 제목 또는 내용에 '김치' 포함
//        assertThat(result.getContent())
//                .extracting(GroupBuy::getTitle)
//                .containsAnyOf("맛있는 김치 공동구매", "배추 공동구매");
//    }
//
//    @Test
//    @DisplayName("내용으로 검색 - '과자' 키워드")
//    void searchByContentKeyword() {
//        // given
//        GroupBuySearchCondition condition = GroupBuySearchCondition.builder()
//                .keyword("과자")
//                .build();
//
//        // when
//        Page<GroupBuy> result = groupBuyRepository.searchGroupBuys(condition, pageable);
//
//        // then
//        assertThat(result.getContent()).hasSize(1);
//        assertThat(result.getContent().get(0).getTitle()).isEqualTo("수입 과자 공동구매");
//    }
//
//    @Test
//    @DisplayName("카테고리 필터링 - '식재료' 카테고리만")
//    void searchByCategory() {
//        // given
//        GroupBuySearchCondition condition = GroupBuySearchCondition.builder()
//                .category("식재료")
//                .build();
//
//        // when
//        Page<GroupBuy> result = groupBuyRepository.searchGroupBuys(condition, pageable);
//
//        // then
//        assertThat(result.getContent()).hasSize(3); // 식재료 카테고리 3개
//        assertThat(result.getContent())
//                .allMatch(gb -> gb.getCategory().equals("식재료"));
//    }
//
//    @Test
//    @DisplayName("상태 필터링 - 모집중(RECRUITING) 상태만")
//    void searchByStatus() {
//        // given
//        GroupBuySearchCondition condition = GroupBuySearchCondition.builder()
//                .status(GroupBuyStatus.RECRUITING)
//                .build();
//
//        // when
//        Page<GroupBuy> result = groupBuyRepository.searchGroupBuys(condition, pageable);
//
//        // then
//        assertThat(result.getContent()).hasSize(3); // RECRUITING 상태 3개
//        assertThat(result.getContent())
//                .allMatch(gb -> gb.getStatus() == GroupBuyStatus.RECRUITING);
//    }
//
//    @Test
//    @DisplayName("레시피 기반 공구만 필터링")
//    void searchRecipeBasedOnly() {
//        // given
//        GroupBuySearchCondition condition = GroupBuySearchCondition.builder()
//                .recipeOnly(true)
//                .build();
//
//        // when
//        Page<GroupBuy> result = groupBuyRepository.searchGroupBuys(condition, pageable);
//
//        // then
//        assertThat(result.getContent()).hasSize(1);
//        assertThat(result.getContent().get(0).getRecipeApiId()).isNotNull();
//        assertThat(result.getContent().get(0).getTitle()).isEqualTo("파스타 재료 공동구매");
//    }
//
//    @Test
//    @DisplayName("복합 조건 검색 - 키워드 + 카테고리 + 상태")
//    void searchWithMultipleConditions() {
//        // given
//        GroupBuySearchCondition condition = GroupBuySearchCondition.builder()
//                .keyword("김치")
//                .category("식재료")
//                .status(GroupBuyStatus.RECRUITING)
//                .build();
//
//        // when
//        Page<GroupBuy> result = groupBuyRepository.searchGroupBuys(condition, pageable);
//
//        // then
//        assertThat(result.getContent()).hasSize(2);
//        assertThat(result.getContent())
//                .allMatch(gb -> gb.getCategory().equals("식재료"))
//                .allMatch(gb -> gb.getStatus() == GroupBuyStatus.RECRUITING)
//                .allMatch(gb -> gb.getTitle().contains("김치") || gb.getContent().contains("김치"));
//    }
//
//    @Test
//    @DisplayName("복합 조건 검색 - 카테고리 + 레시피 기반")
//    void searchByCategoryAndRecipeOnly() {
//        // given
//        GroupBuySearchCondition condition = GroupBuySearchCondition.builder()
//                .category("식재료")
//                .recipeOnly(true)
//                .build();
//
//        // when
//        Page<GroupBuy> result = groupBuyRepository.searchGroupBuys(condition, pageable);
//
//        // then
//        assertThat(result.getContent()).hasSize(1);
//        assertThat(result.getContent().get(0).getCategory()).isEqualTo("식재료");
//        assertThat(result.getContent().get(0).getRecipeApiId()).isNotNull();
//    }
//
//    @Test
//    @DisplayName("검색 조건 없음 - 전체 조회 (삭제된 것 제외)")
//    void searchWithNoCondition() {
//        // given
//        GroupBuySearchCondition condition = GroupBuySearchCondition.builder().build();
//
//        // when
//        Page<GroupBuy> result = groupBuyRepository.searchGroupBuys(condition, pageable);
//
//        // then
//        assertThat(result.getContent()).hasSize(5); // 총 5개 공구
//    }
//
//    @Test
//    @DisplayName("검색 결과 없음")
//    void searchWithNoResult() {
//        // given
//        GroupBuySearchCondition condition = GroupBuySearchCondition.builder()
//                .keyword("존재하지않는키워드12345")
//                .build();
//
//        // when
//        Page<GroupBuy> result = groupBuyRepository.searchGroupBuys(condition, pageable);
//
//        // then
//        assertThat(result.getContent()).isEmpty();
//    }
//}
