package org.smu.randsome.randsomeback.domain.announcement.implement;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.announcement.entity.Announcement;
import org.smu.randsome.randsomeback.domain.announcement.repository.AnnouncementJpaRepository;
import org.smu.randsome.randsomeback.global.config.CacheConfig;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class AnnouncementReader {

    private final AnnouncementJpaRepository announcementJpaRepository;

    // NOTE: 공지사항은 단기 이벤트에서 많지 않을 것으로 예상되어 페이징 처리 없이 전체 조회
    @Cacheable(cacheNames = CacheConfig.ANNOUNCEMENTS)
    @Transactional(readOnly = true)
    public List<Announcement> findAnnouncements() {
        return announcementJpaRepository.findAllByStatus(EntityStatus.ACTIVE);
    }

}
