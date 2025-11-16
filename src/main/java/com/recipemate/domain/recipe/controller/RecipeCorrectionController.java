package com.recipemate.domain.recipe.controller;

import com.recipemate.domain.recipe.dto.RecipeCorrectionCreateRequest;
import com.recipemate.domain.recipe.dto.RecipeCorrectionResponse;
import com.recipemate.domain.recipe.dto.RecipeDetailResponse;
import com.recipemate.domain.recipe.entity.CorrectionType;
import com.recipemate.domain.recipe.entity.Recipe;
import com.recipemate.domain.recipe.entity.RecipeSource;
import com.recipemate.domain.recipe.repository.RecipeRepository;
import com.recipemate.domain.recipe.service.RecipeCorrectionService;
import com.recipemate.domain.recipe.service.RecipeService;
import com.recipemate.domain.user.entity.User;
import com.recipemate.domain.user.repository.UserRepository;
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
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 레시피 개선 제안 컨트롤러 (사용자용)
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/recipes/{recipeApiId}/corrections")
public class RecipeCorrectionController {

    private final RecipeCorrectionService correctionService;
    private final RecipeService recipeService;
    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;

    private static final String MEAL_PREFIX = "meal-";
    private static final String FOOD_PREFIX = "food-";

    /**
     * 제안 작성 폼 페이지
     */
    @GetMapping("/new")
    public String showCorrectionForm(
            @PathVariable String recipeApiId,
            Model model
    ) {
        // 레시피 정보 조회 (API ID로 조회 - meal-{id} 또는 food-{id})
        RecipeDetailResponse recipe = recipeService.getRecipeDetailByApiId(recipeApiId);
        
        model.addAttribute("recipe", recipe);
        model.addAttribute("recipeApiId", recipeApiId);
        model.addAttribute("correctionTypes", CorrectionType.values());
        return "recipes/correction-form";
    }

    /**
     * 제안 제출
     */
    @PostMapping
    public String createCorrection(
            @PathVariable String recipeApiId,
            @Valid @ModelAttribute RecipeCorrectionCreateRequest request,
            BindingResult bindingResult,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        // 디버깅: 요청 데이터 확인
        System.out.println("=== RecipeCorrection Form Submission ===");
        System.out.println("recipeApiId: " + recipeApiId);
        System.out.println("request.getCorrectionType(): " + request.getCorrectionType());
        System.out.println("request.getProposedChange(): " + request.getProposedChange());
        System.out.println("bindingResult.hasErrors(): " + bindingResult.hasErrors());
        if (bindingResult.hasErrors()) {
            System.out.println("Validation Errors:");
            bindingResult.getAllErrors().forEach(error -> {
                System.out.println("  - " + error.getDefaultMessage());
            });
        }
        
        // 유효성 검증 실패 시
        if (bindingResult.hasErrors()) {
            RecipeDetailResponse recipe = recipeService.getRecipeDetailByApiId(recipeApiId);
            model.addAttribute("recipe", recipe);
            model.addAttribute("recipeApiId", recipeApiId);
            model.addAttribute("correctionTypes", CorrectionType.values());
            model.addAttribute("error", "입력 내용을 확인해주세요.");
            return "recipes/correction-form";
        }

        try {
            // API ID를 DB ID로 변환
            Long recipeDbId = convertApiIdToDbId(recipeApiId);
            
            // 이메일로 사용자 조회 (UserDetails의 username은 email)
            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
            
            // 제안 생성
            RecipeCorrectionResponse response = correctionService.createCorrection(
                    user.getId(), recipeDbId, request);
            
            redirectAttributes.addFlashAttribute("message", "제안이 성공적으로 제출되었습니다. 관리자 검토 후 결과를 알려드립니다.");
            return "redirect:/recipes/" + recipeApiId;
            
        } catch (CustomException e) {
            RecipeDetailResponse recipe = recipeService.getRecipeDetailByApiId(recipeApiId);
            model.addAttribute("recipe", recipe);
            model.addAttribute("recipeApiId", recipeApiId);
            model.addAttribute("correctionTypes", CorrectionType.values());
            model.addAttribute("error", e.getMessage());
            return "recipes/correction-form";
        }
    }

    /**
     * API ID를 DB ID로 변환
     */
    private Long convertApiIdToDbId(String apiId) {
        // API ID 파싱 (meal-{id} 또는 food-{id})
        RecipeSource sourceApi;
        String sourceApiId;
        
        if (apiId.startsWith(MEAL_PREFIX)) {
            sourceApi = RecipeSource.MEAL_DB;
            sourceApiId = apiId.substring(MEAL_PREFIX.length());
        } else if (apiId.startsWith(FOOD_PREFIX)) {
            sourceApi = RecipeSource.FOOD_SAFETY;
            sourceApiId = apiId.substring(FOOD_PREFIX.length());
        } else {
            // 사용자 레시피는 이미 DB ID
            try {
                return Long.parseLong(apiId);
            } catch (NumberFormatException e) {
                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
            }
        }
        
        // DB에서 레시피 조회
        Recipe recipe = recipeRepository.findBySourceApiAndSourceApiId(sourceApi, sourceApiId)
                .orElseThrow(() -> new CustomException(ErrorCode.RECIPE_NOT_FOUND));
        
        return recipe.getId();
    }

    /**
     * 제안 상세 조회 (API)
     */
    @GetMapping("/{correctionId}")
    @ResponseBody
    public RecipeCorrectionResponse getCorrection(
            @PathVariable Long correctionId
    ) {
        return correctionService.getCorrection(correctionId);
    }
}
