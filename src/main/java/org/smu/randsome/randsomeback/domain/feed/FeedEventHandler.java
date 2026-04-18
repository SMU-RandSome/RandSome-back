package org.smu.randsome.randsomeback.domain.feed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smu.randsome.randsomeback.domain.candidate.event.CandidateRegistrationApprovedEvent;
import org.smu.randsome.randsomeback.domain.matching.event.MatchingApplicationCompletedEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 결제 승인 후 피드 기록을 담당한다.
 * 트랜잭션 커밋 이후 별도 트랜잭션으로 실행되므로, 피드 기록 실패가 결제·신청 승인에 영향을 주지 않는다.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class FeedEventHandler {

    private final FeedManager feedManager;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onCandidateRegistrationApproved(CandidateRegistrationApprovedEvent event) {
        try {
            feedManager.recordCandidateRegistration(event.nickname());
        } catch (Exception e) {
            log.error("[FeedEventHandler] 후보자 등록 피드 기록 실패 - nickname={}", event.nickname(), e);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onMatchingApplicationSuccess(MatchingApplicationCompletedEvent event) {
        try {
            feedManager.recordMatchRequest(event.nickname(), event.count());
        } catch (Exception e) {
            log.error("[FeedEventHandler] 매칭 신청 피드 기록 실패 - nickname={}, count={}", event.nickname(), event.count(), e);
        }
    }

}