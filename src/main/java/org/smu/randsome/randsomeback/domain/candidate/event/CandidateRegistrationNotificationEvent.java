package org.smu.randsome.randsomeback.domain.candidate.event;

import org.smu.randsome.randsomeback.global.support.notification.NotificationType;

/**
 * 후보자 등록 신청 결과(승인 또는 거절)가 결정되었을 때 발행되는 이벤트입니다. <br>
 * 이 이벤트는 결과에 따른 알림 발송 등 후속 작업을 트리거하기 위해 사용됩니다. <br>
 * 이벤트에는 대상 후보자 등록 ID와 결과 타입이 포함되어 있습니다.
 **/
public record CandidateRegistrationNotificationEvent(Long candidateRegistrationId, NotificationType notificationType) {

}