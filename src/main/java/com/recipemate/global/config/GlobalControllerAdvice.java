package com.recipemate.global.config;

import com.recipemate.domain.directmessage.service.DirectMessageService;
import com.recipemate.domain.notification.service.NotificationService;
import com.recipemate.domain.user.entity.User;
import com.recipemate.domain.user.service.CustomUserDetailsService.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
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
    
    /**
     * 모든 컨트롤러에 미읽은 알림/쪽지 개수를 자동으로 추가
     * 인증된 사용자의 경우 실제 카운트, 미인증 사용자는 0
     * CustomUserDetails에서 User 객체를 직접 가져와 불필요한 DB 조회 방지
     */
    @ModelAttribute
    public void addUnreadCounts(Model model, HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            User user = userDetails.getUser();
            
            // 사용자가 삭제되었거나 유효하지 않은 경우 자동 로그아웃 처리
            if (user == null || user.isDeleted()) {
                new SecurityContextLogoutHandler().logout(request, null, auth);
                
                model.addAttribute("unreadNotificationCount", 0L);
                model.addAttribute("unreadMessageCount", 0L);
                return;
            }
            
            try {
                Long unreadNotificationCount = notificationService.getUnreadCount(user.getId());
                long unreadMessageCount = directMessageService.getUnreadCount(user.getId());

                model.addAttribute("unreadNotificationCount", unreadNotificationCount);
                model.addAttribute("unreadMessageCount", unreadMessageCount);
            } catch (Exception e) {
                // 사용자를 찾을 수 없는 경우 (삭제된 경우 등) 로그아웃 처리
                new SecurityContextLogoutHandler().logout(request, null, auth);

                model.addAttribute("unreadNotificationCount", 0L);
                model.addAttribute("unreadMessageCount", 0L);
            }
        } else {
            model.addAttribute("unreadNotificationCount", 0L);
            model.addAttribute("unreadMessageCount", 0L);
        }
    }
}
