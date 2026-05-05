package org.smu.randsome.randsomeback.domain.matching.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MatchingType {

    RANDOM (1, 5, "무작위 매칭"),
    IDEAL  (1, 5, "이상형 매칭"),
    ;

    private final int minCount;
    private final int maxCount;
    private final String label;

}