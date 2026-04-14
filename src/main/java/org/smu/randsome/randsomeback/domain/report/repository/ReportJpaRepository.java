package org.smu.randsome.randsomeback.domain.report.repository;

import java.util.List;
import java.util.Optional;
import org.smu.randsome.randsomeback.domain.report.entity.Report;
import org.smu.randsome.randsomeback.domain.report.enums.ReportStatus;
import org.smu.randsome.randsomeback.domain.report.enums.ReportTargetType;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReportJpaRepository extends JpaRepository<Report, Long> {

    boolean existsByReporterIdAndTargetTypeAndTargetId(
            Long reporterId,
            ReportTargetType targetType,
            Long targetId
    );

    @Query("SELECT COUNT(r) FROM Report r " +
            "WHERE r.reportedMember.id = :memberId " +
            "AND r.reportStatus IN :statuses " +
            "AND r.status = :status")
    long countByReportedMemberAndReportStatusIn(
            @Param("memberId") Long memberId,
            @Param("statuses") List<ReportStatus> statuses,
            @Param("status") EntityStatus status
    );

    @Query("SELECT r FROM Report r " +
            "JOIN FETCH r.reporter " +
            "JOIN FETCH r.reportedMember " +
            "WHERE r.id = :id AND r.status = :status")
    Optional<Report> findByIdWithMembers(
            @Param("id") Long id,
            @Param("status") EntityStatus status
    );

    @Query("SELECT r FROM Report r " +
            "JOIN FETCH r.reporter " +
            "JOIN FETCH r.reportedMember " +
            "WHERE r.reportStatus IN :statuses AND r.status = :status " +
            "ORDER BY r.id DESC")
    List<Report> findAllByReportStatuses(
            @Param("statuses") List<ReportStatus> statuses,
            @Param("status") EntityStatus status
    );

}