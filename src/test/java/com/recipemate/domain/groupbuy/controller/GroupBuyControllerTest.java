package com.recipemate.domain.groupbuy.controller;

import com.recipemate.domain.groupbuy.dto.CreateGroupBuyRequest;
import com.recipemate.domain.groupbuy.dto.GroupBuyResponse;
import com.recipemate.domain.groupbuy.dto.UpdateGroupBuyRequest;
import com.recipemate.domain.groupbuy.service.GroupBuyService;
import com.recipemate.domain.groupbuy.service.ParticipationService;
import com.recipemate.domain.user.entity.User;
import com.recipemate.domain.user.repository.UserRepository;
import com.recipemate.global.common.DeliveryMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GroupBuyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GroupBuyService groupBuyService;

    @Autowired
    private ParticipationService participationService;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private User participantUser;
    private GroupBuyResponse testGroupBuy;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성 (공구 주최자)
        testUser = User.create(
            "test@example.com",
            "encodedPassword",
            "테스터",
            "010-1234-5678"
        );
        userRepository.save(testUser);

        // 테스트 참여자 생성
        participantUser = User.create(
            "participant@example.com",
            "encodedPassword",
            "참여자",
            "010-9876-5432"
        );
        userRepository.save(participantUser);

        // 테스트용 공구 생성
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
        testGroupBuy = groupBuyService.createGroupBuy(testUser.getId(), createRequest);
    }

    // ========== 페이지 렌더링 테스트 ==========

    @Test
    @DisplayName("공구 목록 페이지 렌더링 성공")
    void listPage_Success() throws Exception {
        mockMvc.perform(get("/group-purchases/list"))
            .andExpect(status().isOk())
            .andExpect(view().name("group-purchases/list"))
            .andExpect(model().attributeExists("groupBuys"))
            .andExpect(model().attributeExists("searchCondition"));
    }

    @Test
    @DisplayName("공구 상세 페이지 렌더링 성공")
    void detailPage_Success() throws Exception {
        mockMvc.perform(get("/group-purchases/" + testGroupBuy.getId()))
            .andExpect(status().isOk())
            .andExpect(view().name("group-purchases/detail"))
            .andExpect(model().attributeExists("groupBuy"))
            .andExpect(model().attribute("groupBuy", 
                org.hamcrest.Matchers.hasProperty("id", org.hamcrest.Matchers.equalTo(testGroupBuy.getId()))));
    }

    @Test
    @DisplayName("공구 작성 페이지 렌더링 성공")
    @WithMockUser
    void createPage_Success() throws Exception {
        mockMvc.perform(get("/group-purchases/new"))
            .andExpect(status().isOk())
            .andExpect(view().name("group-purchases/form"));
    }

    @Test
    @DisplayName("공구 수정 페이지 렌더링 성공")
    @WithMockUser
    void editPage_Success() throws Exception {
        mockMvc.perform(get("/group-purchases/" + testGroupBuy.getId() + "/edit"))
            .andExpect(status().isOk())
            .andExpect(view().name("group-purchases/form"))
            .andExpect(model().attributeExists("groupBuy"));
    }

    // ========== 폼 처리 테스트 ==========

    @Test
    @DisplayName("공구 생성 폼 제출 성공")
    @WithMockUser(username = "test@example.com")
    void createGroupBuy_FormSubmit_Success() throws Exception {
        mockMvc.perform(post("/group-purchases")
                .with(csrf())
                .param("title", "양파 공동구매")
                .param("content", "신선한 양파입니다")
                .param("category", "채소")
                .param("totalPrice", "20000")
                .param("targetHeadcount", "4")
                .param("deadline", LocalDateTime.now().plusDays(7).toString())
                .param("deliveryMethod", "BOTH")
                .param("meetupLocation", "홍대입구역")
                .param("parcelFee", "2500")
                .param("isParticipantListPublic", "true"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrlPattern("/group-purchases/*"))
            .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @DisplayName("공구 수정 폼 제출 성공")
    @WithMockUser(username = "test@example.com")
    void updateGroupBuy_FormSubmit_Success() throws Exception {
        mockMvc.perform(post("/group-purchases/" + testGroupBuy.getId())
                .with(csrf())
                .param("title", "목살 공동구매")
                .param("content", "수정된 내용입니다")
                .param("category", "육류")
                .param("totalPrice", "60000")
                .param("targetHeadcount", "7")
                .param("deadline", LocalDateTime.now().plusDays(10).toString())
                .param("deliveryMethod", "DIRECT")
                .param("meetupLocation", "홍대입구역 3번 출구")
                .param("isParticipantListPublic", "false"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/group-purchases/" + testGroupBuy.getId()))
            .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @DisplayName("공구 삭제 폼 제출 성공")
    @WithMockUser(username = "test@example.com")
    void deleteGroupBuy_FormSubmit_Success() throws Exception {
        mockMvc.perform(post("/group-purchases/" + testGroupBuy.getId() + "/delete")
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/group-purchases/list"))
            .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @DisplayName("공구 참여 폼 제출 성공")
    @WithMockUser(username = "participant@example.com")
    void participate_FormSubmit_Success() throws Exception {
        mockMvc.perform(post("/group-purchases/" + testGroupBuy.getId() + "/participate")
                .with(csrf())
                .param("quantity", "1")
                .param("deliveryMethod", "DIRECT"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/group-purchases/" + testGroupBuy.getId()))
            .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @DisplayName("공구 참여 취소 폼 제출 성공")
    @WithMockUser(username = "participant@example.com")
    void cancelParticipation_FormSubmit_Success() throws Exception {
        // given - 먼저 참여
        com.recipemate.domain.groupbuy.dto.ParticipateRequest request = 
            com.recipemate.domain.groupbuy.dto.ParticipateRequest.builder()
                .selectedDeliveryMethod(DeliveryMethod.DIRECT)
                .quantity(1)
                .build();
        participationService.participate(participantUser.getId(), testGroupBuy.getId(), request);

        // when & then
        mockMvc.perform(post("/group-purchases/" + testGroupBuy.getId() + "/participate/cancel")
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/group-purchases/" + testGroupBuy.getId()))
            .andExpect(flash().attributeExists("successMessage"));
    }

    // ========== 에러 처리 테스트 ==========

    @Test
    @DisplayName("존재하지 않는 공구 상세 페이지 조회 실패")
    void detailPage_NotFound() throws Exception {
        mockMvc.perform(get("/group-purchases/999999"))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("공구 생성 폼 제출 실패 - 잘못된 데이터")
    @WithMockUser(username = "test@example.com")
    void createGroupBuy_FormSubmit_InvalidData() throws Exception {
        mockMvc.perform(post("/group-purchases")
                .with(csrf())
                .param("title", "") // 빈 제목
                .param("content", "내용")
                .param("category", "육류")
                .param("totalPrice", "-1000") // 음수 가격
                .param("targetHeadcount", "1") // 최소 인원 미달
                .param("deadline", LocalDateTime.now().plusDays(7).toString())
                .param("deliveryMethod", "BOTH"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/group-purchases/new"))
            .andExpect(flash().attributeExists("errorMessage"));
    }
}
