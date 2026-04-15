package org.smu.randsome.randsomeback.admin.candidate.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.smu.randsome.randsomeback.ControllerTestSupport;
import org.smu.randsome.randsomeback.admin.candidate.dto.request.CandidateRejectRequest;
import org.smu.randsome.randsomeback.domain.candidate.dto.command.CandidateRegistrationSearchCondition;
import org.smu.randsome.randsomeback.domain.candidate.enums.CandidateRegistrationFilter;
import org.smu.randsome.randsomeback.global.support.response.Cursor;
import org.smu.randsome.randsomeback.global.support.response.CursorSlice;
import org.smu.randsome.randsomeback.security.annotation.TestAdmin;
import org.springframework.http.MediaType;

class CandidateAdminControllerTest extends ControllerTestSupport {

    @TestAdmin
    @Test
    void 관리자가_후보자_신청_승인을_한다() {
        // when & then
        assertThat(mvcTester.post().uri("/v1/admin/candidate-registrations/{candidateRegistrationId}/approve", 1L))
                .apply(print())
                .hasStatusOk();
    }

    @TestAdmin
    @Test
    void 관리자가_후보자_신청을_거절_한다() throws JsonProcessingException {
        // given
        var request = new CandidateRejectRequest("부적절한 지원입니다.");

        // when & then
        assertThat(mvcTester.post().uri("/v1/admin/candidate-registrations/{candidateRegistrationId}/reject", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .apply(print())
                .hasStatusOk();
    }

    @TestAdmin
    @Test
    void 관리자가_후보자_등록_목록을_조회한다() {
        // given
        given(candidateAdminService.findCandidates(any(), any()))
                .willReturn(CursorSlice.of(List.of(), null, false));

        // when & then
        assertThat(mvcTester.get().uri("/v1/admin/candidate-registrations"))
                .apply(print())
                .hasStatusOk();
    }

    @TestAdmin
    @Test
    void 필터와_키워드와_커서를_전달하면_서비스에_올바른_조건이_전달된다() {
        // given
        given(candidateAdminService.findCandidates(any(), any()))
                .willReturn(CursorSlice.of(List.of(), null, false));

        // when
        assertThat(mvcTester.get().uri("/v1/admin/candidate-registrations?filter=COMPLETED&keyword=홍길동&lastId=10&size=20"))
                .hasStatusOk();

        // then
        var conditionCaptor = ArgumentCaptor.forClass(CandidateRegistrationSearchCondition.class);
        var cursorCaptor = ArgumentCaptor.forClass(Cursor.class);
        then(candidateAdminService).should().findCandidates(conditionCaptor.capture(), cursorCaptor.capture());

        assertThat(conditionCaptor.getValue().filter()).isEqualTo(CandidateRegistrationFilter.COMPLETED);
        assertThat(conditionCaptor.getValue().keyword()).isEqualTo("홍길동");
        assertThat(cursorCaptor.getValue().lastCursorId()).isEqualTo(10L);
        assertThat(cursorCaptor.getValue().limit()).isEqualTo(20);
    }

}