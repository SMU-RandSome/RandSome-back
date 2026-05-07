package org.smu.randsome.randsomeback.admin.qr.service;

import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.qr.implement.QrVerificationManager;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketType;
import org.smu.randsome.randsomeback.domain.ticket.implement.TicketHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class QrAdminService {

    private final QrVerificationManager qrVerificationManager;
    private final TicketHandler ticketHandler;

    /**
     * 관리자용 QR 검증 및 티켓 발급 로직.
     * */
    @Transactional
    public void verifyQrAndIssueTicket(String qrToken, TicketType ticketType) {
        Long memberId = qrVerificationManager.verifyAndGetMemberId(qrToken);

        ticketHandler.issueForAdmin(memberId, ticketType, ticketType.getDefaultQuantity(), "소프트웨어 부스 이용으로 인한 티켓 지급");
    }

}