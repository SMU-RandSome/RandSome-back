package org.smu.randsome.randsomeback.domain.attendance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.Builder;
import org.smu.randsome.randsomeback.domain.attendance.entity.Attendance;
import org.smu.randsome.randsomeback.global.constant.ServicePeriod;

@Schema(description = "출석 현황 응답 DTO")
@Builder
public record AttendanceResponse(
        @Schema(description = "총 출석 가능한 일수", example = "30")
        int totalDays,
        @Schema(description = "실제 출석한 일수", example = "15")
        int attendedDays,
        @Schema(description = "출석한 날짜 목록", example = "[\"2026-05-06\", \"2026-05-07\", \"2026-05-08\"]")
        List<LocalDate> attendanceDates
) {
    public static AttendanceResponse from(List<Attendance> attendances) {
        return AttendanceResponse.builder()
                .totalDays(ServicePeriod.TOTAL_DAYS)
                .attendedDays(attendances.size())
                .attendanceDates(attendances.stream()
                        .map(Attendance::getAttendanceDate)
                        .toList())
                .build();
    }
}
