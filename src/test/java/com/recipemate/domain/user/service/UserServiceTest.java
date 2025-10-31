package com.recipemate.domain.user.service;

import com.recipemate.global.exception.CustomException;
import com.recipemate.domain.groupbuy.dto.GroupBuyResponse;
import com.recipemate.domain.groupbuy.entity.GroupBuy;
import com.recipemate.domain.groupbuy.entity.Participation;
import com.recipemate.domain.groupbuy.repository.GroupBuyRepository;
import com.recipemate.domain.groupbuy.repository.ParticipationRepository;
import com.recipemate.domain.user.dto.SignupRequest;
import com.recipemate.domain.user.dto.UserResponse;
import com.recipemate.domain.user.entity.User;
import com.recipemate.domain.user.repository.UserRepository;
import com.recipemate.global.common.DeliveryMethod;
import com.recipemate.global.common.GroupBuyStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupBuyRepository groupBuyRepository;

    @Autowired
    private ParticipationRepository participationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private User participant;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("host@example.com")
                .password(passwordEncoder.encode("password123"))
                .nickname("공구호스트")
                .phoneNumber("010-1234-5678")
                .role(com.recipemate.global.common.UserRole.USER)
                .build();
        userRepository.save(testUser);

        participant = User.builder()
                .email("participant@example.com")
                .password(passwordEncoder.encode("password123"))
                .nickname("참여자")
                .phoneNumber("010-9876-5432")
                .role(com.recipemate.global.common.UserRole.USER)
                .build();
        userRepository.save(participant);
    }

    @Test
    @DisplayName("회원가입에 성공한다")
    void signup_success() {
        SignupRequest request = SignupRequest.of(
                "test@example.com",
                "password123",
                "테스터",
                "010-1234-5678"
        );

        UserResponse response = userService.signup(request);

        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getNickname()).isEqualTo("테스터");
        assertThat(response.getMannerTemperature()).isEqualTo(36.5);
    }

    @Test
    @DisplayName("회원가입 시 비밀번호가 암호화된다")
    void signup_passwordEncoded() {
        SignupRequest request = SignupRequest.of(
                "test@example.com",
                "password123",
                "테스터",
                "010-1234-5678"
        );

        UserResponse response = userService.signup(request);

        User user = userRepository.findById(response.getId()).orElseThrow();
        assertThat(passwordEncoder.matches("password123", user.getPassword())).isTrue();
    }

    @Test
    @DisplayName("이메일이 중복되면 회원가입에 실패한다")
    void signup_duplicateEmail() {
        SignupRequest request1 = SignupRequest.of(
                "test@example.com",
                "password123",
                "테스터1",
                "010-1111-1111"
        );
        userService.signup(request1);

        SignupRequest request2 = SignupRequest.of(
                "test@example.com",
                "password456",
                "테스터2",
                "010-2222-2222"
        );

        assertThatThrownBy(() -> userService.signup(request2))
                .isInstanceOf(CustomException.class)
                .hasMessage("이미 존재하는 이메일입니다.");
    }

    @Test
    @DisplayName("닉네임이 중복되면 회원가입에 실패한다")
    void signup_duplicateNickname() {
        SignupRequest request1 = SignupRequest.of(
                "test1@example.com",
                "password123",
                "테스터",
                "010-1111-1111"
        );
        userService.signup(request1);

        SignupRequest request2 = SignupRequest.of(
                "test2@example.com",
                "password456",
                "테스터",
                "010-2222-2222"
        );

        assertThatThrownBy(() -> userService.signup(request2))
                .isInstanceOf(CustomException.class)
                .hasMessage("이미 존재하는 닉네임입니다.");
    }

    @Test
    @DisplayName("사용자가 만든 모든 공구 목록을 조회한다")
    void getMyGroupBuys_all() {
        // given
        createGroupBuy("공구1", GroupBuyStatus.RECRUITING);
        createGroupBuy("공구2", GroupBuyStatus.IMMINENT);
        createGroupBuy("공구3", GroupBuyStatus.CLOSED);
        
        Pageable pageable = PageRequest.of(0, 10);
        
        // when
        Page<GroupBuyResponse> result = userService.getMyGroupBuys(testUser.getId(), null, pageable);
        
        // then
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent()).hasSize(3);
    }

    @Test
    @DisplayName("사용자가 만든 공구 목록을 상태별로 필터링하여 조회한다")
    void getMyGroupBuys_filterByStatus() {
        // given
        createGroupBuy("모집중공구1", GroupBuyStatus.RECRUITING);
        createGroupBuy("모집중공구2", GroupBuyStatus.RECRUITING);
        createGroupBuy("마감임박공구", GroupBuyStatus.IMMINENT);
        createGroupBuy("마감공구", GroupBuyStatus.CLOSED);
        
        Pageable pageable = PageRequest.of(0, 10);
        
        // when
        Page<GroupBuyResponse> recruitingResult = userService.getMyGroupBuys(testUser.getId(), GroupBuyStatus.RECRUITING, pageable);
        Page<GroupBuyResponse> imminentResult = userService.getMyGroupBuys(testUser.getId(), GroupBuyStatus.IMMINENT, pageable);
        Page<GroupBuyResponse> closedResult = userService.getMyGroupBuys(testUser.getId(), GroupBuyStatus.CLOSED, pageable);
        
        // then
        assertThat(recruitingResult.getTotalElements()).isEqualTo(2);
        assertThat(imminentResult.getTotalElements()).isEqualTo(1);
        assertThat(closedResult.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("사용자가 만든 공구 목록을 페이징하여 조회한다")
    void getMyGroupBuys_pagination() {
        // given
        for (int i = 1; i <= 15; i++) {
            createGroupBuy("공구" + i, GroupBuyStatus.RECRUITING);
        }
        
        Pageable firstPage = PageRequest.of(0, 10);
        Pageable secondPage = PageRequest.of(1, 10);
        
        // when
        Page<GroupBuyResponse> firstResult = userService.getMyGroupBuys(testUser.getId(), null, firstPage);
        Page<GroupBuyResponse> secondResult = userService.getMyGroupBuys(testUser.getId(), null, secondPage);
        
        // then
        assertThat(firstResult.getTotalElements()).isEqualTo(15);
        assertThat(firstResult.getContent()).hasSize(10);
        assertThat(firstResult.getTotalPages()).isEqualTo(2);
        assertThat(secondResult.getContent()).hasSize(5);
    }

    @Test
    @DisplayName("존재하지 않는 사용자의 공구 목록 조회 시 예외가 발생한다")
    void getMyGroupBuys_userNotFound() {
        // given
        Long nonExistentUserId = 999L;
        Pageable pageable = PageRequest.of(0, 10);
        
        // when & then
        assertThatThrownBy(() -> userService.getMyGroupBuys(nonExistentUserId, null, pageable))
                .isInstanceOf(CustomException.class)
                .hasMessage("사용자를 찾을 수 없습니다.");
    }

    private GroupBuy createGroupBuy(String title, GroupBuyStatus status) {
        GroupBuy groupBuy = GroupBuy.builder()
                .title(title)
                .content("테스트 공구 내용")
                .category("식재료")
                .targetHeadcount(10)
                .currentHeadcount(1)
                .totalPrice(10000)
                .meetupLocation("서울시 강남구")
                .deliveryMethod(DeliveryMethod.BOTH)
                .deadline(LocalDateTime.now().plusDays(7))
                .status(status)
                .host(testUser)
                .build();
        return groupBuyRepository.save(groupBuy);
    }

    @Test
    @DisplayName("사용자가 참여한 모든 공구 목록을 조회한다")
    void getParticipatedGroupBuys_all() {
        // given
        GroupBuy groupBuy1 = createGroupBuy("공구1", GroupBuyStatus.RECRUITING);
        GroupBuy groupBuy2 = createGroupBuy("공구2", GroupBuyStatus.IMMINENT);
        GroupBuy groupBuy3 = createGroupBuy("공구3", GroupBuyStatus.CLOSED);
        
        createParticipation(participant, groupBuy1, DeliveryMethod.DIRECT);
        createParticipation(participant, groupBuy2, DeliveryMethod.PARCEL);
        createParticipation(participant, groupBuy3, DeliveryMethod.DIRECT);
        
        Pageable pageable = PageRequest.of(0, 10);
        
        // when
        Page<GroupBuyResponse> result = userService.getParticipatedGroupBuys(participant.getId(), null, pageable);
        
        // then
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent().get(0).getTitle()).isIn("공구1", "공구2", "공구3");
    }

    @Test
    @DisplayName("사용자가 참여한 공구 목록을 상태별로 필터링하여 조회한다")
    void getParticipatedGroupBuys_filterByStatus() {
        // given
        GroupBuy recruitingGroupBuy1 = createGroupBuy("모집중공구1", GroupBuyStatus.RECRUITING);
        GroupBuy recruitingGroupBuy2 = createGroupBuy("모집중공구2", GroupBuyStatus.RECRUITING);
        GroupBuy imminentGroupBuy = createGroupBuy("마감임박공구", GroupBuyStatus.IMMINENT);
        GroupBuy closedGroupBuy = createGroupBuy("마감공구", GroupBuyStatus.CLOSED);
        
        createParticipation(participant, recruitingGroupBuy1, DeliveryMethod.DIRECT);
        createParticipation(participant, recruitingGroupBuy2, DeliveryMethod.DIRECT);
        createParticipation(participant, imminentGroupBuy, DeliveryMethod.PARCEL);
        createParticipation(participant, closedGroupBuy, DeliveryMethod.DIRECT);
        
        Pageable pageable = PageRequest.of(0, 10);
        
        // when
        Page<GroupBuyResponse> recruitingResult = userService.getParticipatedGroupBuys(participant.getId(), GroupBuyStatus.RECRUITING, pageable);
        Page<GroupBuyResponse> imminentResult = userService.getParticipatedGroupBuys(participant.getId(), GroupBuyStatus.IMMINENT, pageable);
        Page<GroupBuyResponse> closedResult = userService.getParticipatedGroupBuys(participant.getId(), GroupBuyStatus.CLOSED, pageable);
        
        // then
        assertThat(recruitingResult.getTotalElements()).isEqualTo(2);
        assertThat(imminentResult.getTotalElements()).isEqualTo(1);
        assertThat(closedResult.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("사용자가 참여한 공구 목록을 페이징하여 조회한다")
    void getParticipatedGroupBuys_pagination() {
        // given
        for (int i = 1; i <= 15; i++) {
            GroupBuy groupBuy = createGroupBuy("공구" + i, GroupBuyStatus.RECRUITING);
            createParticipation(participant, groupBuy, DeliveryMethod.DIRECT);
        }
        
        Pageable firstPage = PageRequest.of(0, 10);
        Pageable secondPage = PageRequest.of(1, 10);
        
        // when
        Page<GroupBuyResponse> firstResult = userService.getParticipatedGroupBuys(participant.getId(), null, firstPage);
        Page<GroupBuyResponse> secondResult = userService.getParticipatedGroupBuys(participant.getId(), null, secondPage);
        
        // then
        assertThat(firstResult.getTotalElements()).isEqualTo(15);
        assertThat(firstResult.getContent()).hasSize(10);
        assertThat(firstResult.getTotalPages()).isEqualTo(2);
        assertThat(secondResult.getContent()).hasSize(5);
    }

    @Test
    @DisplayName("사용자가 참여한 공구가 없으면 빈 목록을 반환한다")
    void getParticipatedGroupBuys_empty() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        
        // when
        Page<GroupBuyResponse> result = userService.getParticipatedGroupBuys(participant.getId(), null, pageable);
        
        // then
        assertThat(result.getTotalElements()).isEqualTo(0);
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 사용자의 참여 공구 목록 조회 시 예외가 발생한다")
    void getParticipatedGroupBuys_userNotFound() {
        // given
        Long nonExistentUserId = 999L;
        Pageable pageable = PageRequest.of(0, 10);
        
        // when & then
        assertThatThrownBy(() -> userService.getParticipatedGroupBuys(nonExistentUserId, null, pageable))
                .isInstanceOf(CustomException.class)
                .hasMessage("사용자를 찾을 수 없습니다.");
    }

    private Participation createParticipation(User user, GroupBuy groupBuy, DeliveryMethod deliveryMethod) {
        Participation participation = Participation.create(user, groupBuy, 1, deliveryMethod);
        return participationRepository.save(participation);
    }
}
