package org.smu.randsome.randsomeback.domain.ticket.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.smu.randsome.randsomeback.global.support.error.CoreException;

class TicketQuantityTest {

    @Test
    void 티켓_수량을_생성할_수_있다() {
        // given
        int value = 10;

        // when
        TicketQuantity quantity = TicketQuantity.initial(value);

        // then
        assertThat(quantity.value()).isEqualTo(value);
    }

    @Test
    void 음수_수량으로_티켓_수량을_생성하면_예외가_발생한다() {
        // given
        int negativeValue = -1;

        // when & then
        assertThatThrownBy(() -> TicketQuantity.initial(negativeValue))
                .isInstanceOf(CoreException.class);
    }

    @Test
    void 티켓_수량을_더할_수_있다() {
        // given
        TicketQuantity quantity = TicketQuantity.initial(10);

        // when
        TicketQuantity result = quantity.plus(5);

        // then
        assertThat(result.value()).isEqualTo(15);
    }

    @Test
    void 티켓_수량을_뺄_수_있다() {
        // given
        TicketQuantity quantity = TicketQuantity.initial(10);

        // when
        TicketQuantity result = quantity.minus(4);

        // then
        assertThat(result.value()).isEqualTo(6);
    }

    @Test
    void 보유한_수량보다_더_많은_수량을_빼면_예외가_발생한다() {
        // given
        TicketQuantity quantity = TicketQuantity.initial(3);

        // when & then
        assertThatThrownBy(() -> quantity.minus(5))
                .isInstanceOf(CoreException.class);
    }

}