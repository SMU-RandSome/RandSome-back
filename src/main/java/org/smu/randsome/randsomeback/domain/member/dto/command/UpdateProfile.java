package org.smu.randsome.randsomeback.domain.member.dto.command;

import lombok.Builder;
import org.smu.randsome.randsomeback.domain.member.enums.Mbti;

@Builder
public record UpdateProfile(
        String legalName,
        Mbti mbti,
        String instagramId,
        String selfIntroduction,
        String idealDescription
) {

}