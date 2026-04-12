package org.smu.randsome.randsomeback.domain.attendance.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.smu.randsome.randsomeback.domain.attendance.dto.AttendanceResponse;
import org.smu.randsome.randsomeback.global.annotation.LoginMember;
import org.smu.randsome.randsomeback.global.support.response.ApiResponse;

@Tag(name = "출석 API", description = "출석 관련 API")
public abstract class AttendanceControllerDocs {

    @Operation(summary = "출석 체크 - JWT [O]",
            description = """
                    - 출석 체크를 하면 1장의 티켓이 지급됩니다.
                    - 이미 출석 체크를 한 경우, 추가로 출석 체크를 할 수 없습니다.
                    """
    )
    public abstract ApiResponse<?> attend(@LoginMember Long memberId);

    @Operation(summary = "내 출석 현황 조회 - JWT [O]",
            description = """
                    - 서비스 기간(5/6 ~ 5/28) 동안의 내 출석 기록을 조회합니다.
                    - totalDays: 전체 서비스 기간(일수), attendedDays: 실제 출석일 수, attendanceDates: 출석한 날짜 목록
                    """
    )
    public abstract ApiResponse<AttendanceResponse> getMyAttendance(@LoginMember Long memberId);

}