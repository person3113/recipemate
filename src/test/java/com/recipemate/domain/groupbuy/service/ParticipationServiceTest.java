package com.recipemate.domain.groupbuy.service;

import com.recipemate.domain.groupbuy.dto.ParticipateRequest;
import com.recipemate.domain.groupbuy.entity.GroupBuy;
import com.recipemate.domain.groupbuy.entity.Participation;
import com.recipemate.domain.groupbuy.repository.GroupBuyRepository;
import com.recipemate.domain.groupbuy.repository.ParticipationRepository;
import com.recipemate.domain.notification.service.NotificationService;
import com.recipemate.domain.user.entity.User;
import com.recipemate.domain.user.repository.UserRepository;
import com.recipemate.global.common.DeliveryMethod;
import com.recipemate.global.common.GroupBuyStatus;
import com.recipemate.global.common.UserRole;
import com.recipemate.global.exception.CustomException;
import com.recipemate.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParticipationServiceTest {

    @Mock
    private ParticipationRepository participationRepository;

    @Mock
    private GroupBuyRepository groupBuyRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ParticipationService participationService;

    private User host;
    private User participant;
    private GroupBuy groupBuy;

    @BeforeEach
    void setUp() throws Exception {
        // 주최자 생성
        host = User.builder()
            .email("host@test.com")
            .password("password123!")
            .nickname("주최자")
            .phoneNumber("010-1234-5678")
            .mannerTemperature(36.5)
            .role(UserRole.USER)
            .build();
        setUserId(host, 1L);

        // 참여자 생성
        participant = User.builder()
            .email("participant@test.com")
            .password("password123!")
            .nickname("참여자")
            .phoneNumber("010-9876-5432")
            .mannerTemperature(36.5)
            .role(UserRole.USER)
            .build();
        setUserId(participant, 2L);

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
        setGroupBuyId(groupBuy, 1L);
    }

    // Reflection utility methods to set IDs
    private void setUserId(User user, Long id) throws Exception {
        Field idField = User.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(user, id);
    }

    private void setGroupBuyId(GroupBuy groupBuy, Long id) throws Exception {
        Field idField = GroupBuy.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(groupBuy, id);
    }

    private void setParticipationId(Participation participation, Long id) throws Exception {
        Field idField = Participation.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(participation, id);
    }

    @Test
    @DisplayName("공구 참여 성공 - currentHeadcount 증가 확인")
    void participate_Success_IncrementsCurrentHeadcount() {
        // given
        ParticipateRequest request = ParticipateRequest.builder()
            .selectedDeliveryMethod(DeliveryMethod.DIRECT)
            .quantity(1)
            .build();

        given(userRepository.findById(participant.getId())).willReturn(Optional.of(participant));
        given(groupBuyRepository.findByIdWithHost(groupBuy.getId())).willReturn(Optional.of(groupBuy));
        given(participationRepository.existsByUserIdAndGroupBuyId(participant.getId(), groupBuy.getId())).willReturn(false);
        given(participationRepository.save(any(Participation.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        participationService.participate(participant.getId(), groupBuy.getId(), request);

        // then
        assertThat(groupBuy.getCurrentHeadcount()).isEqualTo(1);
        
        verify(userRepository).findById(participant.getId());
        verify(groupBuyRepository).findByIdWithHost(groupBuy.getId());
        verify(participationRepository).existsByUserIdAndGroupBuyId(participant.getId(), groupBuy.getId());
        verify(participationRepository).save(any(Participation.class));
    }

    @Test
    @DisplayName("중복 참여 방지 - 이미 참여한 공구에 재참여 시 예외 발생")
    void participate_Fail_AlreadyParticipated() {
        // given
        ParticipateRequest request = ParticipateRequest.builder()
            .selectedDeliveryMethod(DeliveryMethod.DIRECT)
            .quantity(1)
            .build();

        given(userRepository.findById(participant.getId())).willReturn(Optional.of(participant));
        given(groupBuyRepository.findByIdWithHost(groupBuy.getId())).willReturn(Optional.of(groupBuy));
        given(participationRepository.existsByUserIdAndGroupBuyId(participant.getId(), groupBuy.getId())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> participationService.participate(participant.getId(), groupBuy.getId(), request))
            .isInstanceOf(CustomException.class)
            .hasMessageContaining("이미 참여 중인 공동구매입니다");

        verify(userRepository).findById(participant.getId());
        verify(groupBuyRepository).findByIdWithHost(groupBuy.getId());
        verify(participationRepository).existsByUserIdAndGroupBuyId(participant.getId(), groupBuy.getId());
        verify(participationRepository, never()).save(any(Participation.class));
    }

    @Test
    @DisplayName("목표 인원 달성 시 참여 불가")
    void participate_Fail_TargetHeadcountReached() throws Exception {
        // given
        // 목표 인원까지 currentHeadcount 설정 (5명)
        Field currentHeadcountField = GroupBuy.class.getDeclaredField("currentHeadcount");
        currentHeadcountField.setAccessible(true);
        currentHeadcountField.set(groupBuy, 5);

        ParticipateRequest request = ParticipateRequest.builder()
            .selectedDeliveryMethod(DeliveryMethod.DIRECT)
            .quantity(1)
            .build();

        given(userRepository.findById(participant.getId())).willReturn(Optional.of(participant));
        given(groupBuyRepository.findByIdWithHost(groupBuy.getId())).willReturn(Optional.of(groupBuy));
        given(participationRepository.existsByUserIdAndGroupBuyId(participant.getId(), groupBuy.getId())).willReturn(false);

        // when & then
        assertThatThrownBy(() -> participationService.participate(participant.getId(), groupBuy.getId(), request))
            .isInstanceOf(CustomException.class)
            .hasMessageContaining("최대 참여 인원을 초과했습니다");

        verify(userRepository).findById(participant.getId());
        verify(groupBuyRepository).findByIdWithHost(groupBuy.getId());
        verify(participationRepository).existsByUserIdAndGroupBuyId(participant.getId(), groupBuy.getId());
        verify(participationRepository, never()).save(any(Participation.class));
    }

    @Test
    @DisplayName("마감된 공구 참여 불가")
    void participate_Fail_ClosedGroupBuy() {
        // given
        groupBuy.close();

        ParticipateRequest request = ParticipateRequest.builder()
            .selectedDeliveryMethod(DeliveryMethod.DIRECT)
            .quantity(1)
            .build();

        given(userRepository.findById(participant.getId())).willReturn(Optional.of(participant));
        given(groupBuyRepository.findByIdWithHost(groupBuy.getId())).willReturn(Optional.of(groupBuy));

        // when & then
        assertThatThrownBy(() -> participationService.participate(participant.getId(), groupBuy.getId(), request))
            .isInstanceOf(CustomException.class)
            .hasMessageContaining("마감된 공동구매입니다");

        verify(userRepository).findById(participant.getId());
        verify(groupBuyRepository).findByIdWithHost(groupBuy.getId());
        verify(participationRepository, never()).save(any(Participation.class));
    }

    @Test
    @DisplayName("주최자 본인 참여 불가")
    void participate_Fail_HostCannotParticipate() {
        // given
        ParticipateRequest request = ParticipateRequest.builder()
            .selectedDeliveryMethod(DeliveryMethod.DIRECT)
            .quantity(1)
            .build();

        given(userRepository.findById(host.getId())).willReturn(Optional.of(host));
        given(groupBuyRepository.findByIdWithHost(groupBuy.getId())).willReturn(Optional.of(groupBuy));

        // when & then
        assertThatThrownBy(() -> participationService.participate(host.getId(), groupBuy.getId(), request))
            .isInstanceOf(CustomException.class)
            .hasMessageContaining("주최자는 자신의 공동구매에 참여할 수 없습니다");

        verify(userRepository).findById(host.getId());
        verify(groupBuyRepository).findByIdWithHost(groupBuy.getId());
        verify(participationRepository, never()).save(any(Participation.class));
    }

    @Test
    @DisplayName("목표 인원 달성 시 상태가 CLOSED로 변경")
    void participate_Success_StatusChangesToClosedWhenTargetReached() throws Exception {
        // given
        // 목표 인원 - 1명까지 currentHeadcount 설정
        Field currentHeadcountField = GroupBuy.class.getDeclaredField("currentHeadcount");
        currentHeadcountField.setAccessible(true);
        currentHeadcountField.set(groupBuy, 4);

        ParticipateRequest request = ParticipateRequest.builder()
            .selectedDeliveryMethod(DeliveryMethod.DIRECT)
            .quantity(1)
            .build();

        given(userRepository.findById(participant.getId())).willReturn(Optional.of(participant));
        given(groupBuyRepository.findByIdWithHost(groupBuy.getId())).willReturn(Optional.of(groupBuy));
        given(participationRepository.existsByUserIdAndGroupBuyId(participant.getId(), groupBuy.getId())).willReturn(false);
        given(participationRepository.save(any(Participation.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when - 마지막 1명 참여
        participationService.participate(participant.getId(), groupBuy.getId(), request);

        // then
        assertThat(groupBuy.getCurrentHeadcount()).isEqualTo(5);
        assertThat(groupBuy.getStatus()).isEqualTo(GroupBuyStatus.CLOSED);

        verify(userRepository).findById(participant.getId());
        verify(groupBuyRepository).findByIdWithHost(groupBuy.getId());
        verify(participationRepository).existsByUserIdAndGroupBuyId(participant.getId(), groupBuy.getId());
        verify(participationRepository).save(any(Participation.class));
    }

    @Test
    @DisplayName("참여 취소 성공 - currentHeadcount 감소 확인")
    void cancelParticipation_Success_DecrementsCurrentHeadcount() throws Exception {
        // given
        // currentHeadcount를 1로 설정 (참여 후 상태)
        Field currentHeadcountField = GroupBuy.class.getDeclaredField("currentHeadcount");
        currentHeadcountField.setAccessible(true);
        currentHeadcountField.set(groupBuy, 1);

        Participation participation = Participation.create(
            participant,
            groupBuy,
            1,
            DeliveryMethod.DIRECT
        );
        setParticipationId(participation, 1L);

        given(participationRepository.findByUserIdAndGroupBuyId(participant.getId(), groupBuy.getId()))
            .willReturn(Optional.of(participation));
        given(groupBuyRepository.findById(groupBuy.getId())).willReturn(Optional.of(groupBuy));
        willDoNothing().given(participationRepository).delete(participation);

        // when
        participationService.cancelParticipation(participant.getId(), groupBuy.getId());

        // then
        assertThat(groupBuy.getCurrentHeadcount()).isEqualTo(0);

        verify(participationRepository).findByUserIdAndGroupBuyId(participant.getId(), groupBuy.getId());
        verify(groupBuyRepository).findById(groupBuy.getId());
        verify(participationRepository).delete(participation);
    }

    @Test
    @DisplayName("참여 취소 실패 - 참여하지 않은 공구 취소 불가")
    void cancelParticipation_Fail_NotParticipated() {
        // given
        given(participationRepository.findByUserIdAndGroupBuyId(participant.getId(), groupBuy.getId()))
            .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> participationService.cancelParticipation(participant.getId(), groupBuy.getId()))
            .isInstanceOf(CustomException.class)
            .hasMessageContaining("참여하지 않은 공동구매입니다");

        verify(participationRepository).findByUserIdAndGroupBuyId(participant.getId(), groupBuy.getId());
        verify(participationRepository, never()).delete(any(Participation.class));
    }

    @Test
    @DisplayName("참여 취소 실패 - 마감 1일 전 취소 제한")
    void cancelParticipation_Fail_DeadlineLessThanOneDay() throws Exception {
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
        setGroupBuyId(imminentGroupBuy, 2L);

        Participation participation = Participation.create(
            participant,
            imminentGroupBuy,
            1,
            DeliveryMethod.DIRECT
        );
        setParticipationId(participation, 1L);

        given(participationRepository.findByUserIdAndGroupBuyId(participant.getId(), imminentGroupBuy.getId()))
            .willReturn(Optional.of(participation));
        given(groupBuyRepository.findById(imminentGroupBuy.getId())).willReturn(Optional.of(imminentGroupBuy));

        // when & then
        assertThatThrownBy(() -> participationService.cancelParticipation(participant.getId(), imminentGroupBuy.getId()))
            .isInstanceOf(CustomException.class)
            .hasMessageContaining("마감 1일 전에는 참여를 취소할 수 없습니다");

        verify(participationRepository).findByUserIdAndGroupBuyId(participant.getId(), imminentGroupBuy.getId());
        verify(groupBuyRepository).findById(imminentGroupBuy.getId());
        verify(participationRepository, never()).delete(any(Participation.class));
    }

    @Test
    @DisplayName("참여자 목록 조회 성공 - 공개 설정인 경우 모든 사용자 조회 가능")
    void getParticipants_Success_PublicList() throws Exception {
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
        setUserId(participant1, 3L);

        User participant2 = User.builder()
            .email("p2@test.com")
            .password("password123!")
            .nickname("참여자2")
            .phoneNumber("010-2222-2222")
            .mannerTemperature(40.0)
            .role(UserRole.USER)
            .build();
        setUserId(participant2, 4L);

        Participation p1 = Participation.create(participant1, groupBuy, 1, DeliveryMethod.DIRECT);
        setParticipationId(p1, 1L);
        
        Participation p2 = Participation.create(participant2, groupBuy, 1, DeliveryMethod.DIRECT);
        setParticipationId(p2, 2L);

        given(groupBuyRepository.findByIdWithHost(groupBuy.getId())).willReturn(Optional.of(groupBuy));
        given(userRepository.findById(participant.getId())).willReturn(Optional.of(participant));
        given(participationRepository.findByGroupBuyIdWithUser(groupBuy.getId()))
            .willReturn(List.of(p1, p2));

        // when - 참여하지 않은 일반 사용자가 조회
        var result = participationService.getParticipants(groupBuy.getId(), participant.getId());

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getNickname()).isEqualTo("참여자1");
        assertThat(result.get(0).getMannerTemperature()).isEqualTo(38.0);
        assertThat(result.get(1).getNickname()).isEqualTo("참여자2");
        assertThat(result.get(1).getMannerTemperature()).isEqualTo(40.0);

        verify(groupBuyRepository).findByIdWithHost(groupBuy.getId());
        verify(userRepository).findById(participant.getId());
        verify(participationRepository).findByGroupBuyIdWithUser(groupBuy.getId());
    }

    @Test
    @DisplayName("참여자 목록 조회 성공 - 비공개 설정이지만 주최자는 조회 가능")
    void getParticipants_Success_PrivateListByHost() throws Exception {
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
        setGroupBuyId(privateGroupBuy, 2L);

        Participation participation = Participation.create(
            participant,
            privateGroupBuy,
            1,
            DeliveryMethod.DIRECT
        );
        setParticipationId(participation, 1L);

        given(groupBuyRepository.findByIdWithHost(privateGroupBuy.getId())).willReturn(Optional.of(privateGroupBuy));
        given(userRepository.findById(host.getId())).willReturn(Optional.of(host));
        given(participationRepository.findByGroupBuyIdWithUser(privateGroupBuy.getId()))
            .willReturn(List.of(participation));

        // when - 주최자가 조회
        var result = participationService.getParticipants(privateGroupBuy.getId(), host.getId());

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNickname()).isEqualTo("참여자");
        assertThat(result.get(0).getMannerTemperature()).isEqualTo(36.5);

        verify(groupBuyRepository).findByIdWithHost(privateGroupBuy.getId());
        verify(userRepository).findById(host.getId());
        verify(participationRepository).findByGroupBuyIdWithUser(privateGroupBuy.getId());
    }

    @Test
    @DisplayName("참여자 목록 조회 실패 - 비공개 설정이고 주최자가 아닌 경우 접근 거부")
    void getParticipants_Fail_PrivateListByNonHost() throws Exception {
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
        setGroupBuyId(privateGroupBuy, 2L);

        // when & then - 주최자가 아닌 다른 사용자가 조회
        User otherUser = User.builder()
            .email("other@test.com")
            .password("password123!")
            .nickname("다른사용자")
            .phoneNumber("010-9999-9999")
            .mannerTemperature(36.5)
            .role(UserRole.USER)
            .build();
        setUserId(otherUser, 5L);

        given(groupBuyRepository.findByIdWithHost(privateGroupBuy.getId())).willReturn(Optional.of(privateGroupBuy));
        given(userRepository.findById(otherUser.getId())).willReturn(Optional.of(otherUser));

        assertThatThrownBy(() -> participationService.getParticipants(privateGroupBuy.getId(), otherUser.getId()))
            .isInstanceOf(CustomException.class)
            .hasMessageContaining("참여자 목록을 볼 수 있는 권한이 없습니다");

        verify(groupBuyRepository).findByIdWithHost(privateGroupBuy.getId());
        verify(userRepository).findById(otherUser.getId());
        verify(participationRepository, never()).findByGroupBuyIdWithUser(anyLong());
    }
}
