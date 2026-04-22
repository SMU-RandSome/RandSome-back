package org.smu.randsome.randsomeback.infrastructure.redis;

import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisRepository {

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

    public void delete(String key) {
        stringRedisTemplate.delete(key);
    }

    // NOTE: 있는 키에 대해서는 false를 반환하므로, 동일한 키로 중복 시도를 방지하는 용도로 사용할 수 있다.
    public boolean tryAcquire(String key, Duration ttl) {
        Boolean acquired = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", ttl);
        return Boolean.TRUE.equals(acquired);
    }

    // NOTE: DECR은 원자 연산이므로 동시 요청이 몰려도 값이 정확히 1씩 감소한다.
    // 반환값이 음수이면 재고가 이미 소진된 것이다.
    public Long decrement(String key) {
        return stringRedisTemplate.opsForValue().decrement(key);
    }

    public Long increment(String key) {
        return stringRedisTemplate.opsForValue().increment(key);
    }

}