package org.smu.randsome.randsomeback.admin.report.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;
import org.smu.randsome.randsomeback.domain.report.entity.Report;
import org.smu.randsome.randsomeback.domain.report.enums.ReportReason;
import org.smu.randsome.randsomeback.domain.report.enums.ReportStatus;
import org.smu.randsome.randsomeback.domain.report.enums.ReportTargetType;

@Schema(description = "관리자 신고 목록 아이템")
@Builder
public record AdminReportListItem(

        @Schema(description = "신고 ID", example = "1")
        Long id,

        @Schema(description = "신고자 닉네임", example = "남성#ABC123")
        String reporterNickname,

        @Schema(description = "신고 대상 닉네임", example = "여성#XYZ789")
        String reportedMemberNickname,

        @Schema(description = "신고 대상 타입", example = "MATCHING_RESULT")
        ReportTargetType targetType,

        @Schema(description = "신고 사유", example = "INAPPROPRIATE_CONTENT")
        ReportReason reason,

        @Schema(description = "신고 상태", example = "PENDING")
        ReportStatus reportStatus,

        @Schema(description = "신고 생성 시간")
        LocalDateTime createdAt
) {

    public static AdminReportListItem from(Report report) {
        return AdminReportListItem.builder()
                .id(report.getId())
                .reporterNickname(report.getReporter().getNickname())
                .reportedMemberNickname(report.getReportedMember().getNickname())
                .targetType(report.getTargetType())
                .reason(report.getReason())
                .reportStatus(report.getReportStatus())
                .createdAt(report.getCreatedAt())
                .build(
        );
    }

}