package org.smu.randsome.randsomeback.domain.auth.implement.verificationcode;

import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.infrastructure.redis.RedisRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
class VerificationCodeStore {

    private static final String KEY_PREFIX = "verification_code:";
    private static final Duration TTL = Duration.ofMinutes(5);
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
        redisRepository.put(key(email), code, TTL);
    }

    String get(String email) {
        return redisRepository.get(key(email));
    }

    void remove(String email) {
        redisRepository.delete(key(email));
    }

    boolean removeIfPresent(String email, String expectedCode) {
        Long result = stringRedisTemplate.execute(
                REMOVE_IF_PRESENT_SCRIPT,
                List.of(key(email)),
                expectedCode
        );
        return Long.valueOf(1L).equals(result);
    }

    private String key(String email) {
        return KEY_PREFIX + email;
    }

}