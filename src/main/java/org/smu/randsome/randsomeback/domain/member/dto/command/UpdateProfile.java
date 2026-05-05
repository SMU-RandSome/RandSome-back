package org.smu.randsome.randsomeback.domain.member.dto.command;

import lombok.Builder;
import org.smu.randsome.randsomeback.domain.member.enums.DatingStyleTag;
import org.smu.randsome.randsomeback.domain.member.enums.Department;
import org.smu.randsome.randsomeback.domain.member.enums.FaceTypeTag;
import org.smu.randsome.randsomeback.domain.member.enums.Mbti;
import org.smu.randsome.randsomeback.domain.member.enums.PersonalityTag;

@Builder
public record UpdateProfile(
        String legalName,
        Mbti mbti,
        Department department,
        String instagramId,
        String selfIntroduction,
        String idealDescription,
        PersonalityTag personalityTag,
        FaceTypeTag faceTypeTag,
        DatingStyleTag datingStyleTag
) {

}