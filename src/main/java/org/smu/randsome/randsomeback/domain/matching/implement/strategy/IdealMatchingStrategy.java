package org.smu.randsome.randsomeback.domain.matching.implement.strategy;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingApplication;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingResult;
import org.smu.randsome.randsomeback.domain.matching.enums.MatchingType;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.springframework.stereotype.Component;

/**
 * 이상형 기반 매칭 전략의 자리표시 구현체다.
 */
@Slf4j
@Component
public class IdealMatchingStrategy implements MatchingStrategy {

    @Override
    public MatchingType getSupportedType() {
        return MatchingType.IDEAL;
    }

    /**
     * 이상형 기반 매칭 결과를 생성한다.
     *
     * @param matchingApplication 승인된 매칭 신청
     * @return 이상형 매칭 결과 목록
     * @throws CoreException 아직 구현되지 않은 경우
     */
    @Override
    public List<MatchingResult> execute(MatchingApplication matchingApplication) {
        log.warn("[IdealMatchingStrategy] 이상형 매칭 전략 미구현 - matchingApplicationId: {}",
                matchingApplication.getId());
        throw new CoreException(ErrorType.IDEAL_MATCHING_NOT_IMPLEMENTED);
    }

}
