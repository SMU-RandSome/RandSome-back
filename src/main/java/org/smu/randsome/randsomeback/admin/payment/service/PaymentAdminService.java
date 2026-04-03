package org.smu.randsome.randsomeback.admin.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smu.randsome.randsomeback.domain.payment.dto.PaymentWithDetails;
import org.smu.randsome.randsomeback.domain.payment.dto.command.PaymentSearchCondition;
import org.smu.randsome.randsomeback.domain.payment.implement.PaymentManager;
import org.smu.randsome.randsomeback.domain.payment.implement.PaymentReader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * 관리자 결제 승인/거절 유스케이스를 처리하는 서비스.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class PaymentAdminService {

    private final PaymentManager paymentManager;
    private final PaymentReader paymentReader;

    /**
     * 결제를 승인 처리한다.
     *
     * @param paymentId 승인할 결제 ID
     */
    public void approve(Long paymentId) {
        paymentManager.approve(paymentId);
    }

    /**
     * 결제를 거절 처리한다.
     *
     * @param paymentId 거절할 결제 ID
     * @param rejectedReason 거절 사유
     */
    public void reject(Long paymentId, String rejectedReason) {
        paymentManager.reject(paymentId, rejectedReason);
    }

    /**
     * 결제 목록을 검색 조건에 따라 조회한다.
     * @param paymentSearchCondition 검색 조건 (결제 상태, 검색어 등)
     * @param pageable 페이지 정보
     * @return 검색된 결제 목록과 거절 사유를 포함한 페이지
     * */
    public Page<PaymentWithDetails> findPayments(PaymentSearchCondition paymentSearchCondition, Pageable pageable) {
        return paymentReader.findAllPayments(paymentSearchCondition, pageable);
    }

}