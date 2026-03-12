package org.smu.randsome.randsomeback.domain.matching.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.smu.randsome.randsomeback.domain.matching.controller.dto.MatchingApplyRequest;
import org.smu.randsome.randsomeback.global.annotation.LoginMember;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.smu.randsome.randsomeback.global.support.response.ApiResponse;
import org.smu.randsome.randsomeback.global.swagger.ApiExceptions;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Matching Docs", description = "매칭 관련 API 문서")
public abstract class MatchingControllerDocs {

    @Operation(
            summary = "매칭 신청 API - JWT [O]",
            description = """
                    ### 매칭 신청 API입니다.
                    - 사용자가 매칭 유형(`RANDOM`, `IDEAL`)과 신청 인원 수(1~5명)를 전달해 매칭을 신청합니다.
                    - 신청이 생성되면 결제 대기 상태가 함께 등록됩니다.
                    - 결제 유형은 매칭 유형에 따라 자동 변환됩니다.
                    - 성공 시 200 OK 응답이 반환됩니다.
                    """
    )
    @ApiExceptions(values = {
            ErrorType.BAD_REQUEST,
            ErrorType.UNAUTHORIZED_ERROR,
            ErrorType.NOT_FOUND_MEMBER,
            ErrorType.INVALID_PERSON_COUNT,
            ErrorType.DEFAULT_ERROR
    })
    public abstract ResponseEntity<ApiResponse<?>> apply(
            @RequestBody @Valid MatchingApplyRequest request,
            @LoginMember Long memberId
    );

}