package org.smu.randsome.randsomeback.domain.announcement.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.ControllerTestSupport;
import org.smu.randsome.randsomeback.domain.announcement.entity.Announcement;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.security.annotation.TestMember;
import org.springframework.http.HttpStatus;

class AnnouncementControllerTest extends ControllerTestSupport {

    @Test
    @TestMember
    void 인증된_사용자가_공지사항_목록을_조회하면_200을_반환한다() {
        // given
        var admin = mock(Member.class);
        given(announcementService.findAnnouncements()).willReturn(List.of(
                Announcement.register(admin, "제목1", "내용1"),
                Announcement.register(admin, "제목2", "내용2")
        ));

        // when & then
        assertThat(mvcTester.get().uri("/v1/announcements"))
                .apply(print())
                .hasStatus(HttpStatus.OK.value())
                .bodyJson()
                .hasPathSatisfying("$.result", v -> v.assertThat().isEqualTo("SUCCESS"))
                .hasPathSatisfying("$.data.length()", v -> v.assertThat().isEqualTo(2))
                .hasPathSatisfying("$.data[0].title", v -> v.assertThat().isEqualTo("제목1"))
                .hasPathSatisfying("$.data[0].content", v -> v.assertThat().isEqualTo("내용1"))
                .hasPathSatisfying("$.data[1].title", v -> v.assertThat().isEqualTo("제목2"));
    }

    @Test
    @TestMember
    void 공지사항이_없으면_빈_배열을_반환한다() {
        // given
        given(announcementService.findAnnouncements()).willReturn(List.of());

        // when & then
        assertThat(mvcTester.get().uri("/v1/announcements"))
                .apply(print())
                .hasStatus(HttpStatus.OK.value())
                .bodyJson()
                .hasPathSatisfying("$.result", v -> v.assertThat().isEqualTo("SUCCESS"))
                .hasPathSatisfying("$.data.length()", v -> v.assertThat().isEqualTo(0));
    }

    @Test
    void 권한이_없는_사용자가_공지사항을_조회하면_403을_반환한다() {
        assertThat(mvcTester.get().uri("/v1/announcements"))
                .apply(print())
                .hasStatus(HttpStatus.FORBIDDEN.value());
    }

}