package org.smu.randsome.randsomeback.global.jwt.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "토큰 응답 DTO")
@Builder
public record TokenResponse(
        @Schema(description = "액세스 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String accessToken,

        @Schema(description = "리프레시 토큰", example = "dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4...")
        String refreshToken
) {

}