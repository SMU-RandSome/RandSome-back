package org.smu.randsome.randsomeback.domain.candidate.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.smu.randsome.randsomeback.global.annotation.LoginMember;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.smu.randsome.randsomeback.global.support.response.ApiResponse;
import org.smu.randsome.randsomeback.global.swagger.ApiExceptions;

@Tag(name = "Candidate Docs", description = "후보 등록 신청 관련 API 문서")
public abstract class CandidateControllerDocs {

    @Operation(summary = "후보 등록 신청 API",
            description = """
                    ### 후보 등록 신청 API입니다.
                    - 이미 후보 등록 신청이 되어 있는 경우에는 중복 신청이 방지됩니다.
                    - 후보 등록 신청 시 계좌 송금을 통해 후보 등록이 이루어집니다.
                    - 관리자의 승인 후에 최종으로 후보 등록이 완료됩니다.
                    - 성공적으로 신청이 완료되면 200 OK 응답이 반환됩니다.
                    """
    )
    @ApiExceptions(values = {
            ErrorType.UNAUTHORIZED_ERROR,
            ErrorType.NOT_FOUND_MEMBER,
            ErrorType.DUPLICATE_CANDIDATE,
            ErrorType.DEFAULT_ERROR
    })
    public abstract ApiResponse<?> apply(@LoginMember Long memberId);

    @Operation(summary = "후보 등록 신청 철회 API",
            description = """
                    ### 후보 등록 신청 철회 API입니다.
                    - 후보 등록 신청이 승인된 경우에만 신청 철회가 가능합니다.
                    - 신청 철회 시 환불 없이 후보 등록이 철회됩니다.
                    - 성공적으로 신청이 철회되면 200 OK 응답이 반환됩니다.
                    """
    )
    @ApiExceptions(values = {
            ErrorType.UNAUTHORIZED_ERROR,
            ErrorType.NOT_FOUND_CANDIDATE,
            ErrorType.NOT_ALLOW_WITHDRAW_NON_APPROVED,
            ErrorType.DEFAULT_ERROR
    })
    public abstract ApiResponse<?> withdraw(@LoginMember Long memberId);

}
