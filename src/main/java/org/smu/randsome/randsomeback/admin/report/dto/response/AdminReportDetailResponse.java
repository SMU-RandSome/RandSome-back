package org.smu.randsome.randsomeback.admin.report.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;
import org.smu.randsome.randsomeback.domain.report.entity.Report;
import org.smu.randsome.randsomeback.domain.report.enums.ReportReason;
import org.smu.randsome.randsomeback.domain.report.enums.ReportStatus;
import org.smu.randsome.randsomeback.domain.report.enums.ReportTargetType;

@Schema(description = "관리자 신고 상세 응답")
@Builder
public record AdminReportDetailResponse(
        @Schema(description = "신고 ID", example = "1")
        Long id,

        @Schema(description = "신고자 ID", example = "1")
        Long reporterId,

        @Schema(description = "신고자 닉네임", example = "남성#ABC123")
        String reporterNickname,

        @Schema(description = "신고 대상 회원 ID", example = "2")
        Long reportedMemberId,

        @Schema(description = "신고 대상 닉네임", example = "여성#XYZ789")
        String reportedMemberNickname,

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

        @Schema(description = "해당 회원의 총 활성 신고 횟수", example = "2")
        long activeReportCount,

        @Schema(description = "신고 생성 시간")
        LocalDateTime createdAt
) {

    public static AdminReportDetailResponse of(Report report, long activeReportCount) {
        return AdminReportDetailResponse.builder()
                .id(report.getId())
                .reporterId(report.getReporter().getId())
                .reporterNickname(report.getReporter().getNickname())
                .reportedMemberId(report.getReportedMember().getId())
                .reportedMemberNickname(report.getReportedMember().getNickname())
                .targetType(report.getTargetType())
                .targetId(report.getTargetId())
                .reason(report.getReason())
                .description(report.getDescription())
                .reportStatus(report.getReportStatus())
                .activeReportCount(activeReportCount)
                .createdAt(report.getCreatedAt())
                .build();
    }

}