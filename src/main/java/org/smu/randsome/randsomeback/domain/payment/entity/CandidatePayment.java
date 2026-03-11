package org.smu.randsome.randsomeback.domain.payment.entity;

import static java.util.Objects.requireNonNull;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.smu.randsome.randsomeback.domain.candidate.entity.CandidateRegistration;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.payment.enums.PaymentStatus;
import org.smu.randsome.randsomeback.domain.payment.enums.PaymentType;
import org.smu.randsome.randsomeback.global.entity.BaseEntity;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class CandidatePayment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Member member;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, unique = true)
    private CandidateRegistration candidateRegistration;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentType paymentType;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;

    private String rejectedReason;

    private LocalDateTime approvedAt;

    public static CandidatePayment register(CandidateRegistration candidateRegistration) {
        PaymentType paymentType = PaymentType.CANDIDATE_REGISTRATION;

        CandidatePayment payment = new CandidatePayment();

        payment.member = requireNonNull(candidateRegistration.getMember());
        payment.candidateRegistration = requireNonNull(candidateRegistration);
        payment.paymentType = paymentType;
        payment.amount = paymentType.calculateFee(1);
        payment.paymentStatus = PaymentStatus.PENDING;
        payment.approvedAt = null;

        return payment;
    }

    public void approve(LocalDateTime approvedAt) {
        this.paymentStatus = PaymentStatus.APPROVED;
        this.approvedAt = requireNonNull(approvedAt);
        this.rejectedReason = null; // 이전 거절 사유 초기화
    }

    public void reject(String rejectedReason) {
        if (this.paymentStatus == PaymentStatus.APPROVED) {
            throw new CoreException(ErrorType.NOT_ALLOW_ALREADY_APPROVED_PAYMENT);
        }

        this.rejectedReason = requireNonNull(rejectedReason);
        this.paymentStatus = PaymentStatus.REJECTED;
    }

}