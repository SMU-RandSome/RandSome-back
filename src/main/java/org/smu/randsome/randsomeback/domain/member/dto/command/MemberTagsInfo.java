package org.smu.randsome.randsomeback.domain.member.dto.command;

import org.smu.randsome.randsomeback.domain.member.enums.DatingStyleTag;
import org.smu.randsome.randsomeback.domain.member.enums.FaceTypeTag;
import org.smu.randsome.randsomeback.domain.member.enums.PersonalityTag;

public record MemberTagsInfo(
        PersonalityTag personalityTag,
        FaceTypeTag faceTypeTag,
        DatingStyleTag datingStyleTag
) {

}