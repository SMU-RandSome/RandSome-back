package org.smu.randsome.randsomeback.admin.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.payment.dto.PaymentWithReason;
import org.smu.randsome.randsomeback.domain.payment.entity.Payment;
import org.smu.randsome.randsomeback.domain.payment.enums.PaymentStatus;
import org.smu.randsome.randsomeback.domain.payment.implement.PaymentManager;
import org.smu.randsome.randsomeback.domain.payment.implement.PaymentReader;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

class PaymentAdminServiceUnitTest extends UnitTestSupport {

    @InjectMocks
    PaymentAdminService paymentAdminService;

    @Mock
    PaymentManager paymentManager;

    @Mock
    PaymentReader paymentReader;

    // ===== findPayments() =====

    @Test
    void 결제_내역_조회를_PaymentReader에_위임하고_결과를_반환한다() {
        // given
        var statuses = List.of(PaymentStatus.PENDING);
        var pageable = PageRequest.of(0, 10);
        Page<PaymentWithReason> expected = new PageImpl<>(List.of(
                new PaymentWithReason(mock(Payment.class), null)
        ));
        given(paymentReader.findPayments(statuses, pageable)).willReturn(expected);

        // when
        var result = paymentAdminService.findPayments(statuses, pageable);

        // then
        assertThat(result).isEqualTo(expected);
        verify(paymentReader).findPayments(statuses, pageable);
    }

    @Test
    void 결제_내역이_없으면_빈_페이지를_반환한다() {
        // given
        var statuses = List.of(PaymentStatus.COMPLETED, PaymentStatus.REJECTED);
        var pageable = PageRequest.of(0, 10);
        given(paymentReader.findPayments(statuses, pageable)).willReturn(Page.empty());

        // when
        var result = paymentAdminService.findPayments(statuses, pageable);

        // then
        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    void PaymentReader_에서_예외가_발생하면_그대로_전파된다() {
        // given
        var statuses = List.of(PaymentStatus.PENDING);
        var pageable = PageRequest.of(0, 10);
        willThrow(new CoreException(ErrorType.DEFAULT_ERROR))
                .given(paymentReader).findPayments(statuses, pageable);

        // when & then
        assertThatThrownBy(() -> paymentAdminService.findPayments(statuses, pageable))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.DEFAULT_ERROR.getMessage());
    }

}