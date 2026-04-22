package org.smu.randsome.randsomeback.domain.coupon.implement;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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

    public Map<Long, Long> findRemainingStocks(List<CouponEvent> events) {
        Map<Long, Long> activeStocks = fetchActiveStocks(events);

        return events.stream()
                .collect(Collectors.toMap(
                        CouponEvent::getId,
                        event -> remainingStockOf(event, activeStocks)
                ));
    }

    private Map<Long, Long> fetchActiveStocks(List<CouponEvent> events) {
        List<CouponEvent> activeEvents = events.stream()
                .filter(event -> event.getEventStatus() == CouponEventStatus.ACTIVE)
                .toList();

        if (activeEvents.isEmpty()) {
            return Map.of();
        }

        List<String> keys = activeEvents.stream()
                .map(event -> CacheKeys.couponStock(event.getId()))
                .toList();
        List<String> values = redisRepository.mget(keys);

        Map<Long, Long> stocks = new HashMap<>();
        for (int i = 0; i < activeEvents.size(); i++) {
            stocks.put(activeEvents.get(i).getId(), parseStock(values.get(i)));
        }
        return stocks;
    }

    private long remainingStockOf(CouponEvent event, Map<Long, Long> activeStocks) {
        return switch (event.getEventStatus()) {
            case DRAFT -> event.getTotalQuantity();
            case ACTIVE -> activeStocks.getOrDefault(event.getId(), 0L);
            case SOLD_OUT, ENDED -> 0L;
        };
    }

    private long parseStock(String value) {
        return value != null ? Long.parseLong(value) : 0L;
    }

}