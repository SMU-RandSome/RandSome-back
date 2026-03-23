package org.smu.randsome.randsomeback.domain.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "디바이스 토큰 동기화 요청")
public record DeviceTokenSyncRequest(
        @Schema(description = "디바이스 토큰", example = "fcm_device_token_12345")
        @Size(max = 512, message = "디바이스 토큰은 512자를 초과할 수 없습니다.")
        @NotBlank(message = "디바이스 토큰은 필수입니다.")
        String deviceToken
) {

}