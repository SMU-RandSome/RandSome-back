package org.smu.randsome.randsomeback.domain.ticket.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;
import org.smu.randsome.randsomeback.domain.ticket.entity.TicketHistory;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketActionType;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketSource;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketType;

@Builder
@Schema(description = "티켓 변동 이력 항목")
public record TicketHistoryItem(
        @Schema(description = "이력 식별자")
        Long id,

        @Schema(description = "티켓 유형", example = "RANDOM")
        TicketType ticketType,

        @Schema(description = "변동 구분 (USE: 사용, EARN: 획득)", example = "USE")
        TicketActionType actionType,

        @Schema(description = "변동 출처", example = "MATCHING")
        TicketSource source,

        @Schema(description = "변동 수량", example = "3")
        int amount,

        @Schema(description = "변동 설명", example = "매칭 사용으로 인한 티켓 차감")
        String description,

        @Schema(description = "변동 일시")
        LocalDateTime createdAt
) {

    public static TicketHistoryItem from(TicketHistory ticketHistory) {
        return new TicketHistoryItem(
                ticketHistory.getId(),
                ticketHistory.getTicketType(),
                ticketHistory.getActionType(),
                ticketHistory.getSource(),
                ticketHistory.getAmount(),
                ticketHistory.getDescription(),
                ticketHistory.getCreatedAt()
        );
    }

}