package org.smu.randsome.randsomeback.domain.matching.service.command;

import lombok.Builder;
import org.smu.randsome.randsomeback.domain.matching.enums.MatchingType;

@Builder
public record NewMatching(
        int applicationCount,
        MatchingType matchingType
) {

}