package com.recipemate.domain.notification.controller;

import com.recipemate.domain.notification.service.NotificationService;
import com.recipemate.domain.user.entity.User;
import com.recipemate.domain.user.repository.UserRepository;
import com.recipemate.global.exception.CustomException;
import com.recipemate.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 알림 관리 전용 컨트롤러
 * HTML에서 요청하는 /api/notifications 경로를 처리
 */
@Controller
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    /**
     * 전체 알림 읽음 처리
     * POST /api/notifications/read-all
     */
    @PostMapping("/read-all")
    public String markAllNotificationsAsRead(
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        User user = getUserFromUserDetails(userDetails);
        
        notificationService.markAllNotificationsAsRead(user.getId());
        redirectAttributes.addFlashAttribute("successMessage", "모든 알림을 읽음 처리했습니다.");
        return "redirect:/users/me/notifications";
    }

    /**
     * 개별 알림 읽음 처리
     * POST /api/notifications/{id}/read
     */
    @PostMapping("/{notificationId}/read")
    public String markNotificationAsRead(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long notificationId,
            RedirectAttributes redirectAttributes) {
        User user = getUserFromUserDetails(userDetails);
        
        notificationService.markNotificationAsRead(user.getId(), notificationId);
        redirectAttributes.addFlashAttribute("successMessage", "알림을 읽음 처리했습니다.");
        return "redirect:/users/me/notifications";
    }

    /**
     * 개별 알림 삭제
     * DELETE /api/notifications/{id}
     */
    @DeleteMapping("/{notificationId}")
    public String deleteNotification(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long notificationId,
            RedirectAttributes redirectAttributes) {
        User user = getUserFromUserDetails(userDetails);
        
        notificationService.deleteNotification(user.getId(), notificationId);
        redirectAttributes.addFlashAttribute("successMessage", "알림을 삭제했습니다.");
        return "redirect:/users/me/notifications";
    }

    /**
     * 전체 알림 삭제
     * DELETE /api/notifications
     */
    @DeleteMapping
    public String deleteAllNotifications(
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        User user = getUserFromUserDetails(userDetails);
        
        notificationService.deleteAllNotifications(user.getId());
        redirectAttributes.addFlashAttribute("successMessage", "모든 알림을 삭제했습니다.");
        return "redirect:/users/me/notifications";
    }

    private User getUserFromUserDetails(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }
}
