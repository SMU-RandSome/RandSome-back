package org.smu.randsome.randsomeback.domain.auth.controller.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "이메일 인증 코드 검증 요청")
public record EmailVerificationCodeVerifyRequest(

        @NotBlank(message = "이메일을 입력해주세요.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        @Pattern(
                regexp = "^[a-zA-Z0-9._%+\\-]+@sangmyung\\.kr$",
                message = "상명대학교 이메일(@sangmyung.kr)만 사용 가능합니다."
        )
        @Schema(description = "이메일 주소", example = "20222133@sangmyung.kr")
        String email,

        @NotBlank(message = "인증 코드를 입력해주세요.")
        @Size(min = 6, max = 6, message = "인증 코드는 6자리여야 합니다.")
        @Pattern(regexp = "\\d{6}", message = "인증 코드는 숫자 6자리여야 합니다.")
        @Schema(description = "6자리 인증 코드", example = "123456")
        String code
) {

}