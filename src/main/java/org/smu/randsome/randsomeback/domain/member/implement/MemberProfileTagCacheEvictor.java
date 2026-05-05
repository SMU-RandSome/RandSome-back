package org.smu.randsome.randsomeback.domain.member.implement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smu.randsome.randsomeback.domain.member.event.MemberProfileTagUpdatedEvent;
import org.smu.randsome.randsomeback.global.config.CacheKeys;
import org.smu.randsome.randsomeback.infrastructure.redis.RedisRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 프로필 태그 변경 이벤트를 수신해 해당 회원의 프로필 태그 캐시를 무효화한다.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class MemberProfileTagCacheEvictor {

    private final RedisRepository redisRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void evict(MemberProfileTagUpdatedEvent event) {
        redisRepository.delete(CacheKeys.memberProfileTag(event.memberId()));
        log.info("[MemberProfileTagCacheEvictor] 캐시 무효화 완료. memberId={}", event.memberId());
    }

}