package org.smu.randsome.randsomeback.domain.matching.dto.command;

import lombok.Builder;
import org.smu.randsome.randsomeback.domain.matching.entity.vo.IdealTypePreference;
import org.smu.randsome.randsomeback.domain.matching.enums.MatchingType;

@Builder
public record NewMatching(
        int applicationCount,
        MatchingType matchingType,
        IdealTypePreference idealTypePreference
) {

}