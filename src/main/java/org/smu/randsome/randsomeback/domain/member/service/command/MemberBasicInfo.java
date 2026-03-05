package org.smu.randsome.randsomeback.domain.member.service.command;

import org.smu.randsome.randsomeback.domain.member.enums.Gender;
import org.smu.randsome.randsomeback.domain.member.enums.Mbti;

public record MemberBasicInfo(String legalName, Gender gender, Mbti mbti) {

}