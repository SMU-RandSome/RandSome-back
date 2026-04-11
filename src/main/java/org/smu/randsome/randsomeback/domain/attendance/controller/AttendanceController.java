package org.smu.randsome.randsomeback.domain.attendance.controller;

import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.attendance.service.AttendanceService;
import org.smu.randsome.randsomeback.global.annotation.LoginMember;
import org.smu.randsome.randsomeback.global.support.response.ApiResponse;
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

}