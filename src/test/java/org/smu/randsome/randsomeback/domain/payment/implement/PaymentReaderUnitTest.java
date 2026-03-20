package org.smu.randsome.randsomeback.domain.payment.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smu.randsome.randsomeback.UnitTestSupport;
import org.smu.randsome.randsomeback.domain.payment.dto.PaymentWithReason;
import org.smu.randsome.randsomeback.domain.payment.entity.Payment;
import org.smu.randsome.randsomeback.domain.payment.enums.PaymentStatus;
import org.smu.randsome.randsomeback.domain.payment.repository.PaymentRepository;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

class PaymentReaderUnitTest extends UnitTestSupport {

    @InjectMocks
    PaymentReader paymentReader;

    @Mock
    PaymentRepository paymentRepository;

    // ===== find() =====

    @Test
    void ID로_결제를_조회한다() {
        // given
        var paymentId = 1L;
        var payment = mock(Payment.class);
        given(paymentRepository.findByIdAndStatus(paymentId, EntityStatus.ACTIVE))
                .willReturn(Optional.of(payment));

        // when
        var result = paymentReader.find(paymentId);

        // then
        assertThat(result).isEqualTo(payment);
    }

    @Test
    void 존재하지_않는_ID로_조회하면_NOT_FOUND_PAYMENT_예외가_발생한다() {
        // given
        var paymentId = 999L;
        given(paymentRepository.findByIdAndStatus(paymentId, EntityStatus.ACTIVE))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> paymentReader.find(paymentId))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.NOT_FOUND_PAYMENT.getMessage());
    }

    // ===== findPayments() =====

    @Test
    void 결제_상태_목록으로_결제_내역을_조회한다() {
        // given
        var statuses = List.of(PaymentStatus.PENDING);
        var pageable = PageRequest.of(0, 10);
        var paymentWithReason = new PaymentWithReason(mock(Payment.class), null);
        Page<PaymentWithReason> expected = new PageImpl<>(List.of(paymentWithReason));
        given(paymentRepository.findPaymentsWithRejectedReason(statuses, pageable))
                .willReturn(expected);

        // when
        Page<PaymentWithReason> result = paymentReader.findPayments(statuses, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        verify(paymentRepository).findPaymentsWithRejectedReason(statuses, pageable);
    }

    @Test
    void 결제_내역이_없으면_빈_페이지를_반환한다() {
        // given
        var statuses = List.of(PaymentStatus.PENDING);
        var pageable = PageRequest.of(0, 10);
        given(paymentRepository.findPaymentsWithRejectedReason(statuses, pageable))
                .willReturn(Page.empty());

        // when
        Page<PaymentWithReason> result = paymentReader.findPayments(statuses, pageable);

        // then
        assertThat(result.isEmpty()).isTrue();
        assertThat(result.getTotalElements()).isZero();
    }

}