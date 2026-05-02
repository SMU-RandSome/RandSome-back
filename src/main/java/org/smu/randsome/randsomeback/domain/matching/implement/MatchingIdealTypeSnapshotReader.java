package org.smu.randsome.randsomeback.domain.matching.implement;

import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.matching.entity.vo.IdealTypePreference;
import org.smu.randsome.randsomeback.domain.matching.repository.MatchingIdealTypeSnapshotJpaRepository;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class MatchingIdealTypeSnapshotReader {

    private final MatchingIdealTypeSnapshotJpaRepository snapshotJpaRepository;

    public IdealTypePreference find(Long matchingApplicationId) {
        return snapshotJpaRepository.findByMatchingApplicationId(matchingApplicationId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_IDEAL_TYPE_SNAPSHOT))
                .toVO();
    }

}