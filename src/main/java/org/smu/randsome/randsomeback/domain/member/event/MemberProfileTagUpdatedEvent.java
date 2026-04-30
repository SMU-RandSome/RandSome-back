package org.smu.randsome.randsomeback.domain.member.event;

/**
 * 프로필 태그 변경(수정/삭제) 후 발행되는 도메인 이벤트.
 */
public record MemberProfileTagUpdatedEvent(Long memberId) {

}