package org.smu.randsome.randsomeback.domain.member.entity.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.util.Objects;
import org.smu.randsome.randsomeback.domain.member.enums.DatingStyleTag;
import org.smu.randsome.randsomeback.domain.member.enums.FaceTypeTag;
import org.smu.randsome.randsomeback.domain.member.enums.PersonalityTag;

/**
 * 회원의 내 소개 태그를 나타내는 Value Object.
 * 성격 / 얼굴상 / 연애 스타일 각 카테고리에서 단일 태그를 보유한다.
 * 세 태그 모두 필수값이다.
 */
@Embeddable
public record MyProfileTags(
        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        PersonalityTag personalityTag,

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        FaceTypeTag faceTypeTag,

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        DatingStyleTag datingStyleTag
) {

    public MyProfileTags {
        Objects.requireNonNull(personalityTag, "성격 태그는 필수입니다.");
        Objects.requireNonNull(faceTypeTag, "얼굴상 태그는 필수입니다.");
        Objects.requireNonNull(datingStyleTag, "연애 스타일 태그는 필수입니다.");
    }

    public static MyProfileTags of(PersonalityTag personalityTag, FaceTypeTag faceTypeTag, DatingStyleTag datingStyleTag) {
        return new MyProfileTags(personalityTag, faceTypeTag, datingStyleTag);
    }

}