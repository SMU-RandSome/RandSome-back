package org.smu.randsome.randsomeback.domain.announcement.implement;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smu.randsome.randsomeback.domain.announcement.dto.response.AnnouncementItem;
import org.smu.randsome.randsomeback.domain.announcement.repository.AnnouncementJpaRepository;
import org.smu.randsome.randsomeback.global.config.CacheKeys;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.smu.randsome.randsomeback.infrastructure.redis.RedisRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 활성 상태 공지사항 조회를 담당한다.
 * Redis cache-aside 패턴으로 캐싱한다.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class AnnouncementReader {

    private final AnnouncementJpaRepository announcementJpaRepository;
    private final RedisRepository redisRepository;
    private final ObjectMapper objectMapper;

    private static final Duration ANNOUNCEMENTS_TTL = Duration.ofMinutes(10);

    /**
     * 활성 상태 공지사항을 전체 조회한다.
     * 캐시 히트 시 Redis에서 반환하고, 미스 시 DB를 조회한 뒤 캐시에 저장한다.
     *
     * @return 활성 공지사항 목록
     */
    @Transactional(readOnly = true)
    public List<AnnouncementItem> findAnnouncements() {
        return getFromCache().orElseGet(() -> {
            List<AnnouncementItem> result = announcementJpaRepository.findAllByStatusOrderByIdDesc(EntityStatus.ACTIVE)
                    .stream()
                    .map(AnnouncementItem::from)
                    .toList();
            putToCache(result);
            return result;
        });
    }

    private Optional<List<AnnouncementItem>> getFromCache() {
        String cached = redisRepository.get(CacheKeys.ANNOUNCEMENTS);
        if (cached == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(cached, new TypeReference<>() {}));
        } catch (JsonProcessingException e) {
            log.warn("[AnnouncementReader] 캐시 역직렬화 실패, DB 재조회. key={}", CacheKeys.ANNOUNCEMENTS, e);
            return Optional.empty();
        }
    }

    private void putToCache(List<AnnouncementItem> items) {
        try {
            redisRepository.put(CacheKeys.ANNOUNCEMENTS, objectMapper.writeValueAsString(items), ANNOUNCEMENTS_TTL);
        } catch (JsonProcessingException e) {
            log.warn("[AnnouncementReader] 캐시 저장 실패. key={}", CacheKeys.ANNOUNCEMENTS, e);
        }
    }

}