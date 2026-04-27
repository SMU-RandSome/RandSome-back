package org.smu.randsome.randsomeback.domain.member.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PersonalityTag {

    ACTIVE       ("활발한"),
    QUIET        ("조용한"),
    AFFECTIONATE ("다정한"),
    INDEPENDENT  ("독립적인"),
    FUNNY        ("유머있는"),
    SERIOUS      ("진지한"),
    OPTIMISTIC   ("긍정적인"),
    CAREFUL      ("신중한"),
    EMOTIONAL    ("감성적인"),
    RATIONAL     ("이성적인"),
    CONSIDERATE  ("배려심 깊은"),
    TETO         ("테토"),
    EGEN         ("에겐"),
    ;

    private final String displayName;

}
