package org.smu.randsome.randsomeback.domain.announcement.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.announcement.dto.response.AnnouncementItem;
import org.smu.randsome.randsomeback.domain.announcement.implement.AnnouncementReader;

class AnnouncementServiceUnitTest extends UnitTestSupport {

    @InjectMocks
    AnnouncementService announcementService;

    @Mock
    AnnouncementReader announcementReader;

    @Test
    void 공지사항_목록을_조회하면_AnnouncementReader에_위임한다() {
        // given
        List<AnnouncementItem> announcements = List.of(
                AnnouncementItem.builder().id(1L).title("제목1").content("내용1").build(),
                AnnouncementItem.builder().id(2L).title("제목2").content("내용2").build()
        );
        given(announcementReader.findAnnouncements()).willReturn(announcements);

        // when
        List<AnnouncementItem> result = announcementService.findAnnouncements();

        // then
        assertThat(result).hasSize(2);
        verify(announcementReader).findAnnouncements();
    }

    @Test
    void 공지사항이_없으면_빈_목록을_반환한다() {
        // given
        given(announcementReader.findAnnouncements()).willReturn(List.of());

        // when
        List<AnnouncementItem> result = announcementService.findAnnouncements();

        // then
        assertThat(result).isEmpty();
        verify(announcementReader).findAnnouncements();
    }

}
