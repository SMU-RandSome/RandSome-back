package org.smu.randsome.randsomeback.domain.report.dto.command;

import lombok.Builder;
import org.smu.randsome.randsomeback.domain.report.enums.ReportReason;

@Builder
public record NewReport(
        Long matchingResultId,
        ReportReason reason,
        String description
) {

}