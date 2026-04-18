package org.smu.randsome.randsomeback.domain.attendance.event;

import java.time.LocalDate;

/**
 * 출석 체크 DB 커밋 완료 후 발행되는 도메인 이벤트.
 */
public record AttendanceCheckedEvent(Long memberId, LocalDate date) {

}