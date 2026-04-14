package org.smu.randsome.randsomeback.domain.report.implement;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.report.enums.ReportStatus;
import org.smu.randsome.randsomeback.domain.report.enums.ReportTargetType;
import org.smu.randsome.randsomeback.domain.report.repository.ReportJpaRepository;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Component
public class ReportReader {

    private final ReportJpaRepository reportJpaRepository;

    public boolean existsDuplicateReport(Long reporterId, ReportTargetType targetType, Long targetId) {
        return reportJpaRepository.existsByReporterIdAndTargetTypeAndTargetId(reporterId, targetType, targetId);
    }

    public long countActiveReportsByReportedMember(Long reportedMemberId) {
        return reportJpaRepository.countByReportedMemberAndReportStatusIn(
                reportedMemberId,
                List.of(ReportStatus.PENDING, ReportStatus.IN_REVIEW),
                EntityStatus.ACTIVE
        );
    }

}