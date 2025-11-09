package com.recipemate.domain.user.controller;

import com.recipemate.domain.badge.dto.BadgeResponse;
import com.recipemate.domain.badge.service.BadgeService;
import com.recipemate.domain.groupbuy.entity.GroupBuy;
import com.recipemate.domain.groupbuy.entity.Participation;
import com.recipemate.domain.groupbuy.repository.GroupBuyRepository;
import com.recipemate.domain.groupbuy.repository.ParticipationRepository;
import com.recipemate.domain.notification.dto.NotificationResponse;
import com.recipemate.domain.notification.service.NotificationService;
import com.recipemate.domain.user.dto.ChangePasswordRequest;
import com.recipemate.domain.user.dto.MyCommentDto;
import com.recipemate.domain.user.dto.MyLikedPostDto;
import com.recipemate.domain.user.dto.MyPostDto;
import com.recipemate.domain.user.dto.PointHistoryResponse;
import com.recipemate.domain.user.dto.UpdateProfileRequest;
import com.recipemate.domain.user.dto.UserResponse;
import com.recipemate.domain.user.entity.User;
import com.recipemate.domain.user.repository.UserRepository;
import com.recipemate.domain.user.service.PointService;
import com.recipemate.domain.user.service.UserService;
import com.recipemate.domain.wishlist.dto.WishlistResponse;
import com.recipemate.domain.wishlist.service.WishlistService;
import com.recipemate.global.common.GroupBuyStatus;
import com.recipemate.global.exception.CustomException;
import com.recipemate.global.exception.ErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final WishlistService wishlistService;
    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final BadgeService badgeService;
    private final PointService pointService;
    private final GroupBuyRepository groupBuyRepository;
    private final ParticipationRepository participationRepository;

    /**
     * 마이페이지 렌더링
     * GET /users/me
     */
    @GetMapping("/me")
    public String myPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        UserResponse userResponse = userService.getMyProfile(userDetails.getUsername());
        model.addAttribute("user", userResponse);
        model.addAttribute("currentPoints", user.getPoints());
        return "user/my-page";
    }

    /**
     * 프로필 수정 폼 제출 처리
     * POST /users/me
     */
    @PostMapping("/me")
    public String updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @ModelAttribute UpdateProfileRequest request,
            RedirectAttributes redirectAttributes) {
        userService.updateProfile(userDetails.getUsername(), request);
        redirectAttributes.addFlashAttribute("message", "프로필이 수정되었습니다.");
        return "redirect:/users/me";
    }

    /**
     * 비밀번호 변경 폼 제출 처리
     * POST /users/me/password
     */
    @PostMapping("/me/password")
    public String changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @ModelAttribute ChangePasswordRequest request,
            RedirectAttributes redirectAttributes) {
        userService.changePassword(userDetails.getUsername(), request);
        redirectAttributes.addFlashAttribute("message", "비밀번호가 변경되었습니다.");
        return "redirect:/users/me";
    }
    
    /**
     * 내 찜 목록 페이지 렌더링
     * GET /users/me/bookmarks
     */
    @GetMapping("/me/bookmarks")
    public String myWishlistPage(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20, sort = "wishedAt", direction = Sort.Direction.DESC) Pageable pageable,
            Model model) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        Page<WishlistResponse> wishlists = wishlistService.getMyWishlist(user.getId(), pageable);
        model.addAttribute("wishlists", wishlists);
        return "user/bookmarks";
    }

    /**
     * 내 알림 목록 페이지 렌더링
     * GET /users/me/notifications
     * 
     * 알림 관리 기능(읽음/삭제 등)은 NotificationController에서 처리
     */
    @GetMapping("/me/notifications")
    public String myNotificationsPage(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Boolean isRead,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Model model) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        Page<NotificationResponse> notifications = notificationService.getNotifications(user.getId(), isRead, pageable);
        Long unreadCount = notificationService.getUnreadCount(user.getId());
        
        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadCount", unreadCount);
        model.addAttribute("currentFilter", isRead);
        return "user/notifications";
    }

    /**
     * 내 배지 목록 페이지 렌더링
     * GET /users/me/badges
     */
    @GetMapping("/me/badges")
    public String myBadgesPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        List<BadgeResponse> badges = badgeService.getUserBadges(user.getId());
        model.addAttribute("badges", badges);
        return "user/badges";
    }

    /**
     * 내 포인트 내역 페이지 렌더링
     * GET /users/me/points
     */
    @GetMapping("/me/points")
    public String myPointsPage(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Model model) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        Page<PointHistoryResponse> pointHistory = pointService.getPointHistory(user.getId(), pageable);
        model.addAttribute("pointHistory", pointHistory);
        model.addAttribute("currentPoints", user.getPoints());
        return "user/points";
    }

    /**
     * 출석 체크 (HTML 폼용)
     * POST /users/me/check-in
     */
    @PostMapping("/me/check-in")
    public String checkIn(
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        try {
            pointService.dailyCheckIn(user.getId());
            redirectAttributes.addFlashAttribute("message", "출석 체크 완료! 5포인트가 적립되었습니다.");
        } catch (CustomException e) {
            if (e.getErrorCode() == ErrorCode.ALREADY_CHECKED_IN_TODAY) {
                redirectAttributes.addFlashAttribute("error", "오늘은 이미 출석 체크를 완료했습니다.");
            } else {
                throw e;
            }
        }
        
        return "redirect:/users/me/points";
    }

    /**
     * 내가 만든 공구 목록 페이지 렌더링
     * GET /users/me/group-purchases
     */
    @GetMapping("/me/group-purchases")
    public String myGroupPurchasesPage(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) GroupBuyStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Model model) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        Page<GroupBuy> groupBuys;
        if (status != null) {
            groupBuys = groupBuyRepository.findByHostIdAndStatusInAndNotDeleted(user.getId(), List.of(status), pageable);
        } else {
            groupBuys = groupBuyRepository.findByHostIdAndNotDeleted(user.getId(), pageable);
        }
        
        model.addAttribute("groupBuys", groupBuys);
        model.addAttribute("currentStatus", status);
        return "user/my-group-purchases";
    }

    /**
     * 참여중인 공구 목록 페이지 렌더링
     * GET /users/me/participations
     */
    @GetMapping("/me/participations")
    public String myParticipationsPage(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) GroupBuyStatus status,
            @PageableDefault(size = 20, sort = "participatedAt", direction = Sort.Direction.DESC) Pageable pageable,
            Model model) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        List<GroupBuyStatus> statusFilter = (status != null) ? List.of(status) : null;
        Page<Participation> participations = participationRepository.findByUserIdWithGroupBuyAndHost(
                user.getId(), statusFilter, pageable);
        
        model.addAttribute("participations", participations);
        model.addAttribute("currentStatus", status);
        return "user/participations";
    }

    /**
     * 내 커뮤니티 활동 페이지 렌더링
     * GET /users/me/community
     */
    @GetMapping("/me/community")
    public String myCommunityActivityPage(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "posts") String tab,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            Model model) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        if ("posts".equals(tab)) {
            Page<MyPostDto> posts = userService.findMyPosts(user, pageable);
            model.addAttribute("posts", posts);
        } else if ("comments".equals(tab)) {
            Page<MyCommentDto> comments = userService.findMyComments(user, pageable);
            model.addAttribute("comments", comments);
        } else if ("likes".equals(tab)) {
            Page<MyLikedPostDto> likedPosts = userService.findMyLikedPosts(user, pageable);
            model.addAttribute("likedPosts", likedPosts);
        }
        
        model.addAttribute("currentTab", tab);
        return "user/my-community-activity";
    }
    
    // ========== htmx용 HTML Fragment 엔드포인트 (향후 추가) ==========
    // TODO: htmx 통합 시 아래 엔드포인트 구현
    // @GetMapping("/me/fragments/profile") - 프로필 정보 HTML 조각
    // @PostMapping("/me/fragments/profile") - 프로필 수정 폼 처리 후 HTML 조각 반환
    // @GetMapping("/me/fragments/group-purchases") - 내 공구 목록 HTML 조각
    // @GetMapping("/me/fragments/participations") - 내 참여 공구 목록 HTML 조각
    // @GetMapping("/me/fragments/notifications") - 알림 목록 HTML 조각
}
