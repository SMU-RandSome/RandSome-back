package org.smu.randsome.randsomeback.domain.matching.implement;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingApplication;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingResult;
import org.smu.randsome.randsomeback.domain.matching.enums.ApplicationStatus;
import org.smu.randsome.randsomeback.domain.matching.repository.MatchingJpaRepository;
import org.smu.randsome.randsomeback.domain.matching.repository.MatchingResultJpaRepository;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class MatchingReader {

    private final MatchingJpaRepository matchingJpaRepository;
    private final MatchingResultJpaRepository matchingResultJpaRepository;

    // NOTE: 페이징 불필요해서 안넣었습니다.
    @Transactional(readOnly = true)
    public List<MatchingApplication> findByMemberAndStatus(Long memberId, ApplicationStatus applicationStatus) {
        return matchingJpaRepository.findAllByMemberIdAndApplicationStatusAndStatus(
                memberId,
                applicationStatus,
                EntityStatus.ACTIVE
        );
    }

    @Transactional(readOnly = true)
    public List<MatchingResult> findApprovedByApplication(Long applicationId, Long memberId) {
        if (!matchingJpaRepository.existsByIdAndMemberIdAndApplicationStatusAndStatus(
                applicationId,
                memberId,
                ApplicationStatus.APPROVED,
                EntityStatus.ACTIVE
        )) {
            throw new CoreException(ErrorType.NOT_FOUND_APPROVED_MATCHING);
        }

        return matchingResultJpaRepository.findAllByApplicationAndStatus(
                applicationId,
                EntityStatus.ACTIVE
        );
    }

}