package com.recipemate.global.event.listeners;

import com.recipemate.global.common.GroupBuyStatus;
import com.recipemate.domain.groupbuy.entity.GroupBuy;
import com.recipemate.domain.groupbuy.repository.GroupBuyRepository;
import com.recipemate.global.event.GroupBuyDeadlineEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 공구 마감 알림 스케줄러
 * 마감일이 24시간 이내로 남은 공구를 찾아서 참여자들에게 알림 발송
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GroupBuyDeadlineScheduler {

    private final GroupBuyRepository groupBuyRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 매일 오전 9시, 오후 6시에 마감 임박 공구 확인
     * 마감일이 24시간 이내인 공구를 찾아서 알림 발송
     */
    @Scheduled(cron = "0 0 9,18 * * *")
    public void checkDeadlineApproaching() {
        log.info("공구 마감 임박 알림 스케줄러 실행");
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline = now.plusHours(24);
        
        // 마감일이 24시간 이내이고, RECRUITING 또는 IMMINENT 상태인 공구 조회
        List<GroupBuy> approachingGroupBuys = groupBuyRepository
                .findByDeadlineBetweenAndStatusIn(
                        now, 
                        deadline, 
                        List.of(GroupBuyStatus.RECRUITING, GroupBuyStatus.IMMINENT)
                );
        
        log.info("마감 임박 공구 {}건 발견", approachingGroupBuys.size());
        
        for (GroupBuy groupBuy : approachingGroupBuys) {
            try {
                eventPublisher.publishEvent(new GroupBuyDeadlineEvent(groupBuy.getId()));
                log.debug("공구 마감 알림 발송: groupBuyId={}, deadline={}", 
                        groupBuy.getId(), groupBuy.getDeadline());
            } catch (Exception e) {
                log.error("공구 마감 알림 발송 실패: groupBuyId={}", groupBuy.getId(), e);
            }
        }
    }
}
