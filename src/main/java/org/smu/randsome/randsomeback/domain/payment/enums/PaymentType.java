package org.smu.randsome.randsomeback.domain.payment.enums;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;

@Getter
@RequiredArgsConstructor
public enum PaymentType {

    CANDIDATE_REGISTRATION (BigDecimal.valueOf(2000), 1, 1),
    RANDOM_MATCHING        (BigDecimal.valueOf(1000), 1, 5),
    IDEAL_TYPE_MATCHING    (BigDecimal.valueOf(1500), 1, 5);

    private final BigDecimal feePerPerson;
    private final int minCount;
    private final int maxCount;

    public BigDecimal calculateFee(int personCount) {
        if (personCount < minCount || personCount > maxCount) {
            throw new CoreException(ErrorType.INVALID_PERSON_COUNT);
        }
        return feePerPerson.multiply(BigDecimal.valueOf(personCount));
    }

}