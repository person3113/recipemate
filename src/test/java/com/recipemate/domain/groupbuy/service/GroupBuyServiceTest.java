package com.recipemate.domain.groupbuy.service;

import com.recipemate.domain.groupbuy.dto.CreateGroupBuyRequest;
import com.recipemate.domain.groupbuy.dto.GroupBuyResponse;
import com.recipemate.domain.groupbuy.dto.GroupBuySearchCondition;
import com.recipemate.domain.groupbuy.dto.UpdateGroupBuyRequest;
import com.recipemate.domain.groupbuy.entity.GroupBuy;
import com.recipemate.domain.groupbuy.repository.GroupBuyImageRepository;
import com.recipemate.domain.groupbuy.repository.GroupBuyRepository;
import com.recipemate.domain.user.entity.User;
import com.recipemate.domain.user.repository.UserRepository;
import com.recipemate.global.common.DeliveryMethod;
import com.recipemate.global.common.GroupBuyStatus;
import com.recipemate.global.exception.CustomException;
import com.recipemate.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class GroupBuyServiceTest {

    @Autowired
    private GroupBuyService groupBuyService;

    @Autowired
    private GroupBuyRepository groupBuyRepository;

    @Autowired
    private GroupBuyImageRepository groupBuyImageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.create(
            "test@example.com",
            "encodedPassword",
            "테스터",
            "010-1234-5678"
        );
        userRepository.save(testUser);
    }

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

        // when
        GroupBuyResponse response = groupBuyService.createGroupBuy(testUser.getId(), request);

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
        assertThatThrownBy(() -> groupBuyService.createGroupBuy(testUser.getId(), request))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_TITLE);
    }

    @Test
    @DisplayName("마감일이 과거일 때 실패")
    void createGroupBuy_Fail_PastDeadline() {
        // given
        CreateGroupBuyRequest request = CreateGroupBuyRequest.builder()
            .title("삼겹살 공동구매")
            .content("신선한 삼겹살 공동구매합니다")
            .category("육류")
            .totalPrice(50000)
            .targetHeadcount(5)
            .deadline(LocalDateTime.now().minusDays(1))
            .deliveryMethod(DeliveryMethod.BOTH)
            .build();

        // when & then
        assertThatThrownBy(() -> groupBuyService.createGroupBuy(testUser.getId(), request))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_DEADLINE);
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
            .build();

        // when & then
        assertThatThrownBy(() -> groupBuyService.createGroupBuy(testUser.getId(), request))
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

        // when & then
        assertThatThrownBy(() -> groupBuyService.createGroupBuy(testUser.getId(), request))
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

        // when & then
        assertThatThrownBy(() -> groupBuyService.createGroupBuy(testUser.getId(), request))
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

        // when
        GroupBuyResponse response = groupBuyService.createGroupBuy(testUser.getId(), request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getImageUrls()).hasSize(2);
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
        // 10개의 공구 생성
        for (int i = 1; i <= 10; i++) {
            CreateGroupBuyRequest request = CreateGroupBuyRequest.builder()
                .title("공구 " + i)
                .content("공구 내용 " + i)
                .category("육류")
                .totalPrice(10000 * i)
                .targetHeadcount(5)
                .deadline(LocalDateTime.now().plusDays(7))
                .deliveryMethod(DeliveryMethod.BOTH)
                .imageFiles(new ArrayList<>())
                .build();
            groupBuyService.createGroupBuy(testUser.getId(), request);
        }

        Pageable pageable = PageRequest.of(0, 5);

        // when
        Page<GroupBuyResponse> result = groupBuyService.getGroupBuyList(null, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(5);
        assertThat(result.getTotalElements()).isEqualTo(10);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.isFirst()).isTrue();
    }

    @Test
    @DisplayName("공구 목록 조회 성공 - 카테고리 필터링")
    void getGroupBuyList_Success_FilterByCategory() {
        // given
        // 육류 카테고리 공구 3개
        for (int i = 1; i <= 3; i++) {
            CreateGroupBuyRequest request = CreateGroupBuyRequest.builder()
                .title("육류 공구 " + i)
                .content("육류 공구 내용 " + i)
                .category("육류")
                .totalPrice(10000)
                .targetHeadcount(5)
                .deadline(LocalDateTime.now().plusDays(7))
                .deliveryMethod(DeliveryMethod.BOTH)
                .imageFiles(new ArrayList<>())
                .build();
            groupBuyService.createGroupBuy(testUser.getId(), request);
        }

        // 채소 카테고리 공구 2개
        for (int i = 1; i <= 2; i++) {
            CreateGroupBuyRequest request = CreateGroupBuyRequest.builder()
                .title("채소 공구 " + i)
                .content("채소 공구 내용 " + i)
                .category("채소")
                .totalPrice(5000)
                .targetHeadcount(5)
                .deadline(LocalDateTime.now().plusDays(7))
                .deliveryMethod(DeliveryMethod.BOTH)
                .imageFiles(new ArrayList<>())
                .build();
            groupBuyService.createGroupBuy(testUser.getId(), request);
        }

        Pageable pageable = PageRequest.of(0, 10);

        // when - 육류 카테고리만 조회
        Page<GroupBuyResponse> result = groupBuyService.getGroupBuyList(
            GroupBuySearchCondition.builder().category("육류").build(),
            pageable
        );

        // then
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent())
            .allMatch(gb -> gb.getCategory().equals("육류"));
    }

    @Test
    @DisplayName("공구 목록 조회 성공 - 상태 필터링 (RECRUITING)")
    void getGroupBuyList_Success_FilterByStatus() {
        // given
        GroupBuy recruiting = GroupBuy.createGeneral(
            testUser,
            "모집중 공구",
            "내용",
            "육류",
            10000,
            5,
            LocalDateTime.now().plusDays(7),
            DeliveryMethod.BOTH,
            null,
            null,
            true
        );
        groupBuyRepository.save(recruiting);

        // 마감임박 공구 생성
        GroupBuy imminent = GroupBuy.createGeneral(
            testUser,
            "마감임박 공구",
            "내용",
            "육류",
            10000,
            5,
            LocalDateTime.now().plusHours(23), // 24시간 이내
            DeliveryMethod.BOTH,
            null,
            null,
            true
        );
        groupBuyRepository.save(imminent);

        Pageable pageable = PageRequest.of(0, 10);

        // when - RECRUITING 상태만 조회
        Page<GroupBuyResponse> result = groupBuyService.getGroupBuyList(
            GroupBuySearchCondition.builder().status(GroupBuyStatus.RECRUITING).build(),
            pageable
        );

        // then
        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getContent())
            .allMatch(gb -> gb.getStatus() == GroupBuyStatus.RECRUITING);
    }

    @Test
    @DisplayName("공구 목록 조회 성공 - 키워드 검색 (제목)")
    void getGroupBuyList_Success_SearchByKeywordInTitle() {
        // given
        CreateGroupBuyRequest request1 = CreateGroupBuyRequest.builder()
            .title("삼겹살 공동구매")
            .content("신선한 고기")
            .category("육류")
            .totalPrice(50000)
            .targetHeadcount(5)
            .deadline(LocalDateTime.now().plusDays(7))
            .deliveryMethod(DeliveryMethod.BOTH)
            .imageFiles(new ArrayList<>())
            .build();
        groupBuyService.createGroupBuy(testUser.getId(), request1);

        CreateGroupBuyRequest request2 = CreateGroupBuyRequest.builder()
            .title("양파 공동구매")
            .content("싱싱한 양파")
            .category("채소")
            .totalPrice(20000)
            .targetHeadcount(5)
            .deadline(LocalDateTime.now().plusDays(7))
            .deliveryMethod(DeliveryMethod.BOTH)
            .imageFiles(new ArrayList<>())
            .build();
        groupBuyService.createGroupBuy(testUser.getId(), request2);

        Pageable pageable = PageRequest.of(0, 10);

        // when - "삼겹살" 키워드로 검색
        Page<GroupBuyResponse> result = groupBuyService.getGroupBuyList(
            GroupBuySearchCondition.builder().keyword("삼겹살").build(),
            pageable
        );

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).contains("삼겹살");
    }

    @Test
    @DisplayName("공구 목록 조회 성공 - 키워드 검색 (내용)")
    void getGroupBuyList_Success_SearchByKeywordInContent() {
        // given
        CreateGroupBuyRequest request1 = CreateGroupBuyRequest.builder()
            .title("고기 공동구매")
            .content("프리미엄 한우 삼겹살입니다")
            .category("육류")
            .totalPrice(50000)
            .targetHeadcount(5)
            .deadline(LocalDateTime.now().plusDays(7))
            .deliveryMethod(DeliveryMethod.BOTH)
            .imageFiles(new ArrayList<>())
            .build();
        groupBuyService.createGroupBuy(testUser.getId(), request1);

        CreateGroupBuyRequest request2 = CreateGroupBuyRequest.builder()
            .title("채소 공동구매")
            .content("싱싱한 양파")
            .category("채소")
            .totalPrice(20000)
            .targetHeadcount(5)
            .deadline(LocalDateTime.now().plusDays(7))
            .deliveryMethod(DeliveryMethod.BOTH)
            .imageFiles(new ArrayList<>())
            .build();
        groupBuyService.createGroupBuy(testUser.getId(), request2);

        Pageable pageable = PageRequest.of(0, 10);

        // when - "프리미엄" 키워드로 검색
        Page<GroupBuyResponse> result = groupBuyService.getGroupBuyList(
            GroupBuySearchCondition.builder().keyword("프리미엄").build(),
            pageable
        );

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getContent()).contains("프리미엄");
    }

    @Test
    @DisplayName("공구 목록 조회 성공 - 복합 필터 (카테고리 + 키워드)")
    void getGroupBuyList_Success_MultipleFilters() {
        // given
        CreateGroupBuyRequest request1 = CreateGroupBuyRequest.builder()
            .title("삼겹살 공동구매")
            .content("신선한 고기")
            .category("육류")
            .totalPrice(50000)
            .targetHeadcount(5)
            .deadline(LocalDateTime.now().plusDays(7))
            .deliveryMethod(DeliveryMethod.BOTH)
            .imageFiles(new ArrayList<>())
            .build();
        groupBuyService.createGroupBuy(testUser.getId(), request1);

        CreateGroupBuyRequest request2 = CreateGroupBuyRequest.builder()
            .title("목살 공동구매")
            .content("맛있는 고기")
            .category("육류")
            .totalPrice(40000)
            .targetHeadcount(5)
            .deadline(LocalDateTime.now().plusDays(7))
            .deliveryMethod(DeliveryMethod.BOTH)
            .imageFiles(new ArrayList<>())
            .build();
        groupBuyService.createGroupBuy(testUser.getId(), request2);

        CreateGroupBuyRequest request3 = CreateGroupBuyRequest.builder()
            .title("양파 공동구매")
            .content("신선한 양파")
            .category("채소")
            .totalPrice(20000)
            .targetHeadcount(5)
            .deadline(LocalDateTime.now().plusDays(7))
            .deliveryMethod(DeliveryMethod.BOTH)
            .imageFiles(new ArrayList<>())
            .build();
        groupBuyService.createGroupBuy(testUser.getId(), request3);

        Pageable pageable = PageRequest.of(0, 10);

        // when - 카테고리=육류 AND 키워드=삼겹살
        Page<GroupBuyResponse> result = groupBuyService.getGroupBuyList(
            GroupBuySearchCondition.builder()
                .category("육류")
                .keyword("삼겹살")
                .build(),
            pageable
        );

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).contains("삼겹살");
        assertThat(result.getContent().get(0).getCategory()).isEqualTo("육류");
    }

    @Test
    @DisplayName("공구 목록 조회 성공 - 빈 결과")
    void getGroupBuyList_Success_EmptyResult() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when - 존재하지 않는 카테고리로 검색
        Page<GroupBuyResponse> result = groupBuyService.getGroupBuyList(
            GroupBuySearchCondition.builder().category("존재하지않는카테고리").build(),
            pageable
        );

        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    // ========== 공구 상세 조회 테스트 ==========

    @Test
    @DisplayName("공구 상세 조회 성공")
    void getGroupBuyDetail_Success() {
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
        GroupBuyResponse created = groupBuyService.createGroupBuy(testUser.getId(), request);

        // when
        GroupBuyResponse response = groupBuyService.getGroupBuyDetail(created.getId());

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(created.getId());
        assertThat(response.getTitle()).isEqualTo("삼겹살 공동구매");
        assertThat(response.getContent()).isEqualTo("신선한 삼겹살 공동구매합니다");
        assertThat(response.getHostId()).isEqualTo(testUser.getId());
        assertThat(response.getHostNickname()).isEqualTo(testUser.getNickname());
        assertThat(response.getHostMannerTemperature()).isEqualTo(testUser.getMannerTemperature());
    }

    @Test
    @DisplayName("공구 상세 조회 성공 - 주최자 정보 포함 (Fetch Join)")
    void getGroupBuyDetail_Success_WithHostInfo() {
        // given
        CreateGroupBuyRequest request = CreateGroupBuyRequest.builder()
            .title("삼겹살 공동구매")
            .content("신선한 삼겹살 공동구매합니다")
            .category("육류")
            .totalPrice(50000)
            .targetHeadcount(5)
            .deadline(LocalDateTime.now().plusDays(7))
            .deliveryMethod(DeliveryMethod.BOTH)
            .imageFiles(new ArrayList<>())
            .build();
        GroupBuyResponse created = groupBuyService.createGroupBuy(testUser.getId(), request);

        // when
        GroupBuyResponse response = groupBuyService.getGroupBuyDetail(created.getId());

        // then - 주최자 정보가 함께 조회되어야 함
        assertThat(response.getHostId()).isNotNull();
        assertThat(response.getHostNickname()).isEqualTo("테스터");
        assertThat(response.getHostMannerTemperature()).isEqualTo(36.5);
    }

    @Test
    @DisplayName("공구 상세 조회 성공 - 이미지 목록 포함 (순서대로)")
    void getGroupBuyDetail_Success_WithImages() {
        // given
        List<MultipartFile> imageFiles = List.of(
            new MockMultipartFile("image1", "test1.jpg", "image/jpeg", "test1".getBytes()),
            new MockMultipartFile("image2", "test2.jpg", "image/jpeg", "test2".getBytes()),
            new MockMultipartFile("image3", "test3.jpg", "image/jpeg", "test3".getBytes())
        );

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
        GroupBuyResponse created = groupBuyService.createGroupBuy(testUser.getId(), request);

        // when
        GroupBuyResponse response = groupBuyService.getGroupBuyDetail(created.getId());

        // then - 이미지가 순서대로 포함되어야 함
        assertThat(response.getImageUrls()).isNotNull();
        assertThat(response.getImageUrls()).hasSize(3);
    }

    @Test
    @DisplayName("공구 상세 조회 실패 - 존재하지 않는 공구")
    void getGroupBuyDetail_Fail_NotFound() {
        // given
        Long nonExistentId = 999L;

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
        CreateGroupBuyRequest createRequest = CreateGroupBuyRequest.builder()
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
        GroupBuyResponse created = groupBuyService.createGroupBuy(testUser.getId(), createRequest);

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

        // when
        GroupBuyResponse updated = groupBuyService.updateGroupBuy(testUser.getId(), created.getId(), updateRequest);

        // then
        assertThat(updated).isNotNull();
        assertThat(updated.getTitle()).isEqualTo("목살 공동구매");
        assertThat(updated.getContent()).isEqualTo("수정된 내용입니다");
        assertThat(updated.getTotalPrice()).isEqualTo(60000);
        assertThat(updated.getTargetHeadcount()).isEqualTo(7);
        assertThat(updated.getDeliveryMethod()).isEqualTo(DeliveryMethod.DIRECT);
        assertThat(updated.getMeetupLocation()).isEqualTo("홍대입구역 3번 출구");
        assertThat(updated.getParcelFee()).isNull();
        assertThat(updated.getIsParticipantListPublic()).isFalse();
    }

    @Test
    @DisplayName("공구 수정 실패 - 주최자가 아닌 사용자")
    void updateGroupBuy_Fail_NotHost() {
        // given
        CreateGroupBuyRequest createRequest = CreateGroupBuyRequest.builder()
            .title("삼겹살 공동구매")
            .content("신선한 삼겹살 공동구매합니다")
            .category("육류")
            .totalPrice(50000)
            .targetHeadcount(5)
            .deadline(LocalDateTime.now().plusDays(7))
            .deliveryMethod(DeliveryMethod.BOTH)
            .imageFiles(new ArrayList<>())
            .build();
        GroupBuyResponse created = groupBuyService.createGroupBuy(testUser.getId(), createRequest);

        // 다른 사용자 생성
        User otherUser = User.create(
            "other@example.com",
            "encodedPassword",
            "다른사용자",
            "010-9999-9999"
        );
        userRepository.save(otherUser);

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

        // when & then
        assertThatThrownBy(() -> groupBuyService.updateGroupBuy(otherUser.getId(), created.getId(), updateRequest))
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

        // when & then
        assertThatThrownBy(() -> groupBuyService.updateGroupBuy(testUser.getId(), nonExistentId, updateRequest))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.GROUP_BUY_NOT_FOUND);
    }

    // ========== 공구 삭제 테스트 ==========

    @Test
    @DisplayName("공구 삭제 성공 - 주최자가 삭제 (참여자 없음)")
    void deleteGroupBuy_Success() {
        // given
        CreateGroupBuyRequest createRequest = CreateGroupBuyRequest.builder()
            .title("삼겹살 공동구매")
            .content("신선한 삼겹살 공동구매합니다")
            .category("육류")
            .totalPrice(50000)
            .targetHeadcount(5)
            .deadline(LocalDateTime.now().plusDays(7))
            .deliveryMethod(DeliveryMethod.BOTH)
            .imageFiles(new ArrayList<>())
            .build();
        GroupBuyResponse created = groupBuyService.createGroupBuy(testUser.getId(), createRequest);

        // when
        groupBuyService.deleteGroupBuy(testUser.getId(), created.getId());

        // then - 소프트 삭제 검증 (deletedAt이 설정되어야 함)
        GroupBuy deleted = groupBuyRepository.findById(created.getId()).orElseThrow();
        assertThat(deleted.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("공구 삭제 실패 - 주최자가 아닌 사용자")
    void deleteGroupBuy_Fail_NotHost() {
        // given
        CreateGroupBuyRequest createRequest = CreateGroupBuyRequest.builder()
            .title("삼겹살 공동구매")
            .content("신선한 삼겹살 공동구매합니다")
            .category("육류")
            .totalPrice(50000)
            .targetHeadcount(5)
            .deadline(LocalDateTime.now().plusDays(7))
            .deliveryMethod(DeliveryMethod.BOTH)
            .imageFiles(new ArrayList<>())
            .build();
        GroupBuyResponse created = groupBuyService.createGroupBuy(testUser.getId(), createRequest);

        // 다른 사용자 생성
        User otherUser = User.create(
            "other@example.com",
            "encodedPassword",
            "다른사용자",
            "010-9999-9999"
        );
        userRepository.save(otherUser);

        // when & then
        assertThatThrownBy(() -> groupBuyService.deleteGroupBuy(otherUser.getId(), created.getId()))
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
        GroupBuy savedGroupBuy = groupBuyRepository.save(groupBuy);
        savedGroupBuy.increaseParticipant(); // 참여자 1명 추가
        groupBuyRepository.saveAndFlush(savedGroupBuy); // 변경사항 저장 및 즉시 DB에 반영
        
        Long groupBuyId = savedGroupBuy.getId();
        Long userId = testUser.getId();
        entityManager.clear(); // 영속성 컨텍스트 초기화

        // when & then
        assertThatThrownBy(() -> groupBuyService.deleteGroupBuy(userId, groupBuyId))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.HAS_PARTICIPANTS);
    }

    @Test
    @DisplayName("공구 삭제 실패 - 존재하지 않는 공구")
    void deleteGroupBuy_Fail_NotFound() {
        // given
        Long nonExistentId = 999L;

        // when & then
        assertThatThrownBy(() -> groupBuyService.deleteGroupBuy(testUser.getId(), nonExistentId))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.GROUP_BUY_NOT_FOUND);
    }

    // ========== 레시피 기반 공구 테스트 ==========

    @Test
    @DisplayName("레시피 기반 공구 생성 성공")
    void createRecipeBasedGroupBuy_Success() {
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

        // when
        GroupBuyResponse response = groupBuyService.createRecipeBasedGroupBuy(testUser.getId(), request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("김치찌개 재료 공구");
        assertThat(response.getRecipeApiId()).isEqualTo("RECIPE_12345");
        assertThat(response.getRecipeName()).isEqualTo("김치찌개");
        assertThat(response.getRecipeImageUrl()).isEqualTo("https://example.com/recipe/kimchi.jpg");
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
            .recipeApiId(null) // 레시피 ID 누락
            .recipeName("김치찌개")
            .recipeImageUrl("https://example.com/recipe/kimchi.jpg")
            .build();

        // when & then
        assertThatThrownBy(() -> groupBuyService.createRecipeBasedGroupBuy(testUser.getId(), request))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_RECIPE_API_ID);
    }

    @Test
    @DisplayName("레시피 기반 공구 수정 시 레시피 필드는 불변")
    void updateGroupBuy_RecipeFieldsImmutable() {
        // given - 레시피 기반 공구 생성
        CreateGroupBuyRequest createRequest = CreateGroupBuyRequest.builder()
            .title("김치찌개 재료 공구")
            .content("맛있는 김치찌개 재료 공동구매")
            .category("육류")
            .totalPrice(30000)
            .targetHeadcount(4)
            .deadline(LocalDateTime.now().plusDays(7))
            .deliveryMethod(DeliveryMethod.BOTH)
            .imageFiles(new ArrayList<>())
            .recipeApiId("RECIPE_12345")
            .recipeName("김치찌개")
            .recipeImageUrl("https://example.com/recipe/kimchi.jpg")
            .build();
        GroupBuyResponse created = groupBuyService.createRecipeBasedGroupBuy(testUser.getId(), createRequest);

        // 수정 요청 (레시피 필드는 수정 불가)
        UpdateGroupBuyRequest updateRequest = UpdateGroupBuyRequest.builder()
            .title("된장찌개 재료 공구") // 제목만 수정
            .content("맛있는 된장찌개 재료 공동구매")
            .category("육류")
            .totalPrice(35000)
            .targetHeadcount(5)
            .deadline(LocalDateTime.now().plusDays(10))
            .deliveryMethod(DeliveryMethod.DIRECT)
            .isParticipantListPublic(false)
            .build();

        // when
        GroupBuyResponse updated = groupBuyService.updateGroupBuy(testUser.getId(), created.getId(), updateRequest);

        // then - 레시피 필드는 변경되지 않아야 함
        assertThat(updated.getTitle()).isEqualTo("된장찌개 재료 공구"); // 제목은 변경됨
        assertThat(updated.getRecipeApiId()).isEqualTo("RECIPE_12345"); // 레시피 ID는 그대로
        assertThat(updated.getRecipeName()).isEqualTo("김치찌개"); // 레시피 이름은 그대로
        assertThat(updated.getRecipeImageUrl()).isEqualTo("https://example.com/recipe/kimchi.jpg"); // 레시피 이미지는 그대로
    }

    @Test
    @DisplayName("레시피 기반 공구 목록 필터링 성공")
    void getGroupBuyList_Success_FilterRecipeOnly() {
        // given
        // 레시피 기반 공구 2개 생성
        for (int i = 1; i <= 2; i++) {
            CreateGroupBuyRequest request = CreateGroupBuyRequest.builder()
                .title("레시피 공구 " + i)
                .content("레시피 기반 공구")
                .category("육류")
                .totalPrice(30000)
                .targetHeadcount(4)
                .deadline(LocalDateTime.now().plusDays(7))
                .deliveryMethod(DeliveryMethod.BOTH)
                .imageFiles(new ArrayList<>())
                .recipeApiId("RECIPE_" + i)
                .recipeName("레시피 " + i)
                .recipeImageUrl("https://example.com/recipe" + i + ".jpg")
                .build();
            groupBuyService.createRecipeBasedGroupBuy(testUser.getId(), request);
        }

        // 일반 공구 1개 생성
        CreateGroupBuyRequest generalRequest = CreateGroupBuyRequest.builder()
            .title("일반 공구")
            .content("일반 공동구매")
            .category("육류")
            .totalPrice(50000)
            .targetHeadcount(5)
            .deadline(LocalDateTime.now().plusDays(7))
            .deliveryMethod(DeliveryMethod.BOTH)
            .imageFiles(new ArrayList<>())
            .build();
        groupBuyService.createGroupBuy(testUser.getId(), generalRequest);

        Pageable pageable = PageRequest.of(0, 10);

        // when - 레시피 기반 공구만 조회
        Page<GroupBuyResponse> result = groupBuyService.getGroupBuyList(
            GroupBuySearchCondition.builder().recipeOnly(true).build(),
            pageable
        );

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(gb -> gb.getRecipeApiId() != null);
    }

    // ========== 동시성 제어 테스트 ==========

    @Test
    @DisplayName("공구 엔티티에 @Version 필드가 있어 낙관적 락 지원")
    void groupBuy_HasVersionField_ForOptimisticLocking() {
        // given - 공구 생성
        CreateGroupBuyRequest createRequest = CreateGroupBuyRequest.builder()
            .title("삼겹살 공동구매")
            .content("신선한 삼겹살")
            .category("육류")
            .totalPrice(50000)
            .targetHeadcount(5)
            .deadline(LocalDateTime.now().plusDays(7))
            .deliveryMethod(DeliveryMethod.BOTH)
            .imageFiles(new ArrayList<>())
            .build();
        GroupBuyResponse created = groupBuyService.createGroupBuy(testUser.getId(), createRequest);

        // when - 공구 조회
        GroupBuy groupBuy = groupBuyRepository.findById(created.getId()).orElseThrow();

        // then - @Version 필드가 존재하고 초기값이 0이어야 함
        assertThat(groupBuy.getVersion()).isNotNull();
        assertThat(groupBuy.getVersion()).isEqualTo(0L);

        // when - 공구 수정
        UpdateGroupBuyRequest updateRequest = UpdateGroupBuyRequest.builder()
            .title("수정된 공구")
            .content("수정된 내용")
            .category("육류")
            .totalPrice(60000)
            .targetHeadcount(7)
            .deadline(LocalDateTime.now().plusDays(10))
            .deliveryMethod(DeliveryMethod.DIRECT)
            .isParticipantListPublic(false)
            .build();
        groupBuyService.updateGroupBuy(testUser.getId(), created.getId(), updateRequest);
        entityManager.flush();
        entityManager.clear();

        // then - 수정 후 버전이 증가해야 함
        GroupBuy updated = groupBuyRepository.findById(created.getId()).orElseThrow();
        assertThat(updated.getVersion()).isEqualTo(1L);
    }
}
