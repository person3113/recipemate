package com.recipemate.domain.groupbuy.service;

import com.recipemate.domain.groupbuy.entity.GroupBuy;
import com.recipemate.domain.groupbuy.repository.GroupBuyRepository;
import com.recipemate.domain.user.entity.User;
import com.recipemate.domain.user.repository.UserRepository;
import com.recipemate.global.common.DeliveryMethod;
import com.recipemate.global.common.GroupBuyStatus;
import com.recipemate.global.common.UserRole;
import com.recipemate.global.config.CacheConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * GroupBuyService 캐싱 테스트
 * 인기 공구 목록 조회 캐싱을 검증합니다.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class GroupBuyServiceCacheTest {

    @Autowired
    private GroupBuyService groupBuyService;

    @SpyBean
    private GroupBuyRepository groupBuyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CacheManager cacheManager;

    private User testUser;

    @BeforeEach
    void setUp() {
        // 캐시 초기화
        cacheManager.getCacheNames().forEach(cacheName ->
            cacheManager.getCache(cacheName).clear()
        );

        // 기존 테스트 데이터 정리
        groupBuyRepository.deleteAll();
        userRepository.deleteAll();

        // 테스트 사용자 생성
        testUser = User.builder()
            .email("test" + System.currentTimeMillis() + "@example.com")
            .password("password123")
            .nickname("테스트유저" + System.currentTimeMillis())
            .phoneNumber("010-1234-5678")
            .role(UserRole.USER)
            .build();
        testUser = userRepository.save(testUser);

        // 테스트용 공구 데이터 생성 (인기순)
        createGroupBuyWithParticipants("인기공구1", 8, 10); // 참여율 80%
        createGroupBuyWithParticipants("인기공구2", 7, 10); // 참여율 70%
        createGroupBuyWithParticipants("인기공구3", 6, 10); // 참여율 60%
        createGroupBuyWithParticipants("인기공구4", 5, 10); // 참여율 50%
        createGroupBuyWithParticipants("인기공구5", 4, 10); // 참여율 40%
    }

    @Test
    @DisplayName("인기 공구 목록 조회 시 캐싱이 동작한다")
    void testPopularGroupBuysCaching() {
        // given
        int limit = 5;

        // when: 첫 번째 조회
        var firstResult = groupBuyService.getPopularGroupBuys(limit);

        // then: 리포지토리가 한 번 호출됨
        verify(groupBuyRepository, times(1)).findPopularGroupBuys(any(), any());

        // when: 두 번째 조회 (캐시 히트)
        var secondResult = groupBuyService.getPopularGroupBuys(limit);

        // then: 리포지토리가 추가로 호출되지 않음 (총 1번만 호출)
        verify(groupBuyRepository, times(1)).findPopularGroupBuys(any(), any());

        // then: 결과가 동일함
        assertThat(secondResult).hasSize(firstResult.size());
        assertThat(secondResult.get(0).getTitle()).isEqualTo(firstResult.get(0).getTitle());
    }

    @Test
    @DisplayName("다른 limit으로 조회 시 별도 캐시 키를 사용한다")
    void testPopularGroupBuysCaching_DifferentLimit() {
        // given
        int limit1 = 3;
        int limit2 = 5;

        // when: 첫 번째 조회 (limit=3)
        var result1 = groupBuyService.getPopularGroupBuys(limit1);

        // when: 두 번째 조회 (limit=5)
        var result2 = groupBuyService.getPopularGroupBuys(limit2);

        // then: 리포지토리가 두 번 호출됨 (각각 다른 캐시 키)
        verify(groupBuyRepository, times(2)).findPopularGroupBuys(any(), any());

        // then: 결과 크기가 다름
        assertThat(result1).hasSize(3);
        assertThat(result2).hasSize(5);
    }

    @Test
    @DisplayName("캐시 초기화 후 다시 조회하면 리포지토리가 호출된다")
    void testPopularGroupBuysCaching_CacheEviction() {
        // given
        int limit = 5;

        // when: 첫 번째 조회
        groupBuyService.getPopularGroupBuys(limit);

        // when: 캐시 초기화
        cacheManager.getCache(CacheConfig.POPULAR_GROUP_BUYS_CACHE).clear();

        // when: 두 번째 조회
        groupBuyService.getPopularGroupBuys(limit);

        // then: 리포지토리가 두 번 호출됨 (캐시 초기화로 인해)
        verify(groupBuyRepository, times(2)).findPopularGroupBuys(any(), any());
    }

    @Test
    @DisplayName("인기 공구가 참여자 수 기준으로 정렬된다")
    void testPopularGroupBuys_OrderByParticipants() {
        // when
        var result = groupBuyService.getPopularGroupBuys(5);

        // then: 참여자 수가 많은 순서대로 정렬됨
        assertThat(result).hasSize(5);
        assertThat(result.get(0).getTitle()).isEqualTo("인기공구1");
        assertThat(result.get(0).getCurrentHeadcount()).isEqualTo(8);
        assertThat(result.get(1).getTitle()).isEqualTo("인기공구2");
        assertThat(result.get(1).getCurrentHeadcount()).isEqualTo(7);
    }

    @Test
    @DisplayName("RECRUITING과 IMMINENT 상태만 조회된다")
    void testPopularGroupBuys_OnlyActiveStatus() {
        // given: CLOSED 상태의 공구 생성
        GroupBuy closedGroupBuy = createGroupBuyWithParticipants("마감공구", 10, 10);
        closedGroupBuy.close();
        groupBuyRepository.save(closedGroupBuy);

        // when
        var result = groupBuyService.getPopularGroupBuys(10);

        // then: CLOSED 상태는 제외됨
        assertThat(result).noneMatch(gb -> gb.getTitle().equals("마감공구"));
        assertThat(result).allMatch(gb ->
            gb.getStatus() == GroupBuyStatus.RECRUITING ||
            gb.getStatus() == GroupBuyStatus.IMMINENT
        );
    }

    /**
     * 테스트용 공구 생성 헬퍼 메서드
     */
    private GroupBuy createGroupBuyWithParticipants(String title, int currentHeadcount, int targetHeadcount) {
        GroupBuy groupBuy = GroupBuy.createGeneral(
            testUser,
            title,
            "테스트 공구 설명",
            "식재료",
            10000,
            targetHeadcount,
            LocalDateTime.now().plusDays(7),
            DeliveryMethod.BOTH,
            "서울시 강남구",
            3000,
            true
        );

        // 참여자 수 설정 (리플렉션 사용)
        try {
            java.lang.reflect.Field field = GroupBuy.class.getDeclaredField("currentHeadcount");
            field.setAccessible(true);
            field.set(groupBuy, currentHeadcount);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set currentHeadcount", e);
        }

        return groupBuyRepository.save(groupBuy);
    }
}
