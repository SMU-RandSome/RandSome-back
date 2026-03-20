package org.smu.randsome.randsomeback.admin.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.smu.randsome.randsomeback.admin.member.dto.response.MemberAdminResponse;
import org.smu.randsome.randsomeback.admin.member.dto.response.MemberDetailResponse;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.smu.randsome.randsomeback.global.support.response.ApiResponse;
import org.smu.randsome.randsomeback.global.support.response.PageResponse;
import org.smu.randsome.randsomeback.global.swagger.ApiExceptions;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;

@Tag(name = "관리자 회원 관리 API", description = "관리자용 회원 관리 API 문서")
public abstract class MemberAdminControllerDocs {

    @Operation(
            summary = "회원 목록 조회",
            description = """
                    #### 관리자 회원 목록 조회 API입니다.
                    - 활성 상태(EntityStatus.ACTIVE)의 회원 목록을 조회합니다.
                    - 페이지네이션이 적용됩니다.
                    
                    **페이지 파라미터**
                    - page : 페이지 번호 (0부터 시작, 기본값 0)
                    - size : 페이지 당 데이터 수 (기본값 10)
                    """
    )
    @ApiExceptions(values = {
            ErrorType.DEFAULT_ERROR
    })
    public abstract ApiResponse<PageResponse<MemberAdminResponse>> getMembers(
            @ParameterObject Pageable pageable
    );

    @Operation(
            summary = "회원 상세 조회",
            description = """
                    #### 관리자 회원 상세 조회 API입니다.
                    - 특정 회원의 상세 정보를 반환합니다.
                    - 해당 회원의 은행 계좌 정보까지 포함됩니다.
                    
                    **요청 경로 파라미터**
                    - memberId : 조회할 회원의 고유 ID
                    """
    )
    @ApiExceptions(values = {
            ErrorType.NOT_FOUND_MEMBER,
            ErrorType.NOT_FOUND_BANK_ACCOUNT,
            ErrorType.DEFAULT_ERROR
    })
    public abstract ApiResponse<MemberDetailResponse> getMemberDetail(
            @Parameter(name = "memberId", description = "조회할 회원의 고유 ID", required = true) Long memberId
    );

    @Operation(
            summary = "회원 검색",
            description = """
                #### 관리자 회원 검색 API입니다.
                - 닉네임 또는 실명으로 회원을 검색합니다.
                - 활성 상태(EntityStatus.ACTIVE)의 회원만 검색됩니다.
                - 관리자(ROLE_ADMIN)는 검색 결과에서 제외됩니다.
                - 페이지네이션이 적용됩니다.
                
                **요청 파라미터**
                - query : 검색어 (닉네임 또는 실명)
                - page : 페이지 번호 (0부터 시작, 기본값 0)
                - size : 페이지 당 데이터 수 (기본값 10)
                """
    )
    @ApiExceptions(values = {
            ErrorType.DEFAULT_ERROR
    })
    public abstract ApiResponse<PageResponse<MemberAdminResponse>> searchMembers(
            @Parameter(name = "query", description = "검색어 (닉네임 또는 실명)", required = false) String query,
            @ParameterObject Pageable pageable
    );

}