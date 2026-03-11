package org.smu.randsome.randsomeback.domain.matching.enums;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;

@Getter
@AllArgsConstructor
public enum MatchingType {

    RANDOM (BigDecimal.valueOf(1000), 1, 5),
    IDEAL (BigDecimal.valueOf(1500), 1, 5),
    ;

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