package org.smu.randsome.randsomeback.domain.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(
        name = "회원 통계 응답 DTO",
        description = "회원의 노출 횟수, 보낸 신청 수, 출석 일수를 포함합니다."
)
@Builder
public record MemberStatsResponse(
        @Schema(description = "노출 횟수", example = "12")
        long exposureCount,

        @Schema(description = "보낸 신청 수", example = "3")
        long sentApplicationCount,

        @Schema(description = "출석 일수", example = "7")
        long attendanceDays
) {

    public static MemberStatsResponse of(
            long exposureCount,
            long sentApplicationCount,
            long attendanceDays
    ) {
        return MemberStatsResponse.builder()
                .exposureCount(exposureCount)
                .sentApplicationCount(sentApplicationCount)
                .attendanceDays(attendanceDays)
                .build();
    }

}
