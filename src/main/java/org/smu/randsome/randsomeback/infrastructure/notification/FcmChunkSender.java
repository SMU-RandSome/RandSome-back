package org.smu.randsome.randsomeback.infrastructure.notification;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.SendResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.smu.randsome.randsomeback.global.support.notification.NotificationType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class FcmChunkSender {

    private final FirebaseMessaging firebaseMessaging;

    @Retryable(
            retryFor = {FirebaseMessagingException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    @SneakyThrows // FirebaseMessagingException은 체크 예외이므로, @SneakyThrows로 처리하여 호출부에서 예외 처리 부담을 줄임
    public void send(List<String> chunk, NotificationType type) {
        MulticastMessage message = MulticastMessage.builder()
                .addAllTokens(chunk)
                .setNotification(Notification.builder()
                        .setTitle(type.getTitle())
                        .setBody(type.getMessage())
                        .build())
                .putData("type", type.name())
                .build();

        BatchResponse response = firebaseMessaging.sendEachForMulticast(message);
        log.info("[FCM] 청크 발송 완료. 성공={}, 실패={}", response.getSuccessCount(), response.getFailureCount());
        logFailedTokens(chunk, response);
    }

    @Recover
    public void recover(FirebaseMessagingException e, List<String> chunk, NotificationType type) {
        log.error("[FCM] 청크 최종 발송 실패. size={}", chunk.size(), e);
        throw new CoreException(ErrorType.SEND_NOTIFICATION_ERROR, e);
    }

    private void logFailedTokens(List<String> chunk, BatchResponse response) {
        if (response.getFailureCount() > 0) {
            List<SendResponse> responses = response.getResponses();
            for (int i = 0; i < responses.size(); i++) {
                SendResponse sendResponse = responses.get(i);
                if (!sendResponse.isSuccessful()) {
                    MessagingErrorCode errorCode = sendResponse.getException().getMessagingErrorCode();
                    log.warn("[FCM] 토큰 발송 실패. token={}, errorCode={}", chunk.get(i), errorCode);
                }
            }
        }
    }
}
