package com.recipemate.domain.directmessage.repository;

import com.recipemate.domain.directmessage.entity.DirectMessage;
import com.recipemate.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DirectMessageRepository extends JpaRepository<DirectMessage, Long> {

    /**
     * 두 사용자 간의 대화 내용을 생성 시간 오름차순으로 조회
     */
    @Query("SELECT dm FROM DirectMessage dm WHERE " +
           "(dm.sender = :user1 AND dm.receiver = :user2) OR " +
           "(dm.sender = :user2 AND dm.receiver = :user1) " +
           "ORDER BY dm.createdAt ASC")
    List<DirectMessage> findConversationBetween(
        @Param("user1") User user1,
        @Param("user2") User user2
    );

    /**
     * 안 읽은 메시지 개수 조회 (알림 배지용)
     */
    @Query("SELECT COUNT(dm) FROM DirectMessage dm WHERE dm.receiver = :receiver AND dm.isRead = false")
    long countUnreadMessages(@Param("receiver") User receiver);

    /**
     * 수신자의 안 읽은 메시지 일괄 읽음 처리
     */
    @Modifying
    @Query("UPDATE DirectMessage dm SET dm.isRead = true WHERE dm.receiver = :receiver AND dm.sender = :sender AND dm.isRead = false")
    int markAllAsReadBetween(@Param("receiver") User receiver, @Param("sender") User sender);

    /**
     * 최근 대화 상대 목록 조회
     * 발신자로서 보낸 메시지의 수신자들과 수신자로서 받은 메시지의 발신자들을 조회
     */
    @Query("SELECT DISTINCT u FROM User u WHERE u.id IN " +
           "(SELECT dm.receiver.id FROM DirectMessage dm WHERE dm.sender.id = :userId) OR u.id IN " +
           "(SELECT dm.sender.id FROM DirectMessage dm WHERE dm.receiver.id = :userId)")
    List<User> findRecentContacts(@Param("userId") Long userId);
}

