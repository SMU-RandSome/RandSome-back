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

    @Operation(summary = "후보 등록 신청 취소 API",
            description = """
                    ### 후보 등록 신청 취소 API입니다.
                    - 승인 대기(PENDING) 상태인 신청만 취소할 수 있습니다.
                    - 취소 시 등록된 결제 정보도 함께 취소됩니다.
                    - 성공적으로 취소되면 200 OK 응답이 반환됩니다.
                    """
    )
    @ApiExceptions(values = {
            ErrorType.UNAUTHORIZED_ERROR,
            ErrorType.NOT_FOUND_MEMBER,
            ErrorType.NOT_ALLOW_CANCEL_NON_PENDING,
            ErrorType.DEFAULT_ERROR
    })
    public abstract ApiResponse<?> cancel(@LoginMember Long memberId);

}