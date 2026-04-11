package org.smu.randsome.randsomeback.domain.ticket.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TicketType {

    RANDOM(3),
    IDEAL(1),
    ;
    private final int defaultQuantity;

}