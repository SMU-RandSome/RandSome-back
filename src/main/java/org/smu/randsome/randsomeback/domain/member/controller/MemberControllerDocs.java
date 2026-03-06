package org.smu.randsome.randsomeback.domain.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.smu.randsome.randsomeback.domain.member.controller.dto.MemberCreateRequest;
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

}