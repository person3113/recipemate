package com.recipemate.domain.user.service;

import com.recipemate.global.exception.CustomException;
import com.recipemate.domain.groupbuy.dto.GroupBuyResponse;
import com.recipemate.domain.groupbuy.entity.GroupBuy;
import com.recipemate.domain.groupbuy.entity.GroupBuyImage;
import com.recipemate.domain.groupbuy.entity.Participation;
import com.recipemate.domain.groupbuy.repository.GroupBuyImageRepository;
import com.recipemate.domain.groupbuy.repository.GroupBuyRepository;
import com.recipemate.domain.groupbuy.repository.ParticipationRepository;
import com.recipemate.domain.user.dto.SignupRequest;
import com.recipemate.domain.user.dto.UserResponse;
import com.recipemate.domain.user.entity.User;
import com.recipemate.domain.user.repository.UserRepository;
import com.recipemate.global.common.DeliveryMethod;
import com.recipemate.global.common.GroupBuyStatus;
import com.recipemate.global.common.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private GroupBuyRepository groupBuyRepository;

    @Mock
    private GroupBuyImageRepository groupBuyImageRepository;

    @Mock
    private ParticipationRepository participationRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private User participant;

    @BeforeEach
    void setUp() throws Exception {
        testUser = User.builder()
                .email("host@example.com")
                .password("encodedPassword123")
                .nickname("공구호스트")
                .phoneNumber("010-1234-5678")
                .role(UserRole.USER)
                .build();
        setUserId(testUser, 1L);

        participant = User.builder()
                .email("participant@example.com")
                .password("encodedPassword123")
                .nickname("참여자")
                .phoneNumber("010-9876-5432")
                .role(UserRole.USER)
                .build();
        setUserId(participant, 2L);
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
    @DisplayName("회원가입에 성공한다")
    void signup_success() throws Exception {
        // given
        SignupRequest request = SignupRequest.of(
                "test@example.com",
                "password123",
                "테스터",
                "010-1234-5678"
        );

        User savedUser = User.builder()
                .email("test@example.com")
                .password("encodedPassword")
                .nickname("테스터")
                .phoneNumber("010-1234-5678")
                .role(UserRole.USER)
                .build();
        setUserId(savedUser, 3L);

        given(userRepository.existsByEmail(request.getEmail())).willReturn(false);
        given(userRepository.existsByNickname(request.getNickname())).willReturn(false);
        given(passwordEncoder.encode(request.getPassword())).willReturn("encodedPassword");
        given(userRepository.save(any(User.class))).willReturn(savedUser);

        // when
        UserResponse response = userService.signup(request);

        // then
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getNickname()).isEqualTo("테스터");
        assertThat(response.getMannerTemperature()).isEqualTo(36.5);

        verify(userRepository).existsByEmail(request.getEmail());
        verify(userRepository).existsByNickname(request.getNickname());
        verify(passwordEncoder).encode(request.getPassword());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("회원가입 시 비밀번호가 암호화된다")
    void signup_passwordEncoded() throws Exception {
        // given
        SignupRequest request = SignupRequest.of(
                "test@example.com",
                "password123",
                "테스터",
                "010-1234-5678"
        );

        User savedUser = User.builder()
                .email("test@example.com")
                .password("encodedPassword")
                .nickname("테스터")
                .phoneNumber("010-1234-5678")
                .role(UserRole.USER)
                .build();
        setUserId(savedUser, 3L);

        given(userRepository.existsByEmail(request.getEmail())).willReturn(false);
        given(userRepository.existsByNickname(request.getNickname())).willReturn(false);
        given(passwordEncoder.encode(request.getPassword())).willReturn("encodedPassword");
        given(userRepository.save(any(User.class))).willReturn(savedUser);
        given(userRepository.findById(3L)).willReturn(Optional.of(savedUser));
        given(passwordEncoder.matches("password123", "encodedPassword")).willReturn(true);

        // when
        UserResponse response = userService.signup(request);

        // then
        User user = userRepository.findById(response.getId()).orElseThrow();
        assertThat(passwordEncoder.matches("password123", user.getPassword())).isTrue();

        verify(passwordEncoder).encode(request.getPassword());
    }

    @Test
    @DisplayName("이메일이 중복되면 회원가입에 실패한다")
    void signup_duplicateEmail() {
        // given
        SignupRequest request1 = SignupRequest.of(
                "test@example.com",
                "password123",
                "테스터1",
                "010-1111-1111"
        );

        given(userRepository.existsByEmail("test@example.com")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.signup(request1))
                .isInstanceOf(CustomException.class)
                .hasMessage("이미 존재하는 이메일입니다.");

        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("닉네임이 중복되면 회원가입에 실패한다")
    void signup_duplicateNickname() {
        // given
        SignupRequest request1 = SignupRequest.of(
                "test1@example.com",
                "password123",
                "테스터",
                "010-1111-1111"
        );

        given(userRepository.existsByEmail("test1@example.com")).willReturn(false);
        given(userRepository.existsByNickname("테스터")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.signup(request1))
                .isInstanceOf(CustomException.class)
                .hasMessage("이미 존재하는 닉네임입니다.");

        verify(userRepository).existsByEmail("test1@example.com");
        verify(userRepository).existsByNickname("테스터");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("사용자가 만든 모든 공구 목록을 조회한다")
    void getMyGroupBuys_all() throws Exception {
        // given
        GroupBuy groupBuy1 = createGroupBuy(1L, "공구1", GroupBuyStatus.RECRUITING);
        GroupBuy groupBuy2 = createGroupBuy(2L, "공구2", GroupBuyStatus.IMMINENT);
        GroupBuy groupBuy3 = createGroupBuy(3L, "공구3", GroupBuyStatus.CLOSED);
        
        Pageable pageable = PageRequest.of(0, 10);
        Page<GroupBuy> mockPage = new PageImpl<>(Arrays.asList(groupBuy1, groupBuy2, groupBuy3), pageable, 3);
        
        given(userRepository.findById(testUser.getId())).willReturn(Optional.of(testUser));
        given(groupBuyRepository.findByHostIdAndStatusIn(eq(testUser.getId()), anyList(), eq(pageable)))
            .willReturn(mockPage);
        given(groupBuyImageRepository.findByGroupBuyIdInOrderByGroupBuyIdAndDisplayOrder(anyList()))
            .willReturn(List.of());
        
        // when
        Page<GroupBuyResponse> result = userService.getMyGroupBuys(testUser.getId(), null, pageable);
        
        // then
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent()).hasSize(3);

        verify(userRepository).findById(testUser.getId());
        verify(groupBuyRepository).findByHostIdAndStatusIn(eq(testUser.getId()), anyList(), eq(pageable));
    }

    @Test
    @DisplayName("사용자가 만든 공구 목록을 상태별로 필터링하여 조회한다")
    void getMyGroupBuys_filterByStatus() throws Exception {
        // given
        GroupBuy recruitingGroupBuy1 = createGroupBuy(1L, "모집중공구1", GroupBuyStatus.RECRUITING);
        GroupBuy recruitingGroupBuy2 = createGroupBuy(2L, "모집중공구2", GroupBuyStatus.RECRUITING);
        GroupBuy imminentGroupBuy = createGroupBuy(3L, "마감임박공구", GroupBuyStatus.IMMINENT);
        GroupBuy closedGroupBuy = createGroupBuy(4L, "마감공구", GroupBuyStatus.CLOSED);
        
        Pageable pageable = PageRequest.of(0, 10);
        
        Page<GroupBuy> recruitingPage = new PageImpl<>(Arrays.asList(recruitingGroupBuy1, recruitingGroupBuy2), pageable, 2);
        Page<GroupBuy> imminentPage = new PageImpl<>(List.of(imminentGroupBuy), pageable, 1);
        Page<GroupBuy> closedPage = new PageImpl<>(List.of(closedGroupBuy), pageable, 1);
        
        given(userRepository.findById(testUser.getId())).willReturn(Optional.of(testUser));
        given(groupBuyRepository.findByHostIdAndStatusIn(eq(testUser.getId()), eq(List.of(GroupBuyStatus.RECRUITING)), eq(pageable)))
            .willReturn(recruitingPage);
        given(groupBuyRepository.findByHostIdAndStatusIn(eq(testUser.getId()), eq(List.of(GroupBuyStatus.IMMINENT)), eq(pageable)))
            .willReturn(imminentPage);
        given(groupBuyRepository.findByHostIdAndStatusIn(eq(testUser.getId()), eq(List.of(GroupBuyStatus.CLOSED)), eq(pageable)))
            .willReturn(closedPage);
        given(groupBuyImageRepository.findByGroupBuyIdInOrderByGroupBuyIdAndDisplayOrder(anyList()))
            .willReturn(List.of());
        
        // when
        Page<GroupBuyResponse> recruitingResult = userService.getMyGroupBuys(testUser.getId(), GroupBuyStatus.RECRUITING, pageable);
        Page<GroupBuyResponse> imminentResult = userService.getMyGroupBuys(testUser.getId(), GroupBuyStatus.IMMINENT, pageable);
        Page<GroupBuyResponse> closedResult = userService.getMyGroupBuys(testUser.getId(), GroupBuyStatus.CLOSED, pageable);
        
        // then
        assertThat(recruitingResult.getTotalElements()).isEqualTo(2);
        assertThat(imminentResult.getTotalElements()).isEqualTo(1);
        assertThat(closedResult.getTotalElements()).isEqualTo(1);

        verify(userRepository, times(3)).findById(testUser.getId());
    }

    @Test
    @DisplayName("사용자가 만든 공구 목록을 페이징하여 조회한다")
    void getMyGroupBuys_pagination() throws Exception {
        // given
        List<GroupBuy> firstPageGroupBuys = Arrays.asList(
            createGroupBuy(1L, "공구1", GroupBuyStatus.RECRUITING),
            createGroupBuy(2L, "공구2", GroupBuyStatus.RECRUITING),
            createGroupBuy(3L, "공구3", GroupBuyStatus.RECRUITING),
            createGroupBuy(4L, "공구4", GroupBuyStatus.RECRUITING),
            createGroupBuy(5L, "공구5", GroupBuyStatus.RECRUITING),
            createGroupBuy(6L, "공구6", GroupBuyStatus.RECRUITING),
            createGroupBuy(7L, "공구7", GroupBuyStatus.RECRUITING),
            createGroupBuy(8L, "공구8", GroupBuyStatus.RECRUITING),
            createGroupBuy(9L, "공구9", GroupBuyStatus.RECRUITING),
            createGroupBuy(10L, "공구10", GroupBuyStatus.RECRUITING)
        );
        
        List<GroupBuy> secondPageGroupBuys = Arrays.asList(
            createGroupBuy(11L, "공구11", GroupBuyStatus.RECRUITING),
            createGroupBuy(12L, "공구12", GroupBuyStatus.RECRUITING),
            createGroupBuy(13L, "공구13", GroupBuyStatus.RECRUITING),
            createGroupBuy(14L, "공구14", GroupBuyStatus.RECRUITING),
            createGroupBuy(15L, "공구15", GroupBuyStatus.RECRUITING)
        );
        
        Pageable firstPage = PageRequest.of(0, 10);
        Pageable secondPage = PageRequest.of(1, 10);
        
        Page<GroupBuy> firstPageResult = new PageImpl<>(firstPageGroupBuys, firstPage, 15);
        Page<GroupBuy> secondPageResult = new PageImpl<>(secondPageGroupBuys, secondPage, 15);
        
        given(userRepository.findById(testUser.getId())).willReturn(Optional.of(testUser));
        given(groupBuyRepository.findByHostIdAndStatusIn(eq(testUser.getId()), anyList(), eq(firstPage)))
            .willReturn(firstPageResult);
        given(groupBuyRepository.findByHostIdAndStatusIn(eq(testUser.getId()), anyList(), eq(secondPage)))
            .willReturn(secondPageResult);
        given(groupBuyImageRepository.findByGroupBuyIdInOrderByGroupBuyIdAndDisplayOrder(anyList()))
            .willReturn(List.of());
        
        // when
        Page<GroupBuyResponse> firstResult = userService.getMyGroupBuys(testUser.getId(), null, firstPage);
        Page<GroupBuyResponse> secondResult = userService.getMyGroupBuys(testUser.getId(), null, secondPage);
        
        // then
        assertThat(firstResult.getTotalElements()).isEqualTo(15);
        assertThat(firstResult.getContent()).hasSize(10);
        assertThat(firstResult.getTotalPages()).isEqualTo(2);
        assertThat(secondResult.getContent()).hasSize(5);

        verify(userRepository, times(2)).findById(testUser.getId());
    }

    @Test
    @DisplayName("존재하지 않는 사용자의 공구 목록 조회 시 예외가 발생한다")
    void getMyGroupBuys_userNotFound() {
        // given
        Long nonExistentUserId = 999L;
        Pageable pageable = PageRequest.of(0, 10);
        
        given(userRepository.findById(nonExistentUserId)).willReturn(Optional.empty());
        
        // when & then
        assertThatThrownBy(() -> userService.getMyGroupBuys(nonExistentUserId, null, pageable))
                .isInstanceOf(CustomException.class)
                .hasMessage("사용자를 찾을 수 없습니다.");

        verify(userRepository).findById(nonExistentUserId);
        verify(groupBuyRepository, never()).findByHostIdAndStatusIn(anyLong(), anyList(), any(Pageable.class));
    }

    private GroupBuy createGroupBuy(Long id, String title, GroupBuyStatus status) throws Exception {
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
        setGroupBuyId(groupBuy, id);
        return groupBuy;
    }

    @Test
    @DisplayName("사용자가 참여한 모든 공구 목록을 조회한다")
    void getParticipatedGroupBuys_all() throws Exception {
        // given
        GroupBuy groupBuy1 = createGroupBuy("공구1", GroupBuyStatus.RECRUITING);
        setGroupBuyId(groupBuy1, 1L);
        GroupBuy groupBuy2 = createGroupBuy("공구2", GroupBuyStatus.IMMINENT);
        setGroupBuyId(groupBuy2, 2L);
        GroupBuy groupBuy3 = createGroupBuy("공구3", GroupBuyStatus.CLOSED);
        setGroupBuyId(groupBuy3, 3L);
        
        Participation participation1 = createParticipation(1L, participant, groupBuy1, DeliveryMethod.DIRECT);
        Participation participation2 = createParticipation(2L, participant, groupBuy2, DeliveryMethod.PARCEL);
        Participation participation3 = createParticipation(3L, participant, groupBuy3, DeliveryMethod.DIRECT);
        
        Pageable pageable = PageRequest.of(0, 10);
        Page<Participation> mockPage = new PageImpl<>(
            Arrays.asList(participation1, participation2, participation3), 
            pageable, 
            3
        );
        
        given(userRepository.findById(participant.getId())).willReturn(Optional.of(participant));
        given(participationRepository.findByUserIdWithGroupBuyAndHost(eq(participant.getId()), anyList(), eq(pageable)))
            .willReturn(mockPage);
        given(groupBuyImageRepository.findByGroupBuyIdInOrderByGroupBuyIdAndDisplayOrder(anyList()))
            .willReturn(List.of());
        
        // when
        Page<GroupBuyResponse> result = userService.getParticipatedGroupBuys(participant.getId(), null, pageable);
        
        // then
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent().get(0).getTitle()).isIn("공구1", "공구2", "공구3");
        
        verify(userRepository).findById(participant.getId());
        verify(participationRepository).findByUserIdWithGroupBuyAndHost(eq(participant.getId()), anyList(), eq(pageable));
    }

    @Test
    @DisplayName("사용자가 참여한 공구 목록을 상태별로 필터링하여 조회한다")
    void getParticipatedGroupBuys_filterByStatus() throws Exception {
        // given
        GroupBuy recruitingGroupBuy1 = createGroupBuy("모집중공구1", GroupBuyStatus.RECRUITING);
        setGroupBuyId(recruitingGroupBuy1, 1L);
        GroupBuy recruitingGroupBuy2 = createGroupBuy("모집중공구2", GroupBuyStatus.RECRUITING);
        setGroupBuyId(recruitingGroupBuy2, 2L);
        GroupBuy imminentGroupBuy = createGroupBuy("마감임박공구", GroupBuyStatus.IMMINENT);
        setGroupBuyId(imminentGroupBuy, 3L);
        GroupBuy closedGroupBuy = createGroupBuy("마감공구", GroupBuyStatus.CLOSED);
        setGroupBuyId(closedGroupBuy, 4L);
        
        Participation participation1 = createParticipation(1L, participant, recruitingGroupBuy1, DeliveryMethod.DIRECT);
        Participation participation2 = createParticipation(2L, participant, recruitingGroupBuy2, DeliveryMethod.DIRECT);
        Participation participation3 = createParticipation(3L, participant, imminentGroupBuy, DeliveryMethod.PARCEL);
        Participation participation4 = createParticipation(4L, participant, closedGroupBuy, DeliveryMethod.DIRECT);
        
        Pageable pageable = PageRequest.of(0, 10);
        
        Page<Participation> recruitingPage = new PageImpl<>(
            Arrays.asList(participation1, participation2), 
            pageable, 
            2
        );
        Page<Participation> imminentPage = new PageImpl<>(
            List.of(participation3), 
            pageable, 
            1
        );
        Page<Participation> closedPage = new PageImpl<>(
            List.of(participation4), 
            pageable, 
            1
        );
        
        given(userRepository.findById(participant.getId())).willReturn(Optional.of(participant));
        given(participationRepository.findByUserIdWithGroupBuyAndHost(eq(participant.getId()), eq(List.of(GroupBuyStatus.RECRUITING)), eq(pageable)))
            .willReturn(recruitingPage);
        given(participationRepository.findByUserIdWithGroupBuyAndHost(eq(participant.getId()), eq(List.of(GroupBuyStatus.IMMINENT)), eq(pageable)))
            .willReturn(imminentPage);
        given(participationRepository.findByUserIdWithGroupBuyAndHost(eq(participant.getId()), eq(List.of(GroupBuyStatus.CLOSED)), eq(pageable)))
            .willReturn(closedPage);
        given(groupBuyImageRepository.findByGroupBuyIdInOrderByGroupBuyIdAndDisplayOrder(anyList()))
            .willReturn(List.of());
        
        // when
        Page<GroupBuyResponse> recruitingResult = userService.getParticipatedGroupBuys(participant.getId(), GroupBuyStatus.RECRUITING, pageable);
        Page<GroupBuyResponse> imminentResult = userService.getParticipatedGroupBuys(participant.getId(), GroupBuyStatus.IMMINENT, pageable);
        Page<GroupBuyResponse> closedResult = userService.getParticipatedGroupBuys(participant.getId(), GroupBuyStatus.CLOSED, pageable);
        
        // then
        assertThat(recruitingResult.getTotalElements()).isEqualTo(2);
        assertThat(imminentResult.getTotalElements()).isEqualTo(1);
        assertThat(closedResult.getTotalElements()).isEqualTo(1);
        
        verify(userRepository, times(3)).findById(participant.getId());
    }

    @Test
    @DisplayName("사용자가 참여한 공구 목록을 페이징하여 조회한다")
    void getParticipatedGroupBuys_pagination() throws Exception {
        // given
        List<Participation> firstPageParticipations = List.of(
            createParticipation(1L, participant, createGroupBuyWithId(1L, "공구1"), DeliveryMethod.DIRECT),
            createParticipation(2L, participant, createGroupBuyWithId(2L, "공구2"), DeliveryMethod.DIRECT),
            createParticipation(3L, participant, createGroupBuyWithId(3L, "공구3"), DeliveryMethod.DIRECT),
            createParticipation(4L, participant, createGroupBuyWithId(4L, "공구4"), DeliveryMethod.DIRECT),
            createParticipation(5L, participant, createGroupBuyWithId(5L, "공구5"), DeliveryMethod.DIRECT),
            createParticipation(6L, participant, createGroupBuyWithId(6L, "공구6"), DeliveryMethod.DIRECT),
            createParticipation(7L, participant, createGroupBuyWithId(7L, "공구7"), DeliveryMethod.DIRECT),
            createParticipation(8L, participant, createGroupBuyWithId(8L, "공구8"), DeliveryMethod.DIRECT),
            createParticipation(9L, participant, createGroupBuyWithId(9L, "공구9"), DeliveryMethod.DIRECT),
            createParticipation(10L, participant, createGroupBuyWithId(10L, "공구10"), DeliveryMethod.DIRECT)
        );
        
        List<Participation> secondPageParticipations = List.of(
            createParticipation(11L, participant, createGroupBuyWithId(11L, "공구11"), DeliveryMethod.DIRECT),
            createParticipation(12L, participant, createGroupBuyWithId(12L, "공구12"), DeliveryMethod.DIRECT),
            createParticipation(13L, participant, createGroupBuyWithId(13L, "공구13"), DeliveryMethod.DIRECT),
            createParticipation(14L, participant, createGroupBuyWithId(14L, "공구14"), DeliveryMethod.DIRECT),
            createParticipation(15L, participant, createGroupBuyWithId(15L, "공구15"), DeliveryMethod.DIRECT)
        );
        
        Pageable firstPage = PageRequest.of(0, 10);
        Pageable secondPage = PageRequest.of(1, 10);
        
        Page<Participation> firstPageResult = new PageImpl<>(firstPageParticipations, firstPage, 15);
        Page<Participation> secondPageResult = new PageImpl<>(secondPageParticipations, secondPage, 15);
        
        given(userRepository.findById(participant.getId())).willReturn(Optional.of(participant));
        given(participationRepository.findByUserIdWithGroupBuyAndHost(eq(participant.getId()), anyList(), eq(firstPage)))
            .willReturn(firstPageResult);
        given(participationRepository.findByUserIdWithGroupBuyAndHost(eq(participant.getId()), anyList(), eq(secondPage)))
            .willReturn(secondPageResult);
        given(groupBuyImageRepository.findByGroupBuyIdInOrderByGroupBuyIdAndDisplayOrder(anyList()))
            .willReturn(List.of());
        
        // when
        Page<GroupBuyResponse> firstResult = userService.getParticipatedGroupBuys(participant.getId(), null, firstPage);
        Page<GroupBuyResponse> secondResult = userService.getParticipatedGroupBuys(participant.getId(), null, secondPage);
        
        // then
        assertThat(firstResult.getTotalElements()).isEqualTo(15);
        assertThat(firstResult.getContent()).hasSize(10);
        assertThat(firstResult.getTotalPages()).isEqualTo(2);
        assertThat(secondResult.getContent()).hasSize(5);
        
        verify(userRepository, times(2)).findById(participant.getId());
    }
    
    private GroupBuy createGroupBuyWithId(Long id, String title) throws Exception {
        GroupBuy groupBuy = createGroupBuy(title, GroupBuyStatus.RECRUITING);
        setGroupBuyId(groupBuy, id);
        return groupBuy;
    }

    @Test
    @DisplayName("사용자가 참여한 공구가 없으면 빈 목록을 반환한다")
    void getParticipatedGroupBuys_empty() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Participation> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        
        given(userRepository.findById(participant.getId())).willReturn(Optional.of(participant));
        given(participationRepository.findByUserIdWithGroupBuyAndHost(eq(participant.getId()), anyList(), eq(pageable)))
            .willReturn(emptyPage);
        
        // when
        Page<GroupBuyResponse> result = userService.getParticipatedGroupBuys(participant.getId(), null, pageable);
        
        // then
        assertThat(result.getTotalElements()).isEqualTo(0);
        assertThat(result.getContent()).isEmpty();
        
        verify(userRepository).findById(participant.getId());
        verify(participationRepository).findByUserIdWithGroupBuyAndHost(eq(participant.getId()), anyList(), eq(pageable));
    }

    @Test
    @DisplayName("존재하지 않는 사용자의 참여 공구 목록 조회 시 예외가 발생한다")
    void getParticipatedGroupBuys_userNotFound() {
        // given
        Long nonExistentUserId = 999L;
        Pageable pageable = PageRequest.of(0, 10);
        
        given(userRepository.findById(nonExistentUserId)).willReturn(Optional.empty());
        
        // when & then
        assertThatThrownBy(() -> userService.getParticipatedGroupBuys(nonExistentUserId, null, pageable))
                .isInstanceOf(CustomException.class)
                .hasMessage("사용자를 찾을 수 없습니다.");
        
        verify(userRepository).findById(nonExistentUserId);
        verify(participationRepository, never()).findByUserIdWithGroupBuyAndHost(anyLong(), anyList(), any(Pageable.class));
    }

    private GroupBuy createGroupBuy(String title, GroupBuyStatus status) {
        return GroupBuy.builder()
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
    }

    private Participation createParticipation(Long id, User user, GroupBuy groupBuy, DeliveryMethod deliveryMethod) throws Exception {
        Participation participation = Participation.create(user, groupBuy, 1, deliveryMethod);
        setParticipationId(participation, id);
        return participation;
    }
}
