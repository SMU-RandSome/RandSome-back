package org.smu.randsome.randsomeback.domain.report.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.smu.randsome.randsomeback.domain.report.dto.command.NewReport;
import org.smu.randsome.randsomeback.domain.report.enums.ReportReason;

@Schema(description = "신고 생성 요청")
public record ReportCreateRequest(

        @Schema(description = "매칭 결과 ID", example = "1")
        @NotNull(message = "매칭 결과 ID는 필수입니다.")
        Long matchingResultId,

        @Schema(description = "신고 사유", example = "INAPPROPRIATE_CONTENT")
        @NotNull(message = "신고 사유는 필수입니다.")
        ReportReason reason,

        @Schema(description = "신고 상세 설명", example = "프로필 사진이 부적절합니다.")
        @NotBlank(message = "신고 설명은 필수입니다.")
        String description
) {

    public NewReport toNewReport() {
        return NewReport.builder()
                .matchingResultId(matchingResultId)
                .reason(reason)
                .description(description)
                .build();
    }

}