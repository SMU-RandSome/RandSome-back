package org.smu.randsome.randsomeback.domain.matching.event;

/**
 * 매칭 신청이 완료되었을 때 발생하는 이벤트입니다.
 * */
public record MatchingAppliedEvent(Long matchingApplicationId) {

}