package org.smu.randsome.randsomeback.domain.announcement.implement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smu.randsome.randsomeback.domain.announcement.event.AnnouncementDeleteEvent;
import org.smu.randsome.randsomeback.domain.announcement.event.AnnouncementRegisteredEvent;
import org.smu.randsome.randsomeback.global.config.CacheKeys;
import org.smu.randsome.randsomeback.infrastructure.redis.RedisRepository;
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

    private final RedisRepository redisRepository;

    /**
     * 공지사항 등록 트랜잭션이 커밋된 뒤 ANNOUNCEMENTS 캐시를 삭제한다.
     *
     * @param event 공지사항 등록 이벤트
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void evictAnnouncementsCache(AnnouncementRegisteredEvent event) {
        redisRepository.delete(CacheKeys.ANNOUNCEMENTS);
        log.info("[AnnouncementCacheEvictor] 캐시 무효화 완료. announcementId={}", event.announcementId());
    }

    /**
     * 공지사항 삭제 트랜잭션이 커밋된 뒤 ANNOUNCEMENTS 캐시를 삭제한다.
     *
     * @param event 공지사항 삭제 이벤트
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void evictAnnouncementsCache(AnnouncementDeleteEvent event) {
        redisRepository.delete(CacheKeys.ANNOUNCEMENTS);
        log.info("[AnnouncementCacheEvictor] 캐시 무효화 완료. announcementId={}", event.announcementId());
    }

}