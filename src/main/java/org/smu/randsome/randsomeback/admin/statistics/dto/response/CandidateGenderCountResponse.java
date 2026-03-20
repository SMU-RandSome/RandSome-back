package org.smu.randsome.randsomeback.admin.statistics.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.smu.randsome.randsomeback.domain.member.dto.response.CandidateGenderCountItem;

@Schema(description = "후보자 성별 수 응답")
public record CandidateGenderCountResponse(
        @Schema(description = "남자 후보자 수")
        long maleCount,

        @Schema(description = "여자 후보자 수")
        long femaleCount
) {

    public static CandidateGenderCountResponse from(List<CandidateGenderCountItem> items) {
        long maleCount = items.stream()
                .filter(item -> item.gender().isMale())
                .mapToLong(CandidateGenderCountItem::count)
                .sum();

        long femaleCount = items.stream()
                .filter(item -> item.gender().isFemale())
                .mapToLong(CandidateGenderCountItem::count)
                .sum();

        return new CandidateGenderCountResponse(maleCount, femaleCount);
    }

}