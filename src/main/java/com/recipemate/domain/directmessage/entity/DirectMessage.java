package com.recipemate.domain.directmessage.entity;

import com.recipemate.domain.user.entity.User;
import com.recipemate.global.common.BaseEntity;
import com.recipemate.global.exception.CustomException;
import com.recipemate.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "direct_messages",
    indexes = {
        @Index(name = "idx_dm_sender_created", columnList = "sender_id, created_at"),
        @Index(name = "idx_dm_receiver_created", columnList = "receiver_id, created_at"),
        @Index(name = "idx_dm_receiver_is_read", columnList = "receiver_id, is_read, created_at")
    }
)
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DirectMessage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false, foreignKey = @ForeignKey(name = "fk_dm_sender"))
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false, foreignKey = @ForeignKey(name = "fk_dm_receiver"))
    private User receiver;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(nullable = false)
    private Boolean isRead;

    //== 생성 메서드 ==//
    public static DirectMessage create(User sender, User receiver, String content) {
        validateCreateArgs(sender, receiver, content);

        return DirectMessage.builder()
            .sender(sender)
            .receiver(receiver)
            .content(content)
            .isRead(Boolean.FALSE)
            .build();
    }

    private static void validateCreateArgs(User sender, User receiver, String content) {
        if (sender == null || receiver == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
        if (sender.getId().equals(receiver.getId())) {
            throw new CustomException(ErrorCode.CANNOT_SEND_MESSAGE_TO_SELF);
        }
        if (content == null || content.trim().isEmpty()) {
            throw new CustomException(ErrorCode.EMPTY_CONTENT);
        }
        if (content.length() > 1000) {
            throw new CustomException(ErrorCode.CONTENT_TOO_LONG);
        }
    }

    //== 비즈니스 로직 ==//
    /**
     * 메시지를 읽음 처리합니다.
     */
    public void markAsRead() {
        this.isRead = Boolean.TRUE;
    }

    /**
     * 현재 사용자가 이 메시지에 접근할 권한이 있는지 확인합니다.
     * @param userId 확인할 사용자 ID
     * @return 발신자 또는 수신자인 경우 true
     */
    public boolean canAccess(Long userId) {
        return sender.getId().equals(userId) || receiver.getId().equals(userId);
    }
}

