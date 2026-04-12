package org.smu.randsome.randsomeback.domain.coupon.implement;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.global.config.CacheKeys;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.smu.randsome.randsomeback.infrastructure.redis.RedisRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CouponCacheManager {

    private final RedisRepository redisRepository;

    /**
     * 이벤트 활성화 시 Redis에 재고 키를 초기화한다.
     * TTL은 이벤트 만료 시각까지의 남은 시간으로 설정한다.
     */
    public void initializeStock(Long eventId, int totalQuantity, Duration ttl) {
        String key = CacheKeys.couponStock(eventId);
        redisRepository.put(key, String.valueOf(totalQuantity), ttl);
    }

    /**
     * 동일 멤버의 중복 발급 요청을 Redis 단에서 차단한다.
     * SETNX 성공(true) = 최초 요청, 실패(false) = 중복 요청
     */
    public void acquireMemberLockOrThrow(Long eventId, Long memberId, Duration ttl) {
        String key = CacheKeys.couponMemberLock(eventId, memberId);
        boolean acquired = redisRepository.tryAcquire(key, ttl);
        if (!acquired) {
            throw new CoreException(ErrorType.ALREADY_ISSUED_COUPON);
        }
    }

    /**
     * 재고를 원자적으로 1 감소시킨다.
     * 반환값이 음수이면 재고가 소진된 것이므로 보상 후 예외를 던진다.
     */
    public void decrementStockOrThrow(Long eventId, Long memberId) {
        String stockKey = CacheKeys.couponStock(eventId);
        Long remaining = redisRepository.decrement(stockKey);

        if (remaining != null && remaining < 0)  {
            compensate(eventId, memberId);
            throw new CoreException(ErrorType.COUPON_SOLD_OUT);
        }
    }

    /**
     * DECR 결과가 음수일 때 Redis 상태를 원상복구한다.
     * - 감소된 재고 카운터를 다시 증가
     * - 설정된 멤버 락을 해제
     */
    private void compensate(Long eventId, Long memberId) {
        redisRepository.increment(CacheKeys.couponStock(eventId));
        redisRepository.delete(CacheKeys.couponMemberLock(eventId, memberId));
    }

}
