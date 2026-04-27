package org.smu.randsome.randsomeback.domain.scheduler;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.smu.randsome.randsomeback.domain.coupon.entity.CouponEvent;
import org.smu.randsome.randsomeback.domain.coupon.implement.CouponEventManager;
import org.smu.randsome.randsomeback.domain.coupon.implement.CouponEventReader;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponEventScheduler {

    private final CouponEventReader couponEventReader;
    private final CouponEventManager couponEventManager;

    /**
     * 쿠폰 이벤트 활성화 스케줄러 <br>
     * 매 시간 정각마다 실행되어, 활성화 대기 중인 쿠폰 이벤트 중에서 활성화 시간이 도래한 이벤트를 활성화 상태로 변경합니다. <br>
     *
     * */
    @Scheduled(cron = "0 30 * * * *")
    @SchedulerLock(name = "activateDueEvents", lockAtMostFor = "10m", lockAtLeastFor = "1m")
    public void activateDueEvents() {
        List<CouponEvent> events = couponEventReader.findDraftEventsReadyToActivate(LocalDateTime.now());
        events.forEach(event -> {
            try {
                couponEventManager.activate(event.getId());
            } catch (Exception e) {
                log.error("[CouponEventScheduler] 쿠폰 이벤트 활성화 실패 - eventId: {}", event.getId(), e);
            }
        });
    }

    /**
     * 쿠폰 이벤트 종료 스케줄러 <br>
     * 매 시간 정각마다 실행되어, 활성화된 쿠폰 이벤트 중에서 만료 시간이 도래한 이벤트를 종료 상태로 변경합니다. <br>
     *
     * */
    @Scheduled(cron = "0 30 * * * *")
    @SchedulerLock(name = "deactivateDueEvents", lockAtMostFor = "10m", lockAtLeastFor = "1m")
    public void deactivateDueEvents() {
        List<CouponEvent> events = couponEventReader.findActiveEventsReadyToEnd(LocalDateTime.now());
        events.forEach(event -> {
            try {
                couponEventManager.deactivate(event.getId());
            } catch (Exception e) {
                log.error("[CouponEventScheduler] 쿠폰 이벤트 종료 실패 - eventId: {}", event.getId(), e);
            }
        });
    }

}