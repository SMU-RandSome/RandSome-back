package org.smu.randsome.randsomeback.domain.announcement.implement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smu.randsome.randsomeback.domain.announcement.dto.command.NewAnnouncement;
import org.smu.randsome.randsomeback.domain.announcement.entity.Announcement;
import org.smu.randsome.randsomeback.domain.announcement.repository.AnnouncementJpaRepository;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.implement.MemberReader;
import org.smu.randsome.randsomeback.domain.member.implement.MemberValidator;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class AnnouncementManager {

    private final MemberReader memberReader;
    private final AnnouncementJpaRepository announcementJpaRepository;
    private final MemberValidator memberValidator;

    /**
     * 관리자 권한을 검증한 뒤 공지사항을 등록한다.
     *
     * @param adminId 공지사항을 등록하는 관리자 ID
     * @param newAnnouncement 등록할 공지사항 정보
     * @return 등록된 공지사항
     */
    public Announcement register(Long adminId, NewAnnouncement newAnnouncement) {
        Member admin = memberReader.find(adminId);

        memberValidator.validateAdmin(admin);

        Announcement announcement = announcementJpaRepository.save(Announcement.register(
                admin,
                newAnnouncement.title(),
                newAnnouncement.content()
        ));

        log.info("[AnnouncementManager] 공지사항 등록. announcementId={}", announcement.getId());

        return announcement;
    }

}