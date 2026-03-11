package org.smu.randsome.randsomeback.domain.candidate.entity;

import static java.util.Objects.requireNonNull;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.smu.randsome.randsomeback.domain.candidate.enums.RegistrationStatus;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.global.entity.BaseEntity;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class CandidateRegistration extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RegistrationStatus registrationStatus;

    private String rejectedReason;

    private LocalDateTime approvedAt;

    private LocalDateTime rejectedAt;

    private LocalDateTime withdrawnAt;

    public static CandidateRegistration apply(Member member) {
        CandidateRegistration candidateRegistration = new CandidateRegistration();

        candidateRegistration.member = requireNonNull(member);
        candidateRegistration.registrationStatus = RegistrationStatus.PENDING;
        candidateRegistration.approvedAt = null;
        candidateRegistration.rejectedReason = null;
        candidateRegistration.rejectedAt = null;
        candidateRegistration.withdrawnAt = null;

        return candidateRegistration;
    }

    public void approve(LocalDateTime approvedAt) {
        if (registrationStatus.equals(RegistrationStatus.APPROVED)) {
            return;
        }
        checkWithdraw();

        this.registrationStatus = RegistrationStatus.APPROVED;
        this.approvedAt = requireNonNull(approvedAt);
        this.rejectedReason = null;
        this.rejectedAt = null;
    }

    public void reject(String rejectedReason, LocalDateTime rejectedAt) {
        if (registrationStatus.equals(RegistrationStatus.APPROVED)) {
            throw new CoreException(ErrorType.NOT_ALLOW_ALREADY_APPROVED_REGISTRATION);
        }
        checkWithdraw();

        this.registrationStatus = RegistrationStatus.REJECTED;
        this.rejectedAt = requireNonNull(rejectedAt);
        this.rejectedReason = requireNonNull(rejectedReason);
    }

    public void withdraw(LocalDateTime withdrawnAt) {
        // NOTE: 중복 검증이지만 다른 경로에서 호출될 가능성이 있음으로 한번 더 검증
        if (!registrationStatus.equals(RegistrationStatus.APPROVED)) {
            throw new CoreException(ErrorType.NOT_ALLOW_WITHDRAW_NON_APPROVED);
        }

        this.registrationStatus = RegistrationStatus.WITHDRAWN;
        this.withdrawnAt = requireNonNull(withdrawnAt);
        // NOTE: 철회 시엔 승인 시각을 지우지 않음.
    }

    private void checkWithdraw() {
        if (registrationStatus.equals(RegistrationStatus.WITHDRAWN)) {
            throw new CoreException(ErrorType.ALREADY_WITHDRAWN_CANDIDATE);
        }
    }

}