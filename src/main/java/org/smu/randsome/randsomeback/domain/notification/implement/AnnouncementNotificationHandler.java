package org.smu.randsome.randsomeback.domain.notification.implement;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smu.randsome.randsomeback.domain.announcement.event.AnnouncementRegisteredEvent;
import org.smu.randsome.randsomeback.domain.member.entity.MemberDevice;
import org.smu.randsome.randsomeback.domain.member.implement.MemberDeviceReader;
import org.smu.randsome.randsomeback.global.support.notification.NotificationType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class AnnouncementNotificationHandler {

    private final MemberDeviceReader memberDeviceReader;
    private final NotificationManager notificationManager;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(AnnouncementRegisteredEvent event) {
        try {
            List<MemberDevice> devices = memberDeviceReader.findAllActive();

            if (devices.isEmpty()) {
                log.info("[AnnouncementNotificationHandler] 알림을 받을 활성화된 디바이스가 없습니다. 알림 전송을 건너뜁니다.");
                return;
            }

            List<Long> memberIds = devices.stream().map(d -> d.getMember().getId()).toList();
            List<String> fcmTokens = devices.stream().map(MemberDevice::getDeviceToken).toList();

            notificationManager.sendToAll(memberIds, fcmTokens, NotificationType.ANNOUNCEMENT_REGISTERED);

            log.info("[AnnouncementNotificationHandler] 공지사항 알림 전송 요청 완료. announcementId={}, 대상 인원={}", event.announcementId(), memberIds.size());
        } catch (Exception e) {
            log.error("[AnnouncementNotificationHandler] 공지사항 알림 전송 중 오류 발생. announcementId={}",event.announcementId(), e);
            // TODO: Slack 알림 전송 - DB 조회 또는 FCM 전송 자체가 실패한 경우이므로 즉시 알림 필요
        }
    }

}