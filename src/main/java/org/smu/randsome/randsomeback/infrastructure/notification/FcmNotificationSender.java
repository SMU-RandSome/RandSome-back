package org.smu.randsome.randsomeback.infrastructure.notification;

import com.google.common.collect.Lists;
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

    private final FcmChunkSender fcmChunkSender;

    @Override
    public void sendNotification(List<String> fcmTokens, NotificationType type) {
        Lists.partition(fcmTokens, 500)
                .forEach(chunk -> fcmChunkSender.send(chunk, type));
    }

}