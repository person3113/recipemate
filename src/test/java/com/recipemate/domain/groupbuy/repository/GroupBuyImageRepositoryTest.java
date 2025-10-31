package com.recipemate.domain.groupbuy.repository;

import com.recipemate.domain.groupbuy.entity.GroupBuy;
import com.recipemate.domain.groupbuy.entity.GroupBuyImage;
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
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(com.recipemate.global.config.QueryDslConfig.class)
@ActiveProfiles("test")
@DisplayName("GroupBuyImageRepository 통합 테스트")
class GroupBuyImageRepositoryTest {

    @Autowired
    private GroupBuyImageRepository groupBuyImageRepository;

    @Autowired
    private GroupBuyRepository groupBuyRepository;

    @Autowired
    private UserRepository userRepository;

    private GroupBuy groupBuy;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        groupBuyRepository.deleteAll();
        groupBuyImageRepository.deleteAll();

        User host = User.builder()
                .email("host@example.com")
                .password("password")
                .nickname("test-host")
                .phoneNumber("010-1234-5678")
                .role(UserRole.USER)
                .build();
        userRepository.save(host);

        groupBuy = GroupBuy.builder()
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
        groupBuy = groupBuyRepository.save(groupBuy);
    }

    @Test
    @DisplayName("공동구매 ID로 이미지 목록을 displayOrder 오름차순으로 조회한다.")
    void findByGroupBuyIdOrderByDisplayOrderAsc() {
        // given
        GroupBuyImage image1 = createImage(groupBuy, "url1", 1);
        GroupBuyImage image2 = createImage(groupBuy, "url2", 0);
        GroupBuyImage image3 = createImage(groupBuy, "url3", 2);
        groupBuyImageRepository.saveAll(List.of(image1, image2, image3));

        // when
        List<GroupBuyImage> result = groupBuyImageRepository.findByGroupBuyIdOrderByDisplayOrderAsc(groupBuy.getId());

        // then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getDisplayOrder()).isEqualTo(0);
        assertThat(result.get(0).getImageUrl()).isEqualTo("url2");
        assertThat(result.get(1).getDisplayOrder()).isEqualTo(1);
        assertThat(result.get(1).getImageUrl()).isEqualTo("url1");
        assertThat(result.get(2).getDisplayOrder()).isEqualTo(2);
        assertThat(result.get(2).getImageUrl()).isEqualTo("url3");
    }

    private GroupBuyImage createImage(GroupBuy groupBuy, String imageUrl, int displayOrder) {
        return GroupBuyImage.builder()
                .groupBuy(groupBuy)
                .imageUrl(imageUrl)
                .displayOrder(displayOrder)
                .build();
    }
}
