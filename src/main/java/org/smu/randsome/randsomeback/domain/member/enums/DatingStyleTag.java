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
    RESPECTFUL_SPACE ("각자 시간 존중"),
    EXPRESSIVE       ("감정 표현 잘함"),
    GROW_TOGETHER    ("함께 성장"),
    HOME_DATE        ("집 데이트 선호"),
    OUTDOOR_DATE     ("야외 데이트 선호"),
    DEEP_TALK        ("깊은 대화 선호"),
    FRIEND_LIKE      ("친구같은 연애"),
    ROMANTIC         ("로맨틱한 연애"),
    SLOW_STARTER     ("천천히 알아가기"),
    ;

    private final String displayName;

}
