package org.smu.randsome.randsomeback.domain.ticket.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;

@Embeddable
public record TicketQuantity(
        @Column(name = "quantity", nullable = false)
        int value
) {

    public TicketQuantity {
        if (value < 0) {
            throw new CoreException(ErrorType.INVALID_TICKET_AMOUNT);
        }
    }

    public static TicketQuantity initial(int value) {
        return new TicketQuantity(value);
    }

    public TicketQuantity plus(int amount) {
        if (amount <= 0) {
            throw new CoreException(ErrorType.INVALID_TICKET_AMOUNT);
        }
        return new TicketQuantity(this.value + amount);
    }

    public TicketQuantity minus(int amount) {
        if (amount <= 0) {
            throw new CoreException(ErrorType.INVALID_TICKET_AMOUNT);
        }

        if (this.value < amount) {
            throw new CoreException(ErrorType.NOT_ENOUGH_TICKETS);
        }

        return new TicketQuantity(this.value - amount);
    }

    public TicketQuantity minusUpTo(int amount) {
        if (amount <= 0) {
            throw new CoreException(ErrorType.INVALID_TICKET_AMOUNT);
        }
        int deducted = Math.min(this.value, amount);
        return new TicketQuantity(this.value - deducted);
    }

    public boolean isZero() {
        return this.value == 0;
    }

    public boolean isLessThan(int amount) {
        return this.value < amount;
    }

}