package com.recipemate.domain.groupbuy.controller;

import com.recipemate.domain.groupbuy.dto.GroupBuyResponse;
import com.recipemate.domain.groupbuy.dto.GroupBuySearchCondition;
import com.recipemate.domain.groupbuy.service.GroupBuyService;
import com.recipemate.domain.groupbuy.service.ParticipationService;
import com.recipemate.domain.user.entity.User;
import com.recipemate.domain.user.repository.UserRepository;
import com.recipemate.global.common.DeliveryMethod;
import com.recipemate.global.common.GroupBuyStatus;
import com.recipemate.global.config.TestSecurityConfig;
import com.recipemate.global.exception.CustomException;
import com.recipemate.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GroupBuyController.class)
@Import(TestSecurityConfig.class)
class GroupBuyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GroupBuyService groupBuyService;

    @MockitoBean
    private ParticipationService participationService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;
    
    @MockitoBean
    private com.recipemate.domain.user.service.CustomUserDetailsService customUserDetailsService;

    // ========== 페이지 렌더링 테스트 ==========

    @Test
    @DisplayName("공구 목록 페이지 렌더링 성공")
    void listPage_Success() throws Exception {
        // given
        Page<GroupBuyResponse> mockPage = new PageImpl<>(Collections.emptyList());
        given(groupBuyService.getGroupBuyList(any(GroupBuySearchCondition.class), any(Pageable.class)))
            .willReturn(mockPage);

        // when & then
        mockMvc.perform(get("/group-purchases/list"))
            .andExpect(status().isOk())
            .andExpect(view().name("group-purchases/list"))
            .andExpect(model().attributeExists("groupBuys"))
            .andExpect(model().attributeExists("searchCondition"));
    }

    @Test
    @DisplayName("공구 상세 페이지 렌더링 성공")
    void detailPage_Success() throws Exception {
        // given
        Long groupBuyId = 1L;
        GroupBuyResponse mockResponse = GroupBuyResponse.builder()
            .id(groupBuyId)
            .title("테스트 공구")
            .content("테스트 내용")
            .category("육류")
            .totalPrice(50000)
            .currentHeadcount(1)
            .targetHeadcount(5)
            .status(GroupBuyStatus.RECRUITING)
            .deadline(LocalDateTime.now().plusDays(7))
            .deliveryMethod(DeliveryMethod.BOTH)
            .build();
        
        given(groupBuyService.getGroupBuyDetail(groupBuyId)).willReturn(mockResponse);

        // when & then
        mockMvc.perform(get("/group-purchases/" + groupBuyId))
            .andExpect(status().isOk())
            .andExpect(view().name("group-purchases/detail"))
            .andExpect(model().attributeExists("groupBuy"))
            .andExpect(model().attribute("groupBuy", mockResponse));
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
        // given
        Long groupBuyId = 1L;
        GroupBuyResponse mockResponse = GroupBuyResponse.builder()
            .id(groupBuyId)
            .title("테스트 공구")
            .build();
        
        given(groupBuyService.getGroupBuyDetail(groupBuyId)).willReturn(mockResponse);

        // when & then
        mockMvc.perform(get("/group-purchases/" + groupBuyId + "/edit"))
            .andExpect(status().isOk())
            .andExpect(view().name("group-purchases/form"))
            .andExpect(model().attributeExists("groupBuy"));
    }

    // ========== 폼 처리 테스트 ==========

    @Test
    @DisplayName("공구 생성 폼 제출 성공")
    @WithMockUser(username = "test@example.com")
    void createGroupBuy_FormSubmit_Success() throws Exception {
        // given
        User mockUser = User.builder()
            .id(1L)
            .email("test@example.com")
            .password("password")
            .nickname("테스터")
            .phoneNumber("010-1234-5678")
            .role(com.recipemate.global.common.UserRole.USER)
            .build();
        GroupBuyResponse mockResponse = GroupBuyResponse.builder()
            .id(1L)
            .title("양파 공동구매")
            .build();
        
        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(mockUser));
        given(groupBuyService.createGroupBuy(any(), any())).willReturn(mockResponse);

        // when & then
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
            .andExpect(redirectedUrl("/group-purchases/1"))
            .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @DisplayName("공구 수정 폼 제출 성공")
    @WithMockUser(username = "test@example.com")
    void updateGroupBuy_FormSubmit_Success() throws Exception {
        // given
        Long groupBuyId = 1L;
        User mockUser = User.create("test@example.com", "password", "테스터", "010-1234-5678");
        
        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(mockUser));

        // when & then
        mockMvc.perform(post("/group-purchases/" + groupBuyId)
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
            .andExpect(redirectedUrl("/group-purchases/" + groupBuyId))
            .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @DisplayName("공구 삭제 폼 제출 성공")
    @WithMockUser(username = "test@example.com")
    void deleteGroupBuy_FormSubmit_Success() throws Exception {
        // given
        Long groupBuyId = 1L;
        User mockUser = User.create("test@example.com", "password", "테스터", "010-1234-5678");
        
        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(mockUser));

        // when & then
        mockMvc.perform(post("/group-purchases/" + groupBuyId + "/delete")
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/group-purchases/list"))
            .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @DisplayName("공구 참여 폼 제출 성공")
    @WithMockUser(username = "participant@example.com")
    void participate_FormSubmit_Success() throws Exception {
        // given
        Long groupBuyId = 1L;
        User mockUser = User.create("participant@example.com", "password", "참여자", "010-9876-5432");
        
        given(userRepository.findByEmail("participant@example.com")).willReturn(Optional.of(mockUser));

        // when & then
        mockMvc.perform(post("/group-purchases/" + groupBuyId + "/participate")
                .with(csrf())
                .param("quantity", "1")
                .param("deliveryMethod", "DIRECT"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/group-purchases/" + groupBuyId))
            .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @DisplayName("공구 참여 취소 폼 제출 성공")
    @WithMockUser(username = "participant@example.com")
    void cancelParticipation_FormSubmit_Success() throws Exception {
        // given
        Long groupBuyId = 1L;
        User mockUser = User.create("participant@example.com", "password", "참여자", "010-9876-5432");
        
        given(userRepository.findByEmail("participant@example.com")).willReturn(Optional.of(mockUser));

        // when & then
        mockMvc.perform(post("/group-purchases/" + groupBuyId + "/participate/cancel")
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/group-purchases/" + groupBuyId))
            .andExpect(flash().attributeExists("successMessage"));
    }

    // ========== 에러 처리 테스트 ==========

    @Test
    @DisplayName("존재하지 않는 공구 상세 페이지 조회 실패")
    void detailPage_NotFound() throws Exception {
        // given
        Long nonExistentId = 999999L;
        given(groupBuyService.getGroupBuyDetail(nonExistentId))
            .willThrow(new CustomException(ErrorCode.GROUP_BUY_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/group-purchases/" + nonExistentId))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("공구 생성 폼 제출 실패 - 잘못된 데이터")
    @WithMockUser(username = "test@example.com")
    void createGroupBuy_FormSubmit_InvalidData() throws Exception {
        // given
        User mockUser = User.create("test@example.com", "password", "테스터", "010-1234-5678");
        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(mockUser));
        given(groupBuyService.createGroupBuy(anyLong(), any()))
            .willThrow(new CustomException(ErrorCode.INVALID_INPUT_VALUE));

        // when & then
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

