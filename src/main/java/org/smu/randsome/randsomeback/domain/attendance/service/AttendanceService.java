package org.smu.randsome.randsomeback.domain.attendance.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.attendance.entity.Attendance;
import org.smu.randsome.randsomeback.domain.attendance.implement.AttendanceManager;
import org.smu.randsome.randsomeback.domain.attendance.implement.AttendanceReader;
import org.smu.randsome.randsomeback.domain.ticket.implement.TicketHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AttendanceService {

    private final AttendanceManager attendanceManager;
    private final AttendanceReader attendanceReader;
    private final TicketHandler ticketHandler;

    /**
     * 출석 체크를 처리하는 서비스 메서드입니다. <br> 출석 체크가 성공적으로 처리되면, 해당 회원에게 출석 체크로 인한 티켓이 지급됩니다.
     *
     * @param memberId 출석 체크를 하는 회원의 ID
     *
     */
    @Transactional
    public void attend(Long memberId) {
        attendanceManager.create(memberId);
        ticketHandler.issueForAttendance(memberId);
    }

    /**
     * 회원의 출석 체크 내역을 조회하는 서비스 메서드입니다. <br>
     * 조회 시, 서비스 기간 내의 출석 체크 내역만 반환됩니다.
     **/
    public List<Attendance> getMyAttendance(Long memberId) {
        return attendanceReader.findAllInServicePeriod(memberId);
    }

}