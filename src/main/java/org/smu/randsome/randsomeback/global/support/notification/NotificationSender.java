package org.smu.randsome.randsomeback.global.support.notification;

public interface NotificationSender {

    void sendNotification(String fcmToken, String title, String body, NotificationType type);

}