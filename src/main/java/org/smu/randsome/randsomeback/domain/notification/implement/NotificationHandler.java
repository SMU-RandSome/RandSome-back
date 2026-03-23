package org.smu.randsome.randsomeback.domain.notification.implement;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smu.randsome.randsomeback.domain.announcement.event.AnnouncementRegisteredEvent;
import org.smu.randsome.randsomeback.domain.candidate.event.CandidateAppliedEvent;
import org.smu.randsome.randsomeback.domain.matching.event.MatchingAppliedEvent;
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
public class NotificationHandler {

    private final MemberDeviceReader memberDeviceReader;
    private final NotificationManager notificationManager;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void announcementNotify(AnnouncementRegisteredEvent event) {
        try {
            sendNotificationToDevices(memberDeviceReader.findAllActive(), NotificationType.ANNOUNCEMENT_REGISTERED, event.announcementId());
        } catch (Exception e) {
            log.error("[NotificationHandler] 공지사항 알림 전송 중 오류 발생. announcementId={}", event.announcementId(), e);
            // TODO: Slack 알림 전송 - DB 조회 또는 FCM 전송 자체가 실패한 경우이므로 즉시 알림 필요
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void matchingApplicationNotify(MatchingAppliedEvent event) {
        try {
            sendNotificationToDevices(memberDeviceReader.findAllByAdminRole(), NotificationType.MATCHING_APPLIED_TO_ADMIN, event.matchingApplicationId());
        } catch (Exception e) {
            log.error("[NotificationHandler] 매칭 신청 알림 전송 중 오류 발생. matchingApplicationId={}", event.matchingApplicationId(), e);
            // TODO: Slack 알림 전송 - DB 조회 또는 FCM 전송 자체가 실패한 경우이므로 즉시 알림 필요
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void candidateRegistrationNotify(CandidateAppliedEvent event) {
        try {
            sendNotificationToDevices(memberDeviceReader.findAllByAdminRole(), NotificationType.CANDIDATE_APPLIED_TO_ADMIN, event.candidateRegistrationId());
        } catch (Exception e) {
            log.error("[NotificationHandler] 후보자 신청 알림 전송 중 오류 발생. candidateRegistrationId={}", event.candidateRegistrationId(), e);
            // TODO: Slack 알림 전송 - DB 조회 또는 FCM 전송 자체가 실패한 경우이므로 즉시 알림 필요
        }
    }

    private void sendNotificationToDevices(List<MemberDevice> devices, NotificationType type, long contextId) {
        if (devices.isEmpty()) {
            log.info("[NotificationHandler] 알림을 받을 활성화된 디바이스가 없습니다. type={}, contextId={}", type, contextId);
            return;
        }

        List<Long> memberIds = devices.stream().map(d -> d.getMember().getId()).toList();
        List<String> fcmTokens = devices.stream().map(MemberDevice::getDeviceToken).toList();

        notificationManager.sendToAll(memberIds, fcmTokens, type);

        log.info("[NotificationHandler] 알림 전송 요청 완료. type={}, contextId={}, 대상 인원={}", type, contextId, memberIds.size());
    }

}