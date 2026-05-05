package org.smu.randsome.randsomeback.domain.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "비밀번호 업데이트 요청")
public record PasswordUpdateRequest(
        @Schema(description = "이메일 인증 토큰", example = "abc123def456")
        @NotBlank(message = "이메일 인증 토큰은 필수입니다.")
        String emailVerificationToken,

        @Schema(description = "이메일", example = "20211233@sangmyung.kr")
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "올바른 이메일 형식이어야 합니다.")
        @Pattern(regexp = ".+@sangmyung\\.kr$", message = "상명대학교 이메일(@sangmyung.kr)만 사용 가능합니다.")
        String email,

        @Schema(description = "새 비밀번호", example = "newSecurePassword!2024")
        @NotBlank(message = "새 비밀번호를 입력해주세요.")
        @Size(min = 8, max = 100, message = "비밀번호는 8자 이상 100자 이하여야 합니다.")
        String newPassword
) {

}