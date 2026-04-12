package org.smu.randsome.randsomeback.domain.coupon.implement;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.coupon.entity.CouponEvent;
import org.smu.randsome.randsomeback.domain.coupon.enums.CouponEventStatus;
import org.smu.randsome.randsomeback.domain.coupon.repository.CouponEventJpaRepository;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class CouponEventReader {

    private final CouponEventJpaRepository couponEventJpaRepository;

    @Transactional(readOnly = true)
    public CouponEvent find(Long couponEventId) {
        return couponEventJpaRepository.findByIdAndStatus(couponEventId, EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_COUPON_EVENT));
    }

    @Transactional(readOnly = true)
    public List<CouponEvent> findCouponEvents() {
        return couponEventJpaRepository.findAllByStatusOrderByStartsAtDesc(EntityStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public List<CouponEvent> findDraftEventsReadyToActivate(LocalDateTime now) {
        return couponEventJpaRepository.findAllByEventStatusAndStartsAtLessThanEqualAndStatus(
                CouponEventStatus.DRAFT, now, EntityStatus.ACTIVE);
    }

}