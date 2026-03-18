package org.smu.randsome.randsomeback.domain.announcement.implement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smu.randsome.randsomeback.domain.announcement.entity.Announcement;
import org.smu.randsome.randsomeback.domain.announcement.repository.AnnouncementJpaRepository;
import org.smu.randsome.randsomeback.domain.announcement.service.command.NewAnnouncement;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.implement.MemberReader;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class AnnouncementManager {

    private final MemberReader memberReader;
    private final AnnouncementJpaRepository announcementJpaRepository;

    public Announcement register(Long adminId, NewAnnouncement newAnnouncement) {
        Member admin = memberReader.find(adminId);

        validateAdmin(admin);

        Announcement announcement = announcementJpaRepository.save(Announcement.register(
                admin,
                newAnnouncement.title(),
                newAnnouncement.content()
        ));

        log.info("[AnnouncementManager] 공지사항 등록. announcementId={}", announcement.getId());

        return announcement;
    }

    private void validateAdmin(Member admin) {
        if (!admin.isAdmin()) {
            throw new CoreException(ErrorType.FORBIDDEN_ERROR);
        }
    }

}