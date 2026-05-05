package org.smu.randsome.randsomeback.domain.report.implement;

import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.report.enums.ReportTargetType;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReportValidator {

    private final ReportReader reportReader;

    public void validateIsApplicant(Long applicantId, Long reporterId) {
        if (!applicantId.equals(reporterId)) {
            throw new CoreException(ErrorType.FORBIDDEN_MATCHING_RESULT);
        }
    }

    public void validateReportCreation(Long reporterId, Long reportedMemberId, ReportTargetType targetType, Long targetId) {
        validateSelfReport(reporterId, reportedMemberId);
        validateNoDuplicateReport(reporterId, targetType, targetId);
    }

    private void validateSelfReport(Long reporterId, Long reportedMemberId) {
        if (reporterId.equals(reportedMemberId)) {
            throw new CoreException(ErrorType.CANNOT_REPORT_YOURSELF);
        }
    }

    private void validateNoDuplicateReport(Long reporterId, ReportTargetType targetType, Long targetId) {
        if (reportReader.existsDuplicateReport(reporterId, targetType, targetId)) {
            throw new CoreException(ErrorType.ALREADY_REPORTED_MEMBER);
        }
    }

}