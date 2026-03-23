package org.smu.randsome.randsomeback.admin.announcement.service;

import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.announcement.dto.command.NewAnnouncement;
import org.smu.randsome.randsomeback.domain.announcement.entity.Announcement;
import org.smu.randsome.randsomeback.domain.announcement.implement.AnnouncementManager;
import org.smu.randsome.randsomeback.domain.announcement.event.AnnouncementRegisteredEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 관리자 공지사항 유스케이스를 조율한다.
 */
@RequiredArgsConstructor
@Service
public class AnnouncementAdminService {

    private final AnnouncementManager announcementManager;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 공지사항을 등록하고 후처리를 위한 이벤트를 발행한다.
     *
     * @param adminId 공지사항을 등록하는 관리자 ID
     * @param newAnnouncement 등록할 공지사항 정보
     * @return 등록된 공지사항
     *
     */
    @Transactional
    public Announcement registerAnnouncement(Long adminId, NewAnnouncement newAnnouncement) {
        Announcement announcement = announcementManager.register(adminId, newAnnouncement);

        // NOTE: 공지사항 등록 후 캐시 무효화 및 알림 전송
        eventPublisher.publishEvent(new AnnouncementRegisteredEvent(announcement.getId()));

        return announcement;
    }

}