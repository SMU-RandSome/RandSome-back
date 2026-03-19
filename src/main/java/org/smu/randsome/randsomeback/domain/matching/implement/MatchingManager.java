package org.smu.randsome.randsomeback.domain.matching.implement;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smu.randsome.randsomeback.domain.matching.dto.command.NewMatching;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingApplication;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingResult;
import org.smu.randsome.randsomeback.domain.matching.enums.MatchingType;
import org.smu.randsome.randsomeback.domain.matching.implement.strategy.MatchingStrategy;
import org.smu.randsome.randsomeback.domain.matching.repository.MatchingJpaRepository;
import org.smu.randsome.randsomeback.domain.matching.repository.MatchingResultJpaRepository;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.implement.MemberReader;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Component
public class MatchingManager {

    private final MatchingJpaRepository matchingJpaRepository;
    private final MatchingResultJpaRepository matchingResultJpaRepository;
    private final MemberReader memberReader;
    private final List<MatchingStrategy> strategies;

    /**
     * 매칭 신청을 생성한다.
     *
     * @param newMatching 매칭 신청 커맨드
     * @param memberId    신청자 식별자
     * @return 저장된 매칭 신청 엔티티
     */
    public MatchingApplication apply(NewMatching newMatching, Long memberId) {
        Member member = memberReader.find(memberId);

        MatchingApplication saved = matchingJpaRepository.save(MatchingApplication.apply(
                member,
                newMatching.matchingType(),
                newMatching.applicationCount()
        ));

        log.info("[MatchingManager] 매칭 신청 생성 완료 - matchingApplicationId: {}, memberId: {}, matchingType: {}, applicationCount: {}",
                saved.getId(), memberId, saved.getMatchingType(), saved.getApplicationCount());

        return saved;
    }

    /**
     * 결제 승인 완료된 매칭 신청을 승인 상태로 전이하고, 타입별 전략으로 매칭 결과를 생성한다.
     *
     * @param id         매칭 신청 식별자
     * @param approvedAt 승인 시각
     * @throws CoreException 매칭 신청을 찾을 수 없거나 지원 전략이 없는 경우
     */
    @Transactional
    public MatchingApplication approve(Long id, LocalDateTime approvedAt) {
        MatchingApplication matchingApplication = matchingJpaRepository.findByIdAndStatusWithMember(id, EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_MATCHING));

        matchingApplication.approve(approvedAt);

        log.debug("[MatchingManager] 매칭 승인 처리 시작 - matchingApplicationId: {}, matchingType: {}, applicationCount: {}",
                id, matchingApplication.getMatchingType(), matchingApplication.getApplicationCount());

        MatchingStrategy strategy = resolveStrategy(matchingApplication.getMatchingType());
        List<MatchingResult> results = strategy.execute(matchingApplication);

        matchingResultJpaRepository.saveAll(results);

        log.info("[MatchingManager] 매칭 완료 - matchingApplicationId: {}, matchingType: {}, applicationCount: {}, resultCount: {}",
                id, matchingApplication.getMatchingType(), matchingApplication.getApplicationCount(), results.size());

        return matchingApplication;
    }

    /**
     * 결제 거절된 매칭 신청을 거절 상태로 전이한다.
     *
     * @param id             매칭 신청 식별자
     * @param rejectedReason 거절 사유
     * @param rejectedAt     거절 시각
     * @throws CoreException 매칭 신청을 찾을 수 없는 경우
     */
    @Transactional
    public void reject(Long id, String rejectedReason, LocalDateTime rejectedAt) {
        MatchingApplication matchingApplication = matchingJpaRepository.findByIdAndStatus(id, EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_MATCHING));

        matchingApplication.reject(rejectedAt, rejectedReason);

        log.info("[MatchingManager] 매칭 거절 처리 완료 - matchingApplicationId: {}, rejectedAt: {}",
                id, rejectedAt);
    }

    /**
     * 매칭 신청을 철회한다. 승인된 신청은 철회할 수 없으며, 거절된 신청은 이미 매칭 결과가 생성되어 있을 수 있으므로 철회할 수 없다.
     * @param applicationId 매칭 신청 식별자
     * @param memberId 신청자 식별자 (보안 검증용)
     * @throws CoreException 매칭 신청을 찾을 수 없거나, 승인된 신청이거나, 거절된 신청인 경우
     * */
    @Transactional
    public void withdraw(Long applicationId, Long memberId) {
        MatchingApplication matchingApplication = matchingJpaRepository.findByIdAndMemberIdAndStatus(
                applicationId,
                memberId,
                EntityStatus.ACTIVE
        ).orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_MATCHING));

        LocalDateTime withdrawnAt = LocalDateTime.now();
        matchingApplication.withdraw(withdrawnAt);
    }

    /**
     * 매칭 타입에 해당하는 전략 구현체를 조회한다.
     *
     * @param matchingType 매칭 타입
     * @return 타입에 대응되는 전략
     * @throws CoreException 지원 전략이 없는 경우
     */
    private MatchingStrategy resolveStrategy(MatchingType matchingType) {
        return strategies.stream()
                .filter(s -> s.getSupportedType() == matchingType)
                .findFirst()
                .orElseThrow(() -> {
                    log.error("[MatchingManager] 지원하지 않는 매칭 타입 - matchingType: {}", matchingType);
                    return new CoreException(ErrorType.UNSUPPORTED_MATCHING_TYPE);
                });
    }
}