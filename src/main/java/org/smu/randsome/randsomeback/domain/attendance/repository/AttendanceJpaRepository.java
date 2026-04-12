package org.smu.randsome.randsomeback.domain.attendance.repository;

import java.time.LocalDate;
import java.util.List;
import org.smu.randsome.randsomeback.domain.attendance.entity.Attendance;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceJpaRepository extends JpaRepository<Attendance, Long> {

    List<Attendance> findAllByMemberIdAndStatus(Long memberId, EntityStatus status);
    List<Attendance> findAllByMemberIdAndAttendanceDateBetweenAndStatus(
            Long memberId,
            LocalDate startDate,
            LocalDate endDate,
            EntityStatus status
    );

}