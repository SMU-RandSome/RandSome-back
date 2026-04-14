package org.smu.randsome.randsomeback.domain.matching.implement.strategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingApplication;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingResult;
import org.smu.randsome.randsomeback.domain.matching.entity.vo.IdealTypePreference;
import org.smu.randsome.randsomeback.domain.matching.enums.MatchingType;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.enums.Department;
import org.smu.randsome.randsomeback.domain.member.enums.Gender;
import org.smu.randsome.randsomeback.domain.member.implement.MemberReader;
import org.springframework.stereotype.Component;

/**
 * 신청자가 제출한 이상형 태그를 기준으로 후보군을 평가하여 점수 상위 N명을 매칭 결과로 생성하는 전략이다.
 * 각 태그 일치 시 1점씩 부여하며(최대 3점), 동점은 셔플로 무작위 처리한다.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class IdealMatchingStrategy implements MatchingStrategy {

    private final MemberReader memberReader;

    @Override
    public MatchingType getSupportedType() {
        return MatchingType.IDEAL;
    }

    /**
     * 이상형 태그 점수 기준으로 후보군을 정렬하고 신청 수만큼 결과를 생성한다.
     *
     * @param matchingApplication 승인된 매칭 신청 (idealTypePreference 포함)
     * @return 이상형 매칭 결과 목록
     */
    @Override
    public List<MatchingResult> execute(MatchingApplication matchingApplication) {
        Gender targetGender = matchingApplication.getTargetGender();
        Department applicantDepartment = matchingApplication.getMember().getDepartment();
        IdealTypePreference preference = matchingApplication.getIdealTypePreference();

        List<Member> candidates = new ArrayList<>(memberReader.findAllCandidatesByGender(targetGender, applicantDepartment));

        log.debug("[IdealMatchingStrategy] 후보 조회 완료 - matchingApplicationId: {}, targetGender: {}, candidateCount: {}",
                matchingApplication.getId(), targetGender, candidates.size());

        // NOTE: 동점자 간 순서를 무작위로 만들기 위해 정렬 전 셔플한다.
        Collections.shuffle(candidates);

        List<MatchingResult> results = candidates.stream()
                .map(candidate -> new ScoredCandidate(candidate, preference.scoreAgainst(candidate.getMyProfileTags())))
                .sorted(Comparator.comparingInt(ScoredCandidate::score).reversed())
                .limit(matchingApplication.getApplicationCount())
                .map(scored -> MatchingResult.create(matchingApplication, scored.candidate()))
                .toList();

        log.info("[IdealMatchingStrategy] 이상형 매칭 결과 생성 완료 - matchingApplicationId: {}, requestedCount: {}, resultCount: {}",
                matchingApplication.getId(), matchingApplication.getApplicationCount(), results.size());

        return results;
    }

    private record ScoredCandidate(Member candidate, int score) {

    }

}
