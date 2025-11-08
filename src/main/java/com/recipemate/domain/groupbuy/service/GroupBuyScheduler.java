package com.recipemate.domain.groupbuy.service;

import com.recipemate.domain.groupbuy.entity.GroupBuy;
import com.recipemate.domain.groupbuy.repository.GroupBuyRepository;
import com.recipemate.domain.notification.entity.Notification;
import com.recipemate.domain.notification.repository.NotificationRepository;
import com.recipemate.domain.wishlist.entity.Wishlist;
import com.recipemate.domain.wishlist.repository.WishlistRepository;
import com.recipemate.global.common.EntityType;
import com.recipemate.global.common.GroupBuyStatus;
import com.recipemate.global.common.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 공구 상태 자동 업데이트 배치 스케줄러
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GroupBuyScheduler {

    private final GroupBuyRepository groupBuyRepository;
    private final WishlistRepository wishlistRepository;
    private final NotificationRepository notificationRepository;

    /**
     * 서버 시작 시 공구 상태 업데이트
     * - 서버 재시작 중 누락된 상태 업데이트를 보완
     */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void onApplicationReady() {
        log.info("서버 시작 - 공구 상태 일괄 업데이트 시작");
        
        LocalDateTime now = LocalDateTime.now();
        
        // 1. 마감일이 지난 공구 → CLOSED
        updateExpiredGroupBuys(now);
        
        // 2. D-1, D-2 공구 → IMMINENT
        updateImminentGroupBuys(now);
        
        log.info("서버 시작 - 공구 상태 일괄 업데이트 완료");
    }

    /**
     * 공구 상태 자동 업데이트
     * - 마감일이 지난 공구: RECRUITING/IMMINENT → CLOSED
     * - D-1 또는 D-2 공구: RECRUITING → IMMINENT
     * 매 시간 정각에 실행
     */
    @Scheduled(cron = "0 0 * * * *") // 매시 00분 00초
    @Transactional
    public void updateGroupBuyStatus() {
        log.info("공구 상태 자동 업데이트 배치 시작");

        LocalDateTime now = LocalDateTime.now();

        // 1. 마감일이 지난 공구 → CLOSED
        updateExpiredGroupBuys(now);

        // 2. D-1, D-2 공구 → IMMINENT
        updateImminentGroupBuys(now);

        log.info("공구 상태 자동 업데이트 배치 종료");
    }

    /**
     * 마감일이 지난 공구를 CLOSED 상태로 변경
     */
    private void updateExpiredGroupBuys(LocalDateTime now) {
        List<GroupBuyStatus> targetStatuses = Arrays.asList(
                GroupBuyStatus.RECRUITING,
                GroupBuyStatus.IMMINENT
        );

        List<GroupBuy> expiredGroupBuys = groupBuyRepository
                .findByStatusInAndDeadlineBefore(targetStatuses, now);

        int count = 0;
        for (GroupBuy groupBuy : expiredGroupBuys) {
            groupBuy.updateStatus(GroupBuyStatus.CLOSED);
            count++;
        }

        log.info("마감일이 지난 공구 {} 건을 CLOSED 상태로 변경했습니다.", count);
    }

    /**
     * D-1, D-2 공구를 IMMINENT 상태로 변경
     */
    private void updateImminentGroupBuys(LocalDateTime now) {
        LocalDateTime twoDaysLater = now.plusDays(2);

        List<GroupBuy> imminentGroupBuys = groupBuyRepository
                .findByStatusAndDeadlineBetween(
                        GroupBuyStatus.RECRUITING,
                        now,
                        twoDaysLater
                );

        int count = 0;
        for (GroupBuy groupBuy : imminentGroupBuys) {
            groupBuy.updateStatus(GroupBuyStatus.IMMINENT);
            count++;
        }

        log.info("D-1, D-2 공구 {} 건을 IMMINENT 상태로 변경했습니다.", count);
    }

    /**
     * 찜한 공구 마감 임박 알림 발송
     * - D-1 공구를 찜한 사용자에게 알림 발송
     * 매일 자정에 실행
     */
    @Scheduled(cron = "0 0 0 * * *") // 매일 00:00:00
    @Transactional
    public void sendDeadlineNotifications() {
        log.info("공구 마감 임박 알림 배치 시작");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plusDays(1);

        // D-1 공구 조회 (오늘 자정 ~ 내일 자정)
        List<GroupBuy> deadlineTomorrowGroupBuys = groupBuyRepository
                .findByStatusAndDeadlineBetween(
                        GroupBuyStatus.RECRUITING,
                        now,
                        tomorrow
                );

        int notificationCount = 0;

        for (GroupBuy groupBuy : deadlineTomorrowGroupBuys) {
            // 해당 공구를 찜한 사용자들 조회
            List<Wishlist> wishlists = wishlistRepository.findByGroupBuyId(groupBuy.getId());

            for (Wishlist wishlist : wishlists) {
                Long userId = wishlist.getUser().getId();

                // 중복 알림 방지: 이미 해당 공구에 대한 마감 알림이 존재하는지 확인
                boolean alreadyNotified = notificationRepository
                        .existsByUserIdAndTypeAndRelatedEntityId(
                                userId,
                                NotificationType.GROUP_BUY_DEADLINE,
                                groupBuy.getId()
                        );

                if (!alreadyNotified) {
                    // 알림 생성
                    String content = "'" + groupBuy.getTitle() + "' 공구가 내일 마감됩니다.";
                    String url = "/group-purchases/" + groupBuy.getId();

                    Notification notification = Notification.create(
                            wishlist.getUser(),
                            null, // 시스템 알림이므로 actor는 null
                            content,
                            NotificationType.GROUP_BUY_DEADLINE,
                            url,
                            groupBuy.getId(),
                            EntityType.GROUP_BUY
                    );

                    notificationRepository.save(notification);
                    notificationCount++;
                }
            }
        }

        log.info("공구 마감 임박 알림 {} 건을 발송했습니다.", notificationCount);
        log.info("공구 마감 임박 알림 배치 종료");
    }
}
