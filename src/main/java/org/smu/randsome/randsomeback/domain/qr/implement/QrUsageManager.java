package org.smu.randsome.randsomeback.domain.qr.implement;

import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.global.jwt.enums.TokenExpiration;
import org.smu.randsome.randsomeback.infrastructure.redis.RedisRepository;
import org.springframework.stereotype.Component;

/**
 * QR 토큰의 일회용 소비 상태와 회원별 활성 QR을 Redis로 관리하는 컴포넌트.
 *
 * <p>Redis 키 구조:
 * <ul>
 *   <li>{@code qr:used:{jti}} - 소비된 토큰 마킹 (TTL: 30초)</li>
 *   <li>{@code qr:active:{memberId}} - 회원의 현재 활성 QR jti (TTL: 30초)</li>
 * </ul>
 *
 * <p>회원당 하나의 유효한 QR만 존재하도록 보장한다.
 * 새 QR 발급 시 기존 활성 jti를 {@code qr:used:} 로 마킹하여 이전 QR을 즉시 무효화한다.
 */
@RequiredArgsConstructor
@Component
public class QrUsageManager {

    private static final String USED_KEY_PREFIX = "qr:used:";
    private static final String ACTIVE_KEY_PREFIX = "qr:active:";
    private static final Duration TTL = Duration.ofMillis(TokenExpiration.QR_TOKEN.getExpirationTime());

    private final RedisRepository redisRepository;

    /**
     * jti를 원자적으로 소비한다.
     *
     * <p>{@code setIfAbsent} 기반으로 동작하므로 동시 요청이 들어와도
     * 최초 1회만 성공하고 이후 요청은 실패한다.
     *
     * @param jti 소비할 토큰 고유 식별자
     * @return 처음 소비한 경우 {@code true}, 이미 소비된 경우 {@code false}
     */
    public boolean tryConsume(String jti) {
        return redisRepository.tryAcquire(USED_KEY_PREFIX + jti, TTL);
    }

    /**
     * jti를 사용 완료 상태로 즉시 마킹한다. (이전 QR 무효화 용도)
     *
     * @param jti 마킹할 토큰 고유 식별자
     */
    public void markAsUsed(String jti) {
        redisRepository.put(USED_KEY_PREFIX + jti, "1", TTL);
    }

    /**
     * 회원의 현재 활성 QR jti를 조회한다.
     *
     * @param memberId 조회할 회원 ID
     * @return 활성 jti가 있으면 {@link Optional} 에 담아 반환, 없으면 {@link Optional#empty()}
     */
    public Optional<String> findActiveJti(Long memberId) {
        return Optional.ofNullable(redisRepository.get(ACTIVE_KEY_PREFIX + memberId));
    }

    /**
     * 회원의 활성 QR jti를 등록한다.
     *
     * @param memberId 등록할 회원 ID
     * @param jti      활성으로 등록할 토큰 고유 식별자
     */
    public void registerActiveJti(Long memberId, String jti) {
        redisRepository.put(ACTIVE_KEY_PREFIX + memberId, jti, TTL);
    }

}