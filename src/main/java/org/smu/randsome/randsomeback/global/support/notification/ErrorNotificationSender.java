package org.smu.randsome.randsomeback.global.support.notification;

public interface ErrorNotificationSender {

    void sendErrorNotification(String message, Throwable throwable);

}