package org.smu.randsome.randsomeback.domain.report.implement;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.report.enums.ReportTargetType;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;

class ReportValidatorUnitTest extends UnitTestSupport {

    @InjectMocks
    ReportValidator reportValidator;

    @Mock
    ReportReader reportReader;

    @Test
    void 신청자_본인이면_예외가_발생하지_않는다() {
        assertThatCode(() -> reportValidator.validateIsApplicant(1L, 1L))
                .doesNotThrowAnyException();
    }

    @Test
    void 신청자가_아니면_예외가_발생한다() {
        assertThatThrownBy(() -> reportValidator.validateIsApplicant(1L, 2L))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.FORBIDDEN_MATCHING_RESULT.getMessage());
    }

    @Test
    void 중복_신고가_없으면_예외가_발생하지_않는다() {
        given(reportReader.existsDuplicateReport(1L, ReportTargetType.MATCHING_RESULT, 10L))
                .willReturn(false);
        given(reportReader.countActiveReportsByReportedMember(2L))
                .willReturn(0L);

        assertThatCode(() -> reportValidator.validateReportCreation(
                1L, 2L, ReportTargetType.MATCHING_RESULT, 10L))
                .doesNotThrowAnyException();
    }

    @Test
    void 동일_대상에_이미_신고한_경우_예외가_발생한다() {
        given(reportReader.existsDuplicateReport(1L, ReportTargetType.MATCHING_RESULT, 10L))
                .willReturn(true);

        assertThatThrownBy(() -> reportValidator.validateReportCreation(
                1L, 2L, ReportTargetType.MATCHING_RESULT, 10L))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.ALREADY_REPORTED_MEMBER.getMessage());
    }

    @Test
    void 피신고자의_활성_신고가_3건_이상이면_예외가_발생한다() {
        given(reportReader.existsDuplicateReport(1L, ReportTargetType.MATCHING_RESULT, 10L))
                .willReturn(false);
        given(reportReader.countActiveReportsByReportedMember(2L))
                .willReturn(3L);

        assertThatThrownBy(() -> reportValidator.validateReportCreation(
                1L, 2L, ReportTargetType.MATCHING_RESULT, 10L))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.REPORTED_MEMBER_SUSPENDED.getMessage());
    }

    @Test
    void 피신고자의_활성_신고가_2건이면_예외가_발생하지_않는다() {
        given(reportReader.existsDuplicateReport(1L, ReportTargetType.MATCHING_RESULT, 10L))
                .willReturn(false);
        given(reportReader.countActiveReportsByReportedMember(2L))
                .willReturn(2L);

        assertThatCode(() -> reportValidator.validateReportCreation(
                1L, 2L, ReportTargetType.MATCHING_RESULT, 10L))
                .doesNotThrowAnyException();
    }
}
