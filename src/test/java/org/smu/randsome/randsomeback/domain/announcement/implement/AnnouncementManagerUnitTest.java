package org.smu.randsome.randsomeback.domain.announcement.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.announcement.dto.command.NewAnnouncement;
import org.smu.randsome.randsomeback.domain.announcement.entity.Announcement;
import org.smu.randsome.randsomeback.domain.announcement.repository.AnnouncementJpaRepository;
import org.smu.randsome.randsomeback.domain.member.enums.Role;
import org.smu.randsome.randsomeback.domain.member.implement.MemberReader;
import org.smu.randsome.randsomeback.domain.member.implement.MemberValidator;
import org.smu.randsome.randsomeback.fixture.MemberFixture;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;

class AnnouncementManagerUnitTest extends UnitTestSupport {

    @InjectMocks
    AnnouncementManager announcementManager;

    @Mock
    MemberReader memberReader;

    @Mock
    AnnouncementJpaRepository announcementJpaRepository;

    @Mock
    MemberValidator memberValidator;

    @Test
    void 공지사항을_등록하면_저장된_Announcement를_반환한다() {
        // given
        var admin = MemberFixture.create();
        admin.updateRole(Role.ROLE_ADMIN);

        given(memberReader.find(1L)).willReturn(admin);
        given(announcementJpaRepository.save(any(Announcement.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        var newAnnouncement = NewAnnouncement.builder()
                .title("공지사항 제목")
                .content("공지사항 내용")
                .build();

        // when
        Announcement result = announcementManager.register(1L, newAnnouncement);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("공지사항 제목");
        assertThat(result.getContent()).isEqualTo("공지사항 내용");
        assertThat(result.getAdmin()).isEqualTo(admin);
        verify(announcementJpaRepository).save(any(Announcement.class));
    }

    @Test
    void 존재하지_않는_관리자_ID이면_예외가_발생한다() {
        // given
        willThrow(new CoreException(ErrorType.NOT_FOUND_MEMBER))
                .given(memberReader).find(999L);

        var newAnnouncement = NewAnnouncement.builder()
                .title("제목")
                .content("내용")
                .build();

        // when & then
        assertThatThrownBy(() -> announcementManager.register(999L, newAnnouncement))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.NOT_FOUND_MEMBER.getMessage());
    }

    @Test
    void 관리자가_아닐_경우_예외를_반환한다() {
        var admin = MemberFixture.create();
        given(memberReader.find(1L)).willReturn(admin);
        willThrow(new CoreException(ErrorType.FORBIDDEN_ERROR)).given(memberValidator).validateAdmin(admin);

        var newAnnouncement = NewAnnouncement.builder()
                .title("공지사항 제목")
                .content("공지사항 내용")
                .build();

        // when
        assertThatThrownBy(() -> announcementManager.register(1L, newAnnouncement))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.FORBIDDEN_ERROR.getMessage());
    }

}