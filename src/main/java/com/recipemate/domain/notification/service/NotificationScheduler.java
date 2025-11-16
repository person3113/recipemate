package com.recipemate.domain.notification.service;

import com.recipemate.domain.notification.entity.Notification;
import com.recipemate.domain.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 알림 관련 배치 작업을 처리하는 스케줄러
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final NotificationRepository notificationRepository;

    /**
     * 30일 이상 지난 읽은 알림을 자동으로 소프트 삭제하는 배치 작업
     * 매일 새벽 2시에 실행
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void deleteOldReadNotifications() {
        log.info("Starting scheduled task: deleteOldReadNotifications");

        try {
            // 30일 이전 날짜 계산
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

            // 읽은 알림 중 30일 이상 지난 알림 조회
            List<Notification> oldReadNotifications = 
                notificationRepository.findByIsReadTrueAndCreatedAtBefore(thirtyDaysAgo);

            if (oldReadNotifications.isEmpty()) {
                log.info("No old read notifications to delete");
                return;
            }

            // 알림 소프트 삭제
            oldReadNotifications.forEach(Notification::softDelete);

            log.info("Successfully soft deleted {} old read notifications", oldReadNotifications.size());
        } catch (Exception e) {
            log.error("Failed to soft delete old read notifications", e);
            throw e;
        }
    }
}
