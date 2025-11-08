package com.recipemate.domain.notification.repository;

import com.recipemate.domain.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * 사용자의 읽음 여부에 따른 알림 목록 조회 (최신순)
     * Actor(행동자) 정보를 Fetch Join으로 함께 조회하여 N+1 문제 방지
     * @param userId 사용자 ID
     * @param isRead 읽음 여부
     * @return 알림 목록
     */
    @Query("SELECT n FROM Notification n LEFT JOIN FETCH n.actor WHERE n.user.id = :userId AND n.isRead = :isRead ORDER BY n.createdAt DESC")
    List<Notification> findByUserIdAndIsReadOrderByCreatedAtDesc(@Param("userId") Long userId, @Param("isRead") Boolean isRead);

    /**
     * 사용자의 모든 알림 목록 조회 (최신순)
     * Actor(행동자) 정보를 Fetch Join으로 함께 조회하여 N+1 문제 방지
     * @param userId 사용자 ID
     * @return 알림 목록
     */
    @Query("SELECT n FROM Notification n LEFT JOIN FETCH n.actor WHERE n.user.id = :userId ORDER BY n.createdAt DESC")
    List<Notification> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    /**
     * 사용자의 모든 알림 목록 조회 with 페이징 (최신순)
     * Actor(행동자) 정보를 Fetch Join으로 함께 조회하여 N+1 문제 방지
     * @param userId 사용자 ID
     * @param pageable 페이징 정보
     * @return 알림 페이지
     */
    @Query(value = "SELECT n FROM Notification n LEFT JOIN FETCH n.actor WHERE n.user.id = :userId",
           countQuery = "SELECT count(n) FROM Notification n WHERE n.user.id = :userId")
    Page<Notification> findByUserIdWithActor(@Param("userId") Long userId, Pageable pageable);

    /**
     * 사용자의 읽음 여부에 따른 알림 목록 조회 with 페이징 (최신순)
     * Actor(행동자) 정보를 Fetch Join으로 함께 조회하여 N+1 문제 방지
     * @param userId 사용자 ID
     * @param isRead 읽음 여부
     * @param pageable 페이징 정보
     * @return 알림 페이지
     */
    @Query(value = "SELECT n FROM Notification n LEFT JOIN FETCH n.actor WHERE n.user.id = :userId AND n.isRead = :isRead",
           countQuery = "SELECT count(n) FROM Notification n WHERE n.user.id = :userId AND n.isRead = :isRead")
    Page<Notification> findByUserIdAndIsReadWithActor(@Param("userId") Long userId, @Param("isRead") Boolean isRead, Pageable pageable);

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

    /**
     * 특정 사용자, 타입, 관련 엔티티로 알림 존재 여부 확인
     * @param userId 사용자 ID
     * @param type 알림 타입
     * @param relatedEntityId 관련 엔티티 ID
     * @return 알림 존재 여부
     */
    boolean existsByUserIdAndTypeAndRelatedEntityId(Long userId, 
                                                    com.recipemate.global.common.NotificationType type, 
                                                    Long relatedEntityId);

    /**
     * 읽은 알림 중 특정 날짜 이전에 생성된 알림 목록 조회
     * @param dateTime 기준 날짜
     * @return 삭제 대상 알림 목록
     */
    List<Notification> findByIsReadTrueAndCreatedAtBefore(LocalDateTime dateTime);
}
