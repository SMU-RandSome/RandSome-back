package org.smu.randsome.randsomeback.domain.matching.event;

/**
 * 매칭 신청이 승인되었을 때 발행되는 이벤트.
 */
public record MatchingApplicationApprovedEvent(String nickname, int count) {

}
