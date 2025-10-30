package com.recipemate.domain.groupbuy.entity;

import com.recipemate.domain.user.entity.User;
import com.recipemate.global.common.DeliveryMethod;
import com.recipemate.global.common.GroupBuyStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

class GroupBuyTest {

    private User host;

    @BeforeEach
    void setUp() {
        host = User.create(
                "host@example.com",
                "encodedPassword",
                "주최자",
                "010-1234-5678"
        );
    }

    @Test
    @DisplayName("일반 공구 생성 성공")
    void createGeneralGroupBuy() {
        // given
        LocalDateTime deadline = LocalDateTime.now().plusDays(7);

        // when
        GroupBuy groupBuy = GroupBuy.createGeneral(
                host,
                "까르보나라 재료 공구",
                "맛있는 파스타를 만들기 위한 재료 공구입니다.",
                "식재료",
                60000,
                5,
                deadline,
                DeliveryMethod.BOTH,
                "서울대학교 정문",
                3000,
                true
        );

        // then
        assertThat(groupBuy).isNotNull();
        assertThat(groupBuy.getHost()).isEqualTo(host);
        assertThat(groupBuy.getTitle()).isEqualTo("까르보나라 재료 공구");
        assertThat(groupBuy.getTotalPrice()).isEqualTo(60000);
        assertThat(groupBuy.getTargetHeadcount()).isEqualTo(5);
        assertThat(groupBuy.getCurrentHeadcount()).isEqualTo(0);
        assertThat(groupBuy.getStatus()).isEqualTo(GroupBuyStatus.RECRUITING);
        assertThat(groupBuy.getRecipeApiId()).isNull();
        assertThat(groupBuy.getVersion()).isEqualTo(0L);
    }

    @Test
    @DisplayName("레시피 기반 공구 생성 성공")
    void createRecipeBasedGroupBuy() {
        // given
        LocalDateTime deadline = LocalDateTime.now().plusDays(7);

        // when
        GroupBuy groupBuy = GroupBuy.createRecipeBased(
                host,
                "까르보나라 재료 공구",
                "맛있는 파스타를 만들기 위한 재료 공구입니다.",
                "식재료",
                60000,
                5,
                deadline,
                DeliveryMethod.DIRECT,
                "서울대학교 정문",
                null,
                true,
                "52772",
                "Carbonara",
                "https://www.themealdb.com/images/media/meals/llcbn01574260722.jpg"
        );

        // then
        assertThat(groupBuy).isNotNull();
        assertThat(groupBuy.getRecipeApiId()).isEqualTo("52772");
        assertThat(groupBuy.getRecipeName()).isEqualTo("Carbonara");
        assertThat(groupBuy.getRecipeImageUrl()).isEqualTo("https://www.themealdb.com/images/media/meals/llcbn01574260722.jpg");
        assertThat(groupBuy.isRecipeBased()).isTrue();
    }

    @Test
    @DisplayName("참여자 증가")
    void increaseParticipant() {
        // given
        GroupBuy groupBuy = createSampleGroupBuy();

        // when
        groupBuy.increaseParticipant();

        // then
        assertThat(groupBuy.getCurrentHeadcount()).isEqualTo(1);
    }

    @Test
    @DisplayName("참여자 감소")
    void decreaseParticipant() {
        // given
        GroupBuy groupBuy = createSampleGroupBuy();
        groupBuy.increaseParticipant();
        groupBuy.increaseParticipant();

        // when
        groupBuy.decreaseParticipant();

        // then
        assertThat(groupBuy.getCurrentHeadcount()).isEqualTo(1);
    }

    @Test
    @DisplayName("참여자가 0일 때 감소 시도하면 예외 발생")
    void decreaseParticipantWhenZero() {
        // given
        GroupBuy groupBuy = createSampleGroupBuy();

        // when & then
        assertThatThrownBy(groupBuy::decreaseParticipant)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("참여 인원이 1명(주최자)입니다");
    }

    @Test
    @DisplayName("목표 인원 도달 여부 확인")
    void isTargetReached() {
        // given
        GroupBuy groupBuy = createSampleGroupBuy();

        // when
        for (int i = 0; i < 5; i++) {
            groupBuy.increaseParticipant();
        }

        // then
        assertThat(groupBuy.isTargetReached()).isTrue();
        assertThat(groupBuy.getCurrentHeadcount()).isEqualTo(groupBuy.getTargetHeadcount());
    }

    @Test
    @DisplayName("목표 인원 초과 시 예외 발생")
    void increaseParticipantOverTarget() {
        // given
        GroupBuy groupBuy = createSampleGroupBuy();
        for (int i = 0; i < 5; i++) {
            groupBuy.increaseParticipant();
        }

        // when & then
        assertThatThrownBy(groupBuy::increaseParticipant)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("목표 인원에 도달했습니다");
    }

    @Test
    @DisplayName("공구 상태를 마감으로 변경")
    void closeGroupBuy() {
        // given
        GroupBuy groupBuy = createSampleGroupBuy();

        // when
        groupBuy.close();

        // then
        assertThat(groupBuy.getStatus()).isEqualTo(GroupBuyStatus.CLOSED);
    }

