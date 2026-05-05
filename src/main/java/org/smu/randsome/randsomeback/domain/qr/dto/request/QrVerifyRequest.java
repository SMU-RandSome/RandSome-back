package org.smu.randsome.randsomeback.domain.qr.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketType;

@Schema(description = "QR 코드 인증 요청 DTO")
public record QrVerifyRequest(
        @Schema(description = "QR 토큰", example = "abc123def456")
        @NotBlank(message = "QR 토큰은 필수입니다.")
        String qrToken,

        @Schema(description = "티켓 타입")
        @NotNull(message = "티켓 타입은 필수입니다.")
        TicketType ticketType
) {

}