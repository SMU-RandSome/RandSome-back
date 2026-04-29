package org.smu.randsome.randsomeback.domain.matching.implement.strategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingApplication;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingResult;
import org.smu.randsome.randsomeback.domain.matching.entity.vo.IdealTypePreference;
import org.smu.randsome.randsomeback.domain.matching.enums.MatchingType;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.entity.MemberProfileTag;
import org.smu.randsome.randsomeback.domain.member.enums.Department;
import org.smu.randsome.randsomeback.domain.member.enums.Gender;
import org.smu.randsome.randsomeback.domain.member.enums.Mbti;
import org.smu.randsome.randsomeback.domain.member.implement.MemberProfileTagReader;
import org.smu.randsome.randsomeback.domain.member.implement.MemberReader;
import org.springframework.stereotype.Component;

/**
 * 신청자가 제출한 이상형 태그를 기준으로 후보군을 평가하여 점수 상위 N명을 매칭 결과로 생성하는 전략이다. 각 태그 일치 시 1점씩 부여하며(최대 4점), 동점은 셔플로 무작위 처리한다. 점수가 0인 후보(이상형 태그가 하나도
 * 일치하지 않는 경우)는 결과에서 제외된다.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class IdealMatchingStrategy implements MatchingStrategy {

    private final MemberReader memberReader;
    private final MemberProfileTagReader memberProfileTagReader;

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
    /**
     * 후보 ID + MBTI만 경량 조회 → 태그 스코어링 → 상위 N건만 Member 엔티티 로딩. Member 엔티티 전체 로딩(~1만 건)을 상위 N건(1~5건)으로 줄여 DB 전송량과 메모리를 절감한다.
     */
    @Override
    public List<MatchingResult> execute(MatchingApplication matchingApplication) {
        Gender targetGender = matchingApplication.getTargetGender();
        Department applicantDepartment = matchingApplication.getMember().getDepartment();

        Map<Long, Mbti> candidateMbtiMap = memberReader.findCandidateIdMbtiMap(targetGender, applicantDepartment);
        List<Long> candidateIds = new ArrayList<>(candidateMbtiMap.keySet());

        Map<Long, MemberProfileTag> profileTagMap = memberProfileTagReader.findAllByMemberIds(candidateIds);

        // NOTE: 동점자 간 순서를 무작위로 만들기 위해 정렬 전 셔플한다.
        Collections.shuffle(candidateIds);

        IdealTypePreference preference = matchingApplication.getIdealTypePreference();
        int applicationCount = matchingApplication.getApplicationCount();

        List<Long> topCandidateIds = candidateIds.stream()
                .map(id -> new ScoredCandidateId(
                        id,
                        preference.scoreAgainst(profileTagMap.get(id), candidateMbtiMap.get(id))
                ))
                .sorted(Comparator.comparingInt(ScoredCandidateId::score).reversed())
                .filter(scored -> scored.score() > 0)
                .limit(applicationCount)
                .map(ScoredCandidateId::candidateId)
                .toList();

        Map<Long, Member> memberMap = memberReader.findAllByIds(topCandidateIds);

        List<MatchingResult> results = topCandidateIds.stream()
                .map(id -> MatchingResult.create(matchingApplication, memberMap.get(id)))
                .toList();

        log.info("[IdealMatchingStrategy] 이상형 매칭 결과 생성 완료 - matchingApplicationId: {}, requestedCount: {}, resultCount: {}",
                matchingApplication.getId(), applicationCount, results.size());

        return results;
    }

    private record ScoredCandidateId(Long candidateId, int score) {

    }

}
