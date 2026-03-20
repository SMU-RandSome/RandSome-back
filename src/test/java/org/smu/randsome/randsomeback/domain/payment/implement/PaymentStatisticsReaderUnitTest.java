package org.smu.randsome.randsomeback.domain.payment.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.payment.dto.response.PaymentStatusCountItem;
import org.smu.randsome.randsomeback.domain.payment.enums.PaymentStatus;
import org.smu.randsome.randsomeback.domain.payment.repository.PaymentJpaRepository;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;

class PaymentStatisticsReaderUnitTest extends UnitTestSupport {

    @InjectMocks
    PaymentStatisticsReader paymentStatisticsReader;

    @Mock
    PaymentJpaRepository paymentJpaRepository;

    @Test
    void 활성_결제의_상태별_건수를_조회한다() {
        // given
        var expected = List.of(
                new PaymentStatusCountItem(PaymentStatus.PENDING, 3),
                new PaymentStatusCountItem(PaymentStatus.COMPLETED, 2)
        );
        given(paymentJpaRepository.countByPaymentStatusAndStatus(EntityStatus.ACTIVE)).willReturn(expected);

        // when
        var result = paymentStatisticsReader.findAllStatusCount();

        // then
        assertThat(result).isEqualTo(expected);
        verify(paymentJpaRepository).countByPaymentStatusAndStatus(EntityStatus.ACTIVE);
    }

    @Test
    void 결제_내역이_없으면_빈_리스트를_반환한다() {
        // given
        given(paymentJpaRepository.countByPaymentStatusAndStatus(EntityStatus.ACTIVE)).willReturn(List.of());

        // when
        var result = paymentStatisticsReader.findAllStatusCount();

        // then
        assertThat(result).isEmpty();
    }

}
