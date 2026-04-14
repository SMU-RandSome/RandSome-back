package org.smu.randsome.randsomeback.domain.report.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingApplication;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingResult;
import org.smu.randsome.randsomeback.domain.matching.implement.MatchingReader;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.report.dto.command.NewReport;
import org.smu.randsome.randsomeback.domain.report.entity.Report;
import org.smu.randsome.randsomeback.domain.report.enums.ReportReason;
import org.smu.randsome.randsomeback.domain.report.enums.ReportTargetType;
import org.smu.randsome.randsomeback.domain.report.implement.ReportManager;
import org.smu.randsome.randsomeback.domain.report.implement.ReportValidator;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;

class ReportServiceUnitTest extends UnitTestSupport {

    @InjectMocks
    ReportService reportService;

    @Mock
    ReportManager reportManager;

    @Mock
    ReportValidator reportValidator;

    @Mock
    MatchingReader matchingReader;

    @Test
    void 신고를_성공적으로_생성한다() {
        // given
        var reporterId = 1L;
        var matchingResultId = 10L;

        var applicant = mock(Member.class);
        var candidate = mock(Member.class);
        var matchingApplication = mock(MatchingApplication.class);
        var matchingResult = mock(MatchingResult.class);
        var savedReport = mock(Report.class);

        given(applicant.getId()).willReturn(reporterId);
        given(matchingApplication.getMember()).willReturn(applicant);
        given(matchingResult.getMatchingApplication()).willReturn(matchingApplication);
        given(matchingResult.getCandidate()).willReturn(candidate);
        given(matchingResult.getId()).willReturn(matchingResultId);
        given(savedReport.getId()).willReturn(100L);

        given(matchingReader.findMatchingResult(matchingResultId)).willReturn(matchingResult);
        given(reportManager.create(
                eq(applicant), eq(candidate),
                eq(ReportTargetType.MATCHING_RESULT), eq(matchingResultId),
                any(), any()
        )).willReturn(savedReport);

        NewReport newReport = NewReport.builder()
                .matchingResultId(matchingResultId)
                .reason(ReportReason.INAPPROPRIATE_CONTENT)
                .description("부적절한 프로필입니다.")
                .build();

        // when
        Long reportId = reportService.createReport(newReport, reporterId);

        // then
        assertThat(reportId).isEqualTo(100L);
        verify(reportValidator).validateIsApplicant(reporterId, reporterId);
        verify(reportValidator).validateReportCreation(
                eq(reporterId), any(), eq(ReportTargetType.MATCHING_RESULT), eq(matchingResultId)
        );
    }

    @Test
    void 신청자가_아닌_경우_신고_생성에_실패한다() {
        // given
        var reporterId = 2L;
        var applicantId = 1L;
        var matchingResultId = 10L;

        var applicant = mock(Member.class);
        var candidate = mock(Member.class);
        var matchingApplication = mock(MatchingApplication.class);
        var matchingResult = mock(MatchingResult.class);

        given(applicant.getId()).willReturn(applicantId);
        given(matchingApplication.getMember()).willReturn(applicant);
        given(matchingResult.getMatchingApplication()).willReturn(matchingApplication);
        given(matchingResult.getCandidate()).willReturn(candidate);

        given(matchingReader.findMatchingResult(matchingResultId)).willReturn(matchingResult);
        willThrow(new CoreException(ErrorType.FORBIDDEN_MATCHING_RESULT))
                .given(reportValidator).validateIsApplicant(applicantId, reporterId);

        NewReport newReport = NewReport.builder()
                .matchingResultId(matchingResultId)
                .reason(ReportReason.INAPPROPRIATE_CONTENT)
                .description("부적절합니다.")
                .build();

        // when & then
        assertThatThrownBy(() -> reportService.createReport(newReport, reporterId))
                .isInstanceOf(CoreException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.FORBIDDEN_MATCHING_RESULT);
    }

}