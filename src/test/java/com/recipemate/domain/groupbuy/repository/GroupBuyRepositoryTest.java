package com.recipemate.domain.groupbuy.repository;

import com.recipemate.domain.groupbuy.entity.GroupBuy;
import com.recipemate.domain.user.entity.User;
import com.recipemate.domain.user.repository.UserRepository;
import com.recipemate.global.common.DeliveryMethod;
import com.recipemate.global.common.GroupBuyStatus;
import com.recipemate.global.common.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(com.recipemate.global.config.QueryDslConfig.class)
@ActiveProfiles("test")
@DisplayName("GroupBuyRepository 통합 테스트")
class GroupBuyRepositoryTest {

    @Autowired
    private GroupBuyRepository groupBuyRepository;

    @Autowired
    private UserRepository userRepository;

    private User host;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        groupBuyRepository.deleteAll();

        host = User.builder()
                .email("host@example.com")
                .password("password")
                .nickname("test-host")
                .phoneNumber("010-1234-5678")
                .role(UserRole.USER)
                .build();
        userRepository.save(host);
    }

    @Test
    @DisplayName("공동구매를 저장하고 ID로 조회할 수 있다.")
    void saveAndFindById() {
        // given
        GroupBuy groupBuy = GroupBuy.builder()
                .host(host)
                .title("Test Group Buy")
                .content("This is a test.")
                .category("FOOD")
                .totalPrice(10000)
                .targetHeadcount(5)
                .deadline(LocalDateTime.now().plusDays(3))
                .deliveryMethod(DeliveryMethod.DIRECT)
                .status(GroupBuyStatus.RECRUITING)
                .build();

        // when
        GroupBuy savedGroupBuy = groupBuyRepository.save(groupBuy);
        GroupBuy foundGroupBuy = groupBuyRepository.findById(savedGroupBuy.getId()).orElse(null);

        // then
        assertThat(foundGroupBuy).isNotNull();
        assertThat(foundGroupBuy.getId()).isEqualTo(savedGroupBuy.getId());
        assertThat(foundGroupBuy.getTitle()).isEqualTo("Test Group Buy");
        assertThat(foundGroupBuy.getHost().getNickname()).isEqualTo("test-host");
    }

    @Test
    @DisplayName("모집중 상태인 공동구매를 마감일 오름차순으로 조회한다.")
    void findByStatusOrderByDeadlineAsc() {
        // given
        GroupBuy groupBuy1 = createGroupBuy("title1", GroupBuyStatus.RECRUITING, LocalDateTime.now().plusDays(2));
        GroupBuy groupBuy2 = createGroupBuy("title2", GroupBuyStatus.RECRUITING, LocalDateTime.now().plusDays(1));
        GroupBuy groupBuy3 = createGroupBuy("title3", GroupBuyStatus.CLOSED, LocalDateTime.now().plusDays(3));
        groupBuyRepository.saveAll(List.of(groupBuy1, groupBuy2, groupBuy3));

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<GroupBuy> result = groupBuyRepository.findByStatusOrderByDeadlineAsc(GroupBuyStatus.RECRUITING, pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("title2");
        assertThat(result.getContent().get(1).getTitle()).isEqualTo("title1");
    }

    @Test
    @DisplayName("특정 호스트가 생성한 공동구매 목록을 조회한다.")
    void findByHostId() {
        // given
        User anotherHost = User.builder()
                .email("another@example.com")
                .password("password")
                .nickname("another-host")
                .phoneNumber("010-8765-4321")
                .role(UserRole.USER)
                .build();
        userRepository.save(anotherHost);

        GroupBuy groupBuy1 = createGroupBuy("my-group-buy-1", host, GroupBuyStatus.RECRUITING, LocalDateTime.now().plusDays(1));
        GroupBuy groupBuy2 = createGroupBuy("another-group-buy", anotherHost, GroupBuyStatus.RECRUITING, LocalDateTime.now().plusDays(2));
        GroupBuy groupBuy3 = createGroupBuy("my-group-buy-2", host, GroupBuyStatus.CLOSED, LocalDateTime.now().plusDays(3));
        groupBuyRepository.saveAll(List.of(groupBuy1, groupBuy2, groupBuy3));

        // when
        Page<GroupBuy> result = groupBuyRepository.findByHostId(host.getId(), Pageable.unpaged());

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(GroupBuy::getTitle).containsExactlyInAnyOrder("my-group-buy-1", "my-group-buy-2");
        assertThat(result).allMatch(gb -> gb.getHost().getId().equals(host.getId()));
    }

    // ========== 인덱스 활용 조회 테스트 ==========

    @Test
    @DisplayName("레시피 API ID로 공동구매 목록을 조회한다 (idx_groupbuy_recipe_api_id 인덱스 활용)")
    void findByRecipeApiId() {
        // given
        GroupBuy recipeGroupBuy1 = GroupBuy.builder()
                .host(host)
                .title("Recipe Group Buy 1")
                .content("Recipe based")
                .category("FOOD")
                .totalPrice(10000)
                .targetHeadcount(5)
                .deadline(LocalDateTime.now().plusDays(3))
                .deliveryMethod(DeliveryMethod.DIRECT)
                .status(GroupBuyStatus.RECRUITING)
                .recipeApiId("RECIPE_123")
                .recipeName("Recipe Name")
                .recipeImageUrl("https://example.com/recipe.jpg")
                .build();

        GroupBuy recipeGroupBuy2 = GroupBuy.builder()
                .host(host)
                .title("Recipe Group Buy 2")
                .content("Another recipe based")
                .category("FOOD")
                .totalPrice(15000)
                .targetHeadcount(4)
                .deadline(LocalDateTime.now().plusDays(5))
                .deliveryMethod(DeliveryMethod.PARCEL)
                .status(GroupBuyStatus.RECRUITING)
                .recipeApiId("RECIPE_123")
                .recipeName("Recipe Name")
                .recipeImageUrl("https://example.com/recipe.jpg")
                .build();

        GroupBuy normalGroupBuy = createGroupBuy("Normal Group Buy", GroupBuyStatus.RECRUITING, LocalDateTime.now().plusDays(2));
        groupBuyRepository.saveAll(List.of(recipeGroupBuy1, recipeGroupBuy2, normalGroupBuy));

        // when
        List<GroupBuy> result = groupBuyRepository.findByRecipeApiId("RECIPE_123");

        // then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(gb -> "RECIPE_123".equals(gb.getRecipeApiId()));
        assertThat(result).extracting(GroupBuy::getTitle).containsExactlyInAnyOrder("Recipe Group Buy 1", "Recipe Group Buy 2");
    }

    @Test
    @DisplayName("카테고리와 상태로 공동구매를 조회한다 (idx_groupbuy_category 인덱스 활용)")
    void findByCategoryAndStatus() {
        // given
        GroupBuy foodGroupBuy1 = createGroupBuyWithCategory("Food 1", "FOOD", GroupBuyStatus.RECRUITING, LocalDateTime.now().plusDays(1));
        GroupBuy foodGroupBuy2 = createGroupBuyWithCategory("Food 2", "FOOD", GroupBuyStatus.RECRUITING, LocalDateTime.now().plusDays(2));
        GroupBuy clothesGroupBuy = createGroupBuyWithCategory("Clothes 1", "CLOTHES", GroupBuyStatus.RECRUITING, LocalDateTime.now().plusDays(3));
        GroupBuy closedFoodGroupBuy = createGroupBuyWithCategory("Food 3", "FOOD", GroupBuyStatus.CLOSED, LocalDateTime.now().plusDays(4));
        groupBuyRepository.saveAll(List.of(foodGroupBuy1, foodGroupBuy2, clothesGroupBuy, closedFoodGroupBuy));

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<GroupBuy> result = groupBuyRepository.findByCategoryAndStatus("FOOD", GroupBuyStatus.RECRUITING, pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(gb -> "FOOD".equals(gb.getCategory()) && gb.getStatus() == GroupBuyStatus.RECRUITING);
        assertThat(result.getContent()).extracting(GroupBuy::getTitle).containsExactlyInAnyOrder("Food 1", "Food 2");
    }

    @Test
    @DisplayName("레시피 기반 공동구매만 조회한다 (레시피 API ID NULL 체크)")
    void findRecipeBasedGroupBuys() {
        // given
        GroupBuy recipeGroupBuy1 = createRecipeBasedGroupBuy("Recipe 1", "RECIPE_1", GroupBuyStatus.RECRUITING);
        GroupBuy recipeGroupBuy2 = createRecipeBasedGroupBuy("Recipe 2", "RECIPE_2", GroupBuyStatus.RECRUITING);
        GroupBuy normalGroupBuy = createGroupBuy("Normal", GroupBuyStatus.RECRUITING, LocalDateTime.now().plusDays(2));
        groupBuyRepository.saveAll(List.of(recipeGroupBuy1, recipeGroupBuy2, normalGroupBuy));

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<GroupBuy> result = groupBuyRepository.findRecipeBasedGroupBuys(GroupBuyStatus.RECRUITING, pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(gb -> gb.getRecipeApiId() != null);
        assertThat(result.getContent()).extracting(GroupBuy::getTitle).containsExactlyInAnyOrder("Recipe 1", "Recipe 2");
    }

    @Test
    @DisplayName("상태와 마감일 범위로 마감임박 공동구매를 조회한다 (idx_groupbuy_status_deadline 복합 인덱스 활용)")
    void findImminentGroupBuys() {
        // given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime in12Hours = now.plusHours(12);
        LocalDateTime in20Hours = now.plusHours(20);
        LocalDateTime in30Hours = now.plusHours(30);

        GroupBuy imminent1 = createGroupBuy("Imminent 1", GroupBuyStatus.RECRUITING, in12Hours);
        GroupBuy imminent2 = createGroupBuy("Imminent 2", GroupBuyStatus.RECRUITING, in20Hours);
        GroupBuy notImminent = createGroupBuy("Not Imminent", GroupBuyStatus.RECRUITING, in30Hours);
        GroupBuy closedImminent = createGroupBuy("Closed Imminent", GroupBuyStatus.CLOSED, in12Hours);
        groupBuyRepository.saveAll(List.of(imminent1, imminent2, notImminent, closedImminent));

        // when - 24시간 이내 마감 공구 조회
        List<GroupBuy> result = groupBuyRepository.findImminentGroupBuys(
                GroupBuyStatus.RECRUITING,
                now,
                now.plusHours(24)
        );

        // then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(gb -> gb.getStatus() == GroupBuyStatus.RECRUITING);
        assertThat(result).allMatch(gb -> gb.getDeadline().isAfter(now) && gb.getDeadline().isBefore(now.plusHours(24)));
        assertThat(result).extracting(GroupBuy::getTitle).containsExactlyInAnyOrder("Imminent 1", "Imminent 2");
    }

    @Test
    @DisplayName("키워드로 제목 또는 내용을 검색한다")
    void searchByKeyword() {
        // given
        GroupBuy groupBuy1 = createGroupBuyWithContent("Apple Product", "Fresh apples", GroupBuyStatus.RECRUITING);
        GroupBuy groupBuy2 = createGroupBuyWithContent("Orange Juice", "Made from fresh oranges", GroupBuyStatus.RECRUITING);
        GroupBuy groupBuy3 = createGroupBuyWithContent("Banana", "Yellow bananas", GroupBuyStatus.RECRUITING);
        groupBuyRepository.saveAll(List.of(groupBuy1, groupBuy2, groupBuy3));

        Pageable pageable = PageRequest.of(0, 10);

        // when - "apple" 키워드로 검색
        Page<GroupBuy> result = groupBuyRepository.searchByKeyword("apple", GroupBuyStatus.RECRUITING, pageable);

        // then - 제목에 "Apple"이 있거나 내용에 "apples"가 있는 공구가 조회됨
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).contains("Apple");
    }

    @Test
    @DisplayName("참여자 수로 내림차순 정렬하여 조회한다")
    void findByStatusOrderByParticipantsDesc() {
        // given
        GroupBuy groupBuy1 = createGroupBuy("Popular", GroupBuyStatus.RECRUITING, LocalDateTime.now().plusDays(3));
        groupBuy1.increaseParticipant();
        groupBuy1.increaseParticipant();

        GroupBuy groupBuy2 = createGroupBuy("Less Popular", GroupBuyStatus.RECRUITING, LocalDateTime.now().plusDays(4));
        groupBuy2.increaseParticipant();

        GroupBuy groupBuy3 = createGroupBuy("Not Popular", GroupBuyStatus.RECRUITING, LocalDateTime.now().plusDays(5));

        groupBuyRepository.saveAll(List.of(groupBuy1, groupBuy2, groupBuy3));

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<GroupBuy> result = groupBuyRepository.findByStatusOrderByParticipantsDesc(GroupBuyStatus.RECRUITING, pageable);

        // then - 참여자 많은 순서대로 정렬
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Popular");
        assertThat(result.getContent().get(0).getCurrentHeadcount()).isEqualTo(2);
        assertThat(result.getContent().get(1).getTitle()).isEqualTo("Less Popular");
        assertThat(result.getContent().get(1).getCurrentHeadcount()).isEqualTo(1);
        assertThat(result.getContent().get(2).getTitle()).isEqualTo("Not Popular");
        assertThat(result.getContent().get(2).getCurrentHeadcount()).isEqualTo(0);
    }

    @Test
    @DisplayName("호스트의 특정 상태 공동구매 개수를 조회한다")
    void countByHostIdAndStatus() {
        // given
        GroupBuy recruiting1 = createGroupBuy("Recruiting 1", host, GroupBuyStatus.RECRUITING, LocalDateTime.now().plusDays(1));
        GroupBuy recruiting2 = createGroupBuy("Recruiting 2", host, GroupBuyStatus.RECRUITING, LocalDateTime.now().plusDays(2));
        GroupBuy closed1 = createGroupBuy("Closed 1", host, GroupBuyStatus.CLOSED, LocalDateTime.now().plusDays(3));
        groupBuyRepository.saveAll(List.of(recruiting1, recruiting2, closed1));

        // when
        long recruitingCount = groupBuyRepository.countByHostIdAndStatus(host.getId(), GroupBuyStatus.RECRUITING);
        long closedCount = groupBuyRepository.countByHostIdAndStatus(host.getId(), GroupBuyStatus.CLOSED);

        // then
        assertThat(recruitingCount).isEqualTo(2);
        assertThat(closedCount).isEqualTo(1);
    }

    @Test
    @DisplayName("호스트가 같은 레시피로 진행 중인 공동구매가 있는지 확인한다")
    void existsByHostIdAndRecipeApiIdAndStatus() {
        // given
        GroupBuy recipeGroupBuy = createRecipeBasedGroupBuy("Recipe Buy", "RECIPE_123", GroupBuyStatus.RECRUITING);
        groupBuyRepository.save(recipeGroupBuy);

        // when
        boolean exists = groupBuyRepository.existsByHostIdAndRecipeApiIdAndStatus(
                host.getId(), "RECIPE_123", GroupBuyStatus.RECRUITING
        );
        boolean notExists = groupBuyRepository.existsByHostIdAndRecipeApiIdAndStatus(
                host.getId(), "RECIPE_456", GroupBuyStatus.RECRUITING
        );

        // then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("공동구매와 호스트 정보를 Fetch Join으로 조회한다 (N+1 방지)")
    void findByIdWithHost() {
        // given
        GroupBuy groupBuy = createGroupBuy("Test", GroupBuyStatus.RECRUITING, LocalDateTime.now().plusDays(3));
        GroupBuy saved = groupBuyRepository.save(groupBuy);

        // when
        Optional<GroupBuy> result = groupBuyRepository.findByIdWithHost(saved.getId());

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getHost().getNickname()).isEqualTo("test-host"); // 지연 로딩 없이 즉시 접근 가능
    }

    // ========== 헬퍼 메서드 ==========

    private GroupBuy createGroupBuyWithCategory(String title, String category, GroupBuyStatus status, LocalDateTime deadline) {
        return GroupBuy.builder()
                .host(host)
                .title(title)
                .content("content of " + title)
                .category(category)
                .totalPrice(10000)
                .targetHeadcount(5)
                .deadline(deadline)
                .deliveryMethod(DeliveryMethod.DIRECT)
                .status(status)
                .build();
    }

    private GroupBuy createGroupBuyWithContent(String title, String content, GroupBuyStatus status) {
        return GroupBuy.builder()
                .host(host)
                .title(title)
                .content(content)
                .category("FOOD")
                .totalPrice(10000)
                .targetHeadcount(5)
                .deadline(LocalDateTime.now().plusDays(3))
                .deliveryMethod(DeliveryMethod.DIRECT)
                .status(status)
                .build();
    }

    private GroupBuy createRecipeBasedGroupBuy(String title, String recipeApiId, GroupBuyStatus status) {
        return GroupBuy.builder()
                .host(host)
                .title(title)
                .content("Recipe based group buy")
                .category("FOOD")
                .totalPrice(10000)
                .targetHeadcount(5)
                .deadline(LocalDateTime.now().plusDays(3))
                .deliveryMethod(DeliveryMethod.DIRECT)
                .status(status)
                .recipeApiId(recipeApiId)
                .recipeName("Recipe Name")
                .recipeImageUrl("https://example.com/recipe.jpg")
                .build();
    }

    private GroupBuy createGroupBuy(String title, GroupBuyStatus status, LocalDateTime deadline) {
        return createGroupBuy(title, this.host, status, deadline);
    }

    private GroupBuy createGroupBuy(String title, User host, GroupBuyStatus status, LocalDateTime deadline) {
        return GroupBuy.builder()
                .host(host)
                .title(title)
                .content("content of " + title)
                .category("FOOD")
                .totalPrice(10000)
                .targetHeadcount(5)
                .deadline(deadline)
                .deliveryMethod(DeliveryMethod.DIRECT)
                .status(status)
                .build();
    }
}
