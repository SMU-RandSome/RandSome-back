package org.smu.randsome.randsomeback.domain.report.implement;

import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.report.enums.ReportTargetType;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReportValidator {

    private static final int MAX_ACTIVE_REPORTS = 3;

    private final ReportReader reportReader;

    public void validateIsApplicant(Long applicantId, Long reporterId) {
        if (!applicantId.equals(reporterId)) {
            throw new CoreException(ErrorType.FORBIDDEN_MATCHING_RESULT);
        }
    }

    public void validateReportCreation(Long reporterId, Long reportedMemberId, ReportTargetType targetType, Long targetId) {
        validateNoDuplicateReport(reporterId, targetType, targetId);
        validateReportedMemberNotSuspended(reportedMemberId);
    }

    private void validateNoDuplicateReport(Long reporterId, ReportTargetType targetType, Long targetId) {
        if (reportReader.existsDuplicateReport(reporterId, targetType, targetId)) {
            throw new CoreException(ErrorType.ALREADY_REPORTED_MEMBER);
        }
    }

    private void validateReportedMemberNotSuspended(Long reportedMemberId) {
        long activeReportCount = reportReader.countActiveReportsByReportedMember(reportedMemberId);
        if (activeReportCount >= MAX_ACTIVE_REPORTS) {
            throw new CoreException(ErrorType.REPORTED_MEMBER_SUSPENDED);
        }
    }

}