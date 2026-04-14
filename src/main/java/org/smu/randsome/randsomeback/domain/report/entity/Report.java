package org.smu.randsome.randsomeback.domain.report.entity;

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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.report.enums.ReportReason;
import org.smu.randsome.randsomeback.domain.report.enums.ReportStatus;
import org.smu.randsome.randsomeback.domain.report.enums.ReportTargetType;
import org.smu.randsome.randsomeback.global.entity.BaseEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(uniqueConstraints = @UniqueConstraint(
        name = "uk_report_reporter_target",
        columnNames = {"reporter_id", "target_type", "target_id"}
))
public class Report extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn( nullable = false)
    private Member reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Member reportedMember;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportTargetType targetType;

    @Column(nullable = false)
    private Long targetId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportReason reason;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReportStatus reportStatus;

    public static Report create(
            Member reporter,
            Member reportedMember,
            ReportTargetType targetType,
            Long targetId,
            ReportReason reason,
            String description
    ) {
        Report report = new Report();

        report.reporter = requireNonNull(reporter);
        report.reportedMember = requireNonNull(reportedMember);
        report.targetType = requireNonNull(targetType);
        report.targetId = requireNonNull(targetId);
        report.reason = requireNonNull(reason);
        report.description = description;
        report.reportStatus = ReportStatus.PENDING;

        return report;
    }

    public void updateStatusToInReview() {
        this.reportStatus = ReportStatus.IN_REVIEW;
    }

    public void markAsResolved() {
        this.reportStatus = ReportStatus.RESOLVED;
    }

    public void markAsRejected() {
        this.reportStatus = ReportStatus.REJECTED;
    }

    public boolean isPending() {
        return reportStatus == ReportStatus.PENDING;
    }
}
