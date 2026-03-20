package org.smu.randsome.randsomeback.admin.statistics.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.admin.statistics.dto.response.PaymentStatusStatisticsResponse;
import org.smu.randsome.randsomeback.domain.payment.dto.response.PaymentStatusCountItem;
import org.smu.randsome.randsomeback.domain.payment.enums.PaymentStatus;

class PaymentStatusStatisticsResponseTest {

    @Test
    void PENDING은_pendingCount에_집계되고_COMPLETED와_REJECTED는_processedCount에_합산된다() {
        // given
        var items = List.of(
                new PaymentStatusCountItem(PaymentStatus.PENDING, 3),
                new PaymentStatusCountItem(PaymentStatus.COMPLETED, 5),
                new PaymentStatusCountItem(PaymentStatus.REJECTED, 2)
        );

        // when
        var result = PaymentStatusStatisticsResponse.from(items);

        // then
        assertThat(result.pendingCount()).isEqualTo(3);
        assertThat(result.processedCount()).isEqualTo(7);
    }

    @Test
    void PENDING만_있으면_processedCount는_0이다() {
        // given
        var items = List.of(new PaymentStatusCountItem(PaymentStatus.PENDING, 4));

        // when
        var result = PaymentStatusStatisticsResponse.from(items);

        // then
        assertThat(result.pendingCount()).isEqualTo(4);
        assertThat(result.processedCount()).isZero();
    }

    @Test
    void 처리된_결제만_있으면_pendingCount는_0이다() {
        // given
        var items = List.of(
                new PaymentStatusCountItem(PaymentStatus.COMPLETED, 3),
                new PaymentStatusCountItem(PaymentStatus.REJECTED, 1)
        );

        // when
        var result = PaymentStatusStatisticsResponse.from(items);

        // then
        assertThat(result.pendingCount()).isZero();
        assertThat(result.processedCount()).isEqualTo(4);
    }

    @Test
    void 결제_내역이_없으면_모두_0이다() {
        // when
        var result = PaymentStatusStatisticsResponse.from(List.of());

        // then
        assertThat(result.pendingCount()).isZero();
        assertThat(result.processedCount()).isZero();
    }

}
