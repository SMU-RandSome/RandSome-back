package org.smu.randsome.randsomeback.domain.ticket.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TicketType {

    RANDOM ("랜덤 매칭 티켓"),
    IDEAL  ("이상형 매칭 티켓"),
    ;

    private final String displayName;

}