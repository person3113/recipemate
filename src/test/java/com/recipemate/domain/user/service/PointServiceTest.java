//package com.recipemate.domain.user.service;
//
//import com.recipemate.domain.user.dto.PointHistoryResponse;
//import com.recipemate.domain.user.entity.PointHistory;
//import com.recipemate.domain.user.entity.User;
//import com.recipemate.domain.user.repository.PointHistoryRepository;
//import com.recipemate.domain.user.repository.UserRepository;
//import com.recipemate.global.common.PointType;
//import com.recipemate.global.exception.CustomException;
//import com.recipemate.global.exception.ErrorCode;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.BDDMockito.given;
//import static org.mockito.BDDMockito.then;
//
//@ExtendWith(MockitoExtension.class)
//@DisplayName("PointService 테스트")
//class PointServiceTest {
//
//    @Mock
//    private UserRepository userRepository;
//
//    @Mock
//    private PointHistoryRepository pointHistoryRepository;
//
//    @InjectMocks
//    private PointService pointService;
//
//    private User testUser;
//
//    @BeforeEach
//    void setUp() {
//        testUser = User.builder()
//                .id(1L)
//                .email("test@example.com")
//                .nickname("테스터")
//                .points(100)
//                .build();
//    }
//
//    @Test
//    @DisplayName("포인트 적립 - 성공 (하루 1회 제한 통과)")
//    void earnPoints_Success() {
//        // given
//        Long userId = testUser.getId();
//        int amount = 50;
//        String description = "공동구매 생성";
//
//        given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
//        given(pointHistoryRepository.existsByUserAndDescriptionAndCreatedAtBetween(
//                eq(testUser), eq(description), any(LocalDateTime.class), any(LocalDateTime.class)))
//                .willReturn(false);
//
//        // when
//        pointService.earnPoints(userId, amount, description);
//
//        // then
//        assertThat(testUser.getPoints()).isEqualTo(150);
//
//        ArgumentCaptor<PointHistory> captor = ArgumentCaptor.forClass(PointHistory.class);
//        then(pointHistoryRepository).should().save(captor.capture());
//
//        PointHistory savedHistory = captor.getValue();
//        assertThat(savedHistory.getUser()).isEqualTo(testUser);
//        assertThat(savedHistory.getAmount()).isEqualTo(amount);
//        assertThat(savedHistory.getDescription()).isEqualTo(description);
//        assertThat(savedHistory.getType()).isEqualTo(PointType.EARN);
//    }
//
//    @Test
//    @DisplayName("포인트 적립 - 하루 1회 제한으로 중복 지급 방지")
//    void earnPoints_AlreadyEarnedToday_DoesNothing() {
//        // given
//        Long userId = testUser.getId();
//        int amount = 50;
//        String description = "공동구매 생성";
//        int initialPoints = testUser.getPoints();
//
//        given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
//        given(pointHistoryRepository.existsByUserAndDescriptionAndCreatedAtBetween(
//                eq(testUser), eq(description), any(LocalDateTime.class), any(LocalDateTime.class)))
//                .willReturn(true);
//
//        // when
//        pointService.earnPoints(userId, amount, description);
//
//        // then
//        assertThat(testUser.getPoints()).isEqualTo(initialPoints); // 포인트 변경 없음
//        then(pointHistoryRepository).shouldHaveNoInteractions(); // save 호출 안 됨
//    }
//
//    @Test
//    @DisplayName("포인트 적립 - 사용자 없음 예외")
//    void earnPoints_UserNotFound_ThrowsException() {
//        // given
//        Long userId = 999L;
//        int amount = 50;
//        String description = "공동구매 생성";
//
//        given(userRepository.findById(userId)).willReturn(Optional.empty());
//
//        // when & then
//        assertThatThrownBy(() -> pointService.earnPoints(userId, amount, description))
//                .isInstanceOf(CustomException.class)
//                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
//    }
//
//    @Test
//    @DisplayName("출석 체크 - 성공")
//    void dailyCheckIn_Success() {
//        // given
//        Long userId = testUser.getId();
//        int initialPoints = testUser.getPoints();
//
//        given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
//        given(pointHistoryRepository.existsByUserAndDescriptionAndCreatedAtBetween(
//                eq(testUser), eq("출석 체크"), any(LocalDateTime.class), any(LocalDateTime.class)))
//                .willReturn(false);
//
//        // when
//        pointService.dailyCheckIn(userId);
//
//        // then
//        assertThat(testUser.getPoints()).isEqualTo(initialPoints + 5);
//
//        ArgumentCaptor<PointHistory> captor = ArgumentCaptor.forClass(PointHistory.class);
//        then(pointHistoryRepository).should().save(captor.capture());
//
//        PointHistory savedHistory = captor.getValue();
//        assertThat(savedHistory.getUser()).isEqualTo(testUser);
//        assertThat(savedHistory.getAmount()).isEqualTo(5);
//        assertThat(savedHistory.getDescription()).isEqualTo("출석 체크");
//        assertThat(savedHistory.getType()).isEqualTo(PointType.EARN);
//    }
//
//    @Test
//    @DisplayName("출석 체크 - 이미 오늘 출석한 경우 예외 발생")
//    void dailyCheckIn_AlreadyCheckedIn_ThrowsException() {
//        // given
//        Long userId = testUser.getId();
//        int initialPoints = testUser.getPoints();
//
//        given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
//        given(pointHistoryRepository.existsByUserAndDescriptionAndCreatedAtBetween(
//                eq(testUser), eq("출석 체크"), any(LocalDateTime.class), any(LocalDateTime.class)))
//                .willReturn(true);
//
//        // when & then
//        assertThatThrownBy(() -> pointService.dailyCheckIn(userId))
//                .isInstanceOf(CustomException.class)
//                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_CHECKED_IN_TODAY);
//
//        assertThat(testUser.getPoints()).isEqualTo(initialPoints); // 포인트 변경 없음
//    }
//
//    @Test
//    @DisplayName("출석 체크 - 사용자 없음 예외")
//    void dailyCheckIn_UserNotFound_ThrowsException() {
//        // given
//        Long userId = 999L;
//
//        given(userRepository.findById(userId)).willReturn(Optional.empty());
//
//        // when & then
//        assertThatThrownBy(() -> pointService.dailyCheckIn(userId))
//                .isInstanceOf(CustomException.class)
//                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
//    }
//
//    @Test
//    @DisplayName("포인트 사용 - 성공")
//    void usePoints_Success() {
//        // given
//        Long userId = testUser.getId();
//        int amount = 30;
//        String description = "상품 구매";
//
//        given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
//
//        // when
//        pointService.usePoints(userId, amount, description);
//
//        // then
//        assertThat(testUser.getPoints()).isEqualTo(70);
//
//        ArgumentCaptor<PointHistory> captor = ArgumentCaptor.forClass(PointHistory.class);
//        then(pointHistoryRepository).should().save(captor.capture());
//
//        PointHistory savedHistory = captor.getValue();
//        assertThat(savedHistory.getUser()).isEqualTo(testUser);
//        assertThat(savedHistory.getAmount()).isEqualTo(amount);
//        assertThat(savedHistory.getDescription()).isEqualTo(description);
//        assertThat(savedHistory.getType()).isEqualTo(PointType.USE);
//    }
//
//    @Test
//    @DisplayName("포인트 사용 - 잔액 부족 예외")
//    void usePoints_InsufficientPoints_ThrowsException() {
//        // given
//        Long userId = testUser.getId();
//        int amount = 200;
//        String description = "상품 구매";
//
//        given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
//
//        // when & then
//        assertThatThrownBy(() -> pointService.usePoints(userId, amount, description))
//                .isInstanceOf(CustomException.class)
//                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INSUFFICIENT_POINTS);
//
//        assertThat(testUser.getPoints()).isEqualTo(100); // 포인트는 변경되지 않음
//    }
//
//    @Test
//    @DisplayName("포인트 사용 - 사용자 없음 예외")
//    void usePoints_UserNotFound_ThrowsException() {
//        // given
//        Long userId = 999L;
//        int amount = 30;
//        String description = "상품 구매";
//
//        given(userRepository.findById(userId)).willReturn(Optional.empty());
//
//        // when & then
//        assertThatThrownBy(() -> pointService.usePoints(userId, amount, description))
//                .isInstanceOf(CustomException.class)
//                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
//    }
//
//    @Test
//    @DisplayName("포인트 내역 조회 - 성공")
//    void getPointHistory_Success() {
//        // given
//        Long userId = testUser.getId();
//        Pageable pageable = PageRequest.of(0, 10);
//
//        PointHistory history1 = PointHistory.builder()
//                .id(1L)
//                .user(testUser)
//                .amount(50)
//                .description("공동구매 생성")
//                .type(PointType.EARN)
//                .build();
//
//        PointHistory history2 = PointHistory.builder()
//                .id(2L)
//                .user(testUser)
//                .amount(30)
//                .description("상품 구매")
//                .type(PointType.USE)
//                .build();
//
//        Page<PointHistory> historyPage = new PageImpl<>(List.of(history1, history2));
//
//        given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
//        given(pointHistoryRepository.findByUserOrderByCreatedAtDesc(testUser, pageable))
//                .willReturn(historyPage);
//
//        // when
//        Page<PointHistoryResponse> result = pointService.getPointHistory(userId, pageable);
//
//        // then
//        assertThat(result.getTotalElements()).isEqualTo(2);
//        assertThat(result.getContent()).hasSize(2);
//        assertThat(result.getContent().get(0).amount()).isEqualTo(50);
//        assertThat(result.getContent().get(0).description()).isEqualTo("공동구매 생성");
//        assertThat(result.getContent().get(0).type()).isEqualTo(PointType.EARN);
//    }
//
//    @Test
//    @DisplayName("포인트 내역 조회 - 사용자 없음 예외")
//    void getPointHistory_UserNotFound_ThrowsException() {
//        // given
//        Long userId = 999L;
//        Pageable pageable = PageRequest.of(0, 10);
//
//        given(userRepository.findById(userId)).willReturn(Optional.empty());
//
//        // when & then
//        assertThatThrownBy(() -> pointService.getPointHistory(userId, pageable))
//                .isInstanceOf(CustomException.class)
//                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
//    }
//}
