package org.smu.randsome.randsomeback.admin.candidate.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.smu.randsome.randsomeback.admin.candidate.dto.request.CandidateRejectRequest;
import org.smu.randsome.randsomeback.domain.candidate.dto.response.CandidateRegistrationItem;
import org.smu.randsome.randsomeback.domain.candidate.enums.CandidateRegistrationFilter;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.smu.randsome.randsomeback.global.support.response.ApiResponse;
import org.smu.randsome.randsomeback.global.support.response.CursorSlice;
import org.smu.randsome.randsomeback.global.swagger.ApiExceptions;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

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

    @Operation(summary = "후보자 등록 신청 목록 조회", description = """
            관리자가 후보자 등록 신청 목록을 조회하는 API입니다.
            - `filter`는 조회할 후보자 등록 신청의 상태를 필터링하는 데 사용됩니다.
            - `keyword`는 회원 닉네임 또는 법정 이름을 검색하는 데 사용되는 선택적 검색 키워드입니다.
            - `lastId`와 `size`는 페이지네이션을 위한 매개변수로, 마지막으로 조회된 후보자 등록 신청의 ID와 페이지 크기를 나타냅니다.
            - 성공적으로 조회되면, 필터링된 후보자 등록 신청 목록과 다음 페이지에 대한 정보가 반환됩니다.
            """
    )
    public abstract ApiResponse<CursorSlice<CandidateRegistrationItem>> findCandidates(
            @RequestParam(defaultValue = "PENDING") CandidateRegistrationFilter filter,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long lastId,
            @RequestParam(defaultValue = "20") int size
    );

}