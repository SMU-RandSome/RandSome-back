package org.smu.randsome.randsomeback.admin.report.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.ControllerTestSupport;
import org.smu.randsome.randsomeback.admin.report.enums.AdminReportStatusFilter;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.report.entity.Report;
import org.smu.randsome.randsomeback.domain.report.enums.ReportReason;
import org.smu.randsome.randsomeback.domain.report.enums.ReportStatus;
import org.smu.randsome.randsomeback.domain.report.enums.ReportTargetType;
import org.smu.randsome.randsomeback.security.annotation.TestAdmin;
import org.springframework.http.HttpStatus;

class AdminReportControllerTest extends ControllerTestSupport {

    @TestAdmin
    @Test
    void PENDING_상태의_신고_목록을_조회한다() {
        // given
        List<Report> reports = createMockReports();
        given(adminReportService.findReportsByFilter(AdminReportStatusFilter.PENDING))
                .willReturn(reports);

        // when & then
        assertThat(mvcTester.get().uri("/v1/admin/reports")
                        .param("statusFilter", "PENDING"))
                .apply(print())
                .hasStatus(HttpStatus.OK.value())
                .bodyJson()
                .hasPathSatisfying("$.result", v -> v.assertThat().isEqualTo("SUCCESS"))
                .hasPathSatisfying("$.data.length()", v -> v.assertThat().isEqualTo(2))
                .hasPathSatisfying("$.data[0].reporterNickname", v -> v.assertThat().isEqualTo("남성#AAAA1111"))
                .hasPathSatisfying("$.data[0].reportedMemberNickname", v -> v.assertThat().isEqualTo("여성#BBBB2222"))
                .hasPathSatisfying("$.data[0].reason", v -> v.assertThat().isEqualTo("INAPPROPRIATE_CONTENT"))
                .hasPathSatisfying("$.data[0].reportStatus", v -> v.assertThat().isEqualTo("PENDING"));
    }

    @TestAdmin
    @Test
    void 신고_상세를_조회한다() {
        // given
        Report report = createMockReport(1L, ReportStatus.PENDING);
        given(adminReportService.findReport(1L)).willReturn(report);
        given(adminReportService.countActiveReportsForMember(2L)).willReturn(1L);

        // when & then
        assertThat(mvcTester.get().uri("/v1/admin/reports/1"))
                .apply(print())
                .hasStatus(HttpStatus.OK.value())
                .bodyJson()
                .hasPathSatisfying("$.result", v -> v.assertThat().isEqualTo("SUCCESS"))
                .hasPathSatisfying("$.data.reporterId", v -> v.assertThat().isEqualTo(1))
                .hasPathSatisfying("$.data.reportedMemberId", v -> v.assertThat().isEqualTo(2))
                .hasPathSatisfying("$.data.reason", v -> v.assertThat().isEqualTo("INAPPROPRIATE_CONTENT"))
                .hasPathSatisfying("$.data.reportStatus", v -> v.assertThat().isEqualTo("PENDING"))
                .hasPathSatisfying("$.data.activeReportCount", v -> v.assertThat().isEqualTo(1));
    }

    @TestAdmin
    @Test
    void 신고를_처리한다() {
        willDoNothing().given(adminReportService).resolveReport(1L);

        assertThat(mvcTester.post().uri("/v1/admin/reports/1/resolve"))
                .apply(print())
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.result", v -> v.assertThat().isEqualTo("SUCCESS"));
    }

    @TestAdmin
    @Test
    void 신고를_거절한다() {
        willDoNothing().given(adminReportService).rejectReport(1L);

        assertThat(mvcTester.post().uri("/v1/admin/reports/1/reject"))
                .apply(print())
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.result", v -> v.assertThat().isEqualTo("SUCCESS"));
    }

    private List<Report> createMockReports() {
        return List.of(
                createMockReport(1L, ReportStatus.PENDING),
                createMockReport(2L, ReportStatus.PENDING)
        );
    }

    private Report createMockReport(Long reportId, ReportStatus status) {
        Member reporter = mock(Member.class);
        given(reporter.getId()).willReturn(1L);
        given(reporter.getNickname()).willReturn("남성#AAAA1111");

        Member reportedMember = mock(Member.class);
        given(reportedMember.getId()).willReturn(2L);
        given(reportedMember.getNickname()).willReturn("여성#BBBB2222");

        Report report = mock(Report.class);
        given(report.getId()).willReturn(reportId);
        given(report.getReporter()).willReturn(reporter);
        given(report.getReportedMember()).willReturn(reportedMember);
        given(report.getTargetType()).willReturn(ReportTargetType.MATCHING_RESULT);
        given(report.getTargetId()).willReturn(10L);
        given(report.getReason()).willReturn(ReportReason.INAPPROPRIATE_CONTENT);
        given(report.getDescription()).willReturn("부적절한 프로필입니다.");
        given(report.getReportStatus()).willReturn(status);
        given(report.getCreatedAt()).willReturn(LocalDateTime.of(2026, 4, 14, 12, 0));

        return report;
    }
}
