package com.recipemate.domain.groupbuy.service;

import com.recipemate.domain.groupbuy.dto.CreateGroupBuyRequest;
import com.recipemate.domain.groupbuy.dto.GroupBuyResponse;
import com.recipemate.domain.groupbuy.dto.GroupBuySearchCondition;
import com.recipemate.domain.groupbuy.entity.GroupBuy;
import com.recipemate.domain.groupbuy.entity.GroupBuyImage;
import com.recipemate.domain.groupbuy.repository.GroupBuyImageRepository;
import com.recipemate.domain.groupbuy.repository.GroupBuyRepository;
import com.recipemate.domain.user.entity.User;
import com.recipemate.domain.user.repository.UserRepository;
import com.recipemate.global.exception.CustomException;
import com.recipemate.global.exception.ErrorCode;
import com.recipemate.global.util.ImageUploadUtil;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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

        // 3. GroupBuy 엔티티 생성
        GroupBuy groupBuy = GroupBuy.createGeneral(
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

        // 4. GroupBuy 저장
        GroupBuy savedGroupBuy = groupBuyRepository.save(groupBuy);

        // 5. 이미지 엔티티 생성 및 저장
        saveGroupBuyImages(savedGroupBuy, imageUrls);

        // 6. 응답 DTO 생성
        return mapToResponse(savedGroupBuy, imageUrls);
    }

    /**
     * 공구 목록 조회 (검색 및 필터링 지원)
     */
    public Page<GroupBuyResponse> getGroupBuyList(GroupBuySearchCondition condition, Pageable pageable) {
        Specification<GroupBuy> spec = createSpecification(condition);
        Page<GroupBuy> groupBuys = groupBuyRepository.findAll(spec, pageable);
        
        return groupBuys.map(groupBuy -> {
            List<String> imageUrls = groupBuyImageRepository.findByGroupBuyOrderByDisplayOrderAsc(groupBuy)
                .stream()
                .map(GroupBuyImage::getImageUrl)
                .toList();
            return mapToResponse(groupBuy, imageUrls);
        });
    }

    /**
     * 검색 조건으로 Specification 생성
     */
    private Specification<GroupBuy> createSpecification(GroupBuySearchCondition condition) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 카테고리 필터
            if (condition != null && condition.getCategory() != null && !condition.getCategory().isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("category"), condition.getCategory()));
            }

            // 상태 필터
            if (condition != null && condition.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), condition.getStatus()));
            }

            // 레시피 기반 공구만 필터
            if (condition != null && condition.getRecipeOnly() != null && condition.getRecipeOnly()) {
                predicates.add(criteriaBuilder.isNotNull(root.get("recipeApiId")));
            }

            // 키워드 검색 (제목 또는 내용)
            if (condition != null && condition.getKeyword() != null && !condition.getKeyword().isBlank()) {
                String keywordPattern = "%" + condition.getKeyword() + "%";
                Predicate titlePredicate = criteriaBuilder.like(root.get("title"), keywordPattern);
                Predicate contentPredicate = criteriaBuilder.like(root.get("content"), keywordPattern);
                predicates.add(criteriaBuilder.or(titlePredicate, contentPredicate));
            }

            // 정렬: 최신순
            query.orderBy(criteriaBuilder.desc(root.get("createdAt")));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
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
            throw new IllegalArgumentException("제목은 필수입니다");
        }
        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new IllegalArgumentException("내용은 필수입니다");
        }
        if (request.getCategory() == null || request.getCategory().isBlank()) {
            throw new IllegalArgumentException("카테고리는 필수입니다");
        }
        if (request.getTotalPrice() == null) {
            throw new IllegalArgumentException("총 금액은 필수입니다");
        }
        if (request.getTargetHeadcount() == null) {
            throw new IllegalArgumentException("목표 인원은 필수입니다");
        }
        if (request.getDeadline() == null) {
            throw new IllegalArgumentException("마감일은 필수입니다");
        }
        if (request.getDeliveryMethod() == null) {
            throw new IllegalArgumentException("수령 방법은 필수입니다");
        }
    }
}
