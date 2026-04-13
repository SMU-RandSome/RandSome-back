package org.smu.randsome.randsomeback.domain.coupon.implement;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
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

    private final CouponReader couponReader;
    private final CouponEventReader couponEventReader;
    private final MemberReader memberReader;
    private final CouponCacheManager couponCacheManager;
    private final CouponRepository couponRepository;

    public Coupon useCoupon(Long couponId, Long memberId) {
        Coupon coupon = couponReader.findWithEvent(couponId);

        if (!coupon.isOwnedBy(memberId)) {
            throw new CoreException(ErrorType.NOT_FOUND_COUPON);
        }

        coupon.use();

        return coupon;
    }

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

    @Transactional
    public void expireBatch(List<Coupon> coupons) {
        // TODO: 성능 개선 필요 - 대량의 쿠폰을 한 번에 만료 처리할 때, 개별적으로 expire()를 호출하는 대신 배치 업데이트를 고려할 수 있음
        coupons.forEach(Coupon::expire);
        // 파라미터로 전달 받은 coupons는 영속성 컨텍스트에 관리되지 않는 상태이므로, saveAll()을 통해 일괄 저장하여 변경 사항을 DB에 반영한다.
        couponRepository.saveAll(coupons);
    }

    private Duration calculateTtl(CouponEvent couponEvent, LocalDateTime now) {
        return Duration.between(now, couponEvent.getExpiresAt());
    }

}