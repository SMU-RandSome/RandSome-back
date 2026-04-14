package org.smu.randsome.randsomeback.domain.matching.dto.command;

import lombok.Builder;
import org.smu.randsome.randsomeback.domain.matching.entity.vo.IdealTypePreference;
import org.smu.randsome.randsomeback.domain.matching.enums.MatchingType;

/**
 * 매칭 신청을 위한 커맨드 DTO다.
 * <br/>Request DTO에서 변환되어 Service 계층으로 전달되는 도메인 언어 입력 계약이다.
 * <br/>HTTP 관심사를 제거하고 순수 비즈니스 데이터만 담는다.
 */
@Builder
public record NewMatching(
        int applicationCount,
        MatchingType matchingType,
        IdealTypePreference idealTypePreference
) {

}