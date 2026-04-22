package org.smu.randsome.randsomeback.domain.attendance.implement;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.attendance.entity.Attendance;
import org.smu.randsome.randsomeback.domain.attendance.repository.AttendanceJpaRepository;
import org.smu.randsome.randsomeback.global.constant.ServicePeriod;
import org.smu.randsome.randsomeback.global.entity.EntityStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Component
public class AttendanceReader {

    private final AttendanceJpaRepository attendanceJpaRepository;

    public List<Attendance> findAllInServicePeriod(Long memberId) {
        return attendanceJpaRepository.findAllByMemberIdAndAttendanceDateBetweenAndStatusOrderByAttendanceDateAsc(
                memberId,
                ServicePeriod.OPEN_DATE,
                ServicePeriod.CLOSE_DATE,
                EntityStatus.ACTIVE
        );
    }

    public long countInServicePeriod(Long memberId) {
        return attendanceJpaRepository.countByMemberIdAndAttendanceDateBetweenAndStatus(
                memberId,
                ServicePeriod.OPEN_DATE,
                ServicePeriod.CLOSE_DATE,
                EntityStatus.ACTIVE
        );
    }

}