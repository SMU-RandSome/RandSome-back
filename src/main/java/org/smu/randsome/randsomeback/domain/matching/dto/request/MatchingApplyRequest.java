package org.smu.randsome.randsomeback.domain.matching.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.smu.randsome.randsomeback.domain.matching.dto.command.NewMatching;
import org.smu.randsome.randsomeback.domain.matching.enums.MatchingType;

@Schema(name = "매칭 신청 요청 DTO")
public record MatchingApplyRequest(
        @Schema(description = "매칭 인원 수", example = "2")
        @Min(value = 1, message = "매칭 인원 수는 최소 1명 이상이어야 합니다.")
        @Max(value = 5, message = "매칭 인원 수는 최대 5명 이하이어야 합니다.")
        int applicationCount,

        @Schema(description = "매칭 유형")
        @NotNull(message = "매칭 유형은 필수입니다.")
        MatchingType matchingType
) {

    public NewMatching toNewMatching() {
        return NewMatching.builder()
                .applicationCount(applicationCount)
                .matchingType(matchingType)
                .build();
    }

}