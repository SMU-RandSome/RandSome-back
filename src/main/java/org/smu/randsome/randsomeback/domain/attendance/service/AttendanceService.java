package org.smu.randsome.randsomeback.domain.attendance.service;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.attendance.entity.Attendance;
import org.smu.randsome.randsomeback.domain.attendance.event.AttendanceCheckedEvent;
import org.smu.randsome.randsomeback.domain.attendance.implement.AttendanceManager;
import org.smu.randsome.randsomeback.domain.attendance.implement.AttendanceReader;
import org.smu.randsome.randsomeback.domain.attendance.implement.AttendanceValidator;
import org.smu.randsome.randsomeback.domain.ticket.implement.TicketHandler;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AttendanceService {

    private final AttendanceValidator attendanceValidator;
    private final AttendanceManager attendanceManager;
    private final AttendanceReader attendanceReader;
    private final TicketHandler ticketHandler;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 출석 체크를 처리하는 서비스 메서드입니다. <br>
     * 출석 체크가 성공적으로 처리되면, 해당 회원에게 출석 체크로 인한 티켓이 지급됩니다.
     *
     * @param memberId 출석 체크를 하는 회원의 ID
     */
    @Transactional
    public void attend(Long memberId) {
        LocalDate today = LocalDate.now();

        attendanceValidator.validateNotDuplicate(memberId, today);
        attendanceManager.create(memberId, today);
        ticketHandler.issueForAttendance(memberId);

        eventPublisher.publishEvent(new AttendanceCheckedEvent(memberId, today));
    }

    /**
     * 회원의 출석 체크 내역을 조회하는 서비스 메서드입니다. <br>
     * 조회 시, 서비스 기간 내의 출석 체크 내역만 반환됩니다.
     **/
    public List<Attendance> getMyAttendance(Long memberId) {
        return attendanceReader.findAllInServicePeriod(memberId);
    }

    /**
     * 서비스 기간 내 회원의 출석 일수를 조회한다.
     *
     * @param memberId 회원 식별자
     * @return 출석 일수
     */
    public long getAttendanceDays(Long memberId) {
        return attendanceReader.countInServicePeriod(memberId);
    }

}
