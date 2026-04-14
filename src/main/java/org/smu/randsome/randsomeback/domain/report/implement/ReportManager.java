package org.smu.randsome.randsomeback.domain.report.implement;

import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.report.entity.Report;
import org.smu.randsome.randsomeback.domain.report.enums.ReportReason;
import org.smu.randsome.randsomeback.domain.report.enums.ReportTargetType;
import org.smu.randsome.randsomeback.domain.report.repository.ReportJpaRepository;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ReportManager {

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

}