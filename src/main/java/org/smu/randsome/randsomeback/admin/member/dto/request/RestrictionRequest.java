package org.smu.randsome.randsomeback.admin.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "회원 정지 요청")
public record RestrictionRequest(
        @NotBlank(message = "정지 사유는 필수입니다.")
        @Schema(description = "정지 사유", example = "부적절한 행동")
        String reason
) {

}