package org.smu.randsome.randsomeback.domain.member.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DatingStyleTag {

    FREQUENT_CONTACT ("자주 연락"),
    MODERATE_CONTACT ("적당한 연락"),
    PLANNED_DATE     ("계획형 데이트"),
    SPONTANEOUS_DATE ("즉흥형 데이트"),
    SKINSHIP_LOVER   ("스킨십 많은"),
    RESPECTFUL_SPACE ("각자 시간 존중"),
    EXPRESSIVE       ("감정 표현 잘함"),
    GROW_TOGETHER    ("함께 성장"),
    ;

    private final String displayName;

}
