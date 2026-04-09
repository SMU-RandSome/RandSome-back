package org.smu.randsome.randsomeback.domain.member.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FaceTypeTag {

    PUPPY  ("강아지상"),
    CAT    ("고양이상"),
    BEAR   ("곰상"),
    FOX    ("여우상"),
    RABBIT ("토끼상"),
    PURE   ("청순한"),
    CHIC   ("시크한"),
    WARM   ("훈훈한"),
    ;

    private final String displayName;

}
