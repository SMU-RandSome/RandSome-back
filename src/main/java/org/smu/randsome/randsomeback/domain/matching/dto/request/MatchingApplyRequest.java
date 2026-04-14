package org.smu.randsome.randsomeback.domain.matching.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.smu.randsome.randsomeback.domain.matching.dto.command.NewMatching;
import org.smu.randsome.randsomeback.domain.matching.entity.vo.IdealTypePreference;
import org.smu.randsome.randsomeback.domain.matching.enums.MatchingType;
import org.smu.randsome.randsomeback.domain.member.enums.DatingStyleTag;
import org.smu.randsome.randsomeback.domain.member.enums.FaceTypeTag;
import org.smu.randsome.randsomeback.domain.member.enums.PersonalityTag;

@Schema(name = "매칭 신청 요청 DTO")
public record MatchingApplyRequest(
        @Schema(description = "매칭 인원 수", example = "2")
        @Min(value = 1, message = "매칭 인원 수는 최소 1명 이상이어야 합니다.")
        @Max(value = 5, message = "매칭 인원 수는 최대 5명 이하이어야 합니다.")
        int applicationCount,

        @Schema(description = "매칭 유형")
        @NotNull(message = "매칭 유형은 필수입니다.")
        MatchingType matchingType,

        @Schema(description = "이상형 성격 태그 (선택)")
        PersonalityTag preferredPersonalityTag,

        @Schema(description = "이상형 얼굴상 태그 (선택)")
        FaceTypeTag preferredFaceTypeTag,

        @Schema(description = "이상형 연애 스타일 태그 (선택)")
        DatingStyleTag preferredDatingStyleTag
) {

    public static MatchingApplyRequest forRandom(int applicationCount) {
        return new MatchingApplyRequest(applicationCount, MatchingType.RANDOM, null, null, null);
    }

    public NewMatching toNewMatching() {
        IdealTypePreference idealTypePreference = matchingType == MatchingType.IDEAL
                ? IdealTypePreference.of(preferredPersonalityTag, preferredFaceTypeTag, preferredDatingStyleTag)
                : null;

        return NewMatching.builder()
                .applicationCount(applicationCount)
                .matchingType(matchingType)
                .idealTypePreference(idealTypePreference)
                .build();
    }

}