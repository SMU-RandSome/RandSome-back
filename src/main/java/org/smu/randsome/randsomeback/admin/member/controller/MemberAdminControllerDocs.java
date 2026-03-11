package org.smu.randsome.randsomeback.admin.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springdoc.core.annotations.ParameterObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.smu.randsome.randsomeback.admin.member.controller.dto.response.MemberAdminResponse;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.smu.randsome.randsomeback.global.support.response.ApiResponse;
import org.smu.randsome.randsomeback.global.support.response.PageResponse;
import org.smu.randsome.randsomeback.global.swagger.ApiExceptions;
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

}