package org.smu.randsome.randsomeback.admin.report.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.admin.report.enums.AdminReportStatusFilter;
import org.smu.randsome.randsomeback.domain.member.implement.MemberManager;
import org.smu.randsome.randsomeback.domain.report.entity.Report;
import org.smu.randsome.randsomeback.domain.report.implement.ReportManager;
import org.smu.randsome.randsomeback.domain.report.implement.ReportReader;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminReportService {

    private final ReportReader reportReader;
    private final ReportManager reportManager;
    private final MemberManager memberManager;

    public List<Report> findReportsByFilter(AdminReportStatusFilter statusFilter) {
        return reportReader.findAllByStatuses(statusFilter.getStatuses());
    }

    public Report findReport(Long reportId) {
        return reportReader.find(reportId);
    }

    public long countActiveReportsForMember(Long memberId) {
        return reportReader.countActiveReportsByReportedMember(memberId);
    }

    public void resolveReport(Long reportId) {
        reportManager.markAsResolved(reportId);
    }

    public void rejectReport(Long reportId) {
        reportManager.markAsRejected(reportId);
    }

    public void restoreMember(Long memberId) {
        memberManager.restore(memberId);
    }

}