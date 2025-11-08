//package com.recipemate.domain.groupbuy.service;
//
//import com.recipemate.domain.groupbuy.entity.GroupBuy;
//import com.recipemate.domain.groupbuy.repository.GroupBuyRepository;
//import com.recipemate.domain.notification.entity.Notification;
//import com.recipemate.domain.notification.repository.NotificationRepository;
//import com.recipemate.domain.user.entity.User;
//import com.recipemate.domain.user.repository.UserRepository;
//import com.recipemate.domain.wishlist.entity.Wishlist;
//import com.recipemate.domain.wishlist.repository.WishlistRepository;
//import com.recipemate.global.common.DeliveryMethod;
//import com.recipemate.global.common.GroupBuyStatus;
//import com.recipemate.global.common.NotificationType;
//import com.recipemate.global.common.UserRole;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@SpringBootTest
//@Transactional
//@ActiveProfiles("test")
//@DisplayName("공구 상태 자동 변경 배치 테스트")
//class GroupBuySchedulerTest {
//
//    @Autowired
//    private GroupBuyScheduler groupBuyScheduler;
//
//    @Autowired
//    private GroupBuyRepository groupBuyRepository;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private WishlistRepository wishlistRepository;
//
//    @Autowired
//    private NotificationRepository notificationRepository;
//
//    private User host;
//    private User participant;
//
//    @BeforeEach
//    void setUp() {
//        // 테스트용 사용자 생성
//        host = User.builder()
//                .email("host@example.com")
//                .password("password123")
//                .nickname("주최자")
//                .phoneNumber("010-1234-5678")
//                .mannerTemperature(36.5)
//                .role(UserRole.USER)
//                .build();
//        userRepository.save(host);
//
//        participant = User.builder()
//                .email("participant@example.com")
//                .password("password123")
//                .nickname("참여자")
//                .phoneNumber("010-9876-5432")
//                .mannerTemperature(36.5)
//                .role(UserRole.USER)
//                .build();
//        userRepository.save(participant);
//    }
//
//    @Test
//    @DisplayName("마감일이 지난 공구는 CLOSED 상태로 변경된다")
//    void updateExpiredGroupBuysToClosed() {
//        // Given: 마감일이 지난 RECRUITING 상태의 공구
//        GroupBuy expiredGroupBuy = createGroupBuy(
//                "마감된 공구",
//                LocalDateTime.now().minusDays(1), // 어제 마감
//                GroupBuyStatus.RECRUITING
//        );
//        groupBuyRepository.save(expiredGroupBuy);
//
//        // Given: 아직 마감되지 않은 공구
//        GroupBuy activeGroupBuy = createGroupBuy(
//                "진행중인 공구",
//                LocalDateTime.now().plusDays(3), // 3일 후 마감
//                GroupBuyStatus.RECRUITING
//        );
//        groupBuyRepository.save(activeGroupBuy);
//
//        // When: 배치 실행
//        groupBuyScheduler.updateGroupBuyStatus();
//
//        // Then: 마감일이 지난 공구만 CLOSED로 변경
//        GroupBuy updatedExpired = groupBuyRepository.findById(expiredGroupBuy.getId()).orElseThrow();
//        assertThat(updatedExpired.getStatus()).isEqualTo(GroupBuyStatus.CLOSED);
//
//        GroupBuy updatedActive = groupBuyRepository.findById(activeGroupBuy.getId()).orElseThrow();
//        assertThat(updatedActive.getStatus()).isEqualTo(GroupBuyStatus.RECRUITING);
//    }
//
//    @Test
//    @DisplayName("마감일이 지난 IMMINENT 공구도 CLOSED로 변경된다")
//    void updateExpiredImminentGroupBuysToClosed() {
//        // Given: 마감일이 지난 IMMINENT 상태의 공구
//        GroupBuy expiredImminentGroupBuy = createGroupBuy(
//                "마감된 임박 공구",
//                LocalDateTime.now().minusHours(1), // 1시간 전 마감
//                GroupBuyStatus.IMMINENT
//        );
//        groupBuyRepository.save(expiredImminentGroupBuy);
//
//        // When: 배치 실행
//        groupBuyScheduler.updateGroupBuyStatus();
//
//        // Then: CLOSED로 변경
//        GroupBuy updated = groupBuyRepository.findById(expiredImminentGroupBuy.getId()).orElseThrow();
//        assertThat(updated.getStatus()).isEqualTo(GroupBuyStatus.CLOSED);
//    }
//
//    @Test
//    @DisplayName("D-1 공구는 IMMINENT 상태로 변경된다")
//    void updateImminentGroupBuysOneDayBefore() {
//        // Given: 내일 마감인 RECRUITING 공구
//        GroupBuy tomorrowGroupBuy = createGroupBuy(
//                "내일 마감 공구",
//                LocalDateTime.now().plusDays(1).minusHours(1), // 약 23시간 후 마감
//                GroupBuyStatus.RECRUITING
//        );
//        groupBuyRepository.save(tomorrowGroupBuy);
//
//        // When: 배치 실행
//        groupBuyScheduler.updateGroupBuyStatus();
//
//        // Then: IMMINENT로 변경
//        GroupBuy updated = groupBuyRepository.findById(tomorrowGroupBuy.getId()).orElseThrow();
//        assertThat(updated.getStatus()).isEqualTo(GroupBuyStatus.IMMINENT);
//    }
//
//    @Test
//    @DisplayName("D-2 공구는 IMMINENT 상태로 변경된다")
//    void updateImminentGroupBuysTwoDaysBefore() {
//        // Given: 2일 후 마감인 RECRUITING 공구
//        GroupBuy twoDaysGroupBuy = createGroupBuy(
//                "2일 후 마감 공구",
//                LocalDateTime.now().plusDays(2).minusHours(1), // 약 47시간 후 마감
//                GroupBuyStatus.RECRUITING
//        );
//        groupBuyRepository.save(twoDaysGroupBuy);
//
//        // When: 배치 실행
//        groupBuyScheduler.updateGroupBuyStatus();
//
//        // Then: IMMINENT로 변경
//        GroupBuy updated = groupBuyRepository.findById(twoDaysGroupBuy.getId()).orElseThrow();
//        assertThat(updated.getStatus()).isEqualTo(GroupBuyStatus.IMMINENT);
//    }
//
//    @Test
//    @DisplayName("D-3 이후 공구는 RECRUITING 상태를 유지한다")
//    void keepRecruitingStatusForDistantDeadline() {
//        // Given: 3일 후 마감인 공구
//        GroupBuy distantGroupBuy = createGroupBuy(
//                "여유있는 공구",
//                LocalDateTime.now().plusDays(3).plusHours(1), // 3일 1시간 후
//                GroupBuyStatus.RECRUITING
//        );
//        groupBuyRepository.save(distantGroupBuy);
//
//        // When: 배치 실행
//        groupBuyScheduler.updateGroupBuyStatus();
//
//        // Then: RECRUITING 유지
//        GroupBuy updated = groupBuyRepository.findById(distantGroupBuy.getId()).orElseThrow();
//        assertThat(updated.getStatus()).isEqualTo(GroupBuyStatus.RECRUITING);
//    }
//
//    @Test
//    @DisplayName("찜한 공구가 D-1이면 알림을 발송한다")
//    void sendNotificationForWishlistedGroupBuys() {
//        // Given: 내일 마감인 공구
//        GroupBuy tomorrowGroupBuy = createGroupBuy(
//                "내일 마감 공구",
//                LocalDateTime.now().plusDays(1).minusHours(1),
//                GroupBuyStatus.RECRUITING
//        );
//        groupBuyRepository.save(tomorrowGroupBuy);
//
//        // Given: 참여자가 찜한 공구
//        Wishlist wishlist = Wishlist.create(participant, tomorrowGroupBuy);
//        wishlistRepository.save(wishlist);
//
//        // When: 배치 실행
//        groupBuyScheduler.sendDeadlineNotifications();
//
//        // Then: 알림 생성 확인
//        List<Notification> notifications = notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(
//                participant.getId(), false
//        );
//        assertThat(notifications).isNotEmpty();
//        assertThat(notifications.get(0).getType()).isEqualTo(NotificationType.GROUP_BUY_DEADLINE);
//        assertThat(notifications.get(0).getContent()).contains(tomorrowGroupBuy.getTitle());
//    }
//
//    @Test
//    @DisplayName("여러 사용자가 찜한 공구의 경우 각각 알림을 발송한다")
//    void sendNotificationsToMultipleUsers() {
//        // Given: 내일 마감인 공구
//        GroupBuy tomorrowGroupBuy = createGroupBuy(
//                "인기 공구",
//                LocalDateTime.now().plusDays(1),
//                GroupBuyStatus.RECRUITING
//        );
//        groupBuyRepository.save(tomorrowGroupBuy);
//
//        // Given: 두 사용자가 찜함
//        Wishlist wishlist1 = Wishlist.create(participant, tomorrowGroupBuy);
//        wishlistRepository.save(wishlist1);
//
//        User anotherUser = User.builder()
//                .email("another@example.com")
//                .password("password123")
//                .nickname("다른참여자")
//                .phoneNumber("010-1111-2222")
//                .mannerTemperature(36.5)
//                .role(UserRole.USER)
//                .build();
//        userRepository.save(anotherUser);
//
//        Wishlist wishlist2 = Wishlist.create(anotherUser, tomorrowGroupBuy);
//        wishlistRepository.save(wishlist2);
//
//        // When: 배치 실행
//        groupBuyScheduler.sendDeadlineNotifications();
//
//        // Then: 두 사용자 모두 알림 수신
//        List<Notification> notifications1 = notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(
//                participant.getId(), false
//        );
//        List<Notification> notifications2 = notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(
//                anotherUser.getId(), false
//        );
//
//        assertThat(notifications1).hasSize(1);
//        assertThat(notifications2).hasSize(1);
//    }
//
//    @Test
//    @DisplayName("CLOSED 상태의 공구는 상태 변경하지 않는다")
//    void doNotUpdateClosedGroupBuys() {
//        // Given: 이미 CLOSED 상태인 공구 (마감일이 지나지 않았어도)
//        GroupBuy closedGroupBuy = createGroupBuy(
//                "이미 종료된 공구",
//                LocalDateTime.now().plusDays(1),
//                GroupBuyStatus.CLOSED
//        );
//        groupBuyRepository.save(closedGroupBuy);
//
//        // When: 배치 실행
//        groupBuyScheduler.updateGroupBuyStatus();
//
//        // Then: CLOSED 상태 유지
//        GroupBuy updated = groupBuyRepository.findById(closedGroupBuy.getId()).orElseThrow();
//        assertThat(updated.getStatus()).isEqualTo(GroupBuyStatus.CLOSED);
//    }
//
//    @Test
//    @DisplayName("D-3 이후의 찜한 공구에는 알림을 발송하지 않는다")
//    void doNotSendNotificationForDistantDeadline() {
//        // Given: 3일 후 마감인 공구
//        GroupBuy distantGroupBuy = createGroupBuy(
//                "여유있는 공구",
//                LocalDateTime.now().plusDays(3),
//                GroupBuyStatus.RECRUITING
//        );
//        groupBuyRepository.save(distantGroupBuy);
//
//        // Given: 참여자가 찜함
//        Wishlist wishlist = Wishlist.create(participant, distantGroupBuy);
//        wishlistRepository.save(wishlist);
//
//        // When: 배치 실행
//        groupBuyScheduler.sendDeadlineNotifications();
//
//        // Then: 알림 생성되지 않음
//        List<Notification> notifications = notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(
//                participant.getId(), false
//        );
//        assertThat(notifications).isEmpty();
//    }
//
//    @Test
//    @DisplayName("같은 공구에 대해 중복 알림을 발송하지 않는다")
//    void doNotSendDuplicateNotifications() {
//        // Given: 내일 마감인 공구
//        GroupBuy tomorrowGroupBuy = createGroupBuy(
//                "내일 마감 공구",
//                LocalDateTime.now().plusDays(1),
//                GroupBuyStatus.RECRUITING
//        );
//        groupBuyRepository.save(tomorrowGroupBuy);
//
//        // Given: 참여자가 찜함
//        Wishlist wishlist = Wishlist.create(participant, tomorrowGroupBuy);
//        wishlistRepository.save(wishlist);
//
//        // When: 배치 실행 (첫 번째)
//        groupBuyScheduler.sendDeadlineNotifications();
//
//        // When: 배치 다시 실행 (두 번째)
//        groupBuyScheduler.sendDeadlineNotifications();
//
//        // Then: 알림은 1개만 존재 (중복 발송 방지)
//        List<Notification> notifications = notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(
//                participant.getId(), false
//        );
//        assertThat(notifications).hasSize(1);
//    }
//
//    // 헬퍼 메서드
//    private GroupBuy createGroupBuy(String title, LocalDateTime deadline, GroupBuyStatus status) {
//        return GroupBuy.builder()
//                .host(host)
//                .title(title)
//                .content("테스트 공구 내용")
//                .category("식재료")
//                .totalPrice(50000)
//                .targetHeadcount(10)
//                .currentHeadcount(5)
//                .deadline(deadline)
//                .deliveryMethod(DeliveryMethod.BOTH)
//                .meetupLocation("서울시 강남구")
//                .parcelFee(3000)
//                .isParticipantListPublic(true)
//                .status(status)
//                .build();
//    }
//}
