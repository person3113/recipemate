package com.recipemate.domain.groupbuy.service;

import com.recipemate.domain.groupbuy.dto.ParticipateRequest;
import com.recipemate.domain.groupbuy.entity.GroupBuy;
import com.recipemate.domain.groupbuy.entity.Participation;
import com.recipemate.domain.groupbuy.repository.GroupBuyRepository;
import com.recipemate.domain.groupbuy.repository.ParticipationRepository;
import com.recipemate.domain.user.entity.User;
import com.recipemate.domain.user.repository.UserRepository;
import com.recipemate.global.common.DeliveryMethod;
import com.recipemate.global.common.GroupBuyStatus;
import com.recipemate.global.common.UserRole;
import com.recipemate.global.exception.CustomException;
import com.recipemate.global.exception.ErrorCode;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class ParticipationServiceTest {

    @Autowired
    private ParticipationService participationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupBuyRepository groupBuyRepository;

    @Autowired
    private ParticipationRepository participationRepository;

    private User host;
    private User participant;
    private GroupBuy groupBuy;

    @BeforeEach
    void setUp() {
        // 주최자 생성
        host = User.builder()
            .email("host@test.com")
            .password("password123!")
            .nickname("주최자")
            .phoneNumber("010-1234-5678")
            .mannerTemperature(36.5)
            .role(UserRole.USER)
            .build();
        userRepository.save(host);

        // 참여자 생성
        participant = User.builder()
            .email("participant@test.com")
            .password("password123!")
            .nickname("참여자")
            .phoneNumber("010-9876-5432")
            .mannerTemperature(36.5)
            .role(UserRole.USER)
            .build();
        userRepository.save(participant);

        // 공구 생성 (목표 인원 5명)
        groupBuy = GroupBuy.createGeneral(
            host,
            "테스트 공구",
            "테스트 내용",
            "식재료",
            50000,
            5,
            LocalDateTime.now().plusDays(7),
            DeliveryMethod.BOTH,
            "서울역",
            3000,
            true
        );
        groupBuyRepository.save(groupBuy);
    }

    @Test
    @DisplayName("공구 참여 성공 - currentHeadcount 증가 확인")
    void participate_Success_IncrementsCurrentHeadcount() {
        // given
        ParticipateRequest request = ParticipateRequest.builder()
            .selectedDeliveryMethod(DeliveryMethod.DIRECT)
            .quantity(1)
            .build();

        // when
        participationService.participate(participant.getId(), groupBuy.getId(), request);

        // then
        GroupBuy updatedGroupBuy = groupBuyRepository.findById(groupBuy.getId()).orElseThrow();
        assertThat(updatedGroupBuy.getCurrentHeadcount()).isEqualTo(1);

        Participation savedParticipation = participationRepository
            .findByUserIdAndGroupBuyId(participant.getId(), groupBuy.getId())
            .orElseThrow();
        assertThat(savedParticipation.getUser().getId()).isEqualTo(participant.getId());
        assertThat(savedParticipation.getGroupBuy().getId()).isEqualTo(groupBuy.getId());
        assertThat(savedParticipation.getSelectedDeliveryMethod()).isEqualTo(DeliveryMethod.DIRECT);
        assertThat(savedParticipation.getQuantity()).isEqualTo(1);
    }

    @Test
    @DisplayName("중복 참여 방지 - 이미 참여한 공구에 재참여 시 예외 발생")
    void participate_Fail_AlreadyParticipated() {
        // given
        ParticipateRequest request = ParticipateRequest.builder()
            .selectedDeliveryMethod(DeliveryMethod.DIRECT)
            .quantity(1)
            .build();
        participationService.participate(participant.getId(), groupBuy.getId(), request);

        // when & then
        assertThatThrownBy(() -> participationService.participate(participant.getId(), groupBuy.getId(), request))
            .isInstanceOf(CustomException.class)
            .hasMessageContaining("이미 참여 중인 공동구매입니다");
    }

    @Test
    @DisplayName("목표 인원 달성 시 참여 불가")
    void participate_Fail_TargetHeadcountReached() {
        // given
        // 목표 인원까지 참여자 추가 (5명)
        for (int i = 0; i < 5; i++) {
            User newUser = User.builder()
                .email("user" + i + "@test.com")
                .password("password123!")
                .nickname("사용자" + i)
                .phoneNumber("010-0000-000" + i)
                .mannerTemperature(36.5)
                .role(UserRole.USER)
                .build();
            userRepository.save(newUser);

            ParticipateRequest req = ParticipateRequest.builder()
                .selectedDeliveryMethod(DeliveryMethod.DIRECT)
                .quantity(1)
                .build();
            participationService.participate(newUser.getId(), groupBuy.getId(), req);
        }

        // when & then
        ParticipateRequest request = ParticipateRequest.builder()
            .selectedDeliveryMethod(DeliveryMethod.DIRECT)
            .quantity(1)
            .build();

        assertThatThrownBy(() -> participationService.participate(participant.getId(), groupBuy.getId(), request))
            .isInstanceOf(CustomException.class)
            .hasMessageContaining("마감된 공동구매입니다");
    }

    @Test
    @DisplayName("마감된 공구 참여 불가")
    void participate_Fail_ClosedGroupBuy() {
        // given
        groupBuy.close();
        groupBuyRepository.save(groupBuy);

        ParticipateRequest request = ParticipateRequest.builder()
            .selectedDeliveryMethod(DeliveryMethod.DIRECT)
            .quantity(1)
            .build();

        // when & then
        assertThatThrownBy(() -> participationService.participate(participant.getId(), groupBuy.getId(), request))
            .isInstanceOf(CustomException.class)
            .hasMessageContaining("마감된 공동구매입니다");
    }

    @Test
    @DisplayName("주최자 본인 참여 불가")
    void participate_Fail_HostCannotParticipate() {
        // given
        ParticipateRequest request = ParticipateRequest.builder()
            .selectedDeliveryMethod(DeliveryMethod.DIRECT)
            .quantity(1)
            .build();

        // when & then
        assertThatThrownBy(() -> participationService.participate(host.getId(), groupBuy.getId(), request))
            .isInstanceOf(CustomException.class)
            .hasMessageContaining("주최자는 자신의 공동구매에 참여할 수 없습니다");
    }

    @Test
    @DisplayName("목표 인원 달성 시 상태가 CLOSED로 변경")
    void participate_Success_StatusChangesToClosedWhenTargetReached() {
        // given
        // 목표 인원 - 1명까지 참여
        for (int i = 0; i < 4; i++) {
            User newUser = User.builder()
                .email("user" + i + "@test.com")
                .password("password123!")
                .nickname("사용자" + i)
                .phoneNumber("010-0000-000" + i)
                .mannerTemperature(36.5)
                .role(UserRole.USER)
                .build();
            userRepository.save(newUser);

            ParticipateRequest req = ParticipateRequest.builder()
                .selectedDeliveryMethod(DeliveryMethod.DIRECT)
                .quantity(1)
                .build();
            participationService.participate(newUser.getId(), groupBuy.getId(), req);
        }

        // when - 마지막 1명 참여
        ParticipateRequest request = ParticipateRequest.builder()
            .selectedDeliveryMethod(DeliveryMethod.DIRECT)
            .quantity(1)
            .build();
        participationService.participate(participant.getId(), groupBuy.getId(), request);

        // then
        GroupBuy updatedGroupBuy = groupBuyRepository.findById(groupBuy.getId()).orElseThrow();
        assertThat(updatedGroupBuy.getCurrentHeadcount()).isEqualTo(5);
        assertThat(updatedGroupBuy.getStatus()).isEqualTo(GroupBuyStatus.CLOSED);
    }

    @Test
    @DisplayName("참여 취소 성공 - currentHeadcount 감소 확인")
    void cancelParticipation_Success_DecrementsCurrentHeadcount() {
        // given
        ParticipateRequest request = ParticipateRequest.builder()
            .selectedDeliveryMethod(DeliveryMethod.DIRECT)
            .quantity(1)
            .build();
        participationService.participate(participant.getId(), groupBuy.getId(), request);

        // when
        participationService.cancelParticipation(participant.getId(), groupBuy.getId());

        // then
        GroupBuy updatedGroupBuy = groupBuyRepository.findById(groupBuy.getId()).orElseThrow();
        assertThat(updatedGroupBuy.getCurrentHeadcount()).isEqualTo(0);

        boolean exists = participationRepository.existsByUserIdAndGroupBuyId(participant.getId(), groupBuy.getId());
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("참여 취소 실패 - 참여하지 않은 공구 취소 불가")
    void cancelParticipation_Fail_NotParticipated() {
        // given - 참여하지 않은 상태

        // when & then
        assertThatThrownBy(() -> participationService.cancelParticipation(participant.getId(), groupBuy.getId()))
            .isInstanceOf(CustomException.class)
            .hasMessageContaining("참여하지 않은 공동구매입니다");
    }

    @Test
    @DisplayName("참여 취소 실패 - 마감 1일 전 취소 제한")
    void cancelParticipation_Fail_DeadlineLessThanOneDay() {
        // given
        // 마감일이 내일인 공구 생성
        GroupBuy imminentGroupBuy = GroupBuy.createGeneral(
            host,
            "내일 마감 공구",
            "테스트 내용",
            "식재료",
            50000,
            5,
            LocalDateTime.now().plusHours(20), // 20시간 후 마감 (1일 미만)
            DeliveryMethod.BOTH,
            "서울역",
            3000,
            true
        );
        groupBuyRepository.save(imminentGroupBuy);

        // 참여
        ParticipateRequest request = ParticipateRequest.builder()
            .selectedDeliveryMethod(DeliveryMethod.DIRECT)
            .quantity(1)
            .build();
        participationService.participate(participant.getId(), imminentGroupBuy.getId(), request);

        // when & then
        assertThatThrownBy(() -> participationService.cancelParticipation(participant.getId(), imminentGroupBuy.getId()))
            .isInstanceOf(CustomException.class)
            .hasMessageContaining("마감 1일 전에는 참여를 취소할 수 없습니다");
    }

    @Test
    @DisplayName("참여자 목록 조회 성공 - 공개 설정인 경우 모든 사용자 조회 가능")
    void getParticipants_Success_PublicList() {
        // given
        // 여러 참여자 추가
        User participant1 = User.builder()
            .email("p1@test.com")
            .password("password123!")
            .nickname("참여자1")
            .phoneNumber("010-1111-1111")
            .mannerTemperature(38.0)
            .role(UserRole.USER)
            .build();
        userRepository.save(participant1);

        User participant2 = User.builder()
            .email("p2@test.com")
            .password("password123!")
            .nickname("참여자2")
            .phoneNumber("010-2222-2222")
            .mannerTemperature(40.0)
            .role(UserRole.USER)
            .build();
        userRepository.save(participant2);

        ParticipateRequest request = ParticipateRequest.builder()
            .selectedDeliveryMethod(DeliveryMethod.DIRECT)
            .quantity(1)
            .build();

        participationService.participate(participant1.getId(), groupBuy.getId(), request);
        participationService.participate(participant2.getId(), groupBuy.getId(), request);

        // when - 참여하지 않은 일반 사용자가 조회
        var result = participationService.getParticipants(groupBuy.getId(), participant.getId());

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getNickname()).isEqualTo("참여자1");
        assertThat(result.get(0).getMannerTemperature()).isEqualTo(38.0);
        assertThat(result.get(1).getNickname()).isEqualTo("참여자2");
        assertThat(result.get(1).getMannerTemperature()).isEqualTo(40.0);
    }

    @Test
    @DisplayName("참여자 목록 조회 성공 - 비공개 설정이지만 주최자는 조회 가능")
    void getParticipants_Success_PrivateListByHost() {
        // given
        // 비공개 공구 생성
        GroupBuy privateGroupBuy = GroupBuy.createGeneral(
            host,
            "비공개 공구",
            "테스트 내용",
            "식재료",
            50000,
            5,
            LocalDateTime.now().plusDays(7),
            DeliveryMethod.BOTH,
            "서울역",
            3000,
            false  // 참여자 목록 비공개
        );
        groupBuyRepository.save(privateGroupBuy);

        // 참여자 추가
        ParticipateRequest request = ParticipateRequest.builder()
            .selectedDeliveryMethod(DeliveryMethod.DIRECT)
            .quantity(1)
            .build();
        participationService.participate(participant.getId(), privateGroupBuy.getId(), request);

        // when - 주최자가 조회
        var result = participationService.getParticipants(privateGroupBuy.getId(), host.getId());

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNickname()).isEqualTo("참여자");
        assertThat(result.get(0).getMannerTemperature()).isEqualTo(36.5);
    }

    @Test
    @DisplayName("참여자 목록 조회 실패 - 비공개 설정이고 주최자가 아닌 경우 접근 거부")
    void getParticipants_Fail_PrivateListByNonHost() {
        // given
        // 비공개 공구 생성
        GroupBuy privateGroupBuy = GroupBuy.createGeneral(
            host,
            "비공개 공구",
            "테스트 내용",
            "식재료",
            50000,
            5,
            LocalDateTime.now().plusDays(7),
            DeliveryMethod.BOTH,
            "서울역",
            3000,
            false  // 참여자 목록 비공개
        );
        groupBuyRepository.save(privateGroupBuy);

        // 참여자 추가
        ParticipateRequest request = ParticipateRequest.builder()
            .selectedDeliveryMethod(DeliveryMethod.DIRECT)
            .quantity(1)
            .build();
        participationService.participate(participant.getId(), privateGroupBuy.getId(), request);

        // when & then - 주최자가 아닌 다른 사용자가 조회
        User otherUser = User.builder()
            .email("other@test.com")
            .password("password123!")
            .nickname("다른사용자")
            .phoneNumber("010-9999-9999")
            .mannerTemperature(36.5)
            .role(UserRole.USER)
            .build();
        userRepository.save(otherUser);

        assertThatThrownBy(() -> participationService.getParticipants(privateGroupBuy.getId(), otherUser.getId()))
            .isInstanceOf(CustomException.class)
            .hasMessageContaining("참여자 목록을 볼 수 있는 권한이 없습니다");
    }
}
