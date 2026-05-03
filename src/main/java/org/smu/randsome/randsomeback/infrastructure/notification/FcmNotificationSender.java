package org.smu.randsome.randsomeback.infrastructure.notification;

import com.google.common.collect.Lists;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smu.randsome.randsomeback.global.support.notification.ErrorNotificationSender;
import org.smu.randsome.randsomeback.global.support.notification.NotificationSender;
import org.smu.randsome.randsomeback.global.support.notification.NotificationType;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class FcmNotificationSender implements NotificationSender {

    private final FcmChunkSender fcmChunkSender;
    private final ErrorNotificationSender errorNotificationSender;

    @Override
    public void sendNotification(List<String> fcmTokens, NotificationType type) {
        Lists.partition(fcmTokens, 500)
                .forEach(chunk -> {
                    try {
                        fcmChunkSender.send(chunk, type);
                    } catch (Exception e) {
                        log.error("[FCM] 청크 발송 최종 실패, 다음 청크로 진행. size={}", chunk.size(), e);
                        errorNotificationSender.sendErrorNotification("[FCM] 청크 발송 최종 실패. size=" + chunk.size(), e);
                    }
                });
    }

}