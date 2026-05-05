package org.smu.randsome.randsomeback.domain.announcement.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;

class AnnouncementTest extends UnitTestSupport {

    @Test
    void 공지사항을_등록하면_필드가_올바르게_설정된다() {
        // given
        Member admin = mock(Member.class);
        String title = "공지사항 제목";
        String content = "공지사항 내용";

        // when
        Announcement announcement = Announcement.register(admin, title, content);

        // then
        assertThat(announcement.getAdmin()).isEqualTo(admin);
        assertThat(announcement.getTitle()).isEqualTo(title);
        assertThat(announcement.getContent()).isEqualTo(content);
        assertThat(announcement.getStatus()).isEqualTo(EntityStatus.ACTIVE);
    }

    @Test
    void admin이_null이면_NullPointerException이_발생한다() {
        assertThatThrownBy(() -> Announcement.register(null, "제목", "내용"))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void title이_null이면_NullPointerException이_발생한다() {
        // given
        Member admin = mock(Member.class);

        // when & then
        assertThatThrownBy(() -> Announcement.register(admin, null, "내용"))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void content가_null이면_NullPointerException이_발생한다() {
        // given
        Member admin = mock(Member.class);

        // when & then
        assertThatThrownBy(() -> Announcement.register(admin, "제목", null))
                .isInstanceOf(NullPointerException.class);
    }

}
