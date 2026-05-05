package org.smu.randsome.randsomeback.domain.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.auth.dto.command.VerificationEmailCode;
import org.smu.randsome.randsomeback.domain.auth.dto.request.EmailVerificationCodeVerifyRequest;
import org.smu.randsome.randsomeback.domain.auth.dto.request.EmailVerificationRequest;
import org.smu.randsome.randsomeback.domain.auth.dto.request.LoginRequest;
import org.smu.randsome.randsomeback.domain.auth.dto.request.TokenReissueRequest;
import org.smu.randsome.randsomeback.domain.auth.dto.response.EmailVerificationTokenResponse;
import org.smu.randsome.randsomeback.domain.auth.enums.VerificationPurpose;
import org.smu.randsome.randsomeback.domain.auth.service.AuthService;
import org.smu.randsome.randsomeback.domain.auth.service.EmailVerificationService;
import org.smu.randsome.randsomeback.global.annotation.LoginMember;
import org.smu.randsome.randsomeback.global.jwt.dto.TokenResponse;
import org.smu.randsome.randsomeback.global.support.response.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class AuthController extends AuthControllerDocs {

    private final AuthService authService;
    private final EmailVerificationService emailVerificationService;

    @Override
    @PostMapping("/v1/auth/email/verification-codes")
    public ApiResponse<?> sendVerificationCode(
            @RequestBody @Valid EmailVerificationRequest request
    ) {
        emailVerificationService.sendVerificationCodeAsync(request.email());

        return ApiResponse.success();
    }

    @Override
    @PostMapping("/v1/auth/email/verification-codes/verify")
    public ApiResponse<EmailVerificationTokenResponse> verifyEmailVerificationCode(
            @RequestBody @Valid EmailVerificationCodeVerifyRequest request,
            @RequestParam VerificationPurpose purpose
    ) {
        String token = emailVerificationService.verifyEmailCode(new VerificationEmailCode(
                request.email(),
                request.code(),
                purpose
        ));

        return ApiResponse.success(new EmailVerificationTokenResponse(token));
    }

    @Override
    @PostMapping("/v1/auth/login")
    public ApiResponse<TokenResponse> login(@RequestBody @Valid LoginRequest request) {
        TokenResponse response = authService.login(request.email(), request.password());

        return ApiResponse.success(response);
    }

    @Override
    @PostMapping("/v1/auth/reissue")
    public ApiResponse<TokenResponse> reissueToken(@RequestBody @Valid TokenReissueRequest request) {
        TokenResponse response = authService.reissue(request.refreshToken());

        return ApiResponse.success(response);
    }

    @Override
    @PostMapping("/v1/auth/logout")
    public ApiResponse<?> logout(@LoginMember Long memberId) {
        authService.logout(memberId);

        return ApiResponse.success();
    }

}
