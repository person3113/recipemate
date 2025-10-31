package com.recipemate.domain.user.service;

import com.recipemate.domain.groupbuy.dto.GroupBuyResponse;
import com.recipemate.domain.groupbuy.entity.GroupBuy;
import com.recipemate.domain.groupbuy.entity.GroupBuyImage;
import com.recipemate.domain.groupbuy.entity.Participation;
import com.recipemate.domain.groupbuy.repository.GroupBuyImageRepository;
import com.recipemate.domain.groupbuy.repository.GroupBuyRepository;
import com.recipemate.domain.groupbuy.repository.ParticipationRepository;
import com.recipemate.global.common.GroupBuyStatus;
import com.recipemate.global.exception.CustomException;
import com.recipemate.global.exception.ErrorCode;
import com.recipemate.domain.user.dto.ChangePasswordRequest;
import com.recipemate.domain.user.dto.SignupRequest;
import com.recipemate.domain.user.dto.UpdateProfileRequest;
import com.recipemate.domain.user.dto.UserResponse;
import com.recipemate.domain.user.entity.User;
import com.recipemate.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final GroupBuyRepository groupBuyRepository;
    private final GroupBuyImageRepository groupBuyImageRepository;
    private final ParticipationRepository participationRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }

        if (userRepository.existsByNickname(request.getNickname())) {
            throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = User.create(
                request.getEmail(),
                encodedPassword,
                request.getNickname(),
                request.getPhoneNumber()
        );

        User savedUser = userRepository.save(user);

        return UserResponse.from(savedUser);
    }

    public UserResponse getMyProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (request.getNickname() != null && !request.getNickname().equals(user.getNickname())) {
            if (userRepository.existsByNickname(request.getNickname())) {
                throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
            }
        }

        user.updateProfile(request.getNickname(), request.getPhoneNumber(), request.getProfileImageUrl());

        return UserResponse.from(user);
    }

    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        String encodedNewPassword = passwordEncoder.encode(request.getNewPassword());
        user.changePassword(encodedNewPassword);
    }

    public Page<GroupBuyResponse> getMyGroupBuys(Long userId, GroupBuyStatus status, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Page<GroupBuy> groupBuys;
        
        if (status == null) {
            // 모든 상태의 공구 조회
            List<GroupBuyStatus> allStatuses = Arrays.asList(
                GroupBuyStatus.RECRUITING,
                GroupBuyStatus.IMMINENT,
                GroupBuyStatus.CLOSED
            );
            groupBuys = groupBuyRepository.findByHostIdAndStatusIn(userId, allStatuses, pageable);
        } else {
            // 특정 상태의 공구만 조회
            groupBuys = groupBuyRepository.findByHostIdAndStatusIn(userId, List.of(status), pageable);
        }

        return groupBuys.map(groupBuy -> {
            List<String> imageUrls = groupBuyImageRepository.findByGroupBuyOrderByDisplayOrderAsc(groupBuy)
                    .stream()
                    .map(GroupBuyImage::getImageUrl)
                    .toList();
            return mapToResponse(groupBuy, imageUrls);
        });
    }

    public Page<GroupBuyResponse> getParticipatedGroupBuys(Long userId, GroupBuyStatus status, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Page<Participation> participations;
        
        if (status == null) {
            // 모든 상태의 참여 공구 조회
            List<GroupBuyStatus> allStatuses = Arrays.asList(
                GroupBuyStatus.RECRUITING,
                GroupBuyStatus.IMMINENT,
                GroupBuyStatus.CLOSED
            );
            participations = participationRepository.findByUserIdWithGroupBuyAndHost(userId, allStatuses, pageable);
        } else {
            // 특정 상태의 참여 공구만 조회
            participations = participationRepository.findByUserIdWithGroupBuyAndHost(userId, List.of(status), pageable);
        }

        return participations.map(participation -> {
            GroupBuy groupBuy = participation.getGroupBuy();
            List<String> imageUrls = groupBuyImageRepository.findByGroupBuyOrderByDisplayOrderAsc(groupBuy)
                    .stream()
                    .map(GroupBuyImage::getImageUrl)
                    .toList();
            return mapToResponse(groupBuy, imageUrls);
        });
    }

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
}
