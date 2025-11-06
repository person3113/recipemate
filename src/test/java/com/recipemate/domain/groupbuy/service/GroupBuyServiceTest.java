package com.recipemate.domain.groupbuy.service;

import com.recipemate.domain.groupbuy.dto.CreateGroupBuyRequest;
import com.recipemate.domain.groupbuy.dto.GroupBuyResponse;
import com.recipemate.domain.groupbuy.dto.GroupBuySearchCondition;
import com.recipemate.domain.groupbuy.dto.UpdateGroupBuyRequest;
import com.recipemate.domain.groupbuy.entity.GroupBuy;
import com.recipemate.domain.groupbuy.entity.GroupBuyImage;
import com.recipemate.domain.groupbuy.repository.GroupBuyImageRepository;
import com.recipemate.domain.groupbuy.repository.GroupBuyRepository;
import com.recipemate.domain.recipe.dto.RecipeDetailResponse;
import com.recipemate.domain.recipe.service.RecipeService;
import com.recipemate.domain.user.entity.User;
import com.recipemate.domain.user.repository.UserRepository;
import com.recipemate.global.common.DeliveryMethod;
import com.recipemate.global.common.GroupBuyStatus;
import com.recipemate.global.exception.CustomException;
import com.recipemate.global.exception.ErrorCode;
import com.recipemate.global.util.ImageUploadUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class GroupBuyServiceTest {

    @InjectMocks
    private GroupBuyService groupBuyService;

    @Mock
    private GroupBuyRepository groupBuyRepository;

    @Mock
    private GroupBuyImageRepository groupBuyImageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ImageUploadUtil imageUploadUtil;

    @Mock
    private RecipeService recipeService;

    @Mock
    private com.recipemate.domain.badge.service.BadgeService badgeService;

    @Mock
    private com.recipemate.domain.user.service.PointService pointService;

    @Mock
    private org.springframework.context.ApplicationEventPublisher eventPublisher;

    private User testUser;
    private User participantUser;
    private Long testUserId = 1L;

    @BeforeEach
    void setUp() {
        testUser = User.create(
            "test@example.com",
            "encodedPassword",
            "테스터",
            "010-1234-5678"
        );
        setUserId(testUser, testUserId);

        participantUser = User.create(
            "participant@example.com",
            "encodedPassword",
            "참여자",
            "010-8765-4321"
        );
        setUserId(participantUser, 2L);
    }

    // ========== 공구 생성 테스트 ==========

    @Test
    @DisplayName("일반 공구 생성 성공")
    void createGroupBuy_Success() {
        // given
        CreateGroupBuyRequest request = CreateGroupBuyRequest.builder()
            .title("삼겹살 공동구매")
            .content("신선한 삼겹살 공동구매합니다")
            .category("육류")
            .totalPrice(50000)
            .targetHeadcount(5)
            .deadline(LocalDateTime.now().plusDays(7))
            .deliveryMethod(DeliveryMethod.BOTH)
            .meetupLocation("강남역 2번 출구")
            .parcelFee(3000)
            .isParticipantListPublic(true)
            .imageFiles(new ArrayList<>())
            .build();

        GroupBuy savedGroupBuy = GroupBuy.createGeneral(
            testUser,
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
        setGroupBuyId(savedGroupBuy, 1L);

        given(userRepository.findById(testUserId)).willReturn(Optional.of(testUser));
        given(imageUploadUtil.uploadImages(anyList())).willReturn(new ArrayList<>());
        given(groupBuyRepository.save(any(GroupBuy.class))).willReturn(savedGroupBuy);

        // when
        GroupBuyResponse response = groupBuyService.createGroupBuy(testUserId, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getTitle()).isEqualTo("삼겹살 공동구매");
        assertThat(response.getCategory()).isEqualTo("육류");
        assertThat(response.getTotalPrice()).isEqualTo(50000);
        assertThat(response.getTargetHeadcount()).isEqualTo(5);
        assertThat(response.getCurrentHeadcount()).isEqualTo(0);
        assertThat(response.getDeliveryMethod()).isEqualTo(DeliveryMethod.BOTH);
        assertThat(response.getMeetupLocation()).isEqualTo("강남역 2번 출구");
        assertThat(response.getParcelFee()).isEqualTo(3000);
        assertThat(response.getHostNickname()).isEqualTo("테스터");
        
        verify(userRepository).findById(testUserId);
        verify(imageUploadUtil).uploadImages(anyList());
        verify(groupBuyRepository).save(any(GroupBuy.class));
    }

    @Test
    @DisplayName("필수 필드 누락 시 실패 - 제목")
    void createGroupBuy_Fail_NoTitle() {
        // given
        CreateGroupBuyRequest request = CreateGroupBuyRequest.builder()
            .title(null)
            .content("내용")
            .category("육류")
            .totalPrice(50000)
            .targetHeadcount(5)
            .deadline(LocalDateTime.now().plusDays(7))
            .deliveryMethod(DeliveryMethod.BOTH)
            .build();

        // when & then
        assertThatThrownBy(() -> groupBuyService.createGroupBuy(testUserId, request))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_TITLE);
        
        verify(userRepository, never()).save(any(User.class));
        verify(groupBuyRepository, never()).save(any(GroupBuy.class));
    }

    @Test
    @DisplayName("목표 인원이 2명 미만일 때 실패")
    void createGroupBuy_Fail_InvalidHeadcount() {
        // given
        CreateGroupBuyRequest request = CreateGroupBuyRequest.builder()
            .title("삼겹살 공동구매")
            .content("신선한 삼겹살 공동구매합니다")
            .category("육류")
            .totalPrice(50000)
            .targetHeadcount(1)
            .deadline(LocalDateTime.now().plusDays(7))
            .deliveryMethod(DeliveryMethod.BOTH)
            .imageFiles(new ArrayList<>())
            .build();

        given(userRepository.findById(testUserId)).willReturn(Optional.of(testUser));
        given(imageUploadUtil.uploadImages(anyList())).willReturn(new ArrayList<>());

        // when & then
        // GroupBuy.createGeneral()에서 targetHeadcount 검증이 일어남
        assertThatThrownBy(() -> groupBuyService.createGroupBuy(testUserId, request))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_TARGET_HEADCOUNT);
    }

    @Test
    @DisplayName("총 금액이 음수일 때 실패")
    void createGroupBuy_Fail_NegativePrice() {
        // given
        CreateGroupBuyRequest request = CreateGroupBuyRequest.builder()
            .title("삼겹살 공동구매")
            .content("신선한 삼겹살 공동구매합니다")
            .category("육류")
            .totalPrice(-1000)
            .targetHeadcount(5)
            .deadline(LocalDateTime.now().plusDays(7))
            .deliveryMethod(DeliveryMethod.BOTH)
            .build();

        given(userRepository.findById(testUserId)).willReturn(Optional.of(testUser));
        given(imageUploadUtil.uploadImages(anyList())).willReturn(new ArrayList<>());

        // when & then
        assertThatThrownBy(() -> groupBuyService.createGroupBuy(testUserId, request))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_TOTAL_PRICE);
    }

    @Test
    @DisplayName("이미지 3장 초과 시 실패")
    void createGroupBuy_Fail_TooManyImages() {
        // given
        List<MultipartFile> imageFiles = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            imageFiles.add(new MockMultipartFile(
                "image" + i,
                "test" + i + ".jpg",
                "image/jpeg",
                "test image content".getBytes()
            ));
        }

        CreateGroupBuyRequest request = CreateGroupBuyRequest.builder()
            .title("삼겹살 공동구매")
            .content("신선한 삼겹살 공동구매합니다")
            .category("육류")
            .totalPrice(50000)
            .targetHeadcount(5)
            .deadline(LocalDateTime.now().plusDays(7))
            .deliveryMethod(DeliveryMethod.BOTH)
            .imageFiles(imageFiles)
            .build();

        given(userRepository.findById(testUserId)).willReturn(Optional.of(testUser));
        given(imageUploadUtil.uploadImages(imageFiles))
            .willThrow(new IllegalArgumentException("이미지는 최대 3장까지 업로드 가능합니다"));

        // when & then
        assertThatThrownBy(() -> groupBuyService.createGroupBuy(testUserId, request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("이미지는 최대 3장까지 업로드 가능합니다");
    }

    @Test
    @DisplayName("이미지 포함하여 공구 생성 성공")
    void createGroupBuy_WithImages_Success() {
        // given
        List<MultipartFile> imageFiles = List.of(
            new MockMultipartFile("image1", "test1.jpg", "image/jpeg", "test1".getBytes()),
            new MockMultipartFile("image2", "test2.jpg", "image/jpeg", "test2".getBytes())
        );
        
        List<String> uploadedImageUrls = List.of(
            "https://example.com/image1.jpg",
            "https://example.com/image2.jpg"
        );

        CreateGroupBuyRequest request = CreateGroupBuyRequest.builder()
            .title("삼겹살 공동구매")
            .content("신선한 삼겹살 공동구매합니다")
            .category("육류")
            .totalPrice(50000)
            .targetHeadcount(5)
            .deadline(LocalDateTime.now().plusDays(7))
            .deliveryMethod(DeliveryMethod.BOTH)
            .meetupLocation("강남역 2번 출구")
            .imageFiles(imageFiles)
            .build();

        GroupBuy savedGroupBuy = GroupBuy.createGeneral(
            testUser,
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
        setGroupBuyId(savedGroupBuy, 1L);

        given(userRepository.findById(testUserId)).willReturn(Optional.of(testUser));
        given(imageUploadUtil.uploadImages(imageFiles)).willReturn(uploadedImageUrls);
        given(groupBuyRepository.save(any(GroupBuy.class))).willReturn(savedGroupBuy);

        // when
        GroupBuyResponse response = groupBuyService.createGroupBuy(testUserId, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getImageUrls()).hasSize(2);
        verify(groupBuyImageRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 공구 생성 시 실패")
    void createGroupBuy_Fail_UserNotFound() {
        // given
        CreateGroupBuyRequest request = CreateGroupBuyRequest.builder()
            .title("삼겹살 공동구매")
            .content("신선한 삼겹살 공동구매합니다")
            .category("육류")
            .totalPrice(50000)
            .targetHeadcount(5)
            .deadline(LocalDateTime.now().plusDays(7))
            .deliveryMethod(DeliveryMethod.BOTH)
            .build();

        given(userRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> groupBuyService.createGroupBuy(999L, request))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
    }

    // ========== 공구 목록 조회 테스트 ==========

    @Test
    @DisplayName("공구 목록 조회 성공 - 페이징")
    void getGroupBuyList_Success_WithPagination() {
        // given
        List<GroupBuy> groupBuyList = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            GroupBuy groupBuy = GroupBuy.createGeneral(
                testUser,
                "공구 " + i,
                "공구 내용 " + i,
                "육류",
                10000 * i,
                5,
                LocalDateTime.now().plusDays(7),
                DeliveryMethod.BOTH,
                null,
                null,
                true
            );
            setGroupBuyId(groupBuy, (long) i);
            groupBuyList.add(groupBuy);
        }

        Pageable pageable = PageRequest.of(0, 5);
        Page<GroupBuy> groupBuyPage = new PageImpl<>(groupBuyList, pageable, 10);

        given(groupBuyRepository.searchGroupBuys(any(GroupBuySearchCondition.class), eq(pageable)))
            .willReturn(groupBuyPage);
        given(groupBuyImageRepository.findByGroupBuyIdInOrderByGroupBuyIdAndDisplayOrder(anyList()))
            .willReturn(new ArrayList<>());

        // when
        Page<GroupBuyResponse> result = groupBuyService.getGroupBuyList(null, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(5);
        assertThat(result.getTotalElements()).isEqualTo(10);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.isFirst()).isTrue();
        
        verify(groupBuyRepository).searchGroupBuys(any(GroupBuySearchCondition.class), eq(pageable));
    }

    @Test
    @DisplayName("공구 목록 조회 성공 - 카테고리 필터링")
    void getGroupBuyList_Success_FilterByCategory() {
        // given
        List<GroupBuy> meatGroupBuys = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            GroupBuy groupBuy = GroupBuy.createGeneral(
                testUser,
                "육류 공구 " + i,
                "육류 공구 내용 " + i,
                "육류",
                10000,
                5,
                LocalDateTime.now().plusDays(7),
                DeliveryMethod.BOTH,
                null,
                null,
                true
            );
            setGroupBuyId(groupBuy, (long) i);
            meatGroupBuys.add(groupBuy);
        }

        Pageable pageable = PageRequest.of(0, 10);
        Page<GroupBuy> groupBuyPage = new PageImpl<>(meatGroupBuys, pageable, 3);
        GroupBuySearchCondition condition = GroupBuySearchCondition.builder().category("육류").build();

        given(groupBuyRepository.searchGroupBuys(eq(condition), eq(pageable)))
            .willReturn(groupBuyPage);
        given(groupBuyImageRepository.findByGroupBuyIdInOrderByGroupBuyIdAndDisplayOrder(anyList()))
            .willReturn(new ArrayList<>());

        // when
        Page<GroupBuyResponse> result = groupBuyService.getGroupBuyList(condition, pageable);

        // then
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent())
            .allMatch(gb -> gb.getCategory().equals("육류"));
        
        verify(groupBuyRepository).searchGroupBuys(eq(condition), eq(pageable));
    }

    @Test
    @DisplayName("공구 목록 조회 성공 - 키워드 검색")
    void getGroupBuyList_Success_SearchByKeyword() {
        // given
        GroupBuy groupBuy = GroupBuy.createGeneral(
            testUser,
            "삼겹살 공동구매",
            "신선한 고기",
            "육류",
            50000,
            5,
            LocalDateTime.now().plusDays(7),
            DeliveryMethod.BOTH,
            null,
            null,
            true
        );
        setGroupBuyId(groupBuy, 1L);

        Pageable pageable = PageRequest.of(0, 10);
        Page<GroupBuy> groupBuyPage = new PageImpl<>(List.of(groupBuy), pageable, 1);
        GroupBuySearchCondition condition = GroupBuySearchCondition.builder().keyword("삼겹살").build();

        given(groupBuyRepository.searchGroupBuys(eq(condition), eq(pageable)))
            .willReturn(groupBuyPage);
        given(groupBuyImageRepository.findByGroupBuyIdInOrderByGroupBuyIdAndDisplayOrder(anyList()))
            .willReturn(new ArrayList<>());

        // when
        Page<GroupBuyResponse> result = groupBuyService.getGroupBuyList(condition, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).contains("삼겹살");
        
        verify(groupBuyRepository).searchGroupBuys(eq(condition), eq(pageable));
    }

    @Test
    @DisplayName("공구 목록 조회 성공 - 빈 결과")
    void getGroupBuyList_Success_EmptyResult() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<GroupBuy> emptyPage = new PageImpl<>(new ArrayList<>(), pageable, 0);
        GroupBuySearchCondition condition = GroupBuySearchCondition.builder()
            .category("존재하지않는카테고리")
            .build();

        given(groupBuyRepository.searchGroupBuys(eq(condition), eq(pageable)))
            .willReturn(emptyPage);

        // when
        Page<GroupBuyResponse> result = groupBuyService.getGroupBuyList(condition, pageable);

        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    // ========== 공구 상세 조회 테스트 ==========

    @Test
    @DisplayName("공구 상세 조회 성공")
    void getGroupBuyDetail_Success() {
        // given
        GroupBuy groupBuy = GroupBuy.createGeneral(
            testUser,
            "삼겹살 공동구매",
            "신선한 삼겹살 공동구매합니다",
            "육류",
            50000,
            5,
            LocalDateTime.now().plusDays(7),
            DeliveryMethod.BOTH,
            "강남역 2번 출구",
            3000,
            true
        );
        setGroupBuyId(groupBuy, 1L);

        List<GroupBuyImage> images = List.of(
            GroupBuyImage.builder()
                .groupBuy(groupBuy)
                .imageUrl("https://example.com/image1.jpg")
                .displayOrder(0)
                .build()
        );

        given(groupBuyRepository.findByIdWithHost(1L)).willReturn(Optional.of(groupBuy));
        given(groupBuyImageRepository.findByGroupBuyOrderByDisplayOrderAsc(groupBuy))
            .willReturn(images);

        // when
        GroupBuyResponse response = groupBuyService.getGroupBuyDetail(1L);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("삼겹살 공동구매");
        assertThat(response.getContent()).isEqualTo("신선한 삼겹살 공동구매합니다");
        assertThat(response.getHostId()).isEqualTo(testUserId);
        assertThat(response.getHostNickname()).isEqualTo(testUser.getNickname());
        assertThat(response.getHostMannerTemperature()).isEqualTo(testUser.getMannerTemperature());
        assertThat(response.getImageUrls()).hasSize(1);
        
        verify(groupBuyRepository).findByIdWithHost(1L);
        verify(groupBuyImageRepository).findByGroupBuyOrderByDisplayOrderAsc(groupBuy);
    }

    @Test
    @DisplayName("공구 상세 조회 실패 - 존재하지 않는 공구")
    void getGroupBuyDetail_Fail_NotFound() {
        // given
        Long nonExistentId = 999L;
        given(groupBuyRepository.findByIdWithHost(nonExistentId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> groupBuyService.getGroupBuyDetail(nonExistentId))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.GROUP_BUY_NOT_FOUND);
    }

    // ========== 공구 수정 테스트 ==========

    @Test
    @DisplayName("공구 수정 성공 - 주최자가 수정")
    void updateGroupBuy_Success() {
        // given
        GroupBuy groupBuy = GroupBuy.createGeneral(
            testUser,
            "삼겹살 공동구매",
            "신선한 삼겹살 공동구매합니다",
            "육류",
            50000,
            5,
            LocalDateTime.now().plusDays(7),
            DeliveryMethod.BOTH,
            "강남역 2번 출구",
            3000,
            true
        );
        setGroupBuyId(groupBuy, 1L);

        UpdateGroupBuyRequest updateRequest = UpdateGroupBuyRequest.builder()
            .title("목살 공동구매")
            .content("수정된 내용입니다")
            .category("육류")
            .totalPrice(60000)
            .targetHeadcount(7)
            .deadline(LocalDateTime.now().plusDays(10))
            .deliveryMethod(DeliveryMethod.DIRECT)
            .meetupLocation("홍대입구역 3번 출구")
            .parcelFee(null)
            .isParticipantListPublic(false)
            .build();

        given(userRepository.findById(testUserId)).willReturn(Optional.of(testUser));
        given(groupBuyRepository.findByIdWithHost(1L)).willReturn(Optional.of(groupBuy));
        given(groupBuyImageRepository.findByGroupBuyOrderByDisplayOrderAsc(groupBuy))
            .willReturn(new ArrayList<>());

        // when
        GroupBuyResponse updated = groupBuyService.updateGroupBuy(testUserId, 1L, updateRequest);

        // then
        assertThat(updated).isNotNull();
        assertThat(updated.getTitle()).isEqualTo("목살 공동구매");
        assertThat(updated.getContent()).isEqualTo("수정된 내용입니다");
        assertThat(updated.getTotalPrice()).isEqualTo(60000);
        assertThat(updated.getTargetHeadcount()).isEqualTo(7);
        assertThat(updated.getDeliveryMethod()).isEqualTo(DeliveryMethod.DIRECT);
        assertThat(updated.getMeetupLocation()).isEqualTo("홍대입구역 3번 출구");
        assertThat(updated.getIsParticipantListPublic()).isFalse();
        
        verify(userRepository).findById(testUserId);
        verify(groupBuyRepository).findByIdWithHost(1L);
    }

    @Test
    @DisplayName("공구 수정 실패 - 주최자가 아닌 사용자")
    void updateGroupBuy_Fail_NotHost() {
        // given
        GroupBuy groupBuy = GroupBuy.createGeneral(
            testUser,
            "삼겹살 공동구매",
            "신선한 삼겹살 공동구매합니다",
            "육류",
            50000,
            5,
            LocalDateTime.now().plusDays(7),
            DeliveryMethod.BOTH,
            null,
            null,
            true
        );
        setGroupBuyId(groupBuy, 1L);

        User otherUser = User.create(
            "other@example.com",
            "encodedPassword",
            "다른사용자",
            "010-9999-9999"
        );
        setUserId(otherUser, 2L);

        UpdateGroupBuyRequest updateRequest = UpdateGroupBuyRequest.builder()
            .title("목살 공동구매")
            .content("수정된 내용입니다")
            .category("육류")
            .totalPrice(60000)
            .targetHeadcount(7)
            .deadline(LocalDateTime.now().plusDays(10))
            .deliveryMethod(DeliveryMethod.DIRECT)
            .isParticipantListPublic(false)
            .build();

        given(userRepository.findById(2L)).willReturn(Optional.of(otherUser));
        given(groupBuyRepository.findByIdWithHost(1L)).willReturn(Optional.of(groupBuy));

        // when & then
        assertThatThrownBy(() -> groupBuyService.updateGroupBuy(2L, 1L, updateRequest))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNAUTHORIZED_GROUP_BUY_ACCESS);
    }

    @Test
    @DisplayName("공구 수정 실패 - 존재하지 않는 공구")
    void updateGroupBuy_Fail_NotFound() {
        // given
        Long nonExistentId = 999L;
        UpdateGroupBuyRequest updateRequest = UpdateGroupBuyRequest.builder()
            .title("목살 공동구매")
            .content("수정된 내용입니다")
            .category("육류")
            .totalPrice(60000)
            .targetHeadcount(7)
            .deadline(LocalDateTime.now().plusDays(10))
            .deliveryMethod(DeliveryMethod.DIRECT)
            .isParticipantListPublic(false)
            .build();

        given(userRepository.findById(testUserId)).willReturn(Optional.of(testUser));
        given(groupBuyRepository.findByIdWithHost(nonExistentId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> groupBuyService.updateGroupBuy(testUserId, nonExistentId, updateRequest))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.GROUP_BUY_NOT_FOUND);
    }

    // ========== 공구 삭제 테스트 ==========

    @Test
    @DisplayName("공구 삭제 성공 - 주최자가 삭제 (참여자 없음)")
    void deleteGroupBuy_Success() {
        // given
        GroupBuy groupBuy = GroupBuy.createGeneral(
            testUser,
            "삼겹살 공동구매",
            "신선한 삼겹살 공동구매합니다",
            "육류",
            50000,
            5,
            LocalDateTime.now().plusDays(7),
            DeliveryMethod.BOTH,
            null,
            null,
            true
        );
        setGroupBuyId(groupBuy, 1L);

        given(userRepository.findById(testUserId)).willReturn(Optional.of(testUser));
        given(groupBuyRepository.findByIdWithHost(1L)).willReturn(Optional.of(groupBuy));

        // when
        groupBuyService.deleteGroupBuy(testUserId, 1L);

        // then
        assertThat(groupBuy.getDeletedAt()).isNotNull();
        
        verify(userRepository).findById(testUserId);
        verify(groupBuyRepository).findByIdWithHost(1L);
    }

    @Test
    @DisplayName("공구 삭제 실패 - 주최자가 아닌 사용자")
    void deleteGroupBuy_Fail_NotHost() {
        // given
        GroupBuy groupBuy = GroupBuy.createGeneral(
            testUser,
            "삼겹살 공동구매",
            "신선한 삼겹살 공동구매합니다",
            "육류",
            50000,
            5,
            LocalDateTime.now().plusDays(7),
            DeliveryMethod.BOTH,
            null,
            null,
            true
        );
        setGroupBuyId(groupBuy, 1L);

        User otherUser = User.create(
            "other@example.com",
            "encodedPassword",
            "다른사용자",
            "010-9999-9999"
        );
        setUserId(otherUser, 2L);

        given(userRepository.findById(2L)).willReturn(Optional.of(otherUser));
        given(groupBuyRepository.findByIdWithHost(1L)).willReturn(Optional.of(groupBuy));

        // when & then
        assertThatThrownBy(() -> groupBuyService.deleteGroupBuy(2L, 1L))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNAUTHORIZED_GROUP_BUY_ACCESS);
    }

    @Test
    @DisplayName("공구 삭제 실패 - 참여자가 있는 경우")
    void deleteGroupBuy_Fail_HasParticipants() {
        // given
        GroupBuy groupBuy = GroupBuy.createGeneral(
            testUser,
            "삼겹살 공동구매",
            "신선한 삼겹살",
            "육류",
            50000,
            5,
            LocalDateTime.now().plusDays(7),
            DeliveryMethod.BOTH,
            null,
            null,
            true
        );
        setGroupBuyId(groupBuy, 1L);
        groupBuy.addParticipant(participantUser, 1, DeliveryMethod.DIRECT); // 참여자 1명 추가

        given(userRepository.findById(testUserId)).willReturn(Optional.of(testUser));
        given(groupBuyRepository.findByIdWithHost(1L)).willReturn(Optional.of(groupBuy));

        // when & then
        assertThatThrownBy(() -> groupBuyService.deleteGroupBuy(testUserId, 1L))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.HAS_PARTICIPANTS);
    }

    @Test
    @DisplayName("공구 삭제 실패 - 존재하지 않는 공구")
    void deleteGroupBuy_Fail_NotFound() {
        // given
        Long nonExistentId = 999L;
        given(userRepository.findById(testUserId)).willReturn(Optional.of(testUser));
        given(groupBuyRepository.findByIdWithHost(nonExistentId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> groupBuyService.deleteGroupBuy(testUserId, nonExistentId))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.GROUP_BUY_NOT_FOUND);
    }

    // ========== 레시피 기반 공구 테스트 ==========

    @Test
    @DisplayName("레시피 기반 공구 생성 성공 - 수동으로 제공된 레시피 정보 사용")
    void createRecipeBasedGroupBuy_Success_WithManualRecipeInfo() {
        // given
        CreateGroupBuyRequest request = CreateGroupBuyRequest.builder()
            .title("김치찌개 재료 공구")
            .content("맛있는 김치찌개 재료 공동구매")
            .category("육류")
            .totalPrice(30000)
            .targetHeadcount(4)
            .deadline(LocalDateTime.now().plusDays(7))
            .deliveryMethod(DeliveryMethod.BOTH)
            .isParticipantListPublic(true)
            .imageFiles(new ArrayList<>())
            .recipeApiId("RECIPE_12345")
            .recipeName("김치찌개")
            .recipeImageUrl("https://example.com/recipe/kimchi.jpg")
            .build();

        GroupBuy savedGroupBuy = GroupBuy.createRecipeBased(
            testUser,
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
        setGroupBuyId(savedGroupBuy, 1L);

        given(userRepository.findById(testUserId)).willReturn(Optional.of(testUser));
        given(imageUploadUtil.uploadImages(anyList())).willReturn(new ArrayList<>());
        given(groupBuyRepository.save(any(GroupBuy.class))).willReturn(savedGroupBuy);

        // when
        GroupBuyResponse response = groupBuyService.createRecipeBasedGroupBuy(testUserId, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("김치찌개 재료 공구");
        assertThat(response.getRecipeApiId()).isEqualTo("RECIPE_12345");
        assertThat(response.getRecipeName()).isEqualTo("김치찌개");
        assertThat(response.getRecipeImageUrl()).isEqualTo("https://example.com/recipe/kimchi.jpg");
        
        verify(userRepository).findById(testUserId);
        verify(groupBuyRepository).save(any(GroupBuy.class));
        // 수동 제공된 경우 recipeService 호출하지 않음
        verify(recipeService, never()).getRecipeDetail(anyString());
    }

    @Test
    @DisplayName("레시피 기반 공구 생성 실패 - 레시피 API ID 누락")
    void createRecipeBasedGroupBuy_Fail_NoRecipeApiId() {
        // given
        CreateGroupBuyRequest request = CreateGroupBuyRequest.builder()
            .title("김치찌개 재료 공구")
            .content("맛있는 김치찌개 재료 공동구매")
            .category("육류")
            .totalPrice(30000)
            .targetHeadcount(4)
            .deadline(LocalDateTime.now().plusDays(7))
            .deliveryMethod(DeliveryMethod.BOTH)
            .imageFiles(new ArrayList<>())
            .recipeApiId(null)
            .recipeName("김치찌개")
            .recipeImageUrl("https://example.com/recipe/kimchi.jpg")
            .build();

        // when & then
        assertThatThrownBy(() -> groupBuyService.createRecipeBasedGroupBuy(testUserId, request))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_RECIPE_API_ID);
    }

    @Test
    @DisplayName("레시피 기반 공구 생성 실패 - 존재하지 않는 레시피 ID")
    void createRecipeBasedGroupBuy_Fail_RecipeNotFound() {
        // given
        CreateGroupBuyRequest request = CreateGroupBuyRequest.builder()
            .title("김치찌개 재료 공구")
            .content("맛있는 김치찌개 재료 공동구매")
            .category("육류")
            .totalPrice(30000)
            .targetHeadcount(4)
            .deadline(LocalDateTime.now().plusDays(7))
            .deliveryMethod(DeliveryMethod.BOTH)
            .imageFiles(new ArrayList<>())
            .recipeApiId("invalid-recipe-id-999999")
            .build();

        given(recipeService.getRecipeDetail("invalid-recipe-id-999999"))
            .willThrow(new CustomException(ErrorCode.RECIPE_NOT_FOUND));

        // when & then
        assertThatThrownBy(() -> groupBuyService.createRecipeBasedGroupBuy(testUserId, request))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RECIPE_NOT_FOUND);
    }

    @Test
    @DisplayName("레시피 기반 공구 수정 시 레시피 필드는 불변")
    void updateGroupBuy_RecipeFieldsImmutable() {
        // given - 레시피 기반 공구 생성
        GroupBuy groupBuy = GroupBuy.createRecipeBased(
            testUser,
            "김치찌개 재료 공구",
            "맛있는 김치찌개 재료 공동구매",
            "육류",
            30000,
            4,
            LocalDateTime.now().plusDays(7),
            DeliveryMethod.BOTH,
            null,
            null,
            true,
            "RECIPE_12345",
            "김치찌개",
            "https://example.com/recipe/kimchi.jpg"
        );
        setGroupBuyId(groupBuy, 1L);

        UpdateGroupBuyRequest updateRequest = UpdateGroupBuyRequest.builder()
            .title("된장찌개 재료 공구")
            .content("맛있는 된장찌개 재료 공동구매")
            .category("육류")
            .totalPrice(35000)
            .targetHeadcount(5)
            .deadline(LocalDateTime.now().plusDays(10))
            .deliveryMethod(DeliveryMethod.DIRECT)
            .isParticipantListPublic(false)
            .build();

        given(userRepository.findById(testUserId)).willReturn(Optional.of(testUser));
        given(groupBuyRepository.findByIdWithHost(1L)).willReturn(Optional.of(groupBuy));
        given(groupBuyImageRepository.findByGroupBuyOrderByDisplayOrderAsc(groupBuy))
            .willReturn(new ArrayList<>());

        // when
        GroupBuyResponse updated = groupBuyService.updateGroupBuy(testUserId, 1L, updateRequest);

        // then - 레시피 필드는 변경되지 않아야 함
        assertThat(updated.getTitle()).isEqualTo("된장찌개 재료 공구");
        assertThat(updated.getRecipeApiId()).isEqualTo("RECIPE_12345");
        assertThat(updated.getRecipeName()).isEqualTo("김치찌개");
        assertThat(updated.getRecipeImageUrl()).isEqualTo("https://example.com/recipe/kimchi.jpg");
    }

    @Test
    @DisplayName("레시피 기반 공구 목록 필터링 성공")
    void getGroupBuyList_Success_FilterRecipeOnly() {
        // given
        List<GroupBuy> recipeGroupBuys = new ArrayList<>();
        for (int i = 1; i <= 2; i++) {
            GroupBuy groupBuy = GroupBuy.createRecipeBased(
                testUser,
                "레시피 공구 " + i,
                "레시피 기반 공구",
                "육류",
                30000,
                4,
                LocalDateTime.now().plusDays(7),
                DeliveryMethod.BOTH,
                null,
                null,
                true,
                "RECIPE_" + i,
                "레시피 " + i,
                "https://example.com/recipe" + i + ".jpg"
            );
            setGroupBuyId(groupBuy, (long) i);
            recipeGroupBuys.add(groupBuy);
        }

        Pageable pageable = PageRequest.of(0, 10);
        Page<GroupBuy> groupBuyPage = new PageImpl<>(recipeGroupBuys, pageable, 2);
        GroupBuySearchCondition condition = GroupBuySearchCondition.builder().recipeOnly(true).build();

        given(groupBuyRepository.searchGroupBuys(eq(condition), eq(pageable)))
            .willReturn(groupBuyPage);
        given(groupBuyImageRepository.findByGroupBuyIdInOrderByGroupBuyIdAndDisplayOrder(anyList()))
            .willReturn(new ArrayList<>());

        // when
        Page<GroupBuyResponse> result = groupBuyService.getGroupBuyList(condition, pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(gb -> gb.getRecipeApiId() != null);
        
        verify(groupBuyRepository).searchGroupBuys(eq(condition), eq(pageable));
    }

    // ========== 유틸리티 메서드 ==========

    private void setGroupBuyId(GroupBuy groupBuy, Long id) {
        try {
            Field idField = GroupBuy.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(groupBuy, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setUserId(User user, Long id) {
        try {
            Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
