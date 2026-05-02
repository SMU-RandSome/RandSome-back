package org.smu.randsome.randsomeback.domain.coupon.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.Duration;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.admin.coupon.event.CouponEventActivatedEvent;
import org.smu.randsome.randsomeback.admin.coupon.event.CouponEventDeactivatedEvent;
import org.smu.randsome.randsomeback.admin.coupon.event.CouponEventSoldOutEvent;
import org.smu.randsome.randsomeback.global.config.CacheKeys;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.smu.randsome.randsomeback.global.support.notification.ErrorNotificationSender;
import org.smu.randsome.randsomeback.infrastructure.redis.RedisRepository;
import org.springframework.data.redis.RedisConnectionFailureException;

class CouponCacheManagerUnitTest extends UnitTestSupport {

    @InjectMocks
    CouponCacheManager couponCacheManager;

    @Mock
    RedisRepository redisRepository;

    @Mock
    ErrorNotificationSender errorNotificationSender;

    // ── onCouponEventActivated ───────────────────────────────────────

    @Test
    void 재고_초기화_시_올바른_키와_값으로_Redis에_저장한다() {
        // given
        Long eventId = 1L;
        int totalQuantity = 100;
        LocalDateTime activatedAt = LocalDateTime.now();
        CouponEventActivatedEvent event = new CouponEventActivatedEvent(
                eventId, totalQuantity, activatedAt, activatedAt.plusHours(1));

        // when
        couponCacheManager.onCouponEventActivated(event);

        // then
        verify(redisRepository).put(
                eq(CacheKeys.couponStock(eventId)),
                eq("100"),
                any(Duration.class)
        );
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
    void 재고가_남아있으면_감소에_성공하고_남은_재고를_반환한다() {
        // given
        Long eventId = 1L;
        Long memberId = 42L;

        given(redisRepository.decrementIfExists(CacheKeys.couponStock(eventId))).willReturn(1L);

        // when
        long remaining = couponCacheManager.decrementStockOrThrow(eventId, memberId);

        // then
        assertThat(remaining).isEqualTo(1L);
        verify(redisRepository, never()).increment(CacheKeys.couponStock(eventId));
        verify(redisRepository, never()).delete(CacheKeys.couponMemberLock(eventId, memberId));
    }

    @Test
    void 재고가_마지막_한_개일때_감소하면_0을_반환한다() {
        // given: decrementIfExists 결과 0 = 마지막 한 장 획득
        Long eventId = 1L;
        Long memberId = 42L;

        given(redisRepository.decrementIfExists(CacheKeys.couponStock(eventId))).willReturn(0L);

        // when
        long remaining = couponCacheManager.decrementStockOrThrow(eventId, memberId);

        // then
        assertThat(remaining).isZero();
        verify(redisRepository, never()).increment(CacheKeys.couponStock(eventId));
        verify(redisRepository, never()).delete(CacheKeys.couponMemberLock(eventId, memberId));
    }

    @Test
    void 재고_소진시_COUPON_SOLD_OUT_예외를_던지고_Redis_상태를_원복한다() {
        // given: decrementIfExists 결과 음수 = 재고 소진
        Long eventId = 1L;
        Long memberId = 42L;

        given(redisRepository.decrementIfExists(CacheKeys.couponStock(eventId))).willReturn(-1L);

        // when & then
        assertThatThrownBy(() -> couponCacheManager.decrementStockOrThrow(eventId, memberId))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.COUPON_SOLD_OUT.getMessage());

        // 보상 로직: 재고 복구 + 멤버 락 해제
        verify(redisRepository).increment(CacheKeys.couponStock(eventId));
        verify(redisRepository).delete(CacheKeys.couponMemberLock(eventId, memberId));
    }

    @Test
    void Redis_재고_키가_없으면_COUPON_EVENT_NOT_ACTIVE_예외를_던진다() {
        // given: decrementIfExists 결과 null = 키 없음 (Redis 재시작 등)
        Long eventId = 1L;
        Long memberId = 42L;

        given(redisRepository.decrementIfExists(CacheKeys.couponStock(eventId))).willReturn(null);

        // when & then
        assertThatThrownBy(() -> couponCacheManager.decrementStockOrThrow(eventId, memberId))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.COUPON_EVENT_NOT_ACTIVE.getMessage());

