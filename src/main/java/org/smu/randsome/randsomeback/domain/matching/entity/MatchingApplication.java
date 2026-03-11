package org.smu.randsome.randsomeback.domain.matching.entity;

import static java.util.Objects.requireNonNull;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.smu.randsome.randsomeback.domain.matching.enums.ApplicationStatus;
import org.smu.randsome.randsomeback.domain.matching.enums.MatchingType;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.global.entity.BaseEntity;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class MatchingApplication extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchingType matchingType;

    @Column(nullable = false)
    private Integer applicationCount;

    @Column(nullable = false)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus applicationStatus;

    private String rejectedReason;

    private LocalDateTime approvedAt;

    private LocalDateTime rejectedAt;

    private LocalDateTime withdrawnAt;

    public static MatchingApplication apply(
            Member member,
            MatchingType matchingType,
            Integer applicationCount
    ) {
        validateApplicationCount(requireNonNull(applicationCount));

        MatchingApplication matchingApplication = new MatchingApplication();

        matchingApplication.member = requireNonNull(member);
        matchingApplication.matchingType = requireNonNull(matchingType);
        matchingApplication.applicationCount = applicationCount;
        matchingApplication.totalPrice = matchingType.calculateFee(applicationCount);
        matchingApplication.rejectedReason = null;
        matchingApplication.applicationStatus = ApplicationStatus.PENDING;
        matchingApplication.approvedAt = null;
        matchingApplication.rejectedAt = null;
        matchingApplication.withdrawnAt = null;

        return matchingApplication;
    }

    public void approve(LocalDateTime approvedAt) {
        if (applicationStatus.equals(ApplicationStatus.APPROVED)) {
            return;
        }
        this.applicationStatus = ApplicationStatus.APPROVED;
        this.approvedAt = requireNonNull(approvedAt);
        this.rejectedReason = null;
        this.rejectedAt = null;
    }

    public void reject(LocalDateTime rejectedAt, String rejectedReason) {
        if (applicationStatus.equals(ApplicationStatus.APPROVED)) {
            throw new CoreException(ErrorType.NOT_ALLOW_ALREADY_APPROVED_MATCHING);
        }

        this.applicationStatus = ApplicationStatus.REJECTED;
        this.rejectedAt = requireNonNull(rejectedAt);
        this.rejectedReason = requireNonNull(rejectedReason);
        this.approvedAt = null;
    }

    public void withdraw(LocalDateTime withdrawnAt) {
        if (applicationStatus.equals(ApplicationStatus.APPROVED)) {
            throw new CoreException(ErrorType.NOT_ALLOW_WITHDRAW_APPROVED);
        }
        if (applicationStatus.equals(ApplicationStatus.REJECTED)) {
            throw new CoreException(ErrorType.NOT_ALLOW_WITHDRAW_REJECTED);
        }
        if (applicationStatus.equals(ApplicationStatus.WITHDRAWN)) {
            return;
        }

        this.applicationStatus = ApplicationStatus.WITHDRAWN;
        this.withdrawnAt = requireNonNull(withdrawnAt);
        // NOTE: 철회 시엔 승인 시각을 지우지 않음.
    }

    private static void validateApplicationCount(int applicationCount) {
        if (applicationCount < 1 || applicationCount > 5) {
            throw new CoreException(ErrorType.INVALID_PERSON_COUNT);
        }
    }

}