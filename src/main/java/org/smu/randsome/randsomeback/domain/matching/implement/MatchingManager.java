package org.smu.randsome.randsomeback.domain.matching.implement;

import java.time.Duration;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smu.randsome.randsomeback.domain.matching.dto.command.NewMatching;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingApplication;
import org.smu.randsome.randsomeback.domain.matching.entity.MatchingIdealTypeSnapshot;
import org.smu.randsome.randsomeback.domain.matching.repository.MatchingIdealTypeSnapshotJpaRepository;
import org.smu.randsome.randsomeback.domain.matching.repository.MatchingJpaRepository;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.implement.MemberReader;
import org.smu.randsome.randsomeback.global.config.CacheKeys;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.smu.randsome.randsomeback.infrastructure.redis.RedisRepository;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class MatchingManager {

    private final MatchingJpaRepository matchingJpaRepository;
    private final MatchingIdealTypeSnapshotJpaRepository snapshotJpaRepository;
    private final RedisRepository redisRepository;
    private final MemberReader memberReader;

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
                newMatching.applicationCount()
        ));

        if (newMatching.idealTypePreference() != null) {
            snapshotJpaRepository.save(MatchingIdealTypeSnapshot.create(
                    saved.getId(),
                    newMatching.idealTypePreference()
            ));
        }

        log.info(
                "[MatchingManager] 매칭 신청 생성 완료 - matchingApplicationId: {}, memberId: {}, matchingType: {}, applicationCount: {}",
                saved.getId(), memberId, saved.getMatchingType(), saved.getApplicationCount());

        return saved;
    }

    /**
     * 매칭 신청을 취소한다. 승인된 신청은 취소할 수 없으며, 거절된 신청은 이미 매칭 결과가 생성되어 있을 수 있으므로 취소할 수 없다.
     *
     * @param applicationId 매칭 신청 식별자
     * @param memberId      신청자 식별자 (보안 검증용)
     * @throws CoreException 매칭 신청을 찾을 수 없거나, 승인된 신청이거나, 거절된 신청인 경우
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

    private void assertNotDuplicateAndMark(NewMatching newMatching, Long memberId) {
        String idempotencyKey = CacheKeys.matchingIdempotency(
                memberId,
                newMatching.matchingType(),
                newMatching.applicationCount()
        );
        if (!redisRepository.tryAcquire(idempotencyKey, Duration.ofSeconds(5))) {
            throw new CoreException(ErrorType.TOO_MANY_MATCHING_REQUESTS);
        }
    }

}