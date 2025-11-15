package com.recipemate.domain.directmessage.service;

import com.recipemate.domain.directmessage.dto.ContactResponse;
import com.recipemate.domain.directmessage.dto.DirectMessageResponse;
import com.recipemate.domain.directmessage.entity.DirectMessage;
import com.recipemate.domain.directmessage.repository.DirectMessageRepository;
import com.recipemate.domain.notification.service.NotificationService;
import com.recipemate.domain.user.entity.User;
import com.recipemate.domain.user.repository.UserRepository;
import com.recipemate.global.common.EntityType;
import com.recipemate.global.common.NotificationType;
import com.recipemate.global.exception.CustomException;
import com.recipemate.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DirectMessageService {

    private final DirectMessageRepository directMessageRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    /**
     * 쪽지 전송
     */
    @Transactional
    public DirectMessageResponse sendMessage(Long senderId, Long recipientId, String content) {
        User sender = userRepository.findById(senderId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        User recipient = userRepository.findById(recipientId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        DirectMessage message = DirectMessage.create(sender, recipient, content);
        DirectMessage saved = directMessageRepository.save(message);

        // 수신자에게 알림 전송
        notificationService.createNotification(
            recipientId,
            NotificationType.DIRECT_MESSAGE,
            senderId,
            senderId,  // entityId를 발신자 ID로 설정하여 알림 클릭 시 해당 발신자와의 대화로 이동
            EntityType.DIRECT_MESSAGE
        );

        return DirectMessageResponse.from(saved);
    }

    /**
     * 두 사용자 간의 대화 내용 조회
     */
    public List<DirectMessageResponse> getConversation(Long currentUserId, Long otherUserId) {
        User currentUser = userRepository.findById(currentUserId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        User otherUser = userRepository.findById(otherUserId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<DirectMessage> messages = directMessageRepository
            .findConversationBetween(currentUser, otherUser);

        return messages.stream()
            .map(DirectMessageResponse::from)
            .collect(Collectors.toList());
    }

    /**
     * 특정 발신자의 메시지를 모두 읽음 처리
     */
    @Transactional
    public void markAsRead(Long receiverId, Long senderId) {
        User receiver = userRepository.findById(receiverId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        User sender = userRepository.findById(senderId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        directMessageRepository.markAllAsReadBetween(receiver, sender);
    }

    /**
     * 안 읽은 쪽지 개수 조회
     */
    public long getUnreadCount(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return directMessageRepository.countUnreadMessages(user);
    }

    /**
     * 최근 대화 상대 목록 조회
     */
    public List<ContactResponse> getRecentContacts(Long userId) {
        // 사용자 존재 확인
        userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<User> contacts = directMessageRepository.findRecentContacts(userId);

        return contacts.stream()
            .map(ContactResponse::from)
            .collect(Collectors.toList());
    }
}

