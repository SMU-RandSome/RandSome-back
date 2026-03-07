package org.smu.randsome.randsomeback.domain.payment.entity;

import static java.util.Objects.*;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.smu.randsome.randsomeback.domain.candidate.entity.CandidateRegistration;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.payment.enums.PaymentStatus;
import org.smu.randsome.randsomeback.domain.payment.enums.PaymentType;
import org.smu.randsome.randsomeback.global.entity.BaseEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class CandidatePayment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
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

    public static CandidatePayment register(
            Member member,
            CandidateRegistration candidateRegistration
    ) {
        PaymentType paymentType = PaymentType.CANDIDATE_REGISTRATION;

        CandidatePayment payment = new CandidatePayment();

        payment.member = requireNonNull(member);
        payment.candidateRegistration = requireNonNull(candidateRegistration);
        payment.paymentType = paymentType;
        payment.amount = paymentType.calculateFee(1);
        payment.paymentStatus = PaymentStatus.PENDING;

        return payment;
    }

    public void approve() {
        this.paymentStatus = PaymentStatus.APPROVED;
    }

    public void reject(String rejectedReason) {
        this.rejectedReason = requireNonNull(rejectedReason);
        this.paymentStatus = PaymentStatus.REJECTED;
    }

}