package org.smu.randsome.randsomeback.admin.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.smu.randsome.randsomeback.admin.member.dto.request.RestrictionRequest;
import org.smu.randsome.randsomeback.admin.member.dto.response.MemberAdminResponse;
import org.smu.randsome.randsomeback.admin.member.dto.response.MemberDetailResponse;
import org.smu.randsome.randsomeback.domain.member.enums.Role;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.smu.randsome.randsomeback.global.support.response.ApiResponse;
import org.smu.randsome.randsomeback.global.support.response.PageResponse;
import org.smu.randsome.randsomeback.global.swagger.ApiExceptions;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "관리자 회원 관리 API", description = "관리자용 회원 관리 API 문서")
public abstract class MemberAdminControllerDocs {

    @Operation(
            summary = "회원 목록 조회",
            description = """
                    #### 관리자 회원 목록 조회 API입니다.
                    - 삭제된 회원을 제외한 전체 회원(활성·정지 포함) 목록을 조회합니다.
                    - 닉네임 또는 실명으로 검색할 수 있습니다.
                    - 페이지네이션이 적용됩니다.

                    **검색 파라미터**
                    - keyword : 닉네임 또는 실명 검색어 (선택, 미입력 시 전체 조회)

                    **페이지 파라미터**
                    - page : 페이지 번호 (1부터 시작, 기본값 1)
                    - size : 페이지 당 데이터 수 (기본값 20, 최대 30)
                    """
    )
    @ApiExceptions(values = {
            ErrorType.DEFAULT_ERROR
    })
    public abstract ApiResponse<PageResponse<MemberAdminResponse>> findMembers(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    );

    @Operation(
            summary = "회원 상세 조회",
            description = """
                    #### 관리자 회원 상세 조회 API입니다.
                    - 특정 회원의 상세 정보를 반환합니다.
                    
                    **요청 경로 파라미터**
                    - memberId : 조회할 회원의 고유 ID
                    """
    )
    @ApiExceptions(values = {
            ErrorType.NOT_FOUND_MEMBER,
            ErrorType.NOT_FOUND_BANK_ACCOUNT,
            ErrorType.DEFAULT_ERROR
    })
    public abstract ApiResponse<MemberDetailResponse> findMemberDetail(
            @Parameter(name = "memberId", description = "조회할 회원의 고유 ID", required = true) Long memberId
    );

    @Operation(
            summary = "회원 정지",
            description = """
                    #### 관리자 회원 정지 API입니다.
                    - 특정 회원을 정지 상태로 변경합니다.
                    - 정지 사유를 함께 기록합니다.
                    
                    **요청 경로 파라미터**
                    - memberId : 정지할 회원의 고유 ID
                    
                    **요청 본문**
                    - reason : 정지 사유 (예: 부적절한 행동)
                    """
    )
    @ApiExceptions(values = {
            ErrorType.NOT_FOUND_MEMBER,
            ErrorType.DEFAULT_ERROR
    })
    public abstract ApiResponse<?> suspendMember(
            @Parameter(
                    name = "memberId",
                    description = "정지할 회원의 고유 ID",
                    required = true
            )
            Long memberId,
            @RequestBody RestrictionRequest request
    );

    @Operation(
            summary = "회원 정지 해제",
            description = """
                    #### 관리자 회원 정지 해제 API입니다.
                    - 정지된 회원을 활성 상태로 복구합니다.
                    - Redis 블랙리스트에서 해당 회원을 제거합니다.

                    **요청 경로 파라미터**
                    - memberId : 복구할 회원의 고유 ID
                    """
    )
    @ApiExceptions(values = {
            ErrorType.NOT_FOUND_MEMBER,
            ErrorType.DEFAULT_ERROR
    })
    public abstract ApiResponse<?> restoreMember(
            @Parameter(name = "memberId", description = "복구할 회원의 고유 ID", required = true)
            Long memberId
    );

    @Operation(
            summary = "회원 역할 변경",
            description = """
                    #### 관리자 회원 역할 변경 API입니다.
                    - 특정 회원의 역할(Role)을 변경합니다.
                    - ROLE_SUSPEND_MEMBER로의 변경은 불가합니다. (정지는 전용 API를 사용하세요)

                    **요청 경로 파라미터**
                    - memberId : 역할을 변경할 회원의 고유 ID

                    **요청 쿼리 파라미터**
                    - role : 변경할 역할 (ROLE_MEMBER, ROLE_CANDIDATE, ROLE_ADMIN)
                    """
    )
    @ApiExceptions(values = {
            ErrorType.NOT_FOUND_MEMBER,
            ErrorType.INVALID_ROLE_UPDATE,
            ErrorType.DEFAULT_ERROR
    })
    public abstract ApiResponse<?> updateRole(
            @Parameter(name = "memberId", description = "역할을 변경할 회원의 고유 ID", required = true)
            Long memberId,
            @Parameter(name = "role", description = "변경할 역할", required = true)
            Role role
    );

}