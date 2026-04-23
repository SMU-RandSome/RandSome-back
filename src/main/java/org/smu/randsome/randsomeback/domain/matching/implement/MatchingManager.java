package org.smu.randsome.randsomeback.domain.matching.implement;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smu.randsome.randsomeback.domain.matching.dto.command.NewMatching;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingApplication;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingResult;
import org.smu.randsome.randsomeback.domain.matching.enums.MatchingType;
import org.smu.randsome.randsomeback.domain.matching.event.MatchingApplicationCompletedEvent;
import org.smu.randsome.randsomeback.domain.matching.implement.strategy.MatchingStrategy;
import org.smu.randsome.randsomeback.domain.matching.repository.MatchingJpaRepository;
import org.smu.randsome.randsomeback.domain.matching.repository.MatchingResultJpaRepository;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.implement.MemberReader;
import org.smu.randsome.randsomeback.global.config.CacheKeys;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.smu.randsome.randsomeback.infrastructure.redis.RedisRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Component
public class MatchingManager {

    private final MatchingJpaRepository matchingJpaRepository;
    private final MatchingResultJpaRepository matchingResultJpaRepository;
    private final RedisRepository redisRepository;
    private final MemberReader memberReader;
    private final List<MatchingStrategy> strategies;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 매칭 신청을 생성한다.
     * </br> 동일한 회원이 동일한 매칭 타입과 신청 횟수로 5초 안에 중복 신청하는 것을 방지하기 위해, idempotencyKey를 활용하여 캐시에서 중복 여부를 확인한다.
     *
     * @param newMatching 매칭 신청 커맨드
     * @param memberId    신청자 식별자
     * @return 저장된 매칭 신청 엔티티
     */
    public MatchingApplication apply(NewMatching newMatching, Long memberId) {
        Member member = memberReader.find(memberId);

        assertNotDuplicateAndMark(newMatching, memberId);

        MatchingApplication saved = matchingJpaRepository.save(MatchingApplication.apply(
                member,
                newMatching.matchingType(),
                newMatching.applicationCount(),
                newMatching.idealTypePreference()
        ));

        log.info("[MatchingManager] 매칭 신청 생성 완료 - matchingApplicationId: {}, memberId: {}, matchingType: {}, applicationCount: {}",
                saved.getId(), memberId, saved.getMatchingType(), saved.getApplicationCount());

        return saved;
    }

    private void assertNotDuplicateAndMark(NewMatching newMatching, Long memberId) {
        String idempotencyKey = CacheKeys.matchingIdempotency(memberId, newMatching.matchingType(), newMatching.applicationCount());
        if (!redisRepository.tryAcquire(idempotencyKey, Duration.ofSeconds(10))) {
            throw new CoreException(ErrorType.TOO_MANY_MATCHING_REQUESTS);
        }
    }

    /**
     * 매칭 신청에 대해 타입별 전략으로 매칭 결과를 생성하고, 신청을 완료 상태로 전이한다.
     * 매칭 완료 후 피드에 기록할 수 있도록 이벤트를 발행한다.
     *
     * @param matchingApplication 매칭 신청 엔티티
     * @param completedAt   매칭 완료 시각
     * @throws CoreException 매칭 신청을 찾을 수 없거나 지원 전략이 없는 경우
     */
    @Transactional
    public void executeMatching(MatchingApplication matchingApplication, LocalDateTime completedAt) {
        MatchingStrategy strategy = resolveStrategy(matchingApplication.getMatchingType());
        List<MatchingResult> results = strategy.execute(matchingApplication);

        matchingResultJpaRepository.saveAll(results);
        matchingApplication.complete(completedAt, results.size());

        log.info("[MatchingManager] 매칭 완료 - matchingApplicationId: {}, matchingType: {}, applicationCount: {}, resultCount: {}",
                matchingApplication.getId(), matchingApplication.getMatchingType(), matchingApplication.getApplicationCount(), results.size());

        eventPublisher.publishEvent(new MatchingApplicationCompletedEvent(
                matchingApplication.getId(),
                matchingApplication.getMember().getNickname(),
                matchingApplication.getApplicationCount(),
                matchingApplication.getMatchedCount(),
                matchingApplication.getApplicationStatus()
        ));
    }

    /**
     * 매칭 신청을 취소한다. 승인된 신청은 취소할 수 없으며, 거절된 신청은 이미 매칭 결과가 생성되어 있을 수 있으므로 취소할 수 없다.
     *
     * @param applicationId 매칭 신청 식별자
     * @param memberId      신청자 식별자 (보안 검증용)
     * @throws CoreException 매칭 신청을 찾을 수 없거나, 승인된 신청이거나, 거절된 신청인 경우
     *
     */
    public void cancel(Long applicationId, Long memberId) {
        MatchingApplication matchingApplication = matchingJpaRepository.findByIdAndMemberIdAndStatus(
                applicationId,
                memberId,
                EntityStatus.ACTIVE
        ).orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_MATCHING));

        LocalDateTime cancelledAt = LocalDateTime.now();
        matchingApplication.cancel(cancelledAt);
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