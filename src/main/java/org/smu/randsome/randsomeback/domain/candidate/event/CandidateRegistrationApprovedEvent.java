package org.smu.randsome.randsomeback.domain.candidate.event;

/**
 * 후보자 등록 신청이 승인되었을 때 발행되는 이벤트.
 */
public record CandidateRegistrationApprovedEvent(String nickname) {

}
