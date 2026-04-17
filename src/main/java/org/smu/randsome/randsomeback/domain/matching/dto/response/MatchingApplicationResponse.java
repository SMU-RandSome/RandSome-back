package org.smu.randsome.randsomeback.domain.matching.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingApplication;
import org.smu.randsome.randsomeback.domain.matching.enums.MatchingType;

@Schema(description = "매칭 신청 결과 응답 DTO")
@Builder
public record MatchingApplicationResponse(
        @Schema(description = "매칭 신청 ID", example = "1")
        Long matchingApplicationId,

        @Schema(description = "매칭 타입", example = "RANDOM")
        MatchingType matchingType,

        @Schema(description = "매칭 신청 인원", example = "2")
        int requestedCount,

        @Schema(description = "매칭된 인원", example = "1")
        int matchedCount,

        @Schema(description = "환불된 티켓 수", example = "1")
        int refundedTickets,          // 환불된 티켓 수

        @Schema(description = "부분 매칭 여부", example = "true")
        boolean isPartialMatch        // 클라이언트가 분기 처리 가능
) {

    public static MatchingApplicationResponse of(MatchingApplication application) {
        int matchedCount = application.getMatchedCount();
        int refundedTickets = application.getApplicationCount() - matchedCount;
        return MatchingApplicationResponse.builder()
                .matchingApplicationId(application.getId())
                .matchingType(application.getMatchingType())
                .requestedCount(application.getApplicationCount())
                .matchedCount(matchedCount)
                .refundedTickets(refundedTickets)
                .isPartialMatch(refundedTickets > 0)
                .build();
    }

}