package org.smu.randsome.randsomeback.domain.coupon.implement;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.coupon.entity.CouponEvent;
import org.smu.randsome.randsomeback.domain.coupon.enums.CouponEventStatus;
import org.smu.randsome.randsomeback.domain.coupon.repository.CouponEventJpaRepository;
import org.smu.randsome.randsomeback.global.config.CacheKeys;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.smu.randsome.randsomeback.infrastructure.redis.RedisRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class CouponEventReader {

    private final CouponEventJpaRepository couponEventJpaRepository;
    private final RedisRepository redisRepository;

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

    @Transactional(readOnly = true)
    public List<CouponEvent> findActiveEventsReadyToEnd(LocalDateTime now) {
        return couponEventJpaRepository.findAllByEventStatusAndExpiresAtLessThanEqualAndStatus(
                CouponEventStatus.ACTIVE, now, EntityStatus.ACTIVE);
    }

    public long findRemainingStock(CouponEvent event) {
        return switch (event.getEventStatus()) {
            case DRAFT -> event.getTotalQuantity();
            case ACTIVE -> countByStock(event.getId());
            case SOLD_OUT, ENDED -> 0L;
        };
    }

    private long countByStock(Long couponEventId) {
        String stock = redisRepository.get(CacheKeys.couponStock(couponEventId));
        return stock != null ? Long.parseLong(stock) : 0L;
    }

}