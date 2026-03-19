package org.smu.randsome.randsomeback.domain.announcement.implement;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.announcement.dto.response.AnnouncementItem;
import org.smu.randsome.randsomeback.domain.announcement.entity.Announcement;
import org.smu.randsome.randsomeback.domain.announcement.repository.AnnouncementJpaRepository;
import org.smu.randsome.randsomeback.global.config.CacheConfig;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 활성 상태 공지사항 조회를 담당한다.
 */
@RequiredArgsConstructor
@Component
public class AnnouncementReader {

    private final AnnouncementJpaRepository announcementJpaRepository;

    /**
     * 활성 상태 공지사항을 전체 조회한다.
     * 공지사항 수가 크지 않다는 가정 하에 페이징 없이 캐시한다.
     *
     * @return 활성 공지사항 목록
     */
    @Cacheable(cacheNames = CacheConfig.ANNOUNCEMENTS)
    @Transactional(readOnly = true)
    public List<AnnouncementItem> findAnnouncements() {
        List<Announcement> announcements = announcementJpaRepository.findAllByStatus(EntityStatus.ACTIVE);

        return announcements.stream()
                .map(AnnouncementItem::from)
                .toList();
    }

}