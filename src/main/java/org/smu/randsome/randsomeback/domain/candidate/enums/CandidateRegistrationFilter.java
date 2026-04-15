package org.smu.randsome.randsomeback.domain.candidate.enums;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CandidateRegistrationFilter {

    PENDING  (List.of(RegistrationStatus.PENDING)),
    COMPLETED(List.of(
            RegistrationStatus.APPROVED,
            RegistrationStatus.REJECTED,
            RegistrationStatus.WITHDRAWN,
            RegistrationStatus.CANCELED
    ));

    private final List<RegistrationStatus> statuses;

}