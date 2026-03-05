package org.smu.randsome.randsomeback.domain.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.auth.controller.dto.request.EmailVerificationCodeVerifyRequest;
import org.smu.randsome.randsomeback.domain.auth.controller.dto.request.EmailVerificationRequest;
import org.smu.randsome.randsomeback.domain.auth.controller.dto.request.LoginRequest;
import org.smu.randsome.randsomeback.domain.auth.controller.dto.response.EmailVerificationTokenResponse;
import org.smu.randsome.randsomeback.domain.auth.service.AuthService;
import org.smu.randsome.randsomeback.domain.auth.service.EmailVerificationService;
import org.smu.randsome.randsomeback.global.jwt.dto.TokenResponse;
import org.smu.randsome.randsomeback.global.support.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class AuthController extends AuthControllerDocs {

    private final AuthService authService;
    private final EmailVerificationService emailVerificationService;

    @Override
    @PostMapping("/v1/auth/email/verification-codes")
    public ResponseEntity<ApiResponse<?>> sendVerificationCode(
            @RequestBody @Valid EmailVerificationRequest request
    ) {
        emailVerificationService.sendVerificationCodeAsync(request.email());

        return ResponseEntity.ok(ApiResponse.success());
    }

    @Override
    @PostMapping("/v1/auth/email/verification-codes/verify")
    public ResponseEntity<ApiResponse<EmailVerificationTokenResponse>> verifyEmailVerificationCode(
            @RequestBody @Valid EmailVerificationCodeVerifyRequest request
    ) {
        String token = emailVerificationService.verifyEmailCode(request.email(), request.code());

        return ResponseEntity.ok(ApiResponse.success(new EmailVerificationTokenResponse(token)));
    }

    @Override
    @PostMapping("/v1/auth/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@RequestBody @Valid LoginRequest request) {
        TokenResponse response = authService.login(request.email(), request.password());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

}