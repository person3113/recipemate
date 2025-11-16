package com.recipemate.global.config;

import com.recipemate.domain.directmessage.service.DirectMessageService;
import com.recipemate.domain.notification.service.NotificationService;
import com.recipemate.domain.user.entity.User;
import com.recipemate.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * 전역 컨트롤러 어드바이스
 * 모든 뷰 템플릿에 공통 데이터를 자동으로 추가
 */
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {
    
    private final NotificationService notificationService;
    private final DirectMessageService directMessageService;
    private final UserRepository userRepository;
    
    /**
     * 모든 컨트롤러에 미읽은 알림/쪽지 개수를 자동으로 추가
     * 인증된 사용자의 경우 실제 카운트, 미인증 사용자는 0
     */
    @ModelAttribute
    public void addUnreadCounts(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
            
            if (user != null) {
                Long unreadNotificationCount = notificationService.getUnreadCount(user.getId());
                long unreadMessageCount = directMessageService.getUnreadCount(user.getId());
                
                model.addAttribute("unreadNotificationCount", unreadNotificationCount);
                model.addAttribute("unreadMessageCount", unreadMessageCount);
            } else {
                model.addAttribute("unreadNotificationCount", 0L);
                model.addAttribute("unreadMessageCount", 0L);
            }
        } else {
            model.addAttribute("unreadNotificationCount", 0L);
            model.addAttribute("unreadMessageCount", 0L);
        }
    }
}
