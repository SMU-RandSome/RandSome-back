package org.smu.randsome.randsomeback.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.smu.randsome.randsomeback.domain.auth.controller.dto.EmailVerificationRequest;
import org.smu.randsome.randsomeback.global.support.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Auth", description = "인증 관련 API")
public abstract class AuthControllerDocs {

    @Operation(summary = "이메일 인증 코드 전송 JWT -[X]", description = "상명대학교 이메일로 6자리 인증 코드를 전송합니다.")
    public abstract ResponseEntity<ApiResponse<?>> sendVerificationCode(@RequestBody @Valid EmailVerificationRequest request);

}