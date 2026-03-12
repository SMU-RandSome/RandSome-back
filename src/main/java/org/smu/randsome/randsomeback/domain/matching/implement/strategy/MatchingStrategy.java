package org.smu.randsome.randsomeback.domain.matching.implement.strategy;

import java.util.List;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingApplication;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingResult;
import org.smu.randsome.randsomeback.domain.matching.enums.MatchingType;

/**
 * 매칭 타입별 결과 생성 전략 계약이다.
 */
public interface MatchingStrategy {

    /**
     * 전략이 처리할 매칭 타입을 반환한다.
     *
     * @return 지원하는 매칭 타입
     */
    MatchingType getSupportedType();

    /**
     * 매칭 신청 정보를 기준으로 매칭 결과 목록을 생성한다.
     *
     * @param matchingApplication 승인된 매칭 신청
     * @return 생성된 매칭 결과 목록
     */
    List<MatchingResult> execute(MatchingApplication matchingApplication);

}