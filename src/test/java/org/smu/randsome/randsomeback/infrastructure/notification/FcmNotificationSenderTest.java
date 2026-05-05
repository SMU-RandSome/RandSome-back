package org.smu.randsome.randsomeback.infrastructure.notification;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.global.support.notification.NotificationType;

class FcmNotificationSenderTest extends UnitTestSupport {

    @InjectMocks
    FcmNotificationSender fcmNotificationSender;

    @Mock
    FcmChunkSender fcmChunkSender;

    @Test
    void FCM_알림을_정상적으로_발송한다() {
        // given
        var tokens = List.of("token1", "token2");

        // when
        fcmNotificationSender.sendNotification(tokens, NotificationType.ANNOUNCEMENT_REGISTERED);

        // then
        verify(fcmChunkSender, times(1)).send(any(), eq(NotificationType.ANNOUNCEMENT_REGISTERED));
    }

    @Test
    void 정확히_500개_토큰은_1개_청크로_발송한다() {
        // given
        var tokens = Collections.nCopies(500, "token");

        // when
        fcmNotificationSender.sendNotification(tokens, NotificationType.ANNOUNCEMENT_REGISTERED);

        // then
        verify(fcmChunkSender, times(1)).send(any(), eq(NotificationType.ANNOUNCEMENT_REGISTERED));
    }

    @Test
    void 토큰이_500개를_초과하면_청크로_나눠_발송한다() {
        // given - 501개 → 500 + 1로 분할
        var tokens = Collections.nCopies(501, "token");

        // when
        fcmNotificationSender.sendNotification(tokens, NotificationType.ANNOUNCEMENT_REGISTERED);

        // then
        verify(fcmChunkSender, times(2)).send(any(), eq(NotificationType.ANNOUNCEMENT_REGISTERED));
    }

    @Test
    void 토큰_1000개는_2개_청크로_발송한다() {
        // given - 1000개 → 500 + 500으로 분할
        var tokens = Collections.nCopies(1000, "token");

        // when
        fcmNotificationSender.sendNotification(tokens, NotificationType.ANNOUNCEMENT_REGISTERED);

        // then
        verify(fcmChunkSender, times(2)).send(any(), eq(NotificationType.ANNOUNCEMENT_REGISTERED));
    }
}