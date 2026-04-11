package org.smu.randsome.randsomeback.domain.attendance.implement;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smu.randsome.randsomeback.domain.attendance.entity.Attendance;
import org.smu.randsome.randsomeback.domain.attendance.repository.AttendanceJpaRepository;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.implement.MemberReader;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class AttendanceManager {

    private final MemberReader memberReader;
    private final AttendanceJpaRepository attendanceJpaRepository;

    public void create(Long memberId) {
        LocalDate today = LocalDate.now();

        Member member = memberReader.find(memberId);
        // exists check 없이 바로 INSERT — DB unique constraint(UK_ATTENDANCE_MEMBER_DATE)가 유일성을 보장하므로 TOCTOU 없이 안전하게 중복 방지
        try {
            // @Transactional이 Service에 있으므로 save()는 커밋 시점에 flush됨 → try-catch 밖에서 예외 발생.
            // saveAndFlush()로 즉시 flush하여 DataIntegrityViolationException을 여기서 포착.
            attendanceJpaRepository.saveAndFlush(Attendance.create(member, today));
        } catch (DataIntegrityViolationException e) {
            throw new CoreException(ErrorType.DUPLICATE_ATTENDANCE);
        }
        log.info("[AttendanceManager] 출석 기록 생성 - memberId={}, date={}", memberId, today);
    }

}