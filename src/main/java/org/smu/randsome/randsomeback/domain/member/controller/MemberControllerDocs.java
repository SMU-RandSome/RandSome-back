package org.smu.randsome.randsomeback.domain.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.smu.randsome.randsomeback.domain.member.dto.request.DeviceTokenSyncRequest;
import org.smu.randsome.randsomeback.domain.member.dto.request.MemberCreateRequest;
import org.smu.randsome.randsomeback.domain.member.dto.request.MemberUpdateRequest;
import org.smu.randsome.randsomeback.domain.member.dto.request.PasswordUpdateRequest;
import org.smu.randsome.randsomeback.domain.member.dto.response.MemberProfileResponse;
import org.smu.randsome.randsomeback.global.annotation.LoginMember;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.smu.randsome.randsomeback.global.support.response.ApiResponse;
import org.smu.randsome.randsomeback.global.swagger.ApiExceptions;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Member Docs", description = "회원 관련 API 문서")
public abstract class MemberControllerDocs {

    @Operation(
            summary = "회원가입 JWT - [X]",
            description = """
                    ### 사용자가 회원가입을 진행하는 API입니다.
                    - 이메일 인증 토큰이 유효한지 검증합니다.
                    - 회원 기본 정보/소셜 프로필/계좌 정보를 함께 저장합니다.
                    - 약관 동의 여부를 검증 후 저장합니다.
                    - 회원 계좌 정보를 저장합니다.
                    - 성공 시 생성된 회원 ID를 반환합니다.
                    """
    )
    @ApiExceptions(values = {
            ErrorType.BAD_REQUEST,
            ErrorType.INVALID_SIGNUP_REQUEST,
            ErrorType.DUPLICATE_EMAIL,
            ErrorType.DEFAULT_ERROR
    })
    public abstract ApiResponse<Long> signUp(@RequestBody @Valid MemberCreateRequest request);

    @Operation(
            summary = "내 프로필 조회 JWT - [O]",
            description = """
                    ### 로그인한 회원의 프로필 정보를 조회하는 API입니다.
                    - JWT 인증이 필요합니다.
                    - 성공 시 회원 프로필 정보를 반환합니다.
                    - 후보자 신청 이력이 없으면 `candidateRegistrationStatus`는 `NOT_APPLIED`로 반환됩니다.
                    - 신청 이력이 있으면 가장 최근 신청의 상태(`PENDING`, `APPROVED`, `REJECTED`, `WITHDRAWN`)를 반환합니다.
                    """
    )
    @ApiExceptions(values = {
            ErrorType.NOT_FOUND_MEMBER,
            ErrorType.DEFAULT_ERROR
    })
    public abstract ApiResponse<MemberProfileResponse> getMyProfile(@LoginMember Long memberId);

    @Operation(
            summary = "내 프로필 수정 JWT - [O]",
            description = """
                    ### 로그인한 회원의 프로필 정보를 수정하는 API입니다.
                    - JWT 인증이 필요합니다.
                    - 실명, MBTI, 은행명, 계좌번호는 필수 값입니다.
                    - 인스타그램 ID, 자기소개, 이상형 소개는 선택 값입니다.
                    - 성공 시 200을 반환합니다.
                    """
    )
    @ApiExceptions(values = {
            ErrorType.BAD_REQUEST,
            ErrorType.NOT_FOUND_MEMBER,
            ErrorType.DEFAULT_ERROR
    })
    public abstract ApiResponse<?> updateProfile(
            @RequestBody @Valid MemberUpdateRequest request,
            @LoginMember Long memberId
    );

    @Operation(
            summary = "비밀번호 수정 JWT - [x]",
            description = """
                    ### 로그인한 회원의 비밀번호를 수정하는 API입니다.
                    - 비밀번호를 변경하기 위해선 이메일 인증이 선행되어야 합니다.
                    - 이메일 인증 토큰이 유효한지 검증합니다.
                    - 성공 시 200을 반환합니다.
                    """
    )
    @ApiExceptions(values = {
            ErrorType.BAD_REQUEST,
            ErrorType.INVALID_PASSWORD_UPDATE_REQUEST,
            ErrorType.NOT_FOUND_MEMBER,
            ErrorType.DEFAULT_ERROR
    })
    public abstract ApiResponse<?> updatePassword(
            @RequestBody @Valid PasswordUpdateRequest request
    );

    @Operation(
            summary = "디바이스 토큰 동기화 - JWT [O]",
            description = """
                    ### 디바이스 토큰을 동기화합니다.
                    - 사용자가 사용하는 디바이스의 FCM 토큰을 서버에 동기화합니다.
                    - 로그인 시 또는 디바이스 변경 시 호출됩니다.
                    - 성공 시 빈 응답을 반환합니다.
                    """
    )
    @ApiExceptions(values = {
            ErrorType.BAD_REQUEST,
            ErrorType.EMPTY_SECURITY_CONTEXT,
            ErrorType.NOT_FOUND_MEMBER,
            ErrorType.DEFAULT_ERROR
    })
    public abstract ApiResponse<?> syncDevices(
            @Valid DeviceTokenSyncRequest request,
            Long memberId
    );

    @Operation(
            summary = "디바이스 토큰 삭제 - JWT [O]",
            description = """
                    ### 디바이스 토큰을 삭제합니다.
                    - 사용자가 로그아웃하거나 더 이상 푸시 알림을 받지 않으려는 경우 호출됩니다.
                    - 성공 시 빈 응답을 반환합니다.
                    """
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "디바이스 토큰 삭제 성공")
    @ApiExceptions(values = {
            ErrorType.UNAUTHORIZED_ERROR,
            ErrorType.NOT_FOUND_MEMBER,
            ErrorType.NOT_FOUND_FCM_TOKEN,
            ErrorType.DEFAULT_ERROR
    })
    public abstract ApiResponse<?> deleteDevice(
            @RequestParam String deviceToken,
            @LoginMember Long memberId
    );

    @Operation(summary = "후보자 등록 철회 API",
            description = """
                    ### 후보자 등록을 철회하는 API입니다.
                    - JWT 인증이 필요합니다.
                    - 후보자 역할에서 일반 회원 역할로 변경됩니다.
                    - 후보자가 아닐 경우 철회가 허용되지 않습니다.
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