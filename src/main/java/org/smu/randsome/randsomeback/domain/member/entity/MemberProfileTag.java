package org.smu.randsome.randsomeback.domain.member.entity;

import static java.util.Objects.requireNonNull;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.smu.randsome.randsomeback.domain.member.enums.DatingStyleTag;
import org.smu.randsome.randsomeback.domain.member.enums.FaceTypeTag;
import org.smu.randsome.randsomeback.domain.member.enums.PersonalityTag;
import org.smu.randsome.randsomeback.global.entity.BaseEntity;

/**
 * 회원의 프로필 태그를 나타내는 엔티티다.
 * <br/>성격, 얼굴상, 연애 스타일 각 카테고리에서 단일 태그를 보유한다.
 * <br/>Member와 1:1 관계이며, 태그 변경이 Member 엔티티에 영향을 주지 않도록 분리되었다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class MemberProfileTag extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, unique = true)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PersonalityTag personalityTag;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FaceTypeTag faceTypeTag;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DatingStyleTag datingStyleTag;

    public static MemberProfileTag create(
            Member member,
            PersonalityTag personalityTag,
            FaceTypeTag faceTypeTag,
            DatingStyleTag datingStyleTag
    ) {
        MemberProfileTag tag = new MemberProfileTag();
        tag.member = requireNonNull(member);
        tag.personalityTag = requireNonNull(personalityTag, "성격 태그는 필수입니다.");
        tag.faceTypeTag = requireNonNull(faceTypeTag, "얼굴상 태그는 필수입니다.");
        tag.datingStyleTag = requireNonNull(datingStyleTag, "연애 스타일 태그는 필수입니다.");
        return tag;
    }

    public void updateTags(
            PersonalityTag personalityTag,
            FaceTypeTag faceTypeTag,
            DatingStyleTag datingStyleTag
    ) {
        this.personalityTag = requireNonNull(personalityTag, "성격 태그는 필수입니다.");
        this.faceTypeTag = requireNonNull(faceTypeTag, "얼굴상 태그는 필수입니다.");
        this.datingStyleTag = requireNonNull(datingStyleTag, "연애 스타일 태그는 필수입니다.");
    }

}
