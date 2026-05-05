package org.smu.randsome.randsomeback.domain.auth.implement.verificationcode;

import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.global.config.CacheKeys;
import org.smu.randsome.randsomeback.infrastructure.redis.RedisRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
class VerificationCodeStore {

    private static final Duration TTL = Duration.ofMinutes(5);
    private static final int MAX_FAIL_COUNT = 5;
    private static final RedisScript<Long> REMOVE_IF_PRESENT_SCRIPT = RedisScript.of(
            "local val = redis.call('GET', KEYS[1])\n"
                    + "if val == ARGV[1] then\n"
                    + "  redis.call('DEL', KEYS[1])\n"
                    + "  return 1\n"
                    + "end\n"
                    + "return 0",
            Long.class
    );

    private final RedisRepository redisRepository;
    private final StringRedisTemplate stringRedisTemplate;

    void put(String email, String code) {
        redisRepository.put(CacheKeys.verificationCode(email), code, TTL);
        redisRepository.delete(CacheKeys.verificationCodeFail(email));
    }

    String get(String email) {
        return redisRepository.get(CacheKeys.verificationCode(email));
    }

    void remove(String email) {
        redisRepository.delete(CacheKeys.verificationCode(email));
    }

    boolean removeIfPresent(String email, String expectedCode) {
        // Lua 스크립트를 사용하여 키의 값이 예상 코드와 일치하는 경우에만 키를 삭제하고, 그 결과로 1을 반환합니다. 그렇지 않으면 0을 반환합니다.
        Long result = stringRedisTemplate.execute(
                REMOVE_IF_PRESENT_SCRIPT,
                List.of(CacheKeys.verificationCode(email)),
                expectedCode
        );
        return Long.valueOf(1L).equals(result);
    }

    boolean isAttemptsExhausted(String email) {
        String count = redisRepository.get(CacheKeys.verificationCodeFail(email));
        return count != null && Integer.parseInt(count) >= MAX_FAIL_COUNT;
    }

    void incrementFailCount(String email) {
        String failKeyName = CacheKeys.verificationCodeFail(email);
        Long count = redisRepository.increment(failKeyName);
        if (count == 1L) {
            stringRedisTemplate.expire(failKeyName, TTL);
        }
        if (count >= MAX_FAIL_COUNT) {
            remove(email);
        }
    }

}