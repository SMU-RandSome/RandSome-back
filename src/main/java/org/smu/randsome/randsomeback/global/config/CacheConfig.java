package org.smu.randsome.randsomeback.global.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.cache.CaffeineCacheMetrics;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@EnableCaching
@RequiredArgsConstructor
@Configuration
public class CacheConfig {

    public static final String ANNOUNCEMENTS = "announcements";

    private static final int ANNOUNCEMENTS_MAX_SIZE = 1;
    private static final long ANNOUNCEMENTS_TTL_MINUTES = 10;

    private final MeterRegistry meterRegistry;

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        Cache<Object, Object> announcementsCache = buildCache(
                Duration.ofMinutes(ANNOUNCEMENTS_TTL_MINUTES), ANNOUNCEMENTS_MAX_SIZE);

        cacheManager.registerCustomCache(ANNOUNCEMENTS, announcementsCache);
        CaffeineCacheMetrics.monitor(meterRegistry, announcementsCache, ANNOUNCEMENTS);

        log.info("[CacheConfig] Caffeine 캐시 설정 완료");
        return cacheManager;
    }

    private Cache<Object, Object> buildCache(Duration ttl, int maxSize) {
        return Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(ttl)
                .recordStats()
                .build();
    }
}
