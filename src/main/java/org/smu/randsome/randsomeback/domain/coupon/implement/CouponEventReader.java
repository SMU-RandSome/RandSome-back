package org.smu.randsome.randsomeback.domain.coupon.implement;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.coupon.entity.CouponEvent;
import org.smu.randsome.randsomeback.domain.coupon.repository.CouponEventJpaRepository;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class CouponEventReader {

    private final CouponEventJpaRepository couponEventJpaRepository;

    @Transactional(readOnly = true)
    public List<CouponEvent> findCouponEvents() {
        return couponEventJpaRepository.findAllByStatusOrderByStartsAtDesc(EntityStatus.ACTIVE);
    }

}