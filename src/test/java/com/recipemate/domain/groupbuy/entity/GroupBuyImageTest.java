package com.recipemate.domain.groupbuy.entity;

import com.recipemate.domain.user.entity.User;
import com.recipemate.domain.user.repository.UserRepository;
import com.recipemate.global.common.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(com.recipemate.global.config.QueryDslConfig.class)
class GroupBuyImageTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User host;
    private GroupBuy groupBuy;

    @BeforeEach
    void setUp() {
        host = User.builder()
                .email("host@example.com")
                .password("password")
                .nickname("host")
                .phoneNumber("010-1234-5678")
                .mannerTemperature(36.5)
                .role(UserRole.USER)
                .build();
        userRepository.save(host);

        groupBuy = GroupBuy.builder()
                .host(host)
                .title("Test Group Buy")
                .content("Test Content")
                .category("FOOD")
                .totalPrice(10000)
                .targetHeadcount(5)
                .deadline(LocalDateTime.now().plusDays(3))
                .build();
        entityManager.persist(groupBuy);
    }

    @Test
    @DisplayName("성공: GroupBuyImage 엔티티를 저장한다")
    void saveGroupBuyImage_success() {
        // given
        GroupBuyImage image = GroupBuyImage.builder()
                .groupBuy(groupBuy)
                .imageUrl("https://example.com/image.jpg")
                .displayOrder(1)
                .build();

        // when
        entityManager.persist(image);
        entityManager.flush();
        entityManager.clear();

        // then
        GroupBuyImage foundImage = entityManager.find(GroupBuyImage.class, image.getId());
        org.assertj.core.api.Assertions.assertThat(foundImage).isNotNull();
        org.assertj.core.api.Assertions.assertThat(foundImage.getImageUrl()).isEqualTo("https://example.com/image.jpg");
    }

    @Test
    @DisplayName("실패: GroupBuy 없이 GroupBuyImage를 저장하면 예외가 발생한다")
    void createGroupBuyImage_fail_withoutGroupBuy() {
        // given
        GroupBuyImage image = GroupBuyImage.builder()
                .groupBuy(null) // GroupBuy를 null로 설정
                .imageUrl("https://example.com/image.jpg")
                .displayOrder(1)
                .build();

        // when & then
        assertThatThrownBy(() -> {
            entityManager.persist(image);
            entityManager.flush();
        }).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("실패: 같은 공구 내에서 displayOrder가 중복되면 예외가 발생한다")
    void createGroupBuyImage_fail_withDuplicateDisplayOrder() {
        // given
        GroupBuyImage image1 = GroupBuyImage.builder()
                .groupBuy(groupBuy)
                .imageUrl("https://example.com/image1.jpg")
                .displayOrder(1)
                .build();
        entityManager.persist(image1);

        GroupBuyImage image2 = GroupBuyImage.builder()
                .groupBuy(groupBuy)
                .imageUrl("https://example.com/image2.jpg")
                .displayOrder(1) // 중복된 displayOrder
                .build();

        // when & then
        assertThatThrownBy(() -> {
            entityManager.persist(image2);
            entityManager.flush(); // 제약조건 위반은 flush 시점에 발생
        }).isInstanceOf(RuntimeException.class);
    }
}
