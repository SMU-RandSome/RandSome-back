package org.smu.randsome.randsomeback.domain.matching.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.Set;
import lombok.Builder;
import org.smu.randsome.randsomeback.domain.matching.dto.command.NewMatching;
import org.smu.randsome.randsomeback.domain.matching.entity.vo.IdealTypePreference;
import org.smu.randsome.randsomeback.domain.matching.enums.MatchingType;
import org.smu.randsome.randsomeback.domain.member.enums.DatingStyleTag;
import org.smu.randsome.randsomeback.domain.member.enums.FaceTypeTag;
import org.smu.randsome.randsomeback.domain.member.enums.Mbti;
import org.smu.randsome.randsomeback.domain.member.enums.PersonalityTag;

/**
 * 매칭 신청 요청 DTO다.
 * <br/>클라이언트가 매칭 신청 API를 호출할 때 전달하는 요청 데이터를 담는다.
 * <br/>RANDOM 매칭의 경우 이상형 조건 필드를 무시하고, IDEAL 매칭의 경우 이상형 조건을 포함하여 전달한다.
 * <br/>각 카테고리별로 여러 태그를 다중 선택할 수 있다.
 * <br/>신청 인원 수는 1~5명 범위에서만 유효하다.
 */
@Builder
@Schema(name = "매칭 신청 요청 DTO")
public record MatchingApplyRequest(
        @Schema(description = "매칭 인원 수", example = "2")
        @Min(value = 1, message = "매칭 인원 수는 최소 1명 이상이어야 합니다.")
        @Max(value = 5, message = "매칭 인원 수는 최대 5명 이하이어야 합니다.")
        int applicationCount,

        @Schema(description = "매칭 유형")
        @NotNull(message = "매칭 유형은 필수입니다.")
        MatchingType matchingType,

        @Schema(description = "이상형 성격 태그 (다중 선택 가능)")
        Set<PersonalityTag> preferredPersonalityTags,

        @Schema(description = "이상형 얼굴상 태그 (다중 선택 가능)")
        Set<FaceTypeTag> preferredFaceTypeTags,

        @Schema(description = "이상형 연애 스타일 태그 (다중 선택 가능)")
        Set<DatingStyleTag> preferredDatingStyleTags,

        @Schema(description = "이상형 MBTI (다중 선택 가능)")
        Set<Mbti> preferredMbtis
) {

    public static MatchingApplyRequest forRandom(int applicationCount) {
        return MatchingApplyRequest.builder()
                .applicationCount(applicationCount)
                .matchingType(MatchingType.RANDOM)
                .build();
    }

    public NewMatching toNewMatching() {
        IdealTypePreference idealTypePreference = matchingType == MatchingType.IDEAL
                ? IdealTypePreference.of(preferredPersonalityTags, preferredFaceTypeTags, preferredDatingStyleTags, preferredMbtis)
                : null;

        return NewMatching.builder()
                .applicationCount(applicationCount)
                .matchingType(matchingType)
                .idealTypePreference(idealTypePreference)
                .build();
    }

}
