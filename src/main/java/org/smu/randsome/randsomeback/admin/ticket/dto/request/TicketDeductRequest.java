package org.smu.randsome.randsomeback.admin.ticket.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketType;

@Schema(description = "관리자 수동 티켓 차감 요청")
public record TicketDeductRequest(

        @Schema(description = "차감 대상 회원 ID", example = "1")
        @NotNull(message = "회원 ID는 필수입니다.")
        Long memberId,

        @Schema(description = "차감할 티켓 종류", example = "RANDOM")
        @NotNull(message = "티켓 타입은 필수입니다.")
        TicketType ticketType,

        @Schema(description = "차감할 티켓 수량", example = "3")
        @Min(value = 1, message = "티켓 수량은 1 이상이어야 합니다.")
        @Max(value = 100, message = "티켓 수량은 100 이하여야 합니다.")
        int amount,

        @Schema(description = "차감 사유", example = "부정 사용 제재")
        String reason
) {
}
