package org.smu.randsome.randsomeback.admin.statistics.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.smu.randsome.randsomeback.admin.statistics.dto.response.CandidateGenderCountResponse;
import org.smu.randsome.randsomeback.admin.statistics.dto.response.PaymentStatusStatisticsResponse;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.smu.randsome.randsomeback.global.support.response.ApiResponse;
import org.smu.randsome.randsomeback.global.swagger.ApiExceptions;

@Tag(name = "관리자 통계 API", description = "관리자용 통계 조회 API 문서")
public abstract class StatisticsAdminControllerDocs {

    @Operation(
            summary = "후보자 성별 건수 조회",
            description = """
                    #### 관리자 후보자 성별 건수 조회 API입니다.
                    - ROLE_CANDIDATE 권한을 가진 회원만 집계합니다.
                    - 통계 대시보드에서 성별 분포를 확인할 때 사용합니다.
                    """
    )
    @ApiExceptions(values = {
            ErrorType.DEFAULT_ERROR
    })
    public abstract ApiResponse<CandidateGenderCountResponse> getCandidateGenderCount();

    @Operation(
            summary = "결제 상태별 건수 조회",
            description = """
                    #### 관리자 결제 상태별 건수 조회 API입니다.
                    - 대기(`pendingCount`)와 처리 완료(`processedCount`, 승인+거절 합산) 건수를 반환합니다.
                    - 결제 현황 대시보드에서 사용합니다.
                    """
    )
    @ApiExceptions(values = {
            ErrorType.DEFAULT_ERROR
    })
    public abstract ApiResponse<PaymentStatusStatisticsResponse> getPaymentStatusCount();

}