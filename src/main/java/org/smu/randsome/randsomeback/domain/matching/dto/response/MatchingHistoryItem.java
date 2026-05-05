package org.smu.randsome.randsomeback.domain.matching.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingApplication;
import org.smu.randsome.randsomeback.domain.matching.enums.ApplicationStatus;
import org.smu.randsome.randsomeback.domain.matching.enums.MatchingType;

/**
 * 매칭 신청 내역 응답 DTO다.
 * <br/>회원의 과거 매칭 신청 목록을 조회할 때 반환되는 단건 정보를 담는다.
 * <br/>신청 상태, 신청 시각, 신청 인원 수 등 필수 정보를 포함한다.
 */
@Schema(description = "매칭 신청 내역 단건 정보")
public record MatchingHistoryItem(
        @Schema(description = "매칭 신청 ID", example = "101")
        Long id,

        @Schema(description = "매칭 유형 라벨", example = "RANDOM")
        MatchingType matchingType,

        @Schema(description = "매칭 신청 상태", example = "PENDING")
        ApplicationStatus applicationStatus,

        @Schema(description = "매칭 신청 시각")
        LocalDateTime appliedAt,

        @Schema(description = "해당 신청자의 신청 횟수", example = "3")
        int applicationCount,

        @Schema(description = "실제 매칭된 인원 수", example = "2")
        int matchedCount
) {

    /**
     * MatchingApplication 엔티티를 응답 DTO로 변환한다.
     *
     * @param app 매칭 신청 엔티티
     * @return 변환된 응답 DTO
     */
    public static MatchingHistoryItem from(MatchingApplication app) {
        return new MatchingHistoryItem(
                app.getId(),
                app.getMatchingType(),
                app.getApplicationStatus(),
                app.getCreatedAt(),
                app.getApplicationCount(),
                app.getMatchedCount()
        );
    }

}