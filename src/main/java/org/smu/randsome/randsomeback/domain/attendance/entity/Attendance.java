package org.smu.randsome.randsomeback.domain.attendance.entity;

import static java.util.Objects.requireNonNull;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.global.entity.BaseEntity;

@Table(
        uniqueConstraints = @UniqueConstraint(name = "UK_ATTENDANCE_MEMBER_DATE", columnNames = {"member_id", "attendance_date"}),
        indexes = {@Index(name = "idx_attendance_member_date_status", columnList = "member_id, attendance_date, status")}
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Attendance extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Member member;

    @Column(nullable = false)
    private LocalDate attendanceDate;

    public static Attendance create(Member member, LocalDate attendanceDate) {
        Attendance attendance = new Attendance();

        attendance.member = requireNonNull(member);
        attendance.attendanceDate = requireNonNull(attendanceDate);

        return attendance;
    }

}