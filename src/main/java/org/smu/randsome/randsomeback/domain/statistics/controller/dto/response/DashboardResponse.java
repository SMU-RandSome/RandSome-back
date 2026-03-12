package org.smu.randsome.randsomeback.domain.statistics.controller.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "대시보드 통계 응답 DTO")
@Builder
public record DashboardResponse(
        @Schema(description = "승인된 매칭 후보 수", example = "42")
        long candidateCount,

        @Schema(description = "오늘의 매칭 신청 수", example = "5")
        long todayMatchingCount,

        @Schema(description = "전체 매칭 신청 수", example = "123")
        long totalMatchingCount
) {

}