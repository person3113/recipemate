package com.recipemate.domain.groupbuy.service;

import com.recipemate.domain.groupbuy.dto.CreateGroupBuyRequest;
import com.recipemate.domain.groupbuy.dto.GroupBuyResponse;
import com.recipemate.domain.groupbuy.entity.GroupBuy;
import com.recipemate.domain.groupbuy.repository.GroupBuyImageRepository;
import com.recipemate.domain.groupbuy.repository.GroupBuyRepository;
import com.recipemate.domain.user.entity.User;
import com.recipemate.domain.user.repository.UserRepository;
import com.recipemate.global.common.DeliveryMethod;
import com.recipemate.global.exception.CustomException;
import com.recipemate.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
            .isInstanceOf(IllegalArgumentException.class);
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
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("마감일은 현재보다 이후여야 합니다");
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
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("목표 인원은 2명 이상이어야 합니다");
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
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("총 금액은 0원 이상이어야 합니다");
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
}
