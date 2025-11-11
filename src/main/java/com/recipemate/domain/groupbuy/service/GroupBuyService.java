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
import com.recipemate.global.event.GroupBuyCreatedEvent;
import com.recipemate.global.exception.CustomException;
import com.recipemate.global.exception.ErrorCode;
import com.recipemate.global.util.ImageUploadUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupBuyService {

    private final GroupBuyRepository groupBuyRepository;
    private final GroupBuyImageRepository groupBuyImageRepository;
    private final UserRepository userRepository;
    private final ImageUploadUtil imageUploadUtil;
    private final com.recipemate.domain.recipe.service.RecipeService recipeService;
    private final ApplicationEventPublisher eventPublisher;
    private final com.recipemate.domain.groupbuy.repository.ParticipationRepository participationRepository;
    private final com.recipemate.domain.review.repository.ReviewRepository reviewRepository;

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
        // Null-safe: isParticipantListPublic defaults to false if null
        boolean isParticipantListPublic = Boolean.TRUE.equals(request.getIsParticipantListPublic());
        
        GroupBuy groupBuy;
        if (request.getRecipeApiId() != null) {
            groupBuy = GroupBuy.createRecipeBased(
                host,
                request.getTitle(),
                request.getContent(),
                request.getIngredients(), // 재료 목록 별도 전달
                request.getCategory(),
                request.getTotalPrice(),
                request.getTargetHeadcount(),
                request.getDeadline(),
                request.getDeliveryMethod(),
                request.getMeetupLocation(),
                request.getParcelFee(),
                isParticipantListPublic,
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
                isParticipantListPublic
            );
        }
        
        // 마감일을 기준으로 초기 상태 결정 및 설정
        GroupBuyStatus initialStatus = determineStatus(request.getDeadline());
        groupBuy.updateStatus(initialStatus);

        // 4. GroupBuy 저장
        GroupBuy savedGroupBuy = groupBuyRepository.save(groupBuy);

        // 5. 이미지 엔티티 생성 및 저장
        saveGroupBuyImages(savedGroupBuy, imageUrls);

        // 6. 공구 생성 관련 이벤트 발행 (뱃지, 포인트 등)
        eventPublisher.publishEvent(new GroupBuyCreatedEvent(userId));

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
     * 선택된 재료만 content에 추가하고, recipeName과 recipeImageUrl이 없으면 레시피 API에서 가져와서 채웁니다.
     */
    private CreateGroupBuyRequest enrichWithRecipeInfo(CreateGroupBuyRequest request) {
        // 이미 모든 레시피 정보가 제공된 경우 API 호출 생략
        boolean hasRecipeInfo = request.getRecipeName() != null && request.getRecipeImageUrl() != null;
        
        String recipeName = request.getRecipeName();
        String recipeImageUrl = request.getRecipeImageUrl();
        
        // 레시피 정보가 없으면 API에서 조회
        if (!hasRecipeInfo) {
            com.recipemate.domain.recipe.dto.RecipeDetailResponse recipeDetail;
            try {
                recipeDetail = recipeService.getRecipeDetail(request.getRecipeApiId());
                recipeName = recipeDetail.getName();
                recipeImageUrl = recipeDetail.getImageUrl();
            } catch (CustomException e) {
                // 레시피를 찾을 수 없는 경우 예외 전파
                if (e.getErrorCode() == ErrorCode.RECIPE_NOT_FOUND) {
                    throw e;
                }
                // 기타 오류는 일반적인 레시피 조회 오류로 처리
                throw new CustomException(ErrorCode.RECIPE_NOT_FOUND);
            }
        }
        
        // 선택된 재료만 필터링하여 문자열로 변환
        String ingredientsText = buildSelectedIngredientsText(request.getSelectedIngredients());
        
        // 새로운 요청 객체 생성 (레시피 정보 보강)
        // 재료 정보는 content에 추가하지 않고 별도 필드로 관리
        return CreateGroupBuyRequest.builder()
            .title(request.getTitle())
            .content(request.getContent())
            .ingredients(ingredientsText) // 재료는 별도 필드로 저장
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
            .recipeName(recipeName)
            .recipeImageUrl(recipeImageUrl)
            .selectedIngredients(request.getSelectedIngredients())
            .build();
    }

    /**
     * 선택된 재료 목록을 문자열로 변환
     */
    private String buildSelectedIngredientsText(List<com.recipemate.domain.groupbuy.dto.SelectedIngredient> selectedIngredients) {
        if (selectedIngredients == null || selectedIngredients.isEmpty()) {
            return "";
        }
        
        // 선택된 재료만 필터링
        List<com.recipemate.domain.groupbuy.dto.SelectedIngredient> filteredIngredients = selectedIngredients.stream()
            .filter(ingredient -> Boolean.TRUE.equals(ingredient.getSelected()))
            .toList();
        
        if (filteredIngredients.isEmpty()) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder("필요한 재료:\n");
        for (com.recipemate.domain.groupbuy.dto.SelectedIngredient ingredient : filteredIngredients) {
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
     * 공구 상세 조회 (사용자 상태 정보 포함)
     * @param purchaseId 공구 ID
     * @param userId 사용자 ID (로그인한 경우)
     */
    public GroupBuyResponse getGroupBuyDetail(Long purchaseId, Long userId) {
        // GroupBuy와 Host를 Fetch Join으로 조회
        GroupBuy groupBuy = groupBuyRepository.findByIdWithHost(purchaseId)
            .orElseThrow(() -> new CustomException(ErrorCode.GROUP_BUY_NOT_FOUND));
        
        // 이미지 목록을 별도 쿼리로 조회 (displayOrder로 정렬)
        List<String> imageUrls = groupBuyImageRepository.findByGroupBuyOrderByDisplayOrderAsc(groupBuy)
            .stream()
            .map(GroupBuyImage::getImageUrl)
            .toList();
        
        // 사용자가 로그인한 경우에만 상태 정보 계산
        if (userId != null) {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
            
            boolean isHost = groupBuy.isHost(user);
            boolean isParticipant = participationRepository.existsByUserIdAndGroupBuyId(userId, purchaseId);
            boolean isCancellable = LocalDateTime.now().isBefore(groupBuy.getDeadline().minusDays(1));
            
            return mapToResponseWithUserStatus(groupBuy, imageUrls, isHost, isParticipant, isCancellable);
        }
        
        return mapToResponse(groupBuy, imageUrls);
    }

    /**
     * 공구 수정
     */
    @Transactional
    public GroupBuyResponse updateGroupBuy(Long userId, Long groupBuyId, UpdateGroupBuyRequest request, List<String> deletedImages) {
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
        
        // 3-1. 상태 검증 (마감 또는 마감 임박 시 수정 불가)
        if (groupBuy.getStatus() == GroupBuyStatus.CLOSED || groupBuy.getStatus() == GroupBuyStatus.IMMINENT) {
            throw new CustomException(ErrorCode.CANNOT_MODIFY_GROUP_BUY);
        }
        
        // 4. 이미지 처리
        // 4-1. 기존 이미지 목록 조회
        List<GroupBuyImage> currentImages = groupBuyImageRepository.findByGroupBuyOrderByDisplayOrderAsc(groupBuy);
        List<String> currentImageUrls = currentImages.stream()
            .map(GroupBuyImage::getImageUrl)
            .toList();
        
        // 4-2. 삭제할 이미지 처리
        if (deletedImages != null && !deletedImages.isEmpty()) {
            // Cloudinary에서 삭제
            imageUploadUtil.deleteImages(deletedImages);
            
            // DB에서 삭제
            List<GroupBuyImage> imagesToDelete = currentImages.stream()
                .filter(img -> deletedImages.contains(img.getImageUrl()))
                .toList();
            groupBuyImageRepository.deleteAll(imagesToDelete);
            groupBuyImageRepository.flush(); // 즉시 DB에 반영
            
            log.info("Deleted {} images from group buy {}", deletedImages.size(), groupBuyId);
        }
        
        // 4-3. 남은 기존 이미지들의 display_order 재정렬
        List<GroupBuyImage> remainingImages = groupBuyImageRepository.findByGroupBuyOrderByDisplayOrderAsc(groupBuy);
        for (int i = 0; i < remainingImages.size(); i++) {
            GroupBuyImage image = remainingImages.get(i);
            if (image.getDisplayOrder() != i) {
                image.updateDisplayOrder(i);
            }
        }
        
        // 4-4. 새 이미지 업로드 전 총 개수 검증
        List<String> newImageUrls = new ArrayList<>();
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            // 빈 파일이 아닌 것만 필터링
            List<org.springframework.web.multipart.MultipartFile> validFiles = request.getImages().stream()
                .filter(file -> !file.isEmpty())
                .toList();
            
            // 총 이미지 개수 검증 (기존 남은 이미지 + 새 이미지 <= 3)
            int remainingImageCount = remainingImages.size();
            if (remainingImageCount + validFiles.size() > 3) {
                throw new CustomException(ErrorCode.IMAGE_COUNT_EXCEEDED);
            }
            
            if (!validFiles.isEmpty()) {
                newImageUrls = imageUploadUtil.uploadImages(validFiles);
                log.info("Uploaded {} new images for group buy {}", newImageUrls.size(), groupBuyId);
            }
        }
        
        // 4-5. 새 이미지를 DB에 저장
        if (!newImageUrls.isEmpty()) {
            // 재정렬된 기존 이미지 개수를 시작 인덱스로 사용
            int startOrder = remainingImages.size();
            
            for (int i = 0; i < newImageUrls.size(); i++) {
                GroupBuyImage newImage = GroupBuyImage.builder()
                    .groupBuy(groupBuy)
                    .imageUrl(newImageUrls.get(i))
                    .displayOrder(startOrder + i)
                    .build();
                groupBuyImageRepository.save(newImage);
            }
        }
        
        // 5. 공구 정보 수정
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
        
        // 마감일을 기준으로 상태 재계산 및 업데이트
        GroupBuyStatus updatedStatus = determineStatus(groupBuy.getDeadline());
        groupBuy.updateStatus(updatedStatus);
        
        // 6. 최종 이미지 목록 조회
        List<String> finalImageUrls = groupBuyImageRepository.findByGroupBuyOrderByDisplayOrderAsc(groupBuy)
            .stream()
            .map(GroupBuyImage::getImageUrl)
            .toList();
        
        // 7. 응답 DTO 생성
        return mapToResponse(groupBuy, finalImageUrls);
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
        
        // 5. 연관된 이미지 처리
        List<GroupBuyImage> images = groupBuyImageRepository.findByGroupBuyOrderByDisplayOrderAsc(groupBuy);
        if (!images.isEmpty()) {
            // 5-1. Cloudinary에서 이미지 삭제
            List<String> imageUrls = images.stream()
                .map(GroupBuyImage::getImageUrl)
                .toList();
            imageUploadUtil.deleteImages(imageUrls);
            log.info("Deleted {} images from Cloudinary for group buy {}", imageUrls.size(), groupBuyId);
            
            // 5-2. DB에서 이미지 소프트 삭제
            images.forEach(GroupBuyImage::delete);
            log.info("Soft deleted {} images in DB for group buy {}", images.size(), groupBuyId);
        }
        
        // 6. 공구 소프트 삭제
        groupBuy.delete();
        log.info("Soft deleted group buy {}", groupBuyId);
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
        // 후기 정보 조회
        Double averageRating = reviewRepository.findAverageRatingByGroupBuyId(groupBuy.getId());
        long reviewCount = reviewRepository.countByGroupBuyId(groupBuy.getId());
        
        return GroupBuyResponse.builder()
            .id(groupBuy.getId())
            .title(groupBuy.getTitle())
            .content(groupBuy.getContent())
            .ingredients(groupBuy.getIngredients())
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
            .averageRating(averageRating)
            .reviewCount((int) reviewCount)
            .createdAt(groupBuy.getCreatedAt())
            .updatedAt(groupBuy.getUpdatedAt())
            .build();
    }

    /**
     * Entity를 Response DTO로 변환 (사용자 상태 정보 포함)
     */
    private GroupBuyResponse mapToResponseWithUserStatus(GroupBuy groupBuy, List<String> imageUrls, 
                                                          boolean isHost, boolean isParticipant, boolean isCancellable) {
        // 후기 정보 조회
        Double averageRating = reviewRepository.findAverageRatingByGroupBuyId(groupBuy.getId());
        long reviewCount = reviewRepository.countByGroupBuyId(groupBuy.getId());
        
        return GroupBuyResponse.builder()
            .id(groupBuy.getId())
            .title(groupBuy.getTitle())
            .content(groupBuy.getContent())
            .ingredients(groupBuy.getIngredients())
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
            .isHost(isHost)
            .isParticipant(isParticipant)
            .isCancellable(isCancellable)
            .averageRating(averageRating)
            .reviewCount((int) reviewCount)
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
        if (request.getCategory() == null) {
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
     * 마감일을 기준으로 적절한 GroupBuyStatus를 결정
     * 마감일이 현재로부터 2일 이내면 IMMINENT, 그렇지 않으면 RECRUITING 반환
     */
    private GroupBuyStatus determineStatus(LocalDateTime deadline) {
        if (deadline.isBefore(LocalDateTime.now().plusDays(2))) {
            return GroupBuyStatus.IMMINENT;
        }
        return GroupBuyStatus.RECRUITING;
    }
}
