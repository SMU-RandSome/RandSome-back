package org.smu.randsome.randsomeback.domain.matching.implement;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingApplication;
import org.smu.randsome.randsomeback.domain.matching.repository.MatchingJpaRepository;
import org.smu.randsome.randsomeback.domain.matching.service.command.NewMatching;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.implement.MemberReader;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public void approve(Long id, LocalDateTime approvedAt) {
        MatchingApplication matchingApplication = matchingJpaRepository.findByIdAndStatus(id, EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_MATCHING));

        matchingApplication.approve(approvedAt);

        // TODO: MatchingStrategy를 통한 매칭 로직 수행 (RANDOM/IDEAL 전략 분리 예정)

    }

    @Transactional
    public void reject(Long id, String rejectedReason, LocalDateTime rejectedAt) {
        MatchingApplication matchingApplication = matchingJpaRepository.findByIdAndStatus(id, EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_MATCHING));

        matchingApplication.reject(rejectedAt, rejectedReason);
    }

}