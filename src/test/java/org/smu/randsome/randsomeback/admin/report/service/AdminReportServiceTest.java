package org.smu.randsome.randsomeback.admin.report.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.admin.report.enums.AdminReportStatusFilter;
import org.smu.randsome.randsomeback.domain.member.implement.MemberManager;
import org.smu.randsome.randsomeback.domain.report.entity.Report;
import org.smu.randsome.randsomeback.domain.report.implement.ReportManager;
import org.smu.randsome.randsomeback.domain.report.implement.ReportReader;
import org.smu.randsome.randsomeback.domain.report.enums.ReportStatus;

class AdminReportServiceTest extends UnitTestSupport {

    @InjectMocks
    AdminReportService adminReportService;

    @Mock
    ReportReader reportReader;

    @Mock
    ReportManager reportManager;

    @Mock
    MemberManager memberManager;

    @Test
    void PENDING_필터로_신고_목록을_조회한다() {
        // given
        List<Report> reports = List.of(mock(Report.class), mock(Report.class));
        given(reportReader.findAllByStatuses(AdminReportStatusFilter.PENDING.getStatuses()))
                .willReturn(reports);

        // when
        List<Report> result = adminReportService.findReportsByFilter(AdminReportStatusFilter.PENDING);

        // then
        assertThat(result).hasSize(2);
    }

    @Test
    void IN_REVIEW_필터로_신고_목록을_조회한다() {
        // given
        List<Report> reports = List.of(mock(Report.class));
        given(reportReader.findAllByStatuses(AdminReportStatusFilter.IN_REVIEW.getStatuses()))
                .willReturn(reports);

        // when
        List<Report> result = adminReportService.findReportsByFilter(AdminReportStatusFilter.IN_REVIEW);

        // then
        assertThat(result).hasSize(1);
    }

    @Test
    void COMPLETED_필터는_RESOLVED와_REJECTED를_모두_반환한다() {
        // given
        assertThat(AdminReportStatusFilter.COMPLETED.getStatuses())
                .containsExactlyInAnyOrder(ReportStatus.RESOLVED, ReportStatus.REJECTED);
    }

    @Test
    void 신고_단건을_조회한다() {
        // given
        Report report = mock(Report.class);
        given(reportReader.find(1L)).willReturn(report);

        // when
        Report result = adminReportService.findReport(1L);

        // then
        assertThat(result).isEqualTo(report);
    }

    @Test
    void 피신고자의_활성_신고_수를_조회한다() {
        // given
        given(reportReader.countActiveReportsByReportedMember(2L)).willReturn(2L);

        // when
        long count = adminReportService.countActiveReportsForMember(2L);

        // then
        assertThat(count).isEqualTo(2L);
    }

    @Test
    void 신고를_처리한다() {
        // when
        adminReportService.resolveReport(1L);

        // then
        then(reportManager).should().markAsResolved(1L);
    }

    @Test
    void 신고를_거절한다() {
        // when
        adminReportService.rejectReport(1L);

        // then
        then(reportManager).should().markAsRejected(1L);
    }

    @Test
    void 정지된_회원을_복구한다() {
        // when
        adminReportService.restoreMember(1L);

        // then
        then(memberManager).should().restore(1L);
    }
}
