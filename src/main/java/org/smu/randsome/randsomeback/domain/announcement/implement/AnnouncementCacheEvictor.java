package org.smu.randsome.randsomeback.domain.announcement.implement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smu.randsome.randsomeback.domain.announcement.event.AnnouncementRegisteredEvent;
import org.smu.randsome.randsomeback.global.config.CacheConfig;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 공지사항 변경 이벤트를 수신해 공지사항 캐시를 무효화한다.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class AnnouncementCacheEvictor {

    private final CacheManager cacheManager;

    /**
     * 공지사항 등록 트랜잭션이 커밋된 뒤 ANNOUNCEMENTS 캐시 전체를 비운다.
     *
     * @param event 공지사항 등록 이벤트
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void evictAnnouncementsCache(AnnouncementRegisteredEvent event) {
        log.info("[AnnouncementCacheEvictor] AFTER_COMMIT 이벤트 수신, 캐시 무효화 시작");
        Cache cache = cacheManager.getCache(CacheConfig.ANNOUNCEMENTS);
        if (cache != null) {
            cache.clear();
            log.info("[AnnouncementCacheEvictor] 캐시 무효화, announcementId={}", event.announcementId());
        }
    }

}