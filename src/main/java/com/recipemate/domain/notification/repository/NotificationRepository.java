package com.recipemate.domain.notification.repository;

import com.recipemate.domain.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * 사용자의 읽음 여부에 따른 알림 목록 조회 (최신순)
     * @param userId 사용자 ID
     * @param isRead 읽음 여부
     * @return 알림 목록
     */
    List<Notification> findByUserIdAndIsReadOrderByCreatedAtDesc(Long userId, Boolean isRead);

    /**
     * 사용자의 모든 알림 목록 조회 (최신순)
     * @param userId 사용자 ID
     * @return 알림 목록
     */
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * 사용자의 읽지 않은 알림 개수 조회
     * @param userId 사용자 ID
     * @return 읽지 않은 알림 개수
     */
    Long countByUserIdAndIsReadFalse(Long userId);

    /**
     * 사용자의 모든 알림 삭제
     * @param userId 사용자 ID
     */
    void deleteByUserId(Long userId);
}
