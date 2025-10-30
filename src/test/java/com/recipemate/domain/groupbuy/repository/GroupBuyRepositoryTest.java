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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
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
