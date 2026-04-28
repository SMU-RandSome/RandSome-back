package org.smu.randsome.randsomeback.domain.notification.implement;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.announcement.event.AnnouncementRegisteredEvent;
import org.smu.randsome.randsomeback.domain.candidate.event.CandidateAppliedEvent;
import org.smu.randsome.randsomeback.domain.candidate.event.CandidateRegistrationNotificationEvent;
import org.smu.randsome.randsomeback.domain.matching.enums.ApplicationStatus;
import org.smu.randsome.randsomeback.domain.matching.event.MatchingApplicationCompletedEvent;
import org.smu.randsome.randsomeback.global.support.notification.NotificationType;

class NotificationHandlerUnitTest extends UnitTestSupport {

    @InjectMocks
    NotificationHandler notificationHandler;

    @Mock
    NotificationAsyncExecutor notificationAsyncExecutor;

    @Test
    void 공지사항_이벤트_수신시_비동기_실행기에_위임한다() {
        // given
        var event = new AnnouncementRegisteredEvent(1L);

        // when
        notificationHandler.announcementNotify(event);

        // then
        verify(notificationAsyncExecutor).executeAnnouncementNotify(1L);
    }

    @Test
    void 매칭_신청_이벤트_수신시_비동기_실행기에_위임한다() {
        // given
        var event = new MatchingApplicationCompletedEvent(5L, "nickname", 2, 2, ApplicationStatus.SUCCESS);

        // when
        notificationHandler.matchingApplicationNotify(event);

        // then
        verify(notificationAsyncExecutor).executeMatchingApplicationNotify(5L);
    }

    @Test
    void 후보자_신청_이벤트_수신시_비동기_실행기에_위임한다() {
        // given
        var event = new CandidateAppliedEvent(7L);

        // when
        notificationHandler.candidateRegistrationNotify(event);

        // then
        verify(notificationAsyncExecutor).executeCandidateRegistrationNotify(7L);
    }

    @Test
    void 후보자_승인거절_이벤트_수신시_비동기_실행기에_위임한다() {
        // given
        var event = new CandidateRegistrationNotificationEvent(3L, NotificationType.CANDIDATE_APPROVED);

        // when
        notificationHandler.notifyCandidateRegistrationResult(event);

        // then
        verify(notificationAsyncExecutor).executeCandidateRegistrationResultNotify(3L, NotificationType.CANDIDATE_APPROVED);
    }

}
