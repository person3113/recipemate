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
import com.recipemate.global.common.GroupBuyStatus;
import com.recipemate.global.config.CacheConfig;
import com.recipemate.global.exception.CustomException;
import com.recipemate.global.exception.ErrorCode;
import com.recipemate.global.util.ImageUploadUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
    private final com.recipemate.domain.recipe.service.RecipeService recipeService;
    private final com.recipemate.domain.badge.service.BadgeService badgeService;

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

        // 6. 첫 공구 생성 시 배지 수여
        checkAndAwardFirstGroupBuyBadge(userId);

        // 7. 응답 DTO 생성
        return mapToResponse(savedGroupBuy, imageUrls);
    }

    /**
     * 레시피 기반 공구 생성
     * recipeApiId를 받아 레시피 정보를 자동으로 가져와 공구를 생성합니다.
     */
    @Transactional
    public GroupBuyResponse createRecipeBasedGroupBuy(Long userId, CreateGroupBuyRequest request) {
        // 0. 레시피 필드 검증
        if (request.getRecipeApiId() == null || request.getRecipeApiId().isBlank()) {
            throw new CustomException(ErrorCode.INVALID_RECIPE_API_ID);
        }
        
        // 1. 레시피 정보 자동 채우기 (수동 제공된 경우 건너뛰기)
        CreateGroupBuyRequest enrichedRequest = enrichWithRecipeInfo(request);
        
        // 2. 일반 생성 메서드 사용 (내부에서 레시피 필드를 감지하여 처리)
        return createGroupBuy(userId, enrichedRequest);
    }

    /**
     * 레시피 정보로 요청 데이터 보강
     * recipeName이나 recipeImageUrl이 없으면 레시피 API에서 가져와서 채웁니다.
     */
    private CreateGroupBuyRequest enrichWithRecipeInfo(CreateGroupBuyRequest request) {
        // 이미 모든 레시피 정보가 제공된 경우 API 호출 생략
        if (request.getRecipeName() != null && request.getRecipeImageUrl() != null) {
            return request;
        }
        
        // 레시피 상세 정보 조회
        com.recipemate.domain.recipe.dto.RecipeDetailResponse recipeDetail;
        try {
            recipeDetail = recipeService.getRecipeDetail(request.getRecipeApiId());
        } catch (CustomException e) {
            // 레시피를 찾을 수 없는 경우 예외 전파
            if (e.getErrorCode() == ErrorCode.RECIPE_NOT_FOUND) {
                throw e;
            }
            // 기타 오류는 일반적인 레시피 조회 오류로 처리
            throw new CustomException(ErrorCode.RECIPE_NOT_FOUND);
        }
        
        // 재료 목록을 문자열로 변환
        String ingredientsText = buildIngredientsText(recipeDetail);
        
        // 기존 content에 재료 정보 추가
        String enrichedContent = request.getContent() + "\n\n" + ingredientsText;
        
        // 새로운 요청 객체 생성 (레시피 정보 보강)
        return CreateGroupBuyRequest.builder()
            .title(request.getTitle())
            .content(enrichedContent)
            .category(request.getCategory())
            .totalPrice(request.getTotalPrice())
            .targetHeadcount(request.getTargetHeadcount())
            .deadline(request.getDeadline())
            .deliveryMethod(request.getDeliveryMethod())
            .meetupLocation(request.getMeetupLocation())
            .parcelFee(request.getParcelFee())
            .isParticipantListPublic(request.getIsParticipantListPublic())
            .imageFiles(request.getImageFiles())
            .recipeApiId(request.getRecipeApiId())
            .recipeName(request.getRecipeName() != null ? request.getRecipeName() : recipeDetail.getName())
            .recipeImageUrl(request.getRecipeImageUrl() != null ? request.getRecipeImageUrl() : recipeDetail.getImageUrl())
            .build();
    }

    /**
     * 재료 목록을 문자열로 변환
     */
    private String buildIngredientsText(com.recipemate.domain.recipe.dto.RecipeDetailResponse recipeDetail) {
        if (recipeDetail.getIngredients() == null || recipeDetail.getIngredients().isEmpty()) {
            return "필요한 재료: 정보 없음";
        }
        
        StringBuilder sb = new StringBuilder("필요한 재료:\n");
        for (com.recipemate.domain.recipe.dto.RecipeDetailResponse.IngredientInfo ingredient : recipeDetail.getIngredients()) {
            sb.append("- ").append(ingredient.getName());
            if (ingredient.getMeasure() != null && !ingredient.getMeasure().isBlank()) {
                sb.append(" (").append(ingredient.getMeasure()).append(")");
            }
            sb.append("\n");
        }
        
        return sb.toString().trim();
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
        
        // N+1 문제 해결: 모든 공구의 이미지를 한 번에 조회
        List<Long> groupBuyIds = groupBuys.getContent().stream()
                .map(GroupBuy::getId)
                .toList();
        
        List<GroupBuyImage> allImages = groupBuyImageRepository.findByGroupBuyIdInOrderByGroupBuyIdAndDisplayOrder(groupBuyIds);
        
        // GroupBuy ID별로 이미지를 그룹화
        java.util.Map<Long, List<String>> imageMap = allImages.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    img -> img.getGroupBuy().getId(),
                    java.util.stream.Collectors.mapping(GroupBuyImage::getImageUrl, java.util.stream.Collectors.toList())
                ));
        
        return groupBuys.map(groupBuy -> {
            List<String> imageUrls = imageMap.getOrDefault(groupBuy.getId(), List.of());
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
     * 인기 공구 목록 조회 (참여자 수 기준)
     * RECRUITING, IMMINENT 상태의 공구만 조회하며, 5분간 캐싱됩니다.
     * 
     * @param limit 조회할 공구 개수
     * @return 인기 공구 목록
     */
    @Cacheable(value = CacheConfig.POPULAR_GROUP_BUYS_CACHE, key = "'popular:' + #limit")
    public List<GroupBuyResponse> getPopularGroupBuys(int limit) {
        // RECRUITING, IMMINENT 상태의 공구 조회
        List<GroupBuyStatus> activeStatuses = List.of(GroupBuyStatus.RECRUITING, GroupBuyStatus.IMMINENT);
        
        // 참여자 수 기준 내림차순 정렬하여 조회
        Pageable pageable = PageRequest.of(0, limit);
        List<GroupBuy> popularGroupBuys = groupBuyRepository.findPopularGroupBuys(activeStatuses, pageable);
        
        // N+1 문제 해결: 모든 공구의 이미지를 한 번에 조회
        List<Long> groupBuyIds = popularGroupBuys.stream()
                .map(GroupBuy::getId)
                .toList();
        
        List<GroupBuyImage> allImages = groupBuyImageRepository.findByGroupBuyIdInOrderByGroupBuyIdAndDisplayOrder(groupBuyIds);
        
        // GroupBuy ID별로 이미지를 그룹화
        java.util.Map<Long, List<String>> imageMap = allImages.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    img -> img.getGroupBuy().getId(),
                    java.util.stream.Collectors.mapping(GroupBuyImage::getImageUrl, java.util.stream.Collectors.toList())
                ));
        
        // Response DTO로 변환
        return popularGroupBuys.stream()
                .map(groupBuy -> {
                    List<String> imageUrls = imageMap.getOrDefault(groupBuy.getId(), List.of());
                    return mapToResponse(groupBuy, imageUrls);
                })
                .toList();
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

    /**
     * 첫 공구 생성 배지 확인 및 수여
     */
    private void checkAndAwardFirstGroupBuyBadge(Long userId) {
        long groupBuyCount = groupBuyRepository.countByHostIdAndStatus(userId, GroupBuyStatus.RECRUITING);
        if (groupBuyCount == 1) {
            badgeService.checkAndAwardBadge(userId, com.recipemate.global.common.BadgeType.FIRST_GROUP_BUY);
        }
    }
}
