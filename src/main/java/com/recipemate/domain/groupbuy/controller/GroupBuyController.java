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
import com.recipemate.domain.user.service.CustomUserDetailsService.CustomUserDetails;
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
import org.springframework.beans.factory.annotation.Value;
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

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/group-purchases")
@RequiredArgsConstructor
public class GroupBuyController {

    private final GroupBuyService groupBuyService;
    private final ParticipationService participationService;
    private final WishlistService wishlistService;
    private final com.recipemate.domain.recipe.service.RecipeService recipeService;
    private final ObjectMapper objectMapper;
    
    @Value("${kakao.javascript-key:}")
    private String kakaoJavascriptKey;

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
        @RequestParam(required = false) String ingredients,
        @RequestParam(required = false, defaultValue = "latest") String sortBy,
        @RequestParam(required = false, defaultValue = "desc") String direction,
        @PageableDefault(size = 20) Pageable pageable,
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
            .ingredients(ingredients)
            .sortBy(sortBy)
            .direction(direction)
            .build();
        
        Page<GroupBuyResponse> result = groupBuyService.getGroupBuyList(condition, pageable);
        model.addAttribute("groupBuys", result);
        model.addAttribute("searchCondition", condition);
        model.addAttribute("categories", GroupBuyCategory.values()); // enum 값들을 템플릿에 전달
        model.addAttribute("currentSort", sortBy);
        model.addAttribute("currentDir", direction);
        
