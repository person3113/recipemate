package com.recipemate.domain.groupbuy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recipemate.domain.groupbuy.dto.CreateGroupBuyRequest;
import com.recipemate.domain.groupbuy.dto.GroupBuyResponse;
import com.recipemate.domain.groupbuy.dto.GroupBuySearchCondition;
import com.recipemate.domain.groupbuy.dto.ParticipateRequest;
import com.recipemate.domain.groupbuy.dto.SelectedIngredient;
import com.recipemate.domain.groupbuy.dto.UpdateGroupBuyRequest;
import com.recipemate.domain.groupbuy.service.GroupBuyService;
import com.recipemate.domain.groupbuy.service.ParticipationService;
import com.recipemate.domain.user.entity.User;
import com.recipemate.domain.user.repository.UserRepository;
import com.recipemate.domain.wishlist.service.WishlistService;
import com.recipemate.global.common.ApiResponse;
import com.recipemate.global.common.DeliveryMethod;
import com.recipemate.global.common.GroupBuyStatus;
import com.recipemate.global.exception.CustomException;
import com.recipemate.global.exception.ErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequestMapping("/group-purchases")
@RequiredArgsConstructor
public class GroupBuyController {

    private final GroupBuyService groupBuyService;
    private final ParticipationService participationService;
    private final WishlistService wishlistService;
    private final UserRepository userRepository;
    private final com.recipemate.domain.recipe.service.RecipeService recipeService;
    private final ObjectMapper objectMapper;

    // ========== 페이지 렌더링 엔드포인트 ==========
    
    /**
     * 공구 목록 페이지 렌더링
     */
    @GetMapping("/list")
    public String listPage(
        @RequestParam(required = false) String category,
        @RequestParam(required = false) GroupBuyStatus status,
        @RequestParam(required = false, defaultValue = "false") Boolean recipeOnly,
        @RequestParam(required = false) String keyword,
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
        Model model
    ) {
        GroupBuySearchCondition condition = GroupBuySearchCondition.builder()
            .category(category)
            .status(status)
            .recipeOnly(recipeOnly)
            .keyword(keyword)
            .build();
        
        Page<GroupBuyResponse> result = groupBuyService.getGroupBuyList(condition, pageable);
        model.addAttribute("groupBuys", result);
        model.addAttribute("searchCondition", condition);
        
        return "group-purchases/list";
    }
    
    /**
     * 공구 상세 페이지 렌더링
     */
    @GetMapping("/{purchaseId}")
    public String detailPage(@PathVariable Long purchaseId, Model model) {
        GroupBuyResponse groupBuy = groupBuyService.getGroupBuyDetail(purchaseId);
        model.addAttribute("groupBuy", groupBuy);
        return "group-purchases/detail";
    }
    
    /**
     * 공구 작성 페이지 렌더링
     */
    @GetMapping("/new")
    public String createPage(
        @RequestParam(required = false) String recipeApiId,
        Model model
    ) {
        // 레시피 기반 공구인 경우 레시피 정보 조회
        if (recipeApiId != null && !recipeApiId.isBlank()) {
            try {
                com.recipemate.domain.recipe.dto.RecipeDetailResponse recipe = recipeService.getRecipeDetail(recipeApiId);
                model.addAttribute("recipe", recipe);
            } catch (CustomException e) {
                // 레시피를 찾을 수 없는 경우 에러 메시지 추가
                model.addAttribute("errorMessage", "레시피 정보를 불러올 수 없습니다.");
            }
        }
        return "group-purchases/form";
    }
    
    /**
     * 공구 수정 페이지 렌더링
     */
    @GetMapping("/{purchaseId}/edit")
    public String editPage(@PathVariable Long purchaseId, Model model) {
        GroupBuyResponse groupBuy = groupBuyService.getGroupBuyDetail(purchaseId);
        model.addAttribute("groupBuy", groupBuy);
        return "group-purchases/form";
    }

    // ========== 폼 처리 엔드포인트 ==========

    /**
     * 일반 공구 생성 폼 제출
     */
    @PostMapping
    public String createGroupBuy(
        @AuthenticationPrincipal UserDetails userDetails,
        @Valid @ModelAttribute CreateGroupBuyRequest request,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        // 1. 유효성 검증 실패 처리
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/group-purchases/new";
        }
        
