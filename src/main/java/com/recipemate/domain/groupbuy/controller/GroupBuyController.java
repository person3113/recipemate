package com.recipemate.domain.groupbuy.controller;

import com.recipemate.domain.groupbuy.dto.CreateGroupBuyRequest;
import com.recipemate.domain.groupbuy.dto.GroupBuyResponse;
import com.recipemate.domain.groupbuy.dto.GroupBuySearchCondition;
import com.recipemate.domain.groupbuy.dto.ParticipantResponse;
import com.recipemate.domain.groupbuy.dto.ParticipateRequest;
import com.recipemate.domain.groupbuy.dto.UpdateGroupBuyRequest;
import com.recipemate.domain.groupbuy.service.GroupBuyService;
import com.recipemate.domain.groupbuy.service.ParticipationService;
import com.recipemate.domain.user.entity.User;
import com.recipemate.domain.user.repository.UserRepository;
import com.recipemate.global.common.ApiResponse;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/group-purchases")
@RequiredArgsConstructor
public class GroupBuyController {

    private final GroupBuyService groupBuyService;
    private final ParticipationService participationService;
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

    /**
     * 공구 목록 조회 (검색 및 필터링)
     * @param category 카테고리 필터 (optional)
     * @param status 상태 필터 (optional: RECRUITING, IMMINENT, CLOSED)
     * @param recipeOnly 레시피 기반 공구만 조회 (optional, default: false)
     * @param keyword 검색 키워드 - 제목 또는 내용 (optional)
     * @param pageable 페이징 정보 (default: page=0, size=20, sort=createdAt,desc)
     */
    @GetMapping
    public ApiResponse<Page<GroupBuyResponse>> getGroupBuyList(
        @RequestParam(required = false) String category,
        @RequestParam(required = false) GroupBuyStatus status,
        @RequestParam(required = false, defaultValue = "false") Boolean recipeOnly,
        @RequestParam(required = false) String keyword,
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        GroupBuySearchCondition condition = GroupBuySearchCondition.builder()
            .category(category)
            .status(status)
            .recipeOnly(recipeOnly)
            .keyword(keyword)
            .build();
        
        Page<GroupBuyResponse> result = groupBuyService.getGroupBuyList(condition, pageable);
        return ApiResponse.success(result);
    }

    /**
     * 공구 상세 조회
     * @param purchaseId 공구 ID
     */
    @GetMapping("/{purchaseId}")
    public ApiResponse<GroupBuyResponse> getGroupBuyDetail(@PathVariable Long purchaseId) {
        GroupBuyResponse response = groupBuyService.getGroupBuyDetail(purchaseId);
        return ApiResponse.success(response);
    }

    /**
     * 공구 수정
     * @param userDetails 인증된 사용자
     * @param purchaseId 공구 ID
     * @param request 수정 요청 정보
     */
    @PutMapping("/{purchaseId}")
    public ApiResponse<GroupBuyResponse> updateGroupBuy(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long purchaseId,
        @Valid @RequestBody UpdateGroupBuyRequest request
    ) {
        // 이메일로 사용자 조회
        User user = userRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        GroupBuyResponse response = groupBuyService.updateGroupBuy(user.getId(), purchaseId, request);
        return ApiResponse.success(response);
    }

    /**
     * 공구 삭제 (소프트 삭제)
     * @param userDetails 인증된 사용자
     * @param purchaseId 공구 ID
     */
    @DeleteMapping("/{purchaseId}")
    public ApiResponse<Void> deleteGroupBuy(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long purchaseId
    ) {
        // 이메일로 사용자 조회
        User user = userRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        groupBuyService.deleteGroupBuy(user.getId(), purchaseId);
        return ApiResponse.success(null);
    }

    /**
     * 공구 참여
     * @param userDetails 인증된 사용자
     * @param purchaseId 공구 ID
     * @param request 참여 요청 정보 (수령 방법, 수량)
     */
    @PostMapping("/{purchaseId}/participate")
    public ApiResponse<Void> participate(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long purchaseId,
        @Valid @RequestBody ParticipateRequest request
    ) {
        // 이메일로 사용자 조회
        User user = userRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        participationService.participate(user.getId(), purchaseId, request);
        return ApiResponse.success(null);
    }

    /**
     * 공구 참여 취소
     * @param userDetails 인증된 사용자
     * @param purchaseId 공구 ID
     */
    @DeleteMapping("/{purchaseId}/participate")
    public ApiResponse<Void> cancelParticipation(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long purchaseId
    ) {
        // 이메일로 사용자 조회
        User user = userRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        participationService.cancelParticipation(user.getId(), purchaseId);
        return ApiResponse.success(null);
    }

    /**
     * 공구 참여자 목록 조회
     * @param userDetails 인증된 사용자
     * @param purchaseId 공구 ID
     */
    @GetMapping("/{purchaseId}/participants")
    public ApiResponse<List<ParticipantResponse>> getParticipants(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long purchaseId
    ) {
        // 이메일로 사용자 조회
        User user = userRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        List<ParticipantResponse> participants = participationService.getParticipants(purchaseId, user.getId());
        return ApiResponse.success(participants);
    }
}
