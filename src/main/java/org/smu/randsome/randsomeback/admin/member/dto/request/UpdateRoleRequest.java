package org.smu.randsome.randsomeback.admin.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import org.smu.randsome.randsomeback.domain.member.enums.Role;

@Schema(description = "회원 역할 변경 요청")
public record UpdateRoleRequest(
        @NotNull(message = "변경할 역할은 필수입니다.")
        @Schema(description = "변경할 역할", example = "ROLE_CANDIDATE")
        Role role
) {

}
