package org.smu.randsome.randsomeback.admin.report.enums;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.smu.randsome.randsomeback.domain.report.enums.ReportStatus;

@AllArgsConstructor
@Getter
public enum AdminReportStatusFilter {

    PENDING   (List.of(ReportStatus.PENDING)),
    IN_REVIEW (List.of(ReportStatus.IN_REVIEW)),
    COMPLETED (List.of(ReportStatus.RESOLVED, ReportStatus.REJECTED));

    private final List<ReportStatus> statuses;

}