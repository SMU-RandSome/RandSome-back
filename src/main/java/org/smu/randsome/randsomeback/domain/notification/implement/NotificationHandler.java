package org.smu.randsome.randsomeback.domain.notification.implement;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smu.randsome.randsomeback.domain.announcement.event.AnnouncementRegisteredEvent;
import org.smu.randsome.randsomeback.domain.candidate.event.CandidateAppliedEvent;
import org.smu.randsome.randsomeback.domain.matching.event.MatchingAppliedEvent;
import org.smu.randsome.randsomeback.domain.member.entity.MemberDevice;
import org.smu.randsome.randsomeback.domain.member.implement.MemberDeviceReader;
import org.smu.randsome.randsomeback.global.support.notification.ErrorNotificationSender;
import org.smu.randsome.randsomeback.global.support.notification.NotificationSender;
import org.smu.randsome.randsomeback.global.support.notification.NotificationType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class NotificationHandler {

    private final MemberDeviceReader memberDeviceReader;
    private final NotificationManager notificationManager;
    private final NotificationSender notificationSender;
    private final ErrorNotificationSender errorNotificationSender;

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void announcementNotify(AnnouncementRegisteredEvent event) {
        try {
            List<MemberDevice> memberDevices = memberDeviceReader.findAllActive();
            sendNotificationToDevices(
                    memberDevices,
                    NotificationType.ANNOUNCEMENT_REGISTERED,
                    event.announcementId()
            );
        } catch (Exception e) {
            log.error("[NotificationHandler] 공지사항 알림 전송 중 오류 발생. announcementId={}", event.announcementId(), e);
            errorNotificationSender.sendErrorNotification(
                    "[NotificationHandler] 공지사항 알림 전송 중 오류 발생. announcementId=" + event.announcementId() + ", error: "
                            + e.getMessage(), e);
        }
    }

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void matchingApplicationNotify(MatchingAppliedEvent event) {
        try {
            List<MemberDevice> memberDevices = memberDeviceReader.findAllByAdminRole();
            sendNotificationToDevices(
                    memberDevices,
                    NotificationType.MATCHING_APPLIED_TO_ADMIN,
                    event.matchingApplicationId()
            );
        } catch (Exception e) {
            log.error("[NotificationHandler] 매칭 신청 알림 전송 중 오류 발생. matchingApplicationId={}", event.matchingApplicationId(), e);
            errorNotificationSender.sendErrorNotification(
                    "[NotificationHandler] 매칭 신청 알림 전송 중 오류 발생. matchingApplicationId=" + event.matchingApplicationId()
                            + ", error: " + e.getMessage(), e);
        }
    }

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void candidateRegistrationNotify(CandidateAppliedEvent event) {
        try {
            List<MemberDevice> memberDevices = memberDeviceReader.findAllByAdminRole();
            sendNotificationToDevices(
                    memberDevices,
                    NotificationType.CANDIDATE_APPLIED_TO_ADMIN,
                    event.candidateRegistrationId()
            );
        } catch (Exception e) {
            log.error("[NotificationHandler] 후보자 신청 알림 전송 중 오류 발생. candidateRegistrationId={}", event.candidateRegistrationId(),
                    e);
            errorNotificationSender.sendErrorNotification(
                    "[NotificationHandler] 후보자 신청 알림 전송 중 오류 발생. candidateRegistrationId=" + event.candidateRegistrationId()
                            + ", error: " + e.getMessage(), e);
        }
    }
    // TODO: 후보자 승인/거절 알림

    private void sendNotificationToDevices(List<MemberDevice> devices, NotificationType type, long contextId) {
        if (devices.isEmpty()) {
            log.info("[NotificationHandler] 알림을 받을 활성화된 디바이스가 없습니다. type={}, contextId={}", type, contextId);
            return;
        }

        List<Long> memberIds = devices.stream()
                .map(d -> d.getMember().getId())
                .distinct()
                .toList();
        List<String> fcmTokens = devices.stream()
                .map(MemberDevice::getDeviceToken)
                .toList();

        notificationManager.saveNotifications(memberIds, type);
        notificationSender.sendNotification(fcmTokens, type);

        log.info("[NotificationHandler] 알림 전송 요청 완료. type={}, contextId={}, 대상 인원={}", type, contextId, memberIds.size());
    }

}