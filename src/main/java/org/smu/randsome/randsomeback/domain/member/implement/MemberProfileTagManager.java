package org.smu.randsome.randsomeback.domain.member.implement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.entity.MemberProfileTag;
import org.smu.randsome.randsomeback.domain.member.enums.DatingStyleTag;
import org.smu.randsome.randsomeback.domain.member.enums.FaceTypeTag;
import org.smu.randsome.randsomeback.domain.member.enums.PersonalityTag;
import org.smu.randsome.randsomeback.domain.member.repository.MemberProfileTagJpaRepository;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class MemberProfileTagManager {

    private final MemberProfileTagJpaRepository memberProfileTagJpaRepository;

    public MemberProfileTag create(
            Member member,
            PersonalityTag personalityTag,
            FaceTypeTag faceTypeTag,
            DatingStyleTag datingStyleTag
    ) {
        MemberProfileTag saved = memberProfileTagJpaRepository.save(MemberProfileTag.create(
                member,
                personalityTag,
                faceTypeTag,
                datingStyleTag)
        );

        log.info("[MemberProfileTagManager] 프로필 태그 생성 완료 - memberId={}", member.getId());

        return saved;
    }

    public void updateTags(
            Long memberId,
            PersonalityTag personalityTag,
            FaceTypeTag faceTypeTag,
            DatingStyleTag datingStyleTag
    ) {
        MemberProfileTag profileTag = memberProfileTagJpaRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_MEMBER));

        profileTag.updateTags(personalityTag, faceTypeTag, datingStyleTag);

        log.info("[MemberProfileTagManager] 프로필 태그 수정 완료 - memberId={}", memberId);
    }

}
