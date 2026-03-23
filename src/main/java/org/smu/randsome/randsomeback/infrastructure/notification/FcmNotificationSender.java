package org.smu.randsome.randsomeback.infrastructure.notification;

import com.google.common.collect.Lists;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.SendResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smu.randsome.randsomeback.global.support.notification.NotificationSender;
import org.smu.randsome.randsomeback.global.support.notification.NotificationType;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class FcmNotificationSender implements NotificationSender {

    private final FirebaseMessaging firebaseMessaging;

    @Override
    public void sendNotification(List<String> fcmTokens, NotificationType type) {
        List<List<String>> chunks = Lists.partition(fcmTokens, 500); // Guava

        for (List<String> chunk : chunks) {
            MulticastMessage message = MulticastMessage.builder()
                    .addAllTokens(chunk)
                    .setNotification(Notification.builder()
                            .setTitle(type.getTitle())
                            .setBody(type.getMessage())
                            .build())
                    .putData("type", type.name())
                    .build();

            try {
                BatchResponse response = firebaseMessaging.sendEachForMulticast(message);
                log.info("[FCM] 발송 완료. 성공={}, 실패={}", response.getSuccessCount(), response.getFailureCount());

                logFailedTokens(chunk, response);
            } catch (FirebaseMessagingException e) {
                log.error("[FCM] 멀티캐스트 발송 실패", e);
                // TODO: Slack 알림 전송 - 청크 단위 전체 실패이므로 즉시 알림 필요
            }
        }
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