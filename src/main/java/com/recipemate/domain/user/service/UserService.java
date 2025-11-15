package com.recipemate.domain.user.service;

import com.recipemate.domain.badge.entity.Badge;
import com.recipemate.domain.badge.repository.BadgeRepository;
import com.recipemate.domain.comment.entity.Comment;
import com.recipemate.domain.comment.repository.CommentRepository;
import com.recipemate.domain.groupbuy.dto.GroupBuyResponse;
import com.recipemate.domain.groupbuy.entity.GroupBuy;
import com.recipemate.domain.groupbuy.entity.GroupBuyImage;
import com.recipemate.domain.groupbuy.entity.Participation;
import com.recipemate.domain.groupbuy.repository.GroupBuyImageRepository;
import com.recipemate.domain.groupbuy.repository.GroupBuyRepository;
import com.recipemate.domain.groupbuy.repository.ParticipationRepository;
import com.recipemate.domain.like.entity.PostLike;
import com.recipemate.domain.like.repository.PostLikeRepository;
import com.recipemate.domain.post.dto.PostWithCountsDto;
import com.recipemate.domain.post.repository.PostRepository;
import com.recipemate.global.common.BadgeType;
import com.recipemate.global.common.GroupBuyStatus;
import com.recipemate.global.exception.CustomException;
import com.recipemate.global.exception.ErrorCode;
import com.recipemate.domain.user.dto.*;
import com.recipemate.domain.user.entity.User;
import com.recipemate.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final GroupBuyRepository groupBuyRepository;
    private final GroupBuyImageRepository groupBuyImageRepository;
    private final ParticipationRepository participationRepository;
    private final PasswordEncoder passwordEncoder;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostLikeRepository postLikeRepository;
    private final BadgeRepository badgeRepository;
    private final CustomUserDetailsService customUserDetailsService;

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
            return GroupBuyResponse.from(groupBuy, imageUrls);
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

        // N+1 문제 해결: 모든 공구의 이미지를 한 번에 조회
        List<Long> groupBuyIds = participations.getContent().stream()
                .map(p -> p.getGroupBuy().getId())
                .toList();
        
        List<GroupBuyImage> allImages = groupBuyImageRepository.findByGroupBuyIdInOrderByGroupBuyIdAndDisplayOrder(groupBuyIds);
        
        // GroupBuy ID별로 이미지를 그룹화
        java.util.Map<Long, List<String>> imageMap = allImages.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    img -> img.getGroupBuy().getId(),
                    java.util.stream.Collectors.mapping(GroupBuyImage::getImageUrl, java.util.stream.Collectors.toList())
                ));

        return participations.map(participation -> {
            GroupBuy groupBuy = participation.getGroupBuy();
            List<String> imageUrls = imageMap.getOrDefault(groupBuy.getId(), List.of());
            return GroupBuyResponse.from(groupBuy, imageUrls);
        });
    }

    /**
     * 매너온도 업데이트
     * - 후기 작성 시 호출됨
     */
    @Transactional
    public void updateMannerTemperature(Long userId, double delta) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        user.updateMannerTemperature(delta);
    }

    public Page<MyPostDto> findMyPosts(User user, Pageable pageable) {
        return postRepository.findPostsByUserForMyActivity(user, pageable);
    }

    public Page<MyCommentDto> findMyComments(User user, Pageable pageable) {
        Page<Comment> comments = commentRepository.findByAuthorForMyActivity(user, pageable);
        return comments.map(comment -> new MyCommentDto(
                comment.getPost().getId(),
                comment.getPost().getTitle(),
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getDeletedAt(),
                comment.getPost().getDeletedAt(),
                comment.getParent() != null
        ));
    }

    public Page<MyLikedPostDto> findMyLikedPosts(User user, Pageable pageable) {
        Page<PostLike> likedPosts = postLikeRepository.findByUserWithPost(user, pageable);
        
        if (likedPosts.isEmpty()) {
            return Page.empty(pageable);
        }
        
        List<Long> postIds = likedPosts.getContent().stream()
                .map(pl -> pl.getPost().getId())
                .toList();
        
        List<PostWithCountsDto> postsWithCounts = postRepository.findAllWithCountsByIdIn(postIds);
        
        Map<Long, PostWithCountsDto> postMap = postsWithCounts.stream()
                .collect(Collectors.toMap(
                        dto -> dto.getPost().getId(),
                        dto -> dto
                ));
        
        return likedPosts.map(postLike -> {
            PostWithCountsDto dto = postMap.get(postLike.getPost().getId());
            if (dto != null) {
                return new MyLikedPostDto(
                        dto.getPost().getId(),
                        dto.getPost().getTitle(),
                        dto.getPost().getCreatedAt(),
                        dto.getPost().getViewCount(),
                        dto.getCommentCount(),
                        dto.getLikeCount()
                );
            } else {
                return new MyLikedPostDto(
                        postLike.getPost().getId(),
                        postLike.getPost().getTitle(),
                        postLike.getPost().getCreatedAt(),
                        postLike.getPost().getViewCount(),
                        0L,
                        0L
                );
            }
        });
    }

    public UserProfileResponseDto getUserProfile(String nickname, Pageable pageable) {
        User user = userRepository.findByNickname(nickname)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<Badge> badges = badgeRepository.findByUserIdOrderByAcquiredAtDesc(user.getId());
        List<BadgeType> badgeTypes = badges.stream()
                .map(Badge::getBadgeType)
                .collect(Collectors.toList());

        Page<GroupBuy> groupBuys = groupBuyRepository.findByHostIdAndNotDeleted(user.getId(), pageable);

        List<Long> groupBuyIds = groupBuys.getContent().stream()
                .map(GroupBuy::getId)
                .toList();
        
        List<GroupBuyImage> allImages = groupBuyImageRepository.findByGroupBuyIdInOrderByGroupBuyIdAndDisplayOrder(groupBuyIds);
        
        Map<Long, List<String>> imageMap = allImages.stream()
                .collect(Collectors.groupingBy(
                    img -> img.getGroupBuy().getId(),
                    Collectors.mapping(GroupBuyImage::getImageUrl, Collectors.toList())
                ));

        Page<GroupBuyResponse> hostedGroupBuysDto = groupBuys.map(groupBuy -> {
            List<String> imageUrls = imageMap.getOrDefault(groupBuy.getId(), List.of());
            return GroupBuyResponse.from(groupBuy, imageUrls);
        });

        return UserProfileResponseDto.of(
                user.getNickname(), 
                user.getProfileImageUrl(),
                user.getMannerTemperature(), 
                badgeTypes, 
                hostedGroupBuysDto
        );
    }

    /**
     * Security Context 갱신
     * 포인트 충전 등 사용자 정보가 변경되었을 때 세션의 인증 정보를 갱신
     */
    @Transactional(readOnly = true)
    public void refreshAuthentication(String email) {
        UserDetails updatedUserDetails = customUserDetailsService.loadUserByUsername(email);
        
        Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
        Authentication newAuth = new UsernamePasswordAuthenticationToken(
            updatedUserDetails,
            currentAuth.getCredentials(),
            updatedUserDetails.getAuthorities()
        );
        
        SecurityContextHolder.getContext().setAuthentication(newAuth);
    }
}
