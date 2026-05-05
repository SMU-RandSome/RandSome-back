package org.smu.randsome.randsomeback.domain.coupon.implement;

import java.time.Duration;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.admin.coupon.event.CouponEventSoldOutEvent;
import org.smu.randsome.randsomeback.domain.coupon.entity.Coupon;
import org.smu.randsome.randsomeback.domain.coupon.entity.CouponEvent;
import org.smu.randsome.randsomeback.domain.coupon.enums.CouponStatus;
import org.smu.randsome.randsomeback.domain.coupon.repository.CouponRepository;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.implement.MemberReader;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class CouponManager {

    private static final Duration MEMBER_LOCK_TTL = Duration.ofMinutes(5);

    private final CouponReader couponReader;
    private final CouponEventReader couponEventReader;
    private final MemberReader memberReader;
    private final CouponCacheManager couponCacheManager;
    private final CouponRepository couponRepository;
    private final ApplicationEventPublisher eventPublisher;

    // NOTE: CouponService.useCoupon()의 @Transactional 안에서만 호출 하기에, 별도의 트랜잭션을 시작하지 않는다.
    public Coupon useCoupon(Long couponId, Long memberId) {
        Coupon coupon = couponReader.findWithEvent(couponId);

        if (!coupon.isOwnedBy(memberId)) {
            throw new CoreException(ErrorType.NOT_FOUND_COUPON);
        }

        coupon.use(LocalDateTime.now());

        return coupon;
    }

    @Transactional
    public Long issueCoupon(Long couponEventId, Long memberId, LocalDateTime now) {
        CouponEvent couponEvent = couponEventReader.find(couponEventId);
        validateIssuable(couponEvent, now);

        // Redis 기반 동시성 제어
        // TTL을 짧게 고정해 크래시 발생 시 사용자가 단시간 내 재시도할 수 있도록 한다.
        // 실제 중복 발급 방지는 DB unique constraint(UK_CUPON_EVENT_MEMBER)가 보장한다.
        couponCacheManager.acquireMemberLockOrThrow(couponEventId, memberId, MEMBER_LOCK_TTL);
        long remaining = couponCacheManager.decrementStockOrThrow(couponEventId, memberId);

        Member member = memberReader.getReference(memberId);

        return saveCoupon(couponEventId, couponEvent, member, remaining);
    }

    @Transactional
    public int expireBatch(LocalDateTime now) {
        return couponRepository.bulkExpire(
                CouponStatus.AVAILABLE,
                CouponStatus.EXPIRED,
                now,
                EntityStatus.ACTIVE
        );
    }

    private Long saveCoupon(Long couponEventId, CouponEvent couponEvent, Member member, long remaining) {
        Coupon coupon = Coupon.issue(couponEvent, member);

        try {
            Long couponId = couponRepository.saveAndFlush(coupon).getId();

            if (remaining == 0) {
                couponEvent.soldOut();
                eventPublisher.publishEvent(new CouponEventSoldOutEvent(couponEventId));
            }

            return couponId;
        } catch (DataIntegrityViolationException e) {
            throw new CoreException(ErrorType.ALREADY_ISSUED_COUPON);
        }
    }

    private void validateIssuable(CouponEvent couponEvent, LocalDateTime now) {
        if (!couponEvent.isIssuable(now)) {
            throw new CoreException(ErrorType.COUPON_EVENT_NOT_ACTIVE);
        }
    }


}