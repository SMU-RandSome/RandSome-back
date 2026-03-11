package org.smu.randsome.randsomeback.admin.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smu.randsome.randsomeback.domain.payment.implement.PaymentManager;
import org.springframework.stereotype.Service;

/**
 * 관리자 결제 승인/거절 유스케이스를 처리하는 서비스.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class PaymentAdminService {

    private final PaymentManager paymentManager;

    /**
     * 결제를 승인 처리한다.
     *
     * @param paymentId 승인할 결제 ID
     */
    public void confirm(Long paymentId) {
        log.info("[PaymentAdminService] 결제 승인 요청 - paymentId={}", paymentId);

        paymentManager.confirm(paymentId);
    }

    /**
     * 결제를 거절 처리한다.
     *
     * @param paymentId 거절할 결제 ID
     * @param rejectedReason 거절 사유
     */
    public void reject(Long paymentId, String rejectedReason) {
        log.info("[PaymentAdminService] 결제 거절 요청 - paymentId={}, reason={}", paymentId, rejectedReason);

        paymentManager.reject(paymentId, rejectedReason);
    }

}