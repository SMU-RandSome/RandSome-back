package org.smu.randsome.randsomeback.domain.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.smu.randsome.randsomeback.domain.member.controller.dto.MemberCreateRequest;
import org.smu.randsome.randsomeback.domain.member.controller.dto.MemberProfileResponse;
import org.smu.randsome.randsomeback.domain.member.controller.dto.request.MemberUpdateRequest;
import org.smu.randsome.randsomeback.global.annotation.LoginMember;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.smu.randsome.randsomeback.global.support.response.ApiResponse;
import org.smu.randsome.randsomeback.global.swagger.ApiExceptions;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

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
    public abstract ResponseEntity<ApiResponse<Long>> signUp(@RequestBody @Valid MemberCreateRequest request);

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
    public abstract ResponseEntity<ApiResponse<MemberProfileResponse>> getMyProfile(@LoginMember Long memberId);

    @Operation(
            summary = "내 프로필 수정 JWT - [O]",
            description = """
                    ### 로그인한 회원의 프로필 정보를 수정하는 API입니다.
                    - JWT 인증이 필요합니다.
                    - 실명, MBTI는 필수 값입니다.
                    - 인스타그램 ID, 자기소개, 이상형 소개는 선택 값입니다.
                    - 성공 시 응답 바디 없이 200을 반환합니다.
                    """
    )
    @ApiExceptions(values = {
            ErrorType.BAD_REQUEST,
            ErrorType.NOT_FOUND_MEMBER,
            ErrorType.DEFAULT_ERROR
    })
    public abstract ResponseEntity<Void> updateProfile(
            @RequestBody @Valid MemberUpdateRequest request,
            @LoginMember Long memberId
    );

}