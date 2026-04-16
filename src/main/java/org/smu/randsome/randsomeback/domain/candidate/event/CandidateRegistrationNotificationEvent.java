package org.smu.randsome.randsomeback.domain.candidate.event;

import org.smu.randsome.randsomeback.global.support.notification.NotificationType;

/**
 * 후보자 등록 신청이 승인되었을 때 발행되는 이벤트. <br>
 * 이 이벤트는 후보자 등록 신청이 승인된 후에 관련된 후속 작업을 트리거하기 위해 발행된다. <br>
 * 예를 들어, 승인된 후보자에게 승인 알림을 발송하거나, 매칭 시스템에서 후보자 등록을 활성화하는 등의 작업이 이 이벤트를 통해 수행될 수 있다. <br>
 * 이벤트에는 승인된 후보자 등록의 ID가 포함되어 있어, 이벤트 리스너가 해당 후보자 등록
 */
public record CandidateRegistrationNotificationEvent(Long candidateRegistrationId, NotificationType notificationType) {

}