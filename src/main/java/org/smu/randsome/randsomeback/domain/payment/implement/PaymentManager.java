package org.smu.randsome.randsomeback.domain.payment.implement;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.payment.entity.Payment;
import org.smu.randsome.randsomeback.domain.payment.enums.PaymentStatus;
import org.smu.randsome.randsomeback.domain.payment.enums.PaymentType;
import org.smu.randsome.randsomeback.domain.payment.repository.PaymentJpaRepository;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 결제 엔티티 생성/저장을 담당하는 컴포넌트.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class PaymentManager {

    private final PaymentJpaRepository paymentJpaRepository;
    private final PaymentReader paymentReader;
    private final List<PaymentHandler> handlers;

    /**
     * 결제 엔티티를 생성하고 저장한다.
     *
     * @param member      결제 요청 회원
     * @param paymentType 결제 유형
     * @param referenceId 도메인 엔티티 식별자
     * @param amount      결제 금액
     */
    public void register(Member member, PaymentType paymentType, Long referenceId, int amount) {
        paymentJpaRepository.save(Payment.register(
                member,
                paymentType,
                referenceId,
                amount
        ));

        log.info("[PaymentManager] 결제 생성 완료 - memberId={}, paymentType={}, referenceId={}",
                member.getId(), paymentType, referenceId);
    }

    /**
     * 결제를 승인하고 결제 유형에 맞는 후속 도메인 로직을 수행한다.
     *
     * @param paymentId 승인할 결제 ID
     */
    @Transactional
    public void confirm(Long paymentId) {
        LocalDateTime now = LocalDateTime.now();

        Payment payment = paymentReader.find(paymentId);
        payment.confirm(now);

        PaymentHandler paymentHandler = resolveHandler(payment.getPaymentType());
        paymentHandler.approve(payment.getReferenceId(), now);

        log.info("[PaymentManager] 결제 승인 처리 완료 - paymentId={}, paymentType={}, referenceId={}",
                paymentId,
                payment.getPaymentType(),
                payment.getReferenceId()
        );
    }

    /**
     * 결제를 거절하고 결제 유형에 맞는 후속 도메인 로직을 수행한다.
     *
     * @param paymentId 거절할 결제 ID
     * @param reason    거절 사유
     */
    @Transactional
    public void reject(Long paymentId, String reason) {
        LocalDateTime now = LocalDateTime.now();

        Payment payment = paymentReader.find(paymentId);
        PaymentStatus currentStatus = payment.getPaymentStatus();
        payment.reject(now);

        PaymentHandler paymentHandler = resolveHandler(payment.getPaymentType());

        paymentHandler.reject(payment.getReferenceId(), reason, now);

        log.info("[PaymentManager] 결제 거절 처리 완료 - paymentId={}, paymentType={}, referenceId={}, fromStatus={}, toStatus={}",
                paymentId,
                payment.getPaymentType(),
                payment.getReferenceId(),
                currentStatus,
                payment.getPaymentStatus());
    }

    /**
     * 결제 유형을 처리할 핸들러를 탐색한다.
     *
     * @param type 결제 유형
     * @return 결제 유형 처리 핸들러
     */
    private PaymentHandler resolveHandler(PaymentType type) {
        return handlers.stream()
                .filter(h -> h.supports().contains(type))
                .findFirst()
                .orElseThrow(() -> {
                    log.error("[PaymentManager] 결제 핸들러를 찾을 수 없습니다 - paymentType={}", type);
                    return new CoreException(ErrorType.DEFAULT_ERROR);
                });
    }

}