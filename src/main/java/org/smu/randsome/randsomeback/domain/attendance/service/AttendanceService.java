package org.smu.randsome.randsomeback.domain.attendance.service;

import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.attendance.implement.AttendanceManager;
import org.smu.randsome.randsomeback.domain.ticket.implement.TicketHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AttendanceService {

    private final AttendanceManager attendanceManager;
    private final TicketHandler ticketHandler;

    /**
     * 출석 체크를 처리하는 서비스 메서드입니다. <br>
     * 출석 체크가 성공적으로 처리되면, 해당 회원에게 출석 체크로 인한 티켓이 지급됩니다.
     * @param memberId 출석 체크를 하는 회원의 ID
     * */
    @Transactional
    public void attend(Long memberId) {
        attendanceManager.create(memberId);
        ticketHandler.issueForAttendance(memberId);
    }

}