    @Test
    @DisplayName("공구 상태를 마감 임박으로 변경")
    void markAsImminent() {
        // given
        GroupBuy groupBuy = createSampleGroupBuy();

        // when
        groupBuy.markAsImminent();

        // then
        assertThat(groupBuy.getStatus()).isEqualTo(GroupBuyStatus.IMMINENT);
    }

    @Test
    @DisplayName("마감일이 과거인 경우 생성 실패")
    void createWithPastDeadline() {
        // given
        LocalDateTime pastDeadline = LocalDateTime.now().minusDays(1);

        // when & then
        assertThatThrownBy(() -> GroupBuy.createGeneral(
                host,
                "까르보나라 재료 공구",
                "맛있는 파스타를 만들기 위한 재료 공구입니다.",
                "식재료",
                60000,
                5,
                pastDeadline,
                DeliveryMethod.DIRECT,
                "서울대학교 정문",
                null,
                true
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("마감일은 현재보다 이후여야 합니다");
    }

    @Test
    @DisplayName("목표 인원이 2명 미만인 경우 생성 실패")
    void createWithInvalidTargetHeadcount() {
        // given
        LocalDateTime deadline = LocalDateTime.now().plusDays(7);

        // when & then
        assertThatThrownBy(() -> GroupBuy.createGeneral(
                host,
                "까르보나라 재료 공구",
                "맛있는 파스타를 만들기 위한 재료 공구입니다.",
                "식재료",
                60000,
                1,
                deadline,
                DeliveryMethod.DIRECT,
                "서울대학교 정문",
                null,
                true
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("목표 인원은 2명 이상이어야 합니다");
    }

    @Test
    @DisplayName("총 금액이 음수인 경우 생성 실패")
    void createWithNegativeTotalPrice() {
        // given
        LocalDateTime deadline = LocalDateTime.now().plusDays(7);

        // when & then
        assertThatThrownBy(() -> GroupBuy.createGeneral(
                host,
                "까르보나라 재료 공구",
                "맛있는 파스타를 만들기 위한 재료 공구입니다.",
                "식재료",
                -1000,
                5,
                deadline,
                DeliveryMethod.DIRECT,
                "서울대학교 정문",
                null,
                true
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("총 금액은 0원 이상이어야 합니다");
    }

    @Test
    @DisplayName("공구 정보 수정")
    void updateGroupBuy() {
        // given
        GroupBuy groupBuy = createSampleGroupBuy();
        LocalDateTime newDeadline = LocalDateTime.now().plusDays(10);

        // when
        groupBuy.update(
                "수정된 제목",
                "수정된 내용",
                "가공식품",
                80000,
                10,
                newDeadline,
                DeliveryMethod.PARCEL,
                null,
                5000,
                false
        );

        // then
        assertThat(groupBuy.getTitle()).isEqualTo("수정된 제목");
        assertThat(groupBuy.getContent()).isEqualTo("수정된 내용");
        assertThat(groupBuy.getCategory()).isEqualTo("가공식품");
        assertThat(groupBuy.getTotalPrice()).isEqualTo(80000);
        assertThat(groupBuy.getTargetHeadcount()).isEqualTo(10);
        assertThat(groupBuy.getDeadline()).isEqualTo(newDeadline);
        assertThat(groupBuy.getDeliveryMethod()).isEqualTo(DeliveryMethod.PARCEL);
        assertThat(groupBuy.getParcelFee()).isEqualTo(5000);
        assertThat(groupBuy.getIsParticipantListPublic()).isFalse();
    }

    @Test
    @DisplayName("주최자 확인")
    void isHost() {
        // given
        GroupBuy groupBuy = createSampleGroupBuy();
        User otherUser = User.create(
                "other@example.com",
                "encodedPassword",
                "다른사용자",
                "010-9999-9999"
        );

        // when & then
        assertThat(groupBuy.isHost(host)).isTrue();
        assertThat(groupBuy.isHost(otherUser)).isFalse();
    }

    @Test
    @DisplayName("참여 가능 여부 확인 - 모집중이고 목표 미달성")
    void canParticipate() {
        // given
        GroupBuy groupBuy = createSampleGroupBuy();

        // when & then
        assertThat(groupBuy.canParticipate()).isTrue();
    }

    @Test
    @DisplayName("참여 불가능 - 마감된 공구")
    void cannotParticipateWhenClosed() {
        // given
        GroupBuy groupBuy = createSampleGroupBuy();
        groupBuy.close();

        // when & then
        assertThat(groupBuy.canParticipate()).isFalse();
    }

    @Test
    @DisplayName("참여 불가능 - 목표 인원 도달")
    void cannotParticipateWhenTargetReached() {
        // given
        GroupBuy groupBuy = createSampleGroupBuy();
        for (int i = 0; i < 5; i++) {
            groupBuy.increaseParticipant();
        }

        // when & then
        assertThat(groupBuy.canParticipate()).isFalse();
    }

    private GroupBuy createSampleGroupBuy() {
        return GroupBuy.createGeneral(
                host,
                "까르보나라 재료 공구",
                "맛있는 파스타를 만들기 위한 재료 공구입니다.",
                "식재료",
                60000,
                5,
                LocalDateTime.now().plusDays(7),
                DeliveryMethod.BOTH,
                "서울대학교 정문",
                3000,
                true
        );
    }
}
