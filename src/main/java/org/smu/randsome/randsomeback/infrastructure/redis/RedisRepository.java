package org.smu.randsome.randsomeback.infrastructure.redis;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisRepository {

    private static final RedisScript<Long> DECREMENT_IF_EXISTS = RedisScript.of(
            "if redis.call('EXISTS', KEYS[1]) == 0 then return nil end return redis.call('DECR', KEYS[1])",
            Long.class
    );
    private final StringRedisTemplate stringRedisTemplate;

    public void put(String key, String value, Duration ttl) {
        stringRedisTemplate.opsForValue().set(key, value, ttl);
    }

    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    public List<String> mget(List<String> keys) {
        return stringRedisTemplate.opsForValue().multiGet(keys);
    }

    /**
     * 여러 키-값 쌍을 Redis Pipeline으로 한 번의 네트워크 왕복에 저장한다.
     * 각 키에 동일한 TTL이 적용된다.
     */
    public void pipelinePut(Map<String, String> entries, Duration ttl) {
        stringRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            entries.forEach((key, value) ->
                    connection.stringCommands().setEx(key.getBytes(), ttl.getSeconds(), value.getBytes()));
            return null;
        });
    }

    public void delete(String key) {
        stringRedisTemplate.delete(key);
    }
    // NOTE: 있는 키에 대해서는 false를 반환하므로, 동일한 키로 중복 시도를 방지하는 용도로 사용할 수 있다.

    public boolean tryAcquire(String key, Duration ttl) {
        Boolean acquired = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", ttl);
        return Boolean.TRUE.equals(acquired);
    }

    // NOTE: 키가 존재하면 DECR 결과(Long)를 반환하고, 없으면 null을 반환한다.
    // EXISTS + DECR을 Lua 스크립트로 원자적으로 실행해 유령 키 생성을 방지한다.
    public Long decrementIfExists(String key) {
        return stringRedisTemplate.execute(DECREMENT_IF_EXISTS, List.of(key));
    }

    public Long increment(String key) {
        return stringRedisTemplate.opsForValue().increment(key);
    }

}