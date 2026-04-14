package org.smu.randsome.randsomeback.domain.report.implement;

import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.report.entity.Report;
import org.smu.randsome.randsomeback.domain.report.enums.ReportReason;
import org.smu.randsome.randsomeback.domain.report.enums.ReportTargetType;
import org.smu.randsome.randsomeback.domain.report.repository.ReportJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class ReportManager {

    private final ReportReader reportReader;
    private final ReportJpaRepository reportJpaRepository;

    public Report create(
            Member reporter,
            Member reportedMember,
            ReportTargetType targetType,
            Long targetId,
            ReportReason reason,
            String description
    ) {
        return reportJpaRepository.save(
                Report.create(
                        reporter,
                        reportedMember,
                        targetType,
                        targetId,
                        reason,
                        description
                )
        );
    }

    @Transactional
    public void markAsResolved(Long reportId) {
        Report report = reportReader.find(reportId);
        report.markAsResolved();
    }

    @Transactional
    public void markAsRejected(Long reportId) {
        Report report = reportReader.find(reportId);
        report.markAsRejected();
    }

}