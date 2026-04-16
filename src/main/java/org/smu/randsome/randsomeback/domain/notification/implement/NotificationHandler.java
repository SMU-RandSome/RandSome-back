package org.smu.randsome.randsomeback.domain.notification.implement;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smu.randsome.randsomeback.domain.announcement.event.AnnouncementRegisteredEvent;
import org.smu.randsome.randsomeback.domain.candidate.entity.CandidateRegistration;
import org.smu.randsome.randsomeback.domain.candidate.event.CandidateAppliedEvent;
import org.smu.randsome.randsomeback.domain.candidate.event.CandidateRegistrationNotificationEvent;
import org.smu.randsome.randsomeback.domain.candidate.implement.CandidateReader;
import org.smu.randsome.randsomeback.domain.matching.event.MatchingApplicationCompletedEvent;
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
    private final CandidateReader candidateReader;
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
    public void matchingApplicationNotify(MatchingApplicationCompletedEvent event) {
        try {
            List<MemberDevice> memberDevices = memberDeviceReader.findAllByAdminRole();
            sendNotificationToDevices(
                    memberDevices,
                    NotificationType.MATCHING_APPLIED_TO_ADMIN,
                    event.applicationId()
            );
        } catch (Exception e) {
            log.error("[NotificationHandler] 매칭 완료 알림 전송 중 오류 발생. applicationId={}", event.applicationId(), e);
            errorNotificationSender.sendErrorNotification(
                    "[NotificationHandler] 매칭 완료 알림 전송 중 오류 발생. applicationId=" + event.applicationId()
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

    /**
     * 후보자 승인/거절 알림 전송.
     * <p>
     * NOTE: 트랜잭션 점유를 최소화 하기 위해 트랜잭션 없음 — candidateReader.findWithMember()가 패치조인으로 Member를 즉시 로드하므로
     * Lazy 로딩이 발생하지 않습니다. findWithMember()의 구현이 변경되면 @Transactional 추가 필요.
     *
     */
    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void notifyCandidateRegistrationResult(CandidateRegistrationNotificationEvent event) {
        try {
            CandidateRegistration candidateRegistration = candidateReader.findWithMember(event.candidateRegistrationId());
            List<MemberDevice> memberDevices = memberDeviceReader.findAllByMemberId(candidateRegistration.getMember().getId());
            sendNotificationToDevices(
                    memberDevices,
                    event.notificationType(),
                    event.candidateRegistrationId()
            );
        } catch (Exception e) {
            log.error("[NotificationHandler] 후보자 승인 또는 거절 알림 전송 중 오류 발생. candidateRegistrationId={}",
                    event.candidateRegistrationId(),
                    e);
            errorNotificationSender.sendErrorNotification(
                    "[NotificationHandler] 후보자 승인 또는 거절 알림 전송 중 오류 발생. candidateRegistrationId=" + event.candidateRegistrationId()
                            + ", error: " + e.getMessage(), e);
        }

    }

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