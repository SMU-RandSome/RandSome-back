package org.smu.randsome.randsomeback.domain.coupon.implement;

import java.time.Duration;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.coupon.entity.Coupon;
import org.smu.randsome.randsomeback.domain.coupon.entity.CouponEvent;
import org.smu.randsome.randsomeback.domain.coupon.repository.CouponRepository;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.implement.MemberReader;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class CouponManager {

    private final CouponEventReader couponEventReader;
    private final MemberReader memberReader;
    private final CouponCacheManager couponCacheManager;
    private final CouponRepository couponRepository;

    @Transactional
    public Long issueCoupon(Long couponEventId, Long memberId, LocalDateTime now) {
        CouponEvent couponEvent = couponEventReader.find(couponEventId);

        if (!couponEvent.isIssuable(now)) {
            throw new CoreException(ErrorType.COUPON_EVENT_NOT_ACTIVE);
        }

        Member member = memberReader.getReference(memberId);

        // Redis 기반 동시성 제어
        Duration ttl = calculateTtl(couponEvent, now);
        couponCacheManager.acquireMemberLockOrThrow(couponEventId, memberId, ttl);
        couponCacheManager.decrementStockOrThrow(couponEventId, memberId);

        Coupon coupon = Coupon.issue(couponEvent, member);

        return couponRepository.save(coupon).getId();
    }

    private Duration calculateTtl(CouponEvent couponEvent, LocalDateTime now) {
        return Duration.between(now, couponEvent.getExpiresAt());
    }

}