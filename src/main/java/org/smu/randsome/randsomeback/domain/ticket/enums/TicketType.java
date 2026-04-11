package org.smu.randsome.randsomeback.domain.ticket.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.smu.randsome.randsomeback.domain.matching.enums.MatchingType;

@Getter
@AllArgsConstructor
public enum TicketType {

    RANDOM(3),
    IDEAL(1),
    ;
    private final int defaultQuantity;

    public static TicketType from(MatchingType matchingType) {
        return switch (matchingType) {
            case RANDOM -> RANDOM;
            case IDEAL -> IDEAL;
        };
    }

}