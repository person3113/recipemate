package com.recipemate.domain.groupbuy.entity;

import com.recipemate.domain.user.entity.User;
import com.recipemate.global.common.DeliveryMethod;
import com.recipemate.global.common.GroupBuyStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ParticipationTest {

    @Test
    @DisplayName("참여 정보를 생성할 수 있다")
    void createParticipation() {
        // given
        User user = createTestUser("test@test.com", "테스트유저");
        GroupBuy groupBuy = createTestGroupBuy(user, DeliveryMethod.BOTH);
        
        // when
        Participation participation = Participation.create(user, groupBuy, 1, DeliveryMethod.DIRECT);
        
        // then
        assertThat(participation.getUser()).isEqualTo(user);
        assertThat(participation.getGroupBuy()).isEqualTo(groupBuy);
        assertThat(participation.getQuantity()).isEqualTo(1);
        assertThat(participation.getSelectedDeliveryMethod()).isEqualTo(DeliveryMethod.DIRECT);
        assertThat(participation.getParticipatedAt()).isNotNull();
    }
    
    @Test
    @DisplayName("selectedDeliveryMethod는 DIRECT 또는 PARCEL만 가능하다")
    void validateDeliveryMethod() {
        // given
        User user = createTestUser("test@test.com", "테스트유저");
        GroupBuy groupBuy = createTestGroupBuy(user, DeliveryMethod.BOTH);
        
        // when & then - BOTH는 선택 불가
        assertThatThrownBy(() -> Participation.create(user, groupBuy, 1, DeliveryMethod.BOTH))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("DIRECT 또는 PARCEL");
    }
    
    @Test
    @DisplayName("수량은 1 이상이어야 한다")
    void validateQuantity() {
        // given
        User user = createTestUser("test@test.com", "테스트유저");
        GroupBuy groupBuy = createTestGroupBuy(user, DeliveryMethod.BOTH);
        
        // when & then
        assertThatThrownBy(() -> Participation.create(user, groupBuy, 0, DeliveryMethod.DIRECT))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("수량은 1 이상");
    }
    
    @Test
    @DisplayName("공구의 수령 방법과 선택한 수령 방법이 호환되어야 한다")
    void validateCompatibleDeliveryMethod() {
        // given
        User user = createTestUser("test@test.com", "테스트유저");
        GroupBuy directOnlyGroupBuy = createTestGroupBuy(user, DeliveryMethod.DIRECT);
        
        // when & then - 직거래만 가능한 공구에 택배 선택 불가
        assertThatThrownBy(() -> Participation.create(user, directOnlyGroupBuy, 1, DeliveryMethod.PARCEL))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("공구의 수령 방법과 호환");
    }
    
    @Test
    @DisplayName("참여 수량을 변경할 수 있다")
    void updateQuantity() {
        // given
        User user = createTestUser("test@test.com", "테스트유저");
        GroupBuy groupBuy = createTestGroupBuy(user, DeliveryMethod.BOTH);
        Participation participation = Participation.create(user, groupBuy, 1, DeliveryMethod.DIRECT);
        
        // when
        participation.updateQuantity(3);
        
        // then
        assertThat(participation.getQuantity()).isEqualTo(3);
    }
    
    @Test
    @DisplayName("수령 방법을 변경할 수 있다")
    void updateDeliveryMethod() {
        // given
        User user = createTestUser("test@test.com", "테스트유저");
        GroupBuy groupBuy = createTestGroupBuy(user, DeliveryMethod.BOTH);
        Participation participation = Participation.create(user, groupBuy, 1, DeliveryMethod.DIRECT);
        
        // when
        participation.updateDeliveryMethod(DeliveryMethod.PARCEL);
        
        // then
        assertThat(participation.getSelectedDeliveryMethod()).isEqualTo(DeliveryMethod.PARCEL);
    }

    // 테스트 헬퍼 메서드
    private User createTestUser(String email, String nickname) {
        return User.builder()
            .email(email)
            .password("encodedPassword")
            .nickname(nickname)
            .phoneNumber("010-1234-5678")
            .build();
    }

    private GroupBuy createTestGroupBuy(User host, DeliveryMethod deliveryMethod) {
        return GroupBuy.createGeneral(
            host,
            "테스트 공구",
            "테스트 내용",
            "식재료",
            10000,
            5,
            LocalDateTime.now().plusDays(7),
            deliveryMethod,
            "서울역",
            3000,
            true
        );
    }
}
