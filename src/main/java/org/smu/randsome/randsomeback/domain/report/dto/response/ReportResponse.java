package org.smu.randsome.randsomeback.domain.report.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import org.smu.randsome.randsomeback.domain.report.entity.Report;
import org.smu.randsome.randsomeback.domain.report.enums.ReportReason;
import org.smu.randsome.randsomeback.domain.report.enums.ReportStatus;
import org.smu.randsome.randsomeback.domain.report.enums.ReportTargetType;

@Schema(description = "신고 응답")
public record ReportResponse(

        @Schema(description = "신고 ID", example = "1")
        Long id,

        @Schema(description = "신고 대상 타입", example = "MATCHING_RESULT")
        ReportTargetType targetType,

        @Schema(description = "신고 대상 ID", example = "1")
        Long targetId,

        @Schema(description = "신고 사유", example = "INAPPROPRIATE_CONTENT")
        ReportReason reason,

        @Schema(description = "신고 설명", example = "프로필 사진이 부적절합니다.")
        String description,

        @Schema(description = "신고 상태", example = "PENDING")
        ReportStatus reportStatus,

        @Schema(description = "신고 생성 시간")
        LocalDateTime createdAt
) {

    public static ReportResponse from(Report report) {
        return new ReportResponse(
                report.getId(),
                report.getTargetType(),
                report.getTargetId(),
                report.getReason(),
                report.getDescription(),
                report.getReportStatus(),
                report.getCreatedAt()
        );
    }
}
