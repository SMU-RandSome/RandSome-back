package org.smu.randsome.randsomeback.domain.notification.implement;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smu.randsome.randsomeback.domain.notification.repository.NotificationBulkRepository;
import org.smu.randsome.randsomeback.global.support.notification.NotificationSender;
import org.smu.randsome.randsomeback.global.support.notification.NotificationType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Component
public class NotificationManager {

    private final NotificationBulkRepository notificationBulkRepository;
    private final NotificationSender notificationSender;

    /**
     * 모든 사용자에게 알림을 전송하는 메서드입니다.
     * @param memberIds 알림을 받을 회원들의 ID 리스트입니다.
     * @param fcmTokens 알림을 받을 회원들의 FCM 토큰 리스트입니다.
     * @param type 전송할 알림의 유형입니다.
     *
     * */
    @Transactional
    public void sendToAll(List<Long> memberIds, List<String> fcmTokens, NotificationType type) {
        // DB에 먼저 저장해 인앱 알림 이력을 보장한다.
        // FCM 전송은 Best-effort로, 실패해도 이력은 유지된다.
        notificationBulkRepository.saveAll(memberIds, type, LocalDateTime.now());

        notificationSender.sendNotification(fcmTokens, type);
    }

}