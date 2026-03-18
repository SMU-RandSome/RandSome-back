package org.smu.randsome.randsomeback.domain.announcement.service;

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
import org.smu.randsome.randsomeback.domain.announcement.implement.AnnouncementReader;
import org.smu.randsome.randsomeback.domain.member.entity.Member;

class AnnouncementServiceUnitTest extends UnitTestSupport {

    @InjectMocks
    AnnouncementService announcementService;

    @Mock
    AnnouncementReader announcementReader;

    @Test
    void 공지사항_목록을_조회하면_AnnouncementReader에_위임한다() {
        // given
        var admin = mock(Member.class);
        List<Announcement> announcements = List.of(
                Announcement.register(admin, "제목1", "내용1"),
                Announcement.register(admin, "제목2", "내용2")
        );
        given(announcementReader.findAnnouncements()).willReturn(announcements);

        // when
        List<Announcement> result = announcementService.findAnnouncements();

        // then
        assertThat(result).hasSize(2);
        verify(announcementReader).findAnnouncements();
    }

    @Test
    void 공지사항이_없으면_빈_목록을_반환한다() {
        // given
        given(announcementReader.findAnnouncements()).willReturn(List.of());

        // when
        List<Announcement> result = announcementService.findAnnouncements();

        // then
        assertThat(result).isEmpty();
        verify(announcementReader).findAnnouncements();
    }

}