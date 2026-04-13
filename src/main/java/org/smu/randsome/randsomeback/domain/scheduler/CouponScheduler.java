package org.smu.randsome.randsomeback.domain.scheduler;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.smu.randsome.randsomeback.domain.coupon.entity.Coupon;
import org.smu.randsome.randsomeback.domain.coupon.implement.CouponManager;
import org.smu.randsome.randsomeback.domain.coupon.implement.CouponReader;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponScheduler {

    private final CouponReader couponReader;
    private final CouponManager couponManager;

    @Scheduled(cron = "0 0 0 * * *")  // 매일 자정
    @SchedulerLock(name = "coupon-expiration", lockAtMostFor = "5m", lockAtLeastFor = "1m")
    public void expireOverdueCoupons() {
        LocalDateTime now = LocalDateTime.now();

        List<Coupon> expirable = couponReader.findExpirable(now);
        couponManager.expireBatch(expirable);

        log.info("[CouponExpirationScheduler] 쿠폰 만료 처리 완료 - count={}", expirable.size());
    }

}