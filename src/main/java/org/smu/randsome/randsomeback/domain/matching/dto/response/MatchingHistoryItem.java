package org.smu.randsome.randsomeback.domain.matching.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingApplication;
import org.smu.randsome.randsomeback.domain.matching.enums.ApplicationStatus;

@Schema(description = "매칭 신청 내역 단건 정보")
public record MatchingHistoryItem(
        @Schema(description = "매칭 신청 ID", example = "101")
        Long id,

        @Schema(description = "매칭 유형 라벨", example = "이상형 매칭")
        String matchingTypeLabel,

        @Schema(description = "매칭 신청 상태", example = "PENDING")
        ApplicationStatus applicationStatus,

        @Schema(description = "매칭 신청 시각")
        LocalDateTime appliedAt,

        @Schema(description = "해당 신청자의 신청 횟수", example = "3")
        int applicationCount
) {

    public static MatchingHistoryItem from(MatchingApplication app) {
        return new MatchingHistoryItem(
                app.getId(),
                app.getMatchingType().getLabel(),
                app.getApplicationStatus(),
                app.getCreatedAt(),
                app.getApplicationCount()
        );
    }

}