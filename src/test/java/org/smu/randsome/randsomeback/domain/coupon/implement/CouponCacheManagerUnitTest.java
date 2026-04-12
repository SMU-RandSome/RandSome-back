package org.smu.randsome.randsomeback.domain.coupon.implement;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.global.config.CacheKeys;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.smu.randsome.randsomeback.infrastructure.redis.RedisRepository;

class CouponCacheManagerUnitTest extends UnitTestSupport {

    @InjectMocks
    CouponCacheManager couponCacheManager;

    @Mock
    RedisRepository redisRepository;

    // ── initializeStock ──────────────────────────────────────────────

    @Test
    void 재고_초기화_시_올바른_키와_값으로_Redis에_저장한다() {
        // given
        Long eventId = 1L;
        int totalQuantity = 100;
        Duration ttl = Duration.ofHours(1);

        // when
        couponCacheManager.initializeStock(eventId, totalQuantity, ttl);

        // then
        verify(redisRepository).put(CacheKeys.couponStock(eventId), "100", ttl);
    }

    // ── acquireMemberLockOrThrow ──────────────────────────────────────

    @Test
    void 최초_요청이면_멤버_락_획득에_성공하고_예외가_발생하지_않는다() {
        // given
        Long eventId = 1L;
        Long memberId = 42L;
        Duration ttl = Duration.ofHours(1);

        given(redisRepository.tryAcquire(CacheKeys.couponMemberLock(eventId, memberId), ttl))
                .willReturn(true);

        // when & then
        assertThatCode(() -> couponCacheManager.acquireMemberLockOrThrow(eventId, memberId, ttl))
                .doesNotThrowAnyException();
    }

    @Test
    void 동일_멤버가_중복_요청하면_ALREADY_ISSUED_COUPON_예외가_발생한다() {
        // given
        Long eventId = 1L;
        Long memberId = 42L;
        Duration ttl = Duration.ofHours(1);

        given(redisRepository.tryAcquire(CacheKeys.couponMemberLock(eventId, memberId), ttl))
                .willReturn(false);

        // when & then
        assertThatThrownBy(() -> couponCacheManager.acquireMemberLockOrThrow(eventId, memberId, ttl))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.ALREADY_ISSUED_COUPON.getMessage());
    }

    // ── decrementStockOrThrow ────────────────────────────────────────

    @Test
    void 재고가_남아있으면_감소에_성공하고_예외가_발생하지_않는다() {
        // given
        Long eventId = 1L;
        Long memberId = 42L;

        given(redisRepository.decrement(CacheKeys.couponStock(eventId))).willReturn(1L);

        // when & then
        assertThatCode(() -> couponCacheManager.decrementStockOrThrow(eventId, memberId))
                .doesNotThrowAnyException();

        verify(redisRepository, never()).increment(CacheKeys.couponStock(eventId));
        verify(redisRepository, never()).delete(CacheKeys.couponMemberLock(eventId, memberId));
    }

    @Test
    void 재고가_마지막_한_개일때_감소하면_성공하고_예외가_발생하지_않는다() {
        // given: decrement 결과 0 = 마지막 한 장 획득
        Long eventId = 1L;
        Long memberId = 42L;

        given(redisRepository.decrement(CacheKeys.couponStock(eventId))).willReturn(0L);

        // when & then
        assertThatCode(() -> couponCacheManager.decrementStockOrThrow(eventId, memberId))
                .doesNotThrowAnyException();

        verify(redisRepository, never()).increment(CacheKeys.couponStock(eventId));
        verify(redisRepository, never()).delete(CacheKeys.couponMemberLock(eventId, memberId));
    }

    @Test
    void 재고_소진시_COUPON_SOLD_OUT_예외를_던지고_Redis_상태를_원복한다() {
        // given: decrement 결과 음수 = 재고 소진
        Long eventId = 1L;
        Long memberId = 42L;

        given(redisRepository.decrement(CacheKeys.couponStock(eventId))).willReturn(-1L);

        // when & then
        assertThatThrownBy(() -> couponCacheManager.decrementStockOrThrow(eventId, memberId))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.COUPON_SOLD_OUT.getMessage());

        // 보상 로직: 재고 복구 + 멤버 락 해제
        verify(redisRepository).increment(CacheKeys.couponStock(eventId));
        verify(redisRepository).delete(CacheKeys.couponMemberLock(eventId, memberId));
    }

}