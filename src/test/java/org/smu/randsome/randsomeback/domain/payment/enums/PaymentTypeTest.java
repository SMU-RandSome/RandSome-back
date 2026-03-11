package org.smu.randsome.randsomeback.domain.payment.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;

class PaymentTypeTest extends UnitTestSupport {

    @Test
    void 후보자_등록비는_1인_기준_2000원이다() {
        BigDecimal fee = PaymentType.CANDIDATE_REGISTRATION.calculateFee(1);

        assertThat(fee).isEqualByComparingTo(BigDecimal.valueOf(2000));
    }

    @Test
    void 랜덤_매칭비는_인원수에_비례해서_계산된다() {
        assertThat(PaymentType.RANDOM_MATCHING.calculateFee(1)).isEqualByComparingTo(BigDecimal.valueOf(1000));
        assertThat(PaymentType.RANDOM_MATCHING.calculateFee(5)).isEqualByComparingTo(BigDecimal.valueOf(5000));
    }

    @Test
    void 이상형_매칭비는_인원수에_비례해서_계산된다() {
        assertThat(PaymentType.IDEAL_TYPE_MATCHING.calculateFee(1)).isEqualByComparingTo(BigDecimal.valueOf(1500));
        assertThat(PaymentType.IDEAL_TYPE_MATCHING.calculateFee(5)).isEqualByComparingTo(BigDecimal.valueOf(7500));
    }

    @Test
    void 후보자_등록은_1명_초과시_예외가_발생한다() {
        assertThatThrownBy(() -> PaymentType.CANDIDATE_REGISTRATION.calculateFee(2))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.INVALID_PERSON_COUNT.getMessage());
    }

    @Test
    void 인원수가_최솟값_미만이면_예외가_발생한다() {
        assertThatThrownBy(() -> PaymentType.RANDOM_MATCHING.calculateFee(0))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.INVALID_PERSON_COUNT.getMessage());
    }

    @Test
    void 인원수가_최댓값_초과이면_예외가_발생한다() {
        assertThatThrownBy(() -> PaymentType.RANDOM_MATCHING.calculateFee(6))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.INVALID_PERSON_COUNT.getMessage());
    }

}