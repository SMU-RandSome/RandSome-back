package org.smu.randsome.randsomeback.domain.matching.dto.command;

import java.time.LocalDate;
import org.smu.randsome.randsomeback.domain.matching.enums.MatchingSortType;
import org.smu.randsome.randsomeback.domain.member.enums.Gender;

public record MatchingSearchCondition(
        LocalDate date,
        Gender gender,
        String keyword,
        MatchingSortType sort
) {

}
