package org.smu.randsome.randsomeback.admin.report.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.smu.randsome.randsomeback.admin.report.dto.response.AdminReportDetailResponse;
import org.smu.randsome.randsomeback.admin.report.dto.response.AdminReportListItem;
import org.smu.randsome.randsomeback.admin.report.enums.AdminReportStatusFilter;
import org.smu.randsome.randsomeback.global.support.response.ApiResponse;

@Tag(name = "관리자 신고 관리", description = "관리자 신고 검토 및 제재 기능")
public abstract class AdminReportControllerDocs {

    @Operation(
            summary = "신고 목록 조회",
            description = """
                    ## 상태별 신고 목록을 조회합니다.
                    - **PENDING**: 접수된 신고
                    - **IN_REVIEW**: 검토 중인 신고
                    - **COMPLETED**: 처리 완료 및 거절된 신고 (RESOLVED + REJECTED)
                    """
    )
    public abstract ApiResponse<List<AdminReportListItem>> getReports(AdminReportStatusFilter statusFilter);

    @Operation(
            summary = "신고 상세 조회",
            description = """
                    ## 특정 신고의 상세 정보를 조회합니다.
                    신고자/피신고자 정보, 신고 사유, 설명, 상태, 피신고자의 누적 활성 신고 횟수를 반환합니다.
                    """
    )
    public abstract ApiResponse<AdminReportDetailResponse> getReport(Long reportId);

    @Operation(
            summary = "신고 처리 (경고)",
            description = """
                    ## 신고를 처리하고 경고만 부여합니다.
                    신고 상태가 RESOLVED로 변경됩니다. 회원 정지는 발생하지 않습니다.
                    """
    )
    public abstract ApiResponse<?> resolveReport(Long reportId);

    @Operation(
            summary = "신고 거절",
            description = """
                    ## 신고를 거절합니다.
                    신고 상태가 REJECTED로 변경됩니다.
                    """
    )
    public abstract ApiResponse<?> rejectReport(Long reportId);

}
