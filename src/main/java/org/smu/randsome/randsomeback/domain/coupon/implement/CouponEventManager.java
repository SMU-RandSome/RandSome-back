package org.smu.randsome.randsomeback.domain.coupon.implement;

import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.coupon.dto.command.NewCouponEvent;
import org.smu.randsome.randsomeback.domain.coupon.dto.command.UpdateCouponEvent;
import org.smu.randsome.randsomeback.domain.coupon.entity.CouponEvent;
import org.smu.randsome.randsomeback.domain.coupon.repository.CouponEventJpaRepository;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
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

    @Transactional
    public void update(Long couponEventId, UpdateCouponEvent updateCouponEvent) {
        CouponEvent event = couponEventJpaRepository.findByIdAndStatus(couponEventId, EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_COUPON_EVENT));

        event.update(
                updateCouponEvent.name(),
                updateCouponEvent.description(),
                updateCouponEvent.type(),
                updateCouponEvent.totalQuantity(),
                updateCouponEvent.rewardTicketType(),
                updateCouponEvent.rewardTicketAmount(),
                updateCouponEvent.startsAt(),
                updateCouponEvent.expiresAt()
        );
    }

    @Transactional
    public void delete(Long couponEventId) {
        CouponEvent event = couponEventJpaRepository.findByIdAndStatus(couponEventId, EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_COUPON_EVENT));

        event.delete();
    }

}