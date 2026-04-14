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

/**
 * 매칭 신청 요청 DTO다.
 * <br/>클라이언트가 매칭 신청 API를 호출할 때 전달하는 요청 데이터를 담는다.
 * <br/>RANDOM 매칭의 경우 이상형 조건 필드를 무시하고, IDEAL 매칭의 경우 이상형 조건을 포함하여 전달한다.
 * <br/>신청 인원 수는 1~5명 범위에서만 유효하다.
 */
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

    /**
     * 랜덤 매칭 요청을 빠르게 생성하는 팩토리 메서드다.
     *
     * @param applicationCount 신청 인원 수
     * @return 이상형 조건 없는 랜덤 매칭 요청
     */
    public static MatchingApplyRequest forRandom(int applicationCount) {
        return new MatchingApplyRequest(applicationCount, MatchingType.RANDOM, null, null, null);
    }

    /**
     * Request DTO를 도메인 커맨드 DTO로 변환한다.
     * <br/>매칭 타입에 따라 이상형 조건을 포함할지 결정한다.
     *
     * @return 변환된 커맨드 DTO
     */
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