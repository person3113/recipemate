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
    @Query("SELECT n FROM Notification n LEFT JOIN FETCH n.actor WHERE n.user.id = :userId AND n.isRead = :isRead AND n.deletedAt IS NULL ORDER BY n.createdAt DESC")
    List<Notification> findByUserIdAndIsReadOrderByCreatedAtDesc(@Param("userId") Long userId, @Param("isRead") Boolean isRead);

    /**
     * 사용자의 모든 알림 목록 조회 (최신순)
     * Actor(행동자) 정보를 Fetch Join으로 함께 조회하여 N+1 문제 방지
     * @param userId 사용자 ID
     * @return 알림 목록
     */
    @Query("SELECT n FROM Notification n LEFT JOIN FETCH n.actor WHERE n.user.id = :userId AND n.deletedAt IS NULL ORDER BY n.createdAt DESC")
    List<Notification> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    /**
     * 사용자의 모든 알림 목록 조회 with 페이징 (최신순)
     * Actor(행동자) 정보를 Fetch Join으로 함께 조회하여 N+1 문제 방지
     * @param userId 사용자 ID
     * @param pageable 페이징 정보
     * @return 알림 페이지
     */
    @Query(value = "SELECT n FROM Notification n LEFT JOIN FETCH n.actor WHERE n.user.id = :userId AND n.deletedAt IS NULL",
           countQuery = "SELECT count(n) FROM Notification n WHERE n.user.id = :userId AND n.deletedAt IS NULL")
    Page<Notification> findByUserIdWithActor(@Param("userId") Long userId, Pageable pageable);

    /**
     * 사용자의 읽음 여부에 따른 알림 목록 조회 with 페이징 (최신순)
     * Actor(행동자) 정보를 Fetch Join으로 함께 조회하여 N+1 문제 방지
     * @param userId 사용자 ID
     * @param isRead 읽음 여부
     * @param pageable 페이징 정보
     * @return 알림 페이지
     */
    @Query(value = "SELECT n FROM Notification n LEFT JOIN FETCH n.actor WHERE n.user.id = :userId AND n.isRead = :isRead AND n.deletedAt IS NULL",
           countQuery = "SELECT count(n) FROM Notification n WHERE n.user.id = :userId AND n.isRead = :isRead AND n.deletedAt IS NULL")
    Page<Notification> findByUserIdAndIsReadWithActor(@Param("userId") Long userId, @Param("isRead") Boolean isRead, Pageable pageable);

    /**
     * 사용자의 읽지 않은 알림 개수 조회
     * @param userId 사용자 ID
     * @return 읽지 않은 알림 개수
     */
    @Query("SELECT count(n) FROM Notification n WHERE n.user.id = :userId AND n.isRead = false AND n.deletedAt IS NULL")
    Long countByUserIdAndIsReadFalse(@Param("userId") Long userId);

    /**
     * 사용자의 모든 알림 소프트 삭제
     * @param userId 사용자 ID
     */
    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId AND n.deletedAt IS NULL")
    List<Notification> findActiveNotificationsByUserId(@Param("userId") Long userId);

    /**
     * 특정 사용자, 타입, 관련 엔티티로 알림 존재 여부 확인
     * @param userId 사용자 ID
     * @param type 알림 타입
     * @param relatedEntityId 관련 엔티티 ID
     * @return 알림 존재 여부
     */
    @Query("SELECT CASE WHEN COUNT(n) > 0 THEN true ELSE false END FROM Notification n WHERE n.user.id = :userId AND n.type = :type AND n.relatedEntityId = :relatedEntityId AND n.deletedAt IS NULL")
    boolean existsByUserIdAndTypeAndRelatedEntityId(@Param("userId") Long userId, 
                                                    @Param("type") com.recipemate.global.common.NotificationType type, 
                                                    @Param("relatedEntityId") Long relatedEntityId);

    /**
     * 읽은 알림 중 특정 날짜 이전에 생성된 알림 목록 조회
     * @param dateTime 기준 날짜
     * @return 삭제 대상 알림 목록
     */
    @Query("SELECT n FROM Notification n WHERE n.isRead = true AND n.createdAt < :dateTime AND n.deletedAt IS NULL")
    List<Notification> findByIsReadTrueAndCreatedAtBefore(@Param("dateTime") LocalDateTime dateTime);
}
