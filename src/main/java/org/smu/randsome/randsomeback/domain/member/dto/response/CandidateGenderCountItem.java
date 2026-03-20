package org.smu.randsome.randsomeback.domain.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.smu.randsome.randsomeback.domain.member.enums.Gender;

@Schema(description = "후보자 성별 건수 항목")
public record CandidateGenderCountItem(
        @Schema(description = "성별")
        Gender gender,

        @Schema(description = "후보자 수", example = "10")
        long count
) {

}