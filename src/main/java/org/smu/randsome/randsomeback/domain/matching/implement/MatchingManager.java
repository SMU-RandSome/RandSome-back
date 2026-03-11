package org.smu.randsome.randsomeback.domain.matching.implement;

import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingApplication;
import org.smu.randsome.randsomeback.domain.matching.repository.MatchingJpaRepository;
import org.smu.randsome.randsomeback.domain.matching.service.command.NewMatching;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.implement.MemberReader;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class MatchingManager {

    private final MatchingJpaRepository matchingJpaRepository;
    private final MemberReader memberReader;

    public MatchingApplication apply(NewMatching newMatching, Long memberId) {
        Member member = memberReader.find(memberId);

        return matchingJpaRepository.save(MatchingApplication.apply(
                member,
                newMatching.matchingType(),
                newMatching.applicationCount()
        ));
    }

}