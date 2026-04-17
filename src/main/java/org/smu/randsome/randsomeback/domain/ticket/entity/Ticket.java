package org.smu.randsome.randsomeback.domain.ticket.entity;

import static java.util.Objects.requireNonNull;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketType;
import org.smu.randsome.randsomeback.global.entity.BaseEntity;

@Table(
        uniqueConstraints = @UniqueConstraint(name = "UK_TICKET_MEMBER_TICKET_TYPE", columnNames = {"member_id", "ticket_type"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Ticket extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketType ticketType;

    @Embedded
    private TicketQuantity quantity;

    @Version
    private Long version;

    public static Ticket create(Member member, TicketType ticketType, int quantity) {
        Ticket ticket = new Ticket();

        ticket.member = requireNonNull(member);
        ticket.ticketType = requireNonNull(ticketType);
        ticket.quantity = TicketQuantity.initial(quantity);

        return ticket;
    }

    public void earn(int amount) {
        this.quantity = this.quantity.plus(amount);
    }

    public void refund(int amount) {
        this.quantity = this.quantity.plus(amount);
    }

    public void use(int amount) {
        this.quantity = this.quantity.minus(amount);
    }

    public int getQuantityValue() {
        return quantity.value();
    }

}