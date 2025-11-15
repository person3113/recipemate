package com.recipemate.domain.directmessage.controller;

import com.recipemate.domain.directmessage.dto.ContactResponse;
import com.recipemate.domain.directmessage.dto.DirectMessageResponse;
import com.recipemate.domain.directmessage.dto.SendMessageRequest;
import com.recipemate.domain.directmessage.service.DirectMessageService;
import com.recipemate.domain.user.entity.User;
import com.recipemate.domain.user.repository.UserRepository;
import com.recipemate.global.exception.CustomException;
import com.recipemate.global.exception.ErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/direct-messages")
@RequiredArgsConstructor
public class DirectMessageController {

    private final DirectMessageService directMessageService;
    private final UserRepository userRepository;

    /**
     * 특정 사용자와의 대화 페이지
     */
    @GetMapping("/conversation/{userId}")
    public String conversationPage(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model
    ) {
        if (userDetails == null) {
            return "redirect:/auth/login";
        }

        User currentUser = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        User otherUser = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 대화 내용 조회
        List<DirectMessageResponse> messages = directMessageService
                .getConversation(currentUser.getId(), userId);

        // 읽지 않은 메시지 읽음 처리
        directMessageService.markAsRead(currentUser.getId(), userId);

        model.addAttribute("currentUserId", currentUser.getId());
        model.addAttribute("otherUser", otherUser);
        model.addAttribute("messages", messages);

        return "direct-messages/conversation";
    }

    /**
     * 쪽지 전송
     */
    @PostMapping("/send/{recipientId}")
    public String sendMessage(
            @PathVariable Long recipientId,
            @Valid @ModelAttribute SendMessageRequest request,
            BindingResult bindingResult,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        if (userDetails == null) {
            return "redirect:/auth/login";
        }

        User currentUser = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "메시지 전송에 실패했습니다.");
            return "redirect:/direct-messages/conversation/" + recipientId;
        }

        try {
            directMessageService.sendMessage(currentUser.getId(), recipientId, request.getContent());
            redirectAttributes.addFlashAttribute("success", "쪽지를 전송했습니다.");
        } catch (CustomException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/direct-messages/conversation/" + recipientId;
    }

    /**
     * 최근 대화 상대 목록 페이지
     */
    @GetMapping("/contacts")
    public String contactsPage(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model
    ) {
        if (userDetails == null) {
            return "redirect:/auth/login";
        }

        User currentUser = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<ContactResponse> contacts = directMessageService.getRecentContacts(currentUser.getId());
        long unreadCount = directMessageService.getUnreadCount(currentUser.getId());

        model.addAttribute("contacts", contacts);
        model.addAttribute("unreadCount", unreadCount);

        return "direct-messages/contacts";
    }

    /**
     * 안 읽은 쪽지 개수 조회 (AJAX)
     */
    @GetMapping("/unread-count")
    @ResponseBody
    public Long getUnreadCount(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return 0L;
        }

        User currentUser = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return directMessageService.getUnreadCount(currentUser.getId());
    }
}

