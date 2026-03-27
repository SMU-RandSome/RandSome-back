package org.smu.randsome.randsomeback.infrastructure.notification;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.SendResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.global.support.notification.ErrorNotificationSender;
import org.smu.randsome.randsomeback.global.support.notification.NotificationType;

class FcmChunkSenderUnitTest extends UnitTestSupport {

    @InjectMocks
    FcmChunkSender fcmChunkSender;

    @Mock
    FirebaseMessaging firebaseMessaging;

    @Mock
    ErrorNotificationSender errorNotificationSender;

    @Test
    void 청크를_정상_발송한다() throws FirebaseMessagingException {
        // given
        var tokens = List.of("token1", "token2");
        var response = mock(BatchResponse.class);
        given(response.getSuccessCount()).willReturn(2);
        given(response.getFailureCount()).willReturn(0);
        given(firebaseMessaging.sendEachForMulticast(any(MulticastMessage.class))).willReturn(response);

        // when & then
        assertThatNoException().isThrownBy(
                () -> fcmChunkSender.send(tokens, NotificationType.ANNOUNCEMENT_REGISTERED)
        );
        verify(firebaseMessaging, times(1)).sendEachForMulticast(any(MulticastMessage.class));
    }

    @Test
    void 일부_토큰_발송_실패시_예외_없이_실패_토큰을_로그에_기록한다() throws FirebaseMessagingException {
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

        // when & then - 예외 없이 처리되어야 함 (재시도는 Spring 프록시가 담당)
        assertThatNoException().isThrownBy(
                () -> fcmChunkSender.send(tokens, NotificationType.ANNOUNCEMENT_REGISTERED)
        );
    }

    @Test
    void FirebaseMessagingException_발생시_예외가_전파된다() throws FirebaseMessagingException {
        // given - Spring 프록시 없이 직접 호출 시 예외가 전파되어야 재시도 가능
        var tokens = List.of("token1");
        var exception = mock(FirebaseMessagingException.class);
        given(firebaseMessaging.sendEachForMulticast(any(MulticastMessage.class))).willThrow(exception);

        // when & then
        assertThatThrownBy(() -> fcmChunkSender.send(tokens, NotificationType.ANNOUNCEMENT_REGISTERED))
                .isInstanceOf(FirebaseMessagingException.class);
    }

    @Test
    void recover_최종_실패시_에러_알림을_전송한다() {
        // given
        var tokens = List.of("token1");
        var exception = mock(FirebaseMessagingException.class);

        // when
        fcmChunkSender.recover(exception, tokens, NotificationType.ANNOUNCEMENT_REGISTERED);

        // then
        verify(errorNotificationSender).sendErrorNotification(any(), eq(exception));
    }
}
