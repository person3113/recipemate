package com.recipemate.domain.groupbuy.service;

import com.recipemate.domain.groupbuy.dto.CreateGroupBuyRequest;
import com.recipemate.domain.groupbuy.dto.GroupBuyResponse;
import com.recipemate.domain.groupbuy.dto.GroupBuySearchCondition;
import com.recipemate.domain.groupbuy.dto.UpdateGroupBuyRequest;
import com.recipemate.domain.groupbuy.entity.GroupBuy;
import com.recipemate.domain.groupbuy.entity.GroupBuyImage;
import com.recipemate.domain.groupbuy.repository.GroupBuyImageRepository;
import com.recipemate.domain.groupbuy.repository.GroupBuyRepository;
import com.recipemate.domain.user.entity.User;
import com.recipemate.domain.user.repository.UserRepository;
import com.recipemate.global.exception.CustomException;
import com.recipemate.global.exception.ErrorCode;
import com.recipemate.global.util.ImageUploadUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupBuyService {

    private final GroupBuyRepository groupBuyRepository;
    private final GroupBuyImageRepository groupBuyImageRepository;
    private final UserRepository userRepository;
    private final ImageUploadUtil imageUploadUtil;

    /**
     * 일반 공구 생성
     */
    @Transactional
    public GroupBuyResponse createGroupBuy(Long userId, CreateGroupBuyRequest request) {
        // 0. 요청 데이터 검증
        validateRequest(request);
        
        // 1. 사용자 조회
        User host = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2. 이미지 업로드 (있는 경우)
        List<String> imageUrls = imageUploadUtil.uploadImages(request.getImageFiles());

        // 3. GroupBuy 엔티티 생성 (레시피 기반 여부 확인)
        GroupBuy groupBuy;
        if (request.getRecipeApiId() != null) {
            groupBuy = GroupBuy.createRecipeBased(
                host,
                request.getTitle(),
                request.getContent(),
                request.getCategory(),
                request.getTotalPrice(),
                request.getTargetHeadcount(),
                request.getDeadline(),
                request.getDeliveryMethod(),
                request.getMeetupLocation(),
                request.getParcelFee(),
                request.getIsParticipantListPublic(),
                request.getRecipeApiId(),
                request.getRecipeName(),
                request.getRecipeImageUrl()
            );
        } else {
            groupBuy = GroupBuy.createGeneral(
                host,
                request.getTitle(),
                request.getContent(),
                request.getCategory(),
                request.getTotalPrice(),
                request.getTargetHeadcount(),
                request.getDeadline(),
                request.getDeliveryMethod(),
                request.getMeetupLocation(),
                request.getParcelFee(),
                request.getIsParticipantListPublic()
            );
        }

        // 4. GroupBuy 저장
        GroupBuy savedGroupBuy = groupBuyRepository.save(groupBuy);

        // 5. 이미지 엔티티 생성 및 저장
        saveGroupBuyImages(savedGroupBuy, imageUrls);

        // 6. 응답 DTO 생성
        return mapToResponse(savedGroupBuy, imageUrls);
    }

    /**
     * 레시피 기반 공구 생성
     */
    @Transactional
    public GroupBuyResponse createRecipeBasedGroupBuy(Long userId, CreateGroupBuyRequest request) {
        // 0. 레시피 필드 검증
        if (request.getRecipeApiId() == null || request.getRecipeApiId().isBlank()) {
            throw new CustomException(ErrorCode.INVALID_RECIPE_API_ID);
        }
        
        // 일반 생성 메서드 사용 (내부에서 레시피 필드를 감지하여 처리)
        return createGroupBuy(userId, request);
    }

    /**
     * 공구 목록 조회 (검색 및 필터링 지원 - QueryDSL 기반)
     */
    public Page<GroupBuyResponse> getGroupBuyList(GroupBuySearchCondition condition, Pageable pageable) {
        // condition이 null인 경우 빈 조건으로 초기화
        GroupBuySearchCondition searchCondition = (condition != null) 
            ? condition 
            : GroupBuySearchCondition.builder().build();
        
        // QueryDSL 기반 동적 검색 사용
        Page<GroupBuy> groupBuys = groupBuyRepository.searchGroupBuys(searchCondition, pageable);
        
        return groupBuys.map(groupBuy -> {
            List<String> imageUrls = groupBuyImageRepository.findByGroupBuyOrderByDisplayOrderAsc(groupBuy)
                .stream()
                .map(GroupBuyImage::getImageUrl)
                .toList();
            return mapToResponse(groupBuy, imageUrls);
        });
    }

    /**
     * 공구 상세 조회
     */
    public GroupBuyResponse getGroupBuyDetail(Long purchaseId) {
        // GroupBuy와 Host를 Fetch Join으로 조회
        GroupBuy groupBuy = groupBuyRepository.findByIdWithHost(purchaseId)
            .orElseThrow(() -> new CustomException(ErrorCode.GROUP_BUY_NOT_FOUND));
        
        // 이미지 목록을 별도 쿼리로 조회 (displayOrder로 정렬)
        List<String> imageUrls = groupBuyImageRepository.findByGroupBuyOrderByDisplayOrderAsc(groupBuy)
            .stream()
            .map(GroupBuyImage::getImageUrl)
            .toList();
        
        return mapToResponse(groupBuy, imageUrls);
    }

    /**
     * 공구 수정
     */
    @Transactional
    public GroupBuyResponse updateGroupBuy(Long userId, Long groupBuyId, UpdateGroupBuyRequest request) {
        // 1. 사용자 조회
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        // 2. GroupBuy 조회
        GroupBuy groupBuy = groupBuyRepository.findByIdWithHost(groupBuyId)
            .orElseThrow(() -> new CustomException(ErrorCode.GROUP_BUY_NOT_FOUND));
        
        // 3. 권한 검증 (주최자만 수정 가능)
        if (!groupBuy.isHost(user)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_GROUP_BUY_ACCESS);
        }
        
        // 4. 공구 정보 수정
        groupBuy.update(
            request.getTitle(),
            request.getContent(),
            request.getCategory(),
            request.getTotalPrice(),
            request.getTargetHeadcount(),
            request.getDeadline(),
            request.getDeliveryMethod(),
            request.getMeetupLocation(),
            request.getParcelFee(),
            request.getIsParticipantListPublic() != null ? request.getIsParticipantListPublic() : false
        );
        
        // 5. 이미지 목록 조회
        List<String> imageUrls = groupBuyImageRepository.findByGroupBuyOrderByDisplayOrderAsc(groupBuy)
            .stream()
            .map(GroupBuyImage::getImageUrl)
            .toList();
        
        // 6. 응답 DTO 생성
        return mapToResponse(groupBuy, imageUrls);
    }

    /**
     * 공구 삭제 (소프트 삭제)
     */
    @Transactional
    public void deleteGroupBuy(Long userId, Long groupBuyId) {
        // 1. 사용자 조회
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        // 2. GroupBuy 조회
        GroupBuy groupBuy = groupBuyRepository.findByIdWithHost(groupBuyId)
            .orElseThrow(() -> new CustomException(ErrorCode.GROUP_BUY_NOT_FOUND));
        
        // 3. 권한 검증 (주최자만 삭제 가능)
        if (!groupBuy.isHost(user)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_GROUP_BUY_ACCESS);
        }
        
        // 4. 참여자가 있는지 확인
        if (groupBuy.getCurrentHeadcount() > 0) {
            throw new CustomException(ErrorCode.HAS_PARTICIPANTS);
        }
        
        // 5. 소프트 삭제
        groupBuy.delete();
    }

    /**
     * GroupBuy 이미지 저장
     */
    private void saveGroupBuyImages(GroupBuy groupBuy, List<String> imageUrls) {
        if (imageUrls.isEmpty()) {
            return;
        }
        
        List<GroupBuyImage> images = new ArrayList<>();
        for (int i = 0; i < imageUrls.size(); i++) {
            GroupBuyImage image = GroupBuyImage.builder()
                .groupBuy(groupBuy)
                .imageUrl(imageUrls.get(i))
                .displayOrder(i)
                .build();
            images.add(image);
        }
        groupBuyImageRepository.saveAll(images);
    }

    /**
     * Entity를 Response DTO로 변환
     */
    private GroupBuyResponse mapToResponse(GroupBuy groupBuy, List<String> imageUrls) {
        return GroupBuyResponse.builder()
            .id(groupBuy.getId())
            .title(groupBuy.getTitle())
            .content(groupBuy.getContent())
            .category(groupBuy.getCategory())
            .totalPrice(groupBuy.getTotalPrice())
            .targetHeadcount(groupBuy.getTargetHeadcount())
            .currentHeadcount(groupBuy.getCurrentHeadcount())
            .deadline(groupBuy.getDeadline())
            .deliveryMethod(groupBuy.getDeliveryMethod())
            .meetupLocation(groupBuy.getMeetupLocation())
            .parcelFee(groupBuy.getParcelFee())
            .isParticipantListPublic(groupBuy.getParticipantListPublic())
            .status(groupBuy.getStatus())
            .hostId(groupBuy.getHost().getId())
            .hostNickname(groupBuy.getHost().getNickname())
            .hostMannerTemperature(groupBuy.getHost().getMannerTemperature())
            .recipeApiId(groupBuy.getRecipeApiId())
            .recipeName(groupBuy.getRecipeName())
            .recipeImageUrl(groupBuy.getRecipeImageUrl())
            .imageUrls(imageUrls)
            .createdAt(groupBuy.getCreatedAt())
            .updatedAt(groupBuy.getUpdatedAt())
            .build();
    }

    /**
     * 요청 데이터 검증
     */
    private void validateRequest(CreateGroupBuyRequest request) {
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new CustomException(ErrorCode.INVALID_TITLE);
        }
        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new CustomException(ErrorCode.INVALID_CONTENT);
        }
        if (request.getCategory() == null || request.getCategory().isBlank()) {
            throw new CustomException(ErrorCode.INVALID_CATEGORY);
        }
        if (request.getTotalPrice() == null) {
            throw new CustomException(ErrorCode.INVALID_TOTAL_PRICE);
        }
        if (request.getTargetHeadcount() == null) {
            throw new CustomException(ErrorCode.INVALID_TARGET_HEADCOUNT);
        }
        if (request.getDeadline() == null) {
            throw new CustomException(ErrorCode.INVALID_DEADLINE);
        }
        if (request.getDeliveryMethod() == null) {
            throw new CustomException(ErrorCode.INVALID_DELIVERY_METHOD);
        }
    }
}
