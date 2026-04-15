package org.smu.randsome.randsomeback.domain.matching.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.smu.randsome.randsomeback.domain.matching.dto.request.MatchingApplyRequest;
import org.smu.randsome.randsomeback.domain.matching.dto.response.MatchingHistoryItem;
import org.smu.randsome.randsomeback.domain.matching.dto.response.MatchingResultDetailItem;
import org.smu.randsome.randsomeback.global.annotation.LoginMember;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.smu.randsome.randsomeback.global.support.response.ApiResponse;
import org.smu.randsome.randsomeback.global.swagger.ApiExceptions;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Matching Docs", description = "매칭 관련 API 문서")
public abstract class MatchingControllerDocs {

    @Operation(
            summary = "매칭 신청 API - JWT [O]",
            description = """
                    ### 매칭 신청 API입니다.
                    - 사용자가 매칭 유형(`RANDOM`, `IDEAL`)과 신청 인원 수(1~5명)를 전달해 매칭을 신청합니다.
                    - 신청 시 요청 인원에 해당하는 티켓이 자동으로 차감됩니다.
                    - 신청이 생성되면 즉시 매칭 알고리즘이 실행되어 매칭 결과가 생성됩니다.
                    - `IDEAL` 매칭인 경우 요청 DTO에 이상형 조건(성격, 얼굴상, 연애 스타일)을 포함할 수 있습니다.
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
    public abstract ApiResponse<?> apply(
            @RequestBody @Valid MatchingApplyRequest request,
            @LoginMember Long memberId
    );

    @Operation(
            summary = "매칭 신청 내역 목록 조회 API - JWT [O]",
            description = """
                    ### 매칭 신청 내역 목록 조회 API입니다.
                    - 현재 로그인한 사용자의 모든 매칭 신청 내역을 조회합니다.
                    - 최신 순(ID 내림차순)으로 정렬되어 반환됩니다.
                    - 성공 시 200 OK 와 함께 매칭 신청 목록이 반환됩니다.
                    """
    )
    @ApiExceptions(values = {
            ErrorType.UNAUTHORIZED_ERROR,
            ErrorType.DEFAULT_ERROR
    })
    public abstract ApiResponse<List<MatchingHistoryItem>> findMatchings(
            @LoginMember Long memberId
    );

    @Operation(
            summary = "승인된 신청 내역 조회 API - JWT [O]",
            description = """
                    ### 승인된 신청 내역 조회 API입니다.
                    - `applicationId` 경로 변수로 신청 ID를 전달합니다.
                    - 해당 신청이 승인된 상태여야만 매칭 결과를 조회할 수 있습니다.
                    - 성공 시 200 OK 와 함께 매칭 결과 목록이 반환됩니다.
                    """
    )
    @ApiExceptions(values = {
            ErrorType.BAD_REQUEST,
            ErrorType.UNAUTHORIZED_ERROR,
            ErrorType.NOT_FOUND_APPROVED_MATCHING,
            ErrorType.DEFAULT_ERROR
    })
    public abstract ApiResponse<List<MatchingResultDetailItem>> getApprovedApplication(
            @Parameter(
                    description = "신청 ID",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "1"
            )
            Long applicationId,
            @LoginMember Long memberId
    );

    @Operation(
            summary = "매칭 신청 취소 API - JWT [O]",
            description = """
                    ### 매칭 신청 취소 API입니다.
                    - PENDING 상태의 매칭 신청만 취소할 수 있습니다.
                    - 승인(APPROVED) 또는 거절(FAIL)된 신청은 취소할 수 없습니다.
                    - 성공 시 200 OK 응답이 반환됩니다.
                    """
    )
    @ApiExceptions(values = {
            ErrorType.UNAUTHORIZED_ERROR,
            ErrorType.NOT_FOUND_MATCHING,
            ErrorType.NOT_ALLOW_CANCEL_APPROVED,
            ErrorType.NOT_ALLOW_CANCEL_REJECTED,
            ErrorType.DEFAULT_ERROR
    })
    public abstract ApiResponse<?> cancel(
            @Parameter(
                    description = "신청 ID",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "1"
            ) Long applicationId,
            @LoginMember Long memberId
    );

}