package org.smu.randsome.randsomeback.domain.announcement.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.announcement.entity.Announcement;
import org.smu.randsome.randsomeback.domain.announcement.repository.AnnouncementJpaRepository;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;

class AnnouncementReaderUnitTest extends UnitTestSupport {

    @InjectMocks
    AnnouncementReader announcementReader;

    @Mock
    AnnouncementJpaRepository announcementJpaRepository;

    @Test
    void ACTIVE_상태의_공지사항_목록을_반환한다() {
        // given
        var admin = mock(Member.class);
        var a1 = Announcement.register(admin, "제목1", "내용1");
        var a2 = Announcement.register(admin, "제목2", "내용2");
        given(announcementJpaRepository.findAllByStatus(EntityStatus.ACTIVE)).willReturn(List.of(a1, a2));

        // when
        List<Announcement> result = announcementReader.findAnnouncements();

        // then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(a1, a2);
        verify(announcementJpaRepository).findAllByStatus(EntityStatus.ACTIVE);
    }

    @Test
    void 공지사항이_없으면_빈_목록을_반환한다() {
        // given
        given(announcementJpaRepository.findAllByStatus(EntityStatus.ACTIVE)).willReturn(List.of());

        // when
        List<Announcement> result = announcementReader.findAnnouncements();

        // then
        assertThat(result).isEmpty();
    }

}
