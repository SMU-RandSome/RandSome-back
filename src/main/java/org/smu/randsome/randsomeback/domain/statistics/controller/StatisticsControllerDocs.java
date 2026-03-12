package org.smu.randsome.randsomeback.domain.statistics.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.smu.randsome.randsomeback.domain.statistics.controller.dto.response.DashboardResponse;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.smu.randsome.randsomeback.global.support.response.ApiResponse;
import org.smu.randsome.randsomeback.global.swagger.ApiExceptions;
import org.springframework.http.ResponseEntity;

@Tag(name = "Statistics Docs", description = "통계 관련 API 문서")
public abstract class StatisticsControllerDocs {

    @Operation(
            summary = "대시보드 통계 조회 API",
            description = """
                    ### 대시보드 통계 조회 API입니다.
                    - 승인된 매칭 후보 수, 오늘의 매칭 신청 수, 전체 매칭 신청 수를 반환합니다.
                    """
    )
    @ApiExceptions(values = {
            ErrorType.DEFAULT_ERROR
    })
    public abstract ResponseEntity<ApiResponse<DashboardResponse>> getDashboard();

}
