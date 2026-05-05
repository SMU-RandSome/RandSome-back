package org.smu.randsome.randsomeback.global.support.notification;

import java.util.List;

public interface NotificationSender {

    void sendNotification(List<String> fcmToken, NotificationType type);

}