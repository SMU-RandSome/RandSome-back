package org.smu.randsome.randsomeback.global.config;

import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.redis.spring.RedisLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@EnableSchedulerLock(defaultLockAtMostFor = "5m")
@RequiredArgsConstructor
@Configuration
public class ShedLockConfig {

    private final RedisConnectionFactory redisConnectionFactory;

    @Bean
    public LockProvider lockProvider() {
        return new RedisLockProvider(redisConnectionFactory);
    }

}