        // 보상 로직 실행되지 않음 (유령 키 생성 없음)
        verify(redisRepository, never()).increment(CacheKeys.couponStock(eventId));
        verify(redisRepository, never()).delete(CacheKeys.couponMemberLock(eventId, memberId));
    }

    // ── recoverFromActivationFailure ─────────────────────────────────

    @Test
    void 재시도_최종_실패_시_recover_메서드가_호출되고_알림이_발송된다() {
        // given: Redis 연결 실패
        Long eventId = 1L;
        int totalQuantity = 100;
        LocalDateTime activatedAt = LocalDateTime.now();
        LocalDateTime expiresAt = activatedAt.plusHours(1);
        CouponEventActivatedEvent event = new CouponEventActivatedEvent(eventId, totalQuantity, activatedAt, expiresAt);
        RedisConnectionFailureException cause = new RedisConnectionFailureException("Connection timeout");

        // when: recover 메서드 직접 호출 (재시도 실패 후 @Recover가 호출하는 시뮬레이션)
        assertThatCode(() -> couponCacheManager.recoverFromActivationFailure(cause, event))
                .doesNotThrowAnyException();

        // then: 로깅 + 에러 알림 발송 확인 (DB는 이미 커밋됨)
        verify(errorNotificationSender).sendErrorNotification(
                eq(String.format("쿠폰 재고 Redis 초기화 실패 - 쿠폰 ID: %d, 총 재고: %d, 만료 시각: %s, 원인: %s",
                        eventId, totalQuantity, expiresAt, "Connection timeout")),
                eq(cause)
        );
    }

    // ── onCouponEventSoldOut ─────────────────────────────────────────

    @Test
    void 재고_소진_시_올바른_키로_Redis에서_재고를_삭제한다() {
        // given
        Long eventId = 1L;
        CouponEventSoldOutEvent event = new CouponEventSoldOutEvent(eventId);

        // when
        couponCacheManager.onCouponEventSoldOut(event);

        // then
        verify(redisRepository).delete(eq(CacheKeys.couponStock(eventId)));
    }

    // ── recoverFromSoldOutFailure ────────────────────────────────────

    @Test
    void 재고_소진_재시도_최종_실패_시_recover_메서드가_호출되고_알림이_발송된다() {
        // given
        Long eventId = 1L;
        CouponEventSoldOutEvent event = new CouponEventSoldOutEvent(eventId);
        RedisConnectionFailureException cause = new RedisConnectionFailureException("Connection timeout");

        // when
        couponCacheManager.recoverFromSoldOutFailure(cause, event);

        // then
        verify(errorNotificationSender).sendErrorNotification(
                eq(String.format("쿠폰 재고 Redis 삭제 실패(재고 소진) - 쿠폰 이벤트 ID: %d, 원인: %s",
                        eventId, "Connection timeout")),
                eq(cause)
        );
    }

    // ── onCouponEventDeactivated ─────────────────────────────────────

    @Test
    void 비활성화_시_올바른_키로_Redis에서_재고를_삭제한다() {
        // given
        Long eventId = 1L;
        CouponEventDeactivatedEvent event = new CouponEventDeactivatedEvent(eventId);

        // when
        couponCacheManager.onCouponEventDeactivated(event);

        // then
        verify(redisRepository).delete(eq(CacheKeys.couponStock(eventId)));
    }

    // ── recoverFromDeactivationFailure ───────────────────────────────

    @Test
    void 비활성화_재시도_최종_실패_시_recover_메서드가_호출되고_알림이_발송된다() {
        // given: Redis 연결 실패
        Long eventId = 1L;
        CouponEventDeactivatedEvent event = new CouponEventDeactivatedEvent(eventId);
        RedisConnectionFailureException cause = new RedisConnectionFailureException("Connection timeout");

        // when: recover 메서드 직접 호출 (재시도 실패 후 @Recover가 호출하는 시뮬레이션)
        assertThatCode(() -> couponCacheManager.recoverFromDeactivationFailure(cause, event))
                .doesNotThrowAnyException();

        // then: 로깅 + 에러 알림 발송 확인 (DB는 이미 커밋됨)
        verify(errorNotificationSender).sendErrorNotification(
                eq(String.format("쿠폰 재고 Redis 삭제 실패 - 쿠폰 ID: %d, 원인: %s",
                        eventId, "Connection timeout")),
                eq(cause)
        );
    }

}