package org.smu.randsome.randsomeback.domain.notification.implement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smu.randsome.randsomeback.domain.announcement.event.AnnouncementRegisteredEvent;
import org.smu.randsome.randsomeback.domain.candidate.event.CandidateAppliedEvent;
import org.smu.randsome.randsomeback.domain.candidate.event.CandidateRegistrationNotificationEvent;
import org.smu.randsome.randsomeback.domain.matching.event.MatchingApplicationCompletedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class NotificationHandler {

    private final NotificationAsyncExecutor notificationAsyncExecutor;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void announcementNotify(AnnouncementRegisteredEvent event) {
        notificationAsyncExecutor.executeAnnouncementNotify(event.announcementId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void matchingApplicationNotify(MatchingApplicationCompletedEvent event) {
        notificationAsyncExecutor.executeMatchingApplicationNotify(event.applicationId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void candidateRegistrationNotify(CandidateAppliedEvent event) {
        notificationAsyncExecutor.executeCandidateRegistrationNotify(event.candidateRegistrationId());
    }

    /**
     * 후보자 승인/거절 알림 전송.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void notifyCandidateRegistrationResult(CandidateRegistrationNotificationEvent event) {
        notificationAsyncExecutor.executeCandidateRegistrationResultNotify(
                event.candidateRegistrationId(),
                event.notificationType()
        );
    }

}
