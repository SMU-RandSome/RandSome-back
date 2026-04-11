package org.smu.randsome.randsomeback.domain.ticket.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Builder;
import org.smu.randsome.randsomeback.domain.ticket.entity.Ticket;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketType;

@Builder
@Schema(description = "티켓 잔고 응답")
public record TicketBalanceResponse(
        @Schema(description = "랜덤 매칭 티켓 수량", example = "3")
        int randomTicketCount,

        @Schema(description = "이상형 매칭 티켓 수량", example = "1")
        int idealTicketCount
) {

    public static TicketBalanceResponse from(List<Ticket> tickets) {
        Map<TicketType, Integer> map = tickets.stream().collect(
                Collectors.groupingBy(
                        Ticket::getTicketType,
                        Collectors.summingInt(Ticket::getQuantityValue)
                ));

        return TicketBalanceResponse.builder()
                .randomTicketCount(map.getOrDefault(TicketType.RANDOM, 0))
                .idealTicketCount(map.getOrDefault(TicketType.IDEAL, 0))
                .build();
    }

}