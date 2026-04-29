package org.smu.randsome.randsomeback.domain.member.dto;

import org.smu.randsome.randsomeback.domain.member.enums.Mbti;

/**
 * 이상형 매칭 스코어링을 위한 경량 프로젝션이다.
 * Member 엔티티 전체 로딩 대신 ID와 MBTI만 조회하여 스코어링에 사용한다.
 */
public record CandidateIdMbti(Long id, Mbti mbti) {

}
