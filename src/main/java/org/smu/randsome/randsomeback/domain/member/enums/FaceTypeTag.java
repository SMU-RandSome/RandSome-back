package org.smu.randsome.randsomeback.domain.member.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FaceTypeTag {

    PUPPY    ("강아지상"),
    CAT      ("고양이상"),
    BEAR     ("곰상"),
    FOX      ("여우상"),
    RABBIT   ("토끼상"),
    PURE     ("청순한"),
    CHIC     ("시크한"),
    WARM     ("훈훈한"),
    DINOSAUR ("공룡상"),
    HAMSTER  ("햄스터상"),
    WOLF     ("늑대상"),
    CUTE     ("귀여운"),
    STRONG   ("강한"),
    PRINCE   ("왕자님상"),
    DUBU     ("두부상"),
    JOKER    ("조커상"),
    SNAKE    ("뱀상")

    ;

    private final String displayName;

}