        return "group-purchases/list";
    }
    
    /**
     * 공구 상세 페이지 렌더링
     */
    @GetMapping("/{purchaseId}")
    public String detailPage(
        @PathVariable Long purchaseId,
        @AuthenticationPrincipal UserDetails userDetails,
        Model model
    ) {
        // 로그인한 사용자의 ID 조회
        Long currentUserId = null;
        if (userDetails instanceof CustomUserDetails) {
            CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
            currentUserId = customUserDetails.getUserId();
        }
        
        // 사용자 상태 정보를 포함한 공구 상세 조회
        GroupBuyResponse groupBuy = currentUserId != null 
            ? groupBuyService.getGroupBuyDetail(purchaseId, currentUserId)
            : groupBuyService.getGroupBuyDetail(purchaseId);
        model.addAttribute("groupBuy", groupBuy);
        
        // 카카오 지도 API 키 추가
        model.addAttribute("kakaoJavascriptKey", kakaoJavascriptKey);
        
        // 레시피 기반 공구인 경우, 레시피가 삭제되었는지 확인
        if (groupBuy.getRecipeApiId() != null) {
            try {
                String recipeApiId = groupBuy.getRecipeApiId();
                
                // 사용자 레시피 (순수 숫자) vs API 레시피 (meal-, food- 접두사) 구분
                if (recipeApiId.matches("\\d+")) {
                    // 순수 숫자 = 사용자 레시피 (DB ID로 조회)
                    Long recipeDbId = Long.parseLong(recipeApiId);
                    recipeService.getRecipeDetailById(recipeDbId);
                } else {
                    // 접두사 있음 = API 레시피 (API ID로 조회)
                    recipeService.getRecipeDetail(recipeApiId);
                }
                
                model.addAttribute("isRecipeDeleted", false);
            } catch (CustomException e) {
                if (e.getErrorCode() == ErrorCode.RECIPE_NOT_FOUND) {
                    log.info("Recipe {} for group buy {} is deleted", groupBuy.getRecipeApiId(), purchaseId);
                    model.addAttribute("isRecipeDeleted", true);
                } else {
                    log.error("Error checking recipe status for group buy {}: {}", purchaseId, e.getMessage());
                    throw e;
                }
            } catch (Exception e) {
                log.warn("Failed to check recipe status for group buy {}: {}", purchaseId, e.getMessage());
            }
        }
        
        // 재료 목록 JSON 파싱 (모든 공구 유형에서 시도)
        if (groupBuy.getIngredients() != null && !groupBuy.getIngredients().isBlank()) {
            try {
                List<SelectedIngredient> ingredientsList = objectMapper.readValue(
                    groupBuy.getIngredients(),
                    new TypeReference<List<SelectedIngredient>>() {}
                );
                model.addAttribute("ingredientsList", ingredientsList);
            } catch (Exception e) {
                log.warn("Failed to parse ingredients JSON for group buy {}: {}", purchaseId, e.getMessage());
                // 파싱 실패 시 프론트엔드에서 Fallback 표시 (원본 텍스트)
            }
        }
        
        // 참여자 목록 공개 여부 확인 후 로드 (비로그인 사용자도 가능)
        if (groupBuy.getIsParticipantListPublic()) {
            try {
                java.util.List<com.recipemate.domain.groupbuy.dto.ParticipantResponse> participants = 
                    participationService.getParticipants(purchaseId, currentUserId);
                model.addAttribute("participants", participants);
            } catch (Exception e) {
                log.warn("Failed to load participants list for group buy {}: {}", purchaseId, e.getMessage());
                // 참여자 목록 로드 실패해도 페이지는 정상 표시
            }
        }
        
        return "group-purchases/detail";
    }
    
    /**
     * 공구 작성 페이지 렌더링
     */
    @GetMapping("/new")
    public String createPage(
        @RequestParam(required = false) String recipeApiId,
        @RequestParam(required = false) String redirectUrl,
        Model model
    ) {
        // 카카오 지도 API 키 추가
        log.info("Kakao JavaScript Key: {}", kakaoJavascriptKey != null && !kakaoJavascriptKey.isEmpty() ? "Loaded (length: " + kakaoJavascriptKey.length() + ")" : "NOT LOADED or EMPTY");
        model.addAttribute("kakaoJavascriptKey", kakaoJavascriptKey);
        
        // redirectUrl 추가 (취소 버튼에서 사용)
        model.addAttribute("redirectUrl", redirectUrl);
        
        // 레시피 기반 공구인 경우 레시피 정보 조회
        com.recipemate.domain.recipe.dto.RecipeDetailResponse recipe = null;
        if (recipeApiId != null && !recipeApiId.isBlank()) {
            try {
                // ✅ recipeApiId가 순수 숫자인지 확인 (사용자 레시피 = DB ID)
                if (recipeApiId.matches("\\d+")) {
                    // 숫자만 있으면 DB ID로 조회
                    Long dbId = Long.parseLong(recipeApiId);
                    recipe = recipeService.getRecipeDetailById(dbId);
                } else {
                    // 접두사가 있으면 API ID로 조회 (meal-, food- 등)
                    recipe = recipeService.getRecipeDetailByApiId(recipeApiId);
                }

                model.addAttribute("recipe", recipe);
                log.info("레시피 정보 로드 완료 - recipeApiId: {}, 재료 개수: {}", 
                    recipeApiId, recipe.getIngredients() != null ? recipe.getIngredients().size() : 0);
            } catch (CustomException e) {
                // 레시피를 찾을 수 없는 경우 에러 메시지 추가
                log.error("레시피 정보 로드 실패 - recipeApiId: {}, error: {}", recipeApiId, e.getMessage());
                model.addAttribute("errorMessage", "레시피 정보를 불러올 수 없습니다.");
            }
        }
        
        // Flash 속성에서 폼 데이터가 없으면 새로 생성
        CreateGroupBuyRequest formData;
        if (!model.containsAttribute("createGroupBuyRequest")) {
            formData = new CreateGroupBuyRequest();
            
            // 레시피 정보로 폼 초기값 설정 (레시피가 로드된 경우에만)
            if (recipe != null) {
                formData.setTitle(recipe.getName() + " 재료 공동구매");
                formData.setContent(recipe.getName() + " 레시피에 필요한 재료들을 공동구매합니다!\n\n아래 재료들을 함께 구매하면 더 저렴하게 구입할 수 있습니다.");
                formData.setRecipeApiId(recipe.getId());
                formData.setRecipeName(recipe.getName());
                formData.setRecipeImageUrl(recipe.getImageUrl());
                
                // 레시피 기반 공구는 항상 RECIPE 카테고리 사용
                formData.setCategory(GroupBuyCategory.RECIPE);
            }
            
            model.addAttribute("createGroupBuyRequest", formData);
        } else {
            // Flash 속성이 있으면 그것을 사용 (PRG 패턴에서 리다이렉트된 경우)
            formData = (CreateGroupBuyRequest) model.asMap().get("createGroupBuyRequest");
        }
        
        model.addAttribute("formData", formData);
        model.addAttribute("categories", GroupBuyCategory.values()); // 카테고리 목록 전달
        return "group-purchases/form";
    }
    
    /**
     * 공구 수정 페이지 렌더링
     */
    @GetMapping("/{purchaseId}/edit")
    public String editPage(
        @PathVariable Long purchaseId, 
        @RequestParam(required = false) String redirectUrl,
        Model model
    ) {
        GroupBuyResponse groupBuy = groupBuyService.getGroupBuyDetail(purchaseId);
        model.addAttribute("groupBuy", groupBuy);
        model.addAttribute("redirectUrl", redirectUrl);
        
        // 카카오 지도 API 키 추가
        model.addAttribute("kakaoJavascriptKey", kakaoJavascriptKey);
        
        // Flash 속성에서 폼 데이터가 없으면 기존 데이터로 초기화
        UpdateGroupBuyRequest formData;
        if (model.containsAttribute("updateGroupBuyRequest")) {
            // Flash 속성에서 가져오기 (유효성 검증 실패 후 리다이렉트된 경우)
            formData = (UpdateGroupBuyRequest) model.getAttribute("updateGroupBuyRequest");
        } else {
            // 기존 데이터로 초기화
            formData = new UpdateGroupBuyRequest();
            formData.setTitle(groupBuy.getTitle());
            formData.setContent(groupBuy.getContent());
            formData.setCategory(groupBuy.getCategory());
            formData.setTargetAmount(groupBuy.getTargetAmount());
            formData.setTargetHeadcount(groupBuy.getTargetHeadcount());
            formData.setDeadline(groupBuy.getDeadline());
            formData.setDeliveryMethod(groupBuy.getDeliveryMethod());
            formData.setMeetupLocation(groupBuy.getMeetupLocation());
            formData.setLatitude(groupBuy.getLatitude());
            formData.setLongitude(groupBuy.getLongitude());
            formData.setParcelFee(groupBuy.getParcelFee());
            formData.setIsParticipantListPublic(groupBuy.getIsParticipantListPublic());
        }
        
        // 기존 이미지 URL 목록 추가
        model.addAttribute("existingImages", groupBuy.getImageUrls());
        
        // 재료 목록 파싱 (모든 공구 유형에서 시도)
        if (groupBuy.getIngredients() != null && !groupBuy.getIngredients().isBlank()) {
            try {
                List<SelectedIngredient> ingredientsList = objectMapper.readValue(
                    groupBuy.getIngredients(),
                    new TypeReference<List<SelectedIngredient>>() {}
                );
                model.addAttribute("ingredientsList", ingredientsList);
            } catch (Exception e) {
                log.warn("Failed to parse ingredients JSON for group buy {}: {}", purchaseId, e.getMessage());
                // 파싱 실패 시에도 폼은 정상 표시 (사용자가 재료를 다시 입력 가능)
            }
        }
        
        // 레시피 기반 공구 수정 시: 레시피 정보는 추가하지 않음
        // (수정 시에는 DB에 저장된 재료만 표시하고, 레시피 재료는 로딩하지 않음)
        
        model.addAttribute("formData", formData);
        model.addAttribute("categories", GroupBuyCategory.values()); // 카테고리 목록 전달
        return "group-purchases/form";
    }
    
    /**
     * 참여자 관리 페이지 렌더링
     */
    @GetMapping("/{purchaseId}/participants")
    public String participantsPage(
        @PathVariable Long purchaseId,
        @AuthenticationPrincipal UserDetails userDetails,
        Model model
    ) {
        CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
        User user = customUserDetails.getUser();
        
        GroupBuyResponse groupBuy = groupBuyService.getGroupBuyDetail(purchaseId);
        java.util.List<com.recipemate.domain.groupbuy.dto.ParticipantResponse> participants = 
            participationService.getParticipants(purchaseId, user.getId());
        
        model.addAttribute("groupBuy", groupBuy);
        model.addAttribute("participants", participants);
        model.addAttribute("currentUserId", user.getId());
        return "group-purchases/participants";
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
        RedirectAttributes redirectAttributes
    ) {
        // 1. 유효성 검증 실패 처리 - PRG 패턴 적용
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("createGroupBuyRequest", request);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.createGroupBuyRequest", bindingResult);
            redirectAttributes.addFlashAttribute("errorMessage", 
                bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/group-purchases/new";
        }
        
        // 2. JSON에서 선택된 재료 파싱 (일반 공구도 재료 필수)
        if (request.getSelectedIngredientsJson() != null && !request.getSelectedIngredientsJson().isBlank()) {
            try {
                java.util.List<SelectedIngredient> ingredients = objectMapper.readValue(
                    request.getSelectedIngredientsJson(), 
                    new TypeReference<java.util.List<SelectedIngredient>>() {}
                );
                
                if (ingredients == null || ingredients.isEmpty()) {
                    redirectAttributes.addFlashAttribute("createGroupBuyRequest", request);
                    redirectAttributes.addFlashAttribute("errorMessage", "선택된 재료가 없습니다. 최소 1개 이상의 재료를 선택해주세요.");
                    return "redirect:/group-purchases/new";
                }
                
                request.setSelectedIngredients(ingredients);
                log.info("일반 공구 생성: 선택된 재료 개수={}", ingredients.size());
                
            } catch (Exception e) {
                log.error("재료 JSON 파싱 실패: {}", e.getMessage());
                redirectAttributes.addFlashAttribute("createGroupBuyRequest", request);
                redirectAttributes.addFlashAttribute("errorMessage", "재료 정보 처리 중 오류가 발생했습니다.");
                return "redirect:/group-purchases/new";
            }
        } else {
            redirectAttributes.addFlashAttribute("createGroupBuyRequest", request);
            redirectAttributes.addFlashAttribute("errorMessage", "선택된 재료 정보가 없습니다. 최소 1개 이상의 재료를 선택해주세요.");
            return "redirect:/group-purchases/new";
        }
        
        CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
        User user = customUserDetails.getUser();
        
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
        RedirectAttributes redirectAttributes
    ) {
        // 1. 유효성 검증 실패 처리 - PRG 패턴 적용
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("createGroupBuyRequest", request);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.createGroupBuyRequest", bindingResult);
            redirectAttributes.addFlashAttribute("errorMessage", 
                bindingResult.getAllErrors().get(0).getDefaultMessage());
            // recipeApiId를 쿼리 파라미터로 유지
            if (request.getRecipeApiId() != null && !request.getRecipeApiId().isBlank()) {
                redirectAttributes.addAttribute("recipeApiId", request.getRecipeApiId());
            }
            return "redirect:/group-purchases/new";
        }
        
        // 2. JSON에서 선택된 재료 파싱
        if (request.getSelectedIngredientsJson() != null && !request.getSelectedIngredientsJson().isBlank()) {
            try {
                java.util.List<SelectedIngredient> ingredients = objectMapper.readValue(
                    request.getSelectedIngredientsJson(), 
                    new TypeReference<java.util.List<SelectedIngredient>>() {}
                );
                
                if (ingredients == null || ingredients.isEmpty()) {
                    redirectAttributes.addFlashAttribute("createGroupBuyRequest", request);
                    redirectAttributes.addFlashAttribute("errorMessage", "선택된 재료가 없습니다. 최소 1개 이상의 재료를 선택해주세요.");
                    if (request.getRecipeApiId() != null && !request.getRecipeApiId().isBlank()) {
                        redirectAttributes.addAttribute("recipeApiId", request.getRecipeApiId());
                    }
                    return "redirect:/group-purchases/new";
                }
                
                request.setSelectedIngredients(ingredients);
                log.info("레시피 기반 공구 생성: recipeApiId={}, 선택된 재료 개수={}", 
                    request.getRecipeApiId(), ingredients.size());
                
            } catch (Exception e) {
                log.error("재료 JSON 파싱 실패: {}", e.getMessage());
                redirectAttributes.addFlashAttribute("createGroupBuyRequest", request);
                redirectAttributes.addFlashAttribute("errorMessage", "재료 정보 처리 중 오류가 발생했습니다.");
                if (request.getRecipeApiId() != null && !request.getRecipeApiId().isBlank()) {
                    redirectAttributes.addAttribute("recipeApiId", request.getRecipeApiId());
                }
                return "redirect:/group-purchases/new";
            }
        } else {
            redirectAttributes.addFlashAttribute("createGroupBuyRequest", request);
            redirectAttributes.addFlashAttribute("errorMessage", "선택된 재료 정보가 없습니다.");
            if (request.getRecipeApiId() != null && !request.getRecipeApiId().isBlank()) {
                redirectAttributes.addAttribute("recipeApiId", request.getRecipeApiId());
            }
            return "redirect:/group-purchases/new";
        }
        
        CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
        User user = customUserDetails.getUser();
        
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
        @RequestParam(required = false) String deletedImages,
        @RequestParam(required = false) String redirectUrl,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        // 1. 유효성 검증 실패 처리 - PRG 패턴 적용
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("updateGroupBuyRequest", request);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.updateGroupBuyRequest", bindingResult);
            redirectAttributes.addFlashAttribute("errorMessage", 
                bindingResult.getAllErrors().get(0).getDefaultMessage());
            if (redirectUrl != null && !redirectUrl.isBlank()) {
                redirectAttributes.addAttribute("redirectUrl", redirectUrl);
            }
            return "redirect:/group-purchases/" + purchaseId + "/edit";
        }
        
        // 2. 재료 JSON 파싱 (레시피 기반 공구인 경우)
        if (request.getSelectedIngredientsJson() != null && !request.getSelectedIngredientsJson().isBlank()) {
            try {
                List<SelectedIngredient> ingredients = objectMapper.readValue(
                    request.getSelectedIngredientsJson(),
                    new TypeReference<List<SelectedIngredient>>() {}
                );
                request.setSelectedIngredients(ingredients);
                log.info("Parsed {} ingredients for group buy update", ingredients.size());
            } catch (Exception e) {
                log.error("Failed to parse ingredients JSON: {}", e.getMessage());
                redirectAttributes.addFlashAttribute("updateGroupBuyRequest", request);
                redirectAttributes.addFlashAttribute("errorMessage", "재료 정보 처리 중 오류가 발생했습니다.");
                if (redirectUrl != null && !redirectUrl.isBlank()) {
                    redirectAttributes.addAttribute("redirectUrl", redirectUrl);
                }
                return "redirect:/group-purchases/" + purchaseId + "/edit";
            }
        }
        
        // 3. deletedImages JSON 파싱
        List<String> deletedImagesList = new ArrayList<>();
        if (deletedImages != null && !deletedImages.isBlank()) {
            try {
                deletedImagesList = objectMapper.readValue(
                    deletedImages,
                    new TypeReference<List<String>>() {}
                );
                log.info("Parsed {} deleted images for group buy update", deletedImagesList.size());
            } catch (Exception e) {
                log.error("Failed to parse deletedImages JSON: {}", e.getMessage());
                // deletedImages 파싱 실패는 무시하고 계속 진행
            }
        }
        
        CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
        User user = customUserDetails.getUser();
        
        groupBuyService.updateGroupBuy(user.getId(), purchaseId, request, deletedImagesList);
        redirectAttributes.addFlashAttribute("successMessage", "공동구매가 성공적으로 수정되었습니다.");
        
        // redirectUrl이 제공되면 해당 URL로, 없으면 상세 페이지로 리다이렉트
        if (redirectUrl != null && !redirectUrl.isBlank()) {
            return "redirect:" + redirectUrl;
        }
        return "redirect:/group-purchases/" + purchaseId;
    }

    /**
     * 공구 삭제 폼 제출
     */
    @PostMapping("/{purchaseId}/delete")
    public String deleteGroupBuy(
        @PathVariable Long purchaseId,
        @AuthenticationPrincipal UserDetails userDetails,
        RedirectAttributes redirectAttributes
    ) {
        CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
        User user = customUserDetails.getUser();
        
        groupBuyService.deleteGroupBuy(user.getId(), purchaseId);
        redirectAttributes.addFlashAttribute("successMessage", "공동구매 기록이 삭제되었습니다.");
        return "redirect:/group-purchases/list";
    }

    /**
     * 공구 취소 폼 제출
     */
    @PostMapping("/{purchaseId}/cancel")
    public String cancelGroupBuy(
        @PathVariable Long purchaseId,
        @RequestParam(required = false) String redirectUrl,
        @AuthenticationPrincipal UserDetails userDetails,
        RedirectAttributes redirectAttributes
    ) {
        CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
        User user = customUserDetails.getUser();
        
        try {
            groupBuyService.cancelGroupBuy(user.getId(), purchaseId);
            redirectAttributes.addFlashAttribute("successMessage", "공동구매가 취소되었습니다.");
        } catch (CustomException e) {
            log.error("공구 취소 실패 - userId: {}, groupBuyId: {}, error: {}", 
                user.getId(), purchaseId, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        
        // redirectUrl이 제공되면 해당 URL로, 없으면 상세 페이지로 리다이렉트
        if (redirectUrl != null && !redirectUrl.isBlank()) {
            return "redirect:" + redirectUrl;
        }
        return "redirect:/group-purchases/" + purchaseId;
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
        @RequestParam(required = false) Long addressId,
        @RequestParam Integer totalPayment,
        RedirectAttributes redirectAttributes
    ) {
        log.info("공구 참여 요청 - groupBuyId: {}, quantity: {}, deliveryMethod: {}, addressId: {}, totalPayment: {}", 
            purchaseId, quantity, deliveryMethod, addressId, totalPayment);
        
        CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
        User user = customUserDetails.getUser();
        
        try {
            ParticipateRequest request = ParticipateRequest.builder()
                .selectedDeliveryMethod(deliveryMethod)
                .quantity(quantity)
                .addressId(addressId)
                .totalPayment(totalPayment)
                .build();
            participationService.participate(user.getId(), purchaseId, request);
            redirectAttributes.addFlashAttribute("successMessage", "공동구매에 성공적으로 참여했습니다.");
            log.info("공구 참여 성공 - userId: {}, groupBuyId: {}", user.getId(), purchaseId);
        } catch (CustomException e) {
            log.error("참여 실패 - userId: {}, groupBuyId: {}, error: {}", 
                user.getId(), purchaseId, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            log.error("공구 참여 중 예상치 못한 오류 - userId: {}, groupBuyId: {}", 
                user.getId(), purchaseId, e);
            redirectAttributes.addFlashAttribute("errorMessage", "참여 처리 중 오류가 발생했습니다.");
        }
        
        return "redirect:/group-purchases/" + purchaseId;
    }

    /**
     * 공구 참여 취소 폼 제출
     */
    @PostMapping("/{purchaseId}/participate/cancel")
    public String cancelParticipation(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long purchaseId,
        @RequestParam(required = false) String redirectUrl,
        RedirectAttributes redirectAttributes
    ) {
        CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
        User user = customUserDetails.getUser();
        
        try {
            participationService.cancelParticipation(user.getId(), purchaseId);
            redirectAttributes.addFlashAttribute("successMessage", "공동구매 참여가 취소되었습니다.");
        } catch (CustomException e) {
            log.error("참여 취소 실패 - userId: {}, groupBuyId: {}, error: {}", 
                user.getId(), purchaseId, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        
        // redirectUrl이 제공되면 해당 URL로, 없으면 공구 상세 페이지로
        if (redirectUrl != null && !redirectUrl.isBlank()) {
            return "redirect:" + redirectUrl;
        }
        return "redirect:/group-purchases/" + purchaseId;
    }

    /**
     * 참여자 강제 탈퇴 폼 제출 (주최자 전용)
     */
    @PostMapping("/{purchaseId}/participants/{userId}/remove")
    public String forceRemoveParticipant(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long purchaseId,
        @PathVariable Long userId,
        RedirectAttributes redirectAttributes
    ) {
        CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
        User host = customUserDetails.getUser();
        
        try {
            participationService.forceRemoveParticipant(host.getId(), purchaseId, userId);
            redirectAttributes.addFlashAttribute("successMessage", "참여자가 강제 탈퇴 처리되었습니다.");
        } catch (CustomException e) {
            log.error("강제 탈퇴 실패 - hostId: {}, groupBuyId: {}, participantUserId: {}, error: {}", 
                host.getId(), purchaseId, userId, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        
        return "redirect:/group-purchases/" + purchaseId + "/participants";
    }
    
    /**
     * 공구 찜하기 (AJAX용)
     */
    @PostMapping("/{purchaseId}/bookmarks")
    @ResponseBody
    public ApiResponse<Void> addWishlist(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long purchaseId
    ) {
        if (userDetails == null) {
            return ApiResponse.error(ErrorCode.UNAUTHORIZED.getCode(), ErrorCode.UNAUTHORIZED.getMessage());
        }
        
        CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
        User user = customUserDetails.getUser();
        
        wishlistService.addWishlist(user.getId(), purchaseId);
        return ApiResponse.success(null);
    }
    
    /**
     * 공구 찜 취소 (AJAX용)
     */
    @PostMapping("/{purchaseId}/bookmarks/cancel")
    @ResponseBody
    public ApiResponse<Void> removeWishlist(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long purchaseId
    ) {
        if (userDetails == null) {
            return ApiResponse.error(ErrorCode.UNAUTHORIZED.getCode(), ErrorCode.UNAUTHORIZED.getMessage());
        }
        
        CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
        User user = customUserDetails.getUser();
        
        wishlistService.removeWishlist(user.getId(), purchaseId);
        return ApiResponse.success(null);
    }
    
    /**
     * 공구 찜 상태 확인
     * GET /group-purchases/{purchaseId}/bookmarks/status
     */
    @GetMapping("/{purchaseId}/bookmarks/status")
    @ResponseBody
    public ApiResponse<Boolean> checkWishlistStatus(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long purchaseId
    ) {
        if (userDetails == null) {
            return ApiResponse.success(false);
        }
        
        CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
        User user = customUserDetails.getUser();
        
        boolean isWishlisted = wishlistService.isWishlisted(user.getId(), purchaseId);
        
        return ApiResponse.success(isWishlisted);
    }
    
    // ========== htmx용 HTML Fragment 엔드포인트 (향후 추가) ==========
    // TODO: htmx 통합 시 아래 엔드포인트 구현
    // @GetMapping("/search-fragment") - 검색 결과 HTML 조각 (리스트 아이템들)
    // @GetMapping("/{purchaseId}/participants-fragment") - 참여자 목록 HTML 조각
    // @PostMapping("/{purchaseId}/participate-fragment") - 참여 폼 처리 후 HTML 조각 반환
}
