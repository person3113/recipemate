package com.recipemate.global.controller;

import com.recipemate.domain.groupbuy.dto.GroupBuyResponse;
import com.recipemate.domain.groupbuy.service.GroupBuyService;
import com.recipemate.domain.recipe.dto.RecipeListResponse;
import com.recipemate.domain.recipe.service.RecipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * 홈 컨트롤러
 * 루트 경로(/)를 컨트롤러로 매핑하여 GlobalControllerAdvice가 적용되도록 함
 */
@Controller
@RequiredArgsConstructor
public class HomeController {
    
    private final RecipeService recipeService;
    private final GroupBuyService groupBuyService;
    
    /**
     * 홈페이지 렌더링
     * GET /
     */
    @GetMapping("/")
    public String home(Model model) {
        // 인기 레시피 (연결된 공구 수 기준, 최대 5개)
        RecipeListResponse popularRecipesResponse = recipeService.findPopularRecipes(5);
        model.addAttribute("popularRecipes", popularRecipesResponse.getRecipes());
        
        // 마감 임박 공구 (48시간 이내, 최대 3개)
        List<GroupBuyResponse> imminentGroupBuys = groupBuyService.findImminentGroupPurchases(3);
        model.addAttribute("imminentGroupBuys", imminentGroupBuys);
        
        // 오늘의 추천 레시피 (랜덤 3개)
        RecipeListResponse randomRecipesResponse = recipeService.getRandomRecipes(3);
        model.addAttribute("randomRecipes", randomRecipesResponse.getRecipes());
        
        // 인기 공구 (참여자 수 및 최신순 기준, 최대 3개)
        List<GroupBuyResponse> hotGroupBuys = groupBuyService.getPopularGroupBuys(3);
        model.addAttribute("hotGroupBuys", hotGroupBuys);
        
        return "index";
    }
}
