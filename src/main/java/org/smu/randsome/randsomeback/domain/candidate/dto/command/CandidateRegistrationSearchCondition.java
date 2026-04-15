package org.smu.randsome.randsomeback.domain.candidate.dto.command;

import org.smu.randsome.randsomeback.domain.candidate.enums.CandidateRegistrationFilter;

public record CandidateRegistrationSearchCondition(
        CandidateRegistrationFilter filter,
        String keyword
) {

}
