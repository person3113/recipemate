//package com.recipemate.domain.groupbuy.service;
//
//import com.recipemate.domain.groupbuy.dto.CreateGroupBuyRequest;
//import com.recipemate.domain.groupbuy.dto.GroupBuyResponse;
//import com.recipemate.domain.groupbuy.entity.GroupBuy;
//import com.recipemate.domain.groupbuy.repository.GroupBuyImageRepository;
//import com.recipemate.domain.groupbuy.repository.GroupBuyRepository;
//import com.recipemate.domain.user.entity.User;
//import com.recipemate.domain.user.repository.UserRepository;
//import com.recipemate.global.common.DeliveryMethod;
//import com.recipemate.global.util.ImageUploadUtil;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.BDDMockito.given;
//
//@ExtendWith(MockitoExtension.class)
//@DisplayName("GroupBuyService - Null Safety Tests")
//class GroupBuyServiceNullSafetyTest {
//
//    @InjectMocks
//    private GroupBuyService groupBuyService;
//
//    @Mock
//    private GroupBuyRepository groupBuyRepository;
//
//    @Mock
//    private UserRepository userRepository;
//
//    @Mock
//    private GroupBuyImageRepository groupBuyImageRepository;
//
//    @Mock
//    private ImageUploadUtil imageUploadUtil;
//
//    @Mock
//    private com.recipemate.domain.recipe.service.RecipeService recipeService;
//
//    @Mock
//    private com.recipemate.domain.badge.service.BadgeService badgeService;
//
//    @Mock
//    private com.recipemate.domain.user.service.PointService pointService;
//
//    @Mock
//    private org.springframework.context.ApplicationEventPublisher eventPublisher;
//
//    @Test
//    @DisplayName("isParticipantListPublic이 null이면 false로 처리된다")
//    void createGroupBuy_WithNullIsParticipantListPublic_ShouldDefaultToFalse() {
//        // given
//        Long userId = 1L;
//        User user = User.builder()
//            .email("test@test.com")
//            .password("password")
//            .nickname("tester")
//            .phoneNumber("010-1234-5678")
//            .build();
//
//        CreateGroupBuyRequest request = CreateGroupBuyRequest.builder()
//            .title("테스트 공구")
//            .content("테스트 내용")
//            .category("육류")
//            .totalPrice(50000)
//            .targetHeadcount(5)
//            .deadline(LocalDateTime.now().plusDays(7))
//            .deliveryMethod(DeliveryMethod.BOTH)
//            .meetupLocation("서울역")
//            .parcelFee(3000)
//            .isParticipantListPublic(null) // null 값
//            .imageFiles(new ArrayList<>())
//            .build();
//
//        GroupBuy savedGroupBuy = GroupBuy.createGeneral(
//            user,
//            request.getTitle(),
//            request.getContent(),
//            request.getCategory(),
//            request.getTotalPrice(),
//            request.getTargetHeadcount(),
//            request.getDeadline(),
//            request.getDeliveryMethod(),
//            request.getMeetupLocation(),
//            request.getParcelFee(),
//            false // Expected: false (not null)
//        );
//
//        given(userRepository.findById(userId)).willReturn(Optional.of(user));
//        given(groupBuyRepository.save(any(GroupBuy.class))).willReturn(savedGroupBuy);
//        given(imageUploadUtil.uploadImages(any())).willReturn(new ArrayList<>());
//
//        // when
//        GroupBuyResponse response = groupBuyService.createGroupBuy(userId, request);
//
//        // then
//        assertThat(response).isNotNull();
//        assertThat(response.getIsParticipantListPublic()).isFalse(); // null이 아닌 false여야 함
//    }
//
//    @Test
//    @DisplayName("isParticipantListPublic이 true이면 true로 저장된다")
//    void createGroupBuy_WithTrueIsParticipantListPublic_ShouldStoreTrue() {
//        // given
//        Long userId = 1L;
//        User user = User.builder()
//            .email("test@test.com")
//            .password("password")
//            .nickname("tester")
//            .phoneNumber("010-1234-5678")
//            .build();
//
//        CreateGroupBuyRequest request = CreateGroupBuyRequest.builder()
//            .title("테스트 공구")
//            .content("테스트 내용")
//            .category("육류")
//            .totalPrice(50000)
//            .targetHeadcount(5)
//            .deadline(LocalDateTime.now().plusDays(7))
//            .deliveryMethod(DeliveryMethod.BOTH)
//            .meetupLocation("서울역")
//            .parcelFee(3000)
//            .isParticipantListPublic(true) // true 값
//            .imageFiles(new ArrayList<>())
//            .build();
//
//        GroupBuy savedGroupBuy = GroupBuy.createGeneral(
//            user,
//            request.getTitle(),
//            request.getContent(),
//            request.getCategory(),
//            request.getTotalPrice(),
//            request.getTargetHeadcount(),
//            request.getDeadline(),
//            request.getDeliveryMethod(),
//            request.getMeetupLocation(),
//            request.getParcelFee(),
//            true
//        );
//
//        given(userRepository.findById(userId)).willReturn(Optional.of(user));
//        given(groupBuyRepository.save(any(GroupBuy.class))).willReturn(savedGroupBuy);
//        given(imageUploadUtil.uploadImages(any())).willReturn(new ArrayList<>());
//
//        // when
//        GroupBuyResponse response = groupBuyService.createGroupBuy(userId, request);
//
//        // then
//        assertThat(response).isNotNull();
//        assertThat(response.getIsParticipantListPublic()).isTrue();
//    }
//}
