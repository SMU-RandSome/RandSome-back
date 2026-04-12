package org.smu.randsome.randsomeback.domain.attendance.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.attendance.dto.AttendanceResponse;
import org.smu.randsome.randsomeback.domain.attendance.entity.Attendance;
import org.smu.randsome.randsomeback.domain.attendance.service.AttendanceService;
import org.smu.randsome.randsomeback.global.annotation.LoginMember;
import org.smu.randsome.randsomeback.global.support.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class AttendanceController extends AttendanceControllerDocs {

    private final AttendanceService attendanceService;

    @Override
    @PostMapping("/v1/attendance")
    public ApiResponse<?> attend(@LoginMember Long memberId) {
        attendanceService.attend(memberId);

        return ApiResponse.success();
    }

    @Override
    @GetMapping("/v1/attendance")
    public ApiResponse<AttendanceResponse> getMyAttendance(@LoginMember Long memberId) {
        List<Attendance> attendances = attendanceService.getMyAttendance(memberId);

        return ApiResponse.success(AttendanceResponse.from(attendances));
    }

}