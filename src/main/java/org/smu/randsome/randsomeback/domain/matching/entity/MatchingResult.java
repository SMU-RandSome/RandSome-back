package org.smu.randsome.randsomeback.domain.matching.entity;

import static java.util.Objects.requireNonNull;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.global.entity.BaseEntity;

/**
 * 매칭의 결과를 나타내는 엔티티다.
 * <br/>매칭 신청이 완료되면, 매칭 알고리즘에 의해 선정된 후보자들과의 쌍을 저장한다.
 * <br/>하나의 매칭 신청에 대해 여러 개의 매칭 결과가 존재할 수 있다 (신청 인원 수만큼).
 */
@Table(indexes = {
        @Index(name = "idx_matching_result_candidate_status", columnList = "candidate_id, status")})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class MatchingResult extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private MatchingApplication matchingApplication;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Member candidate;

    /**
     * 매칭 신청과 후보자를 연결하는 매칭 결과를 생성한다.
     *
     * @param matchingApplication 매칭 신청
     * @param candidate 선정된 후보자
     * @return 생성된 매칭 결과 엔티티
     */
    public static MatchingResult create(MatchingApplication matchingApplication, Member candidate) {
        MatchingResult result = new MatchingResult();

        result.matchingApplication = requireNonNull(matchingApplication);
        result.candidate = requireNonNull(candidate);

        return result;
    }

}