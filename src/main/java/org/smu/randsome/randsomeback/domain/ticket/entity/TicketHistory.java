package org.smu.randsome.randsomeback.domain.ticket.entity;

import static java.util.Objects.requireNonNull;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.smu.randsome.randsomeback.domain.member.entity.Member;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketActionType;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketSource;
import org.smu.randsome.randsomeback.domain.ticket.enums.TicketType;
import org.smu.randsome.randsomeback.global.entity.BaseEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class TicketHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketType ticketType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketActionType actionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketSource source;

    private int amount;

    @Column(nullable = false, length = 100)
    private String description;

    public static TicketHistory register(
            Member member,
            TicketType ticketType,
            TicketActionType actionType,
            TicketSource source,
            int amount,
            String description
    ) {
        TicketHistory ticketHistory = new TicketHistory();
        ticketHistory.member = requireNonNull(member);
        ticketHistory.ticketType = requireNonNull(ticketType);
        ticketHistory.actionType = requireNonNull(actionType);
        ticketHistory.source = requireNonNull(source);
        ticketHistory.amount = amount;
        ticketHistory.description = (description != null && !description.isBlank())
                ? description : source.getDescription();

        return ticketHistory;
    }

}
