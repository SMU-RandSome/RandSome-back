package org.smu.randsome.randsomeback.domain.candidate.entity;

import static java.util.Objects.requireNonNull;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.smu.randsome.randsomeback.domain.candidate.enums.RegistrationStatus;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.global.entity.BaseEntity;

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

    public static CandidateRegistration apply(Member member) {
        CandidateRegistration candidateRegistration = new CandidateRegistration();

        candidateRegistration.member = requireNonNull(member);
        candidateRegistration.registrationStatus = RegistrationStatus.PENDING;
        candidateRegistration.approvedAt = null;
        candidateRegistration.rejectedReason = null;

        return candidateRegistration;
    }

    public void approve(LocalDateTime now) {
        this.registrationStatus = RegistrationStatus.APPROVED;
        this.approvedAt = now;
    }

    public void reject(String reason, LocalDate today) {
        this.registrationStatus = RegistrationStatus.REJECTED;
        this.rejectedReason = requireNonNull(reason);
    }


}