        User user = userRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        GroupBuyResponse response = groupBuyService.createGroupBuy(user.getId(), request);
        redirectAttributes.addFlashAttribute("successMessage", "공동구매가 성공적으로 생성되었습니다.");
        return "redirect:/group-purchases/" + response.getId();
    }

    /**
     * 레시피 기반 공구 생성 폼 제출
     */
    @PostMapping("/recipe-based")
    public String createRecipeBasedGroupBuy(
        @AuthenticationPrincipal UserDetails userDetails,
        @Valid @ModelAttribute CreateGroupBuyRequest request,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        // 1. 유효성 검증 실패 처리
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/group-purchases/new";
        }
        
        // 2. JSON 형식의 재료 데이터 파싱
        java.util.List<SelectedIngredient> ingredients = new java.util.ArrayList<>();
        if (request.getSelectedIngredientsJson() != null && !request.getSelectedIngredientsJson().isEmpty()) {
            try {
                ingredients = objectMapper.readValue(
                    request.getSelectedIngredientsJson(),
                    objectMapper.getTypeFactory().constructCollectionType(java.util.List.class, SelectedIngredient.class)
                );
            } catch (Exception e) {
                log.error("재료 JSON 파싱 실패: {}", e.getMessage());
                redirectAttributes.addFlashAttribute("errorMessage", "재료 정보 처리 중 오류가 발생했습니다.");
                return "redirect:/group-purchases/new?recipeApiId=" + request.getRecipeApiId();
            }
        }
        
        // 3. 최소 1개 이상의 재료 선택 확인
        if (ingredients.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "최소 1개 이상의 재료를 선택해주세요.");
            return "redirect:/group-purchases/new?recipeApiId=" + request.getRecipeApiId();
        }
        
        // 4. 파싱된 재료 리스트를 request에 설정
        request.setSelectedIngredients(ingredients);
        
        User user = userRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        GroupBuyResponse response = groupBuyService.createRecipeBasedGroupBuy(user.getId(), request);
        redirectAttributes.addFlashAttribute("successMessage", "레시피 기반 공동구매가 성공적으로 생성되었습니다.");
        return "redirect:/group-purchases/" + response.getId();
    }

    /**
     * 공구 수정 폼 제출
     */
    @PostMapping("/{purchaseId}")
    public String updateGroupBuy(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long purchaseId,
        @Valid @ModelAttribute UpdateGroupBuyRequest request,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        // 1. 유효성 검증 실패 처리
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/group-purchases/" + purchaseId + "/edit";
        }
        
        User user = userRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        groupBuyService.updateGroupBuy(user.getId(), purchaseId, request);
        redirectAttributes.addFlashAttribute("successMessage", "공동구매가 성공적으로 수정되었습니다.");
        return "redirect:/group-purchases/" + purchaseId;
    }

    /**
     * 공구 삭제 폼 제출
     */
    @PostMapping("/{purchaseId}/delete")
    public String deleteGroupBuy(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long purchaseId,
        RedirectAttributes redirectAttributes
    ) {
        User user = userRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        groupBuyService.deleteGroupBuy(user.getId(), purchaseId);
        redirectAttributes.addFlashAttribute("successMessage", "공동구매가 성공적으로 삭제되었습니다.");
        return "redirect:/group-purchases/list";
    }

    /**
     * 공구 참여 폼 제출
     */
    @PostMapping("/{purchaseId}/participate")
    public String participate(
        @PathVariable Long purchaseId,
        @AuthenticationPrincipal UserDetails userDetails,
        @RequestParam Integer quantity,
        @RequestParam DeliveryMethod deliveryMethod,
        RedirectAttributes redirectAttributes
    ) {
        User user = userRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        ParticipateRequest request = ParticipateRequest.builder()
            .selectedDeliveryMethod(deliveryMethod)
            .quantity(quantity)
            .build();
        participationService.participate(user.getId(), purchaseId, request);
        redirectAttributes.addFlashAttribute("successMessage", "공동구매에 성공적으로 참여했습니다.");
        return "redirect:/group-purchases/" + purchaseId;
    }

    /**
     * 공구 참여 취소 폼 제출
     */
    @PostMapping("/{purchaseId}/participate/cancel")
    public String cancelParticipation(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long purchaseId,
        RedirectAttributes redirectAttributes
    ) {
        User user = userRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        participationService.cancelParticipation(user.getId(), purchaseId);
        redirectAttributes.addFlashAttribute("successMessage", "공동구매 참여가 취소되었습니다.");
        return "redirect:/group-purchases/" + purchaseId;
    }
    
    /**
     * 공구 찜하기 폼 제출
     */
    @PostMapping("/{purchaseId}/bookmarks")
    public String addWishlist(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long purchaseId,
        RedirectAttributes redirectAttributes
    ) {
        User user = userRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        wishlistService.addWishlist(user.getId(), purchaseId);
        redirectAttributes.addFlashAttribute("successMessage", "찜 목록에 추가되었습니다.");
        return "redirect:/group-purchases/{purchaseId}";
    }
    
    /**
     * 공구 찜 취소 폼 제출
     */
    @PostMapping("/{purchaseId}/bookmarks/cancel")
    public String removeWishlist(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long purchaseId,
        RedirectAttributes redirectAttributes
    ) {
        User user = userRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        wishlistService.removeWishlist(user.getId(), purchaseId);
        redirectAttributes.addFlashAttribute("successMessage", "찜 목록에서 제거되었습니다.");
        return "redirect:/group-purchases/{purchaseId}";
    }
    
    // ========== htmx용 HTML Fragment 엔드포인트 (향후 추가) ==========
    // TODO: htmx 통합 시 아래 엔드포인트 구현
    // @GetMapping("/search-fragment") - 검색 결과 HTML 조각 (리스트 아이템들)
    // @GetMapping("/{purchaseId}/participants-fragment") - 참여자 목록 HTML 조각
    // @PostMapping("/{purchaseId}/participate-fragment") - 참여 폼 처리 후 HTML 조각 반환
}
