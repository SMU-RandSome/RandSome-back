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

    public static CandidateRegistration apply(Member member) {
        CandidateRegistration candidateRegistration = new CandidateRegistration();

        candidateRegistration.member = requireNonNull(member);
        candidateRegistration.registrationStatus = RegistrationStatus.PENDING;
        candidateRegistration.approvedAt = null;
        candidateRegistration.rejectedReason = null;
        candidateRegistration.rejectedAt = null;

        return candidateRegistration;
    }

    public void approve(LocalDateTime approvedAt) {
        if (registrationStatus.equals(RegistrationStatus.APPROVED)) return;

        this.registrationStatus = RegistrationStatus.APPROVED;
        this.approvedAt = requireNonNull(approvedAt);
        this.rejectedReason = null;
        this.rejectedAt = null;
    }

    public void reject(String rejectedReason, LocalDateTime rejectedAt) {
        if (registrationStatus.equals(RegistrationStatus.APPROVED)) {
            throw new CoreException(ErrorType.NOT_ALLOW_ALREADY_APPROVED_REGISTRATION);
        }

        this.registrationStatus = RegistrationStatus.REJECTED;
        this.rejectedAt = requireNonNull(rejectedAt);
        this.rejectedReason = requireNonNull(rejectedReason);
    }

}