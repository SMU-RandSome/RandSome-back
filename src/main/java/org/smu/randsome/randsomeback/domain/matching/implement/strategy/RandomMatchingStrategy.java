package org.smu.randsome.randsomeback.domain.matching.implement.strategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingApplication;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingResult;
import org.smu.randsome.randsomeback.domain.matching.enums.MatchingType;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.enums.Gender;
import org.smu.randsome.randsomeback.domain.member.implement.MemberReader;
import org.springframework.stereotype.Component;

/**
 * 후보군을 무작위로 셔플한 뒤 요청 수만큼 매칭 결과를 생성하는 전략이다.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class RandomMatchingStrategy implements MatchingStrategy {

    private final MemberReader memberReader;

    @Override
    public MatchingType getSupportedType() {
        return MatchingType.RANDOM;
    }

    /**
     * 신청자의 희망 성별 후보군을 무작위 셔플하고 신청 수만큼 결과를 생성한다.
     *
     * @param matchingApplication 승인된 매칭 신청
     * @return 무작위 매칭 결과 목록
     */
    @Override
    public List<MatchingResult> execute(MatchingApplication matchingApplication) {
        Gender targetGender = matchingApplication.getTargetGender();
        // NOTE: 신청 수의 5배를 후보군으로 조회하여, 셔플 후 충분한 후보가 남도록 한다.
        int count = matchingApplication.getApplicationCount() * 5;
        List<Member> candidates = new ArrayList<>(memberReader.findCandidatesByGender(targetGender, count));

        log.debug("[RandomMatchingStrategy] 후보 조회 완료 - matchingApplicationId: {}, targetGender: {}, candidateCount: {}",
                matchingApplication.getId(), targetGender, candidates.size());

        Collections.shuffle(candidates);

        List<MatchingResult> results = candidates.stream()
                .limit(matchingApplication.getApplicationCount())
                .map(candidate -> MatchingResult.create(matchingApplication, candidate))
                .toList();

        log.info("[RandomMatchingStrategy] 무작위 매칭 결과 생성 완료 - matchingApplicationId: {}, requestedCount: {}, resultCount: {}",
                matchingApplication.getId(), matchingApplication.getApplicationCount(), results.size());

        return results;
    }

}