package org.smu.randsome.randsomeback.domain.matching.implement;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.matching.dto.command.MatchingSearchCondition;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingApplication;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingResult;
import org.smu.randsome.randsomeback.domain.matching.repository.MatchingRepository;
import org.smu.randsome.randsomeback.domain.matching.repository.MatchingResultJpaRepository;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.smu.randsome.randsomeback.global.support.response.OffsetLimit;
import org.smu.randsome.randsomeback.global.support.response.PageResponse;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class MatchingReader {

    private final MatchingRepository matchingRepository;
    private final MatchingResultJpaRepository matchingResultJpaRepository;

    // NOTE: 회원당 매칭 수가 많지 않을 것으로 예상되어 페이징 없이 전체 조회한다. 필요 시 페이징 추가 가능하다.
    @Transactional(readOnly = true)
    public List<MatchingApplication> findMatchings(Long memberId) {
        return matchingRepository.findAllByMemberIdAndStatus(
                memberId,
                EntityStatus.ACTIVE
        );
    }

    /**
     * 특정 매칭 신청의 결과를 조회한다.
     * <br/>매칭이 완료된 신청만 결과가 존재하므로, 결과가 없다면 승인되지 않은 신청으로 간주한다.
     * <br/>신청자(memberId)의 신청이 맞는지 보안 검증을 포함한다.
     *
     * @param applicationId 매칭 신청 식별자
     * @param memberId 신청자 식별자 (보안 검증용)
     * @return 매칭 결과 리스트 (후보자 정보 포함)
     * @throws CoreException 결과가 없거나 신청자가 일치하지 않는 경우
     */
    @Transactional(readOnly = true)
    public List<MatchingResult> findApplication(Long applicationId, Long memberId) {
        List<MatchingResult> matchingResults = matchingResultJpaRepository.findAllByApplicationAndMemberIdAndStatus(
                applicationId,
                memberId,
                EntityStatus.ACTIVE
        );

        // 매칭이 완료되었다면 결과가 존재해야 한다. 결과가 없다면 승인된 매칭이 없는 것으로 간주한다.
        if (matchingResults.isEmpty()) {
            throw new CoreException(ErrorType.NOT_FOUND_APPROVED_MATCHING);
        }

        return matchingResults;
    }

    /**
     * 매칭 결과를 ID로 단건 조회한다.
     *
     * @param matchingResultId 매칭 결과 식별자
     * @return 매칭 결과 엔티티 (신청 정보 함께 로드)
     * @throws CoreException 매칭 결과를 찾을 수 없는 경우
     */
    public MatchingResult findMatchingResult(Long matchingResultId) {
        return matchingResultJpaRepository.findByIdAndStatusWithMatchingApplication(matchingResultId, EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_MATCHING_RESULT));
    }

    /**
     * 회원이 다른 사용자의 매칭 결과로 노출된 횟수를 조회한다.
     * <br/>즉, 이 회원을 후보자로 제시한 매칭 결과의 개수를 반환한다.
     *
     * @param memberId 회원 식별자
     * @return 후보자로 노출된 횟수
     */
    @Transactional(readOnly = true)
    public long countExposures(Long memberId) {
        return matchingResultJpaRepository.countByCandidateIdAndStatus(memberId, EntityStatus.ACTIVE);
    }

    /**
     * 매칭 신청 목록을 검색 조건에 따라 페이징 조회한다.
     * <br/>검색 조건에는 신청자 ID, 매칭 상태, 신청 날짜 범위 등이 포함될 수 있다.
     * <br/>반환되는 PageResponse에는 매칭 신청 리스트와 함께 전체 결과 수가 포함되어 클라이언트에서 페이지네이션 UI를 구현할 수 있도록 한다.
     * @param condition 검색 조건
     * @param offsetLimit 페이지네이션 정보 (페이지 번호, 페이지 크기)
     * @return 검색 조건에 맞는 매칭 신청의 페이징 결과
    * */
    @Transactional(readOnly = true)
    public PageResponse<MatchingApplication> findAllByFilter(MatchingSearchCondition condition, OffsetLimit offsetLimit) {
        List<MatchingApplication> items = matchingRepository.findAllByFilter(condition, offsetLimit.offset(), offsetLimit.limit());
        long total = matchingRepository.countByFilter(condition);

        return PageResponse.of(items, offsetLimit.page(), offsetLimit.size(), total);
    }

}