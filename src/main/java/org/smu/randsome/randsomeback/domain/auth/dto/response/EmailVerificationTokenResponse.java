package org.smu.randsome.randsomeback.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "이메일 인증 토큰 응답")
public record EmailVerificationTokenResponse(

        @Schema(description = "이메일 인증 JWT 토큰 (10분 유효)", example = "eyJhbGciOiJIUzI1NiJ9...")
        String emailVerificationToken
) {

}