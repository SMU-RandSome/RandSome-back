package org.smu.randsome.randsomeback.infrastructure.notification;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.SendResponse;
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
    FirebaseMessaging firebaseMessaging;

    @Test
    void FCM_알림을_정상적으로_발송한다() throws FirebaseMessagingException {
        // given
        var tokens = List.of("token1", "token2");
        var response = mock(BatchResponse.class);
        given(response.getSuccessCount()).willReturn(2);
        given(response.getFailureCount()).willReturn(0);
        given(firebaseMessaging.sendEachForMulticast(any(MulticastMessage.class))).willReturn(response);

        // when
        fcmNotificationSender.sendNotification(tokens, NotificationType.ANNOUNCEMENT_REGISTERED);

        // then
        verify(firebaseMessaging, times(1)).sendEachForMulticast(any(MulticastMessage.class));
    }

    @Test
    void 토큰이_500개를_초과하면_청크로_나눠_발송한다() throws FirebaseMessagingException {
        // given - 501개 → 500 + 1로 분할
        var tokens = Collections.nCopies(501, "token");
        var response = mock(BatchResponse.class);
        given(response.getSuccessCount()).willReturn(500);
        given(response.getFailureCount()).willReturn(0);
        given(firebaseMessaging.sendEachForMulticast(any(MulticastMessage.class))).willReturn(response);

        // when
        fcmNotificationSender.sendNotification(tokens, NotificationType.ANNOUNCEMENT_REGISTERED);

        // then
        verify(firebaseMessaging, times(2)).sendEachForMulticast(any(MulticastMessage.class));
    }

    @Test
    void FirebaseMessagingException_발생시_예외가_전파되지_않는다() throws FirebaseMessagingException {
        // given
        var tokens = List.of("token1");
        var fcmException = mock(FirebaseMessagingException.class);
        willThrow(fcmException).given(firebaseMessaging).sendEachForMulticast(any(MulticastMessage.class));

        // when & then
        assertThatNoException().isThrownBy(
                () -> fcmNotificationSender.sendNotification(tokens, NotificationType.ANNOUNCEMENT_REGISTERED)
        );
    }

    @Test
    void 일부_토큰_발송_실패시_예외가_전파되지_않는다() throws FirebaseMessagingException {
        // given
        var tokens = List.of("valid_token", "invalid_token");

        var successResponse = mock(SendResponse.class);
        given(successResponse.isSuccessful()).willReturn(true);

        var failResponse = mock(SendResponse.class);
        given(failResponse.isSuccessful()).willReturn(false);
        given(failResponse.getException()).willReturn(mock(FirebaseMessagingException.class));

        var batchResponse = mock(BatchResponse.class);
        given(batchResponse.getSuccessCount()).willReturn(1);
        given(batchResponse.getFailureCount()).willReturn(1);
        given(batchResponse.getResponses()).willReturn(List.of(successResponse, failResponse));
        given(firebaseMessaging.sendEachForMulticast(any(MulticastMessage.class))).willReturn(batchResponse);

        // when & then
        assertThatNoException().isThrownBy(
                () -> fcmNotificationSender.sendNotification(tokens, NotificationType.ANNOUNCEMENT_REGISTERED)
        );
    }

}