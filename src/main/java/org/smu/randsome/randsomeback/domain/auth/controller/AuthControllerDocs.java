package org.smu.randsome.randsomeback.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.smu.randsome.randsomeback.domain.auth.controller.dto.request.EmailVerificationCodeVerifyRequest;
import org.smu.randsome.randsomeback.domain.auth.controller.dto.request.EmailVerificationRequest;
import org.smu.randsome.randsomeback.domain.auth.controller.dto.request.LoginRequest;
import org.smu.randsome.randsomeback.domain.auth.controller.dto.response.EmailVerificationTokenResponse;
import org.smu.randsome.randsomeback.global.jwt.dto.TokenResponse;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.smu.randsome.randsomeback.global.support.response.ApiResponse;
import org.smu.randsome.randsomeback.global.swagger.ApiExceptions;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Auth", description = "인증 관련 API")
public abstract class AuthControllerDocs {

    @Operation(summary = "이메일 인증 코드 발송 JWT - [X]",
            description = """
                    ### 사용자가 회원가입 시 이메일 인증을 위해 6자리 인증 코드를 발송하는 API입니다.
                    - 요청 시 이메일 형식이 유효한지 검증합니다.
                    - 인증 코드는 6자리 숫자로 구성되며, 발송 후 5분간 유효합니다.
                    """

    )
    @ApiExceptions(values = {
            ErrorType.BAD_REQUEST,
            ErrorType.EMAIL_SEND_FAILED,
            ErrorType.DEFAULT_ERROR
    })
    public abstract ResponseEntity<ApiResponse<?>> sendVerificationCode(@RequestBody @Valid EmailVerificationRequest request);

    @Operation(summary = "이메일 인증 코드 검증 JWT - [X]",
            description = """
                    ### 사용자가 이메일로 받은 6자리 인증 코드를 검증하는 API입니다.
                    - 요청 시 이메일 형식이 유효한지 검증합니다.
                    - 인증 코드는 발송 후 5분간 유효하며, 검증 성공 시 유효기간이 10분인 회원가입 토큰을 발급합니다.
                    """
    )
    @ApiExceptions(values = {
            ErrorType.BAD_REQUEST,
            ErrorType.DEFAULT_ERROR
    })
    public abstract ResponseEntity<ApiResponse<EmailVerificationTokenResponse>> verifyEmailVerificationCode(
            @RequestBody @Valid EmailVerificationCodeVerifyRequest request
    );


    @Operation(summary = "로그인 JWT - [X]",
            description = """
                    ### 사용자가 이메일과 비밀번호로 로그인하는 API입니다.
                    - 요청 시 이메일 형식이 유효한지 검증합니다.
                    - 로그인 성공 시 액세스 토큰과 리프레시 토큰을 발급합니다.
                    """
    )
    @ApiExceptions(values = {
            ErrorType.BAD_REQUEST,
            ErrorType.INVALID_ACCOUNT,
            ErrorType.DEFAULT_ERROR
    })
    public abstract ResponseEntity<ApiResponse<TokenResponse>> login(@RequestBody @Valid LoginRequest request);

}