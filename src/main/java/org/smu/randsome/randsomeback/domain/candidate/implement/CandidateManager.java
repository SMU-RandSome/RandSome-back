package org.smu.randsome.randsomeback.domain.candidate.implement;

import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.candidate.entity.CandidateRegistration;
import org.smu.randsome.randsomeback.domain.candidate.repository.CandidateJpaRepository;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.implement.MemberReader;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CandidateManager {

    private final CandidateJpaRepository candidateJpaRepository;
    private final MemberReader memberReader;

    public CandidateRegistration apply(Long memberId) {
        Member member = memberReader.find(memberId);

        return candidateJpaRepository.save(CandidateRegistration.apply(member));
    }

}