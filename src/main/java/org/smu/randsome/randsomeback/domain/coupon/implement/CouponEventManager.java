package org.smu.randsome.randsomeback.domain.coupon.implement;

import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.coupon.dto.command.NewCouponEvent;
import org.smu.randsome.randsomeback.domain.coupon.entity.CouponEvent;
import org.smu.randsome.randsomeback.domain.coupon.repository.CouponEventJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CouponEventManager {

    private final CouponEventJpaRepository couponEventJpaRepository;

    @Transactional
    public CouponEvent register(NewCouponEvent newCouponEvent) {
        CouponEvent couponEvent = CouponEvent.create(
                newCouponEvent.name(),
                newCouponEvent.description(),
                newCouponEvent.type(),
                newCouponEvent.totalQuantity(),
                newCouponEvent.rewardTicketType(),
                newCouponEvent.rewardTicketAmount(),
                newCouponEvent.startsAt(),
                newCouponEvent.expiresAt()
        );
        return couponEventJpaRepository.save(couponEvent);
    }

}