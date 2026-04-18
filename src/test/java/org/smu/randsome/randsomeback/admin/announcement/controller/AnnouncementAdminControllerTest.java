package org.smu.randsome.randsomeback.admin.announcement.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.ControllerTestSupport;
import org.smu.randsome.randsomeback.admin.announcement.dto.request.AnnouncementRegisterRequest;
import org.smu.randsome.randsomeback.domain.announcement.entity.Announcement;
import org.smu.randsome.randsomeback.security.annotation.TestAdmin;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

class AnnouncementAdminControllerTest extends ControllerTestSupport {

    @Test
    @TestAdmin
    void 유효한_요청이면_201과_공지사항_ID를_반환한다() throws Exception {
        // given
        Announcement announcement = mock(Announcement.class);
        given(announcement.getId()).willReturn(1L);
        given(announcementAdminService.registerAnnouncement(eq(1L), any()))
                .willReturn(announcement);

        var request = new AnnouncementRegisterRequest("공지사항 제목", "공지사항 내용");

        // when & then
        assertThat(mvcTester.post().uri("/v1/admin/announcements")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .apply(print())
                .hasStatus(HttpStatus.CREATED.value())
                .bodyJson()
                .hasPathSatisfying("$.result", v -> v.assertThat().isEqualTo("SUCCESS"))
                .hasPathSatisfying("$.data", v -> v.assertThat().isEqualTo(1))
                .hasPathSatisfying("$.error", v -> v.assertThat().isNull());
    }

    @Test
    @TestAdmin
    void 제목이_비어있으면_400을_반환한다() throws Exception {
        // given
        var request = new AnnouncementRegisterRequest("", "내용");

        // when & then
        assertThat(mvcTester.post().uri("/v1/admin/announcements")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .apply(print())
                .hasStatus(HttpStatus.BAD_REQUEST.value());
    }

}
