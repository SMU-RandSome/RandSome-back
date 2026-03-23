package org.smu.randsome.randsomeback.domain.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "디바이스 토큰 동기화 요청")
public record DeviceTokenSyncRequest(
        @Schema(description = "디바이스 토큰", example = "fcm_device_token_12345")
        @NotBlank(message = "디바이스 토큰은 필수입니다.")
        String deviceToken
) {

}