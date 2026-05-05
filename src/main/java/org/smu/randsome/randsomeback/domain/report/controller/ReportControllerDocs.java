package org.smu.randsome.randsomeback.domain.report.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.smu.randsome.randsomeback.domain.report.dto.request.ReportCreateRequest;
import org.smu.randsome.randsomeback.global.annotation.LoginMember;
import org.smu.randsome.randsomeback.global.support.response.ApiResponse;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "신고", description = "회원 신고 기능")
public abstract class ReportControllerDocs {

    @Operation(
            summary = "신고 생성 - JWT [O]",
            description = """
                    ## 매칭 결과에서 후보자를 신고합니다.
                    매칭 결과 ID, 신고 사유, 신고 설명을 입력해야 합니다.
                    신고는 매칭 신청자만 가능하며, 동일한 매칭 결과에 대해 중복 신고는 불가능합니다.
                    신고 대상 회원의 활성 신고가 3건 이상인 경우 신고가 거부됩니다.
                    """
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "신고가 성공적으로 생성되었습니다.")
    public abstract ApiResponse<Long> createReport(
            @RequestBody @Valid ReportCreateRequest request,
            @LoginMember Long memberId
    );

}