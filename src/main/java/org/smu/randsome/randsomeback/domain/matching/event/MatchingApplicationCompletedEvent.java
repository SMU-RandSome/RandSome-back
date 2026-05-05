package org.smu.randsome.randsomeback.domain.matching.event;

import org.smu.randsome.randsomeback.domain.matching.enums.ApplicationStatus;

/**
 * 매칭이 완료되었을 때 발행되는 이벤트.
 */
public record MatchingApplicationCompletedEvent(
        Long applicationId,
        String nickname,
        int count,
        int matchedCount,
        ApplicationStatus applicationStatus
) {

}
