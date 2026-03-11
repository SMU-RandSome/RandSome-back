package org.smu.randsome.randsomeback.domain.payment.entity;

import static java.util.Objects.requireNonNull;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.payment.enums.PaymentStatus;
import org.smu.randsome.randsomeback.domain.payment.enums.PaymentType;
import org.smu.randsome.randsomeback.global.entity.BaseEntity;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;

@Table(
        uniqueConstraints = @UniqueConstraint(
                name = "uk_payment_reference",
                columnNames = {"paymentType", "referenceId"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Payment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentType paymentType;

    @Column(nullable = false)
    private Long referenceId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;

    private LocalDateTime confirmedAt;

    private LocalDateTime rejectedAt;

    public static Payment register(Member member, PaymentType paymentType, Long referenceId, int amount) {
        Payment payment = new Payment();

        payment.member = requireNonNull(member);
        payment.paymentType = requireNonNull(paymentType);
        payment.referenceId = requireNonNull(referenceId);
        payment.amount = paymentType.calculateFee(amount);
        payment.paymentStatus = PaymentStatus.PENDING;
        payment.confirmedAt = null;
        payment.rejectedAt = null;

        return payment;
    }

    public void confirm(LocalDateTime confirmedAt) {
        if (this.paymentStatus == PaymentStatus.COMPLETED) return;

        this.paymentStatus = PaymentStatus.COMPLETED;
        this.confirmedAt = requireNonNull(confirmedAt);
    }

    public void reject(LocalDateTime rejectedAt) {
        if (this.paymentStatus == PaymentStatus.COMPLETED) {
            throw new CoreException(ErrorType.NOT_ALLOW_ALREADY_CONFIRMED_PAYMENT);
        }

        this.paymentStatus = PaymentStatus.REJECTED;
        this.rejectedAt = requireNonNull(rejectedAt);
    }

}