package org.smu.randsome.randsomeback.domain.attendance.implement;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.IntegrationTestSupport;
import org.smu.randsome.randsomeback.domain.attendance.entity.Attendance;
import org.smu.randsome.randsomeback.domain.attendance.repository.AttendanceJpaRepository;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.member.repository.MemberJpaRepository;
import org.smu.randsome.randsomeback.fixture.MemberFixture;
import org.smu.randsome.randsomeback.global.constant.ServicePeriod;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RequiredArgsConstructor
class AttendanceReaderIntegrationTest extends IntegrationTestSupport {

    final AttendanceReader attendanceReader;
    final AttendanceJpaRepository attendanceJpaRepository;
    final MemberJpaRepository memberJpaRepository;

    @Test
    void 서비스_기간_내의_출석_기록을_조회한다() {
        // given
        Member member = memberJpaRepository.save(MemberFixture.create());
        attendanceJpaRepository.save(Attendance.create(member, ServicePeriod.OPEN_DATE));
        attendanceJpaRepository.save(Attendance.create(member, ServicePeriod.CLOSE_DATE));
        attendanceJpaRepository.save(Attendance.create(member, LocalDate.of(2026, 5, 15)));

        // when
        List<Attendance> result = attendanceReader.findAllInServicePeriod(member.getId());

        // then
        assertThat(result).hasSize(3);
    }

    @Test
    void 서비스_기간_밖의_출석_기록은_조회되지_않는다() {
        // given
        Member member = memberJpaRepository.save(MemberFixture.create());
        attendanceJpaRepository.save(Attendance.create(member, ServicePeriod.OPEN_DATE.minusDays(1)));  // 5/5
        attendanceJpaRepository.save(Attendance.create(member, ServicePeriod.CLOSE_DATE.plusDays(1))); // 5/29
        attendanceJpaRepository.save(Attendance.create(member, ServicePeriod.OPEN_DATE));               // 기간 내

        // when
        List<Attendance> result = attendanceReader.findAllInServicePeriod(member.getId());

        // then
        assertThat(result).hasSize(1)
                .extracting(Attendance::getAttendanceDate)
                .containsExactly(ServicePeriod.OPEN_DATE);
    }

}