package org.smu.randsome.randsomeback.admin.qr.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.smu.randsome.randsomeback.domain.qr.implement.QrVerificationManager;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketType;
import org.smu.randsome.randsomeback.domain.ticket.implement.TicketHandler;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;

@ExtendWith(MockitoExtension.class)
class QrAdminServiceUnitTest {

    @Mock
    QrVerificationManager qrVerificationManager;

    @Mock
    TicketHandler ticketHandler;

    @InjectMocks
    QrAdminService qrAdminService;

    @Test
    void QR인증_후_IDEAL_티켓_발급에_성공한다() {
        // given
        String qrToken = "valid.token";
        Long memberId = 1L;

        given(qrVerificationManager.verifyAndGetMemberId(qrToken)).willReturn(memberId);

        // when
        qrAdminService.verifyQrAndIssueTicket(qrToken, TicketType.IDEAL);

        // then
        then(qrVerificationManager).should().verifyAndGetMemberId(qrToken);
        then(ticketHandler).should().issueForAdmin(memberId, TicketType.IDEAL, TicketType.IDEAL.getDefaultQuantity(), "소프트웨어 부스 이용으로 인한 티켓 지급");
    }

    @Test
    void QR인증_후_RANDOM_티켓_발급에_성공한다() {
        // given
        String qrToken = "valid.token";
        Long memberId = 1L;

        given(qrVerificationManager.verifyAndGetMemberId(qrToken)).willReturn(memberId);

        // when
        qrAdminService.verifyQrAndIssueTicket(qrToken, TicketType.RANDOM);

        // then
        then(ticketHandler).should().issueForAdmin(memberId, TicketType.RANDOM, TicketType.RANDOM.getDefaultQuantity(), "소프트웨어 부스 이용으로 인한 티켓 지급");
    }

    @Test
    void QR검증_실패_시_티켓을_발급하지_않는다() {
        // given
        String qrToken = "invalid.token";
        given(qrVerificationManager.verifyAndGetMemberId(qrToken))
                .willThrow(new CoreException(ErrorType.INVALID_QR_TOKEN));

        // when & then
        assertThatThrownBy(() -> qrAdminService.verifyQrAndIssueTicket(qrToken, TicketType.IDEAL))
                .isInstanceOf(CoreException.class);

        then(ticketHandler).should(never()).issueForAdmin(anyLong(), any(), anyInt(), any());
    }

}