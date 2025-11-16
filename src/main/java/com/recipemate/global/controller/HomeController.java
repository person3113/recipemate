package com.recipemate.global.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 홈 컨트롤러
 * 루트 경로(/)를 컨트롤러로 매핑하여 GlobalControllerAdvice가 적용되도록 함
 */
@Controller
public class HomeController {
    
    /**
     * 홈페이지 렌더링
     * GET /
     */
    @GetMapping("/")
    public String home() {
        return "index";
    }
}
