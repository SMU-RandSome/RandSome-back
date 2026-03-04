package org.smu.randsome.randsomeback.domain.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.domain.auth.controller.dto.EmailVerificationRequest;
import org.smu.randsome.randsomeback.domain.auth.service.AuthService;
import org.smu.randsome.randsomeback.global.support.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class AuthController extends AuthControllerDocs {

    private final AuthService authService;

    @Override
    @PostMapping("/v1/auth/email/verification-codes")
    public ResponseEntity<ApiResponse<?>> sendVerificationCode(
            @RequestBody @Valid EmailVerificationRequest request
    ) {
        authService.sendVerificationCode(request.email());

        return ResponseEntity.ok(ApiResponse.success());
    }

}