package com.recipemate.domain.groupbuy.controller;

import com.fasterxml.jackson.core.type.TypeReference;
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
import com.recipemate.global.common.GroupBuyCategory;
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
        // 문자열 category를 GroupBuyCategory enum으로 변환
        GroupBuyCategory categoryEnum = null;
        if (category != null && !category.isBlank()) {
            try {
                categoryEnum = GroupBuyCategory.valueOf(category);
            } catch (IllegalArgumentException e) {
                // 잘못된 카테고리는 무시
                log.warn("Invalid category value: {}", category);
            }
        }
        
        GroupBuySearchCondition condition = GroupBuySearchCondition.builder()
            .category(categoryEnum)
            .status(status)
            .recipeOnly(recipeOnly)
            .keyword(keyword)
            .build();
        
        Page<GroupBuyResponse> result = groupBuyService.getGroupBuyList(condition, pageable);
        model.addAttribute("groupBuys", result);
        model.addAttribute("searchCondition", condition);
        model.addAttribute("categories", GroupBuyCategory.values()); // enum 값들을 템플릿에 전달
        
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
        // 빈 폼 객체 추가 (Thymeleaf th:object를 위해 필수)
        CreateGroupBuyRequest formData = new CreateGroupBuyRequest();
        
        // 레시피 기반 공구인 경우 레시피 정보 조회 및 초기값 설정
        if (recipeApiId != null && !recipeApiId.isBlank()) {
            try {
                com.recipemate.domain.recipe.dto.RecipeDetailResponse recipe = recipeService.getRecipeDetailByApiId(recipeApiId);
                model.addAttribute("recipe", recipe);
                
                // 레시피 정보로 폼 초기값 설정
                formData.setTitle(recipe.getName() + " 재료 공동구매");
                formData.setContent(recipe.getName() + " 레시피에 필요한 재료들을 공동구매합니다!\n\n아래 재료들을 함께 구매하면 더 저렴하게 구입할 수 있습니다.");
                formData.setRecipeApiId(recipe.getId());
                formData.setRecipeName(recipe.getName());
                formData.setRecipeImageUrl(recipe.getImageUrl());
                
                // 레시피 기반 공구는 항상 RECIPE 카테고리 사용
                formData.setCategory(GroupBuyCategory.RECIPE);
            } catch (CustomException e) {
                // 레시피를 찾을 수 없는 경우 에러 메시지 추가
                model.addAttribute("errorMessage", "레시피 정보를 불러올 수 없습니다.");
            }
        }
        
        model.addAttribute("formData", formData);
        model.addAttribute("createGroupBuyRequest", formData); // POST 핸들러와의 호환성
        model.addAttribute("categories", GroupBuyCategory.values()); // 카테고리 목록 전달
        return "group-purchases/form";
    }
    
    /**
     * 공구 수정 페이지 렌더링
     */
    @GetMapping("/{purchaseId}/edit")
    public String editPage(@PathVariable Long purchaseId, Model model) {
        GroupBuyResponse groupBuy = groupBuyService.getGroupBuyDetail(purchaseId);
        model.addAttribute("groupBuy", groupBuy);
        
        // 기존 데이터로 폼 객체 초기화 (Thymeleaf th:field가 값을 채우기 위해 필요)
        UpdateGroupBuyRequest formData = new UpdateGroupBuyRequest();
        formData.setTitle(groupBuy.getTitle());
        formData.setContent(groupBuy.getContent());
        formData.setCategory(groupBuy.getCategory());
        formData.setTotalPrice(groupBuy.getTotalPrice());
        formData.setTargetHeadcount(groupBuy.getTargetHeadcount());
        formData.setDeadline(groupBuy.getDeadline());
        formData.setDeliveryMethod(groupBuy.getDeliveryMethod());
        formData.setMeetupLocation(groupBuy.getMeetupLocation());
        formData.setParcelFee(groupBuy.getParcelFee());
        formData.setIsParticipantListPublic(groupBuy.getIsParticipantListPublic());
        
        model.addAttribute("formData", formData);
        model.addAttribute("updateGroupBuyRequest", formData); // POST 핸들러와의 호환성
        model.addAttribute("categories", GroupBuyCategory.values()); // 카테고리 목록 전달
        return "group-purchases/form";
    }

    // ========== 폼 처리 엔드포인트 ==========

    /**
     * 일반 공구 생성 폼 제출
     */
    @PostMapping
    public String createGroupBuy(
        @AuthenticationPrincipal UserDetails userDetails,
        @Valid @ModelAttribute("createGroupBuyRequest") CreateGroupBuyRequest request,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        // 1. 유효성 검증 실패 처리
        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMessage", 
                bindingResult.getAllErrors().get(0).getDefaultMessage());
            model.addAttribute("formData", request); // Thymeleaf th:object를 위해 필수
            // 입력된 데이터를 유지하면서 폼 페이지로 직접 반환
            return "group-purchases/form";
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
        @Valid @ModelAttribute("createGroupBuyRequest") CreateGroupBuyRequest request,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        // 1. 유효성 검증 실패 처리
        if (bindingResult.hasErrors()) {
            model.addAttribute("formData", request);
            model.addAttribute("errorMessage", 
                bindingResult.getAllErrors().get(0).getDefaultMessage());
            // 레시피 정보 다시 조회해서 모델에 추가
            if (request.getRecipeApiId() != null && !request.getRecipeApiId().isBlank()) {
                try {
                    com.recipemate.domain.recipe.dto.RecipeDetailResponse recipe = 
                        recipeService.getRecipeDetail(request.getRecipeApiId());
                    model.addAttribute("recipe", recipe);
                } catch (CustomException e) {
                    model.addAttribute("errorMessage", "레시피 정보를 불러올 수 없습니다.");
                }
            }
            // 입력된 데이터를 유지하면서 폼 페이지로 직접 반환
            return "group-purchases/form";
        }
        
        // 2. JSON에서 선택된 재료 파싱
        if (request.getSelectedIngredientsJson() != null && !request.getSelectedIngredientsJson().isBlank()) {
            try {
                java.util.List<SelectedIngredient> ingredients = objectMapper.readValue(
                    request.getSelectedIngredientsJson(), 
                    new TypeReference<java.util.List<SelectedIngredient>>() {}
                );
                
                if (ingredients == null || ingredients.isEmpty()) {
                    model.addAttribute("formData", request);
                    model.addAttribute("errorMessage", "선택된 재료가 없습니다. 최소 1개 이상의 재료를 선택해주세요.");
                    // 레시피 정보 다시 조회해서 모델에 추가
                    if (request.getRecipeApiId() != null && !request.getRecipeApiId().isBlank()) {
                        try {
                            com.recipemate.domain.recipe.dto.RecipeDetailResponse recipe = 
                                recipeService.getRecipeDetail(request.getRecipeApiId());
                            model.addAttribute("recipe", recipe);
                        } catch (CustomException e) {
                            log.error("레시피 조회 실패: recipeApiId={}, error={}", request.getRecipeApiId(), e.getMessage());
                        }
                    }
                    return "group-purchases/form";
                }
                
                request.setSelectedIngredients(ingredients);
                log.info("레시피 기반 공구 생성: recipeApiId={}, 선택된 재료 개수={}", 
                    request.getRecipeApiId(), ingredients.size());
                
            } catch (Exception e) {
                log.error("재료 JSON 파싱 실패: {}", e.getMessage());
                model.addAttribute("formData", request);
                model.addAttribute("errorMessage", "재료 정보 처리 중 오류가 발생했습니다.");
                // 레시피 정보 다시 조회해서 모델에 추가
                if (request.getRecipeApiId() != null && !request.getRecipeApiId().isBlank()) {
                    try {
                        com.recipemate.domain.recipe.dto.RecipeDetailResponse recipe = 
                            recipeService.getRecipeDetail(request.getRecipeApiId());
                        model.addAttribute("recipe", recipe);
                    } catch (CustomException ex) {
                        log.error("레시피 조회 실패: recipeApiId={}, error={}", request.getRecipeApiId(), ex.getMessage());
                    }
                }
                return "group-purchases/form";
            }
        } else {
            model.addAttribute("formData", request);
            model.addAttribute("errorMessage", "선택된 재료 정보가 없습니다.");
            // 레시피 정보 다시 조회해서 모델에 추가
            if (request.getRecipeApiId() != null && !request.getRecipeApiId().isBlank()) {
                try {
                    com.recipemate.domain.recipe.dto.RecipeDetailResponse recipe = 
                        recipeService.getRecipeDetail(request.getRecipeApiId());
                    model.addAttribute("recipe", recipe);
                } catch (CustomException e) {
                    log.error("레시피 조회 실패: recipeApiId={}, error={}", request.getRecipeApiId(), e.getMessage());
                }
            }
            return "group-purchases/form";
        }
        
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
        @Valid @ModelAttribute("updateGroupBuyRequest") UpdateGroupBuyRequest request,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        // 1. 유효성 검증 실패 처리
        if (bindingResult.hasErrors()) {
            model.addAttribute("formData", request);
            model.addAttribute("errorMessage", 
                bindingResult.getAllErrors().get(0).getDefaultMessage());
            // 기존 공동구매 정보 조회해서 모델에 추가
            GroupBuyResponse groupBuy = groupBuyService.getGroupBuyDetail(purchaseId);
            model.addAttribute("groupBuy", groupBuy);
            // 입력된 데이터를 유지하면서 폼 페이지로 직접 반환
            return "group-purchases/form";
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
