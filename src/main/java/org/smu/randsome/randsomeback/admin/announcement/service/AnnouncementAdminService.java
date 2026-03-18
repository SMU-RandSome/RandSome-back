package org.smu.randsome.randsomeback.admin.announcement.service;

import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.announcement.entity.Announcement;
import org.smu.randsome.randsomeback.domain.announcement.implement.AnnouncementManager;
import org.smu.randsome.randsomeback.domain.announcement.service.command.NewAnnouncement;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AnnouncementAdminService {

    private final AnnouncementManager announcementManager;

    /**
     * 공지사항 등록
     *
     * @param adminId         공지사항을 등록하는 관리자 ID
     * @param newAnnouncement 등록할 공지사항 정보 return 등록된 공지사항 Id
     *
     */
    @Transactional
    public Announcement registerAnnouncement(Long adminId, NewAnnouncement newAnnouncement) {
        return announcementManager.register(adminId, newAnnouncement);
        // TODO: 공지사항 알림 전송
    }

}