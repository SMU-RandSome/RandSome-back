package org.smu.randsome.randsomeback.domain.announcement.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.ControllerTestSupport;
import org.smu.randsome.randsomeback.domain.announcement.dto.response.AnnouncementItem;
import org.smu.randsome.randsomeback.security.annotation.TestMember;
import org.smu.randsome.randsomeback.utils.TestDateTimeUtils;
import org.springframework.http.HttpStatus;

class AnnouncementControllerTest extends ControllerTestSupport {

    // TODO: Jackson 직렬화 시 trailing zero 제거로 인한 createdAt 포맷 불일치 이슈 수정 필요
    @Disabled("createdAt 포맷 이슈 수정 필요")
    @Test
    @TestMember
    void 인증된_사용자가_공지사항_목록을_조회하면_200을_반환한다() {
        // given
        LocalDateTime now = TestDateTimeUtils.now();
        given(announcementService.findAnnouncements()).willReturn(List.of(
                AnnouncementItem.builder()
                        .id(1L)
                        .title("제목1")
                        .content("내용1")
                        .createdAt(now)
                        .build(),
                AnnouncementItem.builder()
                        .id(2L)
                        .title("제목2")
                        .content("내용2")
                        .createdAt(now.plusHours(2))
                        .build()
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
                .hasPathSatisfying("$.data[0].createdAt", v -> v.assertThat().isEqualTo(now.toString()))
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
