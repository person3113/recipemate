package com.recipemate.domain.user.controller;

import com.recipemate.domain.badge.dto.BadgeChallengeResponse;
import com.recipemate.domain.badge.dto.BadgeResponse;
import com.recipemate.domain.badge.service.BadgeService;
import com.recipemate.domain.directmessage.service.DirectMessageService;
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
import com.recipemate.domain.user.dto.UserProfileResponseDto;
import com.recipemate.domain.user.dto.UserResponse;
import com.recipemate.domain.user.entity.User;
import com.recipemate.domain.user.repository.UserRepository;
import com.recipemate.domain.user.service.PointService;
import com.recipemate.domain.user.service.UserService;
import com.recipemate.domain.recipe.dto.RecipeCorrectionResponse;
import com.recipemate.domain.recipe.dto.RecipeListResponse;
import com.recipemate.domain.recipe.service.RecipeCorrectionService;
import com.recipemate.domain.recipe.service.RecipeService;
import com.recipemate.domain.recipewishlist.dto.RecipeWishlistResponse;
import com.recipemate.domain.recipewishlist.service.RecipeWishlistService;
import com.recipemate.domain.wishlist.dto.WishlistResponse;
import com.recipemate.domain.wishlist.service.WishlistService;
import com.recipemate.global.common.GroupBuyStatus;
import com.recipemate.global.exception.CustomException;
import com.recipemate.global.exception.ErrorCode;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final WishlistService wishlistService;
    private final RecipeWishlistService recipeWishlistService;
    private final NotificationService notificationService;
    private final DirectMessageService directMessageService;
    private final UserRepository userRepository;
    private final BadgeService badgeService;
    private final PointService pointService;
    private final RecipeService recipeService;
    private final RecipeCorrectionService recipeCorrectionService;
    private final GroupBuyRepository groupBuyRepository;
    private final ParticipationRepository participationRepository;
    private final com.recipemate.domain.review.repository.ReviewRepository reviewRepository;

    /**
     * 마이페이지 렌더링
     * GET /users/me
     */
    @GetMapping("/me")
    public String myPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        UserResponse userResponse = userService.getMyProfile(userDetails.getUsername());

        // 안 읽은 쪽지 개수 조회
        long unreadMessageCount = directMessageService.getUnreadCount(user.getId());
        
        // 보유 배지 개수 조회
        List<BadgeChallengeResponse> badgeChallenges = badgeService.getBadgeChallenges(user.getId());
        long badgeCount = badgeChallenges.stream()
                .filter(BadgeChallengeResponse::isAcquired)
                .count();

        model.addAttribute("user", userResponse);
        model.addAttribute("currentPoints", user.getPoints());
        model.addAttribute("unreadMessageCount", unreadMessageCount);
        model.addAttribute("badgeCount", badgeCount);
        return "user/my-page";
    }

    /**
     * 설정 페이지 렌더링 (프로필 수정, 비밀번호 변경)
     * GET /users/me/settings
     */
    @GetMapping("/me/settings")
    public String mySettingsPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        UserResponse userResponse = userService.getMyProfile(userDetails.getUsername());
        model.addAttribute("user", userResponse);
        model.addAttribute("commentNotification", user.getCommentNotification());
        model.addAttribute("groupPurchaseNotification", user.getGroupPurchaseNotification());
        return "user/settings";
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
        return "redirect:/users/me/settings";
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
        return "redirect:/users/me/settings";
    }

    /**
     * 알림 설정 변경 처리
     * POST /users/me/notifications
     */
    @PostMapping("/me/notifications")
    public String updateNotificationSettings(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Boolean commentNotification,
            @RequestParam Boolean groupPurchaseNotification,
            RedirectAttributes redirectAttributes) {
        userService.updateNotificationSettings(
            userDetails.getUsername(), 
            commentNotification, 
            groupPurchaseNotification
        );
        redirectAttributes.addFlashAttribute("message", "알림 설정이 변경되었습니다.");
        return "redirect:/users/me/settings";
    }

    /**
     * 계정 탈퇴 처리
     * POST /users/me/delete
     */
    @PostMapping("/me/delete")
    public String deleteAccount(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String password,
            RedirectAttributes redirectAttributes) {
        try {
            userService.deleteAccount(userDetails.getUsername(), password);
            SecurityContextHolder.clearContext();
            redirectAttributes.addFlashAttribute("message", "회원 탈퇴가 완료되었습니다.");
            return "redirect:/";
        } catch (CustomException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/users/me/settings";
        }
    }
    
    /**
     * 내 찜 목록 페이지 렌더링
     * GET /users/me/bookmarks
     */
    @GetMapping("/me/bookmarks")
    public String myWishlistPage(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "groupbuy") String tab,
            @PageableDefault(size = 20, sort = "wishedAt", direction = Sort.Direction.DESC) Pageable pageable,
            Model model) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        if ("groupbuy".equals(tab)) {
            Page<WishlistResponse> wishlists = wishlistService.getMyWishlist(user.getId(), pageable);
            model.addAttribute("wishlists", wishlists);
        } else if ("recipe".equals(tab)) {
            Page<RecipeWishlistResponse> recipeWishlists = recipeWishlistService.getMyWishlist(user.getId(), pageable);
            model.addAttribute("recipeWishlists", recipeWishlists);
        }
        
        model.addAttribute("currentTab", tab);
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
     * 내 배지 목록 및 도전과제 페이지 렌더링
     * GET /users/me/badges
     */
    @GetMapping("/me/badges")
    public String myBadgesPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        List<BadgeChallengeResponse> badgeChallenges = badgeService.getBadgeChallenges(user.getId());
        
        List<BadgeChallengeResponse> acquiredBadges = badgeChallenges.stream()
                .filter(BadgeChallengeResponse::isAcquired)
                .collect(Collectors.toList());

        List<BadgeChallengeResponse> challenges = badgeChallenges.stream()
                .filter(b -> !b.isAcquired())
                .collect(Collectors.toList());

        model.addAttribute("acquiredBadges", acquiredBadges);
        model.addAttribute("challenges", challenges);
        
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
     * 내 매너온도 내역 페이지 렌더링
     * GET /users/me/manner-histories
     */
    @GetMapping("/me/manner-histories")
    public String myMannerHistoriesPage(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Model model) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        Page<com.recipemate.domain.user.dto.MannerTempHistoryResponse> mannerHistories = 
                userService.getMannerTempHistories(user.getId(), pageable);
        model.addAttribute("mannerHistories", mannerHistories);
        model.addAttribute("currentTemperature", user.getMannerTemperature());
        return "user/manner-histories";
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
     * 포인트 충전 페이지 렌더링
     * GET /users/me/points/charge
     */
    @GetMapping("/me/points/charge")
    public String pointChargePage(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        model.addAttribute("currentPoints", user.getPoints());
        return "user/point-charge";
    }

    /**
     * 포인트 충전 처리
     * POST /users/me/points/charge
     */
    @PostMapping("/me/points/charge")
    public String chargePoints(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Integer amount,
            RedirectAttributes redirectAttributes) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        // 최소 충전 금액 검증
        if (amount < 1000) {
            redirectAttributes.addFlashAttribute("error", "최소 충전 금액은 1,000포인트입니다.");
            return "redirect:/users/me/points/charge";
        }
        
        // 최대 충전 금액 검증
        if (amount > 1000000) {
            redirectAttributes.addFlashAttribute("error", "최대 충전 금액은 1,000,000포인트입니다.");
            return "redirect:/users/me/points/charge";
        }
        
        try {
            pointService.chargePoints(user.getId(), amount, "포인트 충전");
            
            // Security Context 갱신: 포인트가 변경되었으므로 세션의 인증 정보를 갱신
            userService.refreshAuthentication(userDetails.getUsername());
            
            redirectAttributes.addFlashAttribute("message", 
                String.format("%,d포인트가 충전되었습니다.", amount));
        } catch (CustomException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/users/me/points/charge";
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
        
        // 각 참여에 대한 후기 존재 여부를 한 번에 조회
        List<Long> groupBuyIds = participations.getContent().stream()
                .map(p -> p.getGroupBuy().getId())
                .toList();
        
        java.util.Map<Long, Boolean> reviewExistsMap = new java.util.HashMap<>();
        java.util.Map<Long, Boolean> deletedReviewExistsMap = new java.util.HashMap<>();
        for (Long groupBuyId : groupBuyIds) {
            boolean hasReview = reviewRepository.existsByReviewerIdAndGroupBuyId(user.getId(), groupBuyId);
            boolean hasDeletedReview = reviewRepository.existsDeletedReviewByReviewerIdAndGroupBuyId(user.getId(), groupBuyId);
            reviewExistsMap.put(groupBuyId, hasReview);
            deletedReviewExistsMap.put(groupBuyId, hasDeletedReview);
        }
        
        model.addAttribute("participations", participations);
        model.addAttribute("reviewExistsMap", reviewExistsMap);
        model.addAttribute("deletedReviewExistsMap", deletedReviewExistsMap);
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

    /**
     * 내가 작성한 레시피 목록 페이지 렌더링
     * GET /users/me/recipes
     */
    @GetMapping("/me/recipes")
    public String myRecipesPage(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (userDetails == null) {
            redirectAttributes.addFlashAttribute("error", "로그인이 필요합니다.");
            return "redirect:/auth/login";
        }

        User currentUser = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<RecipeListResponse.RecipeSimpleInfo> recipes = recipeService.getUserRecipes(currentUser, pageable);

        model.addAttribute("recipes", recipes);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);

        return "user/my-recipes";
    }

    /**
     * 내 레시피 개선 제안 목록 페이지 렌더링
     * GET /users/me/corrections
     */
    @GetMapping("/me/corrections")
    public String myCorrectionsPage(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Model model) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        Page<RecipeCorrectionResponse> corrections = recipeCorrectionService.getUserCorrections(user.getId(), pageable);
        
        model.addAttribute("corrections", corrections);
        return "recipes/my-corrections";
    }

    /**
     * 사용자 프로필 페이지 렌더링
     * GET /users/profile/{nickname}
     */
    @GetMapping("/profile/{nickname}")
    public String userProfile(
            @PathVariable String nickname,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Model model) {
        UserProfileResponseDto userProfile = userService.getUserProfile(nickname, pageable);
        model.addAttribute("userProfile", userProfile);
        return "user/profile";
    }

    // ========== htmx용 HTML Fragment 엔드포인트 (향후 추가) ==========
    // TODO: htmx 통합 시 아래 엔드포인트 구현
    // @GetMapping("/me/fragments/profile") - 프로필 정보 HTML 조각
    // @PostMapping("/me/fragments/profile") - 프로필 수정 폼 처리 후 HTML 조각 반환
    // @GetMapping("/me/fragments/group-purchases") - 내 공구 목록 HTML 조각
    // @GetMapping("/me/fragments/participations") - 내 참여 공구 목록 HTML 조각
    // @GetMapping("/me/fragments/notifications") - 알림 목록 HTML 조각
}
