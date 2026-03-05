package org.smu.randsome.randsomeback.domain.member.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Gender {

    MALE   ("남성"),
    FEMALE ("여성")

    ;

    private final String value;

}