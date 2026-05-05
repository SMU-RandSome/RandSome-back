package org.smu.randsome.randsomeback.domain.member.dto;

import org.smu.randsome.randsomeback.domain.member.entity.MemberProfileTag;
import org.smu.randsome.randsomeback.domain.member.enums.DatingStyleTag;
import org.smu.randsome.randsomeback.domain.member.enums.FaceTypeTag;
import org.smu.randsome.randsomeback.domain.member.enums.PersonalityTag;

/**
 * 프로필 태그 캐싱 전용 DTO.
 * Redis 직렬화/역직렬화에 사용되며, MemberProfileTag 엔티티 대신 반환한다.
 */
public record ProfileTags(
        PersonalityTag personalityTag,
        FaceTypeTag faceTypeTag,
        DatingStyleTag datingStyleTag
) {

    public static ProfileTags from(MemberProfileTag entity) {
        return new ProfileTags(
                entity.getPersonalityTag(),
                entity.getFaceTypeTag(),
                entity.getDatingStyleTag()
        );
    }

}
