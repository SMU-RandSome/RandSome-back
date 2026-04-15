package org.smu.randsome.randsomeback.admin.candidate.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.smu.randsome.randsomeback.admin.candidate.dto.request.CandidateRejectRequest;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.smu.randsome.randsomeback.global.support.response.ApiResponse;
import org.smu.randsome.randsomeback.global.swagger.ApiExceptions;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "관리자 후보자 관리 API")
public abstract class CandidateAdminControllerDocs {

    @Operation(summary = "후보자 등록 승인", description = """
            관리자가 후보자 등록을 승인하는 API입니다.
            - `candidateRegistrationId`는 승인할 후보자 등록 신청의 고유 ID입니다.
            - 성공적으로 승인되면, 해당 후보자는 등록된 상태로 변경됩니다.
            """)
    @ApiExceptions(values = {
            ErrorType.NOT_FOUND_CANDIDATE_REGISTRATION,
            ErrorType.DEFAULT_ERROR
    })
    public abstract ApiResponse<?> approve(
            @Parameter(description = "승인할 후보자 등록 신청의 고유 ID", example = "1")
            Long candidateRegistrationId
    );

    @Operation(summary = "후보자 등록 거절", description = """
            관리자가 후보자 등록을 거절하는 API입니다.
            - `candidateRegistrationId`는 거절할 후보자 등록 신청의 고유 ID입니다.
            - 요청 본문에는 거절 사유를 포함해야 합니다.
            - 성공적으로 거절되면, 해당 후보자는 거절된 상태로 변경됩니다.
            """
    )
    @ApiExceptions(values = {
            ErrorType.NOT_FOUND_CANDIDATE_REGISTRATION,
            ErrorType.DEFAULT_ERROR
    })
    public abstract ApiResponse<?> reject(
            @Parameter(description = "거절할 후보자 등록 신청의 고유 ID", example = "1")
            Long candidateRegistrationId,
            @RequestBody CandidateRejectRequest request
    );

}