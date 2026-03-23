package org.smu.randsome.randsomeback.domain.candidate.event;

/**
 * 후보자 신청이 완료되었을 때 발생하는 이벤트입니다.
 * */
public record CandidateAppliedEvent(Long candidateRegistrationId) {

}