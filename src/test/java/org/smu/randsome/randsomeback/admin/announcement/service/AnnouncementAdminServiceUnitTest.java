package org.smu.randsome.randsomeback.admin.announcement.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.announcement.dto.command.NewAnnouncement;
import org.smu.randsome.randsomeback.domain.announcement.entity.Announcement;
import org.smu.randsome.randsomeback.domain.announcement.event.AnnouncementRegisteredEvent;
import org.smu.randsome.randsomeback.domain.announcement.implement.AnnouncementManager;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.springframework.context.ApplicationEventPublisher;

class AnnouncementAdminServiceUnitTest extends UnitTestSupport {

    @InjectMocks
    AnnouncementAdminService announcementAdminService;

    @Mock
    AnnouncementManager announcementManager;

    @Mock
    ApplicationEventPublisher eventPublisher;

    @Test
    void 공지사항_등록에_성공하면_Announcement를_반환한다() {
        // given
        var announcement = mock(Announcement.class);
        given(announcement.getId()).willReturn(1L);

        var newAnnouncement = NewAnnouncement.builder()
                .title("공지사항 제목")
                .content("공지사항 내용")
                .build();

        given(announcementManager.register(1L, newAnnouncement)).willReturn(announcement);

        // when
        var result = announcementAdminService.registerAnnouncement(1L, newAnnouncement);

        // then
        assertThat(result.getId()).isEqualTo(1L);
        verify(announcementManager).register(1L, newAnnouncement);
        verify(eventPublisher).publishEvent(any(AnnouncementRegisteredEvent.class));
    }

    @Test
    void 존재하지_않는_관리자이면_예외가_발생한다() {
        // given
        var newAnnouncement = NewAnnouncement.builder()
                .title("제목")
                .content("내용")
                .build();

        willThrow(new CoreException(ErrorType.NOT_FOUND_MEMBER))
                .given(announcementManager).register(999L, newAnnouncement);

        // when & then
        assertThatThrownBy(() -> announcementAdminService.registerAnnouncement(999L, newAnnouncement))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.NOT_FOUND_MEMBER.getMessage());
        verify(eventPublisher, never()).publishEvent(any(AnnouncementRegisteredEvent.class));
    }

}