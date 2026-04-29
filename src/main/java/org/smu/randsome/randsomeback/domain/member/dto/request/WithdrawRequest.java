package org.smu.randsome.randsomeback.domain.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "회원 탈퇴 요청")
public record WithdrawRequest(
        @Schema(description = "현재 비밀번호", example = "password123!")
        @NotBlank(message = "비밀번호를 입력해주세요.")
        String password
) {

}