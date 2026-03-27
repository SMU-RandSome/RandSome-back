package org.smu.randsome.randsomeback.infrastructure.notification;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MulticastMessage;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.smu.randsome.randsomeback.IntegrationTestSupport;
import org.smu.randsome.randsomeback.global.support.notification.ErrorNotificationSender;
import org.smu.randsome.randsomeback.global.support.notification.NotificationType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * @Retryable은 Spring AOP 프록시를 통해 동작하므로 Spring 컨텍스트가 필요합니다.
 * 재시도 지연(1s + 2s)으로 인해 전체 실패 테스트는 약 3초 소요됩니다.
 */
@RequiredArgsConstructor
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = FcmChunkSenderRetryTest.TestConfig.class)
class FcmChunkSenderRetryTest extends IntegrationTestSupport {

    @Configuration
    @EnableRetry
    static class TestConfig {

        @Bean
        public FirebaseMessaging firebaseMessaging() {
            return Mockito.mock(FirebaseMessaging.class);
        }

        @Bean
        public ErrorNotificationSender errorNotificationSender() {
            return Mockito.mock(ErrorNotificationSender.class);
        }

        @Bean
        public FcmChunkSender fcmChunkSender(FirebaseMessaging firebaseMessaging,
                ErrorNotificationSender errorNotificationSender) {
            return new FcmChunkSender(firebaseMessaging, errorNotificationSender);
        }
    }

    final FcmChunkSender fcmChunkSender;
    final FirebaseMessaging firebaseMessaging;
    final ErrorNotificationSender errorNotificationSender;

    @BeforeEach
    void setUp() {
        reset(firebaseMessaging, errorNotificationSender);
    }

    @Test
    void FirebaseMessagingException_발생시_3회_재시도_후_recover를_호출한다() throws FirebaseMessagingException {
        // given
        var tokens = List.of("token1");
        var exception = mock(FirebaseMessagingException.class);
        given(firebaseMessaging.sendEachForMulticast(any(MulticastMessage.class))).willThrow(exception);

        // when - recover가 예외를 흡수하므로 호출부에서 예외 없음
        assertThatNoException().isThrownBy(
                () -> fcmChunkSender.send(tokens, NotificationType.ANNOUNCEMENT_REGISTERED)
        );

        // then
        verify(firebaseMessaging, times(3)).sendEachForMulticast(any(MulticastMessage.class));
        verify(errorNotificationSender).sendErrorNotification(any(), eq(exception));
    }

    @Test
    void 재시도_중_성공하면_recover가_호출되지_않는다() throws FirebaseMessagingException {
        // given - 첫 번째 시도 실패, 두 번째 시도 성공
        var tokens = List.of("token1");
        var exception = mock(FirebaseMessagingException.class);
        var batchResponse = mock(BatchResponse.class);
        given(batchResponse.getSuccessCount()).willReturn(1);
        given(batchResponse.getFailureCount()).willReturn(0);

        given(firebaseMessaging.sendEachForMulticast(any(MulticastMessage.class)))
                .willThrow(exception)
                .willReturn(batchResponse);

        // when
        assertThatNoException().isThrownBy(
                () -> fcmChunkSender.send(tokens, NotificationType.ANNOUNCEMENT_REGISTERED)
        );

        // then
        verify(firebaseMessaging, times(2)).sendEachForMulticast(any(MulticastMessage.class));
        verify(errorNotificationSender, never()).sendErrorNotification(any(), any());
    }

}