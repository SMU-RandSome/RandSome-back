package org.smu.randsome.randsomeback.domain.coupon.implement;

import java.time.Duration;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smu.randsome.randsomeback.admin.coupon.event.CouponEventActivatedEvent;
import org.smu.randsome.randsomeback.admin.coupon.event.CouponEventDeactivatedEvent;
import org.smu.randsome.randsomeback.admin.coupon.event.CouponEventSoldOutEvent;
import org.smu.randsome.randsomeback.global.config.CacheKeys;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.smu.randsome.randsomeback.global.support.notification.ErrorNotificationSender;
import org.smu.randsome.randsomeback.infrastructure.redis.RedisRepository;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponCacheManager {

    private final RedisRepository redisRepository;
    private final ErrorNotificationSender errorNotificationSender;

    /**
     * 이벤트 활성화 시 Redis에 재고 키를 초기화한다.
     * TTL은 이벤트 만료 시각까지의 남은 시간으로 설정한다.
     * activatedAt은 activate() 검증 시점과 동일하므로, TTL은 항상 양수임이 보장된다.
     * Redis 연결 실패 시 최대 3회(1s → 2s 간격) 재시도한다.
     */
    @Retryable(
            retryFor = RedisConnectionFailureException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCouponEventActivated(CouponEventActivatedEvent event) {
        String key = CacheKeys.couponStock(event.couponEventId());
        Duration ttl = Duration.between(event.activatedAt(), event.expiresAt());
        redisRepository.put(key, String.valueOf(event.totalQuantity()), ttl);
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
     * 키가 없으면(Redis 재시작·활성화 실패) 즉시 예외를 던진다.
     * 반환값이 음수이면 재고가 소진된 것이므로 보상 후 예외를 던진다.
     * DB 트랜잭션 롤백 시(Soft fail) Redis 상태를 원상복구하도록 보상 콜백을 등록한다.
     * Hard crash(OOM, kill -9) 시에는 콜백이 실행되지 않으므로, member lock의 짧은 TTL로 복구를 보완한다.
     */
    public long decrementStockOrThrow(Long eventId, Long memberId) {
        String key = CacheKeys.couponStock(eventId);
        Long remaining = redisRepository.decrementIfExists(key);

        if (remaining == null) {
            log.error("쿠폰 재고 Redis 키 없음 - eventId={}, 관리자 재동기화 필요", eventId);
            throw new CoreException(ErrorType.COUPON_EVENT_NOT_ACTIVE);
        }

        if (remaining < 0) {
            compensate(eventId, memberId);
            throw new CoreException(ErrorType.COUPON_SOLD_OUT);
        }

        registerRollbackCompensation(eventId, memberId);
        return remaining;
    }

    /**
     * DB 기준 남은 재고로 Redis 재고 키를 재동기화한다.
     * Redis 재시작 또는 활성화 실패로 키가 유실된 경우 관리자가 호출한다.
     * 이벤트가 이미 만료된 경우에는 재동기화를 생략한다.
     */
    public void syncStock(Long eventId, long remaining, LocalDateTime expiresAt) {
        Duration ttl = Duration.between(LocalDateTime.now(), expiresAt);
        if (ttl.isNegative() || ttl.isZero()) {
            log.warn("쿠폰 이벤트 이미 만료 - eventId={}, 재동기화 생략", eventId);
            return;
        }
        redisRepository.put(CacheKeys.couponStock(eventId), String.valueOf(remaining), ttl);
        log.info("쿠폰 재고 Redis 재동기화 완료: eventId={}, remaining={}", eventId, remaining);
    }

    /**
     * 재고 소진 시 Redis에 저장된 재고 캐시를 삭제한다.
     * Redis 연결 실패 시 최대 3회(1s → 2s 간격) 재시도한다.
     */
    @Retryable(
            retryFor = RedisConnectionFailureException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCouponEventSoldOut(CouponEventSoldOutEvent event) {
        String key = CacheKeys.couponStock(event.couponEventId());
        redisRepository.delete(key);
    }

    /**
     * 이벤트 비활성화 시 Redis에 저장된 재고 캐시를 삭제한다.
     * Redis 연결 실패 시 최대 3회(1s → 2s 간격) 재시도한다.
     */
    @Retryable(
            retryFor = RedisConnectionFailureException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCouponEventDeactivated(CouponEventDeactivatedEvent event) {
        String key = CacheKeys.couponStock(event.couponEventId());
        redisRepository.delete(key);
    }

    /**
     * onCouponEventActivated 재시도 최종 실패 시 호출된다.
     * DB는 이미 ACTIVE로 커밋되었으므로, Redis 불일치 상황을 로깅하고 모니터링을 위한 알림을 기록한다.
     * 관리자는 로그를 모니터링하여 수동으로 Redis를 초기화해야 한다.
     */
    @Recover
    public void recoverFromActivationFailure(RedisConnectionFailureException ex, CouponEventActivatedEvent event) {
        String logMessage = String.format("쿠폰 재고 Redis 초기화 실패 - 쿠폰 ID: %d, 총 재고: %d, 만료 시각: %s, 원인: %s",
                event.couponEventId(),
                event.totalQuantity(),
                event.expiresAt(),
                ex.getMessage()
        );
        log.error(logMessage, ex);
        errorNotificationSender.sendErrorNotification(logMessage, ex);
    }

    /**
     * onCouponEventSoldOut 재시도 최종 실패 시 호출된다.
     * DB는 이미 SOLD_OUT으로 커밋되었으므로, Redis 불일치 상황을 로깅하고 모니터링을 위한 알림을 기록한다.
     * 관리자는 로그를 모니터링하여 수동으로 Redis를 삭제해야 한다.
     */
    @Recover
    public void recoverFromSoldOutFailure(RedisConnectionFailureException ex, CouponEventSoldOutEvent event) {
        String logMessage = String.format("쿠폰 재고 Redis 삭제 실패(재고 소진) - 쿠폰 이벤트 ID: %d, 원인: %s",
                event.couponEventId(),
                ex.getMessage()
        );
        log.error(logMessage, ex);
        errorNotificationSender.sendErrorNotification(logMessage, ex);
    }

    /**
     * onCouponEventDeactivated 재시도 최종 실패 시 호출된다.
     * DB는 이미 ENDED로 커밋되었으므로, Redis 불일치 상황을 로깅하고 모니터링을 위한 알림을 기록한다.
     * 관리자는 로그를 모니터링하여 수동으로 Redis를 삭제해야 한다.
     */
    @Recover
    public void recoverFromDeactivationFailure(RedisConnectionFailureException ex, CouponEventDeactivatedEvent event) {
        String logMessage = String.format("쿠폰 재고 Redis 삭제 실패 - 쿠폰 ID: %d, 원인: %s",
                event.couponEventId(),
                ex.getMessage()
        );
        log.error(logMessage, ex);
        errorNotificationSender.sendErrorNotification(logMessage, ex);
    }

    private void registerRollbackCompensation(Long eventId, Long memberId) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != TransactionSynchronization.STATUS_ROLLED_BACK) {
                    return;
                }
                try {
                    compensate(eventId, memberId);
                    log.warn("쿠폰 발급 트랜잭션 롤백 감지 - Redis 보상 처리 완료: eventId={}, memberId={}", eventId, memberId);
                } catch (Exception e) {
                    log.error("쿠폰 발급 트랜잭션 롤백 보상 처리 중 실패: eventId={}, memberId={}", eventId, memberId, e);
                }
            }
        });
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