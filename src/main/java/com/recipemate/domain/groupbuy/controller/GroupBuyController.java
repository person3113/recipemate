package com.recipemate.domain.groupbuy.controller;

import com.recipemate.domain.groupbuy.dto.CreateGroupBuyRequest;
import com.recipemate.domain.groupbuy.dto.GroupBuyResponse;
import com.recipemate.domain.groupbuy.service.GroupBuyService;
import com.recipemate.domain.user.entity.User;
import com.recipemate.domain.user.repository.UserRepository;
import com.recipemate.global.common.ApiResponse;
import com.recipemate.global.exception.CustomException;
import com.recipemate.global.exception.ErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/group-purchases")
@RequiredArgsConstructor
public class GroupBuyController {

    private final GroupBuyService groupBuyService;
    private final UserRepository userRepository;

    /**
     * 일반 공구 생성
     */
    @PostMapping
    public ApiResponse<GroupBuyResponse> createGroupBuy(
        @AuthenticationPrincipal UserDetails userDetails,
        @Valid @ModelAttribute CreateGroupBuyRequest request
    ) {
        // 이메일로 사용자 조회
        User user = userRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        GroupBuyResponse response = groupBuyService.createGroupBuy(user.getId(), request);
        return ApiResponse.success(response);
    }
}
