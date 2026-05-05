package org.smu.randsome.randsomeback.domain.member.implement;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.global.config.CacheKeys;
import org.smu.randsome.randsomeback.global.jwt.enums.TokenExpiration;
import org.smu.randsome.randsomeback.infrastructure.redis.RedisRepository;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class SuspensionManager {

    private static final Duration SUSPENSION_TTL = Duration.ofMillis(TokenExpiration.ACCESS_TOKEN.getExpirationTime());

    private final RedisRepository redisRepository;

    public void suspend(Long memberId) {
        redisRepository.put(CacheKeys.suspension(memberId), "true", SUSPENSION_TTL);
    }

    public void restore(Long memberId) {
        redisRepository.delete(CacheKeys.suspension(memberId));
    }

    public boolean isSuspended(Long memberId) {
        return "true".equals(redisRepository.get(CacheKeys.suspension(memberId)));
    }

}
