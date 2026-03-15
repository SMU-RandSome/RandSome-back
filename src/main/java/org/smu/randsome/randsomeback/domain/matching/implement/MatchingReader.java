package org.smu.randsome.randsomeback.domain.matching.implement;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingApplication;
import org.smu.randsome.randsomeback.domain.matching.enums.ApplicationStatus;
import org.smu.randsome.randsomeback.domain.matching.repository.MatchingJpaRepository;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class MatchingReader {

    private final MatchingJpaRepository matchingJpaRepository;

    // NOTE: 페이징 불필요해서 안넣었습니다.
    public List<MatchingApplication> findByMemberAndStatus(Long memberId, ApplicationStatus applicationStatus) {
        return matchingJpaRepository.findAllByMemberIdAndApplicationStatusAndStatus(
                memberId,
                applicationStatus,
                EntityStatus.ACTIVE
        );
    }

}